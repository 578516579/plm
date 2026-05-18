<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="Flag Key" prop="flagKey">
        <el-input v-model="queryParams.flagKey" placeholder="请输入 Flag Key" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="名称" prop="flagName">
        <el-input v-model="queryParams.flagName" placeholder="请输入名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="环境" prop="environment">
        <el-select v-model="queryParams.environment" placeholder="全部" clearable style="width:100px">
          <el-option v-for="d in biz_feature_flag_env" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="灰度策略" prop="rolloutStrategy">
        <el-select v-model="queryParams.rolloutStrategy" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in biz_feature_flag_strategy" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="enabled">
        <el-select v-model="queryParams.enabled" placeholder="全部" clearable style="width:90px">
          <el-option label="开启" value="Y" />
          <el-option label="关闭" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:feature-flag:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:feature-flag:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:feature-flag:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="Flag Key" align="center" prop="flagKey" width="160" />
      <el-table-column label="名称" align="center" prop="flagName" min-width="140" />
      <el-table-column label="环境" align="center" prop="environment" width="90">
        <template #default="{ row }">
          <dict-tag :options="biz_feature_flag_env" :value="row.environment" />
        </template>
      </el-table-column>
      <el-table-column label="灰度策略" align="center" prop="rolloutStrategy" width="110">
        <template #default="{ row }">
          <dict-tag :options="biz_feature_flag_strategy" :value="row.rolloutStrategy" />
        </template>
      </el-table-column>
      <el-table-column label="灰度%" align="center" prop="rolloutPercentage" width="70">
        <template #default="{ row }">
          <span v-if="row.rolloutStrategy === 'percentage'">{{ row.rolloutPercentage }}%</span>
          <span v-else style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="开关" align="center" width="80">
        <template #default="{ row }">
          <el-switch
            v-model="row.enabled"
            active-value="Y"
            inactive-value="N"
            @change="handleToggle(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:feature-flag:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:feature-flag:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Flag Key" prop="flagKey">
              <el-input v-model="form.flagKey" placeholder="如 enable_ai_prd" :disabled="!!form.flagId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="名称" prop="flagName">
              <el-input v-model="form.flagName" placeholder="请输入显示名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="环境" prop="environment">
              <el-select v-model="form.environment" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_feature_flag_env" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="灰度策略" prop="rolloutStrategy">
              <el-select v-model="form.rolloutStrategy" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_feature_flag_strategy" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.rolloutStrategy === 'percentage'">
            <el-form-item label="灰度百分比" prop="rolloutPercentage">
              <el-input-number v-model="form.rolloutPercentage" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.rolloutStrategy === 'user_list'">
            <el-form-item label="用户白名单" prop="userWhitelist">
              <el-input v-model="form.userWhitelist" placeholder='JSON用户ID数组，如 [1,2,3]' />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入描述" />
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
import type { FeatureFlagForm, FeatureFlagQuery } from '../types'
import { listFeatureFlag, getFeatureFlag, addFeatureFlag, updateFeatureFlag, delFeatureFlag, toggleFeatureFlag } from '../api'

defineOptions({ name: 'FeatureFlag' })

const { biz_feature_flag_env, biz_feature_flag_strategy } = useDict('biz_feature_flag_env', 'biz_feature_flag_strategy')

const loading = ref(false)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const queryRef = ref()
const formRef = ref()

const queryParams = reactive<FeatureFlagQuery>({ pageNum: 1, pageSize: 10 })
const defaultForm = (): FeatureFlagForm => ({ flagKey: '', flagName: '', environment: 'dev', rolloutStrategy: 'all_off', rolloutPercentage: 0 })
const form = reactive<FeatureFlagForm>(defaultForm())
const rules = {
  flagKey: [{ required: true, message: 'Flag Key 不能为空', trigger: 'blur' }],
  flagName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  environment: [{ required: true, message: '请选择环境', trigger: 'change' }]
}

function getList() {
  loading.value = true
  listFeatureFlag(queryParams).then((res: any) => {
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
  dialogTitle.value = '新增 Feature Flag'
  dialogVisible.value = true
}

function handleEdit(row: any) {
  getFeatureFlag(row.flagId).then((res: any) => {
    Object.assign(form, res.data)
    dialogTitle.value = '编辑 Feature Flag'
    dialogVisible.value = true
  })
}

async function handleToggle(row: any) {
  try {
    await toggleFeatureFlag(row.flagId)
    ElMessage.success(`已${row.enabled === 'Y' ? '开启' : '关闭'}`)
  } catch {
    row.enabled = row.enabled === 'Y' ? 'N' : 'Y'
    ElMessage.error('操作失败')
  }
}

function handleDelete(row?: any) {
  const ids = row ? [row.flagId] : dataList.value.filter((r: any) => r._checked).map((r: any) => r.flagId)
  ElMessageBox.confirm('确认删除选中记录？', '警告', { type: 'warning' }).then(() => {
    delFeatureFlag(ids).then(() => { ElMessage.success('删除成功'); getList() })
  })
}

function handleExport() {
  download('/business/feature-flag/export', { ...queryParams }, `feature_flag_${Date.now()}.xlsx`)
}

function handleSubmit() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.flagId ? updateFeatureFlag : addFeatureFlag
    api(form).then(() => {
      ElMessage.success(form.flagId ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getList()
    })
  })
}
</script>
