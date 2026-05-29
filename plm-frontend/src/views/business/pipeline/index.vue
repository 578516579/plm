<!--
  CI/CD Pipeline — 原型 pipeline.html
  4 统计卡 (总/成功/运行中/失败+成功率) + 流水线列表 + 触发流水线
-->
<template>
  <div class="app-container pl-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">⚙️ CI/CD 流水线</h2>
        <p class="page-subtitle">AI 驱动的持续集成/持续部署,全链路可观测</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="openAdd"><el-icon><Plus /></el-icon>&nbsp;新建流水线</el-button>
        <el-button type="primary" :disabled="!current.pipelineId" @click="trigger">
          <el-icon><VideoPlay /></el-icon>&nbsp;▶ 触发流水线
        </el-button>
      </div>
    </div>

    <!-- 4 统计卡 -->
    <el-row :gutter="14" class="stat-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">总流水线</div>
          <div class="stat-value">{{ total }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">成功</div>
          <div class="stat-value success">{{ successCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">运行中</div>
          <div class="stat-value blue">{{ runningCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">失败</div>
          <div class="stat-value red">{{ failedCount }}</div>
          <div class="stat-detail">成功率 <strong>{{ successRate }}%</strong></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 14px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">🔄 流水线列表</span>
          <el-input v-model="queryParams.pipelineName" placeholder="搜索" style="width: 240px" clearable @clear="getList" @keyup.enter="getList" />
        </div>
      </template>
      <el-table v-loading="listLoading" :data="list" stripe highlight-current-row @current-change="onSelect">
        <el-table-column label="编号" prop="pipelineNo" width="140" />
        <el-table-column label="流水线" prop="pipelineName" min-width="160" show-overflow-tooltip />
        <el-table-column label="工具" width="100" align="center">
          <template #default="{ row }">{{ ciToolLabel(row.ciTool) }}</template>
        </el-table-column>
        <el-table-column label="触发" width="80" align="center">
          <template #default="{ row }">{{ triggerLabel(row.triggerType) }}</template>
        </el-table-column>
        <el-table-column label="成功率" width="120" align="center">
          <template #default="{ row }">
            <el-progress :percentage="Number(row.successRate || 0)" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column label="上次执行" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="lastRunTag(row.lastRunStatus)">{{ lastRunLabel(row.lastRunStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button link type="success" @click.stop="triggerRow(row)">▶ 触发</el-button>
            <el-button link type="primary" @click.stop="loadPL(row)">详情</el-button>
            <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.pipelineId ? '编辑流水线' : '新建流水线'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="关联项目" prop="projectId" required>
          <el-select v-model="form.projectId" style="width: 100%">
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="流水线名" prop="pipelineName" required>
          <el-input v-model="form.pipelineName" placeholder="如:agriplm-backend-prod" />
        </el-form-item>
        <el-form-item label="代码仓库" prop="repoUrl">
          <el-input v-model="form.repoUrl" placeholder="git@gitlab.com:..." />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="CI 工具" prop="ciTool">
              <el-select v-model="form.ciTool" style="width: 100%">
                <el-option label="Jenkins" value="jenkins" />
                <el-option label="GitLab CI" value="gitlab_ci" />
                <el-option label="GitHub Actions" value="github_actions" />
                <el-option label="Drone" value="drone" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="触发方式" prop="triggerType">
              <el-select v-model="form.triggerType" style="width: 100%">
                <el-option label="Push" value="push" />
                <el-option label="Merge Request" value="pr" />
                <el-option label="Tag" value="tag" />
                <el-option label="Cron 定时" value="cron" />
                <el-option label="手动" value="manual" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="form.triggerType === 'cron'" label="Cron 表达式" prop="cronExpr">
          <el-input v-model="form.cronExpr" placeholder="0 2 * * *" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">{{ form.pipelineId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, VideoPlay } from '@element-plus/icons-vue'
import {
  listPipeline, addPipeline, updatePipeline, delPipeline, getPipeline, triggerPipeline, listProjectsForSelect,
  type Pipeline, type PipelineQuery
} from '@/api/business/pipeline'
import { ciToolLabel, triggerLabel, lastRunLabel, lastRunTag } from './pipelineDict'

const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const listLoading = ref(false)

const emptyForm = (): Pipeline => ({ projectId: 0, pipelineName: '', ciTool: 'jenkins', triggerType: 'push', authorUserId: 1 })
const form = reactive<Pipeline>(emptyForm())
const current = reactive<Pipeline>({ projectId: 0, pipelineName: '' })
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  pipelineName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const list = ref<Pipeline[]>([])
const total = ref(0)
const queryParams = reactive<PipelineQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

const successCount = computed(() => list.value.filter(x => x.lastRunStatus === 'success').length)
const runningCount = computed(() => list.value.filter(x => x.lastRunStatus === 'running').length)
const failedCount = computed(() => list.value.filter(x => x.lastRunStatus === 'failed').length)
const successRate = computed(() => {
  if (!list.value.length) return 0
  return Math.round((successCount.value / list.value.length) * 100)
})

async function getList() {
  listLoading.value = true
  try { const res: any = await listPipeline(queryParams); list.value = res.rows || []; total.value = res.total || 0 } finally { listLoading.value = false }
}
async function loadProjects() {
  try { const res: any = await listProjectsForSelect(); projectOptions.value = res.rows || [] } catch {}
}

function onSelect(row: Pipeline | null) {
  if (row) Object.assign(current, row)
  else Object.keys(current).forEach(k => delete (current as any)[k])
}

function openAdd() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function loadPL(row: Pipeline) {
  if (!row.pipelineId) return
  const res: any = await getPipeline(row.pipelineId)
  if (res.code === 200 && res.data) { Object.assign(form, res.data); dialogVisible.value = true }
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.pipelineId) { await updatePipeline(form); ElMessage.success('更新成功') }
    else { await addPipeline(form); ElMessage.success('创建成功') }
    dialogVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') } finally { saving.value = false }
}

async function trigger() {
  if (!current.pipelineId) return
  try {
    await triggerPipeline(current.pipelineId); ElMessage.success(`${current.pipelineName} 已触发`); await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '触发失败') }
}

async function triggerRow(row: Pipeline) {
  if (!row.pipelineId) return
  try { await triggerPipeline(row.pipelineId); ElMessage.success(`${row.pipelineName} 已触发`); await getList() } catch (e: any) { ElMessage.error(e?.msg || '触发失败') }
}

async function handleDelete(row: Pipeline) {
  if (!row.pipelineId) return
  await ElMessageBox.confirm(`删除 "${row.pipelineName}"?`, '提示', { type: 'warning' })
  await delPipeline(row.pipelineId); ElMessage.success('删除成功')
  if (current.pipelineId === row.pipelineId) Object.keys(current).forEach(k => delete (current as any)[k])
  await getList()
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.pl-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.stat-row :deep(.el-card__body) { padding: 14px 16px; }
.stat-label { color: #6b7280; font-size: 12px; margin-bottom: 5px; }
.stat-value { font-size: 26px; font-weight: 700; color: #2d7a4f; }
.stat-value.success { color: #10b981; }
.stat-value.blue { color: #3b82f6; }
.stat-value.red { color: #ef4444; }
.stat-detail { font-size: 11px; color: #6b7280; margin-top: 3px; }
</style>
