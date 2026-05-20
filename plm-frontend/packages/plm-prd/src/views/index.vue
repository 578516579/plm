<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="PRD编号" prop="prdNo">
        <el-input v-model="queryParams.prdNo" placeholder="PRD-..." clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目ID" prop="projectId">
        <el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="功能名称" prop="title">
        <el-input v-model="queryParams.title" placeholder="模糊匹配" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="业务场景" prop="sceneTemplate">
        <el-select v-model="queryParams.sceneTemplate" placeholder="全部" clearable>
          <el-option v-for="d in scene_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标用户" prop="targetUser">
        <el-select v-model="queryParams.targetUser" placeholder="全部" clearable>
          <el-option v-for="d in user_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="AI 生成" prop="aiGenerated">
        <el-select v-model="queryParams.aiGenerated" placeholder="全部" clearable>
          <el-option label="是 (Y)" value="Y" /><el-option label="否 (N)" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:prd:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:prd:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:prd:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:prd:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="prdId" width="80" />
      <el-table-column label="PRD编号" align="center" prop="prdNo" width="160" />
      <el-table-column label="功能名称" align="left" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="场景" align="center" prop="sceneTemplate" width="120">
        <template #default="scope"><dict-tag :options="scene_options" :value="scope.row.sceneTemplate" /></template>
      </el-table-column>
      <el-table-column label="目标用户" align="center" prop="targetUser" width="120">
        <template #default="scope"><dict-tag :options="user_options" :value="scope.row.targetUser" /></template>
      </el-table-column>
      <el-table-column label="完整度" align="center" prop="completenessScore" width="90">
        <template #default="scope"><span v-if="scope.row.completenessScore != null">{{ scope.row.completenessScore }}%</span></template>
      </el-table-column>
      <el-table-column label="版本" align="center" prop="version" width="80" />
      <el-table-column label="AI" align="center" prop="aiGenerated" width="60">
        <template #default="scope"><el-tag v-if="scope.row.aiGenerated === 'Y'" type="warning" size="small">AI</el-tag></template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope"><dict-tag :options="status_options" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:prd:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:prd:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="860px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="PRD编号" prop="prdNo"><el-input v-model="form.prdNo" placeholder="留空自动生成 PRD-YYYY-NNNN" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目ID" prop="projectId"><el-input v-model="form.projectId" placeholder="必填" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="功能名称" prop="title"><el-input v-model="form.title" placeholder="一句话描述功能" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="功能描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="业务场景" prop="sceneTemplate"><el-select v-model="form.sceneTemplate" placeholder="请选择"><el-option v-for="d in scene_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="目标用户" prop="targetUser"><el-select v-model="form.targetUser" placeholder="请选择"><el-option v-for="d in user_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="版本" prop="version"><el-input v-model="form.version" placeholder="v1.0" /></el-form-item></el-col>
          <el-col :span="24"><el-divider content-position="left">PRD 正文（AI 一键生成 7 段）</el-divider></el-col>
          <el-col :span="24"><el-form-item label="正文" prop="content"><el-input v-model="form.content" type="textarea" :rows="8" placeholder="背景/用户故事/功能/非功能/验收/原型/版本" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="完整度(%)" prop="completenessScore"><el-input-number v-model="form.completenessScore" :min="0" :max="100" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="AI 生成" prop="aiGenerated"><el-select v-model="form.aiGenerated"><el-option label="是 (Y)" value="Y" /><el-option label="否 (N)" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态" prop="status"><el-select v-model="form.status" :disabled="!form.prdId"><el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="作者ID" prop="authorUserId"><el-input v-model="form.authorUserId" placeholder="user_id" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="审核人ID" prop="reviewerUserId"><el-input v-model="form.reviewerUserId" placeholder="user_id" /></el-form-item></el-col>
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

<script setup name="Prd" lang="ts">
import { listPrd, getPrd, addPrd, updatePrd, delPrd } from '../api'
import type { PrdForm, PrdQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_prd_status: status_options,
  biz_prd_scene: scene_options,
  biz_prd_target_user: user_options
} = toRefs<any>(proxy.useDict('biz_prd_status', 'biz_prd_scene', 'biz_prd_target_user'))

const list = ref<PrdForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const dialog = reactive({ title: '', visible: false })
const form = ref<PrdForm>({})

const queryParams = ref<PrdQuery>({
  pageNum: 1, pageSize: 10,
  prdNo: undefined, projectId: undefined, title: undefined,
  sceneTemplate: undefined, targetUser: undefined, version: undefined,
  aiGenerated: undefined, status: undefined, authorUserId: undefined
})
const rules = {
  title: [{ required: true, message: '功能名称不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listPrd(queryParams.value).then((res: any) => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function reset() {
  form.value = {
    prdNo: undefined, projectId: undefined, title: undefined, description: undefined,
    sceneTemplate: undefined, targetUser: undefined, content: undefined,
    completenessScore: 0, version: 'v1.0', aiGenerated: 'N', status: '00',
    authorUserId: undefined, reviewerUserId: undefined
  }
  proxy.resetForm('formRef')
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection: PrdForm[]) {
  ids.value = selection.map(item => item.prdId!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}
function handleAdd() { reset(); dialog.title = '新增 PRD 文档'; dialog.visible = true }
function handleUpdate(row?: PrdForm) {
  reset()
  const id = row?.prdId ?? ids.value[0]
  getPrd(id as number).then((res: any) => { form.value = res.data; dialog.title = '修改 PRD 文档'; dialog.visible = true })
}
function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.prdId ? updatePrd : addPrd
    fn(form.value).then(() => { proxy.$modal.msgSuccess(form.value.prdId ? '修改成功' : '新增成功'); dialog.visible = false; getList() })
  })
}
function cancel() { dialog.visible = false; reset() }
function handleDelete(row?: PrdForm) {
  const toDelete = row?.prdId ? [row.prdId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delPrd(toDelete as number[]))
    .then(() => { getList(); proxy.$modal.msgSuccess('删除成功') })
    .catch(() => {})
}
function handleExport() {
  proxy.download('business/prd/export', { ...queryParams.value }, 'PRD_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
