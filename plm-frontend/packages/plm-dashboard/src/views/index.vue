<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="组件名称" prop="widgetName">
        <el-input v-model="queryParams.widgetName" placeholder="请输入组件名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="组件类型" prop="widgetType">
        <el-select v-model="queryParams.widgetType" placeholder="全部" clearable style="width:110px">
          <el-option v-for="d in biz_dashboard_widget" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="可见" prop="visible">
        <el-select v-model="queryParams.visible" placeholder="全部" clearable style="width:90px">
          <el-option label="显示" value="Y" />
          <el-option label="隐藏" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
          <el-option v-for="d in biz_dashboard_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:dashboard:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:dashboard:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:dashboard:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="dashboardNo" width="160" />
      <el-table-column label="组件名称" align="center" prop="widgetName" min-width="150" />
      <el-table-column label="类型" align="center" prop="widgetType" width="90">
        <template #default="{ row }">
          <dict-tag :options="biz_dashboard_widget" :value="row.widgetType" />
        </template>
      </el-table-column>
      <el-table-column label="数据源" align="center" prop="dataSource" width="160" />
      <el-table-column label="排序" align="center" prop="sortOrder" width="70" />
      <el-table-column label="可见" align="center" width="70">
        <template #default="{ row }">
          <el-tag :type="row.visible === 'Y' ? 'success' : 'info'" size="small">{{ row.visible === 'Y' ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="{ row }">
          <dict-tag :options="biz_dashboard_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:dashboard:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:dashboard:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="组件名称" prop="widgetName">
              <el-input v-model="form.widgetName" placeholder="请输入组件名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组件类型" prop="widgetType">
              <el-select v-model="form.widgetType" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_dashboard_widget" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据源" prop="dataSource">
              <el-input v-model="form.dataSource" placeholder="如 project.health" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否显示" prop="visible">
              <el-radio-group v-model="form.visible">
                <el-radio value="Y">显示</el-radio>
                <el-radio value="N">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="JSON配置" prop="config">
              <el-input v-model="form.config" type="textarea" :rows="4" placeholder="组件 JSON 配置（可选）" />
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
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import { download } from '@/utils/request'
import type { DashboardForm, DashboardQuery } from '../types'
import { listDashboard, getDashboard, addDashboard, updateDashboard, delDashboard } from '../api'

defineOptions({ name: 'Dashboard' })

const { biz_dashboard_widget, biz_dashboard_status } = useDict('biz_dashboard_widget', 'biz_dashboard_status')

const loading = ref(false)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryRef = ref()
const formRef = ref()

const queryParams = reactive<DashboardQuery>({ pageNum: 1, pageSize: 10 })
const defaultForm = (): DashboardForm => ({ widgetName: '', widgetType: 'card', visible: 'Y', sortOrder: 0 })
const form = reactive<DashboardForm>(defaultForm())
const rules = { widgetName: [{ required: true, message: '组件名称不能为空', trigger: 'blur' }] }

function getList() {
  loading.value = true
  listDashboard(queryParams).then(res => {
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
  dialogTitle.value = '新增看板组件'
  dialogVisible.value = true
}

function handleEdit(row: any) {
  getDashboard(row.dashboardId).then(res => {
    Object.assign(form, res.data)
    dialogTitle.value = '编辑看板组件'
    dialogVisible.value = true
  })
}

function handleDelete(row?: any) {
  const ids = row ? [row.dashboardId] : dataList.value.filter((r: any) => r._checked).map((r: any) => r.dashboardId)
  ElMessageBox.confirm('确认删除选中记录？', '警告', { type: 'warning' }).then(() => {
    delDashboard(ids).then(() => { ElMessage.success('删除成功'); getList() })
  })
}

function handleExport() {
  download('/business/dashboard/export', { ...queryParams }, `dashboard_${Date.now()}.xlsx`)
}

function handleSubmit() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.dashboardId ? updateDashboard : addDashboard
    api(form).then(() => {
      ElMessage.success(form.dashboardId ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getList()
    })
  })
}
</script>
