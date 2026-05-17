<template>
  <div class="app-container">
    <!-- 搜索条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="项目编号" prop="projectNo">
        <el-input v-model="queryParams.projectNo" placeholder="请输入项目编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目名称" prop="projectName">
        <el-input v-model="queryParams.projectName" placeholder="请输入项目名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="业务线" prop="businessLine">
        <el-select v-model="queryParams.businessLine" placeholder="全部" clearable>
          <el-option v-for="dict in business_line_options" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="阶段" prop="lifecyclePhase">
        <el-select v-model="queryParams.lifecyclePhase" placeholder="全部" clearable>
          <el-option v-for="dict in phase_options" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="dict in status_options" :key="dict.value" :label="dict.label" :value="dict.value" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:project:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:project:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:project:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:project:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格(列序与原型 projects.html 表头对齐:项目名称/业务线/阶段/进度/健康度/负责人/截止日期) -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="项目编号" align="center" prop="projectNo" width="140" />
      <el-table-column label="项目名称" align="center" prop="projectName" :show-overflow-tooltip="true" />
      <el-table-column label="业务线" align="center" prop="businessLine" width="110">
        <template #default="scope">
          <dict-tag :options="business_line_options" :value="scope.row.businessLine" />
        </template>
      </el-table-column>
      <el-table-column label="阶段" align="center" prop="lifecyclePhase" width="100">
        <template #default="scope">
          <dict-tag :options="phase_options" :value="scope.row.lifecyclePhase" />
        </template>
      </el-table-column>
      <el-table-column label="进度" align="center" prop="progress" width="120">
        <template #default="scope">
          <el-progress :percentage="Number(scope.row.progress) || 0" :stroke-width="10" />
        </template>
      </el-table-column>
      <el-table-column label="健康度" align="center" prop="health" width="90">
        <template #default="scope">
          <dict-tag :options="health_options" :value="scope.row.health" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :options="status_options" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="起止日期" align="center" width="200">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }} ~ {{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:project:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:project:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框(字段顺序贴 PRD/原型:名称→业务线→类型→优先级→负责人→起止日期→阶段→进度→健康度→状态→描述) -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="项目名称" prop="projectName">
              <el-input v-model="form.projectName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务线" prop="businessLine">
              <el-select v-model="form.businessLine" placeholder="请选择业务线" style="width: 100%">
                <el-option v-for="dict in business_line_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="projectType">
              <el-select v-model="form.projectType" placeholder="请选择类型" clearable style="width: 100%">
                <el-option v-for="dict in project_type_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" placeholder="请选择优先级" clearable style="width: 100%">
                <el-option v-for="dict in priority_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人ID" prop="managerUserId">
              <el-input-number v-model="form.managerUserId as any" :min="1" style="width: 100%" placeholder="sys_user.user_id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="起始日期" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="阶段" prop="lifecyclePhase">
              <el-select v-model="form.lifecyclePhase" placeholder="规划中" style="width: 100%">
                <el-option v-for="dict in phase_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="进度(%)" prop="progress">
              <el-input-number v-model="form.progress as any" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="健康度" prop="health">
              <el-select v-model="form.health" placeholder="请选择" clearable style="width: 100%">
                <el-option v-for="dict in health_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="进行中" style="width: 100%">
                <el-option v-for="dict in status_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
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

<script setup name="Project" lang="ts">
import { listProject, getProject, addProject, updateProject, delProject } from '../api'
import type { ProjectForm, ProjectQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_project_type: project_type_options,
  biz_project_status: status_options,
  biz_project_business_line: business_line_options,
  biz_project_priority: priority_options,
  biz_project_phase: phase_options,
  biz_project_health: health_options
} = toRefs<any>(proxy.useDict(
  'biz_project_type',
  'biz_project_status',
  'biz_project_business_line',
  'biz_project_priority',
  'biz_project_phase',
  'biz_project_health'
))

const list = ref<ProjectForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const dialog = reactive({ title: '', visible: false })
const form = ref<ProjectForm>({})

const queryParams = ref<ProjectQuery>({
  pageNum: 1,
  pageSize: 10,
  projectNo: undefined,
  projectName: undefined,
  businessLine: undefined,
  lifecyclePhase: undefined,
  status: undefined
})

const rules = {
  projectName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
  businessLine: [{ required: true, message: '业务线不能为空', trigger: 'change' }]
}

function getList() {
  loading.value = true
  listProject(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    projectName: undefined,
    businessLine: undefined,
    projectType: undefined,
    priority: undefined,
    lifecyclePhase: '00',
    status: '00',
    progress: 0,
    health: undefined,
    description: undefined
  }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection: ProjectForm[]) {
  ids.value = selection.map(item => item.id!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); dialog.title = '新增项目'; dialog.visible = true }

function handleUpdate(row?: ProjectForm) {
  reset()
  const id = row?.id ?? ids.value[0]
  getProject(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '修改项目'
    dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.id ? updateProject : addProject
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.id ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: ProjectForm) {
  const toDelete = row?.id ? [row.id] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？').then(() => delProject(toDelete as number[])).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('business/project/export', { ...queryParams.value }, '项目_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
