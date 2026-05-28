<!--
  Feature Flag — 原型 featureflag.html
  Flag 列表 + 灰度策略说明 + 新建 Flag (snake_case)
-->
<template>
  <div class="app-container ff-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🚩 Feature Flag 管理</h2>
        <p class="page-subtitle">功能灰度发布、A/B 测试、紧急开关,支持环境隔离</p>
      </div>
      <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon>&nbsp;+ 新建 Flag</el-button>
    </div>

    <el-card shadow="never">
      <template #header><span class="card-title">🚩 Feature Flag 列表 ({{ total }})</span></template>
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="Flag Key" prop="flagKey" min-width="180">
          <template #default="{ row }"><code>{{ row.flagKey }}</code></template>
        </el-table-column>
        <el-table-column label="名称" prop="flagName" min-width="160" show-overflow-tooltip />
        <el-table-column label="环境" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="envTag(row.environment)" size="small">{{ envLabel(row.environment) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="模式" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="modeTag(row.rolloutMode)" size="small">{{ modeLabel(row.rolloutMode) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="灰度比" width="160" align="center">
          <template #default="{ row }">
            <span v-if="row.rolloutMode === 'all_on'">100%</span>
            <span v-else-if="row.rolloutMode === 'all_off'">0%</span>
            <el-progress v-else :percentage="Number(row.canaryPercentage || 0)" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="{ row }">
            <el-button link type="success" @click="toggle(row, 'all_on')" :disabled="row.rolloutMode === 'all_on'">全开</el-button>
            <el-button link type="warning" @click="toggle(row, 'all_off')" :disabled="row.rolloutMode === 'all_off'">紧急关闭</el-button>
            <el-button link type="primary" @click="loadFF(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 灰度策略说明 -->
    <el-card shadow="never" class="strategy-card">
      <template #header><span class="card-title">🔄 灰度发布策略说明</span></template>
      <el-row :gutter="14">
        <el-col :span="8">
          <strong>全量开启 (100%)</strong><br>
          <span class="muted">所有用户可见,正式发布</span>
        </el-col>
        <el-col :span="8">
          <strong>灰度发布 (1-99%)</strong><br>
          <span class="muted">按用户 ID 哈希随机分流,可随时调整</span>
        </el-col>
        <el-col :span="8">
          <strong>关闭 (0%)</strong><br>
          <span class="muted">所有用户不可见,可作紧急开关</span>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.flagId ? '编辑 Flag' : '+ 新建 Feature Flag'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="Flag Key" prop="flagKey" required>
          <el-input v-model="form.flagKey" placeholder="如:smart_irrigation_v2 (snake_case)" />
        </el-form-item>
        <el-form-item label="Flag 名称" prop="flagName" required>
          <el-input v-model="form.flagName" placeholder="如:智能灌溉 v2 开关" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="form.projectId" style="width: 100%">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="环境" prop="environment">
              <el-select v-model="form.environment" style="width: 100%">
                <el-option label="开发" value="dev" />
                <el-option label="预发" value="staging" />
                <el-option label="生产" value="prod" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="灰度模式" prop="rolloutMode">
          <el-radio-group v-model="form.rolloutMode">
            <el-radio value="all_off">关闭 (0%)</el-radio>
            <el-radio value="canary">灰度</el-radio>
            <el-radio value="all_on">全量 (100%)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.rolloutMode === 'canary'" label="灰度比例" prop="canaryPercentage">
          <el-slider v-model="form.canaryPercentage" :min="1" :max="99" show-input />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">{{ form.flagId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  listFeatureFlag, addFeatureFlag, updateFeatureFlag, delFeatureFlag, getFeatureFlag, listProjectsForSelect,
  type FeatureFlag, type FlagQuery
} from '@/api/business/feature-flag'
import { envLabel, envTag, modeLabel, modeTag } from './featureFlagDict'

const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const listLoading = ref(false)

const emptyForm = (): FeatureFlag => ({ flagKey: '', flagName: '', environment: 'dev', rolloutMode: 'all_off', canaryPercentage: 10, authorUserId: 1 })
const form = reactive<FeatureFlag>(emptyForm())
const rules = {
  flagKey: [
    { required: true, message: '请输入 Flag Key', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: 'Flag Key 必须为 snake_case', trigger: 'blur' }
  ],
  flagName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const list = ref<FeatureFlag[]>([])
const total = ref(0)
const queryParams = reactive<FlagQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

async function getList() {
  listLoading.value = true
  try { const res: any = await listFeatureFlag(queryParams); list.value = res.rows || []; total.value = res.total || 0 } finally { listLoading.value = false }
}
async function loadProjects() {
  try { const res: any = await listProjectsForSelect(); projectOptions.value = res.rows || [] } catch {}
}

function openAdd() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function loadFF(row: FeatureFlag) {
  if (!row.flagId) return
  const res: any = await getFeatureFlag(row.flagId)
  if (res.code === 200 && res.data) { Object.assign(form, res.data); dialogVisible.value = true }
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.flagId) { await updateFeatureFlag(form); ElMessage.success('更新成功') }
    else { await addFeatureFlag(form); ElMessage.success('创建成功') }
    dialogVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') } finally { saving.value = false }
}

async function toggle(row: FeatureFlag, target: 'all_on' | 'all_off') {
  if (!row.flagId) return
  try {
    await updateFeatureFlag({ ...row, rolloutMode: target })
    ElMessage.success(`${row.flagKey} ${target === 'all_on' ? '已全量开启' : '已紧急关闭'}`)
    await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '切换失败') }
}

async function handleDelete(row: FeatureFlag) {
  if (!row.flagId) return
  await ElMessageBox.confirm(`删除 Flag "${row.flagKey}"?`, '提示', { type: 'warning' })
  await delFeatureFlag(row.flagId); ElMessage.success('删除成功'); await getList()
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.ff-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
code { background: #f4f4f5; padding: 2px 6px; border-radius: 3px; font-family: monospace; font-size: 12px; color: #2d7a4f; }
.strategy-card { margin-top: 14px; background: #e8f5ee; }
.strategy-card :deep(.el-card__header) { border-left: 4px solid #2d7a4f; background: #e8f5ee; }
.muted { color: #6b7280; font-size: 12px; }
</style>
