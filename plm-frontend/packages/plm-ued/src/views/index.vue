<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="设计稿名称" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入设计稿名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="版本" prop="versionLabel">
        <el-input v-model="queryParams.versionLabel" placeholder="如 v1.0" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in biz_ued_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:ued:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:ued:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:ued:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="uedNo" width="160" />
      <el-table-column label="设计稿名称" align="center" prop="title" min-width="160" />
      <el-table-column label="版本" align="center" prop="versionLabel" width="90" />
      <el-table-column label="Figma链接" align="center" width="100">
        <template #default="{ row }">
          <el-link v-if="row.figmaUrl" :href="row.figmaUrl" target="_blank" type="primary" icon="Link">Figma</el-link>
          <span v-else style="color:#c0c4cc;font-size:12px">—</span>
        </template>
      </el-table-column>
      <el-table-column label="AI评分" align="center" prop="aiReviewScore" width="90">
        <template #default="{ row }">
          <span v-if="row.aiReviewScore != null">{{ row.aiReviewScore }}</span>
          <span v-else style="color:#c0c4cc;font-size:12px">—</span>
        </template>
      </el-table-column>
      <el-table-column label="农业组件" align="center" prop="agriComponentTags" width="120" show-overflow-tooltip />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="{ row }">
          <dict-tag :options="biz_ued_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:ued:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:ued:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="设计稿名称" prop="title">
              <el-input v-model="form.title" placeholder="请输入设计稿名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本" prop="versionLabel">
              <el-input v-model="form.versionLabel" placeholder="如 v1.0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="AI评分" prop="aiReviewScore">
              <el-input-number v-model="form.aiReviewScore" :min="0" :max="100" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Figma URL" prop="figmaUrl">
              <el-input v-model="form.figmaUrl" placeholder="https://www.figma.com/..." />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Figma文件Key" prop="figmaFileKey">
              <el-input v-model="form.figmaFileKey" placeholder="Figma file key" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预览URL" prop="previewUrl">
              <el-input v-model="form.previewUrl" placeholder="预览图链接" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="农业组件标签" prop="agriComponentTags">
              <el-input v-model="form.agriComponentTags" placeholder="逗号分隔，如 农田地图,传感器图表" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="标注说明" prop="annotationContent">
              <el-input v-model="form.annotationContent" type="textarea" :rows="3" placeholder="设计标注（Markdown）" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="可用性问题" prop="usabilityIssues">
              <el-input v-model="form.usabilityIssues" type="textarea" :rows="3" placeholder="可用性问题列表" />
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
    <el-dialog title="UED 设计详情" v-model="detailVisible" width="820px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="编号">{{ detail.uedNo }}</el-descriptions-item>
        <el-descriptions-item label="设计稿名称">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ detail.versionLabel }}</el-descriptions-item>
        <el-descriptions-item label="AI评分">{{ detail.aiReviewScore != null ? detail.aiReviewScore : '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :options="biz_ued_status" :value="detail.status" />
        </el-descriptions-item>
        <el-descriptions-item label="农业组件">{{ detail.agriComponentTags }}</el-descriptions-item>
        <el-descriptions-item label="Figma链接" :span="2">
          <el-link v-if="detail.figmaUrl" :href="detail.figmaUrl" target="_blank" type="primary">{{ detail.figmaUrl }}</el-link>
        </el-descriptions-item>
        <el-descriptions-item label="可用性问题" :span="2">{{ detail.usabilityIssues }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detail.aiReviewReport" style="margin-top:16px">
        <div class="section-title">AI 走查报告</div>
        <el-card shadow="never" style="margin-top:8px;max-height:280px;overflow-y:auto">
          <pre style="white-space:pre-wrap;font-size:13px;margin:0">{{ detail.aiReviewReport }}</pre>
        </el-card>
        <div style="margin-top:8px;font-size:12px;color:#999">生成时间：{{ detail.aiGeneratedAt }}</div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="warning" icon="MagicStick" :loading="aiLoading" @click="handleAiReview">AI 走查</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUed, getUed, addUed, updateUed, delUed, aiReviewUed } from '../api'
import type { UedForm, UedQuery } from '../types'

defineOptions({ name: 'Ued' })

const { proxy } = getCurrentInstance()!
const { biz_ued_status } = proxy.useDict('biz_ued_status')

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

const queryParams = reactive<UedQuery>({ pageNum: 1, pageSize: 10 })
const form = ref<UedForm>({ title: '' })
const rules = { title: [{ required: true, message: '设计稿名称不能为空', trigger: 'blur' }] }

function getList() {
  loading.value = true
  listUed(queryParams).then(res => {
    dataList.value = res.rows; total.value = res.total
  }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleSelectionChange(selection: any[]) {
  ids.value = selection.map(r => r.uedId); multiple.value = !ids.value.length
}
function handleAdd() { form.value = { title: '' }; dialogTitle.value = '新增 UED 设计'; dialogVisible.value = true }
function handleEdit(row: any) {
  getUed(row.uedId).then(res => { form.value = res.data; dialogTitle.value = '编辑 UED 设计'; dialogVisible.value = true })
}
function handleDetail(row: any) {
  getUed(row.uedId).then(res => { detail.value = res.data; detailVisible.value = true })
}
function handleAiReview() {
  aiLoading.value = true
  aiReviewUed(detail.value.uedId).then(() => {
    ElMessage.success('AI 走查完成')
    getUed(detail.value.uedId).then(r => { detail.value = r.data }); getList()
  }).catch(() => ElMessage.error('AI 走查失败')).finally(() => { aiLoading.value = false })
}
function handleDelete(row?: any) {
  const delIds = row ? [row.uedId] : ids.value
  ElMessageBox.confirm('确认删除选中 UED 设计？', '警告', { type: 'warning' }).then(() => {
    delUed(delIds).then(() => { ElMessage.success('删除成功'); getList() })
  })
}
function handleExport() { proxy.download('business/ued/export', { ...queryParams }, 'ued.xlsx') }
function submitForm() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.value.uedId ? updateUed : addUed
    api(form.value).then(() => {
      ElMessage.success(form.value.uedId ? '修改成功' : '新增成功')
      dialogVisible.value = false; getList()
    })
  })
}

getList()
</script>

<style scoped>
.section-title { font-weight: 600; font-size: 14px; color: #303133; }
</style>
