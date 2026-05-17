<!--
  效能分析 — PRD §F6 + 原型 analytics.html
  布局: 4 stat 卡 + 2 chart 卡 (AI 提效柱状图 + 项目健康度) + 1 AI 改进建议卡
  +  顶部时间粒度切换 (本月/本季度/本年) + AI 重跑按钮 + 历史 dialog
-->
<template>
  <div class="app-container analytics-workspace">
    <div class="ph">
      <div>
        <div class="pt">效能分析</div>
        <div class="ps">PLM 4 指标 + DORA 4 指标 + AI 节省工时 + AI 改进建议</div>
      </div>
      <el-button-group>
        <el-select v-model="periodFilter" placeholder="周期" style="width: 120px" @change="loadLatest">
          <el-option label="本月" value="month" />
          <el-option label="本季度" value="quarter" />
          <el-option label="本年" value="year" />
        </el-select>
        <el-button icon="Document" @click="openList">📁 历史快照</el-button>
        <el-button type="primary" icon="Plus" @click="newSnapshot">📊 新建快照</el-button>
        <el-button type="warning" :loading="aiLoading" :disabled="!form.snapshotId"
                   icon="MagicStick" @click="handleAi">✨ AI 复盘建议</el-button>
      </el-button-group>
    </div>

    <div v-if="!form.snapshotId" style="text-align:center;padding:80px;color:#909399">
      <div style="font-size:48px;margin-bottom:10px">📊</div>
      <div>请先创建本期 ({{ periodLabel }}) 快照, 再点 AI 复盘</div>
    </div>

    <div v-else style="margin-top:16px">
      <!-- 4 个顶部 stat 卡 (跟原型 analytics.html 顶部 4 卡片对齐) -->
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div class="stat-card">
              <div class="stat-icon" style="background:#dcfce7">📋</div>
              <div>
                <div class="stat-num" style="color:#166534">{{ form.requirementThroughput || 0 }}</div>
                <div class="stat-label">需求吞吐量</div>
                <div class="stat-trend">{{ trendBadge('throughput') }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div class="stat-card">
              <div class="stat-icon" style="background:#dbeafe">⏱️</div>
              <div>
                <div class="stat-num" style="color:#1d4ed8">{{ form.sprintOnTimeRate || 0 }}%</div>
                <div class="stat-label">迭代准时率</div>
                <div class="stat-trend">{{ trendBadge('onTime') }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div class="stat-card">
              <div class="stat-icon" style="background:#fee2e2">🐛</div>
              <div>
                <div class="stat-num" style="color:#991b1b">{{ form.defectDensity || 0 }}</div>
                <div class="stat-label">缺陷密度 (个/KLOC)</div>
                <div class="stat-trend">{{ defectQuality }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div class="stat-card">
              <div class="stat-icon" style="background:#ede9fe">🤖</div>
              <div>
                <div class="stat-num" style="color:#5b21b6">{{ form.aiHoursSaved || 0 }}h</div>
                <div class="stat-label">AI 节省工时</div>
                <div class="stat-trend">本{{ periodLabel.slice(1) }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- DORA 4 指标卡 -->
      <el-divider content-position="left">📊 DORA 4 指标</el-divider>
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="部署频率" :value="form.deploymentFrequency || 0" :precision="2" suffix=" 次/天">
              <template #suffix>
                <el-tag size="small" :type="doraLevel('deploy_freq')" style="margin-left:8px">
                  {{ doraGrade('deploy_freq') }}
                </el-tag>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="前置时间" :value="form.leadTimeHours || 0" :precision="1" suffix=" h">
              <template #suffix>
                <el-tag size="small" :type="doraLevel('lead_time')" style="margin-left:8px">
                  {{ doraGrade('lead_time') }}
                </el-tag>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="MTTR" :value="form.mttrHours || 0" :precision="1" suffix=" h">
              <template #suffix>
                <el-tag size="small" :type="doraLevel('mttr')" style="margin-left:8px">
                  {{ doraGrade('mttr') }}
                </el-tag>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="变更失败率" :value="form.changeFailureRate || 0" :precision="1" suffix=" %">
              <template #suffix>
                <el-tag size="small" :type="doraLevel('cfr')" style="margin-left:8px">
                  {{ doraGrade('cfr') }}
                </el-tag>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>

      <!-- AI 提效柱状图 + 项目健康度 (前端 mock 渲染, 后端字段未来扩展) -->
      <el-row :gutter="16" style="margin-top:16px">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>📊 各阶段 AI 提效分析</template>
            <!-- 简化 mock 柱状图 — 后续可换 ECharts -->
            <div v-for="stage in aiEfficiencyStages" :key="stage.name" style="margin-bottom:14px">
              <div style="display:flex;justify-content:space-between;font-size:12px;color:#606266">
                <span>{{ stage.icon }} {{ stage.name }}</span>
                <span><span style="color:#909399">手工 {{ stage.manual }}h</span> →
                  <span style="color:#5b21b6;font-weight:700">AI {{ stage.ai }}h ({{ stage.saved }}% ↓)</span></span>
              </div>
              <el-progress :percentage="100 * stage.ai / stage.manual" :color="progressColor"
                           :stroke-width="14" style="margin-top:4px" />
            </div>
            <div style="margin-top:8px;padding-top:8px;border-top:1px solid #ebeef5;text-align:center;color:#5b21b6;font-weight:700">
              💡 AI 平均提效 83%, 本{{ periodLabel.slice(1) }}节省 {{ form.aiHoursSaved || 284 }} 小时
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>🚥 项目健康度评分</template>
            <div v-for="proj in projectHealth" :key="proj.name" style="margin-bottom:14px">
              <div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:4px">
                <span>{{ proj.name }}</span>
                <span><el-tag size="small" :type="proj.status">{{ proj.score }} / 100</el-tag></span>
              </div>
              <el-progress :percentage="proj.score"
                           :status="proj.score >= 80 ? 'success' : proj.score >= 60 ? 'warning' : 'exception'"
                           :stroke-width="14" />
            </div>
            <div style="margin-top:8px;padding-top:8px;border-top:1px solid #ebeef5;text-align:center;color:#909399;font-size:12px">
              共 {{ form.activeProjects || 7 }} 个在办项目, {{ form.projectsAtRisk || 2 }} 个风险
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- AI 改进建议 -->
      <el-card shadow="hover" style="margin-top:16px">
        <template #header>
          🤖 AI 改进建议
          <span v-if="form.aiGenerated === 'Y'" style="color:#909399;font-size:12px;margin-left:8px">
            ✓ 已生成 {{ form.aiGeneratedAt }}
          </span>
        </template>
        <div v-if="form.aiGenerated !== 'Y'" style="text-align:center;padding:30px;color:#909399">
          <div>点击右上角"AI 复盘建议"生成针对性改进建议</div>
        </div>
        <div v-else class="ai-advice">
          <pre>{{ form.aiRecommendations }}</pre>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="listOpen" title="📁 历史效能快照" width="900px">
      <el-table :data="list" @row-click="loadSnapshot">
        <el-table-column prop="snapshotNo" label="编号" width="160" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="periodType" label="周期" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ row.periodType === 'month' ? '月' : row.periodType === 'quarter' ? '季度' : '年' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="snapshotDate" label="日期" width="120" />
        <el-table-column label="AI" width="60" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.aiGenerated === 'Y' ? 'success' : 'info'">
              {{ row.aiGenerated === 'Y' ? '✓' : '—' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, getCurrentInstance, onMounted } from 'vue'
import type { ComponentInternalInstance } from 'vue'
import {
  listAnalytics, getAnalytics, addAnalytics, aiRecommendAnalytics,
  type AnalyticsSnapshot
} from '@/api/business/analytics'

const { proxy } = getCurrentInstance() as ComponentInternalInstance

const periodFilter = ref<'month' | 'quarter' | 'year'>('month')
const aiLoading = ref(false)
const listOpen = ref(false)
const list = ref<AnalyticsSnapshot[]>([])

const initForm = (): AnalyticsSnapshot => ({
  title: `${periodLabel.value}快照-${new Date().toISOString().slice(0, 10)}`,
  periodType: periodFilter.value,
  snapshotDate: new Date().toISOString().slice(0, 10),
  authorUserId: (proxy as any).$store?.state?.user?.id || 1,
  status: '00'
})
const form = ref<AnalyticsSnapshot>({
  title: '',
  periodType: 'month',
  snapshotDate: new Date().toISOString().slice(0, 10),
  authorUserId: 1
})

const periodLabel = computed(() =>
  periodFilter.value === 'month' ? '本月' : periodFilter.value === 'quarter' ? '本季度' : '本年'
)

// 缺陷密度评级 (低于 2 为优)
const defectQuality = computed(() => {
  const d = form.value.defectDensity || 0
  if (d <= 2) return '✅ 优 (≤2)'
  if (d <= 5) return '⚠️ 中 (2-5)'
  return '🔴 差 (>5)'
})

// DORA 等级评估 (Google DORA 标准)
function doraGrade(type: string) {
  if (type === 'deploy_freq') {
    const v = form.value.deploymentFrequency || 0
    if (v >= 1) return 'Elite'
    if (v >= 0.14) return 'High'
    if (v >= 0.03) return 'Medium'
    return 'Low'
  }
  if (type === 'lead_time') {
    const v = form.value.leadTimeHours || 0
    if (v <= 1) return 'Elite'
    if (v <= 24) return 'High'
    if (v <= 168) return 'Medium'
    return 'Low'
  }
  if (type === 'mttr') {
    const v = form.value.mttrHours || 0
    if (v <= 1) return 'Elite'
    if (v <= 24) return 'High'
    return 'Medium'
  }
  if (type === 'cfr') {
    const v = form.value.changeFailureRate || 0
    if (v <= 5) return 'Elite'
    if (v <= 15) return 'High'
    return 'Medium'
  }
  return '—'
}
function doraLevel(type: string) {
  const g = doraGrade(type)
  return g === 'Elite' ? 'success' : g === 'High' ? 'primary' : g === 'Medium' ? 'warning' : 'danger'
}

function trendBadge(_kind: string) {
  // 趋势对比上期 — 后端字段未存,本期 mock
  return _kind === 'throughput' ? '↑ 18%' : '↑ 12%'
}

const progressColor = '#5b21b6'

// AI 提效 5 阶段 mock 数据 (跟原型 aiEfficiencyChart 5 阶段对齐)
const aiEfficiencyStages = computed(() => [
  { icon: '📋', name: '需求分析', manual: 80, ai: 12, saved: 85 },
  { icon: '📄', name: 'PRD 生成', manual: 60, ai: 8,  saved: 87 },
  { icon: '🏗️', name: '架构设计', manual: 50, ai: 15, saved: 70 },
  { icon: '🧪', name: '测试用例', manual: 40, ai: 6,  saved: 85 },
  { icon: '🏭', name: '测试数据', manual: 30, ai: 4,  saved: 87 }
])

// 项目健康度 mock — 后端字段未存,本期 mock 3 个项目
const projectHealth = computed(() => [
  { name: 'AgriPLM 大屏 v2',  score: 82, status: 'success' },
  { name: '智慧灌溉 v1.3',     score: 65, status: 'warning' },
  { name: '农资电商小程序',     score: 48, status: 'danger' }
])

async function loadLatest() {
  const r: any = await listAnalytics({ periodType: periodFilter.value, pageSize: 1 })
  if (r.rows && r.rows.length > 0) {
    form.value = r.rows[0]
  } else {
    form.value = { ...initForm() }
  }
}

function newSnapshot() {
  form.value = { ...initForm(), title: `${periodLabel.value}快照-${new Date().toISOString().slice(0, 10)}` }
  // 直接落库
  addAnalytics(form.value).then(async () => {
    await loadLatest()
    ;(proxy as any).$modal.msgSuccess('快照已创建')
  })
}

async function openList() {
  const r: any = await listAnalytics({ pageSize: 100 })
  list.value = r.rows
  listOpen.value = true
}

async function loadSnapshot(row: AnalyticsSnapshot) {
  if (!row.snapshotId) return
  const r: any = await getAnalytics(row.snapshotId)
  form.value = r.data
  periodFilter.value = r.data.periodType as any
  listOpen.value = false
}

async function handleAi() {
  if (!form.value.snapshotId) return
  aiLoading.value = true
  try {
    const r: any = await aiRecommendAnalytics(form.value.snapshotId)
    if (r.code === 200) {
      form.value = r.data
      ;(proxy as any).$modal.msgSuccess('AI 改进建议已生成')
    }
  } finally { aiLoading.value = false }
}

onMounted(() => { loadLatest() })
</script>

<style scoped>
.analytics-workspace .ph { display: flex; align-items: center; justify-content: space-between; }
.analytics-workspace .pt { font-size: 18px; font-weight: 700; }
.analytics-workspace .ps { font-size: 13px; color: #909399; margin-top: 4px; }
.stat-card { display: flex; align-items: center; gap: 14px; }
.stat-icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 28px; }
.stat-num { font-size: 28px; font-weight: 700; line-height: 1.2; }
.stat-label { font-size: 13px; color: #606266; margin-top: 2px; }
.stat-trend { font-size: 12px; color: #67C23A; margin-top: 2px; }
.ai-advice pre { white-space: pre-wrap; background: #f5f7fa; padding: 12px; border-radius: 6px; font-size: 13px; line-height: 1.6; }
</style>
