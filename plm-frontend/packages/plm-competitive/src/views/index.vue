<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="竞品名称" prop="competitorName">
        <el-input v-model="queryParams.competitorName" placeholder="请输入竞品名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="厂商" prop="vendor">
        <el-input v-model="queryParams.vendor" placeholder="请输入厂商" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="价格档" prop="pricingTier">
        <el-select v-model="queryParams.pricingTier" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in biz_competitive_tier" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in biz_competitive_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:competitive:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:competitive:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:competitive:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="competitiveNo" width="160" />
      <el-table-column label="竞品名称" align="center" prop="competitorName" min-width="140" />
      <el-table-column label="厂商" align="center" prop="vendor" width="120" />
      <el-table-column label="价格档" align="center" prop="pricingTier" width="100">
        <template #default="{ row }">
          <dict-tag :options="biz_competitive_tier" :value="row.pricingTier" />
        </template>
      </el-table-column>
      <el-table-column label="监控" align="center" width="80">
        <template #default="{ row }">
          <el-tag :type="row.monitorEnabled === 'Y' ? 'success' : 'info'" size="small">{{ row.monitorEnabled === 'Y' ? '开启' : '关闭' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="AI分析" align="center" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.aiGenerated === 'Y'" type="warning" size="small">已生成</el-tag>
          <span v-else style="color:#c0c4cc;font-size:12px">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="{ row }">
          <dict-tag :options="biz_competitive_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:competitive:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:competitive:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="700px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="竞品名称" prop="competitorName">
              <el-input v-model="form.competitorName" placeholder="请输入竞品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="厂商" prop="vendor">
              <el-input v-model="form.vendor" placeholder="请输入厂商" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="官网" prop="website">
              <el-input v-model="form.website" placeholder="https://..." />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格档" prop="pricingTier">
              <el-select v-model="form.pricingTier" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_competitive_tier" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="定价说明" prop="pricingModel">
              <el-input v-model="form.pricingModel" type="textarea" :rows="2" placeholder="价格模式说明" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优势" prop="strengths">
              <el-input v-model="form.strengths" type="textarea" :rows="3" placeholder="Strengths" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="劣势" prop="weaknesses">
              <el-input v-model="form.weaknesses" type="textarea" :rows="3" placeholder="Weaknesses" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="机会" prop="opportunities">
              <el-input v-model="form.opportunities" type="textarea" :rows="3" placeholder="Opportunities" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="威胁" prop="threats">
              <el-input v-model="form.threats" type="textarea" :rows="3" placeholder="Threats" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="监控关键词" prop="monitorKeywords">
              <el-input v-model="form.monitorKeywords" placeholder="关键词，逗号分隔" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开启监控" prop="monitorEnabled">
              <el-switch v-model="form.monitorEnabled" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="竞品情报详情" v-model="detailVisible" width="800px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="编号">{{ detail.competitiveNo }}</el-descriptions-item>
        <el-descriptions-item label="竞品名称">{{ detail.competitorName }}</el-descriptions-item>
        <el-descriptions-item label="厂商">{{ detail.vendor }}</el-descriptions-item>
        <el-descriptions-item label="官网">
          <a v-if="detail.website" :href="detail.website" target="_blank" rel="noopener">{{ detail.website }}</a>
        </el-descriptions-item>
        <el-descriptions-item label="价格档">
          <dict-tag :options="biz_competitive_tier" :value="detail.pricingTier" />
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :options="biz_competitive_status" :value="detail.status" />
        </el-descriptions-item>
        <el-descriptions-item label="优势" :span="2">{{ detail.strengths }}</el-descriptions-item>
        <el-descriptions-item label="劣势" :span="2">{{ detail.weaknesses }}</el-descriptions-item>
        <el-descriptions-item label="机会" :span="2">{{ detail.opportunities }}</el-descriptions-item>
        <el-descriptions-item label="威胁" :span="2">{{ detail.threats }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detail.aiAnalysisReport" style="margin-top:16px">
        <div class="section-title">AI 分析报告</div>
        <el-card shadow="never" style="margin-top:8px">
          <pre style="white-space:pre-wrap;font-size:13px;margin:0">{{ detail.aiAnalysisReport }}</pre>
        </el-card>
        <div style="margin-top:8px;font-size:12px;color:#999">生成时间：{{ detail.aiGeneratedAt }}</div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="warning" icon="MagicStick" :loading="aiLoading" @click="handleAiAnalyze">AI 分析</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCompetitive, getCompetitive, addCompetitive, updateCompetitive, delCompetitive, aiAnalyzeCompetitive } from '../api'
import type { CompetitiveForm, CompetitiveQuery } from '../types'

defineOptions({ name: 'Competitive' })

const { proxy } = getCurrentInstance()!
const { biz_competitive_status, biz_competitive_tier } = proxy.useDict('biz_competitive_status', 'biz_competitive_tier')

const loading = ref(false)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref<any[]>([])
const ids = ref<(number | string)[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const detailVisible = ref(false)
const aiLoading = ref(false)
const detail = ref<any>({})
const queryRef = ref()
const formRef = ref()

const queryParams = reactive<CompetitiveQuery>({ pageNum: 1, pageSize: 10 })
const form = ref<CompetitiveForm>({ competitorName: '' })
const rules = { competitorName: [{ required: true, message: '竞品名称不能为空', trigger: 'blur' }] }

function getList() {
  loading.value = true
  listCompetitive(queryParams).then((res: any) => {
    dataList.value = res.rows
    total.value = res.total
  }).finally(() => { loading.value = false })
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleSelectionChange(selection: any[]) {
  ids.value = selection.map(r => r.competitiveId)
  multiple.value = !ids.value.length
}
function handleAdd() {
  form.value = { competitorName: '', monitorEnabled: 'N' }
  dialogTitle.value = '新增竞品情报'; dialogVisible.value = true
}
function handleEdit(row: any) {
  getCompetitive(row.competitiveId).then((res: any) => {
    form.value = res.data; dialogTitle.value = '编辑竞品情报'; dialogVisible.value = true
  })
}
function handleDetail(row: any) {
  getCompetitive(row.competitiveId).then((res: any) => { detail.value = res.data; detailVisible.value = true })
}
function handleAiAnalyze() {
  aiLoading.value = true
  aiAnalyzeCompetitive(detail.value.competitiveId).then(() => {
    ElMessage.success('AI 分析完成')
    getCompetitive(detail.value.competitiveId).then((r: any) => { detail.value = r.data })
    getList()
  }).catch(() => ElMessage.error('AI 分析失败')).finally(() => { aiLoading.value = false })
}
function handleDelete(row?: any) {
  const delIds = row ? [row.competitiveId] : ids.value
  ElMessageBox.confirm('确认删除选中竞品情报？', '警告', { type: 'warning' }).then(() => {
    delCompetitive(delIds).then(() => { ElMessage.success('删除成功'); getList() })
  })
}
function handleExport() { proxy.download('business/competitive/export', { ...queryParams }, 'competitive.xlsx') }
function submitForm() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.value.competitiveId ? updateCompetitive : addCompetitive
    api(form.value).then(() => {
      ElMessage.success(form.value.competitiveId ? '修改成功' : '新增成功')
      dialogVisible.value = false; getList()
    })
  })
}

getList()
</script>

<style scoped>
.section-title { font-weight: 600; font-size: 14px; color: #303133; }
</style>
