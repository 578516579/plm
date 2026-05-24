<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="UED编号" prop="uedNo"><el-input v-model="queryParams.uedNo" placeholder="UED-..." clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="项目ID" prop="projectId"><el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="需求ID" prop="requirementId"><el-input v-model="queryParams.requirementId" placeholder="可空" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="设计稿" prop="title"><el-input v-model="queryParams.title" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="版本" prop="versionLabel"><el-input v-model="queryParams.versionLabel" placeholder="v1" clearable @keyup.enter="handleQuery" /></el-form-item>
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
      <el-form-item label="设计师" prop="designerUserId"><el-input v-model="queryParams.designerUserId" placeholder="user_id" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:ued:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:ued:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:ued:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:ued:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="uedId" width="80" />
      <el-table-column label="UED编号" align="center" prop="uedNo" width="160" />
      <el-table-column label="设计稿" align="left" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="需求" align="center" prop="requirementId" width="80" />
      <el-table-column label="版本" align="center" prop="versionLabel" width="80" />
      <el-table-column label="Figma" align="center" prop="figmaUrl" width="80">
        <template #default="scope">
          <el-link v-if="scope.row.figmaUrl" :href="scope.row.figmaUrl" target="_blank" type="primary">查看</el-link>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="AI 评分" align="center" prop="aiReviewScore" width="90">
        <template #default="scope">
          <span v-if="scope.row.aiReviewScore != null">{{ scope.row.aiReviewScore }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope"><dict-tag :options="status_options" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:ued:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:ued:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="900px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="UED 编号" prop="uedNo"><el-input v-model="form.uedNo" placeholder="留空自动生成 UED-YYYY-NNNN" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目ID" prop="projectId"><el-input v-model="form.projectId" placeholder="必填" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="需求ID" prop="requirementId"><el-input v-model="form.requirementId" placeholder="可空,关联需求" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="版本号" prop="versionLabel"><el-input v-model="form.versionLabel" placeholder="v1.0" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="设计稿名称" prop="title"><el-input v-model="form.title" /></el-form-item></el-col>
          <el-col :span="24"><el-divider content-position="left">Figma MCP 集成</el-divider></el-col>
          <el-col :span="16"><el-form-item label="Figma URL" prop="figmaUrl"><el-input v-model="form.figmaUrl" placeholder="https://www.figma.com/file/..." /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="File Key" prop="figmaFileKey"><el-input v-model="form.figmaFileKey" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="预览 URL" prop="previewUrl"><el-input v-model="form.previewUrl" placeholder="可空" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="标注说明" prop="annotationContent"><el-input v-model="form.annotationContent" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-divider content-position="left">AI 规范检查</el-divider></el-col>
          <el-col :span="24"><el-form-item label="AI 评审报告" prop="aiReviewReport"><el-input v-model="form.aiReviewReport" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="AI 评分" prop="aiReviewScore"><el-input-number v-model="form.aiReviewScore" :min="0" :max="100" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="9"><el-form-item label="合规检查" prop="complianceCheck"><el-input v-model="form.complianceCheck" placeholder="WCAG 2.1 / 中文字体 / 等" /></el-form-item></el-col>
          <el-col :span="9"><el-form-item label="可用性问题" prop="usabilityIssues"><el-input v-model="form.usabilityIssues" placeholder="主要发现" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="农业组件" prop="agriComponentTags"><el-input v-model="form.agriComponentTags" placeholder="多个组件用,分隔,如: 大棚卡片,传感器图,作物日历" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="AI 生成" prop="aiGenerated"><el-select v-model="form.aiGenerated"><el-option label="是 (Y)" value="Y" /><el-option label="否 (N)" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态" prop="status"><el-select v-model="form.status" :disabled="!form.uedId"><el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="设计师" prop="designerUserId"><el-input v-model="form.designerUserId" placeholder="user_id" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="Ued" lang="ts">
import { listUed, getUed, addUed, updateUed, delUed } from '../api'
import type { UedForm, UedQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const { biz_ued_status: status_options } = toRefs<any>(proxy.useDict('biz_ued_status'))

const list = ref<UedForm[]>([])
const loading = ref(true); const showSearch = ref(true); const ids = ref<(number | string)[]>([])
const single = ref(true); const multiple = ref(true); const total = ref(0)
const dialog = reactive({ title: '', visible: false }); const form = ref<UedForm>({})

const queryParams = ref<UedQuery>({
  pageNum: 1, pageSize: 10,
  uedNo: undefined, projectId: undefined, requirementId: undefined,
  title: undefined, versionLabel: undefined, aiGenerated: undefined,
  status: undefined, designerUserId: undefined
})
const rules = {
  title: [{ required: true, message: '设计稿名称不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }]
}

function getList() { loading.value = true; listUed(queryParams.value).then((res: any) => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function reset() {
  form.value = {
    uedNo: undefined, projectId: undefined, requirementId: undefined, title: undefined,
    figmaUrl: undefined, figmaFileKey: undefined, versionLabel: 'v1.0', previewUrl: undefined,
    annotationContent: undefined, aiReviewReport: undefined, aiReviewScore: undefined,
    complianceCheck: undefined, usabilityIssues: undefined, agriComponentTags: undefined,
    aiGenerated: 'N', status: '00', designerUserId: undefined, reviewerUserId: undefined
  }
  proxy.resetForm('formRef')
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(s: UedForm[]) { ids.value = s.map(x => x.uedId!); single.value = s.length !== 1; multiple.value = !s.length }
function handleAdd() { reset(); dialog.title = '新增 UED 设计'; dialog.visible = true }
function handleUpdate(row?: UedForm) {
  reset()
  const id = row?.uedId ?? ids.value[0]
  getUed(id as number).then((res: any) => { form.value = res.data; dialog.title = '修改 UED 设计'; dialog.visible = true })
}
function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.uedId ? updateUed : addUed
    fn(form.value).then(() => { proxy.$modal.msgSuccess(form.value.uedId ? '修改成功' : '新增成功'); dialog.visible = false; getList() })
  })
}
function cancel() { dialog.visible = false; reset() }
function handleDelete(row?: UedForm) {
  const toDelete = row?.uedId ? [row.uedId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delUed(toDelete as number[]))
    .then(() => { getList(); proxy.$modal.msgSuccess('删除成功') })
    .catch(() => {})
}
function handleExport() { proxy.download('business/ued/export', { ...queryParams.value }, 'UED_' + new Date().getTime() + '.xlsx') }

getList()
</script>
