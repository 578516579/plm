<!--
  发布管理 — 原型 release.html (DevOps 扩展)
  蓝绿/金丝雀/滚动 + DORA 4 指标 + 5 态状态机 + 一键回滚
-->
<template>
  <div class="app-container release-page">
    <!-- 顶栏 (对齐原型 .ph) -->
    <div class="page-header">
      <div>
        <h2 class="page-title">🎯 发布管理</h2>
        <p class="page-subtitle">蓝绿 / 金丝雀 / 滚动发布策略,AI 发布评审,一键回滚</p>
      </div>
      <div class="btn-row">
        <el-button type="success" plain :disabled="!current.releaseId" @click="aiReview">
          <el-icon><MagicStick /></el-icon>&nbsp;AI 发布评审
        </el-button>
        <el-button type="primary" @click="newRelease">
          <el-icon><Plus /></el-icon>&nbsp;新建发布单
        </el-button>
      </div>
    </div>

    <!-- DORA 4 指标 (对齐原型 .g4) -->
    <el-row :gutter="12" class="dora-row">
      <el-col :span="6" v-for="m in doraMetrics" :key="m.key">
        <el-card shadow="never" :body-style="{ padding: '14px' }" class="dora-card" :class="m.cls">
          <div class="dora-label">{{ m.label }}</div>
          <div class="dora-value" :style="{ color: m.color }">{{ m.value }}</div>
          <div class="dora-sub">{{ m.sub }}</div>
          <div class="dora-trend" :class="m.trend">{{ m.trendText }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-md">
      <!-- 左卡: 发布单基本信息 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">
                {{ current.releaseId ? `🎯 发布单 ${current.releaseNo}` : '➕ 新建发布单' }}
              </span>
              <el-tag :type="statusTag.type" size="small">{{ statusTag.label }}</el-tag>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="关联项目" prop="projectId" required>
              <el-select v-model="form.projectId" placeholder="选择项目" filterable style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>

            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="版本号" prop="version" required>
                  <el-input v-model="form.version" placeholder="如:v2.1.0" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="环境" prop="environment">
                  <el-select v-model="form.environment" placeholder="选择环境" style="width: 100%">
                    <el-option label="开发 (dev)" value="dev" />
                    <el-option label="测试 (test)" value="test" />
                    <el-option label="预发 (staging)" value="staging" />
                    <el-option label="生产 (prod)" value="prod" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="发布策略" prop="strategy">
              <el-radio-group v-model="form.strategy" size="default">
                <el-radio-button value="blue_green">
                  <span class="strategy-icon">🟦</span>&nbsp;蓝绿
                </el-radio-button>
                <el-radio-button value="canary">
                  <span class="strategy-icon">🐤</span>&nbsp;金丝雀
                </el-radio-button>
                <el-radio-button value="rolling">
                  <span class="strategy-icon">🔄</span>&nbsp;滚动
                </el-radio-button>
              </el-radio-group>
              <div class="strategy-hint">{{ strategyHint }}</div>
            </el-form-item>

            <el-form-item label="发布说明" prop="releaseNotes">
              <el-input
                v-model="form.releaseNotes"
                type="textarea" :rows="6"
                placeholder="# 版本变更&#10;&#10;## 新增&#10;- 功能 A&#10;&#10;## 修复&#10;- bug B&#10;&#10;## 影响范围&#10;- 模块 X / Y"
              />
            </el-form-item>

            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="计划时间" prop="plannedAt">
                  <el-date-picker
                    v-model="form.plannedAt"
                    type="datetime"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    placeholder="选择计划发布时间"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="负责人" prop="releasedByUserId">
                  <el-input-number v-model="form.releasedByUserId" :min="1" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item v-if="current.status === '03'" label="回滚原因">
              <el-input
                v-model="form.rollbackReason"
                type="textarea" :rows="2"
                placeholder="回滚必填原因(后端 ServiceException 602)"
              />
            </el-form-item>

            <el-form-item>
              <div class="btn-row">
                <el-button type="primary" :loading="saving" @click="handleSubmit">
                  <el-icon><DocumentChecked /></el-icon>&nbsp;
                  {{ current.releaseId ? '更新' : '保存计划' }}
                </el-button>
                <template v-if="current.releaseId">
                  <el-button v-if="canTransition('01')" type="warning" @click="transition('01')">
                    ▶ 开始发布 (00→01)
                  </el-button>
                  <el-button v-if="canTransition('02')" type="success" @click="transition('02')">
                    ✅ 发布成功 (01→02)
                  </el-button>
                  <el-button v-if="canTransition('03')" type="danger" @click="openRollback">
                    ↩ 回滚 (→03)
                  </el-button>
                  <el-button v-if="canTransition('04')" type="info" @click="confirmDeprecate">
                    🗑 废弃 (→04)
                  </el-button>
                </template>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右卡: AI 评审 + 关键时间线 -->
      <el-col :span="10">
        <el-card shadow="never" class="ai-review-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">🤖 AI 发布评审</span>
              <el-tag v-if="current.aiReviewScore != null"
                :type="aiScoreType"
                size="small">
                {{ current.aiReviewScore.toFixed(0) }} 分
              </el-tag>
            </div>
          </template>

          <div v-if="!current.releaseId" class="empty-state">
            <el-icon :size="36" color="#9ca3af"><Document /></el-icon>
            <p>保存发布单后,点击「AI 发布评审」获取评分与建议</p>
          </div>
          <div v-else-if="!current.aiReviewScore" class="empty-state">
            <el-icon :size="36" color="#f59e0b"><InfoFilled /></el-icon>
            <p>还未评审,点击顶部「✨ AI 发布评审」</p>
          </div>
          <div v-else>
            <el-progress
              :percentage="current.aiReviewScore"
              :stroke-width="14"
              :status="aiScoreStatus"
              :format="(p: number) => `${p.toFixed(0)} / 100`"
              style="margin-bottom: 12px"
            />
            <div v-if="current.aiReviewNotes" class="markdown-body" v-html="renderedNotes" />
          </div>

          <el-divider />

          <h4 class="time-title">📅 关键时间线</h4>
          <el-timeline>
            <el-timeline-item type="info" :timestamp="current.plannedAt || '—'" :hide-timestamp="!current.plannedAt" placement="top">
              <strong>计划时间</strong>
              <p>{{ statusTagFor(current.status).label }}</p>
            </el-timeline-item>
            <el-timeline-item type="warning" :timestamp="current.releasedAt || '—'" :hide-timestamp="!current.releasedAt" placement="top">
              <strong>实际发布</strong>
              <p>01→02 时自动填写</p>
            </el-timeline-item>
            <el-timeline-item v-if="current.rollbackAt" type="danger" :timestamp="current.rollbackAt" placement="top">
              <strong>回滚</strong>
              <p>{{ current.rollbackReason || '—' }}</p>
            </el-timeline-item>
          </el-timeline>

          <el-divider />
          <h4 class="time-title">📊 本单 DORA</h4>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="部署频率">{{ formatNum(current.deploymentFrequency, '次/天') }}</el-descriptions-item>
            <el-descriptions-item label="前置时间">{{ formatNum(current.leadTimeHours, '小时') }}</el-descriptions-item>
            <el-descriptions-item label="MTTR">{{ formatNum(current.mttrMinutes, '分钟') }}</el-descriptions-item>
            <el-descriptions-item label="变更失败率">{{ formatPct(current.changeFailureRate) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部: 发布单列表 (对齐原型 #releaseTable) -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">📋 发布单列表 ({{ total }})</span>
          <el-input
            v-model="queryParams.version" placeholder="搜索版本号" clearable
            style="width: 240px"
            @clear="getList" @keyup.enter="getList"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </template>
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="releaseNo" width="160" />
        <el-table-column label="版本" prop="version" width="120" align="center" />
        <el-table-column label="环境" prop="environment" width="80" align="center" />
        <el-table-column label="策略" width="100" align="center">
          <template #default="{ row }">{{ strategyLabel(row.strategy) }}</template>
        </el-table-column>
        <el-table-column label="AI 评分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.aiReviewScore != null" :style="{ color: row.aiReviewScore >= 80 ? '#10b981' : row.aiReviewScore >= 60 ? '#f59e0b' : '#ef4444' }">
              {{ Number(row.aiReviewScore).toFixed(0) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadRelease(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 回滚 Dialog -->
    <el-dialog v-model="rollbackDialog" title="↩ 回滚发布单" width="500">
      <el-alert type="warning" :closable="false" style="margin-bottom: 12px">
        回滚为反向边操作,需明确原因。回滚成功后 status=03 (已回滚),rollbackAt 自动填写。
      </el-alert>
      <el-input v-model="rollbackReasonInput" type="textarea" :rows="3" placeholder="说明回滚原因…" />
      <template #footer>
        <el-button @click="rollbackDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmRollback">确认回滚</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, MagicStick, DocumentChecked, Search, Document, InfoFilled
} from '@element-plus/icons-vue'
import {
  listRelease, getRelease, addRelease, updateRelease, delRelease,
  listProjectsForSelect, type Release, type ReleaseQuery
} from '@/api/business/release'

const formRef = ref()
const saving = ref(false)
const listLoading = ref(false)
const projects = ref<any[]>([])
const list = ref<Release[]>([])
const total = ref(0)

const emptyForm = (): Release => ({
  version: '', strategy: 'canary', environment: 'staging',
  releaseNotes: '', releasedByUserId: 1
})
const form = reactive<Release>(emptyForm())
const current = reactive<Release>({ version: '' })
const queryParams = reactive<ReleaseQuery>({ pageNum: 1, pageSize: 10, version: '' })

const rollbackDialog = ref(false)
const rollbackReasonInput = ref('')

const rules = {
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }]
}

// === 状态机 (5 态) ===
const statusMap: Record<string, { label: string; type: any }> = {
  '00': { label: '计划中', type: 'info' },
  '01': { label: '发布中', type: 'warning' },
  '02': { label: '已发布', type: 'success' },
  '03': { label: '已回滚', type: 'danger' },
  '04': { label: '已废弃', type: 'info' }
}
function statusTagFor(s?: string) { return statusMap[s || '00'] || { label: s, type: 'info' } }
const statusTag = computed(() => statusTagFor(current.status))

// 5 态合法转换 (含废弃)
const TRANSITIONS: Record<string, string[]> = {
  '00': ['01','04'], '01': ['02','03'], '02': ['03','04'], '03': ['04'], '04': []
}
function canTransition(to: string): boolean {
  const from = current.status || '00'
  return TRANSITIONS[from]?.includes(to) || false
}

// === 策略提示 ===
const strategyHints: Record<string, string> = {
  blue_green: '🟦 蓝绿:同时存在新旧两套环境,流量切换瞬时完成;回滚最快但资源 2x',
  canary: '🐤 金丝雀:先放 5%~10% 流量到新版本,观察 metrics 再渐进扩量',
  rolling: '🔄 滚动:批次替换实例(如每次 25%);资源占用少但回滚慢'
}
const strategyHint = computed(() => strategyHints[form.strategy || ''] || '')
function strategyLabel(s?: string) {
  return ({ blue_green: '蓝绿', canary: '金丝雀', rolling: '滚动' } as Record<string, string>)[s || ''] || s || '-'
}

// === DORA 4 (静态聚合显示;真实数据应来自后端 dora 模块) ===
const doraMetrics = computed(() => {
  // 简易聚合:基于当前列表
  const released = list.value.filter(r => r.status === '02' || r.status === '03')
  const rollback = list.value.filter(r => r.status === '03').length
  const total = list.value.length || 1
  const cfr = (rollback / total) * 100
  const avgLt = released.reduce((s, r) => s + (Number(r.leadTimeHours) || 0), 0) / Math.max(released.length, 1)
  const avgMttr = released.reduce((s, r) => s + (Number(r.mttrMinutes) || 0), 0) / Math.max(released.length, 1)
  return [
    {
      key: 'df', label: '部署频率 (Deployment Frequency)',
      value: `${released.length}`, sub: '本月已发布次数',
      color: '#3b82f6', cls: 'dora-info',
      trend: released.length > 0 ? 'up' : '',
      trendText: released.length > 0 ? '↑ 持续输出' : '—'
    },
    {
      key: 'lt', label: '前置时间 (Lead Time)',
      value: avgLt > 0 ? `${avgLt.toFixed(1)}h` : '—', sub: '需求 → 上线 平均小时',
      color: '#10b981', cls: 'dora-success',
      trend: avgLt < 24 ? 'up' : 'down',
      trendText: avgLt < 24 ? '✓ 高效' : '⚠ 偏慢'
    },
    {
      key: 'mttr', label: 'MTTR (恢复时间)',
      value: avgMttr > 0 ? `${avgMttr.toFixed(0)}m` : '—', sub: '故障平均恢复分钟',
      color: '#f59e0b', cls: 'dora-warning',
      trend: avgMttr < 30 ? 'up' : 'down',
      trendText: avgMttr < 30 ? '✓ 快速恢复' : '⚠ 偏慢'
    },
    {
      key: 'cfr', label: '变更失败率 (CFR)',
      value: `${cfr.toFixed(1)}%`, sub: '回滚次数 / 总发布数',
      color: cfr > 15 ? '#ef4444' : '#10b981',
      cls: cfr > 15 ? 'dora-danger' : 'dora-success',
      trend: cfr < 15 ? 'up' : 'down',
      trendText: cfr < 15 ? '✓ 健康 (<15%)' : '⚠ 超阈值'
    }
  ]
})

// === AI 评审 ===
const aiScoreType = computed(() => {
  const s = current.aiReviewScore || 0
  if (s >= 80) return 'success'
  if (s >= 60) return 'warning'
  return 'danger'
})
const aiScoreStatus = computed(() => {
  const s = current.aiReviewScore || 0
  if (s >= 80) return 'success'
  return s >= 60 ? 'warning' : 'exception'
})
const renderedNotes = computed(() => {
  const md = current.aiReviewNotes || ''
  return md
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/^## (.+)$/gm, '<h3>$1</h3>')
    .replace(/^# (.+)$/gm, '<h2>$1</h2>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>\n?)+/g, m => '<ul>' + m + '</ul>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/^([^<\n].+)$/gm, '<p>$1</p>')
})

// === 工具函数 ===
function formatNum(v?: number, unit = '') {
  if (v == null || v === 0) return '—'
  return `${Number(v).toFixed(1)}${unit ? ' ' + unit : ''}`
}
function formatPct(v?: number) {
  if (v == null) return '—'
  return `${Number(v).toFixed(1)}%`
}

// === API 调用 ===
async function getList() {
  listLoading.value = true
  try {
    const res: any = await listRelease(queryParams)
    list.value = res.rows || []
    total.value = res.total || 0
  } finally {
    listLoading.value = false
  }
}

async function loadProjects() {
  try {
    const res: any = await listProjectsForSelect()
    projects.value = res.rows || []
  } catch { /* ignore */ }
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (current.releaseId) {
      await updateRelease({ ...form, releaseId: current.releaseId })
      ElMessage.success('更新成功')
    } else {
      const res: any = await addRelease(form)
      if (res.code === 200) {
        ElMessage.success('保存成功')
        await getList()
        const latest = list.value.find(x => x.version === form.version && x.projectId === form.projectId)
        if (latest?.releaseId) Object.assign(current, latest)
      } else if (res.code === 701) {
        ElMessage.error('版本号已存在 (UNIQUE 冲突 701)')
      }
    }
  } catch (e: any) {
    if (e?.code === 701) {
      ElMessage.error('版本号已存在 (UNIQUE 冲突 701)')
    } else {
      ElMessage.error(e?.msg || e?.message || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

async function transition(to: string) {
  if (!current.releaseId) return
  try {
    const res: any = await updateRelease({ releaseId: current.releaseId, version: current.version, status: to })
    if (res.code === 200) {
      ElMessage.success(`状态切换至 ${statusTagFor(to).label}`)
      const r = await getRelease(current.releaseId)
      if (r.code === 200) {
        Object.assign(current, r.data)
        Object.assign(form, r.data)
      }
      await getList()
    }
  } catch (e: any) {
    if (e?.code === 601) ElMessage.error('状态转换不合法 (601)')
    else if (e?.code === 602) ElMessage.error('必填字段缺失 (602)')
    else ElMessage.error(e?.msg || '状态转换失败')
  }
}

function openRollback() {
  rollbackReasonInput.value = ''
  rollbackDialog.value = true
}

async function confirmRollback() {
  if (!rollbackReasonInput.value.trim()) {
    ElMessage.warning('请填写回滚原因')
    return
  }
  if (!current.releaseId) return
  try {
    const res: any = await updateRelease({
      releaseId: current.releaseId,
      version: current.version,
      status: '03',
      rollbackReason: rollbackReasonInput.value
    })
    if (res.code === 200) {
      ElMessage.success('已回滚 (→03)')
      rollbackDialog.value = false
      const r = await getRelease(current.releaseId)
      if (r.code === 200) {
        Object.assign(current, r.data)
        Object.assign(form, r.data)
      }
      await getList()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || '回滚失败')
  }
}

async function confirmDeprecate() {
  await ElMessageBox.confirm('确认废弃此发布单?废弃后不可恢复', '提示', { type: 'warning' })
  await transition('04')
}

async function aiReview() {
  if (!current.releaseId) {
    ElMessage.warning('请先保存发布单')
    return
  }
  // 后端没有 ai/review 端点;模拟前端打分 (基于 release notes 长度 + 策略)
  const score = Math.min(95, 60 + (current.releaseNotes?.length || 0) / 30)
  const notes = `## AI 发布评审\n\n` +
    `- **策略**: ${strategyLabel(current.strategy)} (${current.strategy === 'blue_green' ? '✓ 风险最低' : current.strategy === 'canary' ? '✓ 可控渐进' : '⚠ 滚动周期偏长'})\n` +
    `- **环境**: ${current.environment}\n` +
    `- **Release Notes**: ${current.releaseNotes?.length || 0} 字符,${(current.releaseNotes?.length || 0) > 100 ? '✓ 充分' : '⚠ 偏少,建议补充影响范围'}\n` +
    `- **AI 建议**: ${score >= 80 ? '可以发布' : '建议补充测试用例后再上线'}\n`
  try {
    const res: any = await updateRelease({
      releaseId: current.releaseId,
      version: current.version,
      aiReviewScore: score,
      aiReviewNotes: notes
    })
    if (res.code === 200) {
      Object.assign(current, { aiReviewScore: score, aiReviewNotes: notes })
      ElMessage.success(`AI 评分 ${score.toFixed(0)} 已写入`)
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || 'AI 评审失败')
  }
}

async function loadRelease(row: Release) {
  if (!row.releaseId) return
  const res: any = await getRelease(row.releaseId)
  if (res.code === 200 && res.data) {
    Object.assign(form, res.data)
    Object.assign(current, res.data)
    ElMessage.info(`已载入 ${res.data.releaseNo}`)
  }
}

async function handleDelete(row: Release) {
  if (!row.releaseId) return
  await ElMessageBox.confirm(`确认删除发布单 "${row.releaseNo}" (${row.version})?`, '提示', { type: 'warning' })
  await delRelease(row.releaseId)
  ElMessage.success('删除成功')
  if (current.releaseId === row.releaseId) newRelease()
  await getList()
}

function newRelease() {
  Object.assign(form, emptyForm())
  Object.keys(current).forEach(k => delete (current as any)[k])
  formRef.value?.clearValidate()
}

onMounted(async () => {
  await loadProjects()
  await getList()
})
</script>

<style scoped>
.release-page { padding: 20px; }
.page-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 20px;
}
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.btn-row { display: flex; gap: 10px; flex-wrap: wrap; }
.mt-md { margin-top: 16px; }

.dora-row { margin-bottom: 16px; }
.dora-card { transition: transform 0.15s; }
.dora-card:hover { transform: translateY(-2px); }
.dora-card.dora-info    { border-left: 3px solid #3b82f6; }
.dora-card.dora-success { border-left: 3px solid #10b981; }
.dora-card.dora-warning { border-left: 3px solid #f59e0b; }
.dora-card.dora-danger  { border-left: 3px solid #ef4444; }
.dora-label { font-size: 12px; color: #6b7280; }
.dora-value { font-size: 28px; font-weight: 700; margin: 6px 0; }
.dora-sub   { font-size: 11px; color: #9ca3af; }
.dora-trend { font-size: 11px; margin-top: 4px; }
.dora-trend.up   { color: #10b981; }
.dora-trend.down { color: #ef4444; }

.strategy-icon { font-size: 14px; }
.strategy-hint {
  margin-top: 6px; font-size: 12px; color: #6b7280;
  background: #f9fafb; padding: 8px 12px; border-radius: 6px;
}

.ai-review-card { min-height: 580px; }
.empty-state {
  text-align: center; padding: 40px 16px; color: #6b7280;
}
.empty-state p { margin: 12px 0 0; font-size: 13px; }
.time-title { font-size: 13px; margin: 12px 0 8px; color: #374151; }

.markdown-body { font-size: 12px; line-height: 1.7; }
:deep(.markdown-body h2) { font-size: 14px; margin: 8px 0 4px; }
:deep(.markdown-body h3) { font-size: 13px; margin: 6px 0 4px; }
:deep(.markdown-body p)  { margin: 4px 0; }
:deep(.markdown-body ul) { margin: 4px 0; padding-left: 20px; }
:deep(.markdown-body li) { margin: 2px 0; }
</style>
