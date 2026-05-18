<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="设计标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入设计标题" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="DB引擎" prop="dbEngine">
        <el-select v-model="queryParams.dbEngine" placeholder="全部" clearable style="width:130px">
          <el-option v-for="d in biz_dbdesign_engine" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in biz_dbdesign_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:dbdesign:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:dbdesign:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:dbdesign:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="dbdesignNo" width="160" />
      <el-table-column label="设计标题" align="center" prop="title" min-width="160" />
      <el-table-column label="DB引擎" align="center" prop="dbEngine" width="110">
        <template #default="{ row }">
          <dict-tag :options="biz_dbdesign_engine" :value="row.dbEngine" />
        </template>
      </el-table-column>
      <el-table-column label="范式检查" align="center" prop="normalizationCheck" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.normalizationCheck === 'Y'" type="success" size="small">通过</el-tag>
          <el-tag v-else-if="row.normalizationCheck === 'N'" type="danger" size="small">未通过</el-tag>
          <span v-else style="color:#c0c4cc;font-size:12px">—</span>
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
          <dict-tag :options="biz_dbdesign_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:dbdesign:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:dbdesign:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="设计标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入设计标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="DB引擎" prop="dbEngine">
              <el-select v-model="form.dbEngine" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_dbdesign_engine" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="范式检查" prop="normalizationCheck">
              <el-select v-model="form.normalizationCheck" placeholder="请选择" style="width:100%">
                <el-option label="通过" value="Y" />
                <el-option label="未通过" value="N" />
                <el-option label="待检查" value="P" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="ER 图内容" prop="erDiagramContent">
              <el-input v-model="form.erDiagramContent" type="textarea" :rows="4" placeholder="ER 图描述或 Mermaid 代码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="数据字典" prop="dataDictionary">
              <el-input v-model="form.dataDictionary" type="textarea" :rows="5" placeholder="数据字典（Markdown 表格）" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="DDL 脚本" prop="ddlScript">
              <el-input v-model="form.ddlScript" type="textarea" :rows="5" placeholder="CREATE TABLE SQL 脚本" />
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
    <el-dialog title="数据库设计详情" v-model="detailVisible" width="860px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="编号">{{ detail.dbdesignNo }}</el-descriptions-item>
        <el-descriptions-item label="设计标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="DB引擎">
          <dict-tag :options="biz_dbdesign_engine" :value="detail.dbEngine" />
        </el-descriptions-item>
        <el-descriptions-item label="范式检查">
          <el-tag v-if="detail.normalizationCheck === 'Y'" type="success" size="small">通过</el-tag>
          <el-tag v-else-if="detail.normalizationCheck === 'N'" type="danger" size="small">未通过</el-tag>
          <span v-else>待检查</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :options="biz_dbdesign_status" :value="detail.status" />
        </el-descriptions-item>
        <el-descriptions-item label="AI生成">
          <el-tag v-if="detail.aiGenerated === 'Y'" type="warning" size="small">是</el-tag>
          <span v-else>否</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-tabs style="margin-top:16px">
        <el-tab-pane label="ER 图">
          <pre style="white-space:pre-wrap;font-size:13px;margin:0;max-height:280px;overflow-y:auto">{{ detail.erDiagramContent || '（无）' }}</pre>
        </el-tab-pane>
        <el-tab-pane label="数据字典">
          <pre style="white-space:pre-wrap;font-size:13px;margin:0;max-height:280px;overflow-y:auto">{{ detail.dataDictionary || '（无）' }}</pre>
        </el-tab-pane>
        <el-tab-pane label="DDL 脚本">
          <pre style="white-space:pre-wrap;font-size:13px;margin:0;max-height:280px;overflow-y:auto;font-family:monospace">{{ detail.ddlScript || '（无）' }}</pre>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="warning" icon="MagicStick" :loading="aiLoading" @click="handleAiGenerate">AI 生成设计</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDbdesign, getDbdesign, addDbdesign, updateDbdesign, delDbdesign, aiGenerateDbdesign } from '../api'
import type { DbdesignForm, DbdesignQuery } from '../types'

defineOptions({ name: 'Dbdesign' })

const { proxy } = getCurrentInstance()!
const { biz_dbdesign_status, biz_dbdesign_engine } = proxy.useDict('biz_dbdesign_status', 'biz_dbdesign_engine')

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

const queryParams = reactive<DbdesignQuery>({ pageNum: 1, pageSize: 10 })
const form = ref<DbdesignForm>({ title: '' })
const rules = { title: [{ required: true, message: '设计标题不能为空', trigger: 'blur' }] }

function getList() {
  loading.value = true
  listDbdesign(queryParams).then((res: any) => {
    dataList.value = res.rows; total.value = res.total
  }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleSelectionChange(selection: any[]) {
  ids.value = selection.map(r => r.dbdesignId); multiple.value = !ids.value.length
}
function handleAdd() { form.value = { title: '' }; dialogTitle.value = '新增数据库设计'; dialogVisible.value = true }
function handleEdit(row: any) {
  getDbdesign(row.dbdesignId).then((res: any) => { form.value = res.data; dialogTitle.value = '编辑数据库设计'; dialogVisible.value = true })
}
function handleDetail(row: any) {
  getDbdesign(row.dbdesignId).then((res: any) => { detail.value = res.data; detailVisible.value = true })
}
function handleAiGenerate() {
  aiLoading.value = true
  aiGenerateDbdesign(detail.value.dbdesignId).then(() => {
    ElMessage.success('AI 生成完成')
    getDbdesign(detail.value.dbdesignId).then((r: any) => { detail.value = r.data }); getList()
  }).catch(() => ElMessage.error('AI 生成失败')).finally(() => { aiLoading.value = false })
}
function handleDelete(row?: any) {
  const delIds = row ? [row.dbdesignId] : ids.value
  ElMessageBox.confirm('确认删除选中数据库设计？', '警告', { type: 'warning' }).then(() => {
    delDbdesign(delIds).then(() => { ElMessage.success('删除成功'); getList() })
  })
}
function handleExport() { proxy.download('business/dbdesign/export', { ...queryParams }, 'dbdesign.xlsx') }
function submitForm() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.value.dbdesignId ? updateDbdesign : addDbdesign
    api(form.value).then(() => {
      ElMessage.success(form.value.dbdesignId ? '修改成功' : '新增成功')
      dialogVisible.value = false; getList()
    })
  })
}

getList()
</script>

<style scoped>
.section-title { font-weight: 600; font-size: 14px; color: #303133; }
</style>
