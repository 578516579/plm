<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="流水线名称" prop="pipelineName">
        <el-input v-model="queryParams.pipelineName" placeholder="请输入名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="仓库" prop="repository">
        <el-select v-model="queryParams.repository" placeholder="全部" clearable style="width:110px">
          <el-option v-for="d in biz_pipeline_repo" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="触发方式" prop="triggerType">
        <el-select v-model="queryParams.triggerType" placeholder="全部" clearable style="width:110px">
          <el-option v-for="d in biz_pipeline_trigger" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="执行状态" prop="lastRunStatus">
        <el-select v-model="queryParams.lastRunStatus" placeholder="全部" clearable style="width:100px">
          <el-option v-for="d in biz_pipeline_run_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:pipeline:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:pipeline:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:pipeline:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="pipelineNo" width="160" />
      <el-table-column label="流水线名称" align="center" prop="pipelineName" min-width="150" />
      <el-table-column label="仓库" align="center" prop="repository" width="90">
        <template #default="{ row }">
          <dict-tag :options="biz_pipeline_repo" :value="row.repository" />
        </template>
      </el-table-column>
      <el-table-column label="分支" align="center" prop="branch" width="100" />
      <el-table-column label="触发方式" align="center" prop="triggerType" width="100">
        <template #default="{ row }">
          <dict-tag :options="biz_pipeline_trigger" :value="row.triggerType" />
        </template>
      </el-table-column>
      <el-table-column label="最近执行" align="center" prop="lastRunStatus" width="90">
        <template #default="{ row }">
          <dict-tag v-if="row.lastRunStatus" :options="biz_pipeline_run_status" :value="row.lastRunStatus" />
          <span v-else style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="成功率" align="center" prop="successRate" width="80">
        <template #default="{ row }">
          <span v-if="row.successRate != null">{{ row.successRate }}%</span>
          <span v-else style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="{ row }">
          <dict-tag :options="biz_pipeline_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:pipeline:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:pipeline:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="流水线名称" prop="pipelineName">
              <el-input v-model="form.pipelineName" placeholder="请输入流水线名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库" prop="repository">
              <el-select v-model="form.repository" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_pipeline_repo" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="触发方式" prop="triggerType">
              <el-select v-model="form.triggerType" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_pipeline_trigger" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分支" prop="branch">
              <el-input v-model="form.branch" placeholder="请输入分支名" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="阶段列表" prop="stages">
              <el-input v-model="form.stages" placeholder='JSON，如 ["build","test","scan","deploy"]' />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import { download } from '@/utils/request'
import type { PipelineForm, PipelineQuery } from '../types'
import { listPipeline, getPipeline, addPipeline, updatePipeline, delPipeline } from '../api'

defineOptions({ name: 'Pipeline' })

const { biz_pipeline_repo, biz_pipeline_trigger, biz_pipeline_run_status, biz_pipeline_status } =
  useDict('biz_pipeline_repo', 'biz_pipeline_trigger', 'biz_pipeline_run_status', 'biz_pipeline_status')

const loading = ref(false)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryRef = ref()
const formRef = ref()

const queryParams = reactive<PipelineQuery>({ pageNum: 1, pageSize: 10 })
const defaultForm = (): PipelineForm => ({ pipelineName: '', repository: 'backend', triggerType: 'push', branch: 'main' })
const form = reactive<PipelineForm>(defaultForm())
const rules = { pipelineName: [{ required: true, message: '流水线名称不能为空', trigger: 'blur' }] }

function getList() {
  loading.value = true
  listPipeline(queryParams).then((res: any) => {
    dataList.value = res.rows || []
    total.value = res.total
  }).finally(() => { loading.value = false })
}
getList()

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleSelectionChange(rows: any[]) { multiple.value = rows.length === 0 }

function handleAdd() {
  Object.assign(form, defaultForm())
  dialogTitle.value = '新增流水线'
  dialogVisible.value = true
}

function handleEdit(row: any) {
  getPipeline(row.pipelineId).then((res: any) => {
    Object.assign(form, res.data)
    dialogTitle.value = '编辑流水线'
    dialogVisible.value = true
  })
}

function handleDelete(row?: any) {
  const ids = row ? [row.pipelineId] : dataList.value.filter((r: any) => r._checked).map((r: any) => r.pipelineId)
  ElMessageBox.confirm('确认删除选中记录？', '警告', { type: 'warning' }).then(() => {
    delPipeline(ids).then(() => { ElMessage.success('删除成功'); getList() })
  })
}

function handleExport() {
  download('/business/pipeline/export', { ...queryParams }, `pipeline_${Date.now()}.xlsx`)
}

function handleSubmit() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.pipelineId ? updatePipeline : addPipeline
    api(form).then(() => {
      ElMessage.success(form.pipelineId ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getList()
    })
  })
}
</script>
