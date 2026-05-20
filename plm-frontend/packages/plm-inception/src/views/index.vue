<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="立项编号" prop="inceptionNo"><el-input v-model="queryParams.inceptionNo" placeholder="INC-..." clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="项目名称" prop="projectName"><el-input v-model="queryParams.projectName" placeholder="模糊匹配" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="业务线" prop="businessLine">
        <el-select v-model="queryParams.businessLine" placeholder="全部" clearable>
          <el-option v-for="d in line_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="项目类型" prop="inceptionType">
        <el-select v-model="queryParams.inceptionType" placeholder="全部" clearable>
          <el-option v-for="d in type_options" :key="d.value" :label="d.label" :value="d.value" />
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
      <el-form-item label="提交人" prop="submitterUserId"><el-input v-model="queryParams.submitterUserId" placeholder="user_id" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:inception:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:inception:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:inception:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:inception:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="inceptionId" width="80" />
      <el-table-column label="立项编号" align="center" prop="inceptionNo" width="160" />
      <el-table-column label="项目名称" align="left" prop="projectName" :show-overflow-tooltip="true" />
      <el-table-column label="业务线" align="center" prop="businessLine" width="140">
        <template #default="scope"><dict-tag :options="line_options" :value="scope.row.businessLine" /></template>
      </el-table-column>
      <el-table-column label="项目类型" align="center" prop="inceptionType" width="100">
        <template #default="scope"><dict-tag :options="type_options" :value="scope.row.inceptionType" /></template>
      </el-table-column>
      <el-table-column label="工期(月)" align="center" prop="estimatedDurationMonths" width="100" />
      <el-table-column label="团队规模" align="center" prop="estimatedTeam" width="100" />
      <el-table-column label="AI" align="center" prop="aiGenerated" width="60">
        <template #default="scope"><el-tag v-if="scope.row.aiGenerated === 'Y'" type="warning" size="small">AI</el-tag></template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope"><dict-tag :options="status_options" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="提交人" align="center" prop="submitterUserId" width="80" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:inception:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:inception:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="900px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="12"><el-form-item label="立项编号" prop="inceptionNo"><el-input v-model="form.inceptionNo" placeholder="留空自动生成 INC-YYYY-NNNN" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目名称" prop="projectName"><el-input v-model="form.projectName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="业务线" prop="businessLine"><el-select v-model="form.businessLine"><el-option v-for="d in line_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="项目类型" prop="inceptionType"><el-select v-model="form.inceptionType"><el-option v-for="d in type_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="预计工期(月)" prop="estimatedDurationMonths"><el-input-number v-model="form.estimatedDurationMonths" :min="1" :max="60" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="项目背景" prop="background"><el-input v-model="form.background" type="textarea" :rows="3" placeholder="为什么要立项,期望达成什么目标" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="团队规模" prop="estimatedTeam"><el-input v-model="form.estimatedTeam" placeholder="如: 8 人(1PM+2 设计+4 研发+1 测试)" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="关联项目 ID" prop="projectId"><el-input v-model="form.projectId" placeholder="批准后回填" /></el-form-item></el-col>
          <el-col :span="24"><el-divider content-position="left">AI 辅助生成</el-divider></el-col>
          <el-col :span="8"><el-form-item label="AI 生成" prop="aiGenerated"><el-select v-model="form.aiGenerated"><el-option label="是 (Y)" value="Y" /><el-option label="否 (N)" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态" prop="status"><el-select v-model="form.status" :disabled="!form.inceptionId"><el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="提交人" prop="submitterUserId"><el-input v-model="form.submitterUserId" placeholder="user_id" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="AI 立项建议书" prop="aiProposalContent"><el-input v-model="form.aiProposalContent" type="textarea" :rows="4" placeholder="AI 基于背景+业务线自动生成的完整立项建议书" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="AI 风险识别" prop="aiRisks"><el-input v-model="form.aiRisks" type="textarea" :rows="3" placeholder="AI 识别的关键风险点(技术/进度/成本/资源)" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="审批人" prop="approverUserId"><el-input v-model="form.approverUserId" placeholder="user_id" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="驳回原因" prop="rejectReason"><el-input v-model="form.rejectReason" type="textarea" :rows="2" placeholder="04 驳回时填,反向打回 00 草稿可重写" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="Inception" lang="ts">
import { listInception, getInception, addInception, updateInception, delInception } from '../api'
import type { InceptionForm, InceptionQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_inception_status: status_options,
  biz_inception_type: type_options,
  biz_inception_biz_line: line_options
} = toRefs<any>(proxy.useDict('biz_inception_status', 'biz_inception_type', 'biz_inception_biz_line'))

const list = ref<InceptionForm[]>([])
const loading = ref(true); const showSearch = ref(true); const ids = ref<(number | string)[]>([])
const single = ref(true); const multiple = ref(true); const total = ref(0)
const dialog = reactive({ title: '', visible: false }); const form = ref<InceptionForm>({})

const queryParams = ref<InceptionQuery>({
  pageNum: 1, pageSize: 10,
  inceptionNo: undefined, projectName: undefined,
  businessLine: undefined, inceptionType: undefined,
  aiGenerated: undefined, status: undefined,
  submitterUserId: undefined, approverUserId: undefined
})
const rules = {
  projectName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
  businessLine: [{ required: true, message: '业务线不能为空', trigger: 'change' }],
  background: [{ required: true, message: '项目背景不能为空', trigger: 'blur' }]
}

function getList() { loading.value = true; listInception(queryParams.value).then((res: any) => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function reset() {
  form.value = {
    inceptionNo: undefined, projectName: undefined,
    businessLine: undefined, inceptionType: undefined, background: undefined,
    estimatedDurationMonths: undefined, estimatedTeam: undefined,
    aiGenerated: 'N', aiProposalContent: undefined, aiRisks: undefined,
    status: '00', rejectReason: undefined,
    submitterUserId: undefined, approverUserId: undefined, projectId: undefined
  }
  proxy.resetForm('formRef')
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(s: InceptionForm[]) { ids.value = s.map(x => x.inceptionId!); single.value = s.length !== 1; multiple.value = !s.length }
function handleAdd() { reset(); dialog.title = '新增项目立项'; dialog.visible = true }
function handleUpdate(row?: InceptionForm) {
  reset()
  const id = row?.inceptionId ?? ids.value[0]
  getInception(id as number).then((res: any) => { form.value = res.data; dialog.title = '修改项目立项'; dialog.visible = true })
}
function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.inceptionId ? updateInception : addInception
    fn(form.value).then(() => { proxy.$modal.msgSuccess(form.value.inceptionId ? '修改成功' : '新增成功'); dialog.visible = false; getList() })
  })
}
function cancel() { dialog.visible = false; reset() }
function handleDelete(row?: InceptionForm) {
  const toDelete = row?.inceptionId ? [row.inceptionId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delInception(toDelete as number[]))
    .then(() => { getList(); proxy.$modal.msgSuccess('删除成功') })
    .catch(() => {})
}
function handleExport() { proxy.download('business/inception/export', { ...queryParams.value }, '立项_' + new Date().getTime() + '.xlsx') }

getList()
</script>
