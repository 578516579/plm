<!--
  运维手册 — PRD §F5.3 + 原型 opsmanual.html
  严格对齐: 左卡运维配置 (监控/告警渠道/IoT 设备) + 右卡 AI 生成运维手册
-->
<template>
  <div class="app-container mo-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🛠️ 运维手册</h2>
        <p class="page-subtitle">AI 生成监控告警、故障排查、备份恢复、IoT 运维指南</p>
      </div>
      <el-button type="success" :loading="aiLoading" :disabled="!current.manualOpsId" @click="triggerAi">
        <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成运维手册
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">⚙️ 运维配置</span></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-form-item label="关联项目" prop="projectId" required>
              <el-select v-model="form.projectId" style="width: 100%">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="手册名称" prop="title" required>
              <el-input v-model="form.title" placeholder="如:智慧灌溉 v0.5 运维手册" />
            </el-form-item>
            <el-form-item label="监控方案" prop="monitoringSolution">
              <el-select v-model="form.monitoringSolution" style="width: 100%">
                <el-option label="Prometheus + Grafana" value="prometheus_grafana" />
                <el-option label="阿里云监控" value="aliyun_monitor" />
                <el-option label="Zabbix" value="zabbix" />
              </el-select>
            </el-form-item>
            <el-form-item label="告警通知渠道">
              <el-checkbox v-model="alertDingtalk">钉钉</el-checkbox>
              <el-checkbox v-model="alertFeishu">飞书</el-checkbox>
              <el-checkbox v-model="alertWecom">企业微信</el-checkbox>
              <el-checkbox v-model="alertEmail">邮件</el-checkbox>
            </el-form-item>
            <el-form-item label="IoT 设备类型">
              <el-checkbox v-model="iotSoil">土壤传感器</el-checkbox>
              <el-checkbox v-model="iotWeather">气象站</el-checkbox>
              <el-checkbox v-model="iotDrone">无人机</el-checkbox>
              <el-checkbox v-model="iotIrrigation">灌溉控制器</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSubmit(false)">保存</el-button>
              <el-button type="success" :loading="saving || aiLoading" @click="handleSubmit(true)">
                <el-icon><MagicStick /></el-icon>&nbsp;✨ 保存并生成运维手册
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="preview-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">🛠️ 运维手册预览</span>
              <el-tag :type="statusTag.type" size="small">{{ statusTag.label }}</el-tag>
            </div>
          </template>
          <div v-if="!current.manualOpsId" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><Tools /></el-icon>
            <p>配置运维方案后点击「生成运维手册」</p>
          </div>
          <div v-else-if="aiLoading" class="loading-state">
            <el-icon class="rotate" :size="36" color="#3b82f6"><Loading /></el-icon>
            <p>AI 正在生成运维手册,预计 10-20 秒……</p>
          </div>
          <div v-else-if="hasManual">
            <el-collapse v-model="activeSections">
              <el-collapse-item title="📊 监控指标" name="metrics">
                <pre class="md-content">{{ current.monitorMetrics }}</pre>
              </el-collapse-item>
              <el-collapse-item title="🚨 告警规则" name="alerts">
                <pre class="md-content">{{ current.alertRules }}</pre>
              </el-collapse-item>
              <el-collapse-item title="💾 备份策略" name="backup">
                <pre class="md-content">{{ current.backupStrategy }}</pre>
              </el-collapse-item>
              <el-collapse-item title="🔍 故障排查" name="trouble">
                <pre class="md-content">{{ current.troubleshootGuide }}</pre>
              </el-collapse-item>
              <el-collapse-item title="🌾 IoT 设备运维" name="iot">
                <pre class="md-content">{{ current.iotMaintenance }}</pre>
              </el-collapse-item>
            </el-collapse>
          </div>
          <div v-else class="ai-not-yet">
            <el-icon :size="40" color="#f59e0b"><InfoFilled /></el-icon>
            <p>配置已保存 ({{ current.manualOpsNo }}),点击生成</p>
            <el-button type="success" :loading="aiLoading" @click="triggerAi">✨ 立即生成</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header><span class="card-title">📚 历史运维手册 ({{ total }})</span></template>
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="manualOpsNo" width="160" />
        <el-table-column label="手册名" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="监控" width="160" align="center">
          <template #default="{ row }">{{ monLabel(row.monitoringSolution) }}</template>
        </el-table-column>
        <el-table-column label="告警渠道" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="ch in (row.alertChannels || '').split(',').filter(Boolean)" :key="ch" size="small" style="margin-right: 4px">
              {{ channelLabel(ch) }}
            </el-tag>
          </template>
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
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Tools, Loading, InfoFilled } from '@element-plus/icons-vue'
import {
  listManualOps, addManualOps, updateManualOps, delManualOps, aiGenerateManualOps, getManualOps, listProjectsForSelect,
  type ManualOps, type ManualOpsQuery
} from '@/api/business/manual-ops'

const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)
const activeSections = ref(['metrics'])

// 多选 checkbox 单独绑定再合成 CSV
const alertDingtalk = ref(true)
const alertFeishu = ref(true)
const alertWecom = ref(false)
const alertEmail = ref(true)
const iotSoil = ref(true)
const iotWeather = ref(true)
const iotDrone = ref(false)
const iotIrrigation = ref(true)

const emptyForm = (): ManualOps => ({
  projectId: 0, title: '', monitoringSolution: 'prometheus_grafana',
  alertChannels: '', iotDeviceTypes: '', authorUserId: 1
})
const form = reactive<ManualOps>(emptyForm())
const current = reactive<ManualOps>({ projectId: 0, title: '' })
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

watch([alertDingtalk, alertFeishu, alertWecom, alertEmail], () => {
  form.alertChannels = [
    alertDingtalk.value && 'dingtalk',
    alertFeishu.value && 'feishu',
    alertWecom.value && 'wecom',
    alertEmail.value && 'email'
  ].filter(Boolean).join(',')
}, { immediate: true })

watch([iotSoil, iotWeather, iotDrone, iotIrrigation], () => {
  form.iotDeviceTypes = [
    iotSoil.value && 'soil_sensor',
    iotWeather.value && 'weather_station',
    iotDrone.value && 'drone',
    iotIrrigation.value && 'irrigation_ctrl'
  ].filter(Boolean).join(',')
}, { immediate: true })

const list = ref<ManualOps[]>([])
const total = ref(0)
const queryParams = reactive<ManualOpsQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

const hasManual = computed(() => !!(current.monitorMetrics || current.alertRules))
const statusMap: Record<string, { label: string; type: any }> = {
  '00': { label: '草稿', type: 'info' }, '01': { label: '生成中', type: 'warning' },
  '02': { label: '已生成', type: 'success' }, '03': { label: '已发布', type: 'primary' }
}
const statusTagFor = (s?: string) => statusMap[s || '00'] || { label: s || '-', type: 'info' as any }
const statusTag = computed(() => statusTagFor(current.status))

const monLabel = (v?: string) => ({ prometheus_grafana: 'Prom+Grafana', aliyun_monitor: '阿里云', zabbix: 'Zabbix' } as Record<string, string>)[v || ''] || v || '-'
const channelLabel = (v?: string) => ({ dingtalk: '钉钉', feishu: '飞书', wecom: '企微', email: '邮件' } as Record<string, string>)[v || ''] || v || '-'

async function getList() {
  listLoading.value = true
  try { const res: any = await listManualOps(queryParams); list.value = res.rows || []; total.value = res.total || 0 } finally { listLoading.value = false }
}
async function loadProjects() {
  try { const res: any = await listProjectsForSelect(); projectOptions.value = res.rows || [] } catch {}
}

async function handleSubmit(triggerAfter: boolean) {
  await formRef.value.validate()
  saving.value = true
  try {
    if (current.manualOpsId) { await updateManualOps({ ...form, manualOpsId: current.manualOpsId }); ElMessage.success('更新成功') }
    else {
      await addManualOps(form); ElMessage.success('保存成功'); await getList()
      const latest = list.value.find(x => x.title === form.title)
      if (latest?.manualOpsId) Object.assign(current, latest)
    }
    if (triggerAfter && current.manualOpsId) await triggerAi()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') } finally { saving.value = false }
}

async function triggerAi() {
  if (!current.manualOpsId) return
  aiLoading.value = true
  try {
    const res: any = await aiGenerateManualOps(current.manualOpsId)
    if (res.code === 200 && res.data) { Object.assign(current, res.data); ElMessage.success('运维手册已生成'); await getList() }
  } catch (e: any) { ElMessage.error(e?.msg || 'AI 失败') } finally { aiLoading.value = false }
}

async function quickAi(row: ManualOps) {
  if (!row.manualOpsId) return
  aiLoading.value = true
  try { await aiGenerateManualOps(row.manualOpsId); ElMessage.success('已生成'); await getList() } finally { aiLoading.value = false }
}

async function loadM(row: ManualOps) {
  if (!row.manualOpsId) return
  const res: any = await getManualOps(row.manualOpsId)
  if (res.code === 200 && res.data) {
    Object.assign(form, res.data); Object.assign(current, res.data)
    const chans = (res.data.alertChannels || '').split(',')
    alertDingtalk.value = chans.includes('dingtalk'); alertFeishu.value = chans.includes('feishu')
    alertWecom.value = chans.includes('wecom'); alertEmail.value = chans.includes('email')
    const iot = (res.data.iotDeviceTypes || '').split(',')
    iotSoil.value = iot.includes('soil_sensor'); iotWeather.value = iot.includes('weather_station')
    iotDrone.value = iot.includes('drone'); iotIrrigation.value = iot.includes('irrigation_ctrl')
    ElMessage.info(`已载入 ${res.data.manualOpsNo}`)
  }
}

async function handleDelete(row: ManualOps) {
  if (!row.manualOpsId) return
  await ElMessageBox.confirm(`删除 "${row.manualOpsNo}"?`, '提示', { type: 'warning' })
  await delManualOps(row.manualOpsId); ElMessage.success('删除成功')
  if (current.manualOpsId === row.manualOpsId) Object.keys(current).forEach(k => delete (current as any)[k])
  await getList()
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.mo-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.preview-card { min-height: 560px; }
.empty-state, .loading-state, .ai-not-yet { text-align: center; padding: 50px 20px; color: #6b7280; }
.empty-state p, .loading-state p, .ai-not-yet p { margin: 12px 0; }
.rotate { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.md-content { font-family: 'Consolas', monospace; font-size: 12px; line-height: 1.7; white-space: pre-wrap; color: #374151; }
</style>
