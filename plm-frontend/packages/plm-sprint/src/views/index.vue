<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="迭代编号" prop="sprintNo">
        <el-input v-model="queryParams.sprintNo" placeholder="请输入迭代编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目ID" prop="projectId">
        <el-input v-model="queryParams.projectId" placeholder="请输入项目ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="迭代名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="名称模糊" clearable @keyup.enter="handleQuery" />
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

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:sprint:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:sprint:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:sprint:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:sprint:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="DataLine" :disabled="single" @click="handleStats" v-hasPermi="['business:sprint:stats']">健康度</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="sprintId" width="80" />
      <el-table-column label="迭代编号" align="center" prop="sprintNo" width="160" />
      <el-table-column label="项目ID" align="center" prop="projectId" width="80" />
      <el-table-column label="名称" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="目标" align="center" prop="goal" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="status_options" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="计划起止" align="center" width="220">
        <template #default="scope">
          <span>{{ parseTime(scope.row.plannedStartDate, '{y}-{m}-{d}') }} ~ {{ parseTime(scope.row.plannedEndDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="实际起止" align="center" width="220">
        <template #default="scope">
          <span v-if="scope.row.actualStartDate">{{ parseTime(scope.row.actualStartDate, '{y}-{m}-{d}') }} ~ {{ scope.row.actualEndDate ? parseTime(scope.row.actualEndDate, '{y}-{m}-{d}') : '进行中' }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="周期" align="center" prop="durationDays" width="80" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:sprint:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:sprint:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="迭代编号" prop="sprintNo">
              <el-input v-model="form.sprintNo" placeholder="留空自动生成 SPR-YYYY-NNNN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input v-model="form.projectId" placeholder="请输入项目ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="迭代名称" prop="name">
              <el-input v-model="form.name" placeholder="如 Sprint 26W21" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="迭代目标" prop="goal">
              <el-input v-model="form.goal" placeholder="一句话目标" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划开始" prop="plannedStartDate">
              <el-date-picker v-model="form.plannedStartDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束" prop="plannedEndDate">
              <el-date-picker v-model="form.plannedEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="周期(天)" prop="durationDays">
              <el-input-number v-model="form.durationDays" :min="1" :max="60" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" :disabled="!form.sprintId" placeholder="请选择">
                <el-option v-for="dict in status_options" :key="dict.value" :label="dict.label" :value="dict.value" />
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

    <!-- 健康度统计对话框 -->
    <el-dialog title="迭代健康度" v-model="statsDialog.visible" width="540px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="迭代 ID">{{ statsData.sprintId }}</el-descriptions-item>
        <el-descriptions-item label="完成率">{{ (statsData.completeRate * 100).toFixed(1) }}%</el-descriptions-item>
        <el-descriptions-item label="计划任务">{{ statsData.plannedTaskCount }}</el-descriptions-item>
        <el-descriptions-item label="已完成">{{ statsData.completedTaskCount }}</el-descriptions-item>
        <el-descriptions-item label="进行中">{{ statsData.inProgressTaskCount }}</el-descriptions-item>
        <el-descriptions-item label="剩余">{{ statsData.remainingTaskCount }}</el-descriptions-item>
        <el-descriptions-item label="是否准时">
          <el-tag :type="statsData.onTime ? 'success' : 'danger'">{{ statsData.onTime ? '是' : '否' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="偏差天数">{{ statsData.daysOverPlan }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="Sprint" lang="ts">
import { listSprint, getSprint, addSprint, updateSprint, delSprint, sprintStats } from '@/api/business/sprint'
import type { SprintForm, SprintQuery, SprintStats } from '@/types/api/business/sprint'

const { proxy } = getCurrentInstance() as any
const { biz_sprint_status: status_options } = toRefs<any>(proxy.useDict('biz_sprint_status'))

const list = ref<SprintForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const dialog = reactive({ title: '', visible: false })
const form = ref<SprintForm>({})

const statsDialog = reactive({ visible: false })
const statsData = ref<SprintStats>({} as SprintStats)

const queryParams = ref<SprintQuery>({
  pageNum: 1,
  pageSize: 10,
  sprintNo: undefined,
  projectId: undefined,
  name: undefined,
  status: undefined
})

const rules = {
  name: [{ required: true, message: '迭代名称不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }],
  plannedStartDate: [{ required: true, message: '计划开始日期不能为空', trigger: 'change' }]
}

function getList() {
  loading.value = true
  listSprint(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    sprintNo: undefined,
    projectId: undefined,
    name: undefined,
    goal: undefined,
    status: '00',
    plannedStartDate: undefined,
    plannedEndDate: undefined,
    durationDays: 14
  }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection: SprintForm[]) {
  ids.value = selection.map(item => item.sprintId!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); dialog.title = '新增迭代'; dialog.visible = true }

function handleUpdate(row?: SprintForm) {
  reset()
  const id = row?.sprintId ?? ids.value[0]
  getSprint(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '修改迭代'
    dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.sprintId ? updateSprint : addSprint
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.sprintId ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: SprintForm) {
  const toDelete = row?.sprintId ? [row.sprintId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delSprint(toDelete as number[]))
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => {})
}

function handleStats() {
  const id = ids.value[0]
  if (!id) return
  sprintStats(id as number).then((res: any) => {
    statsData.value = res.data
    statsDialog.visible = true
  })
}

function handleExport() {
  proxy.download('business/sprint/export', { ...queryParams.value }, '迭代_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
