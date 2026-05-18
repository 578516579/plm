<template>
  <div class="app-container">
    <!-- 搜索条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="API编号" prop="apidocNo">
        <el-input v-model="queryParams.apidocNo" placeholder="API-..." clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目ID" prop="projectId">
        <el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="接口标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="标题模糊" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="HTTP方法" prop="httpMethod">
        <el-select v-model="queryParams.httpMethod" placeholder="全部" clearable style="width: 110px">
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
          <el-option label="PATCH" value="PATCH" />
        </el-select>
      </el-form-item>
      <el-form-item label="路径" prop="path">
        <el-input v-model="queryParams.path" placeholder="/business/..." clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="版本" prop="version">
        <el-input v-model="queryParams.version" placeholder="v1" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="自动提取" prop="autoExtracted">
        <el-select v-model="queryParams.autoExtracted" placeholder="全部" clearable>
          <el-option label="是 (Y)" value="Y" />
          <el-option label="否 (N)" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:apidoc:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:apidoc:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:apidoc:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:apidoc:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="apidocId" width="80" />
      <el-table-column label="编号" align="center" prop="apidocNo" width="160" />
      <el-table-column label="HTTP" align="center" prop="httpMethod" width="80">
        <template #default="scope">
          <el-tag :type="methodTagType(scope.row.httpMethod)" size="small">{{ scope.row.httpMethod }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="路径" align="left" prop="path" :show-overflow-tooltip="true" />
      <el-table-column label="标题" align="left" prop="title" :show-overflow-tooltip="true" width="220" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="版本" align="center" prop="version" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="status_options" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="自动" align="center" prop="autoExtracted" width="70">
        <template #default="scope">
          <el-tag v-if="scope.row.autoExtracted === 'Y'" type="info" size="small">自动</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="同步时间" align="center" prop="lastSyncedAt" width="160">
        <template #default="scope"><span>{{ parseTime(scope.row.lastSyncedAt) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:apidoc:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:apidoc:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="900px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="API编号" prop="apidocNo">
              <el-input v-model="form.apidocNo" placeholder="留空自动生成 API-YYYY-NNNN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input v-model="form.projectId" placeholder="必填" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="接口标题" prop="title">
              <el-input v-model="form.title" placeholder="一句话描述接口" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="HTTP方法" prop="httpMethod">
              <el-select v-model="form.httpMethod" placeholder="请选择">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
                <el-option label="PATCH" value="PATCH" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接口路径" prop="path">
              <el-input v-model="form.path" placeholder="/business/xxx" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="版本" prop="version">
              <el-input v-model="form.version" placeholder="v1" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="2" placeholder="Markdown 兼容" />
            </el-form-item>
          </el-col>

          <!-- OpenAPI Schema -->
          <el-col :span="24">
            <el-divider content-position="left">OpenAPI Schema</el-divider>
          </el-col>
          <el-col :span="12">
            <el-form-item label="请求 Schema" prop="requestSchema">
              <el-input v-model="form.requestSchema" type="textarea" :rows="4" placeholder="JSON Schema" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="响应 Schema" prop="responseSchema">
              <el-input v-model="form.responseSchema" type="textarea" :rows="4" placeholder="JSON Schema" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="OpenAPI 规范" prop="openapiSpec">
              <el-input v-model="form.openapiSpec" type="textarea" :rows="3" placeholder="完整 OpenAPI 3.0 片段" />
            </el-form-item>
          </el-col>

          <!-- 源码追溯 -->
          <el-col :span="24">
            <el-divider content-position="left">源码追溯（自动提取专用）</el-divider>
          </el-col>
          <el-col :span="12">
            <el-form-item label="源类" prop="sourceClass">
              <el-input v-model="form.sourceClass" placeholder="cn.com.bosssfot.dv.plm.xxx.Controller" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="源方法" prop="sourceMethod">
              <el-input v-model="form.sourceMethod" placeholder="listXxx" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="自动提取" prop="autoExtracted">
              <el-select v-model="form.autoExtracted" placeholder="Y/N">
                <el-option label="是 (Y)" value="Y" />
                <el-option label="否 (N)" value="N" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" :disabled="!form.apidocId" placeholder="请选择">
                <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ApiDoc" lang="ts">
import { listApiDoc, getApiDoc, addApiDoc, updateApiDoc, delApiDoc } from '../api'
import type { ApiDocForm, ApiDocQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const { biz_apidoc_status: status_options } = toRefs<any>(proxy.useDict('biz_apidoc_status'))

const list = ref<ApiDocForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const dialog = reactive({ title: '', visible: false })
const form = ref<ApiDocForm>({})

const queryParams = ref<ApiDocQuery>({
  pageNum: 1, pageSize: 10,
  apidocNo: undefined, projectId: undefined, title: undefined,
  httpMethod: undefined, path: undefined, version: undefined,
  status: undefined, autoExtracted: undefined
})

const rules = {
  title: [{ required: true, message: '接口标题不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }],
  httpMethod: [{ required: true, message: 'HTTP 方法不能为空', trigger: 'change' }],
  path: [{ required: true, message: '接口路径不能为空', trigger: 'blur' }],
  version: [{ required: true, message: '版本不能为空', trigger: 'blur' }]
}

const METHOD_TAGS: Record<string, string> = {
  GET: 'success',
  POST: 'primary',
  PUT: 'warning',
  DELETE: 'danger',
  PATCH: 'info'
}

function methodTagType(method: string) {
  return METHOD_TAGS[method] || 'info'
}

function getList() {
  loading.value = true
  listApiDoc(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    apidocNo: undefined, projectId: undefined, title: undefined,
    httpMethod: 'GET', path: undefined, description: undefined,
    requestSchema: undefined, responseSchema: undefined, openapiSpec: undefined,
    sourceClass: undefined, sourceMethod: undefined,
    version: 'v1', status: '00', autoExtracted: 'N'
  }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection: ApiDocForm[]) {
  ids.value = selection.map(item => item.apidocId!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); dialog.title = '新增 API 文档'; dialog.visible = true }

function handleUpdate(row?: ApiDocForm) {
  reset()
  const id = row?.apidocId ?? ids.value[0]
  getApiDoc(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '修改 API 文档'
    dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.apidocId ? updateApiDoc : addApiDoc
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.apidocId ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: ApiDocForm) {
  const toDelete = row?.apidocId ? [row.apidocId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delApiDoc(toDelete as number[]))
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => {})
}

function handleExport() {
  proxy.download('business/apidoc/export', { ...queryParams.value }, 'API文档_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
