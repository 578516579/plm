<!--
  自动化测试 — PRD §F4.5 + 原型 autotest.html
  严格对齐: 顶部 4 统计卡 + 测试套件管理表 + 最新执行结果
-->
<template>
  <div class="app-container at-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🤖 自动化测试</h2>
        <p class="page-subtitle">AI 生成测试脚本,定时执行,智能根因分析</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="openAdd"><el-icon><Plus /></el-icon>&nbsp;新增套件</el-button>
        <el-button type="success" :loading="aiLoading" :disabled="!current.autotestId" @click="triggerAi">
          <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成脚本
        </el-button>
        <el-button type="primary" :loading="runLoading" :disabled="!current.autotestId" @click="runNow">
          <el-icon><VideoPlay /></el-icon>&nbsp;▶ 立即执行
        </el-button>
      </div>
    </div>

    <!-- 4 统计卡 (对齐原型 .grid4) -->
    <el-row :gutter="14" class="stat-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">测试套件</div>
          <div class="stat-value g">{{ total }}</div>
          <div class="stat-detail">含 AI 生成 {{ aiCount }} 套</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">最新执行通过率</div>
          <div class="stat-value success">{{ avgPassRate }}%</div>
          <div class="stat-detail up">↑ 较上次 +3%</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">执行耗时</div>
          <div class="stat-value blue">~ 4m32s</div>
          <div class="stat-detail">上次执行</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">失败用例</div>
          <div class="stat-value red">{{ totalFailed }}</div>
          <div class="stat-detail dn">需修复</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 14px">
      <!-- 左:套件管理 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">🤖 测试套件管理</span>
              <el-input v-model="queryParams.title" placeholder="搜索套件名" style="width: 200px" clearable @clear="getList" @keyup.enter="getList" />
            </div>
          </template>
          <el-table v-loading="listLoading" :data="list" stripe highlight-current-row @current-change="onSelect">
            <el-table-column label="编号" prop="autotestNo" width="140" />
            <el-table-column label="套件名" prop="title" min-width="160" show-overflow-tooltip />
            <el-table-column label="类型" width="90" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="suiteTypeTag(row.testSuiteType)">{{ suiteTypeLabel(row.testSuiteType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="框架" prop="framework" width="100" align="center">
              <template #default="{ row }">{{ frameworkLabel(row.framework) }}</template>
            </el-table-column>
            <el-table-column label="通过率" prop="passRate" width="100" align="center">
              <template #default="{ row }">
                <el-progress
                  :percentage="Number(row.passRate || 0)"
                  :stroke-width="8"
                  :status="(row.passRate || 0) >= 90 ? 'success' : ((row.passRate || 0) >= 70 ? 'warning' : 'exception')"
                />
              </template>
            </el-table-column>
            <el-table-column label="上次" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="resultTag(resultOf(row))">{{ resultLabel(resultOf(row)) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="loadAt(row)">详情</el-button>
                <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
        </el-card>
      </el-col>

      <!-- 右:最新执行结果 -->
      <el-col :span="10">
        <el-card shadow="never" class="result-card">
          <template #header><span class="card-title">📊 最新执行结果</span></template>

          <div v-if="!current.autotestId" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><DataAnalysis /></el-icon>
            <p>选择左侧套件查看执行结果</p>
          </div>

          <div v-else>
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item label="套件编号"><code>{{ current.autotestNo }}</code></el-descriptions-item>
              <el-descriptions-item label="框架">{{ frameworkLabel(current.framework) }}</el-descriptions-item>
              <el-descriptions-item label="调度 Cron"><code>{{ current.scheduleCron || '(手动)' }}</code></el-descriptions-item>
              <el-descriptions-item label="最近执行">{{ current.lastExecutedAt || '(未执行)' }}</el-descriptions-item>
              <el-descriptions-item label="结果">
                <el-tag size="small" :type="resultTag(resultOf(current))">{{ resultLabel(resultOf(current)) }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <el-row :gutter="10" style="margin-top: 14px">
              <el-col :span="8">
                <el-statistic :value="current.totalCases || 0" title="总用例" />
              </el-col>
              <el-col :span="8">
                <el-statistic :value="current.passedCases || 0" title="通过" :value-style="{ color: '#10b981' }" />
              </el-col>
              <el-col :span="8">
                <el-statistic :value="current.failedCases || 0" title="失败" :value-style="{ color: '#ef4444' }" />
              </el-col>
            </el-row>

            <div v-if="current.lastRootCauseAnalysis" class="rca-block">
              <el-divider content-position="left">
                <span style="color:#ef4444">🤖 AI 根因分析</span>
              </el-divider>
              <pre class="rca-text">{{ current.lastRootCauseAnalysis }}</pre>
            </div>

            <el-divider content-position="left">📜 脚本片段</el-divider>
            <pre class="script-code">{{ truncatedScript }}</pre>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑套件 Dialog -->
    <el-dialog v-model="dialogVisible" :title="form.autotestId ? '编辑套件' : '新增套件'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="关联项目" prop="projectId" required>
          <el-select v-model="form.projectId" style="width: 100%">
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="套件名" prop="title" required>
          <el-input v-model="form.title" placeholder="如:灌溉决策 E2E 套件" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="类型" prop="testSuiteType">
              <el-select v-model="form.testSuiteType" style="width: 100%">
                <el-option label="UI" value="ui" />
                <el-option label="API 接口" value="api" />
                <el-option label="性能压测" value="perf" />
                <el-option label="回归测试" value="regression" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="框架" prop="framework">
              <el-select v-model="form.framework" style="width: 100%">
                <el-option label="Playwright" value="playwright" />
                <el-option label="Selenium" value="selenium" />
                <el-option label="JMeter" value="jmeter" />
                <el-option label="Cypress" value="cypress" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="目标 URL" prop="targetUrl">
          <el-input v-model="form.targetUrl" placeholder="http://localhost" />
        </el-form-item>
        <el-form-item label="启用调度" prop="scheduleEnabled">
          <el-switch v-model="form.scheduleEnabled" active-value="Y" inactive-value="N" />
        </el-form-item>
        <el-form-item label="调度 Cron" prop="scheduleCron">
          <el-input v-model="form.scheduleCron" placeholder="0 2 * * *  (每日凌晨 2 点)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">{{ form.autotestId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick, VideoPlay, DataAnalysis } from '@element-plus/icons-vue'
import {
  listAutoTest, addAutoTest, updateAutoTest, delAutoTest, aiGenerateAutoTest, runAutoTest, getAutoTest, listProjectsForSelect,
  type AutoTest, type AutoTestQuery
} from '@/api/business/autotest'

const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const runLoading = ref(false)
const listLoading = ref(false)

const emptyForm = (): AutoTest => ({
  projectId: 0, title: '', testSuiteType: 'ui', framework: 'playwright',
  scheduleCron: '0 2 * * *', targetUrl: '', scheduleEnabled: 'N', authorUserId: 1
})
const form = reactive<AutoTest>(emptyForm())
const current = reactive<AutoTest>({ projectId: 0, title: '' })
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入套件名', trigger: 'blur' }]
}

const list = ref<AutoTest[]>([])
const total = ref(0)
const queryParams = reactive<AutoTestQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

const aiCount = computed(() => list.value.filter(x => x.aiGenerated === 'Y').length)
const avgPassRate = computed(() => {
  const ratios = list.value.map(x => Number(x.passRate || 0)).filter(x => x > 0)
  if (!ratios.length) return 0
  return Math.round(ratios.reduce((s, n) => s + n, 0) / ratios.length)
})
const totalFailed = computed(() => list.value.reduce((s, x) => s + Number(x.failedCases || 0), 0))

const statusMap: Record<string, { label: string; type: any }> = {
  '00': { label: '草稿', type: 'info' }, '01': { label: '已激活', type: 'success' }, '02': { label: '已禁用', type: 'danger' }
}
const statusTagFor = (s?: string) => statusMap[s || '00'] || { label: s || '-', type: 'info' as any }

const suiteTypeLabel = (v?: string) =>
  ({ ui: 'UI', api: 'API', perf: '性能', regression: '回归' } as Record<string, string>)[v || ''] || v || '-'

const suiteTypeTag = (v?: string): any =>
  ({ ui: 'success', api: 'primary', perf: 'warning', regression: 'info' } as Record<string, string>)[v || ''] || 'info'

const frameworkLabel = (v?: string) =>
  ({ playwright: 'Playwright', selenium: 'Selenium', jmeter: 'JMeter', cypress: 'Cypress' } as Record<string, string>)[v || ''] || v || '-'

const resultLabel = (v?: string) =>
  ({ passed: '通过', failed: '失败', skipped: '跳过', partial: '部分通过', never: '未执行' } as Record<string, string>)[v || ''] || v || '-'

const resultTag = (v?: string): any =>
  ({ passed: 'success', failed: 'danger', skipped: 'info', partial: 'warning', never: 'info' } as Record<string, string>)[v || ''] || 'info'

const resultOf = (row: AutoTest) => {
  if (!row.lastExecutedAt) return 'never'
  if ((row.failedCases || 0) > 0) return 'failed'
  if ((row.passRate || 0) >= 99) return 'passed'
  return 'partial'
}

const truncatedScript = computed(() => (current.scriptContent || '(待 AI 生成)').slice(0, 600))

async function getList() {
  listLoading.value = true
  try { const res: any = await listAutoTest(queryParams); list.value = res.rows || []; total.value = res.total || 0 }
  finally { listLoading.value = false }
}

async function loadProjects() {
  try { const res: any = await listProjectsForSelect(); projectOptions.value = res.rows || [] } catch { /* */ }
}

function onSelect(row: AutoTest | null) {
  // 仅在用户选中某行时同步详情。不处理 row=null:
  // getList() 刷新会替换 :data 数组,el-table 按对象引用丢失 current-row 并触发 current-change(null);
  // 若此时清空 current,会导致「AI 生成脚本 / 立即执行」回写 current 后详情面板塌缩(script-code / 执行统计消失)。
  // 保留当前选中即可,初始空态由 current 初始为空保证。
  if (row) Object.assign(current, row)
}

function openAdd() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function loadAt(row: AutoTest) {
  if (!row.autotestId) return
  const res: any = await getAutoTest(row.autotestId)
  if (res.code === 200 && res.data) {
    Object.assign(form, res.data); Object.assign(current, res.data)
    dialogVisible.value = true
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.autotestId) { await updateAutoTest(form); ElMessage.success('更新成功') }
    else { await addAutoTest(form); ElMessage.success('创建成功') }
    dialogVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') }
  finally { saving.value = false }
}

async function triggerAi() {
  if (!current.autotestId) return
  aiLoading.value = true
  try {
    const res: any = await aiGenerateAutoTest(current.autotestId)
    if (res.code === 200 && res.data) { Object.assign(current, res.data); ElMessage.success('AI 脚本已生成'); await getList() }
  } catch (e: any) { ElMessage.error(e?.msg || 'AI 失败') }
  finally { aiLoading.value = false }
}

async function runNow() {
  if (!current.autotestId) return
  if (current.status !== '01') {
    ElMessage.warning('仅「已激活」套件可立即执行,请先把套件状态切到 01')
    return
  }
  runLoading.value = true
  try {
    const res: any = await runAutoTest(current.autotestId)
    if (res.code === 200 && res.data) {
      Object.assign(current, res.data)
      const rate = Number(res.data.passRate || 0)
      const failed = Number(res.data.failedCases || 0)
      if (failed > 0) ElMessage.warning(`执行完成: 通过率 ${rate}%,失败 ${failed} 例,见下方 AI 根因分析`)
      else ElMessage.success(`执行完成: 全通过,通过率 ${rate}%`)
      await getList()
    }
  } catch (e: any) { ElMessage.error(e?.msg || '执行失败') }
  finally { runLoading.value = false }
}

async function handleDelete(row: AutoTest) {
  if (!row.autotestId) return
  await ElMessageBox.confirm(`确认删除套件 "${row.title}"?`, '提示', { type: 'warning' })
  await delAutoTest(row.autotestId); ElMessage.success('删除成功')
  if (current.autotestId === row.autotestId) Object.keys(current).forEach(k => delete (current as any)[k])
  await getList()
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.at-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.stat-row :deep(.el-card__body) { padding: 14px 16px; }
.stat-card { text-align: left; }
.stat-label { color: #6b7280; font-size: 12px; margin-bottom: 5px; }
.stat-value { font-size: 26px; font-weight: 700; margin-bottom: 3px; }
.stat-value.g { color: #2d7a4f; }
.stat-value.success { color: #10b981; }
.stat-value.blue { color: #3b82f6; }
.stat-value.red { color: #ef4444; }
.stat-detail { font-size: 12px; color: #6b7280; }
.stat-detail.up { color: #10b981; }
.stat-detail.dn { color: #ef4444; }
.result-card { min-height: 540px; }
.empty-state { text-align: center; padding: 60px 20px; color: #6b7280; }
.empty-state p { margin: 12px 0; }
code { background: #f4f4f5; padding: 2px 6px; border-radius: 3px; font-family: monospace; font-size: 12px; }
.script-code {
  background: #1e1e2e; color: #cdd6f4; padding: 12px 14px; border-radius: 6px;
  font-family: 'Consolas', monospace; font-size: 11.5px; line-height: 1.6;
  overflow-x: auto; white-space: pre-wrap; max-height: 240px; overflow-y: auto;
}
.rca-block { margin-top: 8px; }
.rca-text {
  background: #fef2f2; color: #7f1d1d; padding: 10px 12px; border-radius: 6px;
  border-left: 3px solid #ef4444;
  font-family: 'Consolas', monospace; font-size: 12px; line-height: 1.65;
  white-space: pre-wrap; max-height: 320px; overflow-y: auto; margin: 0;
}
</style>
