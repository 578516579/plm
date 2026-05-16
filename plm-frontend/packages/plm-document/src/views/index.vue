<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="文档编号">
        <el-input v-model="queryParams.documentNo" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目ID">
        <el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="文档类型">
        <el-select v-model="queryParams.docType" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="d in doc_type_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
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
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:document:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:document:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:document:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:document:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="documentId" width="80" />
      <el-table-column label="编号" align="center" prop="documentNo" width="200" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="类型" align="center" prop="docType" width="120">
        <template #default="scope"><dict-tag :options="doc_type_options" :value="scope.row.docType" /></template>
      </el-table-column>
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="版本" align="center" prop="version" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope"><dict-tag :options="status_options" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="作者" align="center" prop="authorUserId" width="80" />
      <el-table-column label="审核人" align="center" prop="reviewerUserId" width="80" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:document:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:document:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="900px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12"><el-form-item label="编号" prop="documentNo"><el-input v-model="form.documentNo" placeholder="留空自动 DOC-<TYPE>-YYYY-NNNN" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目ID" prop="projectId"><el-input v-model="form.projectId" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="docType">
              <el-select v-model="form.docType"><el-option v-for="d in doc_type_options" :key="d.value" :label="d.label" :value="d.value" /></el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="版本" prop="version"><el-input v-model="form.version" placeholder="v1.0" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item></el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" :disabled="!form.documentId">
                <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="作者ID" prop="authorUserId"><el-input v-model="form.authorUserId" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="审核人ID" prop="reviewerUserId"><el-input v-model="form.reviewerUserId" placeholder="进入「已发布」必填" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="关联类型" prop="relatedEntityType"><el-input v-model="form.relatedEntityType" placeholder="如 sprint / requirement" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="关联ID" prop="relatedEntityId"><el-input v-model="form.relatedEntityId" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="标签" prop="tags"><el-input v-model="form.tags" placeholder="CSV" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="内容 (Markdown)" prop="content"><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="cancel">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Document" lang="ts">
import { listDocument, getDocument, addDocument, updateDocument, delDocument } from '../api'
import type { DocumentForm, DocumentQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_doc_type: doc_type_options,
  biz_doc_status: status_options
} = toRefs<any>(proxy.useDict('biz_doc_type', 'biz_doc_status'))

const list = ref<DocumentForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const dialog = reactive({ title: '', visible: false })
const form = ref<DocumentForm>({})

const queryParams = ref<DocumentQuery>({
  pageNum: 1, pageSize: 10,
  documentNo: undefined, projectId: undefined, docType: undefined, status: undefined
})

const rules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  docType: [{ required: true, message: '文档类型不能为空', trigger: 'change' }],
  projectId: [{ required: true, message: '项目ID不能为空', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listDocument(queryParams.value).then((res: any) => {
    list.value = res.rows; total.value = res.total; loading.value = false
  })
}

function reset() {
  form.value = {
    documentNo: undefined, projectId: undefined, docType: 'prd', title: undefined,
    content: undefined, version: 'v1.0', status: '00',
    authorUserId: undefined, reviewerUserId: undefined, tags: undefined,
    relatedEntityType: undefined, relatedEntityId: undefined
  }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(s: DocumentForm[]) {
  ids.value = s.map(item => item.documentId!)
  single.value = s.length !== 1
  multiple.value = !s.length
}

function handleAdd() { reset(); dialog.title = '新增文档'; dialog.visible = true }

function handleUpdate(row?: DocumentForm) {
  reset()
  const id = row?.documentId ?? ids.value[0]
  getDocument(id as number).then((res: any) => {
    form.value = res.data; dialog.title = '修改文档'; dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.documentId ? updateDocument : addDocument
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.documentId ? '修改成功' : '新增成功')
      dialog.visible = false; getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: DocumentForm) {
  const toDel = row?.documentId ? [row.documentId] : ids.value
  proxy.$modal.confirm('确认删除 ' + toDel.length + ' 项?').then(() => delDocument(toDel as number[]))
    .then(() => { getList(); proxy.$modal.msgSuccess('删除成功') })
    .catch(() => {})
}

function handleExport() {
  proxy.download('business/document/export', { ...queryParams.value }, '文档_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
