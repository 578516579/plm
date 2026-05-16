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

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="项目ID" align="center" prop="id" width="80" />
      <el-table-column label="项目编号" align="center" prop="projectNo" />
      <el-table-column label="项目名称" align="center" prop="projectName" :show-overflow-tooltip="true" />
      <el-table-column label="类型" align="center" prop="projectType">
        <template #default="scope">
          <dict-tag :options="project_type_options" :value="scope.row.projectType" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="status_options" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="起止日期" align="center" width="220">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }} ~ {{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="预算" align="right" prop="budget" width="120" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:project:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:project:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="640px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="项目编号" prop="projectNo">
              <el-input v-model="form.projectNo" placeholder="请输入项目编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目名称" prop="projectName">
              <el-input v-model="form.projectName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="projectType">
              <el-select v-model="form.projectType" placeholder="请选择类型">
                <el-option v-for="dict in project_type_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态">
                <el-option v-for="dict in status_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
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
            <el-form-item label="预算" prop="budget">
              <el-input-number v-model="form.budget" :min="0" :precision="2" style="width: 100%" />
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
import { listProject, getProject, addProject, updateProject, delProject } from '@/api/business/project'
import type { ProjectForm, ProjectQuery } from '@/types/api/business/project'

const { proxy } = getCurrentInstance() as any
const { biz_project_type: project_type_options, biz_project_status: status_options } = toRefs<any>(proxy.useDict('biz_project_type', 'biz_project_status'))

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
  projectType: undefined,
  status: undefined
})

const rules = {
  projectNo: [{ required: true, message: '项目编号不能为空', trigger: 'blur' }],
  projectName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }]
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
  form.value = { projectNo: undefined, projectName: undefined, projectType: undefined, status: '0', budget: undefined, description: undefined }
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
