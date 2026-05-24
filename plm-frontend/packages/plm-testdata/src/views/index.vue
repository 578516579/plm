<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="数据集编号" prop="testdataNo"><el-input v-model="queryParams.testdataNo" placeholder="TD-..." clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="项目ID" prop="projectId"><el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="任务标题" prop="title"><el-input v-model="queryParams.title" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="目标表" prop="targetTable">
        <el-select v-model="queryParams.targetTable" placeholder="全部" clearable filterable>
          <el-option v-for="d in table_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="输出格式" prop="outputFormat">
        <el-select v-model="queryParams.outputFormat" placeholder="全部" clearable>
          <el-option v-for="d in format_options" :key="d.value" :label="d.label" :value="d.value" />
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
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:testdata:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:testdata:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:testdata:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:testdata:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="testdataId" width="80" />
      <el-table-column label="数据集编号" align="center" prop="testdataNo" width="160" />
      <el-table-column label="任务标题" align="left" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="目标表" align="center" prop="targetTable" width="160" :show-overflow-tooltip="true">
        <template #default="scope"><dict-tag :options="table_options" :value="scope.row.targetTable" /></template>
      </el-table-column>
      <el-table-column label="数量" align="center" prop="generateCount" width="80" />
      <el-table-column label="格式" align="center" prop="outputFormat" width="100">
        <template #default="scope"><dict-tag :options="format_options" :value="scope.row.outputFormat" /></template>
      </el-table-column>
      <el-table-column label="AI" align="center" prop="aiGenerated" width="60">
        <template #default="scope"><el-tag v-if="scope.row.aiGenerated === 'Y'" type="warning" size="small">AI</el-tag></template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope"><dict-tag :options="status_options" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="生成时间" align="center" prop="generatedAt" width="160">
        <template #default="scope"><span>{{ parseTime(scope.row.generatedAt) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:testdata:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:testdata:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="860px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="12"><el-form-item label="数据集编号" prop="testdataNo"><el-input v-model="form.testdataNo" placeholder="留空自动生成 TD-YYYY-NNNN" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目ID" prop="projectId"><el-input v-model="form.projectId" placeholder="必填" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="任务标题" prop="title"><el-input v-model="form.title" placeholder="如: 茶园传感器 200 条 7 天连续数据" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="目标表" prop="targetTable"><el-select v-model="form.targetTable" filterable><el-option v-for="d in table_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="生成数量" prop="generateCount"><el-input-number v-model="form.generateCount" :min="1" :max="10000" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="输出格式" prop="outputFormat"><el-select v-model="form.outputFormat"><el-option v-for="d in format_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="字段语义" prop="fieldSemantics"><el-input v-model="form.fieldSemantics" type="textarea" :rows="2" placeholder="字段名 → 业务含义,JSON 或 YAML" /></el-form-item></el-col>
          <el-col :span="24"><el-divider content-position="left">AgriKB 农业规则</el-divider></el-col>
          <el-col :span="12"><el-form-item label="中国坐标范围" prop="ruleChinaCoord"><el-select v-model="form.ruleChinaCoord"><el-option label="启用" value="Y" /><el-option label="关闭" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="时序连续性" prop="ruleTimeContinuity"><el-select v-model="form.ruleTimeContinuity"><el-option label="启用" value="Y" /><el-option label="关闭" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="传感器值域" prop="ruleSensorRange"><el-select v-model="form.ruleSensorRange"><el-option label="启用" value="Y" /><el-option label="关闭" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="包含异常值" prop="ruleIncludeOutliers"><el-select v-model="form.ruleIncludeOutliers"><el-option label="包含 5%" value="Y" /><el-option label="不包含" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="24"><el-divider content-position="left">生成结果</el-divider></el-col>
          <el-col :span="24"><el-form-item label="生成内容" prop="generatedContent"><el-input v-model="form.generatedContent" type="textarea" :rows="5" placeholder="生成的 CSV/JSON/SQL 内容(可空,大数据存外部)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="AI 生成" prop="aiGenerated"><el-select v-model="form.aiGenerated"><el-option label="是 (Y)" value="Y" /><el-option label="否 (N)" value="N" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态" prop="status"><el-select v-model="form.status" :disabled="!form.testdataId"><el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="作者ID" prop="authorUserId"><el-input v-model="form.authorUserId" placeholder="user_id" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="TestData" lang="ts">
import { listTestData, getTestData, addTestData, updateTestData, delTestData } from '../api'
import type { TestDataForm, TestDataQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_testdata_status: status_options,
  biz_testdata_format: format_options,
  biz_testdata_table: table_options
} = toRefs<any>(proxy.useDict('biz_testdata_status', 'biz_testdata_format', 'biz_testdata_table'))

const list = ref<TestDataForm[]>([])
const loading = ref(true); const showSearch = ref(true); const ids = ref<(number | string)[]>([])
const single = ref(true); const multiple = ref(true); const total = ref(0)
const dialog = reactive({ title: '', visible: false }); const form = ref<TestDataForm>({})

const queryParams = ref<TestDataQuery>({
  pageNum: 1, pageSize: 10,
  testdataNo: undefined, projectId: undefined, title: undefined,
  targetTable: undefined, outputFormat: undefined,
  aiGenerated: undefined, status: undefined, authorUserId: undefined
})
const rules = {
  title: [{ required: true, message: '任务标题不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }],
  targetTable: [{ required: true, message: '目标表不能为空', trigger: 'change' }],
  generateCount: [{ required: true, message: '生成数量不能为空', trigger: 'blur' }]
}

function getList() { loading.value = true; listTestData(queryParams.value).then((res: any) => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function reset() {
  form.value = {
    testdataNo: undefined, projectId: undefined, title: undefined,
    targetTable: undefined, generateCount: 100, outputFormat: 'csv',
    fieldSemantics: undefined,
    ruleChinaCoord: 'Y', ruleTimeContinuity: 'Y', ruleSensorRange: 'Y', ruleIncludeOutliers: 'N',
    generatedContent: undefined,
    aiGenerated: 'N', status: '00', authorUserId: undefined
  }
  proxy.resetForm('formRef')
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(s: TestDataForm[]) { ids.value = s.map(x => x.testdataId!); single.value = s.length !== 1; multiple.value = !s.length }
function handleAdd() { reset(); dialog.title = '新增测试数据任务'; dialog.visible = true }
function handleUpdate(row?: TestDataForm) {
  reset()
  const id = row?.testdataId ?? ids.value[0]
  getTestData(id as number).then((res: any) => { form.value = res.data; dialog.title = '修改测试数据任务'; dialog.visible = true })
}
function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.testdataId ? updateTestData : addTestData
    fn(form.value).then(() => { proxy.$modal.msgSuccess(form.value.testdataId ? '修改成功' : '新增成功'); dialog.visible = false; getList() })
  })
}
function cancel() { dialog.visible = false; reset() }
function handleDelete(row?: TestDataForm) {
  const toDelete = row?.testdataId ? [row.testdataId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delTestData(toDelete as number[]))
    .then(() => { getList(); proxy.$modal.msgSuccess('删除成功') })
    .catch(() => {})
}
function handleExport() { proxy.download('business/testdata/export', { ...queryParams.value }, '测试数据_' + new Date().getTime() + '.xlsx') }

getList()
</script>
