<template>
  <div class="app-container">

    <!-- 搜索 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="立项编号" prop="inceptionNo">
        <el-input v-model="queryParams.inceptionNo" placeholder="请输入立项编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目名称" prop="projectName">
        <el-input v-model="queryParams.projectName" placeholder="请输入项目名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="业务线" prop="businessLine">
        <el-select v-model="queryParams.businessLine" placeholder="全部" clearable style="width:130px">
          <el-option v-for="d in biz_line_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:inception:add']">新建立项</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:inception:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:inception:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:inception:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="立项编号" align="center" prop="inceptionNo" width="150" />
      <el-table-column label="项目名称" align="center" prop="projectName" :show-overflow-tooltip="true" min-width="180" />
      <el-table-column label="业务线" align="center" prop="businessLine" width="110">
        <template #default="scope">
          <dict-tag :options="biz_line_options" :value="scope.row.businessLine" />
        </template>
      </el-table-column>
      <el-table-column label="项目类型" align="center" prop="inceptionType" width="110">
        <template #default="scope">
          <dict-tag :options="inception_type_options" :value="scope.row.inceptionType" />
        </template>
      </el-table-column>
      <el-table-column label="工期(月)" align="center" prop="estimatedDurationMonths" width="90" />
      <el-table-column label="AI建议书" align="center" prop="aiGenerated" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.aiGenerated === 'Y' ? 'success' : 'info'" size="small">
            {{ scope.row.aiGenerated === 'Y' ? '已生成' : '未生成' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="status_options" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)" v-hasPermi="['business:inception:query']">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:inception:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:inception:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增 / 编辑对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="760px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="项目名称" prop="projectName">
              <el-input v-model="form.projectName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务线" prop="businessLine">
              <el-select v-model="form.businessLine" placeholder="请选择业务线" style="width:100%">
                <el-option v-for="d in biz_line_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目类型" prop="inceptionType">
              <el-select v-model="form.inceptionType" placeholder="请选择类型" style="width:100%">
                <el-option v-for="d in inception_type_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="背景与诉求" prop="background">
              <el-input v-model="form.background" type="textarea" :rows="4" placeholder="说明项目背景、用户诉求、期望目标…" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计工期(月)" prop="estimatedDurationMonths">
              <el-input-number v-model="form.estimatedDurationMonths as number" :min="1" :max="60" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="团队规模" prop="estimatedTeam">
              <el-input v-model="form.estimatedTeam" placeholder="如 产品×1 前端×2 后端×3 测试×2" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="其他补充说明" />
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

    <!-- 详情 + AI 建议书 -->
    <el-dialog title="立项详情 & AI建议书" v-model="detailVisible" width="900px" append-to-body>
      <el-row :gutter="20" v-if="currentRow">
        <!-- 左：基本信息 + AI 操作 -->
        <el-col :span="10">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="立项编号">{{ currentRow.inceptionNo }}</el-descriptions-item>
            <el-descriptions-item label="项目名称">{{ currentRow.projectName }}</el-descriptions-item>
            <el-descriptions-item label="业务线">
              <dict-tag :options="biz_line_options" :value="currentRow.businessLine" />
            </el-descriptions-item>
            <el-descriptions-item label="项目类型">
              <dict-tag :options="inception_type_options" :value="currentRow.inceptionType" />
            </el-descriptions-item>
            <el-descriptions-item label="预计工期">{{ currentRow.estimatedDurationMonths }} 个月</el-descriptions-item>
            <el-descriptions-item label="团队规模">{{ currentRow.estimatedTeam }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <dict-tag :options="status_options" :value="currentRow.status" />
            </el-descriptions-item>
          </el-descriptions>

          <div class="mt16">
            <el-button
              type="primary" icon="MagicStick" :loading="aiLoading"
              @click="handleAiGenerate"
              v-hasPermi="['business:inception:edit']"
              style="width:100%"
            >✨ AI 生成立项建议书</el-button>
          </div>

          <!-- 风险区 -->
          <div v-if="currentRow.aiRisks" class="risk-card mt16">
            <div class="risk-title">⚠️ AI 风险识别</div>
            <pre class="risk-content">{{ currentRow.aiRisks }}</pre>
          </div>

          <!-- 状态流转按钮 -->
          <div class="mt16 flow-btns">
            <el-button
              v-if="currentRow.status === '00'"
              type="warning" plain size="small"
              @click="changeStatus('01')"
              v-hasPermi="['business:inception:edit']"
            >📤 提交审批</el-button>
            <el-button
              v-if="currentRow.status === '01'"
              type="info" plain size="small"
              @click="changeStatus('02')"
              v-hasPermi="['business:inception:edit']"
            >🔄 进入审批</el-button>
            <el-button
              v-if="currentRow.status === '02'"
              type="success" plain size="small"
              @click="changeStatus('03')"
              v-hasPermi="['business:inception:edit']"
            >✅ 批准</el-button>
            <el-button
              v-if="currentRow.status === '02'"
              type="danger" plain size="small"
              @click="handleReject"
              v-hasPermi="['business:inception:edit']"
            >❌ 驳回</el-button>
            <el-button
              v-if="currentRow.status === '04'"
              type="primary" plain size="small"
              @click="changeStatus('00')"
              v-hasPermi="['business:inception:edit']"
            >🔁 打回重写</el-button>
          </div>
        </el-col>

        <!-- 右：AI 建议书 -->
        <el-col :span="14">
          <div class="proposal-panel">
            <div class="proposal-title">📋 立项建议书</div>
            <div v-if="!currentRow.aiProposalContent" class="proposal-placeholder">
              <div style="font-size:36px;margin-bottom:10px">🚀</div>
              <div>点击「AI 生成立项建议书」开始</div>
            </div>
            <div v-else class="proposal-body">
              <pre class="proposal-md">{{ currentRow.aiProposalContent }}</pre>
            </div>
          </div>
        </el-col>
      </el-row>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 驳回原因弹窗 -->
    <el-dialog title="驳回原因" v-model="rejectVisible" width="420px" append-to-body>
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因（必填）" />
      <template #footer>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
        <el-button @click="rejectVisible = false">取消</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { listInception, getInception, addInception, updateInception, delInception, aiGenerateInception } from '../api'
import type { InceptionForm, InceptionQuery } from '../types'

defineOptions({ name: 'Inception' })

const { proxy } = getCurrentInstance() as any
const {
  biz_inception_biz_line: biz_line_options,
  biz_inception_type: inception_type_options,
  biz_inception_status: status_options
} = toRefs<any>(proxy.useDict('biz_inception_biz_line', 'biz_inception_type', 'biz_inception_status'))

const list = ref<InceptionForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const dialog = reactive({ title: '', visible: false })
const form = ref<InceptionForm>({})

const detailVisible = ref(false)
const currentRow = ref<InceptionForm | null>(null)
const aiLoading = ref(false)
const rejectVisible = ref(false)
const rejectReason = ref('')

const queryParams = ref<InceptionQuery>({
  pageNum: 1, pageSize: 10,
  inceptionNo: undefined, projectName: undefined,
  businessLine: undefined, status: undefined
})

const rules = {
  projectName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listInception(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = { projectName: undefined, businessLine: undefined, inceptionType: undefined, background: undefined, estimatedDurationMonths: 6, estimatedTeam: undefined, remark: undefined }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection: InceptionForm[]) {
  ids.value = selection.map(item => item.inceptionId!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); dialog.title = '新建立项'; dialog.visible = true }

function handleUpdate(row?: InceptionForm) {
  reset()
  const id = row?.inceptionId ?? ids.value[0]
  getInception(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '修改立项'
    dialog.visible = true
  })
}

function handleDetail(row: InceptionForm) {
  getInception(row.inceptionId!).then((res: any) => {
    currentRow.value = res.data
    detailVisible.value = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.inceptionId ? updateInception : addInception
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.inceptionId ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: InceptionForm) {
  const toDelete = row?.inceptionId ? [row.inceptionId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 条立项记录？').then(() =>
    delInception(toDelete as number[])
  ).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('business/inception/export', { ...queryParams.value }, '项目立项_' + new Date().getTime() + '.xlsx')
}

function handleAiGenerate() {
  if (!currentRow.value?.inceptionId) return
  aiLoading.value = true
  aiGenerateInception(currentRow.value.inceptionId).then((res: any) => {
    currentRow.value = { ...currentRow.value, ...res.data }
    proxy.$modal.msgSuccess('AI 立项建议书生成成功')
    getList()
  }).finally(() => { aiLoading.value = false })
}

function changeStatus(newStatus: string) {
  if (!currentRow.value?.inceptionId) return
  updateInception({ inceptionId: currentRow.value.inceptionId, status: newStatus }).then(() => {
    currentRow.value!.status = newStatus
    proxy.$modal.msgSuccess('状态已更新')
    getList()
  })
}

function handleReject() {
  rejectReason.value = ''
  rejectVisible.value = true
}

function confirmReject() {
  if (!rejectReason.value.trim()) {
    proxy.$modal.msgError('驳回原因不能为空')
    return
  }
  updateInception({ inceptionId: currentRow.value!.inceptionId, status: '04', rejectReason: rejectReason.value }).then(() => {
    currentRow.value!.status = '04'
    currentRow.value!.rejectReason = rejectReason.value
    rejectVisible.value = false
    proxy.$modal.msgSuccess('已驳回')
    getList()
  })
}

getList()
</script>

<style scoped>
.mt16 { margin-top: 16px; }

.risk-card {
  background: #fffbeb;
  border: 1px solid #fcd34d;
  border-radius: 8px;
  padding: 12px;
}
.risk-title { font-weight: 700; font-size: 13px; color: #b45309; margin-bottom: 6px; }
.risk-content { font-size: 12px; color: #78350f; white-space: pre-wrap; margin: 0; }

.flow-btns { display: flex; flex-wrap: wrap; gap: 8px; }

.proposal-panel {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  min-height: 380px;
  padding: 16px;
  display: flex;
  flex-direction: column;
}
.proposal-title { font-weight: 700; font-size: 14px; margin-bottom: 12px; }
.proposal-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 14px;
}
.proposal-body { flex: 1; overflow-y: auto; }
.proposal-md {
  font-size: 12.5px;
  line-height: 1.7;
  white-space: pre-wrap;
  margin: 0;
  font-family: inherit;
}
</style>
