<!--
  接口详细设计 LLD — PRD §F3.3 + 原型 apidesign.html
  严格对齐: 接口列表 + 接口详情双卡片 + + 手动添加 + AI 生成
-->
<template>
  <div class="app-container api-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🔌 接口详细设计 (LLD)</h2>
        <p class="page-subtitle">AI 生成 OpenAPI 规范接口文档,支持 Mock 服务</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="openAdd"><el-icon><Plus /></el-icon>&nbsp;+ 手动添加接口</el-button>
        <el-button
          type="success"
          :loading="aiLoading"
          :disabled="!current.apidesignId"
          @click="triggerAi"
        >
          <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成接口设计
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左:接口列表 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">🔌 接口列表 ({{ total }})</span>
              <el-input
                v-model="queryParams.path"
                placeholder="搜索 path"
                style="width: 200px"
                clearable
                @clear="getList"
                @keyup.enter="getList"
              />
            </div>
          </template>
          <el-table v-loading="listLoading" :data="list" stripe highlight-current-row @current-change="onSelect" max-height="500">
            <el-table-column label="Method" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="methodTag(row.httpMethod)" size="small" effect="dark">{{ row.httpMethod }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Path" prop="path" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <code>{{ row.path }}</code>
              </template>
            </el-table-column>
            <el-table-column label="说明" prop="description" min-width="120" show-overflow-tooltip />
            <el-table-column label="Mock" width="60" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.mockEnabled === 'Y'" type="success" size="small">ON</el-tag>
                <el-tag v-else type="info" size="small">OFF</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
        </el-card>
      </el-col>

      <!-- 右:接口详情 -->
      <el-col :span="12">
        <el-card shadow="never" class="detail-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">📄 接口详情</span>
              <el-tag v-if="current.apidesignId" :type="statusTag.type" size="small">{{ statusTag.label }}</el-tag>
            </div>
          </template>

          <div v-if="!current.apidesignId" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><Connection /></el-icon>
            <p>请在左侧列表选择一个接口</p>
          </div>

          <div v-else>
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item label="编号"><code>{{ current.apidesignNo }}</code></el-descriptions-item>
              <el-descriptions-item label="Method">
                <el-tag :type="methodTag(current.httpMethod)" size="small" effect="dark">{{ current.httpMethod }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="Path"><code>{{ current.path }}</code></el-descriptions-item>
              <el-descriptions-item label="说明">{{ current.description || '-' }}</el-descriptions-item>
              <el-descriptions-item label="鉴权">
                <el-tag :type="current.authRequired === 'Y' ? 'warning' : 'info'" size="small">
                  {{ current.authRequired === 'Y' ? '需要 Token' : '公开' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="Mock 服务">
                <el-switch
                  :model-value="current.mockEnabled === 'Y'"
                  @change="toggleMock"
                />
              </el-descriptions-item>
            </el-descriptions>

            <el-tabs v-model="detailTab" style="margin-top: 14px">
              <el-tab-pane label="📥 请求 Schema" name="req">
                <pre class="json-code">{{ current.requestSchema || '(待 AI 生成)' }}</pre>
              </el-tab-pane>
              <el-tab-pane label="📤 响应 Schema" name="resp">
                <pre class="json-code">{{ current.responseSchema || '(待 AI 生成)' }}</pre>
              </el-tab-pane>
              <el-tab-pane label="✨ 示例" name="example">
                <strong>请求示例:</strong>
                <pre class="json-code">{{ current.exampleRequest || '-' }}</pre>
                <strong>响应示例:</strong>
                <pre class="json-code">{{ current.exampleResponse || '-' }}</pre>
              </el-tab-pane>
              <el-tab-pane label="⚠️ 错误码" name="errors">
                <pre class="json-code">{{ current.errorCodes || '(待 AI 生成)' }}</pre>
              </el-tab-pane>
              <el-tab-pane v-if="current.mockEnabled === 'Y'" label="🔧 Mock" name="mock">
                <pre class="json-code">{{ current.mockResponse || '(未配置)' }}</pre>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增接口 Dialog (对齐原型 modal-newapi) -->
    <el-dialog v-model="dialogVisible" title="+ 手动添加接口" width="540px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联项目" prop="projectId" required>
          <el-select v-model="form.projectId" style="width: 100%">
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="10">
            <el-form-item label="HTTP" prop="httpMethod" required>
              <el-select v-model="form.httpMethod" style="width: 100%">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
                <el-option label="PATCH" value="PATCH" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="14">
            <el-form-item label="Path" prop="path" required>
              <el-input v-model="form.path" placeholder="/api/v1/irrigation/recommend" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="说明" prop="description">
          <el-input v-model="form.description" placeholder="功能说明" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="鉴权" prop="authRequired">
              <el-switch
                :model-value="form.authRequired === 'Y'"
                @change="form.authRequired = ($event ? 'Y' : 'N')"
                active-text="需要" inactive-text="公开"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Mock" prop="mockEnabled">
              <el-switch
                :model-value="form.mockEnabled === 'Y'"
                @change="form.mockEnabled = ($event ? 'Y' : 'N')"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">✅ 添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick, Connection } from '@element-plus/icons-vue'
import {
  listApiDesign, addApiDesign, updateApiDesign, delApiDesign, aiGenerateApiDesign,
  listProjectsForSelect, type ApiDesign, type ApiDesignQuery
} from '@/api/business/apidesign'

const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)
const detailTab = ref('req')

const emptyForm = (): ApiDesign => ({
  projectId: 0, httpMethod: 'GET', path: '', description: '',
  authRequired: 'Y', mockEnabled: 'N', authorUserId: 1
})
const form = reactive<ApiDesign>(emptyForm())
const current = reactive<ApiDesign>({ projectId: 0, httpMethod: 'GET', path: '' })
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  httpMethod: [{ required: true, message: '选择 HTTP 方法', trigger: 'change' }],
  path: [{ required: true, message: '请输入接口路径', trigger: 'blur' }]
}

const list = ref<ApiDesign[]>([])
const total = ref(0)
const queryParams = reactive<ApiDesignQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

const statusMap: Record<string, { label: string; type: any }> = {
  '00': { label: '草稿', type: 'info' }, '01': { label: '评审中', type: 'warning' },
  '02': { label: '已确认', type: 'success' }, '03': { label: '已废弃', type: 'danger' }
}
const statusTagFor = (s?: string) => statusMap[s || '00'] || { label: s || '-', type: 'info' as any }
const statusTag = computed(() => statusTagFor(current.status))

function methodTag(m?: string): any {
  return ({ GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger', PATCH: 'info' } as Record<string, string>)[m || ''] || 'info'
}

async function getList() {
  listLoading.value = true
  try { const res: any = await listApiDesign(queryParams); list.value = res.rows || []; total.value = res.total || 0 }
  finally { listLoading.value = false }
}

async function loadProjects() {
  try { const res: any = await listProjectsForSelect(); projectOptions.value = res.rows || [] } catch { /* */ }
}

function onSelect(row: ApiDesign | null) {
  if (row) Object.assign(current, row)
  else Object.keys(current).forEach(k => delete (current as any)[k])
}

function openAdd() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    await addApiDesign(form); ElMessage.success('添加成功')
    dialogVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '添加失败') }
  finally { saving.value = false }
}

async function triggerAi() {
  if (!current.apidesignId) return
  aiLoading.value = true
  try {
    const res: any = await aiGenerateApiDesign(current.apidesignId)
    if (res.code === 200 && res.data) { Object.assign(current, res.data); ElMessage.success('AI Schema + 示例 + 错误码已生成'); await getList() }
  } catch (e: any) { ElMessage.error(e?.msg || 'AI 失败') }
  finally { aiLoading.value = false }
}

async function toggleMock(enabled: boolean) {
  if (!current.apidesignId) return
  try {
    await updateApiDesign({ ...current, mockEnabled: enabled ? 'Y' : 'N' })
    current.mockEnabled = enabled ? 'Y' : 'N'
    ElMessage.success(`Mock ${enabled ? '已开启' : '已关闭'}`)
  } catch (e: any) { ElMessage.error(e?.msg || '切换失败') }
}

async function handleDelete(row: ApiDesign) {
  if (!row.apidesignId) return
  await ElMessageBox.confirm(`确认删除 ${row.httpMethod} ${row.path}?`, '提示', { type: 'warning' })
  await delApiDesign(row.apidesignId); ElMessage.success('删除成功')
  if (current.apidesignId === row.apidesignId) Object.keys(current).forEach(k => delete (current as any)[k])
  await getList()
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.api-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.detail-card { min-height: 560px; }
.empty-state { text-align: center; padding: 60px 20px; color: #6b7280; }
.empty-state p { margin: 12px 0; }
code { background: #f4f4f5; padding: 2px 6px; border-radius: 3px; font-family: monospace; font-size: 12px; }
.json-code {
  background: #1e1e2e; color: #cdd6f4; padding: 12px 14px; border-radius: 6px;
  font-family: 'Consolas', monospace; font-size: 12px; line-height: 1.6;
  overflow-x: auto; white-space: pre-wrap; margin: 8px 0;
}
</style>
