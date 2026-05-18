<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="套件名称" prop="suiteName">
        <el-input v-model="queryParams.suiteName" placeholder="请输入套件名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="框架" prop="framework">
        <el-select v-model="queryParams.framework" placeholder="全部" clearable style="width:130px">
          <el-option v-for="d in biz_autotest_framework" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标模块" prop="targetModule">
        <el-input v-model="queryParams.targetModule" placeholder="请输入目标模块" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
          <el-option v-for="d in biz_autotest_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:autotest:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:autotest:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:autotest:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="autotestNo" width="160" />
      <el-table-column label="套件名称" align="center" prop="suiteName" min-width="150" />
      <el-table-column label="框架" align="center" prop="framework" width="110">
        <template #default="{ row }">
          <dict-tag :options="biz_autotest_framework" :value="row.framework" />
        </template>
      </el-table-column>
      <el-table-column label="目标模块" align="center" prop="targetModule" width="120" />
      <el-table-column label="通过率" align="center" prop="lastRunPassRate" width="90">
        <template #default="{ row }">
          <span v-if="row.lastRunPassRate != null">{{ row.lastRunPassRate }}%</span>
          <span v-else style="color:#c0c4cc">—</span>
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
          <dict-tag :options="biz_autotest_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template #default="{ row }">
          <el-button link type="primary" icon="MagicStick" v-hasPermi="['business:autotest:edit']" @click="handleAiGenerate(row)" :loading="row._aiLoading">AI生成</el-button>
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:autotest:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:autotest:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="套件名称" prop="suiteName">
              <el-input v-model="form.suiteName" placeholder="请输入套件名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="测试框架" prop="framework">
              <el-select v-model="form.framework" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_autotest_framework" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标模块" prop="targetModule">
              <el-input v-model="form.targetModule" placeholder="请输入目标模块" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入描述" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="脚本内容" prop="scriptContent">
              <el-input v-model="form.scriptContent" type="textarea" :rows="6" placeholder="测试脚本内容（可由AI生成）" />
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
import type { AutotestForm, AutotestQuery } from '../types'
import { listAutotest, getAutotest, addAutotest, updateAutotest, delAutotest, aiGenerateAutotest } from '../api'

defineOptions({ name: 'Autotest' })

const { biz_autotest_framework, biz_autotest_status } = useDict('biz_autotest_framework', 'biz_autotest_status')

const loading = ref(false)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryRef = ref()
const formRef = ref()

const queryParams = reactive<AutotestQuery>({ pageNum: 1, pageSize: 10 })

const defaultForm = (): AutotestForm => ({ suiteName: '', framework: 'pytest' })
const form = reactive<AutotestForm>(defaultForm())

const rules = { suiteName: [{ required: true, message: '套件名称不能为空', trigger: 'blur' }] }

function getList() {
  loading.value = true
  listAutotest(queryParams).then(res => {
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
  dialogTitle.value = '新增自动化测试套件'
  dialogVisible.value = true
}

function handleEdit(row: any) {
  getAutotest(row.autotestId).then(res => {
    Object.assign(form, res.data)
    dialogTitle.value = '编辑自动化测试套件'
    dialogVisible.value = true
  })
}

async function handleAiGenerate(row: any) {
  row._aiLoading = true
  try {
    await aiGenerateAutotest(row.autotestId)
    ElMessage.success('AI生成脚本成功')
    getList()
  } catch {
    ElMessage.error('AI生成失败')
  } finally {
    row._aiLoading = false
  }
}

function handleDelete(row?: any) {
  const ids = row ? [row.autotestId] : dataList.value.filter((r: any) => r._checked).map((r: any) => r.autotestId)
  ElMessageBox.confirm(`确认删除选中的记录？`, '警告', { type: 'warning' }).then(() => {
    delAutotest(ids).then(() => { ElMessage.success('删除成功'); getList() })
  })
}

function handleExport() {
  download('/business/autotest/export', { ...queryParams }, `autotest_${Date.now()}.xlsx`)
}

function handleSubmit() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.autotestId ? updateAutotest : addAutotest
    api(form).then(() => {
      ElMessage.success(form.autotestId ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getList()
    })
  })
}
</script>
