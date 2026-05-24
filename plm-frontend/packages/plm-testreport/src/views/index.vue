<template>
  <div class="app-container">
    <!-- 搜索条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="报告编号" prop="testreportNo">
        <el-input v-model="queryParams.testreportNo" placeholder="TR-..." clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目ID" prop="projectId">
        <el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="迭代ID" prop="sprintId">
        <el-input v-model="queryParams.sprintId" placeholder="可空" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="方案ID" prop="testplanId">
        <el-input v-model="queryParams.testplanId" placeholder="可空" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="标题模糊" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="风险" prop="riskLevel">
        <el-select v-model="queryParams.riskLevel" placeholder="全部" clearable>
          <el-option v-for="d in risk_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="AI 生成" prop="aiGenerated">
        <el-select v-model="queryParams.aiGenerated" placeholder="全部" clearable>
          <el-option label="是 (Y)" value="Y" />
          <el-option label="否 (N)" value="N" />
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

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:testreport:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:testreport:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:testreport:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:testreport:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="testreportId" width="80" />
      <el-table-column label="编号" align="center" prop="testreportNo" width="160" />
      <el-table-column label="标题" align="left" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="迭代" align="center" prop="sprintId" width="80" />
      <el-table-column label="总用例" align="center" prop="totalCases" width="80" />
      <el-table-column label="通过" align="center" prop="passedCases" width="80">
        <template #default="scope">
          <span style="color:#67c23a">{{ scope.row.passedCases ?? 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="失败" align="center" prop="failedCases" width="80">
        <template #default="scope">
          <span style="color:#f56c6c">{{ scope.row.failedCases ?? 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="覆盖率" align="center" prop="coverageRate" width="90">
        <template #default="scope">
          <span v-if="scope.row.coverageRate != null">{{ scope.row.coverageRate }}%</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="风险" align="center" prop="riskLevel" width="110">
        <template #default="scope">
          <el-tag v-if="scope.row.riskLevel === 'green'" type="success">绿 (低)</el-tag>
          <el-tag v-else-if="scope.row.riskLevel === 'yellow'" type="warning">黄 (中)</el-tag>
          <el-tag v-else-if="scope.row.riskLevel === 'red'" type="danger">红 (高)</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="AI" align="center" prop="aiGenerated" width="70">
        <template #default="scope">
          <el-tag v-if="scope.row.aiGenerated === 'Y'" type="warning" size="small">AI</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="status_options" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:testreport:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:testreport:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="880px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="报告编号" prop="testreportNo">
              <el-input v-model="form.testreportNo" placeholder="留空自动生成 TR-YYYY-NNNN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input v-model="form.projectId" placeholder="必填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="迭代ID" prop="sprintId">
              <el-input v-model="form.sprintId" placeholder="可空" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="测试方案ID" prop="testplanId">
              <el-input v-model="form.testplanId" placeholder="可空" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="报告标题" prop="title">
              <el-input v-model="form.title" placeholder="本次测试的总结性标题" />
            </el-form-item>
          </el-col>

          <!-- 测试统计 -->
          <el-col :span="24">
            <el-divider content-position="left">测试统计</el-divider>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总用例数" prop="totalCases">
              <el-input-number v-model="form.totalCases" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="通过" prop="passedCases">
              <el-input-number v-model="form.passedCases" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="失败" prop="failedCases">
              <el-input-number v-model="form.failedCases" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="覆盖率(%)" prop="coverageRate">
              <el-input-number v-model="form.coverageRate" :min="0" :max="100" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>

          <!-- 缺陷统计 -->
          <el-col :span="24">
            <el-divider content-position="left">缺陷统计（按优先级）</el-divider>
          </el-col>
          <el-col :span="8">
            <el-form-item label="P0 缺陷" prop="p0Defects">
              <el-input-number v-model="form.p0Defects" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="P1 缺陷" prop="p1Defects">
              <el-input-number v-model="form.p1Defects" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="P2 缺陷" prop="p2Defects">
              <el-input-number v-model="form.p2Defects" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="缺陷摘要" prop="defectSummary">
              <el-input v-model="form.defectSummary" type="textarea" :rows="2" placeholder="主要缺陷概述" />
            </el-form-item>
          </el-col>

          <!-- 上线风险评估 -->
          <el-col :span="24">
            <el-divider content-position="left">上线风险评估</el-divider>
          </el-col>
          <el-col :span="12">
            <el-form-item label="风险等级" prop="riskLevel">
              <el-select v-model="form.riskLevel" placeholder="请选择">
                <el-option v-for="d in risk_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="AI 生成" prop="aiGenerated">
              <el-select v-model="form.aiGenerated" placeholder="Y/N">
                <el-option label="是 (Y)" value="Y" />
                <el-option label="否 (N)" value="N" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="风险评价" prop="riskEvaluation">
              <el-input v-model="form.riskEvaluation" type="textarea" :rows="2" placeholder="对风险等级的详细说明" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="改进建议" prop="recommendations">
              <el-input v-model="form.recommendations" type="textarea" :rows="2" placeholder="后续改进措施" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="审核人ID" prop="reviewerUserId">
              <el-input v-model="form.reviewerUserId" placeholder="user_id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" :disabled="!form.testreportId" placeholder="请选择">
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

<script setup name="TestReport" lang="ts">
import { listTestReport, getTestReport, addTestReport, updateTestReport, delTestReport } from '../api'
import type { TestReportForm, TestReportQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_testreport_status: status_options,
  biz_testreport_risk: risk_options
} = toRefs<any>(proxy.useDict('biz_testreport_status', 'biz_testreport_risk'))

const list = ref<TestReportForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const dialog = reactive({ title: '', visible: false })
const form = ref<TestReportForm>({})

const queryParams = ref<TestReportQuery>({
  pageNum: 1, pageSize: 10,
  testreportNo: undefined, projectId: undefined, sprintId: undefined, testplanId: undefined,
  title: undefined, riskLevel: undefined, aiGenerated: undefined, status: undefined
})

const rules = {
  title: [{ required: true, message: '报告标题不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }],
  riskLevel: [{ required: true, message: '风险等级不能为空', trigger: 'change' }]
}

function getList() {
  loading.value = true
  listTestReport(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    testreportNo: undefined, projectId: undefined, sprintId: undefined, testplanId: undefined,
    title: undefined,
    totalCases: 0, passedCases: 0, failedCases: 0, coverageRate: undefined,
    defectSummary: undefined, p0Defects: 0, p1Defects: 0, p2Defects: 0,
    riskLevel: 'green', riskEvaluation: undefined, recommendations: undefined,
    aiGenerated: 'N', status: '00', reviewerUserId: undefined
  }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection: TestReportForm[]) {
  ids.value = selection.map(item => item.testreportId!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); dialog.title = '新增测试报告'; dialog.visible = true }

function handleUpdate(row?: TestReportForm) {
  reset()
  const id = row?.testreportId ?? ids.value[0]
  getTestReport(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '修改测试报告'
    dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.testreportId ? updateTestReport : addTestReport
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.testreportId ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: TestReportForm) {
  const toDelete = row?.testreportId ? [row.testreportId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delTestReport(toDelete as number[]))
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => {})
}

function handleExport() {
  proxy.download('business/testreport/export', { ...queryParams.value }, '测试报告_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
