<template>
  <div class="app-container">
    <!-- 搜索条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="发布编号" prop="releaseNo">
        <el-input v-model="queryParams.releaseNo" placeholder="REL-..." clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="版本号" prop="version">
        <el-input v-model="queryParams.version" placeholder="v1.0.0" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目ID" prop="projectId">
        <el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="策略" prop="strategy">
        <el-select v-model="queryParams.strategy" placeholder="全部" clearable>
          <el-option v-for="d in strategy_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="环境" prop="environment">
        <el-input v-model="queryParams.environment" placeholder="dev/sit/uat/prod" clearable @keyup.enter="handleQuery" />
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

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:release:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:release:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:release:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:release:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="releaseId" width="80" />
      <el-table-column label="编号" align="center" prop="releaseNo" width="160" />
      <el-table-column label="版本" align="center" prop="version" width="120" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="策略" align="center" prop="strategy" width="100">
        <template #default="scope">
          <dict-tag :options="strategy_options" :value="scope.row.strategy" />
        </template>
      </el-table-column>
      <el-table-column label="环境" align="center" prop="environment" width="90" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="status_options" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="AI 评分" align="center" prop="aiReviewScore" width="90">
        <template #default="scope">
          <span v-if="scope.row.aiReviewScore != null">{{ scope.row.aiReviewScore }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="计划时间" align="center" prop="plannedAt" width="160">
        <template #default="scope"><span>{{ parseTime(scope.row.plannedAt) }}</span></template>
      </el-table-column>
      <el-table-column label="发布时间" align="center" prop="releasedAt" width="160">
        <template #default="scope"><span>{{ parseTime(scope.row.releasedAt) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:release:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:release:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="860px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="发布编号" prop="releaseNo">
              <el-input v-model="form.releaseNo" placeholder="留空自动生成 REL-YYYY-NNNN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本号" prop="version">
              <el-input v-model="form.version" placeholder="v1.0.0" />
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
            <el-form-item label="发布策略" prop="strategy">
              <el-select v-model="form.strategy" placeholder="请选择">
                <el-option v-for="d in strategy_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="环境" prop="environment">
              <el-input v-model="form.environment" placeholder="dev/sit/uat/prod" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划时间" prop="plannedAt">
              <el-date-picker v-model="form.plannedAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" :disabled="!form.releaseId" placeholder="请选择">
                <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="发布说明" prop="releaseNotes">
              <el-input v-model="form.releaseNotes" type="textarea" :rows="3" placeholder="本版本变更内容" />
            </el-form-item>
          </el-col>

          <!-- DORA 4 指标 -->
          <el-col :span="24">
            <el-divider content-position="left">DORA 4 指标</el-divider>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部署频率" prop="deploymentFrequency">
              <el-input-number v-model="form.deploymentFrequency" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="前置时间(h)" prop="leadTimeHours">
              <el-input-number v-model="form.leadTimeHours" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="MTTR(min)" prop="mttrMinutes">
              <el-input-number v-model="form.mttrMinutes" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="变更失败率(%)" prop="changeFailureRate">
              <el-input-number v-model="form.changeFailureRate" :min="0" :max="100" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>

          <!-- AI 评审 + 回滚 -->
          <el-col :span="24">
            <el-divider content-position="left">AI 评审 / 回滚</el-divider>
          </el-col>
          <el-col :span="12">
            <el-form-item label="AI 评分" prop="aiReviewScore">
              <el-input-number v-model="form.aiReviewScore" :min="0" :max="100" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="AI 评审说明" prop="aiReviewNotes">
              <el-input v-model="form.aiReviewNotes" type="textarea" :rows="2" placeholder="AI 自动生成的评审摘要" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="回滚原因" prop="rollbackReason">
              <el-input v-model="form.rollbackReason" type="textarea" :rows="2" placeholder="状态进入「已回滚(03)」时必填" />
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

<script setup name="Release" lang="ts">
import { listRelease, getRelease, addRelease, updateRelease, delRelease } from '../api'
import type { ReleaseForm, ReleaseQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_release_status: status_options,
  biz_release_strategy: strategy_options
} = toRefs<any>(proxy.useDict('biz_release_status', 'biz_release_strategy'))

const list = ref<ReleaseForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const dialog = reactive({ title: '', visible: false })
const form = ref<ReleaseForm>({})

const queryParams = ref<ReleaseQuery>({
  pageNum: 1, pageSize: 10,
  releaseNo: undefined, version: undefined, projectId: undefined,
  strategy: undefined, environment: undefined, status: undefined
})

const rules = {
  version: [{ required: true, message: '版本号不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }],
  strategy: [{ required: true, message: '发布策略不能为空', trigger: 'change' }]
}

function getList() {
  loading.value = true
  listRelease(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    releaseNo: undefined, version: undefined, projectId: undefined, sprintId: undefined,
    strategy: 'rolling', environment: undefined, releaseNotes: undefined,
    plannedAt: undefined, status: '00',
    aiReviewScore: undefined, aiReviewNotes: undefined,
    deploymentFrequency: undefined, leadTimeHours: undefined,
    mttrMinutes: undefined, changeFailureRate: undefined,
    rollbackReason: undefined
  }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection: ReleaseForm[]) {
  ids.value = selection.map(item => item.releaseId!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); dialog.title = '新增发布'; dialog.visible = true }

function handleUpdate(row?: ReleaseForm) {
  reset()
  const id = row?.releaseId ?? ids.value[0]
  getRelease(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '修改发布'
    dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.releaseId ? updateRelease : addRelease
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.releaseId ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: ReleaseForm) {
  const toDelete = row?.releaseId ? [row.releaseId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delRelease(toDelete as number[]))
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => {})
}

function handleExport() {
  proxy.download('business/release/export', { ...queryParams.value }, '发布_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
