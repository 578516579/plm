<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="缺陷编号" prop="defectNo">
        <el-input v-model="queryParams.defectNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目ID" prop="projectId">
        <el-input v-model="queryParams.projectId" placeholder="项目筛选" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="严重级别" prop="severity">
        <el-select v-model="queryParams.severity" placeholder="全部" clearable>
          <el-option v-for="d in severity_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="全部" clearable>
          <el-option v-for="d in category_options" :key="d.value" :label="d.label" :value="d.value" />
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
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:defect:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:defect:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:defect:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:defect:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="defectId" width="80" />
      <el-table-column label="编号" align="center" prop="defectNo" width="180" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="严重级别" align="center" prop="severity" width="120">
        <template #default="scope">
          <dict-tag :options="severity_options" :value="scope.row.severity" />
        </template>
      </el-table-column>
      <el-table-column label="分类" align="center" prop="category" width="100">
        <template #default="scope">
          <dict-tag :options="category_options" :value="scope.row.category" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="status_options" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="指派" align="center" prop="assigneeUserId" width="80" />
      <el-table-column label="报告人" align="center" prop="reporterUserId" width="80" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:defect:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:defect:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="780px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="缺陷编号" prop="defectNo">
              <el-input v-model="form.defectNo" placeholder="留空自动生成 DEFECT-YYYY-NNNN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input v-model="form.projectId" placeholder="必填" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="迭代ID" prop="sprintId">
              <el-input v-model="form.sprintId" placeholder="可空" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="任务ID" prop="taskId">
              <el-input v-model="form.taskId" placeholder="可空" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="指派开发" prop="assigneeUserId">
              <el-input v-model="form.assigneeUserId" placeholder="user_id" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入缺陷标题" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="严重级别" prop="severity">
              <el-select v-model="form.severity">
                <el-option v-for="d in severity_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category">
                <el-option v-for="d in category_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" :disabled="!form.defectId">
                <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="重现步骤" prop="reproduceSteps">
              <el-input v-model="form.reproduceSteps" type="textarea" :rows="3" placeholder="1. ...&#10;2. ..." />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期望结果" prop="expectedResult">
              <el-input v-model="form.expectedResult" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际结果" prop="actualResult">
              <el-input v-model="form.actualResult" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="解决说明" prop="resolution">
              <el-input v-model="form.resolution" type="textarea" :rows="2" placeholder="进入「已解决」状态时必填" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="标签" prop="tags">
              <el-input v-model="form.tags" placeholder="逗号分隔,如 regression,flaky" />
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

<script setup name="Defect" lang="ts">
import { listDefect, getDefect, addDefect, updateDefect, delDefect } from '@/api/business/defect'
import type { DefectForm, DefectQuery } from '@/types/api/business/defect'

const { proxy } = getCurrentInstance() as any
const {
  biz_defect_severity: severity_options,
  biz_defect_category: category_options,
  biz_defect_status: status_options
} = toRefs<any>(proxy.useDict('biz_defect_severity', 'biz_defect_category', 'biz_defect_status'))

const list = ref<DefectForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const dialog = reactive({ title: '', visible: false })
const form = ref<DefectForm>({})

const queryParams = ref<DefectQuery>({
  pageNum: 1,
  pageSize: 10,
  defectNo: undefined,
  projectId: undefined,
  severity: undefined,
  category: undefined,
  status: undefined
})

const rules = {
  title: [{ required: true, message: '缺陷标题不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listDefect(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    defectNo: undefined,
    projectId: undefined,
    sprintId: undefined,
    taskId: undefined,
    title: undefined,
    description: undefined,
    severity: '02',
    category: '01',
    status: '00',
    assigneeUserId: undefined,
    reproduceSteps: undefined,
    expectedResult: undefined,
    actualResult: undefined,
    resolution: undefined,
    tags: undefined
  }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection: DefectForm[]) {
  ids.value = selection.map(item => item.defectId!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); dialog.title = '新增缺陷'; dialog.visible = true }

function handleUpdate(row?: DefectForm) {
  reset()
  const id = row?.defectId ?? ids.value[0]
  getDefect(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '修改缺陷'
    dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.defectId ? updateDefect : addDefect
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.defectId ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: DefectForm) {
  const toDelete = row?.defectId ? [row.defectId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delDefect(toDelete as number[]))
    .then(() => { getList(); proxy.$modal.msgSuccess('删除成功') })
    .catch(() => {})
}

function handleExport() {
  proxy.download('business/defect/export', { ...queryParams.value }, '缺陷_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
