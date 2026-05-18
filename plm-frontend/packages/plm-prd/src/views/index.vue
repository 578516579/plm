<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="功能名称" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入功能名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="业务场景" prop="sceneTemplate">
        <el-select v-model="queryParams.sceneTemplate" placeholder="全部" clearable style="width:140px">
          <el-option v-for="d in biz_prd_scene" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标用户" prop="targetUser">
        <el-select v-model="queryParams.targetUser" placeholder="全部" clearable style="width:140px">
          <el-option v-for="d in biz_prd_target_user" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in biz_prd_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:prd:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:prd:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:prd:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="prdNo" width="160" />
      <el-table-column label="功能名称" align="center" prop="title" min-width="160" />
      <el-table-column label="业务场景" align="center" prop="sceneTemplate" width="120">
        <template #default="{ row }">
          <dict-tag :options="biz_prd_scene" :value="row.sceneTemplate" />
        </template>
      </el-table-column>
      <el-table-column label="目标用户" align="center" prop="targetUser" width="110">
        <template #default="{ row }">
          <dict-tag :options="biz_prd_target_user" :value="row.targetUser" />
        </template>
      </el-table-column>
      <el-table-column label="版本" align="center" prop="version" width="80" />
      <el-table-column label="完整度" align="center" prop="completenessScore" width="90">
        <template #default="{ row }">
          <span v-if="row.completenessScore != null">{{ row.completenessScore }}%</span>
        </template>
      </el-table-column>
      <el-table-column label="AI生成" align="center" width="85">
        <template #default="{ row }">
          <el-tag v-if="row.aiGenerated === 'Y'" type="warning" size="small">AI</el-tag>
          <span v-else style="color:#c0c4cc;font-size:12px">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="{ row }">
          <dict-tag :options="biz_prd_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:prd:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:prd:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="700px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="功能名称" prop="title">
              <el-input v-model="form.title" placeholder="请输入功能名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务场景" prop="sceneTemplate">
              <el-select v-model="form.sceneTemplate" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_prd_scene" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标用户" prop="targetUser">
              <el-select v-model="form.targetUser" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_prd_target_user" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本" prop="version">
              <el-input v-model="form.version" placeholder="如 v1.0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="完整度%" prop="completenessScore">
              <el-input-number v-model="form.completenessScore" :min="0" :max="100" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="2" placeholder="需求描述" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="内容" prop="content">
              <el-input v-model="form.content" type="textarea" :rows="6" placeholder="PRD 正文（Markdown）" />
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
    <el-dialog title="PRD 文档详情" v-model="detailVisible" width="820px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="编号">{{ detail.prdNo }}</el-descriptions-item>
        <el-descriptions-item label="功能名称">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="业务场景">
          <dict-tag :options="biz_prd_scene" :value="detail.sceneTemplate" />
        </el-descriptions-item>
        <el-descriptions-item label="目标用户">
          <dict-tag :options="biz_prd_target_user" :value="detail.targetUser" />
        </el-descriptions-item>
        <el-descriptions-item label="版本">{{ detail.version }}</el-descriptions-item>
        <el-descriptions-item label="完整度">{{ detail.completenessScore != null ? detail.completenessScore + '%' : '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :options="biz_prd_status" :value="detail.status" />
        </el-descriptions-item>
        <el-descriptions-item label="AI生成">
          <el-tag v-if="detail.aiGenerated === 'Y'" type="warning" size="small">是</el-tag>
          <span v-else>否</span>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detail.description }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detail.content" style="margin-top:16px">
        <div class="section-title">PRD 正文</div>
        <el-card shadow="never" style="margin-top:8px;max-height:320px;overflow-y:auto">
          <pre style="white-space:pre-wrap;font-size:13px;margin:0">{{ detail.content }}</pre>
        </el-card>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="warning" icon="MagicStick" :loading="aiLoading" @click="handleAiGenerate">AI 生成 PRD</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listPrd, getPrd, addPrd, updatePrd, delPrd, aiGeneratePrd } from '../api'
import type { PrdForm, PrdQuery } from '../types'

defineOptions({ name: 'Prd' })

const { proxy } = getCurrentInstance()!
const { biz_prd_status, biz_prd_scene, biz_prd_target_user } = proxy.useDict('biz_prd_status', 'biz_prd_scene', 'biz_prd_target_user')

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

const queryParams = reactive<PrdQuery>({ pageNum: 1, pageSize: 10 })
const form = ref<PrdForm>({ title: '' })
const rules = { title: [{ required: true, message: '功能名称不能为空', trigger: 'blur' }] }

function getList() {
  loading.value = true
  listPrd(queryParams).then(res => {
    dataList.value = res.rows; total.value = res.total
  }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleSelectionChange(selection: any[]) {
  ids.value = selection.map(r => r.prdId); multiple.value = !ids.value.length
}
function handleAdd() { form.value = { title: '' }; dialogTitle.value = '新增 PRD 文档'; dialogVisible.value = true }
function handleEdit(row: any) {
  getPrd(row.prdId).then(res => { form.value = res.data; dialogTitle.value = '编辑 PRD 文档'; dialogVisible.value = true })
}
function handleDetail(row: any) {
  getPrd(row.prdId).then(res => { detail.value = res.data; detailVisible.value = true })
}
function handleAiGenerate() {
  aiLoading.value = true
  aiGeneratePrd(detail.value.prdId).then(() => {
    ElMessage.success('AI 生成完成')
    getPrd(detail.value.prdId).then(r => { detail.value = r.data }); getList()
  }).catch(() => ElMessage.error('AI 生成失败')).finally(() => { aiLoading.value = false })
}
function handleDelete(row?: any) {
  const delIds = row ? [row.prdId] : ids.value
  ElMessageBox.confirm('确认删除选中 PRD 文档？', '警告', { type: 'warning' }).then(() => {
    delPrd(delIds).then(() => { ElMessage.success('删除成功'); getList() })
  })
}
function handleExport() { proxy.download('business/prd/export', { ...queryParams }, 'prd.xlsx') }
function submitForm() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.value.prdId ? updatePrd : addPrd
    api(form.value).then(() => {
      ElMessage.success(form.value.prdId ? '修改成功' : '新增成功')
      dialogVisible.value = false; getList()
    })
  })
}

getList()
</script>

<style scoped>
.section-title { font-weight: 600; font-size: 14px; color: #303133; }
</style>
