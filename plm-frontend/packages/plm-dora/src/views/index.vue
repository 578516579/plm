<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="周期" prop="period">
        <el-input v-model="queryParams.period" placeholder="如 2026-05 或 2026-Q2" clearable style="width:150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="效能等级" prop="doraLevel">
        <el-select v-model="queryParams.doraLevel" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in biz_dora_level" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
          <el-option v-for="d in biz_dora_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:dora:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:dora:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:dora:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="doraNo" width="160" />
      <el-table-column label="周期" align="center" prop="period" width="110" />
      <el-table-column label="部署频率(次/天)" align="center" prop="deployFrequency" width="130">
        <template #default="{ row }">
          <span v-if="row.deployFrequency != null">{{ row.deployFrequency }}</span>
          <span v-else style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="前置时间(h)" align="center" prop="leadTimeHours" width="110">
        <template #default="{ row }">
          <span v-if="row.leadTimeHours != null">{{ row.leadTimeHours }}</span>
          <span v-else style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="变更失败率%" align="center" prop="changeFailureRate" width="115">
        <template #default="{ row }">
          <span v-if="row.changeFailureRate != null">{{ row.changeFailureRate }}%</span>
          <span v-else style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="MTTR(h)" align="center" prop="mttrHours" width="90">
        <template #default="{ row }">
          <span v-if="row.mttrHours != null">{{ row.mttrHours }}</span>
          <span v-else style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="效能等级" align="center" prop="doraLevel" width="110">
        <template #default="{ row }">
          <dict-tag v-if="row.doraLevel" :options="biz_dora_level" :value="row.doraLevel" />
          <span v-else style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="{ row }">
          <dict-tag :options="biz_dora_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template #default="{ row }">
          <el-button link type="primary" icon="MagicStick" v-hasPermi="['business:dora:edit']" @click="handleAiGenerate(row)" :loading="row._aiLoading" :disabled="row.status === '01'">AI分析</el-button>
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:dora:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:dora:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="周期" prop="period">
              <el-input v-model="form.period" placeholder="如 2026-05 或 2026-Q2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部署频率(次/天)" prop="deployFrequency">
              <el-input-number v-model="form.deployFrequency as number" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="前置时间(h)" prop="leadTimeHours">
              <el-input-number v-model="form.leadTimeHours as number" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="变更失败率%" prop="changeFailureRate">
              <el-input-number v-model="form.changeFailureRate as number" :min="0" :max="100" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="MTTR(h)" prop="mttrHours">
              <el-input-number v-model="form.mttrHours as number" :min="0" :precision="2" style="width:100%" />
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
import type { DoraForm, DoraQuery } from '../types'
import { listDora, getDora, addDora, updateDora, delDora, aiGenerateDora } from '../api'

defineOptions({ name: 'Dora' })

const { biz_dora_level, biz_dora_status } = useDict('biz_dora_level', 'biz_dora_status')

const loading = ref(false)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryRef = ref()
const formRef = ref()

const queryParams = reactive<DoraQuery>({ pageNum: 1, pageSize: 10 })
const defaultForm = (): DoraForm => ({ period: '' })
const form = reactive<DoraForm>(defaultForm())
const rules = { period: [{ required: true, message: '周期不能为空', trigger: 'blur' }] }

function getList() {
  loading.value = true
  listDora(queryParams).then((res: any) => {
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
  dialogTitle.value = '新增DORA效能记录'
  dialogVisible.value = true
}

function handleEdit(row: any) {
  getDora(row.doraId).then((res: any) => {
    Object.assign(form, res.data)
    dialogTitle.value = '编辑DORA效能记录'
    dialogVisible.value = true
  })
}

async function handleAiGenerate(row: any) {
  row._aiLoading = true
  try {
    await aiGenerateDora(row.doraId)
    ElMessage.success('AI分析生成成功')
    getList()
  } catch {
    ElMessage.error('AI分析失败')
  } finally {
    row._aiLoading = false
  }
}

function handleDelete(row?: any) {
  const ids = row ? [row.doraId] : dataList.value.filter((r: any) => r._checked).map((r: any) => r.doraId)
  ElMessageBox.confirm('确认删除选中记录？', '警告', { type: 'warning' }).then(() => {
    delDora(ids).then(() => { ElMessage.success('删除成功'); getList() })
  })
}

function handleExport() {
  download('/business/dora/export', { ...queryParams }, `dora_${Date.now()}.xlsx`)
}

function handleSubmit() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.doraId ? updateDora : addDora
    api(form).then(() => {
      ElMessage.success(form.doraId ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getList()
    })
  })
}
</script>
