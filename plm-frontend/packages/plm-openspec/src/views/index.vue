<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="规范名称" prop="specName">
        <el-input v-model="queryParams.specName" placeholder="请输入规范名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="规范类型" prop="specType">
        <el-select v-model="queryParams.specType" placeholder="全部" clearable style="width:140px">
          <el-option v-for="d in biz_openspec_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="AI增强" prop="aiEnhanced">
        <el-select v-model="queryParams.aiEnhanced" placeholder="全部" clearable style="width:100px">
          <el-option label="已增强" value="Y" />
          <el-option label="未增强" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
          <el-option v-for="d in biz_openspec_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:openspec:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:openspec:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:openspec:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="openspecNo" width="160" />
      <el-table-column label="规范名称" align="center" prop="specName" min-width="150" />
      <el-table-column label="类型" align="center" prop="specType" width="120">
        <template #default="{ row }">
          <dict-tag :options="biz_openspec_type" :value="row.specType" />
        </template>
      </el-table-column>
      <el-table-column label="版本" align="center" prop="version" width="80" />
      <el-table-column label="AI增强" align="center" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.aiEnhanced === 'Y'" type="warning" size="small">已增强</el-tag>
          <span v-else style="color:#c0c4cc;font-size:12px">—</span>
        </template>
      </el-table-column>
      <el-table-column label="AgriKB" align="center" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.agrikbRef === 'Y'" type="success" size="small">引用</el-tag>
          <span v-else style="color:#c0c4cc;font-size:12px">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="{ row }">
          <dict-tag :options="biz_openspec_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template #default="{ row }">
          <el-button link type="primary" icon="MagicStick" v-hasPermi="['business:openspec:edit']" @click="handleAiGenerate(row)" :loading="row._aiLoading">AI增强</el-button>
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:openspec:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:openspec:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="规范名称" prop="specName">
              <el-input v-model="form.specName" placeholder="请输入规范名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规范类型" prop="specType">
              <el-select v-model="form.specType" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_openspec_type" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本" prop="version">
              <el-input v-model="form.version" placeholder="如 v1.0" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="规范内容" prop="content">
              <el-input v-model="form.content" type="textarea" :rows="8" placeholder="YAML 或 JSON 规范内容（可由AI生成）" />
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
import type { OpenspecForm, OpenspecQuery } from '../types'
import { listOpenspec, getOpenspec, addOpenspec, updateOpenspec, delOpenspec, aiGenerateOpenspec } from '../api'

defineOptions({ name: 'Openspec' })

const { biz_openspec_type, biz_openspec_status } = useDict('biz_openspec_type', 'biz_openspec_status')

const loading = ref(false)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryRef = ref()
const formRef = ref()

const queryParams = reactive<OpenspecQuery>({ pageNum: 1, pageSize: 10 })
const defaultForm = (): OpenspecForm => ({ specName: '', specType: 'openapi31', version: 'v1.0' })
const form = reactive<OpenspecForm>(defaultForm())
const rules = { specName: [{ required: true, message: '规范名称不能为空', trigger: 'blur' }] }

function getList() {
  loading.value = true
  listOpenspec(queryParams).then(res => {
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
  dialogTitle.value = '新增AI规范'
  dialogVisible.value = true
}

function handleEdit(row: any) {
  getOpenspec(row.openspecId).then(res => {
    Object.assign(form, res.data)
    dialogTitle.value = '编辑AI规范'
    dialogVisible.value = true
  })
}

async function handleAiGenerate(row: any) {
  row._aiLoading = true
  try {
    await aiGenerateOpenspec(row.openspecId)
    ElMessage.success('AI规范增强成功')
    getList()
  } catch {
    ElMessage.error('AI增强失败')
  } finally {
    row._aiLoading = false
  }
}

function handleDelete(row?: any) {
  const ids = row ? [row.openspecId] : dataList.value.filter((r: any) => r._checked).map((r: any) => r.openspecId)
  ElMessageBox.confirm('确认删除选中记录？', '警告', { type: 'warning' }).then(() => {
    delOpenspec(ids).then(() => { ElMessage.success('删除成功'); getList() })
  })
}

function handleExport() {
  download('/business/openspec/export', { ...queryParams }, `openspec_${Date.now()}.xlsx`)
}

function handleSubmit() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.openspecId ? updateOpenspec : addOpenspec
    api(form).then(() => {
      ElMessage.success(form.openspecId ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getList()
    })
  })
}
</script>
