<!--
  AI 调用审计日志 — V3 (2026-05-18)
  对应 tb_ai_invocation_log 表 + AiInvocationLogController
  顶部 Provider 维度汇总卡片 + 列表(可按 caller/provider/success 过滤) + 详情弹窗
-->
<template>
  <div class="app-container audit-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📊 AI 调用审计</h2>
        <p class="page-subtitle">所有走 AiService 的调用都会记录在此 — 用于追踪 token 消耗、成功率、各 provider 表现</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="loadAll">🔄 刷新</el-button>
      </div>
    </div>

    <!-- Provider 维度汇总 -->
    <el-row :gutter="14" class="summary-row" v-if="summary.length">
      <el-col v-for="row in summary" :key="row.provider" :span="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-head">
            <el-tag :type="providerTag(row.provider)" size="small" effect="dark">
              {{ providerLabel(row.provider) }}
            </el-tag>
            <span class="cnt">{{ row.total }} 次</span>
          </div>
          <div class="summary-body">
            <div class="stat-line">
              <span class="lab">成功率</span>
              <strong :class="successColor(row.success_rate)">{{ Number(row.success_rate || 0).toFixed(2) }}%</strong>
            </div>
            <div class="stat-line">
              <span class="lab">总 tokens</span>
              <strong>{{ formatNum(row.total_tokens) }}</strong>
            </div>
            <div class="stat-line">
              <span class="lab">平均延迟</span>
              <strong>{{ row.avg_elapsed_ms || 0 }}ms</strong>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-else description="暂无审计记录,触发任意 aiGenerate/aiAnalyze 后刷新" :image-size="80" style="padding: 30px 0" />

    <!-- 过滤条 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="调用方">
          <el-input v-model="query.callerTag" placeholder="如 ai-agent / inception / dora" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="Provider">
          <el-select v-model="query.provider" placeholder="全部" clearable style="width: 160px">
            <el-option label="Mock"      value="mock" />
            <el-option label="Dify"      value="dify" />
            <el-option label="OpenAI"    value="openai" />
            <el-option label="Anthropic" value="anthropic" />
          </el-select>
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="query.success" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never" style="margin-top: 14px">
      <el-table :data="list" stripe v-loading="loading" @selection-change="onSelectionChange" style="width: 100%">
        <el-table-column type="selection" width="42" />
        <el-table-column prop="logId" label="ID" width="80" />
        <el-table-column prop="callerTag" label="调用方" min-width="180">
          <template #default="{ row }">
            <span class="caller-chip">{{ row.callerTag }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="provider" label="Provider" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="providerTag(row.provider)" effect="plain">{{ providerLabel(row.provider) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="模型" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.model" class="model-chip">{{ row.model }}</span>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="success" label="结果" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.success ? 'success' : 'danger'" effect="dark">
              {{ row.success ? '✓' : '✗' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalTokens" label="Tokens" width="110" align="right">
          <template #default="{ row }">
            <span v-if="row.totalTokens">
              {{ formatNum(row.totalTokens) }}
              <small v-if="row.promptTokens || row.completionTokens" class="token-detail">
                ({{ row.promptTokens || 0 }}+{{ row.completionTokens || 0 }})
              </small>
            </span>
            <span v-else class="muted">0</span>
          </template>
        </el-table-column>
        <el-table-column prop="elapsedMs" label="耗时" width="90" align="right">
          <template #default="{ row }">
            <span :class="elapsedClass(row.elapsedMs)">{{ row.elapsedMs || 0 }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="invokedAt" label="时间" width="160">
          <template #default="{ row }">
            <span class="muted">{{ row.invokedAt }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagi-bar">
        <el-button :disabled="!selected.length" type="danger" plain size="small" @click="handleBatchDelete">
          批量删除({{ selected.length }})
        </el-button>
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          :current-page="query.pageNum"
          :page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="onPageChange"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="审计详情" width="640px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="日志 ID">{{ detail.logId }}</el-descriptions-item>
        <el-descriptions-item label="调用方">{{ detail.callerTag }}</el-descriptions-item>
        <el-descriptions-item label="Provider">{{ providerLabel(detail.provider) }}</el-descriptions-item>
        <el-descriptions-item label="模型">{{ detail.model || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结果">
          <el-tag :type="detail.success ? 'success' : 'danger'" size="small">
            {{ detail.success ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="完成原因">{{ detail.finishReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Token 消耗">
          总 {{ detail.totalTokens || 0 }} ( 输入 {{ detail.promptTokens || 0 }} + 输出 {{ detail.completionTokens || 0 }} )
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.elapsedMs || 0 }}ms</el-descriptions-item>
        <el-descriptions-item label="Request ID">
          <code v-if="detail.requestId">{{ detail.requestId }}</code>
          <span v-else class="muted">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="detail.errorMsg">
          <code class="error-code">{{ detail.errorMsg }}</code>
        </el-descriptions-item>
        <el-descriptions-item label="调用时间">{{ detail.invokedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listAiInvocationLog, getAiInvocationLog, delAiInvocationLog, getProviderSummary,
  type AiInvocationLog, type AiInvocationLogQuery, type ProviderSummaryRow
} from '@/api/business/ai-invocation-log'

const list = ref<AiInvocationLog[]>([])
const total = ref(0)
const loading = ref(false)
const summary = ref<ProviderSummaryRow[]>([])
const selected = ref<AiInvocationLog[]>([])

const detailVisible = ref(false)
const detail = ref<AiInvocationLog | null>(null)

const query = reactive<AiInvocationLogQuery>({
  pageNum: 1,
  pageSize: 20,
  callerTag: '',
  provider: '',
  success: null
})

async function getList() {
  loading.value = true
  try {
    const params = { ...query, success: query.success === null ? undefined : query.success }
    const res: any = await listAiInvocationLog(params)
    list.value = res.rows || []
    total.value = res.total || 0
  } catch (e: any) {
    ElMessage.error(e?.msg || '查询失败')
  } finally { loading.value = false }
}

async function loadSummary() {
  try {
    const res: any = await getProviderSummary()
    if (res.code === 200 && Array.isArray(res.data)) {
      summary.value = res.data as ProviderSummaryRow[]
    }
  } catch { /* 静默 */ }
}

async function loadAll() { await Promise.all([getList(), loadSummary()]) }

function resetQuery() {
  query.callerTag = ''
  query.provider = ''
  query.success = null
  query.pageNum = 1
  getList()
}
function onPageChange(p: number) { query.pageNum = p; getList() }
function onSizeChange(s: number) { query.pageSize = s; query.pageNum = 1; getList() }
function onSelectionChange(rows: AiInvocationLog[]) { selected.value = rows }

async function showDetail(row: AiInvocationLog) {
  if (!row.logId) return
  const res: any = await getAiInvocationLog(row.logId)
  if (res.code === 200) { detail.value = res.data; detailVisible.value = true }
}

async function handleDelete(row: AiInvocationLog) {
  if (!row.logId) return
  await ElMessageBox.confirm(`删除审计 #${row.logId}?`, '提示', { type: 'warning' })
  await delAiInvocationLog(row.logId)
  ElMessage.success('删除成功')
  await loadAll()
}

async function handleBatchDelete() {
  if (!selected.value.length) return
  const ids = selected.value.map(r => r.logId!).filter(Boolean)
  await ElMessageBox.confirm(`删除 ${ids.length} 条审计记录?`, '提示', { type: 'warning' })
  await delAiInvocationLog(ids)
  ElMessage.success('批量删除成功')
  await loadAll()
}

function providerLabel(p?: string) {
  return ({ mock: 'Mock', dify: 'Dify', openai: 'OpenAI 兼容', anthropic: 'Anthropic' } as Record<string,string>)[p || ''] || p || '-'
}
function providerTag(p?: string): any {
  return ({ mock: 'info', dify: 'primary', openai: 'success', anthropic: 'warning' } as Record<string,string>)[p || ''] || 'info'
}
function successColor(rate: number) {
  const r = Number(rate || 0)
  if (r >= 99) return 'green-strong'
  if (r >= 90) return 'green-mid'
  if (r >= 70) return 'yellow'
  return 'red'
}
function elapsedClass(ms: number) {
  if (!ms) return ''
  if (ms < 200) return 'fast'
  if (ms < 3000) return 'normal'
  return 'slow'
}
function formatNum(n: number | undefined): string {
  if (!n) return '0'
  return n.toLocaleString('en-US')
}

onMounted(loadAll)
</script>

<style scoped>
.audit-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 16px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; }

.summary-row :deep(.el-card__body) { padding: 14px; }
.summary-card { transition: transform 0.15s; }
.summary-card:hover { transform: translateY(-2px); }
.summary-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.cnt { color: #6b7280; font-size: 12px; }
.summary-body .stat-line { display: flex; justify-content: space-between; padding: 3px 0; font-size: 13px; }
.stat-line .lab { color: #6b7280; }
.stat-line strong { color: #111827; font-size: 14px; }
.green-strong { color: #059669; }
.green-mid    { color: #10b981; }
.yellow       { color: #d97706; }
.red          { color: #dc2626; }

.filter-card { margin-top: 14px; }
.filter-card :deep(.el-card__body) { padding: 14px 16px; }
.filter-card :deep(.el-form-item) { margin-bottom: 0; }

.caller-chip { font-family: 'Courier New', monospace; font-size: 12px; color: #4b5563; }
.model-chip { font-family: 'Courier New', monospace; font-size: 12px; color: #4b5563; background: #f3f4f6; padding: 2px 6px; border-radius: 4px; }
.token-detail { color: #9ca3af; font-size: 11px; margin-left: 4px; }
.muted { color: #9ca3af; }
.fast { color: #059669; }
.normal { color: #2563eb; }
.slow { color: #dc2626; font-weight: 600; }
.error-code { color: #dc2626; font-size: 12px; word-break: break-all; }

.pagi-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 14px; }
</style>
