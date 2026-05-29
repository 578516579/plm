<!--
  测试数据工厂 — PRD §F4.3 + 原型 testdata.html
  严格对齐: 左卡数据生成配置 (5 农业表 + 3 格式 + 4 规则开关) + 右卡数据预览
-->
<template>
  <div class="app-container td-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🏭 测试数据工厂</h2>
        <p class="page-subtitle">基于字段语义 + AgriKB,智能生成农业场景真实感测试数据</p>
      </div>
      <el-button plain @click="openAdd"><el-icon><Plus /></el-icon>&nbsp;新增数据集</el-button>
    </div>

    <el-row :gutter="20">
      <!-- 左:数据生成配置 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">⚙️ 数据生成配置</span></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-form-item label="关联项目" prop="projectId" required>
              <el-select v-model="form.projectId" style="width: 100%">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="数据集名称" prop="title" required>
              <el-input v-model="form.title" placeholder="如:智慧灌溉土壤数据集" />
            </el-form-item>
            <el-form-item label="选择数据表" prop="targetTable" required>
              <el-select v-model="form.targetTable" style="width: 100%">
                <el-option label="土壤传感器数据 (t_soil_sensor_data)" value="soil_sensor" />
                <el-option label="气象记录 (t_weather_record)" value="weather" />
                <el-option label="作物信息 (t_crop_info)" value="crop" />
                <el-option label="病虫害记录 (t_pest_record)" value="pest" />
                <el-option label="灌溉计划 (t_irrigation_plan)" value="irrigation" />
              </el-select>
            </el-form-item>

            <el-card class="ai-semantics-card" shadow="never">
              <div class="semantics-title">🤖 AI 识别字段语义</div>
              <div class="semantics-content">{{ semanticsPreview }}</div>
            </el-card>

            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="生成数量" prop="generateCount">
                  <el-input-number v-model="form.generateCount" :min="1" :max="10000" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="输出格式" prop="outputFormat">
                  <el-select v-model="form.outputFormat" style="width: 100%">
                    <el-option label="JSON" value="json" />
                    <el-option label="SQL INSERT" value="sql" />
                    <el-option label="CSV" value="csv" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="数据规则约束">
              <el-checkbox
                :model-value="form.ruleChinaCoord === 'Y'"
                @change="form.ruleChinaCoord = ($event ? 'Y' : 'N')"
              >坐标限定中国农田范围内</el-checkbox>
              <el-checkbox
                :model-value="form.ruleTimeContinuity === 'Y'"
                @change="form.ruleTimeContinuity = ($event ? 'Y' : 'N')"
              >时间序列满足业务连续性</el-checkbox>
              <el-checkbox
                :model-value="form.ruleSensorRange === 'Y'"
                @change="form.ruleSensorRange = ($event ? 'Y' : 'N')"
              >数值符合农业传感器正常范围</el-checkbox>
              <el-checkbox
                :model-value="form.ruleIncludeOutliers === 'Y'"
                @change="form.ruleIncludeOutliers = ($event ? 'Y' : 'N')"
              >包含异常值 (边界测试)</el-checkbox>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSubmit(false)">
                <el-icon><DocumentAdd /></el-icon>&nbsp;保存配置
              </el-button>
              <AiButton :loading="aiLoading" :saving="saving" @click="handleSubmit(true)">
                🏭 生成测试数据
              </AiButton>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右:数据预览 -->
      <el-col :span="12">
        <el-card shadow="never" class="preview-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">👁️ 数据预览</span>
              <el-tag :type="previewBadge.type" size="small">{{ previewBadge.label }}</el-tag>
            </div>
          </template>

          <div v-if="!current.testdataId" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><Box /></el-icon>
            <p>配置后点击「生成测试数据」</p>
          </div>

          <div v-else-if="aiLoading" class="loading-state">
            <el-icon class="rotate" :size="36" color="#3b82f6"><Loading /></el-icon>
            <p>AI 正在生成 {{ form.generateCount }} 条数据,预计 5-15 秒……</p>
          </div>

          <div v-else-if="current.generatedContent">
            <pre class="data-preview">{{ truncatedContent }}</pre>
            <div v-if="(current.generatedContent.length || 0) > 1500" class="more-tip">
              ... 共 {{ current.generateCount }} 条记录 (仅展示前 1500 字符)
            </div>
            <el-button-group style="margin-top: 12px">
              <el-button size="small" @click="copyContent">
                <el-icon><CopyDocument /></el-icon>&nbsp;复制
              </el-button>
              <el-button size="small" @click="downloadContent">
                <el-icon><Download /></el-icon>&nbsp;下载
              </el-button>
            </el-button-group>
          </div>

          <div v-else class="ai-not-yet">
            <el-icon :size="40" color="#f59e0b"><InfoFilled /></el-icon>
            <p>配置已保存 ({{ current.testdataNo }}),点击生成</p>
            <AiButton :loading="aiLoading" @click="triggerAi">
              立即生成
            </AiButton>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 历史数据集 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span class="card-title">📚 历史数据集 ({{ total }})</span>
      </template>
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="testdataNo" width="160" />
        <el-table-column label="数据集名称" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="目标表" prop="targetTable" width="130">
          <template #default="{ row }">{{ tableLabel(row.targetTable) }}</template>
        </el-table-column>
        <el-table-column label="格式" prop="outputFormat" width="80" align="center" />
        <el-table-column label="数量" prop="generateCount" width="80" align="center" />
        <el-table-column label="AI" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.aiGenerated === 'Y'" type="success" size="small">已生成</el-tag>
            <el-tag v-else type="info" size="small">未生成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadTd(row)">载入</el-button>
            <AiButton link @click="quickAi(row)">生成</AiButton>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增数据集" width="540px">
      <p class="muted">直接使用左侧的「数据生成配置」即可创建,本对话框用于快速命名后续填配置。</p>
      <el-form :model="form">
        <el-form-item label="数据集名称">
          <el-input v-model="form.title" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick, DocumentAdd, Box, Loading, InfoFilled, CopyDocument, Download } from '@element-plus/icons-vue'
import {
  listTestData, addTestData, updateTestData, delTestData, aiGenerateTestData, getTestData, listProjectsForSelect,
  type TestData, type TestDataQuery
} from '@/api/business/testdata'

const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)

const emptyForm = (): TestData => ({
  projectId: 0, title: '', targetTable: 'soil_sensor', outputFormat: 'json',
  generateCount: 100,
  ruleChinaCoord: 'Y', ruleTimeContinuity: 'Y', ruleSensorRange: 'Y', ruleIncludeOutliers: 'N',
  authorUserId: 1
})
const form = reactive<TestData>(emptyForm())
const current = reactive<TestData>({ projectId: 0, title: '' })
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  targetTable: [{ required: true, message: '请选择数据表', trigger: 'change' }]
}

const list = ref<TestData[]>([])
const total = ref(0)
const queryParams = reactive<TestDataQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

const tableSemantics: Record<string, string> = {
  soil_sensor: 'sensor_id 设备唯一ID · field_code 地块编码 · moisture 含水率% · temperature 温度°C · ph 酸碱度 · recorded_at 采集时间',
  weather: 'station_id 气象站 · temperature 气温 · humidity 湿度 · rainfall_mm 降雨量 · wind_speed_ms 风速 · recorded_at',
  crop: 'crop_code 作物编码 · crop_name 作物名 · variety 品种 · sowing_date · harvest_date · growth_stage 生长期',
  pest: 'crop_code 作物 · pest_type 病虫害类型 · severity 轻/中/重 · area_mu 受灾亩 · reported_at',
  irrigation: 'field_code 地块 · plan_date 计划日期 · water_m3 灌溉量 · method 滴灌/喷灌 · executed Y/N'
}

const semanticsPreview = computed(() => tableSemantics[form.targetTable || ''] || '(选择数据表后展示字段语义)')

const tableLabel = (v?: string) =>
  ({
    soil_sensor: '土壤传感器', weather: '气象', crop: '作物', pest: '病虫害', irrigation: '灌溉'
  } as Record<string, string>)[v || ''] || v || '-'

const previewBadge = computed(() => {
  if (!current.testdataId) return { label: '等待生成', type: 'info' as any }
  if (current.generatedContent) return { label: '已生成', type: 'success' as any }
  return { label: '待生成', type: 'warning' as any }
})

const truncatedContent = computed(() => (current.generatedContent || '').slice(0, 1500))

async function getList() {
  listLoading.value = true
  try { const res: any = await listTestData(queryParams); list.value = res.rows || []; total.value = res.total || 0 }
  finally { listLoading.value = false }
}

async function loadProjects() {
  try { const res: any = await listProjectsForSelect(); projectOptions.value = res.rows || [] } catch { /* */ }
}

function openAdd() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function handleSubmit(triggerAfter: boolean) {
  await formRef.value.validate()
  saving.value = true
  try {
    if (current.testdataId) { await updateTestData({ ...form, testdataId: current.testdataId }); ElMessage.success('更新成功') }
    else {
      await addTestData(form); ElMessage.success('保存成功'); await getList()
      const latest = list.value.find(x => x.title === form.title)
      if (latest?.testdataId) Object.assign(current, latest)
    }
    if (triggerAfter && current.testdataId) await triggerAi()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') }
  finally { saving.value = false }
}

async function triggerAi() {
  if (!current.testdataId) { ElMessage.warning('请先保存'); return }
  aiLoading.value = true
  try {
    const res: any = await aiGenerateTestData(current.testdataId)
    if (res.code === 200 && res.data) { Object.assign(current, res.data); ElMessage.success('数据已生成'); await getList() }
  } catch (e: any) { ElMessage.error(e?.msg || 'AI 失败') }
  finally { aiLoading.value = false }
}

async function quickAi(row: TestData) {
  if (!row.testdataId) return
  aiLoading.value = true
  try { await aiGenerateTestData(row.testdataId); ElMessage.success(`${row.testdataNo} 已生成`); await getList() }
  finally { aiLoading.value = false }
}

async function loadTd(row: TestData) {
  if (!row.testdataId) return
  const res: any = await getTestData(row.testdataId)
  if (res.code === 200 && res.data) {
    Object.assign(form, res.data)
    Object.assign(current, res.data)
    ElMessage.info(`已载入 ${res.data.testdataNo}`)
  }
}

async function handleDelete(row: TestData) {
  if (!row.testdataId) return
  await ElMessageBox.confirm(`确认删除 "${row.testdataNo}"?`, '提示', { type: 'warning' })
  await delTestData(row.testdataId); ElMessage.success('删除成功')
  if (current.testdataId === row.testdataId) Object.keys(current).forEach(k => delete (current as any)[k])
  await getList()
}

function copyContent() {
  navigator.clipboard.writeText(current.generatedContent || '').then(() => ElMessage.success('已复制'))
}

function downloadContent() {
  const ext = current.outputFormat === 'sql' ? 'sql' : current.outputFormat === 'csv' ? 'csv' : 'json'
  const blob = new Blob([current.generatedContent || ''], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = `${current.testdataNo}.${ext}`
  a.click(); URL.revokeObjectURL(url)
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.td-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.muted { color: #6b7280; font-size: 13px; }
.ai-semantics-card {
  background: #f9fafb; margin-bottom: 14px;
}
:deep(.ai-semantics-card .el-card__body) { padding: 12px 14px; }
.semantics-title { font-size: 12px; color: #6b7280; margin-bottom: 4px; }
.semantics-content { font-size: 12px; color: #374151; line-height: 1.6; }
.preview-card { min-height: 540px; }
.empty-state, .loading-state, .ai-not-yet { text-align: center; padding: 50px 20px; color: #6b7280; }
.empty-state p, .loading-state p, .ai-not-yet p { margin: 12px 0; }
.rotate { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.data-preview {
  background: #1e1e2e; color: #a3e635; padding: 14px 16px; border-radius: 8px;
  font-family: 'Consolas', monospace; font-size: 11.5px; line-height: 1.55;
  overflow-x: auto; white-space: pre-wrap; max-height: 400px; overflow-y: auto;
}
.more-tip { font-size: 11.5px; color: #9ca3af; margin-top: 6px; text-align: center; }
</style>
