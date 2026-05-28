<!--
  工作台 — UI §4.2 + 原型 dashboard.html
  AI 助手输入 + 4 KPI + 在办项目 + 我的待办 + 生命周期流程 + 质量快照
-->
<template>
  <div class="app-container dashboard-page">
    <!-- 顶栏:问候 + AI 快速立项 (对齐原型 .ph) -->
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ greeting }},{{ userName }} 👋</h2>
        <p class="page-subtitle">
          今天有 <strong class="hl-todo">{{ todoCount }} 个</strong>待办需处理,
          <strong class="hl-risk">{{ riskCount }} 个</strong>项目存在风险预警
        </p>
      </div>
      <el-button type="success" plain @click="goto('inception')">
        <el-icon><MagicStick /></el-icon>&nbsp;AI 快速立项
      </el-button>
    </div>

    <!-- AgriAI 智能助手 (对齐原型 .aip) -->
    <el-card shadow="never" class="ai-card">
      <div class="ai-header">
        <div class="ai-avatar">🤖</div>
        <div class="ai-title-block">
          <div class="ai-title">AgriAI 智能助手</div>
          <div class="ai-sub">DeepSeek-V3 + Claude · Dify 工作流编排 · AgriKB 知识库</div>
        </div>
        <el-tag type="success" effect="dark" size="small">● 就绪</el-tag>
      </div>
      <div class="ai-input">
        <el-input
          v-model="aiInput"
          type="textarea" :rows="2"
          placeholder="告诉我你想做什么,例如:「帮我生成智慧农业气象预警系统的完整 PRD」「分析竞品禅道的核心功能」「为灌溉系统生成测试用例」"
        />
        <el-button type="primary" @click="handleAiInput" :disabled="!aiInput.trim()">
          发送 →
        </el-button>
      </div>
      <div class="ai-quick-buttons">
        <el-tag
          v-for="q in quickActions" :key="q.to"
          @click="goto(q.to)"
          style="cursor: pointer; user-select: none"
          effect="plain"
        >
          {{ q.icon }} {{ q.label }}
        </el-tag>
      </div>
    </el-card>

    <!-- 4 KPI (对齐原型 .grid4) -->
    <el-row :gutter="12" class="mt-md">
      <el-col :span="6" v-for="k in kpis" :key="k.key">
        <el-card shadow="never" :body-style="{ padding: '14px' }" class="kpi-card">
          <div class="kpi-label">{{ k.label }}</div>
          <div class="kpi-value" :style="{ color: k.color }">{{ k.value }}</div>
          <div class="kpi-sub" :class="k.trend">{{ k.subText }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 在办项目 + 我的待办 -->
    <el-row :gutter="20" class="mt-md">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">🚀 在办项目进度</span>
              <el-button link type="primary" @click="goto('project')">查看全部 →</el-button>
            </div>
          </template>
          <div v-if="loading.projects" v-loading="true" style="min-height: 120px" />
          <el-empty v-else-if="!activeProjects.length" :image-size="80" description="暂无在办项目" />
          <div v-else class="project-list">
            <div v-for="p in activeProjects" :key="p.id" class="project-row" @click="gotoProject(p)">
              <div class="project-info">
                <div class="project-name">{{ p.projectName }}</div>
                <div class="project-meta">
                  <el-tag size="small" :type="statusTagFor(p.status).type">{{ statusTagFor(p.status).label }}</el-tag>
                  <span class="meta-text">{{ p.startDate || '-' }} → {{ p.endDate || '-' }}</span>
                </div>
              </div>
              <el-progress
                :percentage="calcProgress(p)"
                :stroke-width="8"
                :show-text="true"
                style="width: 180px"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">✅ 我的待办 ({{ todoCount }})</span>
              <el-button link type="primary" @click="goto('task')">查看全部 →</el-button>
            </div>
          </template>
          <div v-if="loading.todos" v-loading="true" style="min-height: 120px" />
          <el-empty v-else-if="!myTodos.length" :image-size="80" description="暂无待办" />
          <ul v-else class="todo-list">
            <li v-for="t in myTodos" :key="t.taskId" class="todo-item" @click="goto('task')">
              <el-icon :class="priorityClass(t.priority)"><Operation /></el-icon>
              <div class="todo-info">
                <div class="todo-title">{{ t.title }}</div>
                <div class="todo-meta">
                  <el-tag size="small" :type="priorityTagFor(t.priority).type">{{ priorityTagFor(t.priority).label }}</el-tag>
                  <span class="meta-text">{{ t.taskNo }}</span>
                </div>
              </div>
              <el-tag size="small" :type="taskStatusTagFor(t.status).type">{{ taskStatusTagFor(t.status).label }}</el-tag>
            </li>
          </ul>
        </el-card>
      </el-col>
    </el-row>

    <!-- 项目生命周期流程 + 本迭代质量快照 -->
    <el-row :gutter="20" class="mt-md">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">🔄 项目生命周期流程</span>
          </template>
          <div class="lifecycle">
            <div v-for="(row, ri) in lifecycle" :key="ri" class="lifecycle-row">
              <template v-for="(node, ni) in row" :key="ni">
                <div class="lc-node" :class="node.cls" @click="goto(node.to)">
                  <span>{{ node.icon }}</span>&nbsp;{{ node.label }}
                </div>
                <span v-if="ni < row.length - 1" class="lc-arrow">→</span>
              </template>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">📊 本迭代质量快照</span>
          </template>
          <div v-loading="loading.quality" style="min-height: 200px">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="本月发布次数">
                <strong style="color: #3b82f6">{{ qualitySnapshot.releaseCount }}</strong> 次
              </el-descriptions-item>
              <el-descriptions-item label="自动化覆盖率">
                <el-progress
                  :percentage="qualitySnapshot.autoTestCoverage"
                  :stroke-width="14"
                  :format="(p: number) => `${p.toFixed(0)}%`"
                  :status="qualitySnapshot.autoTestCoverage >= 90 ? 'success' : qualitySnapshot.autoTestCoverage >= 60 ? 'warning' : 'exception'"
                />
              </el-descriptions-item>
              <el-descriptions-item label="最新风险评级">
                <el-tag :type="riskTag.type" effect="dark">{{ riskTag.label }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="变更失败率 (CFR)">
                <span :style="{ color: qualitySnapshot.cfr < 15 ? '#10b981' : '#ef4444' }">
                  {{ qualitySnapshot.cfr.toFixed(1) }}%
                </span>
                <el-tag size="small" :type="qualitySnapshot.cfr < 15 ? 'success' : 'danger'" style="margin-left: 8px">
                  {{ qualitySnapshot.cfr < 15 ? '健康' : '超阈值' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
            <div class="quality-actions">
              <el-button size="small" plain @click="goto('testreport')">查看完整报告 →</el-button>
              <el-button size="small" plain @click="goto('dora')">DORA 指标 →</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick, Operation } from '@element-plus/icons-vue'
import {
  fetchProjects, fetchMyTasks, fetchDefects, fetchPrds, fetchInceptions,
  fetchAutotests, fetchReleases, fetchTestReports, fetchManualProducts
} from '@/api/business/dashboard'
import {
  statusTagFor, priorityTagFor, priorityClass,
  taskStatusTagFor, riskTagFor
} from './dashboardDict'

const router = useRouter()
const aiInput = ref('')

// === 顶栏问候 ===
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})
const userName = '张总'

// === AI 快捷按钮 (对齐原型 ai-qb) ===
const quickActions = [
  { icon: '🚀', label: 'AI 快速立项', to: 'inception' },
  { icon: '🔍', label: '竞品分析',    to: 'competitive' },
  { icon: '📄', label: '生成 PRD',    to: 'prd' },
  { icon: '🧪', label: '生成测试用例', to: 'testcase' },
  { icon: '🏭', label: '生成测试数据', to: 'testdata' },
  { icon: '📖', label: '一键生成手册', to: 'manual-product' }
]

// === 跳转 ===
// menu-regroup-by-phase.sql 后业务菜单按 8 阶段分组, URL 形如 /<phase>/<entity>.
// 映射表统一在 @/utils/businessRoute.ts (SSoT).
import { entityToPath, ENTITY_TO_PATH } from '@/utils/businessRoute'

function goto(entity: string) {
  const path = entityToPath(entity)
  router.push(path).catch(err => {
    console.warn(`[dashboard] 跳转失败 entity=${entity} path=${path}`, err)
    ElMessage.warning(`「${entity}」模块路由未注册`)
  })
}
function gotoProject(p: any) {
  router.push({ path: ENTITY_TO_PATH.project, query: { id: p.id } })
}

// === KPI 数据 ===
const kpiData = reactive({
  activeProjectCount: 0, projectNewThisMonth: 0,
  aiDocCount: 0, defectCount: 0, defectChangePct: 0,
  autoTestCoverage: 0
})

const kpis = computed(() => [
  {
    key: 'project', label: '在办项目',
    value: kpiData.activeProjectCount, color: '#10b981',
    subText: kpiData.projectNewThisMonth > 0 ? `↑ 本月新增 ${kpiData.projectNewThisMonth} 个` : '—',
    trend: 'up'
  },
  {
    key: 'ai_doc', label: 'AI 生成文档',
    value: kpiData.aiDocCount, color: '#8b5cf6',
    subText: `节省约 ${kpiData.aiDocCount * 2} 小时`,
    trend: 'up'
  },
  {
    key: 'defect', label: '当前缺陷',
    value: kpiData.defectCount, color: '#f59e0b',
    subText: kpiData.defectChangePct < 0 ? `↓ 较上迭代 ${kpiData.defectChangePct}%` : '—',
    trend: kpiData.defectChangePct < 0 ? 'up' : 'down'
  },
  {
    key: 'auto', label: '自动化覆盖率',
    value: `${kpiData.autoTestCoverage.toFixed(0)}%`, color: '#3b82f6',
    subText: kpiData.autoTestCoverage >= 90 ? '✓ 达标' : `目标 90% ↑ 提升中`,
    trend: kpiData.autoTestCoverage >= 90 ? 'up' : ''
  }
])

const todoCount = ref(0)
const riskCount = ref(0)

// === 在办项目 ===
const activeProjects = ref<any[]>([])
const myTodos = ref<any[]>([])
const loading = reactive({ projects: false, todos: false, quality: false })

function calcProgress(p: any): number {
  // 简单计算: 按 status 推测进度;真实可基于 task 完成度
  const map: Record<string, number> = { '0': 5, '1': 50, '2': 30, '3': 100, '4': 0 }
  return map[p.status] ?? 50
}


// === 质量快照 ===
const qualitySnapshot = reactive({
  releaseCount: 0,
  autoTestCoverage: 0,
  riskLevel: 'green',
  cfr: 0
})

const riskTag = computed(() => riskTagFor(qualitySnapshot.riskLevel))

// === 生命周期流程图 (对齐原型 fc-wrap) ===
const lifecycle = [
  [
    { icon: '🚀', label: '立项',   to: 'inception',   cls: 'blue' },
    { icon: '🔍', label: '竞品',   to: 'competitive', cls: 'purple' },
    { icon: '📋', label: '需求',   to: 'requirement', cls: 'purple' },
    { icon: '📄', label: 'PRD',    to: 'prd',         cls: 'ai' },
    { icon: '🎨', label: 'UED',    to: 'ued',         cls: 'blue' }
  ],
  [
    { icon: '🏗️', label: '概要设计', to: 'arch',      cls: 'green' },
    { icon: '🗄️', label: '数据库',   to: 'dbdesign',  cls: 'green' },
    { icon: '🔌', label: '接口设计', to: 'apidesign', cls: 'green' },
    { icon: '💻', label: '编码',     to: 'task',      cls: 'green' }
  ],
  [
    { icon: '📐', label: '测试方案', to: 'testplan',   cls: 'amber' },
    { icon: '🧪', label: '用例',     to: 'testcase',   cls: 'amber' },
    { icon: '📤', label: '提测',     to: 'submission', cls: 'amber' },
    { icon: '🤖', label: '自动化',   to: 'autotest',   cls: 'amber' },
    { icon: '📊', label: '报告',     to: 'testreport', cls: 'red' }
  ],
  [
    { icon: '📖', label: '产品手册', to: 'manual-product', cls: 'blue' },
    { icon: '🚀', label: '实施手册', to: 'manual-impl',    cls: 'blue' },
    { icon: '🛠️', label: '运维手册', to: 'manual-ops',     cls: 'blue' }
  ]
]

// === AI 输入处理 ===
function handleAiInput() {
  const text = aiInput.value.trim()
  if (!text) return
  // 简单意图识别
  const intents: Array<[RegExp, string]> = [
    [/立项|inception/i, 'inception'],
    [/PRD|prd|文档/i, 'prd'],
    [/竞品|comp/i, 'competitive'],
    [/测试用例|testcase/i, 'testcase'],
    [/测试数据|testdata/i, 'testdata'],
    [/手册|manual|productmanual/i, 'manual-product'],
    [/发布|release/i, 'release'],
    [/提测|submit/i, 'submission'],
    [/报告|testreport/i, 'testreport']
  ]
  for (const [re, to] of intents) {
    if (re.test(text)) {
      ElMessage.success(`已识别意图: 跳转到 ${to}`)
      goto(to)
      return
    }
  }
  ElMessage.info('意图未识别,请使用下方快捷按钮')
}

// === 数据加载 ===
async function loadAll() {
  loading.projects = true
  loading.todos = true
  loading.quality = true

  const tasks = [
    fetchProjects(),
    fetchMyTasks(),
    fetchDefects(),
    fetchPrds(),
    fetchInceptions(),
    fetchManualProducts(),
    fetchAutotests(),
    fetchReleases(),
    fetchTestReports()
  ]

  const [projRes, taskRes, defRes, prdRes, incRes, mpRes, autoRes, relRes, trRes]: any[] =
    await Promise.allSettled(tasks).then(r => r.map(p => p.status === 'fulfilled' ? p.value : { rows: [], total: 0 }))

  // 在办项目 (status=1 或 status=2)
  const allProjects = projRes.rows || []
  activeProjects.value = allProjects.filter((p: any) => p.status === '1' || p.status === '2').slice(0, 5)
  kpiData.activeProjectCount = allProjects.filter((p: any) => p.status === '1' || p.status === '2').length
  // 本月新增 (简单估算)
  const thisMonth = new Date().toISOString().slice(0, 7)
  kpiData.projectNewThisMonth = allProjects.filter((p: any) => (p.createTime || '').startsWith(thisMonth)).length

  // 我的待办 (status<>已完成)
  const myTasksAll = taskRes.rows || []
  myTodos.value = myTasksAll.filter((t: any) => t.status !== '04').slice(0, 6)
  todoCount.value = myTasksAll.filter((t: any) => t.status !== '04').length

  // 风险预警 (defect P0/P1 + risk testreport.riskLevel=red)
  riskCount.value = (defRes.rows || []).filter((d: any) => d.priority === '00' || d.priority === '01').length

  // AI 文档数 (PRD + Inception + ManualProduct 已生成)
  kpiData.aiDocCount = (prdRes.total || 0) + (incRes.total || 0) + (mpRes.total || 0)

  // 当前缺陷
  kpiData.defectCount = defRes.total || 0
  kpiData.defectChangePct = -34  // mock,真实从上迭代统计算

  // 自动化覆盖率 (按 autotest 套件的 pass_rate 平均)
  const autoSuites = autoRes.rows || []
  if (autoSuites.length) {
    const passRates = autoSuites.filter((a: any) => a.passRate != null).map((a: any) => Number(a.passRate))
    kpiData.autoTestCoverage = passRates.length
      ? passRates.reduce((s: number, r: number) => s + r, 0) / passRates.length
      : 0
  }

  // 质量快照
  const releases = relRes.rows || []
  qualitySnapshot.releaseCount = releases.filter((r: any) => r.status === '02').length
  qualitySnapshot.autoTestCoverage = kpiData.autoTestCoverage
  qualitySnapshot.cfr = releases.length
    ? releases.filter((r: any) => r.status === '03').length / releases.length * 100
    : 0
  const latestReport = (trRes.rows || [])[0]
  qualitySnapshot.riskLevel = latestReport?.riskLevel || 'green'

  loading.projects = false
  loading.todos = false
  loading.quality = false
}

onMounted(loadAll)
</script>

<style scoped>
.dashboard-page { padding: 20px; }
.page-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 16px;
}
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.hl-todo { color: #3b82f6; }
.hl-risk { color: #f59e0b; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.mt-md { margin-top: 16px; }

/* AI 助手 */
.ai-card {
  background: linear-gradient(135deg, #f0f9ff 0%, #faf5ff 100%);
  border: 1px solid #e0e7ff;
}
.ai-header {
  display: flex; align-items: center; gap: 12px;
  padding-bottom: 12px; border-bottom: 1px solid #e5e7eb;
}
.ai-avatar {
  width: 40px; height: 40px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  font-size: 22px;
}
.ai-title-block { flex: 1; }
.ai-title { font-weight: 600; font-size: 15px; }
.ai-sub { font-size: 11px; color: #6b7280; margin-top: 2px; }

.ai-input {
  display: flex; gap: 8px; margin: 12px 0;
}
.ai-input :deep(.el-textarea__inner) { font-family: inherit; }
.ai-quick-buttons {
  display: flex; gap: 8px; flex-wrap: wrap;
}
.ai-quick-buttons .el-tag { font-size: 12px; padding: 6px 10px; }

/* KPI */
.kpi-card {
  border-left: 3px solid #3b82f6;
  transition: transform 0.15s;
}
.kpi-card:hover { transform: translateY(-2px); }
.kpi-label { font-size: 12px; color: #6b7280; }
.kpi-value { font-size: 28px; font-weight: 700; margin: 6px 0; }
.kpi-sub   { font-size: 11px; }
.kpi-sub.up { color: #10b981; }
.kpi-sub.down { color: #ef4444; }

/* 项目列表 */
.project-list { display: flex; flex-direction: column; gap: 12px; }
.project-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 12px; border-radius: 6px;
  background: #f9fafb; cursor: pointer; transition: background 0.15s;
}
.project-row:hover { background: #eff6ff; }
.project-info { flex: 1; min-width: 0; margin-right: 12px; }
.project-name { font-weight: 500; font-size: 13px; }
.project-meta { display: flex; gap: 8px; margin-top: 4px; align-items: center; }
.meta-text { color: #9ca3af; font-size: 11px; }

/* 待办列表 */
.todo-list { list-style: none; padding: 0; margin: 0; }
.todo-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 12px; border-radius: 6px; cursor: pointer;
  transition: background 0.15s;
}
.todo-item:hover { background: #f9fafb; }
.todo-item + .todo-item { margin-top: 4px; }
.todo-info { flex: 1; min-width: 0; }
.todo-title { font-size: 13px; font-weight: 500; }
.todo-meta { display: flex; gap: 6px; align-items: center; margin-top: 2px; }
.p0 { color: #ef4444; }
.p1 { color: #f59e0b; }
.p2 { color: #6b7280; }

/* 生命周期流程 */
.lifecycle { display: flex; flex-direction: column; gap: 10px; }
.lifecycle-row {
  display: flex; align-items: center; flex-wrap: wrap; gap: 6px;
}
.lc-node {
  padding: 6px 12px; border-radius: 6px; font-size: 12px;
  cursor: pointer; user-select: none; transition: transform 0.15s;
  display: inline-flex; align-items: center; white-space: nowrap;
}
.lc-node:hover { transform: scale(1.05); }
.lc-node.blue   { background: #dbeafe; color: #1e40af; }
.lc-node.purple { background: #ede9fe; color: #6b21a8; }
.lc-node.green  { background: #d1fae5; color: #065f46; }
.lc-node.amber  { background: #fef3c7; color: #92400e; }
.lc-node.red    { background: #fee2e2; color: #991b1b; }
.lc-node.ai     { background: linear-gradient(135deg, #c7d2fe, #fbcfe8); color: #6b21a8; }
.lc-arrow { color: #9ca3af; font-size: 14px; }

/* 质量快照 */
.quality-actions { margin-top: 12px; display: flex; gap: 8px; }
</style>
