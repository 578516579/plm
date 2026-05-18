<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="手册标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="监控方案" prop="monitoringPlan">
        <el-select v-model="queryParams.monitoringPlan" placeholder="全部" clearable style="width:160px">
          <el-option v-for="d in biz_manual_ops_monitor" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
          <el-option v-for="d in biz_manual_ops_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:manual-ops:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:manual-ops:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:manual-ops:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="manualOpsNo" width="160" />
      <el-table-column label="标题" align="center" prop="title" min-width="160" />
      <el-table-column label="监控方案" align="center" prop="monitoringPlan" width="140">
        <template #default="{ row }">
          <dict-tag :options="biz_manual_ops_monitor" :value="row.monitoringPlan" />
        </template>
      </el-table-column>
      <el-table-column label="AI生成" align="center" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.aiGenerated === 'Y'" type="warning" size="small">已生成</el-tag>
          <span v-else style="color:#c0c4cc;font-size:12px">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="{ row }">
          <dict-tag :options="biz_manual_ops_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template #default="{ row }">
          <el-button link type="primary" icon="MagicStick" v-hasPermi="['business:manual-ops:edit']" @click="handleAiGenerate(row)" :loading="row._aiLoading">AI生成</el-button>
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:manual-ops:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:manual-ops:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="手册标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入手册标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="监控方案" prop="monitoringPlan">
              <el-select v-model="form.monitoringPlan" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_manual_ops_monitor" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="告警渠道" prop="alertChannels">
              <el-input v-model="form.alertChannels" placeholder='JSON，如 ["feishu","email"]' />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="IoT设备类型" prop="iotDeviceTypes">
              <el-input v-model="form.iotDeviceTypes" placeholder='JSON，如 ["sensor","gateway"]' />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="手册内容" prop="content">
              <el-input v-model="form.content" type="textarea" :rows="6" placeholder="手册内容（可由AI生成）" />
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
import type { ManualOpsForm, ManualOpsQuery } from '../types'
import { listManualOps, getManualOps, addManualOps, updateManualOps, delManualOps, aiGenerateManualOps } from '../api'

defineOptions({ name: 'ManualOps' })

const { biz_manual_ops_monitor, biz_manual_ops_status } = useDict('biz_manual_ops_monitor', 'biz_manual_ops_status')

const loading = ref(false)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryRef = ref()
const formRef = ref()

const queryParams = reactive<ManualOpsQuery>({ pageNum: 1, pageSize: 10 })
const defaultForm = (): ManualOpsForm => ({ title: '', monitoringPlan: 'prometheus_grafana' })
const form = reactive<ManualOpsForm>(defaultForm())
const rules = { title: [{ required: true, message: '标题不能为空', trigger: 'blur' }] }

function getList() {
  loading.value = true
  listManualOps(queryParams).then((res: any) => {
    dataList.value = (res.rows || []).map((r: any) => ({ ...r, _aiLoading: false }))
    total.value = res.total
  }).finally(() => { loading.value = false })
}
getList()

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleSelectionChange(rows: any[]) { multiple.value = rows.length === 0 }

function handleAdd() {
  Object.assign(form, defaultForm())
  dialogTitle.value = '新增运维手册'
  dialogVisible.value = true
}

function handleEdit(row: any) {
  getManualOps(row.manualOpsId).then((res: any) => {
    Object.assign(form, res.data)
    dialogTitle.value = '编辑运维手册'
    dialogVisible.value = true
  })
}

async function handleAiGenerate(row: any) {
  row._aiLoading = true
  try {
    await aiGenerateManualOps(row.manualOpsId)
    ElMessage.success('AI生成成功')
    getList()
  } catch {
    ElMessage.error('AI生成失败')
  } finally {
    row._aiLoading = false
  }
}

function handleDelete(row?: any) {
  const ids = row ? [row.manualOpsId] : dataList.value.filter((r: any) => r._checked).map((r: any) => r.manualOpsId)
  ElMessageBox.confirm('确认删除选中记录？', '警告', { type: 'warning' }).then(() => {
    delManualOps(ids).then(() => { ElMessage.success('删除成功'); getList() })
  })
}

function handleExport() {
  download('/business/manual-ops/export', { ...queryParams }, `manual_ops_${Date.now()}.xlsx`)
}

function handleSubmit() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.manualOpsId ? updateManualOps : addManualOps
    api(form).then(() => {
      ElMessage.success(form.manualOpsId ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getList()
    })
  })
}
</script>
