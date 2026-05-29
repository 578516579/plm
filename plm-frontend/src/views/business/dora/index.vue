<!--
  DevOps 效能看板 / DORA 指标 — 原型 devops.html
  4 DORA 指标卡 + 趋势 + AI 改进建议
-->
<template>
  <div class="app-container dora-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📊 DevOps 效能看板</h2>
        <p class="page-subtitle">DORA 指标、部署热力图、前置时间拆解,AI 持续改进建议</p>
      </div>
      <div class="header-actions">
        <el-select
          v-model="queryParams.projectId"
          placeholder="选择项目"
          clearable
          filterable
          style="width: 180px"
          @change="getList"
        >
          <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
        </el-select>
        <el-select v-model="periodFilter" style="width: 120px" @change="getList">
          <el-option label="本月" value="month" />
          <el-option label="本季度" value="quarter" />
        </el-select>
        <el-button plain @click="openAdd"><el-icon><Plus /></el-icon>&nbsp;录入指标</el-button>
        <el-button
          type="success"
          :loading="recomputing"
          v-hasPermi="['business:dora:edit']"
          @click="handleRefreshCompute"
        >
          <el-icon><MagicStick /></el-icon>&nbsp;🔄 重新聚合 4 指标
        </el-button>
        <el-button type="primary" @click="getList">
          <el-icon><Refresh /></el-icon>&nbsp;🔄 刷新
        </el-button>
      </div>
    </div>

    <!-- 4 DORA 指标卡 -->
    <el-row :gutter="14" class="dora-row">
      <el-col :span="6">
        <el-card shadow="never" class="dora-card">
          <div class="dora-name">📈 部署频率</div>
          <div class="dora-value g">{{ getMetric('deploy_frequency') }}</div>
          <div class="dora-unit">次/天</div>
          <el-tag :type="levelTag('deploy_frequency')" size="small">{{ levelLabel('deploy_frequency') }}</el-tag>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="dora-card">
          <div class="dora-name">⏱️ 前置时间</div>
          <div class="dora-value blue">{{ getMetric('lead_time') }}</div>
          <div class="dora-unit">小时</div>
          <el-tag :type="levelTag('lead_time')" size="small">{{ levelLabel('lead_time') }}</el-tag>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="dora-card">
          <div class="dora-name">🚨 MTTR</div>
          <div class="dora-value am">{{ getMetric('mttr') }}</div>
          <div class="dora-unit">小时</div>
          <el-tag :type="levelTag('mttr')" size="small">{{ levelLabel('mttr') }}</el-tag>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="dora-card">
          <div class="dora-name">❌ 变更失败率</div>
          <div class="dora-value red">{{ getMetric('change_failure_rate') }}%</div>
          <div class="dora-unit">百分比</div>
          <el-tag :type="levelTag('change_failure_rate')" size="small">{{ levelLabel('change_failure_rate') }}</el-tag>
        </el-card>
      </el-col>
    </el-row>

    <!-- AI 改进建议 -->
    <el-card shadow="never" style="margin-top: 14px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">🤖 AI 持续改进建议</span>
          <AiButton v-if="current.metricId" size="small" :loading="aiLoading" @click="triggerAi">
            AI 建议
          </AiButton>
        </div>
      </template>
      <div v-if="!current.aiSuggestion" class="empty-state">
        <p>点击下方任一指标后,使用「AI 建议」生成改进建议</p>
      </div>
      <div v-else class="suggestion-box">
        <pre>{{ current.aiSuggestion }}</pre>
      </div>
    </el-card>

    <!-- 指标历史 -->
    <el-card shadow="never" style="margin-top: 14px">
      <template #header><span class="card-title">📜 指标历史 ({{ total }})</span></template>
      <el-table v-loading="listLoading" :data="list" stripe highlight-current-row @current-change="onSelect">
        <el-table-column label="期间" prop="periodLabel" width="120" />
        <el-table-column label="指标" prop="metricType" width="180">
          <template #default="{ row }">{{ metricLabel(row.metricType) }}</template>
        </el-table-column>
        <el-table-column label="值" prop="metricValue" width="100" align="center" />
        <el-table-column label="单位" prop="metricUnit" width="80" align="center" />
        <el-table-column label="等级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="rowLevelTag(row.level)" size="small">{{ rowLevelLabel(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="150" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isComputed === 'Y'" type="success" size="small">自动算</el-tag>
            <el-tag v-else type="warning" size="small">人工录</el-tag>
            <div v-if="row.computedAt" class="compute-time">{{ parseTime(row.computedAt, '{m}-{d} {h}:{i}') }}</div>
            <div v-if="row.periodDays" class="compute-time">窗口 {{ row.periodDays }} 天</div>
          </template>
        </el-table-column>
        <el-table-column label="AI 建议" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.aiSuggestion || '(未生成)' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <AiButton link @click.stop="quickAi(row)">AI</AiButton>
            <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="录入 DORA 指标" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="指标类型" prop="metricType" required>
          <el-select v-model="form.metricType" style="width: 100%">
            <el-option label="📈 部署频率" value="deploy_frequency" />
            <el-option label="⏱️ 前置时间" value="lead_time" />
            <el-option label="🚨 MTTR (恢复时间)" value="mttr" />
            <el-option label="❌ 变更失败率" value="change_failure_rate" />
          </el-select>
        </el-form-item>
        <el-form-item label="指标值" prop="metricValue" required>
          <el-input-number v-model="form.metricValue" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单位" prop="metricUnit">
          <el-input v-model="form.metricUnit" placeholder="次/天 / 小时 / %" />
        </el-form-item>
        <el-form-item label="等级" prop="level">
          <el-select v-model="form.level" style="width: 100%">
            <el-option label="🥇 Elite" value="elite" />
            <el-option label="🥈 High" value="high" />
            <el-option label="🥉 Medium" value="medium" />
            <el-option label="⚠️ Low" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="期间" prop="periodLabel">
          <el-input v-model="form.periodLabel" placeholder="如:2026-05" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">录入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, MagicStick } from '@element-plus/icons-vue'
import {
  listDora, addDora, delDora, aiAnalyzeDora, refreshCompute, listProjectsForSelect,
  type DoraMetric, type DoraQuery
} from '@/api/business/dora'
import { metricLabel, rowLevelLabel, rowLevelTag } from './doraDict'
import { parseTime } from '@/utils/plm'

const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)
const recomputing = ref(false)
const periodFilter = ref('month')
const projects = ref<any[]>([])

const emptyForm = (): DoraMetric => ({ metricType: 'deploy_frequency', metricValue: 0, metricUnit: '次/天', level: 'medium', periodLabel: new Date().toISOString().slice(0, 7) })
const form = reactive<DoraMetric>(emptyForm())
const current = reactive<DoraMetric>({})
const rules = {
  metricType: [{ required: true, message: '请选择指标类型', trigger: 'change' }],
  metricValue: [{ required: true, message: '请输入数值', trigger: 'blur' }]
}

const list = ref<DoraMetric[]>([])
const total = ref(0)
const queryParams = reactive<DoraQuery>({ pageNum: 1, pageSize: 50 })

function getMetric(type: string) {
  const m = list.value.find(x => x.metricType === type)
  return m?.metricValue ?? '-'
}
function levelLabel(type: string) {
  const m = list.value.find(x => x.metricType === type)
  return rowLevelLabel(m?.level)
}
function levelTag(type: string): any {
  const m = list.value.find(x => x.metricType === type)
  return rowLevelTag(m?.level)
}

async function getList() {
  listLoading.value = true
  try { const res: any = await listDora(queryParams); list.value = res.rows || []; total.value = res.total || 0 } finally { listLoading.value = false }
}

function onSelect(row: DoraMetric | null) { if (row) Object.assign(current, row) }

function openAdd() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try { await addDora(form); ElMessage.success('录入成功'); dialogVisible.value = false; await getList() }
  catch (e: any) { ElMessage.error(e?.msg || '录入失败') } finally { saving.value = false }
}

async function triggerAi() {
  if (!current.metricId) return
  aiLoading.value = true
  try {
    const res: any = await aiAnalyzeDora(current.metricId)
    if (res.code === 200 && res.data) { Object.assign(current, res.data); ElMessage.success('AI 建议已生成'); await getList() }
  } catch (e: any) { ElMessage.error(e?.msg || 'AI 失败') } finally { aiLoading.value = false }
}

async function quickAi(row: DoraMetric) {
  if (!row.metricId) return
  aiLoading.value = true
  try { await aiAnalyzeDora(row.metricId); ElMessage.success('已生成'); await getList() } finally { aiLoading.value = false }
}

async function handleDelete(row: DoraMetric) {
  if (!row.metricId) return
  await ElMessageBox.confirm(`删除指标 "${metricLabel(row.metricType)} ${row.periodLabel}"?`, '提示', { type: 'warning' })
  await delDora(row.metricId); ElMessage.success('删除成功'); await getList()
}

async function loadProjects() {
  try {
    const res: any = await listProjectsForSelect()
    projects.value = res.rows || []
  } catch { /* ignore */ }
}

async function handleRefreshCompute() {
  if (!queryParams.projectId) {
    ElMessage.warning('请先在顶栏选择项目')
    return
  }
  recomputing.value = true
  try {
    const res: any = await refreshCompute(queryParams.projectId, 30)
    if (res.code === 200) {
      const n = Array.isArray(res.data) ? res.data.length : 4
      ElMessage.success(`已重算 ${n} 个 DORA 指标(窗口 30 天)`)
      await getList()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || '聚合失败')
  } finally {
    recomputing.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadProjects(), getList()])
})
</script>

<style scoped>
.dora-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; align-items: center; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.dora-row :deep(.el-card__body) { padding: 18px 16px; text-align: center; }
.dora-name { color: #6b7280; font-size: 13px; margin-bottom: 8px; font-weight: 600; }
.dora-value { font-size: 32px; font-weight: 700; margin-bottom: 4px; }
.dora-value.g { color: #2d7a4f; }
.dora-value.blue { color: #3b82f6; }
.dora-value.am { color: #f59e0b; }
.dora-value.red { color: #ef4444; }
.dora-unit { color: #9ca3af; font-size: 12px; margin-bottom: 10px; }
.empty-state { text-align: center; padding: 30px; color: #6b7280; }
.suggestion-box pre { white-space: pre-wrap; font-size: 13px; line-height: 1.7; color: #374151; padding: 8px; background: #f9fafb; border-radius: 6px; }
.compute-time { font-size: 10px; color: #9ca3af; margin-top: 2px; }
</style>
