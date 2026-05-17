<!--
  竞品情报 — PRD §F1.3 + 原型 competitive.html (3 tab: 矩阵/监控/SWOT)
-->
<template>
  <div class="app-container comp-workspace">
    <div class="ph">
      <div>
        <div class="pt">竞品情报</div>
        <div class="ps">15 维度 × 5 竞品矩阵 + 动态监控 + 本品 SWOT 分析</div>
      </div>
      <el-button-group>
        <el-button icon="Document" @click="openList">📁 历史分析</el-button>
        <el-button type="primary" icon="Plus" @click="newComp">🔍 新建分析</el-button>
        <el-button type="warning" :loading="aiLoading" :disabled="!form.competitiveId"
                   icon="MagicStick" @click="handleAi">✨ AI 重跑分析</el-button>
      </el-button-group>
    </div>

    <el-tabs v-model="activeTab" type="card" style="margin-top:16px">
      <!-- Tab 1: 竞品对比矩阵 -->
      <el-tab-pane label="🔲 竞品对比矩阵" name="matrix">
        <el-card v-if="!matrix" shadow="hover">
          <div style="text-align:center;padding:40px;color:#909399">
            <div style="font-size:40px;margin-bottom:10px">📊</div>
            <div>暂无矩阵数据, 请点击右上角 "AI 重跑分析" 生成</div>
          </div>
        </el-card>
        <el-card v-else shadow="hover">
          <el-table :data="matrixRows" border size="small" :cell-style="cellStyle">
            <el-table-column prop="dim" label="功能维度" width="160" fixed>
              <template #default="{ row }">
                <span style="font-weight:500;font-size:12.5px">{{ row.dim }}</span>
              </template>
            </el-table-column>
            <el-table-column v-for="(v, vi) in matrix.vendors" :key="vi"
                             :label="v.isOurProduct ? '★ ' + v.name : v.name"
                             align="center" :width="120">
              <template #default="{ row }">
                <span :style="scoreStyle(row.scores[vi], v.isOurProduct)">
                  {{ scoreIcon(row.scores[vi], v.isOurProduct) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top:12px;font-size:12px;color:#909399">
            图例: ★ 本品独有 · ✓ 已实现 · △ 部分 · ✗ 缺失
          </div>
        </el-card>
      </el-tab-pane>

      <!-- Tab 2: 竞品动态监控 -->
      <el-tab-pane label="📰 竞品动态监控" name="monitor">
        <el-card shadow="hover">
          <el-table :data="monitors" size="small">
            <el-table-column prop="vendor" label="竞品" width="120" />
            <el-table-column prop="news" label="最新动态" min-width="320" :show-overflow-tooltip="true" />
            <el-table-column label="威胁度" width="100" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="threatColor(row.threatLevel)">
                  {{ threatLabel(row.threatLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="date" label="日期" width="120" align="center" />
          </el-table>
          <div v-if="monitors.length === 0" style="text-align:center;padding:40px;color:#909399">
            <div>暂无监控数据</div>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- Tab 3: 本品 SWOT -->
      <el-tab-pane label="📐 本品 SWOT 分析" name="swot">
        <el-card v-if="!ourSwot" shadow="hover">
          <div style="text-align:center;padding:40px;color:#909399">
            <div>暂无 SWOT 数据</div>
          </div>
        </el-card>
        <el-row v-else :gutter="16">
          <el-col :span="12">
            <el-card shadow="hover" :body-style="{ background: '#dcfce7' }">
              <template #header><span style="color:#166534;font-weight:700">💪 优势 (Strengths)</span></template>
              <ul style="margin:0;padding-left:20px">
                <li v-for="(s, i) in ourSwot.strengths" :key="i" style="margin-bottom:6px">{{ s }}</li>
              </ul>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" :body-style="{ background: '#fee2e2' }">
              <template #header><span style="color:#991b1b;font-weight:700">⚠️ 劣势 (Weaknesses)</span></template>
              <ul style="margin:0;padding-left:20px">
                <li v-for="(s, i) in ourSwot.weaknesses" :key="i" style="margin-bottom:6px">{{ s }}</li>
              </ul>
            </el-card>
          </el-col>
          <el-col :span="12" style="margin-top:16px">
            <el-card shadow="hover" :body-style="{ background: '#dbeafe' }">
              <template #header><span style="color:#1d4ed8;font-weight:700">🌱 机会 (Opportunities)</span></template>
              <ul style="margin:0;padding-left:20px">
                <li v-for="(s, i) in ourSwot.opportunities" :key="i" style="margin-bottom:6px">{{ s }}</li>
              </ul>
            </el-card>
          </el-col>
          <el-col :span="12" style="margin-top:16px">
            <el-card shadow="hover" :body-style="{ background: '#fef3c7' }">
              <template #header><span style="color:#92400e;font-weight:700">🔥 威胁 (Threats)</span></template>
              <ul style="margin:0;padding-left:20px">
                <li v-for="(s, i) in ourSwot.threats" :key="i" style="margin-bottom:6px">{{ s }}</li>
              </ul>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="listOpen" title="📁 历史竞品分析" width="900px">
      <el-table :data="list" @row-click="loadComp">
        <el-table-column prop="competitiveNo" label="编号" width="160" />
        <el-table-column prop="competitorName" label="名称/项目" min-width="200" />
        <el-table-column label="AI" width="60" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.aiGenerated === 'Y' ? 'success' : 'info'">
              {{ row.aiGenerated === 'Y' ? '✓' : '—' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, getCurrentInstance } from 'vue'
import type { ComponentInternalInstance } from 'vue'
import {
  listCompetitive, getCompetitive, addCompetitive, aiAnalyzeCompetitive,
  parseMatrix, parseMonitors, parseOurSwot,
  type Competitive
} from '@/api/business/competitive'

const { proxy } = getCurrentInstance() as ComponentInternalInstance

const activeTab = ref('matrix')
const aiLoading = ref(false)
const listOpen = ref(false)
const list = ref<Competitive[]>([])

const initForm = (): Competitive => ({
  projectId: 1,
  competitorName: '项目级矩阵分析',
  authorUserId: (proxy as any).$store?.state?.user?.id || 1,
  status: '00'
})
const form = ref<Competitive>(initForm())

const matrix = computed(() => parseMatrix(form.value))
const monitors = computed(() => parseMonitors(form.value))
const ourSwot = computed(() => parseOurSwot(form.value))

// 把矩阵数据 reshape 成 el-table 可消费的 row 结构
const matrixRows = computed(() => {
  if (!matrix.value) return []
  return matrix.value.dimensions.map((d, di) => ({
    dim: d.name,
    scores: matrix.value!.scores[di] || []
  }))
})

function scoreIcon(score: number, isOur: boolean) {
  if (score === 1) return isOur ? '★' : '✓'
  if (score === 0.5) return '△'
  return '✗'
}

function scoreStyle(score: number, isOur: boolean) {
  const base = 'padding:2px 10px;border-radius:6px;font-size:13px;font-weight:700;display:inline-block;'
  if (score === 1) {
    return base + (isOur
      ? 'background:#ede9fe;color:#5b21b6;'
      : 'background:#dcfce7;color:#166534;')
  }
  if (score === 0.5) return base + 'background:#fef3c7;color:#92400e;'
  return base + 'background:#fee2e2;color:#991b1b;'
}

function cellStyle({ columnIndex }: { columnIndex: number }) {
  // 本品列高亮 (从右数第 1 列,即 5 列里的最后 1 列)
  const lastVendorIdx = (matrix.value?.vendors.length || 0)
  if (columnIndex === lastVendorIdx) return { background: '#f5f3ff' }
  return {}
}

function threatColor(level: string) {
  return level === 'high' ? 'danger' : level === 'mid' ? 'warning' : 'info'
}
function threatLabel(level: string) {
  return level === 'high' ? '高' : level === 'mid' ? '中' : '低'
}

function newComp() { form.value = initForm() }
async function openList() {
  const res: any = await listCompetitive({ pageSize: 100 })
  list.value = res.rows
  listOpen.value = true
}
async function loadComp(row: Competitive) {
  if (!row.competitiveId) return
  const res: any = await getCompetitive(row.competitiveId)
  form.value = res.data
  listOpen.value = false
}

async function handleAi() {
  if (!form.value.competitiveId) {
    await addCompetitive(form.value)
    const r: any = await listCompetitive({ competitorName: form.value.competitorName, pageSize: 5 })
    const fresh = r.rows.find((x: Competitive) => x.competitorName === form.value.competitorName)
    if (fresh) form.value.competitiveId = fresh.competitiveId
  }
  if (!form.value.competitiveId) return
  aiLoading.value = true
  try {
    const ai: any = await aiAnalyzeCompetitive(form.value.competitiveId)
    if (ai.code === 200) {
      form.value = ai.data
      ;(proxy as any).$modal.msgSuccess(`分析完成: ${matrix.value?.dimensions.length} 维度 × ${matrix.value?.vendors.length} 竞品`)
    }
  } finally { aiLoading.value = false }
}

// 初始 mock 让首屏有内容看
form.value.matrixJson = ''
</script>

<style scoped>
.comp-workspace .ph { display: flex; align-items: center; justify-content: space-between; }
.comp-workspace .pt { font-size: 18px; font-weight: 700; }
.comp-workspace .ps { font-size: 13px; color: #909399; margin-top: 4px; }
</style>
