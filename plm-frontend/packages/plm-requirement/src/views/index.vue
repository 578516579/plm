<template>
  <div class="app-container">
    <!-- 搜索条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="需求编号" prop="requirementNo">
        <el-input v-model="queryParams.requirementNo" placeholder="请输入需求编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目ID" prop="projectId">
        <el-input v-model="queryParams.projectId" placeholder="请输入项目ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="标题模糊匹配" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="来源" prop="source">
        <el-select v-model="queryParams.source" placeholder="全部" clearable>
          <el-option v-for="dict in source_options" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-select v-model="queryParams.priority" placeholder="全部" clearable>
          <el-option v-for="dict in priority_options" :key="dict.value" :label="dict.label" :value="dict.value" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:requirement:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:requirement:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:requirement:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:requirement:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="requirementId" width="80" />
      <el-table-column label="需求编号" align="center" prop="requirementNo" width="160" />
      <el-table-column label="项目ID" align="center" prop="projectId" width="80" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="来源" align="center" prop="source" width="100">
        <template #default="scope">
          <dict-tag :options="source_options" :value="scope.row.source" />
        </template>
      </el-table-column>
      <el-table-column label="优先级" align="center" prop="priority" width="100">
        <template #default="scope">
          <dict-tag :options="priority_options" :value="scope.row.priority" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="status_options" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="指派" align="center" prop="assigneeUserId" width="80" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:requirement:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:requirement:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="需求编号" prop="requirementNo">
              <el-input v-model="form.requirementNo" placeholder="留空自动生成 REQ-YYYY-NNNN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input v-model="form.projectId" placeholder="请输入项目ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="需求标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入需求标题" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源" prop="source">
              <el-select v-model="form.source" placeholder="请选择">
                <el-option v-for="dict in source_options" :key="dict.value" :label="dict.label" :value="dict.value" />
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
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择" :disabled="!form.requirementId">
                <el-option v-for="dict in status_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="指派用户ID" prop="assigneeUserId">
              <el-input v-model="form.assigneeUserId" placeholder="可空" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="评审纪要" prop="reviewNote">
              <el-input v-model="form.reviewNote" type="textarea" :rows="2" placeholder="状态推进时简要纪要" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="4" placeholder="Markdown 兼容" />
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

<script setup name="Requirement" lang="ts">
import { listRequirement, getRequirement, addRequirement, updateRequirement, delRequirement } from '../api'
import type { RequirementForm, RequirementQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_req_source: source_options,
  biz_req_priority: priority_options,
  biz_req_status: status_options
} = toRefs<any>(proxy.useDict('biz_req_source', 'biz_req_priority', 'biz_req_status'))

const list = ref<RequirementForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const dialog = reactive({ title: '', visible: false })
const form = ref<RequirementForm>({})

const queryParams = ref<RequirementQuery>({
  pageNum: 1,
  pageSize: 10,
  requirementNo: undefined,
  projectId: undefined,
  title: undefined,
  source: undefined,
  priority: undefined,
  status: undefined,
  assigneeUserId: undefined
})

const rules = {
  title: [{ required: true, message: '需求标题不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listRequirement(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    requirementNo: undefined,
    projectId: undefined,
    title: undefined,
    description: undefined,
    source: '01',
    priority: '02',
    status: '00',
    assigneeUserId: undefined,
    reviewNote: undefined
  }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection: RequirementForm[]) {
  ids.value = selection.map(item => item.requirementId!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); dialog.title = '新增需求'; dialog.visible = true }

function handleUpdate(row?: RequirementForm) {
  reset()
  const id = row?.requirementId ?? ids.value[0]
  getRequirement(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '修改需求'
    dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.requirementId ? updateRequirement : addRequirement
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.requirementId ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: RequirementForm) {
  const toDelete = row?.requirementId ? [row.requirementId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delRequirement(toDelete as number[]))
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => {})
}

function handleExport() {
  proxy.download('business/requirement/export', { ...queryParams.value }, '需求_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
