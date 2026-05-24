<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="竞品编号" prop="competitiveNo"><el-input v-model="queryParams.competitiveNo" placeholder="COMP-..." clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="项目ID" prop="projectId"><el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="竞品名称" prop="competitorName"><el-input v-model="queryParams.competitorName" placeholder="模糊匹配" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="厂商" prop="vendor"><el-input v-model="queryParams.vendor" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="价格档" prop="pricingTier">
        <el-select v-model="queryParams.pricingTier" placeholder="全部" clearable>
          <el-option v-for="d in tier_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="监控开启" prop="monitorEnabled">
        <el-select v-model="queryParams.monitorEnabled" placeholder="全部" clearable>
          <el-option label="是 (Y)" value="Y" /><el-option label="否 (N)" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:competitive:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:competitive:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:competitive:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:competitive:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="competitiveId" width="80" />
      <el-table-column label="竞品编号" align="center" prop="competitiveNo" width="160" />
      <el-table-column label="竞品名称" align="left" prop="competitorName" :show-overflow-tooltip="true" />
      <el-table-column label="厂商" align="center" prop="vendor" width="140" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="价格档" align="center" prop="pricingTier" width="100">
        <template #default="scope"><dict-tag :options="tier_options" :value="scope.row.pricingTier" /></template>
      </el-table-column>
      <el-table-column label="监控" align="center" prop="monitorEnabled" width="70">
        <template #default="scope"><el-tag v-if="scope.row.monitorEnabled === 'Y'" type="primary" size="small">监控</el-tag></template>
      </el-table-column>
      <el-table-column label="AI" align="center" prop="aiGenerated" width="60">
        <template #default="scope"><el-tag v-if="scope.row.aiGenerated === 'Y'" type="warning" size="small">AI</el-tag></template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope"><dict-tag :options="status_options" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="最近监控" align="center" prop="lastMonitoredAt" width="160">
        <template #default="scope"><span>{{ parseTime(scope.row.lastMonitoredAt) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:competitive:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:competitive:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="900px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12"><el-form-item label="竞品编号" prop="competitiveNo"><el-input v-model="form.competitiveNo" placeholder="留空自动生成 COMP-YYYY-NNNN" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目ID" prop="projectId"><el-input v-model="form.projectId" placeholder="必填" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="竞品名称" prop="competitorName"><el-input v-model="form.competitorName" placeholder="必填" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="厂商" prop="vendor"><el-input v-model="form.vendor" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="官网" prop="website"><el-input v-model="form.website" placeholder="https://" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="定价模式" prop="pricingModel"><el-input v-model="form.pricingModel" placeholder="SaaS/买断/混合" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="价格档" prop="pricingTier"><el-select v-model="form.pricingTier"><el-option v-for="d in tier_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="功能矩阵" prop="featureMatrix"><el-input v-model="form.featureMatrix" type="textarea" :rows="2" placeholder="JSON 或 CSV: 功能名→是否支持" /></el-form-item></el-col>
          <el-col :span="24"><el-divider content-position="left">SWOT 四象限分析</el-divider></el-col>
          <el-col :span="12"><el-form-item label="优势 (S)" prop="strengths"><el-input v-model="form.strengths" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="劣势 (W)" prop="weaknesses"><el-input v-model="form.weaknesses" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="机会 (O)" prop="opportunities"><el-input v-model="form.opportunities" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="威胁 (T)" prop="threats"><el-input v-model="form.threats" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="24"><el-divider content-position="left">AI 分析 + 自动监控</el-divider></el-col>
          <el-col :span="24"><el-form-item label="AI 分析报告" prop="aiAnalysisReport"><el-input v-model="form.aiAnalysisReport" type="textarea" :rows="3" placeholder="AI 自动综合分析 (爬取官网 + App Store)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="AI 生成" prop="aiGenerated"><el-select v-model="form.aiGenerated"><el-option label="是 (Y)" value="Y" /><el-option label="否 (N)" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="监控开启" prop="monitorEnabled"><el-select v-model="form.monitorEnabled"><el-option label="是 (Y)" value="Y" /><el-option label="否 (N)" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态" prop="status"><el-select v-model="form.status" :disabled="!form.competitiveId"><el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="监控关键词" prop="monitorKeywords"><el-input v-model="form.monitorKeywords" placeholder="多个关键词用,分隔" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="作者ID" prop="authorUserId"><el-input v-model="form.authorUserId" placeholder="user_id" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="Competitive" lang="ts">
import { listCompetitive, getCompetitive, addCompetitive, updateCompetitive, delCompetitive } from '../api'
import type { CompetitiveForm, CompetitiveQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_competitive_status: status_options,
  biz_competitive_tier: tier_options
} = toRefs<any>(proxy.useDict('biz_competitive_status', 'biz_competitive_tier'))

const list = ref<CompetitiveForm[]>([])
const loading = ref(true); const showSearch = ref(true); const ids = ref<(number | string)[]>([])
const single = ref(true); const multiple = ref(true); const total = ref(0)
const dialog = reactive({ title: '', visible: false }); const form = ref<CompetitiveForm>({})

const queryParams = ref<CompetitiveQuery>({
  pageNum: 1, pageSize: 10,
  competitiveNo: undefined, projectId: undefined, competitorName: undefined,
  vendor: undefined, pricingTier: undefined, aiGenerated: undefined,
  monitorEnabled: undefined, status: undefined, authorUserId: undefined
})
const rules = {
  competitorName: [{ required: true, message: '竞品名称不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }]
}

function getList() { loading.value = true; listCompetitive(queryParams.value).then((res: any) => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function reset() {
  form.value = {
    competitiveNo: undefined, projectId: undefined, competitorName: undefined,
    vendor: undefined, website: undefined, pricingModel: undefined, pricingTier: undefined,
    featureMatrix: undefined, strengths: undefined, weaknesses: undefined,
    opportunities: undefined, threats: undefined, aiAnalysisReport: undefined,
    aiGenerated: 'N', monitorEnabled: 'N', monitorKeywords: undefined,
    status: '00', authorUserId: undefined
  }
  proxy.resetForm('formRef')
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(s: CompetitiveForm[]) { ids.value = s.map(x => x.competitiveId!); single.value = s.length !== 1; multiple.value = !s.length }
function handleAdd() { reset(); dialog.title = '新增竞品情报'; dialog.visible = true }
function handleUpdate(row?: CompetitiveForm) {
  reset()
  const id = row?.competitiveId ?? ids.value[0]
  getCompetitive(id as number).then((res: any) => { form.value = res.data; dialog.title = '修改竞品情报'; dialog.visible = true })
}
function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.competitiveId ? updateCompetitive : addCompetitive
    fn(form.value).then(() => { proxy.$modal.msgSuccess(form.value.competitiveId ? '修改成功' : '新增成功'); dialog.visible = false; getList() })
  })
}
function cancel() { dialog.visible = false; reset() }
function handleDelete(row?: CompetitiveForm) {
  const toDelete = row?.competitiveId ? [row.competitiveId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delCompetitive(toDelete as number[]))
    .then(() => { getList(); proxy.$modal.msgSuccess('删除成功') })
    .catch(() => {})
}
function handleExport() { proxy.download('business/competitive/export', { ...queryParams.value }, '竞品_' + new Date().getTime() + '.xlsx') }

getList()
</script>
