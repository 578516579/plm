<!--
  项目立项 — PRD §F1.1 + 原型 inception.html
  严格对齐原型:左卡片立项配置 + 右卡片 AI 生成立项建议书 + 底部历史立项列表
-->
<template>
  <div class="app-container inception-page">
    <!-- 页头 (对齐原型 .ph) -->
    <div class="page-header">
      <div>
        <h2 class="page-title">🚀 项目立项</h2>
        <p class="page-subtitle">AI 辅助立项分析:输入诉求 → 生成立项建议书 + 风险识别</p>
      </div>
      <el-button type="primary" plain @click="resetForm">
        <el-icon><Refresh /></el-icon>&nbsp;重置表单
      </el-button>
    </div>

    <!-- 双列卡片 (对齐原型 .grid2) -->
    <el-row :gutter="20">
      <!-- 左:立项配置 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">⚙️ 立项配置</span>
          </template>
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="110px"
            label-position="right"
          >
            <el-form-item label="项目名称" prop="projectName" required>
              <el-input
                v-model="form.projectName"
                placeholder="如:农业病虫害智能识别与防治决策系统"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>

            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="业务线" prop="businessLine">
                  <el-select v-model="form.businessLine" placeholder="选择业务线" style="width: 100%">
                    <el-option label="植保服务" value="plant_protection" />
                    <el-option label="精准农业" value="precision_farming" />
                    <el-option label="农资流通" value="agri_supply" />
                    <el-option label="质量溯源" value="traceability" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="项目类型" prop="inceptionType">
                  <el-select v-model="form.inceptionType" placeholder="选择类型" style="width: 100%">
                    <el-option label="新产品研发" value="new_product" />
                    <el-option label="版本迭代" value="iteration" />
                    <el-option label="技术重构" value="refactor" />
                    <el-option label="平台建设" value="platform" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="背景与诉求" prop="background">
              <el-input
                v-model="form.background"
                type="textarea"
                :rows="5"
                placeholder="描述项目背景、市场痛点、目标用户、期望成果……"
                maxlength="2000"
                show-word-limit
              />
            </el-form-item>

            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="预计工期(月)" prop="estimatedDurationMonths">
                  <el-input-number
                    v-model="form.estimatedDurationMonths"
                    :min="1"
                    :max="60"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="预计团队" prop="estimatedTeam">
                  <el-input
                    v-model="form.estimatedTeam"
                    placeholder="如:产品×1 前端×2 后端×3 测试×2 AI×2"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item>
              <div class="btn-row">
                <el-button
                  type="primary"
                  :loading="saving"
                  @click="handleSubmit(false)"
                >
                  <el-icon><DocumentAdd /></el-icon>&nbsp;保存草稿
                </el-button>
                <AiButton
                  :loading="saving || aiLoading"
                  @click="handleSubmit(true)"
                >保存并 AI 分析</AiButton>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右:AI 立项建议书预览 -->
      <el-col :span="12">
        <el-card shadow="never" class="ai-output-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">📋 立项建议书 (AI 生成)</span>
              <el-tag :type="statusTag.type" size="small">{{ statusTag.label }}</el-tag>
            </div>
          </template>

          <!-- 空状态 -->
          <div v-if="!current.inceptionId" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><Document /></el-icon>
            <p>填写左侧配置后点击「保存并 AI 分析」生成立项建议书</p>
          </div>

          <!-- AI 生成进度 -->
          <div v-else-if="aiLoading" class="loading-state">
            <el-icon class="rotate" :size="36" color="#3b82f6"><Loading /></el-icon>
            <p>AI 正在分析中,预计 5-10 秒……</p>
          </div>

          <!-- 已生成 -->
          <div v-else-if="current.aiProposalContent" class="ai-content">
            <el-descriptions :column="2" size="small" border style="margin-bottom: 12px">
              <el-descriptions-item label="立项编号">
                <code>{{ current.inceptionNo }}</code>
              </el-descriptions-item>
              <el-descriptions-item label="AI 生成">
                <el-tag size="small" :type="current.aiGenerated === 'Y' ? 'success' : 'info'">
                  {{ current.aiGenerated === 'Y' ? '已生成' : '未生成' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <el-divider content-position="left">📝 立项建议书</el-divider>
            <div class="markdown-body" v-html="renderedProposal" />

            <template v-if="current.aiRisks">
              <el-divider content-position="left">⚠️ 风险识别</el-divider>
              <el-alert
                :title="current.aiRisks"
                type="warning"
                :closable="false"
                show-icon
              />
            </template>
          </div>

          <!-- 仅保存草稿,未 AI -->
          <div v-else class="ai-not-yet">
            <el-icon :size="40" color="#f59e0b"><InfoFilled /></el-icon>
            <p>草稿已保存 (编号 {{ current.inceptionNo }}),点击下方按钮触发 AI 分析</p>
            <AiButton :loading="aiLoading" @click="triggerAi">AI 分析并生成</AiButton>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 历史立项列表 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">📚 历史立项 ({{ total }})</span>
          <div>
            <el-input
              v-model="queryParams.projectName"
              placeholder="搜索项目名称"
              style="width: 240px; margin-right: 8px"
              clearable
              @clear="getList"
              @keyup.enter="getList"
            />
            <el-button :icon="Search" @click="getList">查询</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="inceptionNo" width="160" />
        <el-table-column label="项目名称" prop="projectName" min-width="200" show-overflow-tooltip />
        <el-table-column label="业务线" width="100" align="center">
          <template #default="{ row }">{{ businessLineLabel(row.businessLine) }}</template>
        </el-table-column>
        <el-table-column label="项目类型" width="110" align="center">
          <template #default="{ row }">{{ inceptionTypeLabel(row.inceptionType) }}</template>
        </el-table-column>
        <el-table-column label="工期(月)" prop="estimatedDurationMonths" width="80" align="center" />
        <el-table-column label="AI" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.aiGenerated === 'Y'" type="success" size="small">已生成</el-tag>
            <el-tag v-else type="info" size="small">未生成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadInception(row)">载入编辑</el-button>
            <el-button link type="success" @click="quickAi(row)" :disabled="row.aiGenerated === 'Y'">AI 分析</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh, DocumentAdd, Document, Loading, InfoFilled, Search
} from '@element-plus/icons-vue'
import AiButton from '@/components/AiButton/index.vue'
import {
  listInception, addInception, updateInception, delInception,
  aiGenerateInception, getInception, type Inception, type InceptionQuery
} from '@/api/business/inception'
import { businessLineLabel, inceptionTypeLabel, statusTagFor } from './inceptionDict'

const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)

const emptyForm = (): Inception => ({
  projectName: '',
  businessLine: 'precision_farming',
  inceptionType: 'new_product',
  background: '',
  estimatedDurationMonths: 6,
  estimatedTeam: '',
  submitterUserId: 1
})

const form = reactive<Inception>(emptyForm())
const current = reactive<Inception>({ projectName: '' })

const rules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
}

const list = ref<Inception[]>([])
const total = ref(0)
const queryParams = reactive<InceptionQuery>({ pageNum: 1, pageSize: 10, projectName: '' })

const statusTag = computed(() => statusTagFor(current.status))

// === 简化 Markdown 渲染 (避免引入大依赖) ===
const renderedProposal = computed(() => {
  const md = current.aiProposalContent || ''
  return md
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
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
    const res: any = await listInception(queryParams)
    list.value = res.rows || []
    total.value = res.total || 0
  } finally {
    listLoading.value = false
  }
}

// === 保存 + AI ===
async function handleSubmit(triggerAiAfter: boolean) {
  await formRef.value.validate()
  saving.value = true
  try {
    if (current.inceptionId) {
      await updateInception({ ...form, inceptionId: current.inceptionId })
      ElMessage.success('更新成功')
    } else {
      const res: any = await addInception(form)
      if (res.code === 200) {
        ElMessage.success('保存成功')
        // 重新查列表,拿最新一条做 current
        await getList()
        const latest = list.value.find(x => x.projectName === form.projectName)
        if (latest?.inceptionId) Object.assign(current, latest)
      }
    }
    if (triggerAiAfter && current.inceptionId) {
      await triggerAi()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function triggerAi() {
  if (!current.inceptionId) {
    ElMessage.warning('请先保存草稿')
    return
  }
  aiLoading.value = true
  try {
    const res: any = await aiGenerateInception(current.inceptionId)
    if (res.code === 200 && res.data) {
      Object.assign(current, res.data)
      ElMessage.success('AI 立项建议书已生成')
      await getList()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || 'AI 生成失败')
  } finally {
    aiLoading.value = false
  }
}

async function quickAi(row: Inception) {
  if (!row.inceptionId) return
  aiLoading.value = true
  try {
    const res: any = await aiGenerateInception(row.inceptionId)
    if (res.code === 200) {
      ElMessage.success(`${row.inceptionNo} AI 分析完成`)
      await getList()
    }
  } finally {
    aiLoading.value = false
  }
}

async function loadInception(row: Inception) {
  if (!row.inceptionId) return
  const res: any = await getInception(row.inceptionId)
  if (res.code === 200 && res.data) {
    Object.assign(form, {
      projectName: res.data.projectName,
      businessLine: res.data.businessLine,
      inceptionType: res.data.inceptionType,
      background: res.data.background,
      estimatedDurationMonths: res.data.estimatedDurationMonths,
      estimatedTeam: res.data.estimatedTeam,
      submitterUserId: res.data.submitterUserId || 1
    })
    Object.assign(current, res.data)
    ElMessage.info(`已载入 ${res.data.inceptionNo}`)
  }
}

async function handleDelete(row: Inception) {
  if (!row.inceptionId) return
  await ElMessageBox.confirm(`确认删除立项 "${row.inceptionNo}"?`, '提示', {
    type: 'warning'
  })
  await delInception(row.inceptionId)
  ElMessage.success('删除成功')
  if (current.inceptionId === row.inceptionId) resetForm()
  await getList()
}

function resetForm() {
  Object.assign(form, emptyForm())
  Object.keys(current).forEach(k => delete (current as any)[k])
  formRef.value?.clearValidate()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.inception-page { padding: 20px; }
.page-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 20px;
}
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.btn-row { display: flex; gap: 10px; }
.empty-state, .loading-state, .ai-not-yet {
  text-align: center; padding: 60px 20px; color: #6b7280;
}
.empty-state p, .loading-state p, .ai-not-yet p { margin: 12px 0; }
.rotate { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.ai-content { font-size: 13px; }
.markdown-body { line-height: 1.7; padding: 10px 0; }
:deep(.markdown-body h2) { font-size: 16px; margin: 12px 0 8px; color: #111827; }
:deep(.markdown-body h3) { font-size: 14px; margin: 10px 0 6px; color: #1f2937; }
:deep(.markdown-body h4) { font-size: 13px; margin: 8px 0 4px; color: #374151; }
:deep(.markdown-body p)  { margin: 6px 0; }
:deep(.markdown-body ul) { margin: 6px 0; padding-left: 22px; }
:deep(.markdown-body li) { margin: 2px 0; }
.ai-output-card { min-height: 540px; }
code {
  background: #f4f4f5; padding: 2px 6px; border-radius: 3px;
  font-family: 'Consolas', 'Monaco', monospace; font-size: 12px;
}
</style>
