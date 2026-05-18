<template>
  <div class="app-container">
    <!-- 搜索条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="方案编号" prop="testplanNo">
        <el-input v-model="queryParams.testplanNo" placeholder="TP-..." clearable @keyup.enter="handleQuery" />
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
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="AI 生成" prop="aiGenerated">
        <el-select v-model="queryParams.aiGenerated" placeholder="全部" clearable>
          <el-option label="是 (Y)" value="Y" />
          <el-option label="否 (N)" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item label="作者" prop="authorUserId">
        <el-input v-model="queryParams.authorUserId" placeholder="user_id" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:testplan:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:testplan:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:testplan:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:testplan:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="testplanId" width="80" />
      <el-table-column label="编号" align="center" prop="testplanNo" width="160" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="迭代" align="center" prop="sprintId" width="80" />
      <el-table-column label="测试类型" align="center" prop="testTypes" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="周期" align="center" prop="testCycleDays" width="80">
        <template #default="scope">
          <span v-if="scope.row.testCycleDays != null">{{ scope.row.testCycleDays }}天</span>
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
      <el-table-column label="作者" align="center" prop="authorUserId" width="80" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:testplan:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:testplan:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="820px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="方案编号" prop="testplanNo">
              <el-input v-model="form.testplanNo" placeholder="留空自动生成 TP-YYYY-NNNN" />
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
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" :disabled="!form.testplanId" placeholder="请选择">
                <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="方案标题" prop="title">
              <el-input v-model="form.title" placeholder="一句话概述本次测试目标" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="测试类型" prop="testTypes">
              <el-input v-model="form.testTypes" placeholder="如: 功能/性能/安全 (多个用,)" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="测试周期" prop="testCycleDays">
              <el-input-number v-model="form.testCycleDays" :min="1" :max="60" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="测试范围" prop="scope">
              <el-input v-model="form.scope" type="textarea" :rows="2" placeholder="覆盖的功能模块" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="测试策略" prop="strategy">
              <el-input v-model="form.strategy" type="textarea" :rows="2" placeholder="测试方法 / 入口准则 / 出口准则" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="工具推荐" prop="toolsRecommended">
              <el-input v-model="form.toolsRecommended" type="textarea" :rows="2" placeholder="AI 建议或人工填" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="资源计划" prop="resourcesPlan">
              <el-input v-model="form.resourcesPlan" type="textarea" :rows="2" placeholder="人力/环境/数据准备" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="风险评估" prop="riskAssessment">
              <el-input v-model="form.riskAssessment" type="textarea" :rows="2" placeholder="风险点 + 缓解方案" />
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
          <el-col :span="12">
            <el-form-item label="作者ID" prop="authorUserId">
              <el-input v-model="form.authorUserId" placeholder="user_id" />
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

<script setup name="TestPlan" lang="ts">
import { listTestPlan, getTestPlan, addTestPlan, updateTestPlan, delTestPlan } from '../api'
import type { TestPlanForm, TestPlanQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const { biz_testplan_status: status_options } = toRefs<any>(proxy.useDict('biz_testplan_status'))

const list = ref<TestPlanForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const dialog = reactive({ title: '', visible: false })
const form = ref<TestPlanForm>({})

const queryParams = ref<TestPlanQuery>({
  pageNum: 1, pageSize: 10,
  testplanNo: undefined, projectId: undefined, sprintId: undefined,
  title: undefined, aiGenerated: undefined, status: undefined, authorUserId: undefined
})

const rules = {
  title: [{ required: true, message: '方案标题不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }],
  testTypes: [{ required: true, message: '测试类型不能为空', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listTestPlan(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    testplanNo: undefined, projectId: undefined, sprintId: undefined,
    title: undefined, testTypes: undefined, testCycleDays: 10,
    scope: undefined, strategy: undefined,
    toolsRecommended: undefined, resourcesPlan: undefined, riskAssessment: undefined,
    aiGenerated: 'N', status: '00', authorUserId: undefined
  }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection: TestPlanForm[]) {
  ids.value = selection.map(item => item.testplanId!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); dialog.title = '新增测试方案'; dialog.visible = true }

function handleUpdate(row?: TestPlanForm) {
  reset()
  const id = row?.testplanId ?? ids.value[0]
  getTestPlan(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '修改测试方案'
    dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.testplanId ? updateTestPlan : addTestPlan
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.testplanId ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: TestPlanForm) {
  const toDelete = row?.testplanId ? [row.testplanId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delTestPlan(toDelete as number[]))
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => {})
}

function handleExport() {
  proxy.download('business/testplan/export', { ...queryParams.value }, '测试方案_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
