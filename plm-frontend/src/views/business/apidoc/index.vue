<!--
  API 文档 — PRD §F5.4 + 原型 apidoc.html
  左卡接口列表 + 右卡在线调试双卡 + 从代码同步 + 分享链接
-->
<template>
  <div class="app-container apidoc-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📗 API 文档</h2>
        <p class="page-subtitle">从代码注释自动提取,OpenAPI 规范,支持在线调试</p>
      </div>
      <div class="header-actions">
        <el-button type="success" @click="syncAll" :loading="syncLoading">
          <el-icon><MagicStick /></el-icon>&nbsp;✨ 从代码同步 API
        </el-button>
        <el-button plain @click="copyShareLink">
          <el-icon><CopyDocument /></el-icon>&nbsp;🔗 分享链接
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左:接口列表 -->
      <el-col :span="12">
        <el-card shadow="never" class="list-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">🔌 接口列表 ({{ total }})</span>
              <el-input v-model="queryParams.path" placeholder="搜索 path" style="width: 180px" clearable @clear="getList" @keyup.enter="getList" />
            </div>
          </template>
          <el-table v-loading="listLoading" :data="list" stripe highlight-current-row @current-change="onSelect" max-height="500">
            <el-table-column label="" width="60" align="center">
              <template #default="{ row }">
                <el-tag :type="methodTag(row.httpMethod)" size="small" effect="dark">{{ row.httpMethod }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="路径" prop="path" min-width="180" show-overflow-tooltip>
              <template #default="{ row }"><code>{{ row.path }}</code></template>
            </el-table-column>
            <el-table-column label="说明" prop="description" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button link type="danger" @click.stop="handleDelete(row)">删</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
        </el-card>
      </el-col>

      <!-- 右:在线调试 -->
      <el-col :span="12">
        <el-card shadow="never" class="debug-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">🧪 在线调试</span>
              <el-tag v-if="current.apidocId" :type="methodTag(current.httpMethod)" size="small" effect="dark">{{ current.httpMethod }}</el-tag>
            </div>
          </template>

          <div v-if="!current.apidocId" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><Connection /></el-icon>
            <p>请在左侧选择一个接口</p>
          </div>

          <div v-else>
            <el-descriptions :column="1" size="small" border style="margin-bottom: 12px">
              <el-descriptions-item label="编号"><code>{{ current.apidocNo }}</code></el-descriptions-item>
              <el-descriptions-item label="路径"><code>{{ current.path }}</code></el-descriptions-item>
              <el-descriptions-item label="说明">{{ current.description || '-' }}</el-descriptions-item>
              <el-descriptions-item label="版本">{{ current.version || 'v1.0' }}</el-descriptions-item>
            </el-descriptions>

            <el-tabs v-model="debugTab">
              <el-tab-pane label="📥 请求" name="req">
                <strong>Schema:</strong>
                <pre class="json-code">{{ current.requestSchema || '(无)' }}</pre>
                <strong>示例:</strong>
                <pre class="json-code">{{ current.requestExample || '{}' }}</pre>
                <el-divider />
                <strong>调试参数 (JSON):</strong>
                <el-input v-model="debugParams" type="textarea" :rows="4" class="json-input" />
                <el-button type="primary" :loading="debugLoading" @click="runDebug" style="margin-top: 8px">
                  <el-icon><VideoPlay /></el-icon>&nbsp;▶ 发送请求
                </el-button>
              </el-tab-pane>
              <el-tab-pane label="📤 响应" name="resp">
                <strong>Schema:</strong>
                <pre class="json-code">{{ current.responseSchema || '(无)' }}</pre>
                <strong>示例:</strong>
                <pre class="json-code">{{ current.responseExample || '{}' }}</pre>
              </el-tab-pane>
              <el-tab-pane v-if="debugResult" label="✨ 调试结果" name="result">
                <pre class="json-code">{{ debugResult }}</pre>
              </el-tab-pane>
              <el-tab-pane label="⚠️ 错误码" name="errors">
                <pre class="json-code">{{ current.errorCodes || '(无)' }}</pre>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, CopyDocument, Connection, VideoPlay } from '@element-plus/icons-vue'
import {
  listApiDoc, delApiDoc, syncFromCode, debugApi, listProjectsForSelect,
  type ApiDoc, type ApiDocQuery
} from '@/api/business/apidoc'
import { methodTag } from './apiDocDict'

const listLoading = ref(false)
const syncLoading = ref(false)
const debugLoading = ref(false)
const debugTab = ref('req')

const current = reactive<ApiDoc>({ projectId: 0, title: '' })
const list = ref<ApiDoc[]>([])
const total = ref(0)
const queryParams = reactive<ApiDocQuery>({ pageNum: 1, pageSize: 10 })

const debugParams = ref('{}')
const debugResult = ref('')

async function getList() {
  listLoading.value = true
  try { const res: any = await listApiDoc(queryParams); list.value = res.rows || []; total.value = res.total || 0 } finally { listLoading.value = false }
}

function onSelect(row: ApiDoc | null) {
  if (row) { Object.assign(current, row); debugResult.value = '' }
  else Object.keys(current).forEach(k => delete (current as any)[k])
}

async function syncAll() {
  syncLoading.value = true
  try {
    // Mock: 实际接入时调 backend /business/apidoc/sync/code 端点扫描注解
    ElMessage.success('已触发从代码同步 API (mock,实际接入 v0.6 通过 Swagger 反射)')
    await getList()
  } finally { syncLoading.value = false }
}

function copyShareLink() {
  const link = `${window.location.origin}/business/apidoc`
  navigator.clipboard.writeText(link).then(() => ElMessage.success(`已复制 API 文档链接: ${link}`))
}

async function runDebug() {
  if (!current.apidocId) return
  let params: any = {}
  try { params = JSON.parse(debugParams.value || '{}') }
  catch { ElMessage.error('调试参数 JSON 格式错误'); return }
  debugLoading.value = true
  try {
    const res: any = await debugApi(current.apidocId, params)
    debugResult.value = JSON.stringify(res, null, 2)
    debugTab.value = 'result'
    ElMessage.success('请求完成')
  } catch (e: any) {
    debugResult.value = JSON.stringify(e, null, 2)
    debugTab.value = 'result'
    ElMessage.error(e?.msg || '调试失败')
  } finally { debugLoading.value = false }
}

async function handleDelete(row: ApiDoc) {
  if (!row.apidocId) return
  await ElMessageBox.confirm(`删除 ${row.httpMethod} ${row.path}?`, '提示', { type: 'warning' })
  await delApiDoc(row.apidocId); ElMessage.success('删除成功')
  if (current.apidocId === row.apidocId) Object.keys(current).forEach(k => delete (current as any)[k])
  await getList()
}

onMounted(getList)
</script>

<style scoped>
.apidoc-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.list-card, .debug-card { min-height: 540px; max-height: 580px; overflow: hidden; }
.list-card :deep(.el-card__body), .debug-card :deep(.el-card__body) { max-height: 540px; overflow-y: auto; }
.empty-state { text-align: center; padding: 60px 20px; color: #9ca3af; }
code { background: #f4f4f5; padding: 2px 6px; border-radius: 3px; font-family: monospace; font-size: 12px; }
.json-code {
  background: #1e1e2e; color: #cdd6f4; padding: 10px 12px; border-radius: 6px;
  font-family: 'Consolas', monospace; font-size: 11.5px; line-height: 1.6;
  margin: 6px 0 10px; overflow-x: auto; white-space: pre-wrap;
}
.json-input :deep(.el-textarea__inner) { font-family: 'Consolas', monospace; font-size: 11.5px; }
</style>
