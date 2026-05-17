<!--
  工作台 — UI §4.2 + 原型 dashboard.html
  布局: 顶部欢迎 + 4 stat 卡 + AI Assistant 面板 + Widget 网格 (4 widgets)
  +    底部 17 阶段 lifecycle swimlane
-->
<template>
  <div class="app-container dashboard-workspace">
    <!-- 顶部欢迎 -->
    <el-card shadow="hover" :body-style="{ padding: '20px 24px' }">
      <div class="welcome">
        <div>
          <div class="hello">早上好, {{ userName }} 👋</div>
          <div class="hello-sub">
            您有 <b style="color:#5b21b6">{{ data.myTodos?.length || 0 }} 个待办</b>,
            <b style="color:#F56C6C">{{ data.stats?.currentDefects || 0 }} 个缺陷</b> 待处理
          </div>
        </div>
        <el-button-group>
          <el-button icon="Refresh" :loading="loading" @click="loadAggregate">刷新</el-button>
          <el-button type="primary" icon="Setting" @click="settingsOpen = true">⚙️ 自定义</el-button>
        </el-button-group>
      </div>
    </el-card>

    <!-- 4 个 stat 卡 (跟原型顶部 4 卡片对齐) -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background:#dcfce7">📦</div>
            <div>
              <div class="stat-num" style="color:#166534">{{ data.stats?.activeProjects || 0 }}</div>
              <div class="stat-label">在办项目</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background:#ede9fe">📄</div>
            <div>
              <div class="stat-num" style="color:#5b21b6">{{ data.stats?.aiDocsGenerated || 0 }}</div>
              <div class="stat-label">AI 文档生成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background:#fee2e2">🐛</div>
            <div>
              <div class="stat-num" style="color:#991b1b">{{ data.stats?.currentDefects || 0 }}</div>
              <div class="stat-label">当前缺陷</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background:#dbeafe">🤖</div>
            <div>
              <div class="stat-num" style="color:#1d4ed8">{{ data.stats?.autoTestCoverage || 0 }}%</div>
              <div class="stat-label">自动化覆盖率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- AI Assistant 面板 -->
    <el-card shadow="hover" style="margin-top:16px" :body-style="{ padding: '20px', background: 'linear-gradient(135deg, #5b21b6 0%, #1d4ed8 100%)' }">
      <div class="ai-panel">
        <div class="ai-info">
          <div class="ai-title">✨ AgriAI 助手</div>
          <div class="ai-sub">DeepSeek-V3 + Claude · Dify 工作流 · AgriKB 知识库</div>
        </div>
        <div class="ai-cmds">
          <el-button v-for="cmd in quickCmds" :key="cmd.label" plain size="small"
                     :style="{ background: 'rgba(255,255,255,0.15)', color: 'white', border: 'none' }"
                     @click="runCmd(cmd)">{{ cmd.icon }} {{ cmd.label }}</el-button>
        </div>
      </div>
    </el-card>

    <!-- Widget 网格 -->
    <el-row :gutter="16" style="margin-top:16px">
      <!-- 在办项目进度 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            🚀 在办项目进度
            <el-button link type="primary" style="float:right" @click="$router.push('/business/project')">查看全部 →</el-button>
          </template>
          <div v-for="proj in data.activeProjects || []" :key="proj.name" style="margin-bottom:14px">
            <div style="display:flex;justify-content:space-between;font-size:13px;margin-bottom:4px">
              <span style="font-weight:500">{{ proj.name }}</span>
              <span>{{ proj.progress }}%</span>
            </div>
            <el-progress :percentage="proj.progress" :color="progressColor(proj.color)" :stroke-width="10" />
          </div>
        </el-card>
      </el-col>

      <!-- 我的待办 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            ✅ 我的待办
            <el-button link type="primary" style="float:right">查看全部 →</el-button>
          </template>
          <div v-for="todo in data.myTodos || []" :key="todo.title" style="padding:8px 0;border-bottom:1px dashed #ebeef5">
            <div style="display:flex;justify-content:space-between;align-items:center">
              <div style="font-size:13px">{{ todo.title }}</div>
              <div>
                <el-tag size="small" :type="priorityColor(todo.priority)">{{ todo.priority }}</el-tag>
                <span style="margin-left:8px;color:#909399;font-size:12px">{{ todo.dueDate }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 本迭代质量快照 + AI 指标 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>📊 本迭代质量快照</template>
          <el-row :gutter="8">
            <el-col :span="8">
              <el-statistic title="缺陷数" :value="data.qualitySnapshot?.defectCount || 0"
                            :value-style="{ color: '#F56C6C' }" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="测试通过率" :value="data.qualitySnapshot?.testPassRate || 0" :precision="1" suffix=" %"
                            :value-style="{ color: '#67C23A' }" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="代码覆盖率" :value="data.qualitySnapshot?.codeCoverage || 0" :precision="1" suffix=" %"
                            :value-style="{ color: '#5b21b6' }" />
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>🤖 AI 改进指标</template>
          <div style="margin-bottom:12px">
            <span style="font-size:14px">本月节省 <b style="color:#5b21b6">{{ data.aiMetrics?.hoursSaved || 0 }} 小时</b>, 生成
              <b style="color:#5b21b6">{{ data.aiMetrics?.docsGenerated || 0 }}</b> 篇文档</span>
          </div>
          <div v-for="(rec, i) in data.aiMetrics?.recommendations || []" :key="i"
               style="padding:6px 10px;background:#f5f3ff;border-radius:6px;margin-bottom:6px;font-size:12.5px;color:#5b21b6">
            💡 {{ rec }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 项目生命周期 17 阶段 swimlane (原型 lifecycle widget) -->
    <el-card shadow="hover" style="margin-top:16px">
      <template #header>🔄 项目生命周期 (17 阶段)</template>
      <div class="lifecycle">
        <div v-for="(stage, i) in data.lifecycle || []" :key="i" class="lifecycle-item">
          <div class="lifecycle-num">{{ i + 1 }}</div>
          <div class="lifecycle-name">{{ stage }}</div>
        </div>
      </div>
    </el-card>

    <!-- 自定义 widget settings -->
    <el-dialog v-model="settingsOpen" title="⚙️ 自定义工作台 widget" width="500px">
      <p style="color:#909399;font-size:13px">勾选要显示的 widget (后端 PUT /business/dashboard 持久化):</p>
      <el-checkbox-group v-model="enabledWidgets">
        <el-checkbox label="stats">📊 顶部 4 stat 卡</el-checkbox>
        <el-checkbox label="active_projects">🚀 在办项目</el-checkbox>
        <el-checkbox label="my_todos">✅ 我的待办</el-checkbox>
        <el-checkbox label="quality_snapshot">📊 本迭代质量</el-checkbox>
        <el-checkbox label="ai_metrics">🤖 AI 改进指标</el-checkbox>
        <el-checkbox label="lifecycle">🔄 项目生命周期</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="settingsOpen = false">取消</el-button>
        <el-button type="primary" @click="saveSettings">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, getCurrentInstance, onMounted } from 'vue'
import type { ComponentInternalInstance } from 'vue'
import { aggregateDashboard, type DashboardAggregate } from '@/api/business/dashboard'

const { proxy } = getCurrentInstance() as ComponentInternalInstance

const loading = ref(false)
const settingsOpen = ref(false)
const enabledWidgets = ref<string[]>(['stats', 'active_projects', 'my_todos', 'quality_snapshot', 'ai_metrics', 'lifecycle'])
const userName = ref((proxy as any).$store?.state?.user?.name || '张总')
const data = ref<DashboardAggregate>({
  stats: { activeProjects: 0, aiDocsGenerated: 0, currentDefects: 0, autoTestCoverage: 0 },
  activeProjects: [],
  myTodos: [],
  qualitySnapshot: { defectCount: 0, testPassRate: 0, codeCoverage: 0 },
  aiMetrics: { hoursSaved: 0, docsGenerated: 0, recommendations: [] },
  lifecycle: []
})

const quickCmds = [
  { icon: '🚀', label: '立项建议', route: '/business/inception' },
  { icon: '🔍', label: '竞品分析', route: '/business/competitive' },
  { icon: '📄', label: 'PRD',     route: '/business/prd' },
  { icon: '🎨', label: 'UED',     route: '/business/ued' },
  { icon: '🏗️', label: '架构',    route: '/business/arch' },
  { icon: '🧪', label: '测试用例', route: '/business/testcase' },
  { icon: '🏭', label: '测试数据', route: '/business/testdata' },
  { icon: '📖', label: '产品手册', route: '/business/manual-product' }
]

function runCmd(cmd: { route: string }) {
  ;(proxy as any).$router.push(cmd.route)
}

function progressColor(color: string) {
  return color === 'success' ? '#67C23A' : color === 'warning' ? '#E6A23C' : color === 'danger' ? '#F56C6C' : '#5b21b6'
}

function priorityColor(p: string) {
  return p === 'P0' ? 'danger' : p === 'P1' ? 'warning' : 'info'
}

async function loadAggregate() {
  loading.value = true
  try {
    const userId = (proxy as any).$store?.state?.user?.id || 1
    const r: any = await aggregateDashboard(userId)
    if (r.code === 200) data.value = r.data
  } finally { loading.value = false }
}

function saveSettings() {
  // TODO: 后端 PUT /business/dashboard 保存 widget_types CSV
  settingsOpen.value = false
  ;(proxy as any).$modal.msgSuccess('已保存 widget 配置 (mock, 待接后端 PUT)')
}

onMounted(() => { loadAggregate() })
</script>

<style scoped>
.dashboard-workspace .welcome { display: flex; align-items: center; justify-content: space-between; }
.hello { font-size: 20px; font-weight: 700; color: #303133; }
.hello-sub { font-size: 13px; color: #606266; margin-top: 4px; }
.stat-card { display: flex; align-items: center; gap: 14px; }
.stat-icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 28px; }
.stat-num { font-size: 28px; font-weight: 700; line-height: 1.2; }
.stat-label { font-size: 13px; color: #606266; margin-top: 2px; }
.ai-panel { display: flex; align-items: center; justify-content: space-between; color: white; }
.ai-title { font-size: 18px; font-weight: 700; }
.ai-sub { font-size: 12.5px; opacity: 0.9; margin-top: 4px; }
.ai-cmds { display: flex; gap: 8px; flex-wrap: wrap; max-width: 720px; }
.lifecycle { display: flex; gap: 4px; flex-wrap: wrap; overflow-x: auto; padding: 8px 0; }
.lifecycle-item {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 8px 10px; background: #f5f3ff; border-radius: 8px; min-width: 80px;
}
.lifecycle-num { width: 24px; height: 24px; border-radius: 50%; background: #5b21b6; color: white; font-size: 12px; font-weight: 700; display: flex; align-items: center; justify-content: center; }
.lifecycle-name { font-size: 11.5px; color: #303133; }
</style>
