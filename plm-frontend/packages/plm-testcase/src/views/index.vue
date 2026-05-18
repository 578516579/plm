<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="用例编号" prop="testcaseNo">
        <el-input v-model="queryParams.testcaseNo" placeholder="TC-..." clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目ID" prop="projectId">
        <el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="全部" clearable>
          <el-option v-for="d in category_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-select v-model="queryParams.priority" placeholder="全部" clearable>
          <el-option v-for="d in priority_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="自动化" prop="isAutomated">
        <el-select v-model="queryParams.isAutomated" placeholder="全部" clearable>
          <el-option label="是 (Y)" value="Y" />
          <el-option label="否 (N)" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:testcase:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:testcase:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="VideoPlay" :disabled="single" @click="handleExecute()" v-hasPermi="['business:testcase:execute']">执行</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:testcase:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Download" @click="handleExport" v-hasPermi="['business:testcase:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="testcaseId" width="80" />
      <el-table-column label="用例编号" align="center" prop="testcaseNo" width="160" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="分类" align="center" prop="category" width="100">
        <template #default="scope"><dict-tag :options="category_options" :value="scope.row.category" /></template>
      </el-table-column>
      <el-table-column label="优先级" align="center" prop="priority" width="100">
        <template #default="scope"><dict-tag :options="priority_options" :value="scope.row.priority" /></template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope"><dict-tag :options="status_options" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="自动化" align="center" prop="isAutomated" width="80" />
      <el-table-column label="执行次数" align="center" prop="executionCount" width="80" />
      <el-table-column label="最近执行" align="center" width="160">
        <template #default="scope"><span v-if="scope.row.lastExecutedAt">{{ parseTime(scope.row.lastExecutedAt) }}</span><span v-else>—</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:testcase:edit']">修改</el-button>
          <el-button link type="warning" icon="VideoPlay" @click="handleExecute(scope.row)" v-hasPermi="['business:testcase:execute']">执行</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 编辑对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="780px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="用例编号" prop="testcaseNo">
              <el-input v-model="form.testcaseNo" placeholder="留空自动生成 TC-YYYY-NNNN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input v-model="form.projectId" placeholder="必填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求ID" prop="requirementId">
              <el-input v-model="form.requirementId" placeholder="可空" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="自动化" prop="isAutomated">
              <el-radio-group v-model="form.isAutomated">
                <el-radio value="N">否</el-radio>
                <el-radio value="Y">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="form.isAutomated === 'Y'">
            <el-form-item label="脚本路径" prop="automationScriptPath">
              <el-input v-model="form.automationScriptPath" placeholder="如 plm-frontend/e2e/project.spec.ts" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category"><el-option v-for="d in category_options" :key="d.value" :label="d.label" :value="d.value" /></el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority"><el-option v-for="d in priority_options" :key="d.value" :label="d.label" :value="d.value" /></el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" :disabled="!form.testcaseId">
                <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="前置条件" prop="preconditions"><el-input v-model="form.preconditions" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="步骤" prop="steps"><el-input v-model="form.steps" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="期望结果" prop="expectedResult"><el-input v-model="form.expectedResult" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="实际结果" prop="actualResult"><el-input v-model="form.actualResult" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="标签" prop="tags"><el-input v-model="form.tags" placeholder="CSV: smoke,regression" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="cancel">取消</el-button>
      </template>
    </el-dialog>

    <!-- 执行对话框 -->
    <el-dialog title="执行用例" v-model="execDialog.visible" width="540px" append-to-body>
      <el-form :model="execForm" label-width="100px">
        <el-form-item label="用例编号"><el-tag>{{ execForm.testcaseNo }}</el-tag></el-form-item>
        <el-form-item label="执行结果">
          <el-radio-group v-model="execForm.status">
            <el-radio value="03">已通过</el-radio>
            <el-radio value="04">已失败</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="实际结果"><el-input v-model="execForm.actualResult" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmExecute">提交</el-button>
        <el-button @click="execDialog.visible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TestCase" lang="ts">
import { listTestCase, getTestCase, addTestCase, updateTestCase, delTestCase, executeTestCase } from '../api'
import type { TestCaseForm, TestCaseQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_testcase_category: category_options,
  biz_testcase_priority: priority_options,
  biz_testcase_status: status_options
} = toRefs<any>(proxy.useDict('biz_testcase_category', 'biz_testcase_priority', 'biz_testcase_status'))

const list = ref<TestCaseForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const dialog = reactive({ title: '', visible: false })
const form = ref<TestCaseForm>({})
const execDialog = reactive({ visible: false })
const execForm = ref<any>({ testcaseId: null, testcaseNo: '', status: '03', actualResult: '' })

const queryParams = ref<TestCaseQuery>({
  pageNum: 1, pageSize: 10,
  testcaseNo: undefined, projectId: undefined, category: undefined,
  priority: undefined, status: undefined, isAutomated: undefined
})

const rules = {
  title: [{ required: true, message: '用例标题不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }],
  steps: [{ required: true, message: '测试步骤不能为空', trigger: 'blur' }],
  expectedResult: [{ required: true, message: '期望结果不能为空', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listTestCase(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    testcaseNo: undefined, projectId: undefined, requirementId: undefined,
    title: undefined, description: undefined,
    category: '01', priority: '01', status: '00',
    preconditions: undefined, steps: undefined, expectedResult: undefined, actualResult: undefined,
    isAutomated: 'N', automationScriptPath: undefined, tags: undefined
  }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(s: TestCaseForm[]) {
  ids.value = s.map(item => item.testcaseId!)
  single.value = s.length !== 1
  multiple.value = !s.length
}

function handleAdd() { reset(); dialog.title = '新增用例'; dialog.visible = true }

function handleUpdate(row?: TestCaseForm) {
  reset()
  const id = row?.testcaseId ?? ids.value[0]
  getTestCase(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '修改用例'
    dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.testcaseId ? updateTestCase : addTestCase
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.testcaseId ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: TestCaseForm) {
  const toDel = row?.testcaseId ? [row.testcaseId] : ids.value
  proxy.$modal.confirm('确认删除 ' + toDel.length + ' 项?').then(() => delTestCase(toDel as number[]))
    .then(() => { getList(); proxy.$modal.msgSuccess('删除成功') })
    .catch(() => {})
}

function handleExecute(row?: TestCaseForm) {
  const target = row || list.value.find((x: any) => x.testcaseId === ids.value[0])
  if (!target) return
  // 业务规则: 必须先推到"执行中"才能 execute
  if (target.status !== '02') {
    proxy.$modal.confirm('用例状态非「执行中」,先推到「执行中」再 execute?').then(() => {
      updateTestCase({ testcaseId: target.testcaseId, status: '02' }).then(() => {
        execForm.value = { testcaseId: target.testcaseId, testcaseNo: target.testcaseNo, status: '03', actualResult: '' }
        execDialog.visible = true
      })
    }).catch(() => {})
  } else {
    execForm.value = { testcaseId: target.testcaseId, testcaseNo: target.testcaseNo, status: '03', actualResult: '' }
    execDialog.visible = true
  }
}

function confirmExecute() {
  executeTestCase(execForm.value.testcaseId, {
    status: execForm.value.status,
    actualResult: execForm.value.actualResult
  }).then(() => {
    proxy.$modal.msgSuccess('执行结果已记录')
    execDialog.visible = false
    getList()
  })
}

function handleExport() {
  proxy.download('business/testcase/export', { ...queryParams.value }, '测试用例_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
