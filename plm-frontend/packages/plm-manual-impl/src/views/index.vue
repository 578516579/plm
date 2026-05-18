<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="手册标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="部署方式" prop="deploymentMode">
        <el-select v-model="queryParams.deploymentMode" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in biz_manual_impl_deploy" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作系统" prop="os">
        <el-select v-model="queryParams.os" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in biz_manual_impl_os" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
          <el-option v-for="d in biz_manual_impl_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:manual-impl:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:manual-impl:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:manual-impl:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="manualImplNo" width="160" />
      <el-table-column label="标题" align="center" prop="title" min-width="160" />
      <el-table-column label="部署方式" align="center" prop="deploymentMode" width="110">
        <template #default="{ row }">
          <dict-tag :options="biz_manual_impl_deploy" :value="row.deploymentMode" />
        </template>
      </el-table-column>
      <el-table-column label="操作系统" align="center" prop="os" width="110">
        <template #default="{ row }">
          <dict-tag :options="biz_manual_impl_os" :value="row.os" />
        </template>
      </el-table-column>
      <el-table-column label="数据库" align="center" prop="database" width="110">
        <template #default="{ row }">
          <dict-tag :options="biz_manual_impl_db" :value="row.database" />
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
          <dict-tag :options="biz_manual_impl_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template #default="{ row }">
          <el-button link type="primary" icon="MagicStick" v-hasPermi="['business:manual-impl:edit']" @click="handleAiGenerate(row)" :loading="row._aiLoading">AI生成</el-button>
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:manual-impl:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:manual-impl:remove']" @click="handleDelete(row)">删除</el-button>
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
          <el-col :span="8">
            <el-form-item label="部署方式" prop="deploymentMode">
              <el-select v-model="form.deploymentMode" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_manual_impl_deploy" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="操作系统" prop="os">
              <el-select v-model="form.os" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_manual_impl_os" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="数据库" prop="database">
              <el-select v-model="form.database" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_manual_impl_db" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="环境变量" prop="envVars">
              <el-input v-model="form.envVars" type="textarea" :rows="2" placeholder="KEY=VALUE，多个换行" />
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
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import { download } from '@/utils/request'
import type { ManualImplForm, ManualImplQuery } from '../types'
import { listManualImpl, getManualImpl, addManualImpl, updateManualImpl, delManualImpl, aiGenerateManualImpl } from '../api'

defineOptions({ name: 'ManualImpl' })

const { biz_manual_impl_deploy, biz_manual_impl_os, biz_manual_impl_db, biz_manual_impl_status } =
  useDict('biz_manual_impl_deploy', 'biz_manual_impl_os', 'biz_manual_impl_db', 'biz_manual_impl_status')

const loading = ref(false)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryRef = ref()
const formRef = ref()

const queryParams = reactive<ManualImplQuery>({ pageNum: 1, pageSize: 10 })
const defaultForm = (): ManualImplForm => ({ title: '', deploymentMode: 'docker', os: 'centos7', database: 'mysql8' })
const form = reactive<ManualImplForm>(defaultForm())
const rules = { title: [{ required: true, message: '标题不能为空', trigger: 'blur' }] }

function getList() {
  loading.value = true
  listManualImpl(queryParams).then(res => {
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
  dialogTitle.value = '新增实施手册'
  dialogVisible.value = true
}

function handleEdit(row: any) {
  getManualImpl(row.manualImplId).then(res => {
    Object.assign(form, res.data)
    dialogTitle.value = '编辑实施手册'
    dialogVisible.value = true
  })
}

async function handleAiGenerate(row: any) {
  row._aiLoading = true
  try {
    await aiGenerateManualImpl(row.manualImplId)
    ElMessage.success('AI生成成功')
    getList()
  } catch {
    ElMessage.error('AI生成失败')
  } finally {
    row._aiLoading = false
  }
}

function handleDelete(row?: any) {
  const ids = row ? [row.manualImplId] : dataList.value.filter((r: any) => r._checked).map((r: any) => r.manualImplId)
  ElMessageBox.confirm('确认删除选中记录？', '警告', { type: 'warning' }).then(() => {
    delManualImpl(ids).then(() => { ElMessage.success('删除成功'); getList() })
  })
}

function handleExport() {
  download('/business/manual-impl/export', { ...queryParams }, `manual_impl_${Date.now()}.xlsx`)
}

function handleSubmit() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.manualImplId ? updateManualImpl : addManualImpl
    api(form).then(() => {
      ElMessage.success(form.manualImplId ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getList()
    })
  })
}
</script>
