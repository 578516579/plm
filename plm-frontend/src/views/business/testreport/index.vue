<!--
  测试报告 — PRD §F4.7 + 原型 testreport.html
  上线风险评级 🟢/🟡/🔴 + P0/P1/P2 缺陷统计 + AI 自动生成 + AI 推荐
-->
<template>
  <div class="app-container testreport-page">
    <!-- 顶栏 (对齐原型 .ph) -->
    <div class="page-header">
      <div>
        <h2 class="page-title">📊 测试报告</h2>
        <p class="page-subtitle">AI 自动生成测试报告,输出上线风险评级 (绿/黄/红)</p>
      </div>
      <div class="btn-row">
        <el-button plain :disabled="!current.testreportId" @click="exportMarkdown">
          <el-icon><Download /></el-icon>&nbsp;导出 Markdown
        </el-button>
        <el-button type="primary" @click="newReport">
          <el-icon><Plus /></el-icon>&nbsp;新建报告
        </el-button>
      </div>
    </div>

    <!-- 顶部:风险评级大徽章 (对齐原型 testReportContent 焦点) -->
    <el-card v-if="current.testreportId" shadow="never" class="risk-banner" :class="riskCls">
      <div class="risk-content">
        <div class="risk-icon">{{ riskIcon }}</div>
        <div class="risk-main">
          <div class="risk-label">{{ riskLabel }}</div>
          <div class="risk-sub">{{ current.testreportNo }} · {{ current.title }}</div>
        </div>
        <div class="risk-meta">
          <div class="risk-coverage">
            <div class="risk-coverage-label">覆盖率</div>
            <div class="risk-coverage-val">{{ formatPct(current.coverageRate) }}</div>
          </div>
          <el-divider direction="vertical" />
          <div class="risk-pass">
            <div class="risk-coverage-label">通过率</div>
            <div class="risk-coverage-val">{{ passRate }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" class="mt-md">
      <!-- 左卡: 报告表单 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">
                {{ current.testreportId ? `📝 报告 ${current.testreportNo}` : '➕ 新建测试报告' }}
              </span>
              <el-tag :type="statusTag.type" size="small">{{ statusTag.label }}</el-tag>
            </div>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-form-item label="关联项目" prop="projectId" required>
              <el-select v-model="form.projectId" placeholder="选择项目" filterable style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>

            <el-form-item label="报告标题" prop="title" required>
              <el-input v-model="form.title" placeholder="如:Sprint 26W21 测试报告" maxlength="200" />
            </el-form-item>

            <el-divider content-position="left">📈 测试统计</el-divider>

            <el-row :gutter="10">
              <el-col :span="8">
                <el-form-item label="用例总数" prop="totalCases">
                  <el-input-number v-model="form.totalCases" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="通过数" prop="passedCases">
                  <el-input-number v-model="form.passedCases" :min="0" :max="form.totalCases" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="失败数" prop="failedCases">
                  <el-input-number v-model="form.failedCases" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="覆盖率 %" prop="coverageRate">
              <div style="display: flex; align-items: center; gap: 12px; width: 100%">
                <el-input-number v-model="form.coverageRate" :min="0" :max="100" :precision="2" style="width: 180px" controls-position="right" />
                <el-progress :percentage="form.coverageRate || 0" :stroke-width="14" style="flex: 1" :status="coverageStatus" />
              </div>
            </el-form-item>

            <el-divider content-position="left">🐛 缺陷统计</el-divider>

            <el-row :gutter="10">
              <el-col :span="8">
                <el-form-item label="P0 致命" prop="p0Defects">
                  <el-input-number v-model="form.p0Defects" :min="0" style="width: 100%" controls-position="right" />
                  <span class="hint-text danger" v-if="(form.p0Defects || 0) > 0">⚠️ P0 缺陷 = 红灯</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="P1 严重" prop="p1Defects">
                  <el-input-number v-model="form.p1Defects" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="P2 一般" prop="p2Defects">
                  <el-input-number v-model="form.p2Defects" :min="0" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="缺陷分布" prop="defectSummary">
              <el-input v-model="form.defectSummary" type="textarea" :rows="2" placeholder="如:按模块分布 / 按发现阶段分布……" />
            </el-form-item>

            <el-divider content-position="left">🎯 风险评级 (PRD §F4.7)</el-divider>

            <el-form-item label="风险等级" prop="riskLevel">
              <el-radio-group v-model="form.riskLevel">
                <el-radio-button value="green">🟢 绿灯 (可发布)</el-radio-button>
                <el-radio-button value="yellow">🟡 黄灯 (需谨慎)</el-radio-button>
                <el-radio-button value="red">🔴 红灯 (禁止上线)</el-radio-button>
              </el-radio-group>
              <div class="hint-text" style="margin-top: 6px">{{ riskHint }}</div>
            </el-form-item>

            <el-form-item label="风险评估" prop="riskEvaluation">
              <el-input v-model="form.riskEvaluation" type="textarea" :rows="3" placeholder="详细评估,如:2 P1 缺陷待修复,核心流程未完全覆盖……" />
            </el-form-item>

            <el-form-item label="AI 建议" prop="recommendations">
              <el-input v-model="form.recommendations" type="textarea" :rows="3" placeholder="如:上线 24h 监控 / 灰度发布 5% 流量 / 回归测试通过后再扩量……" />
            </el-form-item>

            <el-form-item>
              <div class="btn-row">
                <el-button type="primary" :loading="saving" @click="handleSubmit">
                  <el-icon><DocumentChecked /></el-icon>&nbsp;
                  {{ current.testreportId ? '更新' : '保存草稿' }}
                </el-button>
                <el-button v-if="current.testreportId && current.status !== '02'" type="warning" @click="submitReview">
                  📝 提交评审 (→01)
                </el-button>
                <el-button v-if="current.testreportId && current.status === '01'" type="success" @click="publish">
                  ✅ 发布报告 (→02)
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右卡: 缺陷分布饼图风格 + AI 建议 -->
      <el-col :span="10">
        <el-card shadow="never" class="defect-card">
          <template #header>
            <span class="card-title">🐛 缺陷分布</span>
          </template>
          <div class="defect-bars">
            <div class="defect-bar p0">
              <div class="defect-label">P0 致命</div>
              <div class="defect-bar-wrap"><div class="defect-fill" :style="{ width: defectPct.p0 + '%' }" /></div>
              <div class="defect-count">{{ form.p0Defects || 0 }}</div>
            </div>
            <div class="defect-bar p1">
              <div class="defect-label">P1 严重</div>
              <div class="defect-bar-wrap"><div class="defect-fill" :style="{ width: defectPct.p1 + '%' }" /></div>
              <div class="defect-count">{{ form.p1Defects || 0 }}</div>
            </div>
            <div class="defect-bar p2">
              <div class="defect-label">P2 一般</div>
              <div class="defect-bar-wrap"><div class="defect-fill" :style="{ width: defectPct.p2 + '%' }" /></div>
              <div class="defect-count">{{ form.p2Defects || 0 }}</div>
            </div>
          </div>
          <el-divider />

          <h4 class="time-title">⚖️ 评级标准 (PRD §F4.7)</h4>
          <ul class="risk-rules">
            <li>🟢 <strong>绿灯</strong>:P0=0 AND P1≤2 AND 覆盖率≥80%</li>
            <li>🟡 <strong>黄灯</strong>:P0=0 AND (P1≤5 OR 覆盖率≥60%)</li>
            <li>🔴 <strong>红灯</strong>:P0&gt;0 OR P1&gt;5 OR 覆盖率&lt;60%</li>
          </ul>
          <el-button size="small" plain @click="autoRiskLevel" style="margin-top: 8px">
            🤖 按规则自动评级
          </el-button>

          <el-divider />

          <h4 class="time-title">📅 时间线</h4>
          <el-timeline>
            <el-timeline-item v-if="current.generatedAt" type="primary" :timestamp="current.generatedAt" placement="top">
              <strong>AI 生成</strong>
            </el-timeline-item>
            <el-timeline-item v-if="current.status === '02'" type="success" :timestamp="current.updateTime" placement="top">
              <strong>已发布</strong>
              <p>报告状态 02</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部: 测试报告列表 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">📋 测试报告列表 ({{ total }})</span>
          <el-input v-model="queryParams.title" placeholder="搜索标题" style="width: 240px" clearable @clear="getList" @keyup.enter="getList">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </template>
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="testreportNo" width="160" />
        <el-table-column label="报告标题" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="覆盖率" width="100" align="center">
          <template #default="{ row }">{{ formatPct(row.coverageRate) }}</template>
        </el-table-column>
        <el-table-column label="P0/P1/P2" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #ef4444">{{ row.p0Defects || 0 }}</span> /
            <span style="color: #f59e0b">{{ row.p1Defects || 0 }}</span> /
            <span style="color: #6b7280">{{ row.p2Defects || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="riskTagFor(row.riskLevel).type" size="small">{{ riskTagFor(row.riskLevel).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadReport(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, DocumentChecked, Search, Download } from '@element-plus/icons-vue'
import {
  listTestReport, getTestReport, addTestReport, updateTestReport, delTestReport,
  listProjectsForSelect, type TestReport, type TestReportQuery
} from '@/api/business/testreport'

const formRef = ref()
const saving = ref(false)
const listLoading = ref(false)
const projects = ref<any[]>([])
const list = ref<TestReport[]>([])
const total = ref(0)

const emptyForm = (): TestReport => ({
  title: '',
  totalCases: 0, passedCases: 0, failedCases: 0,
  coverageRate: 0,
  p0Defects: 0, p1Defects: 0, p2Defects: 0,
  riskLevel: 'green'
})
const form = reactive<TestReport>(emptyForm())
const current = reactive<TestReport>({ title: '' })
const queryParams = reactive<TestReportQuery>({ pageNum: 1, pageSize: 10, title: '' })

const rules = {
  title: [{ required: true, message: '请输入报告标题', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }]
}

// === 状态 + 风险标签 ===
const statusMap: Record<string, { label: string; type: any }> = {
  '00': { label: '草稿', type: 'info' },
  '01': { label: '审核中', type: 'warning' },
  '02': { label: '已发布', type: 'success' }
}
function statusTagFor(s?: string) { return statusMap[s || '00'] || { label: s, type: 'info' } }
const statusTag = computed(() => statusTagFor(current.status))

const riskMap: Record<string, { label: string; type: any }> = {
  green:  { label: '🟢 绿灯', type: 'success' },
  yellow: { label: '🟡 黄灯', type: 'warning' },
  red:    { label: '🔴 红灯', type: 'danger' }
}
function riskTagFor(s?: string) { return riskMap[s || 'green'] || { label: s, type: 'info' } }

const riskLevel = computed(() => current.testreportId ? current.riskLevel : form.riskLevel)
const riskIcon = computed(() => ({ green: '🟢', yellow: '🟡', red: '🔴' } as any)[riskLevel.value || 'green'] || '⚪')
const riskLabel = computed(() => ({
  green: '绿灯 - 可以发布',
  yellow: '黄灯 - 需谨慎,建议二次评审',
  red: '红灯 - 禁止上线'
} as any)[riskLevel.value || 'green'] || '未评级')
const riskCls = computed(() => `risk-${riskLevel.value || 'green'}`)

const riskHint = computed(() => ({
  green: '所有指标达标,可走标准发布流程',
  yellow: '存在风险,建议增加灰度观察期或补充测试',
  red: '必须先修复 P0 / 提升覆盖率才能上线'
} as any)[form.riskLevel || 'green'])

const coverageStatus = computed(() => {
  const c = form.coverageRate || 0
  if (c >= 80) return 'success' as const
  if (c >= 60) return 'warning' as const
  return 'exception' as const
})

const passRate = computed(() => {
  const t = form.totalCases || 0
  const p = form.passedCases || 0
  if (t === 0) return '—'
  return `${((p / t) * 100).toFixed(1)}%`
})

const defectPct = computed(() => {
  const max = Math.max(form.p0Defects || 0, form.p1Defects || 0, form.p2Defects || 0, 1)
  return {
    p0: ((form.p0Defects || 0) / max) * 100,
    p1: ((form.p1Defects || 0) / max) * 100,
    p2: ((form.p2Defects || 0) / max) * 100
  }
})

function formatPct(v?: number) {
  if (v == null) return '—'
  return `${Number(v).toFixed(1)}%`
}

// === 按规则自动评级 (PRD §F4.7) ===
function autoRiskLevel() {
  const p0 = form.p0Defects || 0
  const p1 = form.p1Defects || 0
  const cov = form.coverageRate || 0
  let level = 'green'
  if (p0 > 0 || p1 > 5 || cov < 60) level = 'red'
  else if (p1 > 2 || cov < 80) level = 'yellow'
  form.riskLevel = level
  ElMessage.success(`已按规则评级: ${riskTagFor(level).label}`)
}

// === CRUD ===
async function getList() {
  listLoading.value = true
  try {
    const res: any = await listTestReport(queryParams)
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
    if (current.testreportId) {
      await updateTestReport({ ...form, testreportId: current.testreportId })
      ElMessage.success('更新成功')
    } else {
      const res: any = await addTestReport(form)
      if (res.code === 200) {
        ElMessage.success('保存成功')
        await getList()
        const latest = list.value.find(x => x.title === form.title)
        if (latest?.testreportId) Object.assign(current, latest)
      }
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || '保存失败')
  } finally {
    saving.value = false
  }
}

async function submitReview() {
  if (!current.testreportId) return
  const res: any = await updateTestReport({ testreportId: current.testreportId, title: current.title, status: '01' })
  if (res.code === 200) {
    Object.assign(current, { status: '01' })
    ElMessage.success('已提交评审')
    await getList()
  }
}

async function publish() {
  if (!current.testreportId) return
  if (current.riskLevel === 'red') {
    await ElMessageBox.confirm('当前为🔴红灯,确认仍要发布报告?(此操作不影响发布单状态)', '警告', { type: 'warning' })
  }
  const res: any = await updateTestReport({ testreportId: current.testreportId, title: current.title, status: '02' })
  if (res.code === 200) {
    Object.assign(current, { status: '02' })
    ElMessage.success('已发布')
    await getList()
  }
}

async function loadReport(row: TestReport) {
  if (!row.testreportId) return
  const res: any = await getTestReport(row.testreportId)
  if (res.code === 200 && res.data) {
    Object.assign(form, res.data)
    Object.assign(current, res.data)
    ElMessage.info(`已载入 ${res.data.testreportNo}`)
  }
}

async function handleDelete(row: TestReport) {
  if (!row.testreportId) return
  await ElMessageBox.confirm(`确认删除报告 "${row.testreportNo}"?`, '提示', { type: 'warning' })
  await delTestReport(row.testreportId)
  ElMessage.success('删除成功')
  if (current.testreportId === row.testreportId) newReport()
  await getList()
}

function newReport() {
  Object.assign(form, emptyForm())
  Object.keys(current).forEach(k => delete (current as any)[k])
  formRef.value?.clearValidate()
}

function exportMarkdown() {
  const md = `# ${current.title}\n\n` +
    `**编号**: ${current.testreportNo}\n` +
    `**风险评级**: ${riskTagFor(current.riskLevel).label}\n\n` +
    `## 测试统计\n` +
    `- 用例总数: ${current.totalCases}\n` +
    `- 通过 / 失败: ${current.passedCases} / ${current.failedCases}\n` +
    `- 覆盖率: ${formatPct(current.coverageRate)}\n\n` +
    `## 缺陷分布\n` +
    `- P0 致命: ${current.p0Defects}\n` +
    `- P1 严重: ${current.p1Defects}\n` +
    `- P2 一般: ${current.p2Defects}\n\n` +
    `## 风险评估\n${current.riskEvaluation || '—'}\n\n` +
    `## AI 建议\n${current.recommendations || '—'}\n`
  const blob = new Blob([md], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${current.testreportNo || 'TR'}.md`
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  await loadProjects()
  await getList()
})
</script>

<style scoped>
.testreport-page { padding: 20px; }
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

/* 风险评级 Banner */
.risk-banner.risk-green  :deep(.el-card__body) { background: linear-gradient(135deg, #d1fae5 0%, #ecfdf5 100%); }
.risk-banner.risk-yellow :deep(.el-card__body) { background: linear-gradient(135deg, #fef3c7 0%, #fffbeb 100%); }
.risk-banner.risk-red    :deep(.el-card__body) { background: linear-gradient(135deg, #fee2e2 0%, #fef2f2 100%); }
.risk-content { display: flex; align-items: center; gap: 24px; padding: 8px 16px; }
.risk-icon { font-size: 56px; }
.risk-main { flex: 1; }
.risk-label { font-size: 22px; font-weight: 700; color: #111827; }
.risk-sub { font-size: 12px; color: #6b7280; margin-top: 4px; }
.risk-meta { display: flex; align-items: center; gap: 12px; }
.risk-coverage, .risk-pass { text-align: center; }
.risk-coverage-label { font-size: 11px; color: #6b7280; }
.risk-coverage-val { font-size: 20px; font-weight: 700; color: #111827; margin-top: 2px; }

/* Defect Bars */
.defect-card { min-height: 580px; }
.defect-bars { display: flex; flex-direction: column; gap: 12px; }
.defect-bar { display: grid; grid-template-columns: 80px 1fr 40px; gap: 8px; align-items: center; }
.defect-label { font-size: 12px; font-weight: 600; }
.defect-bar.p0 .defect-label { color: #ef4444; }
.defect-bar.p1 .defect-label { color: #f59e0b; }
.defect-bar.p2 .defect-label { color: #6b7280; }
.defect-bar-wrap { background: #f3f4f6; height: 18px; border-radius: 4px; overflow: hidden; }
.defect-fill { height: 100%; transition: width 0.3s; }
.defect-bar.p0 .defect-fill { background: linear-gradient(90deg, #ef4444, #fca5a5); }
.defect-bar.p1 .defect-fill { background: linear-gradient(90deg, #f59e0b, #fcd34d); }
.defect-bar.p2 .defect-fill { background: linear-gradient(90deg, #6b7280, #d1d5db); }
.defect-count { font-weight: 700; text-align: right; }

/* 评级标准 */
.risk-rules { list-style: none; padding: 0; margin: 0; font-size: 12px; }
.risk-rules li { padding: 4px 0; line-height: 1.5; }

.time-title { font-size: 13px; margin: 8px 0; color: #374151; font-weight: 600; }
.hint-text { font-size: 11px; color: #6b7280; }
.hint-text.danger { color: #ef4444; }
</style>
