<template>
  <div class="dash-container" v-loading="loading">
    <!-- 欢迎区 -->
    <div class="dash-hero">
      <div class="hero-left">
        <div class="hero-title">早上好，{{ userName }} 👋</div>
        <div class="hero-sub">
          今天有 <strong class="hero-strong">{{ aggregate.myTodos?.length || 0 }} 个</strong> 待办需处理，
          <strong class="hero-warning">{{ riskCount }} 个</strong>项目存在风险预警
        </div>
      </div>
      <el-button type="primary" :icon="Star" @click="$router.push('/business/inception')">
        ✨ AI 快速立项
      </el-button>
    </div>

    <!-- AI 助手 -->
    <el-card class="dash-ai" shadow="never">
      <div class="ai-head">
        <div class="ai-avatar">🤖</div>
        <div class="ai-meta">
          <div class="ai-title">AgriAI 智能助手</div>
          <div class="ai-sub">DeepSeek-V3 + Claude · Dify 工作流编排 · AgriKB 知识库</div>
        </div>
        <el-tag type="success" effect="light" round style="margin-left: auto">● 就绪</el-tag>
      </div>
      <div class="ai-body">
        <el-input
          v-model="aiInput"
          type="textarea"
          :rows="2"
          placeholder="告诉我你想做什么,例如：「帮我生成智慧农业气象预警系统的完整 PRD」「分析竞品禅道的核心功能」「为灌溉系统生成测试用例」"
        />
        <el-button type="primary" @click="onSendAi">发送 →</el-button>
      </div>
      <div class="ai-quick">
        <el-tag
          v-for="q in aiQuickActions"
          :key="q.label"
          class="ai-q"
          effect="plain"
          @click="$router.push(q.to)"
        >
          {{ q.icon }} {{ q.label }}
        </el-tag>
      </div>
    </el-card>

    <!-- 4 大统计卡 -->
    <el-row :gutter="14" class="stats-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">在办项目</div>
          <div class="stat-value stat-green">{{ aggregate.stats?.activeProjects ?? '-' }}</div>
          <div class="stat-delta stat-up">↑ 本月新增 2 个</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">AI 生成文档</div>
          <div class="stat-value stat-purple">{{ aggregate.stats?.aiDocsGenerated ?? '-' }}</div>
          <div class="stat-delta stat-up">节省约 {{ aggregate.aiMetrics?.hoursSaved ?? 0 }} 小时</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">当前缺陷</div>
          <div class="stat-value stat-amber">{{ aggregate.stats?.currentDefects ?? '-' }}</div>
          <div class="stat-delta stat-up">↓ 较上迭代 -34%</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">自动化覆盖率</div>
          <div class="stat-value stat-blue">{{ aggregate.stats?.autoTestCoverage ?? '-' }}%</div>
          <div class="stat-delta">目标 90% ↑ 提升中</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 在办项目 + 我的待办 -->
    <el-row :gutter="14" class="dash-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-title">🚀 在办项目进度</div>
          </template>
          <div v-for="proj in aggregate.activeProjects || []" :key="proj.name" class="proj-row">
            <div class="proj-name">{{ proj.name }}</div>
            <el-progress :percentage="proj.progress" :status="mapProgressStatus(proj.color)" :stroke-width="8" />
          </div>
          <el-button type="primary" plain size="small" @click="$router.push('/business/project')">
            查看全部项目 →
          </el-button>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-title">✅ 我的待办</div>
          </template>
          <div v-for="todo in aggregate.myTodos || []" :key="todo.title" class="todo-row">
            <el-tag :type="mapPriorityTag(todo.priority)" effect="dark" size="small" round>{{ todo.priority }}</el-tag>
            <div class="todo-title">{{ todo.title }}</div>
            <div class="todo-date">{{ todo.dueDate }}</div>
          </div>
          <el-empty v-if="(aggregate.myTodos || []).length === 0" description="今日无待办" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 生命周期可视化 + 质量快照 -->
    <el-row :gutter="14" class="dash-row">
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-title">🔄 项目生命周期流程</div>
          </template>
          <div class="lifecycle-wrap">
            <span
              v-for="(stage, idx) in aggregate.lifecycle || []"
              :key="stage"
              class="lc-node"
              :class="mapLifecycleColor(idx)"
            >
              {{ stage }}
              <span v-if="idx < (aggregate.lifecycle?.length || 0) - 1" class="lc-arrow">→</span>
            </span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-title">📊 本迭代质量快照</div>
          </template>
          <div class="q-row">
            <div class="q-label">缺陷数</div>
            <div class="q-value stat-amber">{{ aggregate.qualitySnapshot?.defectCount ?? '-' }}</div>
          </div>
          <div class="q-row">
            <div class="q-label">用例通过率</div>
            <div class="q-value stat-green">{{ aggregate.qualitySnapshot?.testPassRate ?? '-' }}%</div>
          </div>
          <div class="q-row">
            <div class="q-label">代码覆盖率</div>
            <div class="q-value stat-blue">{{ aggregate.qualitySnapshot?.codeCoverage ?? '-' }}%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- AI 改进建议（次要,仅在有内容时显示） -->
    <el-card v-if="aggregate.aiMetrics?.recommendations?.length" class="dash-ai-tips" shadow="never">
      <template #header>
        <div class="panel-title">💡 AI 改进建议</div>
      </template>
      <ul class="tips-list">
        <li v-for="(tip, i) in aggregate.aiMetrics.recommendations" :key="i">{{ tip }}</li>
      </ul>
    </el-card>

    <!-- 数据来源标注 -->
    <div class="dash-footer">
      <el-tag size="small" type="info" effect="plain">
        本期聚合数据为示例值（mock），下个迭代接入真实跨模块查询
      </el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { Star } from '@element-plus/icons-vue'
import { aggregateDashboard } from '../api'
import type { DashboardAggregate } from '../types'

defineOptions({ name: 'Dashboard' })

const loading = ref(false)
const aiInput = ref('')
const userName = ref('张总')   // TODO: 从 useUserStore 拿真实昵称
const aggregate = reactive<Partial<DashboardAggregate>>({})

const aiQuickActions = [
  { icon: '🚀', label: 'AI 快速立项', to: '/business/inception' },
  { icon: '🔍', label: '竞品分析',   to: '/business/competitive' },
  { icon: '📄', label: '生成 PRD',   to: '/business/prd' },
  { icon: '🧪', label: '生成测试用例', to: '/business/testcase' },
  { icon: '🏭', label: '生成测试数据', to: '/business/testdata' },
  { icon: '📖', label: '一键生成手册', to: '/business/manual-product' }
]

/** 风险项数 = 待办中 P0 的条数 */
const riskCount = computed(() =>
  (aggregate.myTodos || []).filter(t => t.priority === 'P0').length
)

async function loadAggregate() {
  loading.value = true
  try {
    const res: any = await aggregateDashboard()
    Object.assign(aggregate, res.data || {})
  } catch (err) {
    console.warn('[dashboard] aggregate 加载失败', err)
  } finally {
    loading.value = false
  }
}

function onSendAi() {
  if (!aiInput.value.trim()) return
  // 本期 ai-agent 模块未就绪 — 占位提示
  const msg = aiInput.value
  aiInput.value = ''
  alert('AI 命令已记录 (本期占位): ' + msg + '\n\nai-agent 模块就绪后启用真实自然语言生成')
}

/** 进度条配色 mapping(原型 primary/success/warning → el-progress status) */
function mapProgressStatus(color: string): '' | 'success' | 'warning' | 'exception' {
  if (color === 'success') return 'success'
  if (color === 'warning') return 'warning'
  if (color === 'danger')  return 'exception'
  return ''
}

/** P0/P1/P2 → el-tag type */
function mapPriorityTag(p: string): 'danger' | 'warning' | 'info' {
  if (p === 'P0') return 'danger'
  if (p === 'P1') return 'warning'
  return 'info'
}

/** 17 阶段按 PRD 分泳道着色：前 5 蓝、设计编码 4 绿、测试 5 橙、交付 3 紫 */
function mapLifecycleColor(idx: number): string {
  if (idx < 5)  return 'lc-blue'
  if (idx < 9)  return 'lc-green'
  if (idx < 14) return 'lc-amber'
  return 'lc-purple'
}

// 60 秒自动刷新
let timer: number | undefined
onMounted(() => {
  loadAggregate()
  timer = window.setInterval(loadAggregate, 60_000)
})
onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
.dash-container {
  padding: 16px 18px 32px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 欢迎区 */
.dash-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: linear-gradient(135deg, #ecfdf5 0%, #f0f9ff 100%);
  border-radius: 10px;
  border: 1px solid #d1fae5;
}
.hero-title  { font-size: 17px; font-weight: 700; }
.hero-sub    { font-size: 12.5px; color: #475569; margin-top: 4px; }
.hero-strong { color: #16a34a; }
.hero-warning { color: #f59e0b; }

/* AI 助手 */
.dash-ai :deep(.el-card__body) { padding: 14px 16px; }
.ai-head    { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.ai-avatar  {
  width: 36px; height: 36px; border-radius: 50%; background: #f0fdf4;
  display: flex; align-items: center; justify-content: center; font-size: 18px;
}
.ai-title   { font-size: 14px; font-weight: 700; }
.ai-sub     { font-size: 11.5px; color: #64748b; margin-top: 2px; }
.ai-body    { display: flex; gap: 8px; align-items: stretch; }
.ai-body :deep(.el-textarea) { flex: 1; }
.ai-quick   { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; }
.ai-q       { cursor: pointer; }

/* stat 卡 */
.stats-row    { margin: 4px 0; }
.stat-card    { border-radius: 8px; }
.stat-card :deep(.el-card__body) { padding: 14px; }
.stat-label   { font-size: 12px; color: #64748b; }
.stat-value   { font-size: 24px; font-weight: 700; margin: 4px 0; }
.stat-delta   { font-size: 11.5px; color: #94a3b8; }
.stat-up      { color: #16a34a; }
.stat-green   { color: #16a34a; }
.stat-purple  { color: #8b5cf6; }
.stat-amber   { color: #f59e0b; }
.stat-blue    { color: #3b82f6; }

/* row */
.dash-row     { margin: 0; }
.dash-row .el-col { margin-bottom: 14px; }
.panel        { border-radius: 8px; height: 100%; }
.panel :deep(.el-card__header) { padding: 10px 14px; }
.panel :deep(.el-card__body)   { padding: 12px 14px; }
.panel-title  { font-size: 13.5px; font-weight: 700; }

/* 项目进度 */
.proj-row     { margin-bottom: 12px; }
.proj-name    { font-size: 13px; margin-bottom: 4px; }

/* 待办 */
.todo-row     {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 0; border-bottom: 1px dashed #e5e7eb;
}
.todo-row:last-child { border-bottom: none; }
.todo-title   { flex: 1; font-size: 13px; }
.todo-date    { font-size: 11.5px; color: #94a3b8; }

/* 生命周期 */
.lifecycle-wrap { display: flex; flex-wrap: wrap; gap: 4px; align-items: center; }
.lc-node {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 4px 8px; border-radius: 4px; font-size: 11.5px; line-height: 1.6;
}
.lc-blue   { background: #dbeafe; color: #1e40af; }
.lc-green  { background: #d1fae5; color: #065f46; }
.lc-amber  { background: #fef3c7; color: #92400e; }
.lc-purple { background: #ede9fe; color: #5b21b6; }
.lc-arrow  { margin: 0 4px; color: #94a3b8; }

/* 质量快照 */
.q-row       { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px dashed #e5e7eb; }
.q-row:last-child { border-bottom: none; }
.q-label     { font-size: 12.5px; color: #475569; }
.q-value     { font-size: 18px; font-weight: 700; }

/* AI tips */
.tips-list   { padding-left: 16px; margin: 4px 0; font-size: 12.5px; color: #475569; line-height: 1.8; }

/* footer */
.dash-footer { text-align: center; opacity: 0.7; margin-top: 4px; }
</style>
