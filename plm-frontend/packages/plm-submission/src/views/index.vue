<template>
  <div class="app-container">
    <!-- 搜索条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="提测编号" prop="submissionNo">
        <el-input v-model="queryParams.submissionNo" placeholder="SUB-..." clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目ID" prop="projectId">
        <el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="迭代ID" prop="sprintId">
        <el-input v-model="queryParams.sprintId" placeholder="可空" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="标题模糊" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="环境" prop="environment">
        <el-input v-model="queryParams.environment" placeholder="dev/sit/uat" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="门禁" prop="qualityGatePassed">
        <el-select v-model="queryParams.qualityGatePassed" placeholder="全部" clearable>
          <el-option label="通过 (Y)" value="Y" />
          <el-option label="未通过 (N)" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item label="提交人" prop="submitterUserId">
        <el-input v-model="queryParams.submitterUserId" placeholder="user_id" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:submission:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:submission:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:submission:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:submission:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="submissionId" width="80" />
      <el-table-column label="编号" align="center" prop="submissionNo" width="180" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="迭代" align="center" prop="sprintId" width="80" />
      <el-table-column label="环境" align="center" prop="environment" width="100" />
      <el-table-column label="覆盖率" align="center" prop="unitTestCoverage" width="90">
        <template #default="scope">
          <span v-if="scope.row.unitTestCoverage != null">{{ scope.row.unitTestCoverage }}%</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="门禁" align="center" prop="qualityGatePassed" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.qualityGatePassed === 'Y'" type="success">通过</el-tag>
          <el-tag v-else-if="scope.row.qualityGatePassed === 'N'" type="danger">未过</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="status_options" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="提交人" align="center" prop="submitterUserId" width="80" />
      <el-table-column label="提交时间" align="center" prop="submittedAt" width="180">
        <template #default="scope"><span>{{ parseTime(scope.row.submittedAt) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:submission:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:submission:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="820px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="提测编号" prop="submissionNo">
              <el-input v-model="form.submissionNo" placeholder="留空自动生成 SUB-YYYY-NNNN" />
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
            <el-form-item label="环境" prop="environment">
              <el-input v-model="form.environment" placeholder="dev/sit/uat/prod" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="提测标题" prop="title">
              <el-input v-model="form.title" placeholder="一句话描述本次提测内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="范围" prop="scope">
              <el-input v-model="form.scope" type="textarea" :rows="2" placeholder="本次提测范围（功能列表/模块）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期望测试天数" prop="expectedTestDays">
              <el-input-number v-model="form.expectedTestDays" :min="1" :max="60" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" :disabled="!form.submissionId" placeholder="请选择">
                <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="风险备注" prop="riskNotes">
              <el-input v-model="form.riskNotes" type="textarea" :rows="2" placeholder="已知风险点" />
            </el-form-item>
          </el-col>

          <!-- AI 质量门禁 4 项 -->
          <el-col :span="24">
            <el-divider content-position="left">AI 质量门禁（4 项,任意一项 N 则门禁失败）</el-divider>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单测覆盖率(%)" prop="unitTestCoverage">
              <el-input-number v-model="form.unitTestCoverage" :min="0" :max="100" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="代码扫描通过" prop="codeScanPassed">
              <el-select v-model="form.codeScanPassed" placeholder="Y/N">
                <el-option label="是 (Y)" value="Y" />
                <el-option label="否 (N)" value="N" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="PRD 完整" prop="prdCompleted">
              <el-select v-model="form.prdCompleted" placeholder="Y/N">
                <el-option label="是 (Y)" value="Y" />
                <el-option label="否 (N)" value="N" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="API 文档更新" prop="apiDocUpdated">
              <el-select v-model="form.apiDocUpdated" placeholder="Y/N">
                <el-option label="是 (Y)" value="Y" />
                <el-option label="否 (N)" value="N" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="审核人ID" prop="reviewerUserId">
              <el-input v-model="form.reviewerUserId" placeholder="user_id" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="退回原因" prop="rejectReason">
              <el-input v-model="form.rejectReason" type="textarea" :rows="2" placeholder="状态进入「已退回(04)」时必填" />
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

<script setup name="Submission" lang="ts">
import { listSubmission, getSubmission, addSubmission, updateSubmission, delSubmission } from '../api'
import type { SubmissionForm, SubmissionQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const { biz_submission_status: status_options } = toRefs<any>(proxy.useDict('biz_submission_status'))

const list = ref<SubmissionForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const dialog = reactive({ title: '', visible: false })
const form = ref<SubmissionForm>({})

const queryParams = ref<SubmissionQuery>({
  pageNum: 1, pageSize: 10,
  submissionNo: undefined, projectId: undefined, sprintId: undefined,
  title: undefined, environment: undefined, status: undefined,
  qualityGatePassed: undefined, submitterUserId: undefined
})

const rules = {
  title: [{ required: true, message: '提测标题不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listSubmission(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    submissionNo: undefined, projectId: undefined, sprintId: undefined,
    title: undefined, scope: undefined, environment: undefined,
    expectedTestDays: undefined, riskNotes: undefined,
    unitTestCoverage: undefined, codeScanPassed: undefined,
    prdCompleted: undefined, apiDocUpdated: undefined,
    qualityGatePassed: undefined, status: '00',
    rejectReason: undefined, reviewerUserId: undefined
  }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection: SubmissionForm[]) {
  ids.value = selection.map(item => item.submissionId!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); dialog.title = '新增提测'; dialog.visible = true }

function handleUpdate(row?: SubmissionForm) {
  reset()
  const id = row?.submissionId ?? ids.value[0]
  getSubmission(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '修改提测'
    dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.submissionId ? updateSubmission : addSubmission
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.submissionId ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: SubmissionForm) {
  const toDelete = row?.submissionId ? [row.submissionId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delSubmission(toDelete as number[]))
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => {})
}

function handleExport() {
  proxy.download('business/submission/export', { ...queryParams.value }, '提测_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
