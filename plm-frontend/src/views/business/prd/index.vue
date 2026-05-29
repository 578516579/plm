<!--
  AI PRD 生成器 — PRD §F2.2 + 原型 prd.html
  严格对齐:左卡片需求输入 + 左下 AI 进度时间线 + 右卡片 PRD 预览 (完整度徽章) + 底部我的 PRD 列表
-->
<template>
  <div class="app-container prd-page">
    <!-- 顶部 (对齐原型 .ph) -->
    <div class="page-header">
      <div>
        <h2 class="page-title">📄 AI PRD 生成器</h2>
        <p class="page-subtitle">输入需求描述,AI 基于 AgriKB 知识库自动生成完整 PRD</p>
      </div>
      <el-button plain @click="showHistory = true">
        <el-icon><Folder /></el-icon>&nbsp;我的 PRD ({{ total }})
      </el-button>
    </div>

    <!-- 双列卡片 -->
    <el-row :gutter="20">
      <!-- 左列 -->
      <el-col :span="12">
        <!-- 上:需求输入 -->
        <el-card shadow="never">
          <template #header>
            <span class="card-title">✍️ 需求输入</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
            <el-form-item label="关联项目" prop="projectId">
              <el-select
                v-model="form.projectId"
                placeholder="选择项目"
                style="width: 100%"
                filterable
                @change="onProjectChange"
              >
                <el-option
                  v-for="p in projects"
                  :key="p.id"
                  :label="p.projectName"
                  :value="p.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="关联需求">
              <el-select
                v-model="form.requirementId"
                placeholder="可选 — 关联的源需求"
                style="width: 100%"
                clearable
                filterable
                :disabled="!form.projectId"
              >
                <el-option
                  v-for="r in requirements"
                  :key="r.requirementId"
                  :label="`${r.requirementNo} - ${r.title}`"
                  :value="r.requirementId"
                />
              </el-select>
              <div v-if="!form.projectId" class="form-hint">请先选择项目以加载该项目下的需求</div>
            </el-form-item>

            <el-form-item label="功能名称" prop="title">
              <el-input v-model="form.title" placeholder="如:AI 灌溉推荐引擎" maxlength="200" show-word-limit />
            </el-form-item>

            <el-form-item label="需求描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="4"
                placeholder="自然语言描述需求,AI 将基于此自动结构化生成 PRD……"
                maxlength="2000"
                show-word-limit
              />
            </el-form-item>

            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="业务场景">
                  <el-select v-model="form.sceneTemplate" placeholder="选择场景" style="width: 100%">
                    <el-option label="精准灌溉管理" value="irrigation" />
                    <el-option label="农资销售" value="agri_sales" />
                    <el-option label="病虫害防治" value="pest_control" />
                    <el-option label="农产品溯源" value="traceability" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="目标用户">
                  <el-select v-model="form.targetUser" placeholder="选择用户" style="width: 100%">
                    <el-option label="农场主/种植户" value="farmer" />
                    <el-option label="农技人员" value="agronomist" />
                    <el-option label="企业管理员" value="admin" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="版本" prop="version">
              <el-input v-model="form.version" placeholder="v1.0" style="width: 200px" />
            </el-form-item>

            <el-form-item>
              <div class="btn-row">
                <el-button type="primary" :loading="saving" @click="handleSubmit(false)">
                  <el-icon><DocumentAdd /></el-icon>&nbsp;保存草稿
                </el-button>
                <el-button type="success" :loading="saving || aiLoading" @click="handleSubmit(true)">
                  <el-icon><MagicStick /></el-icon>&nbsp;AI 生成完整 PRD
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 下:AI 进度 (生成中显示) -->
        <el-card v-if="aiLoading || aiSteps.length" shadow="never" style="margin-top: 12px">
          <template #header>
            <span class="card-title">🤖 AI 生成进度</span>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(step, i) in aiSteps"
              :key="i"
              :type="step.done ? 'success' : 'primary'"
              :hollow="!step.done"
            >
              {{ step.label }}
              <el-icon v-if="!step.done && step.active" class="rotate"><Loading /></el-icon>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <!-- 右:PRD 预览 -->
      <el-col :span="12">
        <el-card shadow="never" class="prd-preview-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">📄 PRD 预览</span>
              <el-tag :type="completenessTag.type" size="small">{{ completenessTag.label }}</el-tag>
            </div>
          </template>

          <!-- 空状态 -->
          <div v-if="!current.prdId" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><Document /></el-icon>
            <p>填写左侧需求 → 点击「AI 生成完整 PRD」</p>
            <p class="hint">预计生成时间 1-2 分钟</p>
          </div>

          <!-- AI 生成中 -->
          <div v-else-if="aiLoading" class="loading-state">
            <el-icon class="rotate" :size="36" color="#3b82f6"><Loading /></el-icon>
            <p>AI 正在生成 PRD,正在调用知识库……</p>
          </div>

          <!-- 已生成 -->
          <div v-else-if="current.content" class="prd-content">
            <el-descriptions :column="2" size="small" border style="margin-bottom: 12px">
              <el-descriptions-item label="编号">
                <code>{{ current.prdNo }}</code>
              </el-descriptions-item>
              <el-descriptions-item label="版本">
                <el-tag size="small">{{ current.version || 'v1.0' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="完整度">
                <el-progress
                  :percentage="current.completenessScore || 0"
                  :stroke-width="14"
                  :format="(p: number) => `${p.toFixed(0)}%`"
                  :status="(current.completenessScore || 0) >= 80 ? 'success' : 'warning'"
                />
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="statusTagFor(current.status).type" size="small">
                  {{ statusTagFor(current.status).label }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <el-divider content-position="left">📝 PRD 正文</el-divider>
            <div class="markdown-body" v-html="renderedContent" />

            <el-divider />
            <div class="action-row">
              <el-button size="small" @click="copyPrd">
                <el-icon><CopyDocument /></el-icon>&nbsp;复制全文
              </el-button>
              <el-button size="small" type="primary" @click="downloadPrd">
                <el-icon><Download /></el-icon>&nbsp;导出 Markdown
              </el-button>
              <el-button size="small" type="success" :disabled="current.status !== '00'" @click="submitForReview">
                <el-icon><Promotion /></el-icon>&nbsp;提交评审
              </el-button>
            </div>
          </div>

          <!-- 仅保存草稿,未 AI 生成 -->
          <div v-else class="prd-not-yet">
            <el-icon :size="40" color="#f59e0b"><InfoFilled /></el-icon>
            <p>草稿已保存 (编号 {{ current.prdNo }}),点击下方按钮触发 AI 生成</p>
            <el-button type="primary" :loading="aiLoading" @click="triggerAi">
              <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成完整 PRD
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 我的 PRD 弹窗 -->
    <el-dialog v-model="showHistory" title="📂 我的 PRD" width="900">
      <el-input
        v-model="queryParams.title"
        placeholder="搜索功能名称"
        clearable
        @clear="getList"
        @keyup.enter="getList"
        style="margin-bottom: 12px"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-table v-loading="listLoading" :data="list" stripe @row-click="(row: any) => { loadPrd(row); showHistory = false }">
        <el-table-column label="编号" prop="prdNo" width="160" />
        <el-table-column label="功能名称" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="版本" prop="version" width="100" align="center" />
        <el-table-column label="完整度" width="140" align="center">
          <template #default="{ row }">
            <el-progress
              v-if="row.completenessScore != null"
              :percentage="row.completenessScore"
              :stroke-width="8"
              :show-text="false"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
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
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Folder, DocumentAdd, MagicStick, Document, Loading, InfoFilled,
  CopyDocument, Download, Promotion, Search
} from '@element-plus/icons-vue'
import {
  listPrd, getPrd, addPrd, updatePrd, delPrd, aiGeneratePrd, listProjectsForSelect,
  listRequirementsForSelect,
  type Prd, type PrdQuery
} from '@/api/business/prd'
import { statusTagFor } from './prdDict'

const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)
const showHistory = ref(false)

const emptyForm = (): Prd => ({
  title: '',
  description: '',
  sceneTemplate: 'irrigation',
  targetUser: 'farmer',
  version: 'v1.0',
  authorUserId: 1
})

const form = reactive<Prd>(emptyForm())
const current = reactive<Prd>({ title: '' })
const projects = ref<any[]>([])
const requirements = ref<any[]>([])  // 2026-05-25 新增 — 关联需求下拉
const list = ref<Prd[]>([])
const total = ref(0)
const queryParams = reactive<PrdQuery>({ pageNum: 1, pageSize: 10, title: '' })

const rules = {
  title: [{ required: true, message: '请输入功能名称', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }]
}

const aiSteps = ref<{ label: string; done: boolean; active: boolean }[]>([])

const completenessTag = computed(() => {
  const score = current.completenessScore
  if (score == null) return { label: '等待生成', type: 'info' as const }
  if (score >= 80) return { label: `${score.toFixed(0)}% 完整`, type: 'success' as const }
  if (score >= 50) return { label: `${score.toFixed(0)}% 部分`, type: 'warning' as const }
  return { label: `${score.toFixed(0)}% 缺失`, type: 'danger' as const }
})

// 简易 Markdown 渲染
const renderedContent = computed(() => {
  const md = current.content || ''
  return md
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/^### (.+)$/gm, '<h4>$1</h4>')
    .replace(/^## (.+)$/gm, '<h3>$1</h3>')
    .replace(/^# (.+)$/gm, '<h2>$1</h2>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>\n?)+/g, m => '<ul>' + m + '</ul>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/^([^<\n].+)$/gm, '<p>$1</p>')
})

// AI 进度 mock (前端展示动画;真实进度由后端 SSE 推送,本期占位)
async function simulateAiProgress() {
  const steps = [
    '🔍 检索 AgriKB 知识库……',
    '📝 生成背景与目标段……',
    '👥 提取用户故事……',
    '⚙️ 整理功能描述……',
    '📐 编写非功能需求……',
    '✅ 整理验收标准……',
    '🎨 推荐原型说明……'
  ]
  aiSteps.value = steps.map(s => ({ label: s, done: false, active: false }))
  for (let i = 0; i < steps.length; i++) {
    aiSteps.value[i].active = true
    await new Promise(r => setTimeout(r, 250))
    aiSteps.value[i].done = true
    aiSteps.value[i].active = false
  }
}

async function getList() {
  listLoading.value = true
  try {
    const res: any = await listPrd(queryParams)
    list.value = res.rows || []
    total.value = res.total || 0
  } finally {
    listLoading.value = false
  }
}

async function loadProjects() {
  try {
    const res: any = await listProjectsForSelect()
    projects.value = res.rows || []
  } catch { /* 项目列表加载失败不阻塞 */ }
}

// 2026-05-25 新增 — 项目切换时刷新需求下拉
async function loadRequirements(projectId?: number) {
  if (!projectId) { requirements.value = []; return }
  try {
    const res: any = await listRequirementsForSelect(projectId)
    requirements.value = res.rows || []
  } catch { requirements.value = [] }
}

function onProjectChange(projectId?: number) {
  form.requirementId = undefined
  loadRequirements(projectId)
}

async function handleSubmit(triggerAiAfter: boolean) {
  await formRef.value.validate()
  saving.value = true
  try {
    if (current.prdId) {
      await updatePrd({ ...form, prdId: current.prdId })
      ElMessage.success('更新成功')
    } else {
      const res: any = await addPrd(form)
      if (res.code === 200) {
        ElMessage.success('保存成功')
        await getList()
        const latest = list.value.find(x => x.title === form.title && x.version === form.version)
        if (latest?.prdId) Object.assign(current, latest)
      }
    }
    if (triggerAiAfter && current.prdId) {
      await triggerAi()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function triggerAi() {
  if (!current.prdId) {
    ElMessage.warning('请先保存草稿')
    return
  }
  aiLoading.value = true
  const progressTask = simulateAiProgress()
  try {
    const [res] = await Promise.all([aiGeneratePrd(current.prdId), progressTask])
    if (res.code === 200 && res.data) {
      Object.assign(current, res.data)
      ElMessage.success(`PRD 已生成 — 完整度 ${(res.data.completenessScore || 0).toFixed(0)}%`)
      await getList()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || 'AI 生成失败')
  } finally {
    aiLoading.value = false
  }
}

async function loadPrd(row: Prd) {
  if (!row.prdId) return
  const res: any = await getPrd(row.prdId)
  if (res.code === 200 && res.data) {
    Object.assign(form, {
      projectId: res.data.projectId,
      requirementId: res.data.requirementId,    // 2026-05-25 加载关联需求
      title: res.data.title,
      description: res.data.description,
      sceneTemplate: res.data.sceneTemplate,
      targetUser: res.data.targetUser,
      version: res.data.version,
      authorUserId: res.data.authorUserId || 1
    })
    Object.assign(current, res.data)
    // 同步加载该 PRD 关联项目下的需求清单(便于查看/切换关联需求)
    if (res.data.projectId) await loadRequirements(res.data.projectId)
    ElMessage.info(`已载入 ${res.data.prdNo}`)
  }
}

async function handleDelete(row: Prd) {
  if (!row.prdId) return
  await ElMessageBox.confirm(`确认删除 PRD "${row.prdNo}"?`, '提示', { type: 'warning' })
  await delPrd(row.prdId)
  ElMessage.success('删除成功')
  if (current.prdId === row.prdId) {
    Object.keys(current).forEach(k => delete (current as any)[k])
  }
  await getList()
}

async function submitForReview() {
  if (!current.prdId) return
  await updatePrd({ prdId: current.prdId, title: current.title, status: '01' })
  Object.assign(current, { status: '01' })
  ElMessage.success('已提交评审')
  await getList()
}

function copyPrd() {
  navigator.clipboard.writeText(current.content || '')
  ElMessage.success('已复制到剪贴板')
}

function downloadPrd() {
  const blob = new Blob([current.content || ''], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${current.prdNo || 'PRD'}-${current.version || 'v1.0'}.md`
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  await loadProjects()
  await getList()
})
</script>

<style scoped>
.prd-page { padding: 20px; }
.form-hint { font-size: 11px; color: #94a3b8; margin-top: 4px; line-height: 1.4; }
.page-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 20px;
}
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.btn-row { display: flex; gap: 10px; }
.empty-state, .loading-state, .prd-not-yet {
  text-align: center; padding: 60px 20px; color: #6b7280;
}
.empty-state p, .loading-state p, .prd-not-yet p { margin: 8px 0; }
.empty-state .hint { font-size: 12px; color: #9ca3af; }
.rotate { animation: spin 1s linear infinite; vertical-align: middle; }
@keyframes spin { to { transform: rotate(360deg); } }
.prd-preview-card { min-height: 540px; }
.prd-content { font-size: 13px; }
.markdown-body { line-height: 1.7; padding: 10px 0; max-height: 480px; overflow-y: auto; }
:deep(.markdown-body h2) { font-size: 16px; margin: 12px 0 8px; color: #111827; }
:deep(.markdown-body h3) { font-size: 14px; margin: 10px 0 6px; color: #1f2937; }
:deep(.markdown-body h4) { font-size: 13px; margin: 8px 0 4px; color: #374151; }
:deep(.markdown-body p)  { margin: 6px 0; }
:deep(.markdown-body ul) { margin: 6px 0; padding-left: 22px; }
:deep(.markdown-body li) { margin: 2px 0; }
:deep(.markdown-body code) {
  background: #f4f4f5; padding: 1px 4px; border-radius: 3px;
  font-family: 'Consolas', monospace; font-size: 12px;
}
.action-row { display: flex; gap: 8px; flex-wrap: wrap; }
code {
  background: #f4f4f5; padding: 2px 6px; border-radius: 3px;
  font-family: 'Consolas', monospace; font-size: 12px;
}
</style>
