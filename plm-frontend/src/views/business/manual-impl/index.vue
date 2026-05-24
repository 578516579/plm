<!--
  实施手册 — PRD §F5.2 + 原型 implmanual.html
  严格对齐: 左卡配置 (部署模式/OS/DB/环境变量) + 右卡 AI 生成手册预览
-->
<template>
  <div class="app-container mi-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🚀 实施手册</h2>
        <p class="page-subtitle">AI 生成部署安装、初始化配置、升级步骤手册</p>
      </div>
      <el-button type="success" :loading="aiLoading" :disabled="!current.manualImplId" @click="triggerAi">
        <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成实施手册
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">⚙️ 配置信息</span></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-form-item label="关联项目" prop="projectId" required>
              <el-select v-model="form.projectId" style="width: 100%">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="手册名称" prop="title" required>
              <el-input v-model="form.title" placeholder="如:智慧灌溉 v0.5 实施手册" />
            </el-form-item>
            <el-form-item label="部署模式" prop="deployMode">
              <el-select v-model="form.deployMode" style="width: 100%">
                <el-option label="Docker Compose (单机)" value="docker_compose" />
                <el-option label="Kubernetes (集群)" value="k8s" />
                <el-option label="裸机部署" value="baremetal" />
              </el-select>
            </el-form-item>
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="操作系统" prop="osType">
                  <el-select v-model="form.osType" style="width: 100%">
                    <el-option label="CentOS 7+" value="centos" />
                    <el-option label="Ubuntu 20.04" value="ubuntu" />
                    <el-option label="麒麟 OS (国产化)" value="kylin" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="数据库" prop="databaseType">
                  <el-select v-model="form.databaseType" style="width: 100%">
                    <el-option label="PostgreSQL 14" value="postgres" />
                    <el-option label="MySQL 8.0" value="mysql" />
                    <el-option label="人大金仓 (国产化)" value="kingbase" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="环境变量 (JSON)" prop="envVars">
              <el-input v-model="form.envVars" type="textarea" :rows="4" class="json-input"
                placeholder='{"DB_HOST":"192.168.1.100","DB_PORT":"5432","REDIS_HOST":"192.168.1.101"}' />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSubmit(false)">
                <el-icon><DocumentAdd /></el-icon>&nbsp;保存
              </el-button>
              <el-button type="success" :loading="saving || aiLoading" @click="handleSubmit(true)">
                <el-icon><MagicStick /></el-icon>&nbsp;✨ 保存并生成手册
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="preview-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">🚀 实施手册预览</span>
              <el-tag :type="statusTag.type" size="small">{{ statusTag.label }}</el-tag>
            </div>
          </template>
          <div v-if="!current.manualImplId" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><Document /></el-icon>
            <p>配置后点击「保存并生成手册」</p>
          </div>
          <div v-else-if="aiLoading" class="loading-state">
            <el-icon class="rotate" :size="36" color="#3b82f6"><Loading /></el-icon>
            <p>AI 正在生成实施手册,预计 10-20 秒……</p>
          </div>
          <div v-else-if="hasManual" class="manual-content">
            <el-collapse v-model="activeSection">
              <el-collapse-item title="📦 安装步骤" name="install">
                <pre class="md-content">{{ current.installSteps }}</pre>
              </el-collapse-item>
              <el-collapse-item title="⚙️ 初始化配置" name="initConfig">
                <pre class="md-content">{{ current.initConfigSteps }}</pre>
              </el-collapse-item>
              <el-collapse-item title="🔄 升级步骤" name="upgrade">
                <pre class="md-content">{{ current.upgradeSteps }}</pre>
              </el-collapse-item>
              <el-collapse-item title="↩️ 回滚步骤" name="rollback">
                <pre class="md-content">{{ current.rollbackSteps }}</pre>
              </el-collapse-item>
            </el-collapse>
          </div>
          <div v-else class="ai-not-yet">
            <el-icon :size="40" color="#f59e0b"><InfoFilled /></el-icon>
            <p>配置已保存 ({{ current.manualImplNo }}),点击生成</p>
            <el-button type="success" :loading="aiLoading" @click="triggerAi">✨ 立即生成</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header><span class="card-title">📚 历史实施手册 ({{ total }})</span></template>
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="manualImplNo" width="160" />
        <el-table-column label="手册名" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="部署" width="120" align="center">
          <template #default="{ row }">{{ deployLabel(row.deployMode) }}</template>
        </el-table-column>
        <el-table-column label="OS" width="100" align="center">
          <template #default="{ row }">{{ osLabel(row.osType) }}</template>
        </el-table-column>
        <el-table-column label="AI" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.aiGenerated === 'Y'" type="success" size="small">已生成</el-tag>
            <el-tag v-else type="info" size="small">未生成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadM(row)">载入</el-button>
            <el-button link type="success" @click="quickAi(row)">AI</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, DocumentAdd, Document, Loading, InfoFilled } from '@element-plus/icons-vue'
import {
  listManualImpl, addManualImpl, updateManualImpl, delManualImpl, aiGenerateManualImpl, getManualImpl, listProjectsForSelect,
  type ManualImpl, type ManualImplQuery
} from '@/api/business/manual-impl'

const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)
const activeSection = ref(['install'])

const emptyForm = (): ManualImpl => ({
  projectId: 0, title: '', deployMode: 'docker_compose', osType: 'centos', databaseType: 'postgres',
  envVars: '{"DB_HOST":"","DB_PORT":"5432","REDIS_HOST":""}', authorUserId: 1
})
const form = reactive<ManualImpl>(emptyForm())
const current = reactive<ManualImpl>({ projectId: 0, title: '' })
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const list = ref<ManualImpl[]>([])
const total = ref(0)
const queryParams = reactive<ManualImplQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

const hasManual = computed(() => !!(current.installSteps || current.initConfigSteps))
const statusMap: Record<string, { label: string; type: any }> = {
  '00': { label: '草稿', type: 'info' }, '01': { label: '评审中', type: 'warning' },
  '02': { label: '已发布', type: 'success' }, '03': { label: '已废弃', type: 'danger' }
}
const statusTagFor = (s?: string) => statusMap[s || '00'] || { label: s || '-', type: 'info' as any }
const statusTag = computed(() => statusTagFor(current.status))

const deployLabel = (v?: string) => ({ docker_compose: 'Docker', k8s: 'K8s', baremetal: '裸机' } as Record<string,string>)[v||''] || v || '-'
const osLabel = (v?: string) => ({ centos: 'CentOS', ubuntu: 'Ubuntu', kylin: '麒麟' } as Record<string,string>)[v||''] || v || '-'

async function getList() {
  listLoading.value = true
  try { const res: any = await listManualImpl(queryParams); list.value = res.rows || []; total.value = res.total || 0 } finally { listLoading.value = false }
}
async function loadProjects() {
  try { const res: any = await listProjectsForSelect(); projectOptions.value = res.rows || [] } catch {}
}

async function handleSubmit(triggerAfter: boolean) {
  await formRef.value.validate()
  saving.value = true
  try {
    if (current.manualImplId) { await updateManualImpl({ ...form, manualImplId: current.manualImplId }); ElMessage.success('更新成功') }
    else {
      await addManualImpl(form); ElMessage.success('保存成功'); await getList()
      const latest = list.value.find(x => x.title === form.title)
      if (latest?.manualImplId) Object.assign(current, latest)
    }
    if (triggerAfter && current.manualImplId) await triggerAi()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') } finally { saving.value = false }
}

async function triggerAi() {
  if (!current.manualImplId) return
  aiLoading.value = true
  try {
    const res: any = await aiGenerateManualImpl(current.manualImplId)
    if (res.code === 200 && res.data) { Object.assign(current, res.data); ElMessage.success('实施手册已生成'); await getList() }
  } catch (e: any) { ElMessage.error(e?.msg || 'AI 失败') } finally { aiLoading.value = false }
}

async function quickAi(row: ManualImpl) {
  if (!row.manualImplId) return
  aiLoading.value = true
  try { await aiGenerateManualImpl(row.manualImplId); ElMessage.success('已生成'); await getList() } finally { aiLoading.value = false }
}

async function loadM(row: ManualImpl) {
  if (!row.manualImplId) return
  const res: any = await getManualImpl(row.manualImplId)
  if (res.code === 200 && res.data) { Object.assign(form, res.data); Object.assign(current, res.data); ElMessage.info(`已载入 ${res.data.manualImplNo}`) }
}

async function handleDelete(row: ManualImpl) {
  if (!row.manualImplId) return
  await ElMessageBox.confirm(`删除 "${row.manualImplNo}"?`, '提示', { type: 'warning' })
  await delManualImpl(row.manualImplId); ElMessage.success('删除成功')
  if (current.manualImplId === row.manualImplId) Object.keys(current).forEach(k => delete (current as any)[k])
  await getList()
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.mi-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.json-input :deep(.el-textarea__inner) { font-family: 'Consolas', monospace; font-size: 11.5px; }
.preview-card { min-height: 560px; }
.empty-state, .loading-state, .ai-not-yet { text-align: center; padding: 50px 20px; color: #6b7280; }
.empty-state p, .loading-state p, .ai-not-yet p { margin: 12px 0; }
.rotate { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.md-content { font-family: 'Consolas', monospace; font-size: 12px; line-height: 1.7; white-space: pre-wrap; color: #374151; }
</style>
