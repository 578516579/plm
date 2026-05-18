<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="报告标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="周期类型" prop="period">
        <el-select v-model="queryParams.period" placeholder="全部" clearable style="width:110px">
          <el-option v-for="d in biz_analytics_period" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="周期值" prop="periodValue">
        <el-input v-model="queryParams.periodValue" placeholder="如 2026-05" clearable style="width:120px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
          <el-option v-for="d in biz_analytics_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:analytics:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:analytics:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:analytics:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="analyticsNo" width="160" />
      <el-table-column label="报告标题" align="center" prop="title" min-width="150" />
      <el-table-column label="周期" align="center" prop="period" width="90">
        <template #default="{ row }">
          <dict-tag :options="biz_analytics_period" :value="row.period" />
        </template>
      </el-table-column>
      <el-table-column label="周期值" align="center" prop="periodValue" width="100" />
      <el-table-column label="健康分" align="center" prop="projectHealthScore" width="80">
        <template #default="{ row }">
          <span v-if="row.projectHealthScore != null">{{ row.projectHealthScore }}</span>
          <span v-else style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="AI节省(h)" align="center" prop="aiTimeSaved" width="95">
        <template #default="{ row }">
          <span v-if="row.aiTimeSaved != null">{{ row.aiTimeSaved }}</span>
          <span v-else style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="AI分析" align="center" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.aiGenerated === 'Y'" type="warning" size="small">已生成</el-tag>
          <span v-else style="color:#c0c4cc;font-size:12px">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="{ row }">
          <dict-tag :options="biz_analytics_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template #default="{ row }">
          <el-button link type="primary" icon="MagicStick" v-hasPermi="['business:analytics:edit']" @click="handleAiGenerate(row)" :loading="row._aiLoading" :disabled="row.status === '01'">AI分析</el-button>
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:analytics:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:analytics:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="报告标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入报告标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="周期类型" prop="period">
              <el-select v-model="form.period" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_analytics_period" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="周期值" prop="periodValue">
              <el-input v-model="form.periodValue" placeholder="如 2026-05 或 2026-Q2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求吞吐量" prop="requirementThroughput">
              <el-input-number v-model="form.requirementThroughput as number" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="迭代准时率%" prop="iterationOnTimeRate">
              <el-input-number v-model="form.iterationOnTimeRate as number" :min="0" :max="100" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="缺陷密度" prop="defectDensity">
              <el-input-number v-model="form.defectDensity as number" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="AI节省(h)" prop="aiTimeSaved">
              <el-input-number v-model="form.aiTimeSaved as number" :min="0" :precision="1" style="width:100%" />
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
import type { AnalyticsForm, AnalyticsQuery } from '../types'
import { listAnalytics, getAnalytics, addAnalytics, updateAnalytics, delAnalytics, aiGenerateAnalytics } from '../api'

defineOptions({ name: 'Analytics' })

const { biz_analytics_period, biz_analytics_status } = useDict('biz_analytics_period', 'biz_analytics_status')

const loading = ref(false)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryRef = ref()
const formRef = ref()

const queryParams = reactive<AnalyticsQuery>({ pageNum: 1, pageSize: 10 })
const defaultForm = (): AnalyticsForm => ({ title: '', period: 'monthly' })
const form = reactive<AnalyticsForm>(defaultForm())
const rules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  period: [{ required: true, message: '请选择周期类型', trigger: 'change' }]
}

function getList() {
  loading.value = true
  listAnalytics(queryParams).then((res: any) => {
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
  dialogTitle.value = '新增效能分析'
  dialogVisible.value = true
}

function handleEdit(row: any) {
  getAnalytics(row.analyticsId).then((res: any) => {
    Object.assign(form, res.data)
    dialogTitle.value = '编辑效能分析'
    dialogVisible.value = true
  })
}

async function handleAiGenerate(row: any) {
  row._aiLoading = true
  try {
    await aiGenerateAnalytics(row.analyticsId)
    ElMessage.success('AI分析生成成功')
    getList()
  } catch {
    ElMessage.error('AI分析失败')
  } finally {
    row._aiLoading = false
  }
}

function handleDelete(row?: any) {
  const ids = row ? [row.analyticsId] : dataList.value.filter((r: any) => r._checked).map((r: any) => r.analyticsId)
  ElMessageBox.confirm('确认删除选中记录？', '警告', { type: 'warning' }).then(() => {
    delAnalytics(ids).then(() => { ElMessage.success('删除成功'); getList() })
  })
}

function handleExport() {
  download('/business/analytics/export', { ...queryParams }, `analytics_${Date.now()}.xlsx`)
}

function handleSubmit() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.analyticsId ? updateAnalytics : addAnalytics
    api(form).then(() => {
      ElMessage.success(form.analyticsId ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getList()
    })
  })
}
</script>
