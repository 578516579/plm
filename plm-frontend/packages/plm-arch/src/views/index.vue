<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="架构编号" prop="archNo"><el-input v-model="queryParams.archNo" placeholder="ARCH-..." clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="项目ID" prop="projectId"><el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="PRD ID" prop="prdId"><el-input v-model="queryParams.prdId" placeholder="可空" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="标题" prop="title"><el-input v-model="queryParams.title" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="架构模式" prop="archMode">
        <el-select v-model="queryParams.archMode" placeholder="全部" clearable>
          <el-option v-for="d in mode_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="技术栈" prop="primaryStack">
        <el-select v-model="queryParams.primaryStack" placeholder="全部" clearable>
          <el-option v-for="d in stack_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="数据库" prop="databaseChoice">
        <el-select v-model="queryParams.databaseChoice" placeholder="全部" clearable>
          <el-option v-for="d in db_options" :key="d.value" :label="d.label" :value="d.value" />
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
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:arch:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:arch:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:arch:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:arch:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="archId" width="80" />
      <el-table-column label="架构编号" align="center" prop="archNo" width="160" />
      <el-table-column label="标题" align="left" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="架构模式" align="center" prop="archMode" width="120">
        <template #default="scope"><dict-tag :options="mode_options" :value="scope.row.archMode" /></template>
      </el-table-column>
      <el-table-column label="技术栈" align="center" prop="primaryStack" width="110">
        <template #default="scope"><dict-tag :options="stack_options" :value="scope.row.primaryStack" /></template>
      </el-table-column>
      <el-table-column label="数据库" align="center" prop="databaseChoice" width="110">
        <template #default="scope"><dict-tag :options="db_options" :value="scope.row.databaseChoice" /></template>
      </el-table-column>
      <el-table-column label="部署" align="center" prop="deploymentType" width="100">
        <template #default="scope"><dict-tag :options="deploy_options" :value="scope.row.deploymentType" /></template>
      </el-table-column>
      <el-table-column label="AI" align="center" prop="aiGenerated" width="60">
        <template #default="scope"><el-tag v-if="scope.row.aiGenerated === 'Y'" type="warning" size="small">AI</el-tag></template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope"><dict-tag :options="status_options" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:arch:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:arch:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="900px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="架构编号" prop="archNo"><el-input v-model="form.archNo" placeholder="留空自动生成 ARCH-YYYY-NNNN" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目ID" prop="projectId"><el-input v-model="form.projectId" placeholder="必填" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="PRD ID" prop="prdId"><el-input v-model="form.prdId" placeholder="可空,关联 PRD" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="架构标题" prop="title"><el-input v-model="form.title" /></el-form-item></el-col>
          <el-col :span="24"><el-divider content-position="left">技术选型 (AI 推荐)</el-divider></el-col>
          <el-col :span="8"><el-form-item label="架构模式" prop="archMode"><el-select v-model="form.archMode"><el-option v-for="d in mode_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="主技术栈" prop="primaryStack"><el-select v-model="form.primaryStack"><el-option v-for="d in stack_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="数据库选型" prop="databaseChoice"><el-select v-model="form.databaseChoice"><el-option v-for="d in db_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="AI 编排" prop="aiOrchestration"><el-select v-model="form.aiOrchestration" clearable><el-option v-for="d in ai_engine_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="部署方式" prop="deploymentType"><el-select v-model="form.deploymentType"><el-option v-for="d in deploy_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="IoT 协议" prop="iotProtocol"><el-input v-model="form.iotProtocol" placeholder="MQTT/HTTP/CoAP" /></el-form-item></el-col>
          <el-col :span="24"><el-divider content-position="left">设计内容 + C4 图 + NFR</el-divider></el-col>
          <el-col :span="24"><el-form-item label="设计正文" prop="designContent"><el-input v-model="form.designContent" type="textarea" :rows="4" placeholder="Markdown" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="C4 模型" prop="c4DiagramContent"><el-input v-model="form.c4DiagramContent" type="textarea" :rows="3" placeholder="Mermaid C4 容器图源码" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="NFR 映射" prop="nfrMapping"><el-input v-model="form.nfrMapping" type="textarea" :rows="2" placeholder="性能/可用/扩展 等非功能需求 → 架构组件" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="AI 生成" prop="aiGenerated"><el-select v-model="form.aiGenerated"><el-option label="是 (Y)" value="Y" /><el-option label="否 (N)" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态" prop="status"><el-select v-model="form.status" :disabled="!form.archId"><el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="审核人" prop="reviewerUserId"><el-input v-model="form.reviewerUserId" placeholder="user_id" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="Arch" lang="ts">
import { listArch, getArch, addArch, updateArch, delArch } from '../api'
import type { ArchForm, ArchQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_arch_mode: mode_options, biz_arch_stack: stack_options,
  biz_arch_database: db_options, biz_arch_deployment: deploy_options,
  biz_arch_ai_engine: ai_engine_options
} = toRefs<any>(proxy.useDict('biz_arch_mode', 'biz_arch_stack', 'biz_arch_database', 'biz_arch_deployment', 'biz_arch_ai_engine'))
// Arch dict 缺 status (无 biz_arch_status),用通用字典约定 (00 草稿 01 评审中 02 已确认 03 已废弃)
const status_options = ref([
  { value: '00', label: '草稿' },
  { value: '01', label: '评审中' },
  { value: '02', label: '已确认' },
  { value: '03', label: '已废弃' }
])

const list = ref<ArchForm[]>([])
const loading = ref(true); const showSearch = ref(true); const ids = ref<(number | string)[]>([])
const single = ref(true); const multiple = ref(true); const total = ref(0)
const dialog = reactive({ title: '', visible: false }); const form = ref<ArchForm>({})

const queryParams = ref<ArchQuery>({
  pageNum: 1, pageSize: 10,
  archNo: undefined, projectId: undefined, prdId: undefined, title: undefined,
  archMode: undefined, primaryStack: undefined, databaseChoice: undefined,
  deploymentType: undefined, aiGenerated: undefined, status: undefined, authorUserId: undefined
})
const rules = {
  title: [{ required: true, message: '架构标题不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }]
}

function getList() { loading.value = true; listArch(queryParams.value).then((res: any) => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function reset() {
  form.value = {
    archNo: undefined, projectId: undefined, prdId: undefined, title: undefined,
    archMode: undefined, primaryStack: undefined, databaseChoice: undefined,
    aiOrchestration: undefined, deploymentType: undefined, iotProtocol: undefined,
    designContent: undefined, c4DiagramContent: undefined, nfrMapping: undefined,
    aiGenerated: 'N', status: '00', authorUserId: undefined, reviewerUserId: undefined
  }
  proxy.resetForm('formRef')
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(s: ArchForm[]) { ids.value = s.map(x => x.archId!); single.value = s.length !== 1; multiple.value = !s.length }
function handleAdd() { reset(); dialog.title = '新增架构设计'; dialog.visible = true }
function handleUpdate(row?: ArchForm) {
  reset()
  const id = row?.archId ?? ids.value[0]
  getArch(id as number).then((res: any) => { form.value = res.data; dialog.title = '修改架构设计'; dialog.visible = true })
}
function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.archId ? updateArch : addArch
    fn(form.value).then(() => { proxy.$modal.msgSuccess(form.value.archId ? '修改成功' : '新增成功'); dialog.visible = false; getList() })
  })
}
function cancel() { dialog.visible = false; reset() }
function handleDelete(row?: ArchForm) {
  const toDelete = row?.archId ? [row.archId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delArch(toDelete as number[]))
    .then(() => { getList(); proxy.$modal.msgSuccess('删除成功') })
    .catch(() => {})
}
function handleExport() { proxy.download('business/arch/export', { ...queryParams.value }, '架构_' + new Date().getTime() + '.xlsx') }

getList()
</script>
