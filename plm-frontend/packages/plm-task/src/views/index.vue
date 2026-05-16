<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="任务编号" prop="taskNo">
        <el-input v-model="queryParams.taskNo" placeholder="请输入任务编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目ID" prop="projectId">
        <el-input v-model="queryParams.projectId" placeholder="请输入项目ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="需求ID" prop="requirementId">
        <el-input v-model="queryParams.requirementId" placeholder="可空" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="迭代ID" prop="sprintId">
        <el-input v-model="queryParams.sprintId" placeholder="可空" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="dict in status_options" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-select v-model="queryParams.priority" placeholder="全部" clearable>
          <el-option v-for="dict in priority_options" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:task:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:task:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:task:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:task:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="taskId" width="80" />
      <el-table-column label="任务编号" align="center" prop="taskNo" width="160" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="需求" align="center" prop="requirementId" width="80" />
      <el-table-column label="迭代" align="center" prop="sprintId" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="status_options" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="优先级" align="center" prop="priority" width="100">
        <template #default="scope">
          <dict-tag :options="priority_options" :value="scope.row.priority" />
        </template>
      </el-table-column>
      <el-table-column label="预估" align="center" prop="estimatedHours" width="70" />
      <el-table-column label="实际" align="center" prop="actualHours" width="70" />
      <el-table-column label="负责人" align="center" prop="assigneeUserId" width="80" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:task:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:task:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="780px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="任务编号" prop="taskNo">
              <el-input v-model="form.taskNo" placeholder="留空自动生成 TASK-YYYY-NNNN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input v-model="form.projectId" placeholder="请输入项目ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求ID" prop="requirementId">
              <el-input v-model="form.requirementId" placeholder="可空" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="迭代ID" prop="sprintId">
              <el-input v-model="form.sprintId" placeholder="可空" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="任务标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入任务标题" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" :disabled="!form.taskId" placeholder="请选择">
                <el-option v-for="dict in status_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" placeholder="请选择">
                <el-option v-for="dict in priority_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="负责人ID" prop="assigneeUserId">
              <el-input v-model="form.assigneeUserId" placeholder="user_id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预估工时" prop="estimatedHours">
              <el-input-number v-model="form.estimatedHours" :min="0" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际工时" prop="actualHours">
              <el-input-number v-model="form.actualHours" :min="0" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="14">
            <el-form-item label="MR 链接" prop="mrUrl">
              <el-input v-model="form.mrUrl" placeholder="https://..." />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="MR 分支" prop="mrBranch">
              <el-input v-model="form.mrBranch" placeholder="feature/xxx" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="4" />
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

<script setup name="Task" lang="ts">
import { listTask, getTask, addTask, updateTask, delTask } from '../api'
import type { TaskForm, TaskQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_task_status: status_options,
  biz_task_priority: priority_options
} = toRefs<any>(proxy.useDict('biz_task_status', 'biz_task_priority'))

const list = ref<TaskForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const dialog = reactive({ title: '', visible: false })
const form = ref<TaskForm>({})

const queryParams = ref<TaskQuery>({
  pageNum: 1,
  pageSize: 10,
  taskNo: undefined,
  projectId: undefined,
  requirementId: undefined,
  sprintId: undefined,
  title: undefined,
  status: undefined,
  priority: undefined,
  assigneeUserId: undefined
})

const rules = {
  title: [{ required: true, message: '任务标题不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listTask(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    taskNo: undefined,
    projectId: undefined,
    requirementId: undefined,
    sprintId: undefined,
    title: undefined,
    description: undefined,
    status: '00',
    priority: '02',
    assigneeUserId: undefined,
    estimatedHours: undefined,
    actualHours: undefined,
    mrUrl: undefined,
    mrBranch: undefined
  }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection: TaskForm[]) {
  ids.value = selection.map(item => item.taskId!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); dialog.title = '新增任务'; dialog.visible = true }

function handleUpdate(row?: TaskForm) {
  reset()
  const id = row?.taskId ?? ids.value[0]
  getTask(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '修改任务'
    dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.taskId ? updateTask : addTask
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.taskId ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: TaskForm) {
  const toDelete = row?.taskId ? [row.taskId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delTask(toDelete as number[]))
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => {})
}

function handleExport() {
  proxy.download('business/task/export', { ...queryParams.value }, '任务_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
