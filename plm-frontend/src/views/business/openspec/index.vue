<!--
  AI OpenSpec — PRD §F3.5 + 原型 aispec.html
  规范列表 + 规范预览双卡 + AI 生成新规范
-->
<template>
  <div class="app-container os-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📐 AI OpenSpec</h2>
        <p class="page-subtitle">AI 自动生成 OpenAPI / AsyncAPI / AI Function Spec 规范,与 AgriKB 联动</p>
      </div>
      <AiButton @click="openAdd">AI 生成新规范</AiButton>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never" class="list-card">
          <template #header><span class="card-title">🤖 规范列表</span></template>
          <el-table v-loading="listLoading" :data="list" stripe highlight-current-row @current-change="onSelect">
            <el-table-column label="编号" prop="openspecNo" width="140" />
            <el-table-column label="规范名" prop="specName" min-width="160" show-overflow-tooltip />
            <el-table-column label="类型" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="specTypeTag(row.specType)" size="small">{{ specTypeLabel(row.specType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="版本" prop="specVersion" width="80" align="center" />
            <el-table-column label="AI" width="60" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.aiGenerated === 'Y'" type="success" size="small">Y</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button link type="danger" @click.stop="handleDelete(row)">删</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="preview-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">📄 规范预览</span>
              <el-tag v-if="current.specType" :type="specTypeTag(current.specType)" size="small">{{ specTypeLabel(current.specType) }}</el-tag>
            </div>
          </template>
          <div v-if="!current.openspecId" class="empty-state"><p>点击左侧规范查看内容</p></div>
          <div v-else>
            <el-descriptions :column="2" size="small" border>
              <el-descriptions-item label="规范名">{{ current.specName }}</el-descriptions-item>
              <el-descriptions-item label="版本">{{ current.specVersion }}</el-descriptions-item>
              <el-descriptions-item label="描述" :span="2">{{ current.description || '-' }}</el-descriptions-item>
            </el-descriptions>
            <pre class="spec-code">{{ current.specContent || '(待 AI 生成)' }}</pre>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" title="✨ AI 生成新规范" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="规范名称" prop="specName" required>
          <el-input v-model="form.specName" placeholder="如:农业 IoT 数据推送接口" />
        </el-form-item>
        <el-form-item label="规范类型" prop="specType">
          <el-select v-model="form.specType" style="width: 100%">
            <el-option label="OpenAPI 3.1" value="openapi_31" />
            <el-option label="AsyncAPI 3.0" value="asyncapi_30" />
            <el-option label="AI Function Spec" value="ai_function_spec" />
            <el-option label="GraphQL Schema" value="graphql" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" placeholder="接口功能描述..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import { listOpenSpec, addOpenSpec, delOpenSpec, aiGenerateOpenSpec, type OpenSpec, type OpenSpecQuery } from '@/api/business/openspec'
import { specTypeLabel, specTypeTag } from './openspecDict'

const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const listLoading = ref(false)

const emptyForm = (): OpenSpec => ({ specName: '', specType: 'openapi_31', specVersion: 'v1.0', authorUserId: 1 })
const form = reactive<OpenSpec>(emptyForm())
const current = reactive<OpenSpec>({ specName: '' })
const rules = {
  specName: [{ required: true, message: '请输入规范名称', trigger: 'blur' }]
}

const list = ref<OpenSpec[]>([])
const total = ref(0)
const queryParams = reactive<OpenSpecQuery>({ pageNum: 1, pageSize: 10 })

async function getList() {
  listLoading.value = true
  try { const res: any = await listOpenSpec(queryParams); list.value = res.rows || []; total.value = res.total || 0 } finally { listLoading.value = false }
}

function onSelect(row: OpenSpec | null) {
  if (row) Object.assign(current, row)
  else Object.keys(current).forEach(k => delete (current as any)[k])
}

function openAdd() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    const res: any = await addOpenSpec(form)
    ElMessage.success('创建成功'); dialogVisible.value = false; await getList()
    // 创建后立即触发 AI 生成
    if (res.code === 200) {
      await getList()
      const latest = list.value.find(x => x.specName === form.specName)
      if (latest?.openspecId) {
        const aiRes: any = await aiGenerateOpenSpec(latest.openspecId)
        if (aiRes.code === 200 && aiRes.data) { Object.assign(current, aiRes.data); ElMessage.success('AI 规范已生成'); await getList() }
      }
    }
  } catch (e: any) { ElMessage.error(e?.msg || '生成失败') } finally { saving.value = false }
}

async function handleDelete(row: OpenSpec) {
  if (!row.openspecId) return
  await ElMessageBox.confirm(`删除 "${row.specName}"?`, '提示', { type: 'warning' })
  await delOpenSpec(row.openspecId); ElMessage.success('删除成功')
  if (current.openspecId === row.openspecId) Object.keys(current).forEach(k => delete (current as any)[k])
  await getList()
}

onMounted(getList)
</script>

<style scoped>
.os-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.list-card, .preview-card { min-height: 500px; }
.preview-card :deep(.el-card__header) { border-top: 3px solid #3b82f6; }
.list-card :deep(.el-card__header) { border-top: 3px solid #7c3aed; }
.empty-state { text-align: center; padding: 60px 20px; color: #9ca3af; }
.spec-code {
  background: #1e1e2e; color: #cdd6f4; padding: 14px 16px; border-radius: 8px;
  font-family: 'Consolas', monospace; font-size: 11.5px; line-height: 1.6;
  margin-top: 14px; overflow: auto; white-space: pre-wrap; max-height: 400px;
}
</style>
