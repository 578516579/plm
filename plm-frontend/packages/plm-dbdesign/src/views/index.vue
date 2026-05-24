<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="DB编号" prop="dbdesignNo"><el-input v-model="queryParams.dbdesignNo" placeholder="DB-..." clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="项目ID" prop="projectId"><el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="架构ID" prop="archId"><el-input v-model="queryParams.archId" placeholder="可空" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="标题" prop="title"><el-input v-model="queryParams.title" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="DB引擎" prop="dbEngine">
        <el-select v-model="queryParams.dbEngine" placeholder="全部" clearable>
          <el-option v-for="d in engine_options" :key="d.value" :label="d.label" :value="d.value" />
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
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:dbdesign:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:dbdesign:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:dbdesign:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:dbdesign:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="dbdesignId" width="80" />
      <el-table-column label="DB编号" align="center" prop="dbdesignNo" width="160" />
      <el-table-column label="标题" align="left" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="架构" align="center" prop="archId" width="80" />
      <el-table-column label="DB引擎" align="center" prop="dbEngine" width="120">
        <template #default="scope"><dict-tag :options="engine_options" :value="scope.row.dbEngine" /></template>
      </el-table-column>
      <el-table-column label="AI" align="center" prop="aiGenerated" width="60">
        <template #default="scope"><el-tag v-if="scope.row.aiGenerated === 'Y'" type="warning" size="small">AI</el-tag></template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope"><dict-tag :options="status_options" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:dbdesign:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:dbdesign:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="900px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="DB编号" prop="dbdesignNo"><el-input v-model="form.dbdesignNo" placeholder="留空自动生成 DB-YYYY-NNNN" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目ID" prop="projectId"><el-input v-model="form.projectId" placeholder="必填" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="架构 ID" prop="archId"><el-input v-model="form.archId" placeholder="可空,关联架构" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设计标题" prop="title"><el-input v-model="form.title" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="DB 引擎" prop="dbEngine"><el-select v-model="form.dbEngine"><el-option v-for="d in engine_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status" :disabled="!form.dbdesignId"><el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="24"><el-divider content-position="left">ER 图 + 数据字典 (AI 生成)</el-divider></el-col>
          <el-col :span="24"><el-form-item label="ER 图内容" prop="erDiagramContent"><el-input v-model="form.erDiagramContent" type="textarea" :rows="4" placeholder="Mermaid ER / PlantUML / dbdiagram.io 源码" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="数据字典" prop="dataDictionary"><el-input v-model="form.dataDictionary" type="textarea" :rows="3" placeholder="表名/字段/含义 — Markdown 表格或 JSON" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="DDL 脚本" prop="ddlScript"><el-input v-model="form.ddlScript" type="textarea" :rows="5" placeholder="CREATE TABLE ..." /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="规范检查" prop="normalizationCheck"><el-input v-model="form.normalizationCheck" type="textarea" :rows="2" placeholder="3NF/BCNF 检查结果 / 命名规约违规项" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="AI 生成" prop="aiGenerated"><el-select v-model="form.aiGenerated"><el-option label="是 (Y)" value="Y" /><el-option label="否 (N)" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="作者ID" prop="authorUserId"><el-input v-model="form.authorUserId" placeholder="user_id" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="审核人" prop="reviewerUserId"><el-input v-model="form.reviewerUserId" placeholder="user_id" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="DbDesign" lang="ts">
import { listDbDesign, getDbDesign, addDbDesign, updateDbDesign, delDbDesign } from '../api'
import type { DbDesignForm, DbDesignQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_dbdesign_status: status_options,
  biz_dbdesign_engine: engine_options
} = toRefs<any>(proxy.useDict('biz_dbdesign_status', 'biz_dbdesign_engine'))

const list = ref<DbDesignForm[]>([])
const loading = ref(true); const showSearch = ref(true); const ids = ref<(number | string)[]>([])
const single = ref(true); const multiple = ref(true); const total = ref(0)
const dialog = reactive({ title: '', visible: false }); const form = ref<DbDesignForm>({})

const queryParams = ref<DbDesignQuery>({
  pageNum: 1, pageSize: 10,
  dbdesignNo: undefined, projectId: undefined, archId: undefined, title: undefined,
  dbEngine: undefined, aiGenerated: undefined, status: undefined, authorUserId: undefined
})
const rules = {
  title: [{ required: true, message: '设计标题不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }]
}

function getList() { loading.value = true; listDbDesign(queryParams.value).then((res: any) => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function reset() {
  form.value = {
    dbdesignNo: undefined, projectId: undefined, archId: undefined, title: undefined,
    dbEngine: 'mysql', erDiagramContent: undefined, dataDictionary: undefined,
    ddlScript: undefined, normalizationCheck: undefined,
    aiGenerated: 'N', status: '00', authorUserId: undefined, reviewerUserId: undefined
  }
  proxy.resetForm('formRef')
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(s: DbDesignForm[]) { ids.value = s.map(x => x.dbdesignId!); single.value = s.length !== 1; multiple.value = !s.length }
function handleAdd() { reset(); dialog.title = '新增数据库设计'; dialog.visible = true }
function handleUpdate(row?: DbDesignForm) {
  reset()
  const id = row?.dbdesignId ?? ids.value[0]
  getDbDesign(id as number).then((res: any) => { form.value = res.data; dialog.title = '修改数据库设计'; dialog.visible = true })
}
function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.dbdesignId ? updateDbDesign : addDbDesign
    fn(form.value).then(() => { proxy.$modal.msgSuccess(form.value.dbdesignId ? '修改成功' : '新增成功'); dialog.visible = false; getList() })
  })
}
function cancel() { dialog.visible = false; reset() }
function handleDelete(row?: DbDesignForm) {
  const toDelete = row?.dbdesignId ? [row.dbdesignId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delDbDesign(toDelete as number[]))
    .then(() => { getList(); proxy.$modal.msgSuccess('删除成功') })
    .catch(() => {})
}
function handleExport() { proxy.download('business/dbdesign/export', { ...queryParams.value }, 'DB设计_' + new Date().getTime() + '.xlsx') }

getList()
</script>
