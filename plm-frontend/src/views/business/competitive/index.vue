<!--
  竞品情报 — PRD §F1.3 + 原型 competitive.html
  严格对齐原型 3 Tab: 竞品对比矩阵 / 动态监控 / SWOT 分析 + AI 生成竞品报告按钮
-->
<template>
  <div class="app-container competitive-page">
    <!-- 页头 (对齐原型 .ph) -->
    <div class="page-header">
      <div>
        <h2 class="page-title">🔍 竞品情报系统</h2>
        <p class="page-subtitle">AI 持续监控竞品动态，自动生成对比分析报告</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" plain @click="openAdd">
          <el-icon><Plus /></el-icon>&nbsp;新增竞品
        </el-button>
        <el-button
          type="success"
          :loading="aiLoading"
          :disabled="!current.competitiveId"
          @click="triggerAi"
        >
          <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成竞品报告
        </el-button>
      </div>
    </div>

    <!-- Tabs (对齐原型 3 个 tab) -->
    <el-tabs v-model="activeTab" class="comp-tabs">
      <!-- Tab 1: 对比矩阵 -->
      <el-tab-pane label="📊 竞品对比矩阵" name="matrix">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">📊 功能能力对比矩阵</span>
              <el-input
                v-model="queryParams.competitorName"
                placeholder="搜索竞品"
                style="width: 240px"
                clearable
                @clear="getList"
                @keyup.enter="getList"
              />
            </div>
          </template>

          <el-table
            v-loading="listLoading"
            :data="list"
            stripe
            @row-click="loadCompetitive"
            row-class-name="clickable-row"
          >
            <el-table-column label="编号" prop="competitiveNo" width="160" />
            <el-table-column label="竞品名称" prop="competitorName" width="180" show-overflow-tooltip />
            <el-table-column label="厂商" prop="vendor" width="140" show-overflow-tooltip />
            <el-table-column label="官网" min-width="200">
              <template #default="{ row }">
                <el-link v-if="row.website" :href="row.website" target="_blank" type="primary">
                  {{ row.website }}
                </el-link>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="价格档" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="pricingTierTag(row.pricingTier)" size="small">
                  {{ pricingTierLabel(row.pricingTier) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="价格模型" prop="pricingModel" min-width="160" show-overflow-tooltip />
            <el-table-column label="监控" width="80" align="center">
              <template #default="{ row }">
                <el-tag
                  :type="row.monitorEnabled === 'Y' ? 'success' : 'info'"
                  size="small"
                >
                  {{ row.monitorEnabled === 'Y' ? '订阅中' : '未订阅' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="AI" width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.aiGenerated === 'Y'" type="success" size="small">已分析</el-tag>
                <el-tag v-else type="info" size="small">未分析</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTagFor(row.status).type" size="small">
                  {{ statusTagFor(row.status).label }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="loadCompetitive(row)">编辑</el-button>
                <el-button link type="success" @click.stop="quickAi(row)">AI 分析</el-button>
                <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <pagination
            v-if="total > 0"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            :total="total"
            @pagination="getList"
          />
        </el-card>
      </el-tab-pane>

      <!-- Tab 2: 动态监控 -->
      <el-tab-pane label="🔮 竞品动态监控" name="monitor">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">🔮 已订阅监控的竞品</span>
              <el-button type="primary" plain size="small" @click="openSubscribeDialog">
                <el-icon><Bell /></el-icon>&nbsp;+ 订阅推送
              </el-button>
            </div>
          </template>

          <el-table :data="monitoredList" stripe>
            <el-table-column label="竞品" prop="competitorName" min-width="160" />
            <el-table-column label="监控关键词" prop="monitorKeywords" min-width="240" show-overflow-tooltip>
              <template #default="{ row }">
                <el-tag
                  v-for="kw in (row.monitorKeywords || '').split(',').filter(Boolean)"
                  :key="kw"
                  size="small"
                  style="margin-right: 4px"
                >
                  {{ kw.trim() }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最近监控" prop="lastMonitoredAt" width="180" />
            <el-table-column label="操作" width="160" align="center">
              <template #default="{ row }">
                <el-button link type="warning" @click="toggleMonitor(row, 'N')">取消订阅</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无订阅,在「对比矩阵」点击「+订阅推送」开始" />
            </template>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- Tab 3: SWOT 分析 -->
      <el-tab-pane label="📋 SWOT 分析" name="swot">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">
                📋 SWOT 分析 {{ current.competitorName ? '— ' + current.competitorName : '' }}
              </span>
              <el-button
                v-if="current.competitiveId"
                type="success"
                size="small"
                :loading="aiLoading"
                @click="triggerAi"
              >
                <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 重新分析
              </el-button>
            </div>
          </template>

          <div v-if="!current.competitiveId" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><DataAnalysis /></el-icon>
            <p>请先在「对比矩阵」选择一个竞品</p>
          </div>

          <div v-else>
            <el-row :gutter="14">
              <el-col :span="12">
                <el-card shadow="hover" class="swot-card swot-s">
                  <template #header><span>💪 优势 (Strengths)</span></template>
                  <div class="swot-content">{{ current.strengths || '(未填,触发 AI 分析自动生成)' }}</div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="hover" class="swot-card swot-w">
                  <template #header><span>📉 劣势 (Weaknesses)</span></template>
                  <div class="swot-content">{{ current.weaknesses || '(未填)' }}</div>
                </el-card>
              </el-col>
            </el-row>
            <el-row :gutter="14" style="margin-top: 14px">
              <el-col :span="12">
                <el-card shadow="hover" class="swot-card swot-o">
                  <template #header><span>🌟 机会 (Opportunities)</span></template>
                  <div class="swot-content">{{ current.opportunities || '(未填)' }}</div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="hover" class="swot-card swot-t">
                  <template #header><span>⚠️ 威胁 (Threats)</span></template>
                  <div class="swot-content">{{ current.threats || '(未填)' }}</div>
                </el-card>
              </el-col>
            </el-row>

            <template v-if="current.aiAnalysisReport">
              <el-divider content-position="left">🤖 AI 综合分析报告</el-divider>
              <div class="markdown-body" v-html="renderedReport" />
            </template>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增/编辑 竞品 Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.competitiveId ? '编辑竞品' : '新增竞品'"
      width="640px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="关联项目" prop="projectId" required>
          <el-select v-model="form.projectId" placeholder="选择关联项目" style="width: 100%">
            <el-option
              v-for="p in projectOptions"
              :key="p.id"
              :label="p.projectName"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="竞品名称" prop="competitorName" required>
          <el-input v-model="form.competitorName" placeholder="如:禅道 / LigaAI / Jira" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="厂商" prop="vendor">
              <el-input v-model="form.vendor" placeholder="如:青岛易软天创" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格档" prop="pricingTier">
              <el-select v-model="form.pricingTier" placeholder="选择" style="width: 100%">
                <el-option label="免费" value="free" />
                <el-option label="中端" value="midrange" />
                <el-option label="企业" value="enterprise" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="官网" prop="website">
          <el-input v-model="form.website" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="价格模型" prop="pricingModel">
          <el-input
            v-model="form.pricingModel"
            type="textarea"
            :rows="2"
            placeholder="如:社区版免费,企业版 2 万/年起"
          />
        </el-form-item>
        <el-form-item label="订阅监控" prop="monitorEnabled">
          <el-switch
            :model-value="form.monitorEnabled === 'Y'"
            @change="form.monitorEnabled = ($event ? 'Y' : 'N')"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item v-if="form.monitorEnabled === 'Y'" label="监控关键词" prop="monitorKeywords">
          <el-input v-model="form.monitorKeywords" placeholder="逗号分隔,如:AI,RAG,Dify" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ form.competitiveId ? '保存修改' : '创建竞品' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick, Bell, DataAnalysis } from '@element-plus/icons-vue'
import {
  listCompetitive, addCompetitive, updateCompetitive, delCompetitive,
  aiAnalyzeCompetitive, getCompetitive, listProjectsForSelect,
  type Competitive, type CompetitiveQuery
} from '@/api/business/competitive'
import { pricingTierLabel, pricingTierTag, statusTagFor } from './competitiveDict'

const activeTab = ref('matrix')
const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)

const emptyForm = (): Competitive => ({
  projectId: 0,
  competitorName: '',
  vendor: '',
  website: '',
  pricingModel: '',
  pricingTier: 'midrange',
  monitorEnabled: 'N',
  monitorKeywords: '',
  authorUserId: 1
})

const form = reactive<Competitive>(emptyForm())
const current = reactive<Competitive>({ projectId: 0, competitorName: '' })

const rules = {
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }],
  competitorName: [{ required: true, message: '请输入竞品名称', trigger: 'blur' }]
}

const list = ref<Competitive[]>([])
const total = ref(0)
const queryParams = reactive<CompetitiveQuery>({ pageNum: 1, pageSize: 10, competitorName: '' })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

const monitoredList = computed(() => list.value.filter(x => x.monitorEnabled === 'Y'))

// === Markdown 渲染 ===
const renderedReport = computed(() => {
  const md = current.aiAnalysisReport || ''
  return md
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/^### (.+)$/gm, '<h4>$1</h4>')
    .replace(/^## (.+)$/gm, '<h3>$1</h3>')
    .replace(/^# (.+)$/gm, '<h2>$1</h2>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>\n?)+/g, m => '<ul>' + m + '</ul>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/^([^<\n].+)$/gm, '<p>$1</p>')
})

// === 列表加载 ===
async function getList() {
  listLoading.value = true
  try {
    const res: any = await listCompetitive(queryParams)
    list.value = res.rows || []
    total.value = res.total || 0
  } finally {
    listLoading.value = false
  }
}

async function loadProjects() {
  try {
    const res: any = await listProjectsForSelect()
    projectOptions.value = res.rows || []
  } catch (e) {
    /* ignore */
  }
}

// === 新增/编辑 ===
function openAdd() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

async function loadCompetitive(row: Competitive) {
  if (!row.competitiveId) return
  const res: any = await getCompetitive(row.competitiveId)
  if (res.code === 200 && res.data) {
    Object.assign(form, res.data)
    Object.assign(current, res.data)
    dialogVisible.value = true
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.competitiveId) {
      await updateCompetitive(form)
      ElMessage.success('更新成功')
    } else {
      await addCompetitive(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await getList()
  } catch (e: any) {
    ElMessage.error(e?.msg || '保存失败')
  } finally {
    saving.value = false
  }
}

// === AI 分析 ===
async function triggerAi() {
  if (!current.competitiveId) {
    ElMessage.warning('请先选择一个竞品')
    return
  }
  aiLoading.value = true
  try {
    const res: any = await aiAnalyzeCompetitive(current.competitiveId)
    if (res.code === 200 && res.data) {
      Object.assign(current, res.data)
      ElMessage.success('AI 综合分析报告已生成')
      activeTab.value = 'swot'
      await getList()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || 'AI 分析失败')
  } finally {
    aiLoading.value = false
  }
}

async function quickAi(row: Competitive) {
  if (!row.competitiveId) return
  aiLoading.value = true
  try {
    const res: any = await aiAnalyzeCompetitive(row.competitiveId)
    if (res.code === 200) {
      ElMessage.success(`${row.competitorName} AI 分析完成`)
      Object.assign(current, res.data || {})
      await getList()
    }
  } finally {
    aiLoading.value = false
  }
}

// === 监控订阅 ===
function openSubscribeDialog() {
  ElMessage.info('在「对比矩阵」编辑某竞品,开启「订阅监控」即可')
  activeTab.value = 'matrix'
}

async function toggleMonitor(row: Competitive, target: 'Y' | 'N') {
  if (!row.competitiveId) return
  try {
    await updateCompetitive({ ...row, monitorEnabled: target })
    ElMessage.success(target === 'Y' ? '已订阅' : '已取消订阅')
    await getList()
  } catch (e: any) {
    ElMessage.error(e?.msg || '操作失败')
  }
}

// === 删除 ===
async function handleDelete(row: Competitive) {
  if (!row.competitiveId) return
  await ElMessageBox.confirm(`确认删除竞品 "${row.competitorName}"?`, '提示', { type: 'warning' })
  await delCompetitive(row.competitiveId)
  ElMessage.success('删除成功')
  if (current.competitiveId === row.competitiveId) {
    Object.keys(current).forEach(k => delete (current as any)[k])
  }
  await getList()
}

onMounted(() => {
  getList()
  loadProjects()
})
</script>

<style scoped>
.competitive-page { padding: 20px; }
.page-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 20px;
}
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.comp-tabs { background: #fff; padding: 0 16px; border-radius: 8px; }
.clickable-row { cursor: pointer; }
.empty-state { text-align: center; padding: 60px 20px; color: #6b7280; }
.swot-card { min-height: 140px; }
.swot-s :deep(.el-card__header) { background: #ecfdf5; color: #166534; }
.swot-w :deep(.el-card__header) { background: #fef2f2; color: #991b1b; }
.swot-o :deep(.el-card__header) { background: #eff6ff; color: #1d4ed8; }
.swot-t :deep(.el-card__header) { background: #fef3c7; color: #92400e; }
.swot-content { font-size: 13px; line-height: 1.7; min-height: 60px; color: #374151; }
.markdown-body { line-height: 1.7; padding: 10px 0; font-size: 13px; }
:deep(.markdown-body h2) { font-size: 16px; margin: 12px 0 8px; color: #111827; }
:deep(.markdown-body h3) { font-size: 14px; margin: 10px 0 6px; color: #1f2937; }
:deep(.markdown-body h4) { font-size: 13px; margin: 8px 0 4px; color: #374151; }
:deep(.markdown-body ul) { margin: 6px 0; padding-left: 22px; }
</style>
