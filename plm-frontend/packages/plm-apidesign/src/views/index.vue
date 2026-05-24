<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="接口编号" prop="apidesignNo"><el-input v-model="queryParams.apidesignNo" placeholder="API-D-..." clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="项目ID" prop="projectId"><el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="架构ID" prop="archId"><el-input v-model="queryParams.archId" placeholder="可空" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="标题" prop="title"><el-input v-model="queryParams.title" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="HTTP方法" prop="httpMethod">
        <el-select v-model="queryParams.httpMethod" placeholder="全部" clearable style="width: 110px">
          <el-option label="GET" value="GET" /><el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" /><el-option label="DELETE" value="DELETE" /><el-option label="PATCH" value="PATCH" />
        </el-select>
      </el-form-item>
      <el-form-item label="路径" prop="path"><el-input v-model="queryParams.path" placeholder="/business/..." clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="Mock" prop="mockEnabled">
        <el-select v-model="queryParams.mockEnabled" placeholder="全部" clearable>
          <el-option label="是 (Y)" value="Y" /><el-option label="否 (N)" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:apidesign:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:apidesign:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:apidesign:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:apidesign:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="apidesignId" width="80" />
      <el-table-column label="编号" align="center" prop="apidesignNo" width="160" />
      <el-table-column label="HTTP" align="center" prop="httpMethod" width="80">
        <template #default="scope"><el-tag :type="methodTagType(scope.row.httpMethod)" size="small">{{ scope.row.httpMethod }}</el-tag></template>
      </el-table-column>
      <el-table-column label="路径" align="left" prop="path" :show-overflow-tooltip="true" />
      <el-table-column label="标题" align="left" prop="title" :show-overflow-tooltip="true" width="220" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="架构" align="center" prop="archId" width="80" />
      <el-table-column label="Mock" align="center" prop="mockEnabled" width="70">
        <template #default="scope"><el-tag v-if="scope.row.mockEnabled === 'Y'" type="primary" size="small">Mock</el-tag></template>
      </el-table-column>
      <el-table-column label="AI" align="center" prop="aiGenerated" width="60">
        <template #default="scope"><el-tag v-if="scope.row.aiGenerated === 'Y'" type="warning" size="small">AI</el-tag></template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope"><dict-tag :options="status_options" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:apidesign:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:apidesign:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="920px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="接口编号" prop="apidesignNo"><el-input v-model="form.apidesignNo" placeholder="留空自动生成 API-D-YYYY-NNNN" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目ID" prop="projectId"><el-input v-model="form.projectId" placeholder="必填" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="架构 ID" prop="archId"><el-input v-model="form.archId" placeholder="可空,关联架构" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="接口标题" prop="title"><el-input v-model="form.title" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="HTTP方法" prop="httpMethod"><el-select v-model="form.httpMethod"><el-option label="GET" value="GET" /><el-option label="POST" value="POST" /><el-option label="PUT" value="PUT" /><el-option label="DELETE" value="DELETE" /><el-option label="PATCH" value="PATCH" /></el-select></el-form-item></el-col>
          <el-col :span="18"><el-form-item label="接口路径" prop="path"><el-input v-model="form.path" placeholder="/business/xxx" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-divider content-position="left">设计 Schema (AI 生成 OpenAPI 3.0)</el-divider></el-col>
          <el-col :span="12"><el-form-item label="请求 Schema" prop="requestSchema"><el-input v-model="form.requestSchema" type="textarea" :rows="4" placeholder="JSON Schema" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="响应 Schema" prop="responseSchema"><el-input v-model="form.responseSchema" type="textarea" :rows="4" placeholder="JSON Schema" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="OpenAPI 规范" prop="openapiSpec"><el-input v-model="form.openapiSpec" type="textarea" :rows="3" placeholder="OpenAPI 3.0 片段" /></el-form-item></el-col>
          <el-col :span="24"><el-divider content-position="left">Mock 服务 (F3.6 联调)</el-divider></el-col>
          <el-col :span="6"><el-form-item label="Mock 开启" prop="mockEnabled"><el-select v-model="form.mockEnabled"><el-option label="是 (Y)" value="Y" /><el-option label="否 (N)" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="18"><el-form-item label="Mock 响应" prop="mockResponse"><el-input v-model="form.mockResponse" type="textarea" :rows="3" placeholder="预设 JSON 响应" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="AI 生成" prop="aiGenerated"><el-select v-model="form.aiGenerated"><el-option label="是 (Y)" value="Y" /><el-option label="否 (N)" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态" prop="status"><el-select v-model="form.status" :disabled="!form.apidesignId"><el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="审核人" prop="reviewerUserId"><el-input v-model="form.reviewerUserId" placeholder="user_id" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="ApiDesign" lang="ts">
import { listApiDesign, getApiDesign, addApiDesign, updateApiDesign, delApiDesign } from '../api'
import type { ApiDesignForm, ApiDesignQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const { biz_apidesign_status: status_options } = toRefs<any>(proxy.useDict('biz_apidesign_status'))

const METHOD_TAGS: Record<string, string> = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger', PATCH: 'info' }
function methodTagType(method: string) { return METHOD_TAGS[method] || 'info' }

const list = ref<ApiDesignForm[]>([])
const loading = ref(true); const showSearch = ref(true); const ids = ref<(number | string)[]>([])
const single = ref(true); const multiple = ref(true); const total = ref(0)
const dialog = reactive({ title: '', visible: false }); const form = ref<ApiDesignForm>({})

const queryParams = ref<ApiDesignQuery>({
  pageNum: 1, pageSize: 10,
  apidesignNo: undefined, projectId: undefined, archId: undefined, title: undefined,
  httpMethod: undefined, path: undefined, mockEnabled: undefined,
  aiGenerated: undefined, status: undefined, authorUserId: undefined
})
const rules = {
  title: [{ required: true, message: '接口标题不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }],
  httpMethod: [{ required: true, message: 'HTTP 方法不能为空', trigger: 'change' }],
  path: [{ required: true, message: '接口路径不能为空', trigger: 'blur' }]
}

function getList() { loading.value = true; listApiDesign(queryParams.value).then((res: any) => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function reset() {
  form.value = {
    apidesignNo: undefined, projectId: undefined, archId: undefined, title: undefined,
    httpMethod: 'GET', path: undefined, description: undefined,
    requestSchema: undefined, responseSchema: undefined, openapiSpec: undefined,
    mockEnabled: 'N', mockResponse: undefined,
    aiGenerated: 'N', status: '00', authorUserId: undefined, reviewerUserId: undefined
  }
  proxy.resetForm('formRef')
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(s: ApiDesignForm[]) { ids.value = s.map(x => x.apidesignId!); single.value = s.length !== 1; multiple.value = !s.length }
function handleAdd() { reset(); dialog.title = '新增接口设计'; dialog.visible = true }
function handleUpdate(row?: ApiDesignForm) {
  reset()
  const id = row?.apidesignId ?? ids.value[0]
  getApiDesign(id as number).then((res: any) => { form.value = res.data; dialog.title = '修改接口设计'; dialog.visible = true })
}
function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.apidesignId ? updateApiDesign : addApiDesign
    fn(form.value).then(() => { proxy.$modal.msgSuccess(form.value.apidesignId ? '修改成功' : '新增成功'); dialog.visible = false; getList() })
  })
}
function cancel() { dialog.visible = false; reset() }
function handleDelete(row?: ApiDesignForm) {
  const toDelete = row?.apidesignId ? [row.apidesignId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delApiDesign(toDelete as number[]))
    .then(() => { getList(); proxy.$modal.msgSuccess('删除成功') })
    .catch(() => {})
}
function handleExport() { proxy.download('business/apidesign/export', { ...queryParams.value }, '接口设计_' + new Date().getTime() + '.xlsx') }

getList()
</script>
