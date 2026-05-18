<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="接口标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入接口标题" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="HTTP方法" prop="httpMethod">
        <el-select v-model="queryParams.httpMethod" placeholder="全部" clearable style="width:100px">
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
          <el-option label="PATCH" value="PATCH" />
        </el-select>
      </el-form-item>
      <el-form-item label="接口路径" prop="path">
        <el-input v-model="queryParams.path" placeholder="如 /api/v1/..." clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in biz_apidesign_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:apidesign:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:apidesign:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:apidesign:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="apidesignNo" width="160" />
      <el-table-column label="接口标题" align="center" prop="title" min-width="140" />
      <el-table-column label="方法" align="center" prop="httpMethod" width="80">
        <template #default="{ row }">
          <el-tag :type="methodTagType(row.httpMethod)" size="small">{{ row.httpMethod }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="接口路径" align="center" prop="path" min-width="180" show-overflow-tooltip />
      <el-table-column label="Mock" align="center" width="80">
        <template #default="{ row }">
          <el-tag :type="row.mockEnabled === 'Y' ? 'success' : 'info'" size="small">{{ row.mockEnabled === 'Y' ? '开启' : '关闭' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="AI生成" align="center" width="85">
        <template #default="{ row }">
          <el-tag v-if="row.aiGenerated === 'Y'" type="warning" size="small">AI</el-tag>
          <span v-else style="color:#c0c4cc;font-size:12px">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="{ row }">
          <dict-tag :options="biz_apidesign_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:apidesign:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:apidesign:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="740px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="接口标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入接口标题" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="HTTP方法" prop="httpMethod">
              <el-select v-model="form.httpMethod" placeholder="请选择" style="width:100%">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
                <el-option label="PATCH" value="PATCH" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="接口路径" prop="path">
              <el-input v-model="form.path" placeholder="/api/v1/resource" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="2" placeholder="接口描述" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="请求 Schema" prop="requestSchema">
              <el-input v-model="form.requestSchema" type="textarea" :rows="4" placeholder="JSON Schema 或示例" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="响应 Schema" prop="responseSchema">
              <el-input v-model="form.responseSchema" type="textarea" :rows="4" placeholder="JSON Schema 或示例" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开启 Mock" prop="mockEnabled">
              <el-switch v-model="form.mockEnabled" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col v-if="form.mockEnabled === 'Y'" :span="24">
            <el-form-item label="Mock 响应" prop="mockResponse">
              <el-input v-model="form.mockResponse" type="textarea" :rows="3" placeholder="Mock 响应体（JSON）" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="接口设计详情" v-model="detailVisible" width="860px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="编号">{{ detail.apidesignNo }}</el-descriptions-item>
        <el-descriptions-item label="接口标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="HTTP方法">
          <el-tag :type="methodTagType(detail.httpMethod)" size="small">{{ detail.httpMethod }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="接口路径"><code>{{ detail.path }}</code></el-descriptions-item>
        <el-descriptions-item label="Mock">
          <el-tag :type="detail.mockEnabled === 'Y' ? 'success' : 'info'" size="small">{{ detail.mockEnabled === 'Y' ? '开启' : '关闭' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :options="biz_apidesign_status" :value="detail.status" />
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detail.description }}</el-descriptions-item>
      </el-descriptions>
      <el-tabs style="margin-top:16px">
        <el-tab-pane label="请求 Schema">
          <pre style="white-space:pre-wrap;font-size:13px;margin:0;max-height:260px;overflow-y:auto;font-family:monospace">{{ detail.requestSchema || '（无）' }}</pre>
        </el-tab-pane>
        <el-tab-pane label="响应 Schema">
          <pre style="white-space:pre-wrap;font-size:13px;margin:0;max-height:260px;overflow-y:auto;font-family:monospace">{{ detail.responseSchema || '（无）' }}</pre>
        </el-tab-pane>
        <el-tab-pane label="OpenAPI Spec">
          <pre style="white-space:pre-wrap;font-size:13px;margin:0;max-height:260px;overflow-y:auto;font-family:monospace">{{ detail.openapiSpec || '（无）' }}</pre>
        </el-tab-pane>
        <el-tab-pane v-if="detail.mockEnabled === 'Y'" label="Mock 响应">
          <pre style="white-space:pre-wrap;font-size:13px;margin:0;max-height:260px;overflow-y:auto;font-family:monospace">{{ detail.mockResponse || '（无）' }}</pre>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="warning" icon="MagicStick" :loading="aiLoading" @click="handleAiGenerate">AI 生成规范</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { listApidesign, getApidesign, addApidesign, updateApidesign, delApidesign, aiGenerateApidesign } from '../api'
import type { ApidesignForm, ApidesignQuery } from '../types'

defineOptions({ name: 'Apidesign' })

const { proxy } = getCurrentInstance()!
const { biz_apidesign_status } = proxy.useDict('biz_apidesign_status')

const loading = ref(false)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref<any[]>([])
const ids = ref<(number | string)[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const detailVisible = ref(false)
const aiLoading = ref(false)
const detail = ref<any>({})
const queryRef = ref()
const formRef = ref()

const queryParams = reactive<ApidesignQuery>({ pageNum: 1, pageSize: 10 })
const form = ref<ApidesignForm>({ title: '', mockEnabled: 'N' })
const rules = {
  title: [{ required: true, message: '接口标题不能为空', trigger: 'blur' }],
  httpMethod: [{ required: true, message: '请选择 HTTP 方法', trigger: 'change' }],
  path: [{ required: true, message: '接口路径不能为空', trigger: 'blur' }]
}

function methodTagType(method: string) {
  const map: Record<string, string> = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger', PATCH: 'info' }
  return map[method] || 'info'
}

function getList() {
  loading.value = true
  listApidesign(queryParams).then((res: any) => {
    dataList.value = res.rows; total.value = res.total
  }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleSelectionChange(selection: any[]) {
  ids.value = selection.map(r => r.apidesignId); multiple.value = !ids.value.length
}
function handleAdd() { form.value = { title: '', mockEnabled: 'N' }; dialogTitle.value = '新增接口设计'; dialogVisible.value = true }
function handleEdit(row: any) {
  getApidesign(row.apidesignId).then((res: any) => { form.value = res.data; dialogTitle.value = '编辑接口设计'; dialogVisible.value = true })
}
function handleDetail(row: any) {
  getApidesign(row.apidesignId).then((res: any) => { detail.value = res.data; detailVisible.value = true })
}
function handleAiGenerate() {
  aiLoading.value = true
  aiGenerateApidesign(detail.value.apidesignId).then(() => {
    ElMessage.success('AI 生成完成')
    getApidesign(detail.value.apidesignId).then((r: any) => { detail.value = r.data }); getList()
  }).catch(() => ElMessage.error('AI 生成失败')).finally(() => { aiLoading.value = false })
}
function handleDelete(row?: any) {
  const delIds = row ? [row.apidesignId] : ids.value
  ElMessageBox.confirm('确认删除选中接口设计？', '警告', { type: 'warning' }).then(() => {
    delApidesign(delIds).then(() => { ElMessage.success('删除成功'); getList() })
  })
}
function handleExport() { proxy.download('business/apidesign/export', { ...queryParams }, 'apidesign.xlsx') }
function submitForm() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.value.apidesignId ? updateApidesign : addApidesign
    api(form.value).then(() => {
      ElMessage.success(form.value.apidesignId ? '修改成功' : '新增成功')
      dialogVisible.value = false; getList()
    })
  })
}

getList()
</script>
