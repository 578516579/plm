<!--
  效能分析 — PRD §F6 + 原型 analytics.html
  严格对齐: 顶部 4 指标卡 + 各阶段 AI 提效 + 项目健康度 + AI 改进建议
-->
<template>
  <div class="app-container an-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📈 效能分析</h2>
        <p class="page-subtitle">AI 驱动的产研效能洞察与持续改进建议</p>
      </div>
      <div class="header-actions">
        <el-select v-model="periodFilter" style="width: 130px" @change="getList">
          <el-option label="本月" value="month" />
          <el-option label="本季度" value="quarter" />
          <el-option label="本年" value="year" />
        </el-select>
        <el-button plain @click="openAdd"><el-icon><Plus /></el-icon>&nbsp;新建快照</el-button>
        <el-button type="success" :loading="aiLoading" :disabled="!current.snapshotId" @click="triggerAi">
          <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成改进建议
        </el-button>
      </div>
    </div>

    <!-- 4 指标卡 (对齐原型 .grid4) -->
    <el-row :gutter="14" class="stat-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">需求吞吐量</div>
          <div class="stat-value g">{{ current.reqThroughput || 0 }}</div>
          <div class="stat-detail up">↑ 较上月 +18%</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">迭代准时率</div>
          <div class="stat-value blue">{{ current.sprintOnTimeRate || 0 }}%</div>
          <div class="stat-detail up">↑ 较上季 +12%</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">缺陷密度</div>
          <div class="stat-value am">{{ current.defectDensity || 0 }}</div>
          <div class="stat-detail up">条/千行 ↓ 改善</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">AI 节省工时</div>
          <div class="stat-value pu">{{ current.aiSavedHours || 0 }}h</div>
          <div class="stat-detail up">{{ periodLabel }} 累计</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 阶段 AI 提效 + 项目健康度 -->
    <el-row :gutter="20" style="margin-top: 14px">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="card-title">📊 各阶段 AI 提效分析</span></template>
          <div v-if="!stageEntries.length" class="empty-state">
            <el-icon :size="40" color="#9ca3af"><DataAnalysis /></el-icon>
            <p>选择快照后展示各阶段提效数据</p>
          </div>
          <div v-else>
            <div v-for="s in stageEntries" :key="s.stage" class="stage-row">
              <div class="stage-label">{{ s.stage }}</div>
              <el-progress
                :percentage="Math.min(100, Number(s.improvement || 0))"
                :stroke-width="14"
                :format="(p: number) => `${p}% 提效`"
                :color="stageColor(Number(s.improvement || 0))"
              />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="card-title">🚥 项目健康度评分</span></template>
          <div v-if="!healthEntries.length" class="empty-state">
            <el-icon :size="40" color="#9ca3af"><Histogram /></el-icon>
            <p>选择快照后展示项目健康度</p>
          </div>
          <div v-else>
            <div v-for="h in healthEntries" :key="h.name" class="health-row">
              <div class="health-name">{{ h.name }}</div>
              <el-progress
                :percentage="Number(h.score || 0)"
                :stroke-width="12"
                :status="healthStatus(Number(h.score || 0))"
                :format="(p: number) => p + ' 分'"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- AI 改进建议 -->
    <el-card shadow="never" style="margin-top: 14px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">🤖 AI 改进建议</span>
          <el-tag v-if="current.aiGenerated === 'Y'" type="success" size="small">已生成</el-tag>
        </div>
      </template>
      <div v-if="!current.aiSuggestions" class="empty-state">
        <p>选择快照后点击「AI 生成改进建议」</p>
      </div>
      <div v-else class="markdown-body" v-html="renderedSuggestions" />
    </el-card>

    <!-- 历史快照 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header><span class="card-title">📚 历史效能快照 ({{ total }})</span></template>
      <el-table v-loading="listLoading" :data="list" stripe @row-click="loadSnapshot" row-class-name="clickable-row">
        <el-table-column label="编号" prop="snapshotNo" width="160" />
        <el-table-column label="期间" prop="periodLabel" width="120" />
        <el-table-column label="需求吞吐" prop="reqThroughput" width="100" align="center" />
        <el-table-column label="准时率" prop="sprintOnTimeRate" width="100" align="center">
          <template #default="{ row }">{{ row.sprintOnTimeRate || 0 }}%</template>
        </el-table-column>
        <el-table-column label="缺陷密度" prop="defectDensity" width="100" align="center" />
        <el-table-column label="AI 节省" prop="aiSavedHours" width="100" align="center">
          <template #default="{ row }">{{ row.aiSavedHours || 0 }}h</template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button link type="success" @click.stop="quickAi(row)">AI</el-button>
            <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建效能快照" width="540px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="期间">
          <el-select v-model="form.period" style="width: 100%">
            <el-option label="月度" value="month" />
            <el-option label="季度" value="quarter" />
            <el-option label="年度" value="year" />
          </el-select>
        </el-form-item>
        <el-form-item label="期间标签">
          <el-input v-model="form.periodLabel" placeholder="如:2026-05" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick, DataAnalysis, Histogram } from '@element-plus/icons-vue'
import {
  listAnalytics, addAnalytics, delAnalytics, aiGenerateAnalytics, getAnalytics,
  type AnalyticsSnapshot, type AnalyticsQuery
} from '@/api/business/analytics'

const dialogVisible = ref(false)
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)
const periodFilter = ref('month')

const emptyForm = (): AnalyticsSnapshot => ({ period: 'month', periodLabel: new Date().toISOString().slice(0, 7) })
const form = reactive<AnalyticsSnapshot>(emptyForm())
const current = reactive<AnalyticsSnapshot>({ })

const list = ref<AnalyticsSnapshot[]>([])
const total = ref(0)
const queryParams = reactive<AnalyticsQuery>({ pageNum: 1, pageSize: 10 })

const periodLabel = computed(() => current.periodLabel || '本月')

const stageEntries = computed(() => {
  try {
    const data = JSON.parse(current.stageEfficiency || '[]')
    return Array.isArray(data) ? data : []
  } catch { return [] }
})

const healthEntries = computed(() => {
  try {
    const data = JSON.parse(current.projectHealthScores || '[]')
    return Array.isArray(data) ? data : []
  } catch { return [] }
})

const renderedSuggestions = computed(() => {
  const md = current.aiSuggestions || ''
  return md
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/^### (.+)$/gm, '<h4>$1</h4>')
    .replace(/^## (.+)$/gm, '<h3>$1</h3>')
    .replace(/^# (.+)$/gm, '<h2>$1</h2>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>\n?)+/g, m => '<ul>' + m + '</ul>')
    .replace(/\n\n/g, '</p><p>').replace(/^([^<\n].+)$/gm, '<p>$1</p>')
})

function stageColor(p: number) {
  if (p >= 70) return '#10b981'
  if (p >= 40) return '#3b82f6'
  return '#f59e0b'
}

function healthStatus(score: number): any {
  if (score >= 85) return 'success'
  if (score >= 60) return 'warning'
  return 'exception'
}

async function getList() {
  listLoading.value = true
  try {
    queryParams.period = periodFilter.value
    const res: any = await listAnalytics(queryParams)
    list.value = res.rows || []; total.value = res.total || 0
    if (list.value.length && !current.snapshotId) Object.assign(current, list.value[0])
  } finally { listLoading.value = false }
}

function openAdd() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function loadSnapshot(row: AnalyticsSnapshot) {
  if (!row.snapshotId) return
  const res: any = await getAnalytics(row.snapshotId)
  if (res.code === 200 && res.data) Object.assign(current, res.data)
}

async function handleSubmit() {
  saving.value = true
  try {
    await addAnalytics(form); ElMessage.success('创建成功')
    dialogVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '创建失败') } finally { saving.value = false }
}

async function triggerAi() {
  if (!current.snapshotId) return
  aiLoading.value = true
  try {
    const res: any = await aiGenerateAnalytics(current.snapshotId)
    if (res.code === 200 && res.data) { Object.assign(current, res.data); ElMessage.success('AI 改进建议已生成'); await getList() }
  } catch (e: any) { ElMessage.error(e?.msg || 'AI 失败') } finally { aiLoading.value = false }
}

async function quickAi(row: AnalyticsSnapshot) {
  if (!row.snapshotId) return
  aiLoading.value = true
  try { await aiGenerateAnalytics(row.snapshotId); ElMessage.success('已生成'); await getList() } finally { aiLoading.value = false }
}

async function handleDelete(row: AnalyticsSnapshot) {
  if (!row.snapshotId) return
  await ElMessageBox.confirm(`删除 "${row.snapshotNo}"?`, '提示', { type: 'warning' })
  await delAnalytics(row.snapshotId); ElMessage.success('删除成功')
  if (current.snapshotId === row.snapshotId) Object.keys(current).forEach(k => delete (current as any)[k])
  await getList()
}

onMounted(() => { getList() })
</script>

<style scoped>
.an-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; align-items: center; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.stat-row :deep(.el-card__body) { padding: 14px 16px; }
.stat-label { color: #6b7280; font-size: 12px; margin-bottom: 5px; }
.stat-value { font-size: 26px; font-weight: 700; margin-bottom: 3px; }
.stat-value.g { color: #2d7a4f; }
.stat-value.blue { color: #3b82f6; }
.stat-value.am { color: #f59e0b; }
.stat-value.pu { color: #7c3aed; }
.stat-detail { font-size: 12px; color: #6b7280; }
.stat-detail.up { color: #10b981; }
.chart-card { min-height: 320px; }
.empty-state { text-align: center; padding: 50px 20px; color: #6b7280; }
.stage-row, .health-row { margin-bottom: 16px; }
.stage-label, .health-name { font-size: 13px; margin-bottom: 5px; color: #374151; }
.clickable-row { cursor: pointer; }
.markdown-body { line-height: 1.7; font-size: 13px; padding: 8px 0; }
:deep(.markdown-body h2) { font-size: 16px; margin: 12px 0 8px; }
:deep(.markdown-body h3) { font-size: 14px; margin: 10px 0 6px; }
:deep(.markdown-body ul) { margin: 6px 0; padding-left: 22px; }
</style>
