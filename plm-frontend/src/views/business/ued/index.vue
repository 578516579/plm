<!--
  UED 设计协同 — PRD §F2.3 + 原型 ued.html
  严格对齐原型: 设计稿版本管理 + AI 评审 + 农业 UI 组件库
-->
<template>
  <div class="app-container ued-page">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">🎨 UED 设计协同</h2>
        <p class="page-subtitle">与 Figma 集成,AI 辅助设计规范检查与标注生成</p>
      </div>
      <el-button
        type="success"
        :loading="aiLoading"
        :disabled="!current.uedId"
        @click="runAiReview"
      >
        <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 设计规范检查
      </el-button>
    </div>

    <!-- 双列 grid2 -->
    <el-row :gutter="20">
      <!-- 左: 设计稿版本管理 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">🎨 设计稿版本管理</span>
              <el-button type="primary" plain size="small" @click="openAdd">
                <el-icon><Plus /></el-icon>&nbsp;新增设计稿
              </el-button>
            </div>
          </template>

          <el-table
            v-loading="listLoading"
            :data="list"
            stripe
            highlight-current-row
            @current-change="onSelect"
          >
            <el-table-column label="设计稿" prop="title" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="title-cell">
                  <strong>{{ row.title }}</strong>
                  <el-tag v-if="row.versionLabel" size="small" type="info">{{ row.versionLabel }}</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTagFor(row.status).type" size="small">
                  {{ statusTagFor(row.status).label }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="评审分" width="90" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.aiReviewScore" :type="scoreTagType(row.aiReviewScore)" size="small">
                  {{ row.aiReviewScore }}
                </el-tag>
                <span v-else class="muted">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="loadUed(row)">编辑</el-button>
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

          <!-- Figma 同步入口 (对齐原型 .upload-btn) -->
          <div class="figma-sync-btn" @click="openFigmaSyncDialog">
            <el-icon><Link /></el-icon>&nbsp;📎 从 Figma 同步设计稿
          </div>
        </el-card>
      </el-col>

      <!-- 右: AI 设计评审报告 -->
      <el-col :span="12">
        <el-card shadow="never" class="review-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">🔍 AI 设计评审报告</span>
              <el-tag v-if="current.aiReviewScore" :type="scoreTagType(current.aiReviewScore)" size="small">
                评审分 {{ current.aiReviewScore }} / 100
              </el-tag>
            </div>
          </template>

          <div v-if="!current.uedId" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><Picture /></el-icon>
            <p>点击左侧设计稿后,使用「AI 设计规范检查」生成评审报告</p>
          </div>

          <div v-else-if="aiLoading" class="loading-state">
            <el-icon class="rotate" :size="36" color="#3b82f6"><Loading /></el-icon>
            <p>AI 正在分析设计规范,预计 5-10 秒……</p>
          </div>

          <div v-else-if="current.aiReviewReport" class="ai-content">
            <el-descriptions :column="2" size="small" border style="margin-bottom: 12px">
              <el-descriptions-item label="设计稿">{{ current.title }}</el-descriptions-item>
              <el-descriptions-item label="版本">{{ current.versionLabel || '-' }}</el-descriptions-item>
              <el-descriptions-item label="Figma">
                <el-link v-if="current.figmaUrl" :href="current.figmaUrl" target="_blank" type="primary">
                  打开
                </el-link>
                <span v-else>-</span>
              </el-descriptions-item>
              <el-descriptions-item label="AI 生成">
                <el-tag size="small" :type="current.aiGenerated === 'Y' ? 'success' : 'info'">
                  {{ current.aiGenerated === 'Y' ? '已生成' : '未生成' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <el-divider content-position="left">📝 评审报告</el-divider>
            <div class="markdown-body" v-html="renderedReport" />

            <template v-if="current.usabilityIssues">
              <el-divider content-position="left">⚠️ 可用性问题</el-divider>
              <el-alert :title="current.usabilityIssues" type="warning" :closable="false" show-icon />
            </template>

            <template v-if="agriTags.length">
              <el-divider content-position="left">🌾 农业 UI 组件标签</el-divider>
              <el-tag
                v-for="tag in agriTags"
                :key="tag"
                size="small"
                effect="plain"
                style="margin-right: 6px"
              >
                {{ tag }}
              </el-tag>
            </template>
          </div>

          <div v-else class="ai-not-yet">
            <el-icon :size="40" color="#f59e0b"><InfoFilled /></el-icon>
            <p>设计稿已加载 (UED {{ current.uedNo }}),点击「AI 设计规范检查」开始评审</p>
            <el-button type="success" :loading="aiLoading" @click="runAiReview">
              <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 规范检查
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 农业 UI 组件库 (对齐原型 §3) -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span class="card-title">🧩 农业 UI 组件库</span>
      </template>
      <div class="agri-components">
        <div
          v-for="c in agriComponents"
          :key="c.tag"
          class="agri-comp-card"
          :class="{ selected: form.agriComponentTags?.includes(c.tag) }"
          @click="toggleComponentTag(c.tag)"
        >
          <div class="agri-comp-icon">{{ c.icon }}</div>
          <div class="agri-comp-label">{{ c.label }}</div>
        </div>
      </div>
      <p class="agri-tip">
        💡 点击勾选可在新建/编辑设计稿时关联农业组件标签
      </p>
    </el-card>

    <!-- 新增/编辑 Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.uedId ? '编辑设计稿' : '新增设计稿'"
      width="640px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联项目" prop="projectId" required>
          <el-select v-model="form.projectId" placeholder="选择项目" style="width: 100%">
            <el-option
              v-for="p in projectOptions"
              :key="p.id"
              :label="p.projectName"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设计稿名称" prop="title" required>
          <el-input v-model="form.title" placeholder="如:灌溉控制台 v2.1 设计稿" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="版本标签" prop="versionLabel">
              <el-input v-model="form.versionLabel" placeholder="如 v2.1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Figma Key" prop="figmaFileKey">
              <el-input v-model="form.figmaFileKey" placeholder="abc123" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Figma URL" prop="figmaUrl">
          <el-input v-model="form.figmaUrl" placeholder="https://www.figma.com/file/..." />
        </el-form-item>
        <el-form-item label="预览图 URL" prop="previewUrl">
          <el-input v-model="form.previewUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="农业组件" prop="agriComponentTags">
          <div class="agri-quick">
            <el-tag
              v-for="c in agriComponents"
              :key="c.tag"
              :type="(form.agriComponentTags || '').includes(c.tag) ? 'success' : 'info'"
              effect="plain"
              style="margin: 4px 4px 4px 0; cursor: pointer"
              @click="toggleComponentTag(c.tag)"
            >
              {{ c.icon }} {{ c.label }}
            </el-tag>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ form.uedId ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Figma 同步 Dialog -->
    <el-dialog v-model="figmaSyncVisible" title="📎 从 Figma 同步设计稿" width="540px">
      <el-form label-width="100px">
        <el-form-item label="Figma URL">
          <el-input v-model="figmaSyncUrl" placeholder="https://www.figma.com/file/abc123/..." />
        </el-form-item>
        <el-form-item label="关联项目">
          <el-select v-model="figmaSyncProject" style="width: 100%">
            <el-option
              v-for="p in projectOptions"
              :key="p.id"
              :label="p.projectName"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="Figma MCP 实接入留待 v0.6,本期 mock 写库,自动填充 figmaFileKey + versionLabel=v1.0"
        />
      </el-form>
      <template #footer>
        <el-button @click="figmaSyncVisible = false">取消</el-button>
        <el-button type="primary" @click="syncFromFigma">✅ 立即同步</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick, Link, Picture, Loading, InfoFilled } from '@element-plus/icons-vue'
import {
  listUed, addUed, updateUed, delUed, aiReviewUed, getUed, listProjectsForSelect,
  type Ued, type UedQuery
} from '@/api/business/ued'

const dialogVisible = ref(false)
const figmaSyncVisible = ref(false)
const figmaSyncUrl = ref('')
const figmaSyncProject = ref<number | null>(null)
const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)

const emptyForm = (): Ued => ({
  projectId: 0,
  title: '',
  versionLabel: 'v1.0',
  figmaUrl: '',
  figmaFileKey: '',
  agriComponentTags: '',
  designerUserId: 1
})

const form = reactive<Ued>(emptyForm())
const current = reactive<Ued>({ projectId: 0, title: '' })

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入设计稿名称', trigger: 'blur' }]
}

const list = ref<Ued[]>([])
const total = ref(0)
const queryParams = reactive<UedQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

// 农业 UI 组件 (对齐原型 §3 5 张卡片)
const agriComponents = [
  { tag: '农情大屏组件', icon: '📊', label: '农情大屏组件' },
  { tag: '移动端农事记录', icon: '📱', label: '移动端农事记录' },
  { tag: 'IoT数据看板', icon: '🌡️', label: 'IoT 数据看板' },
  { tag: '地块地图组件', icon: '🗺️', label: '地块地图组件' },
  { tag: '农作物生长周期', icon: '🌾', label: '农作物生长周期' }
]

const statusMap: Record<string, { label: string; type: any }> = {
  '00': { label: '草稿',   type: 'info' },
  '01': { label: '评审中', type: 'warning' },
  '02': { label: '已确认', type: 'success' },
  '03': { label: '已废弃', type: 'danger' }
}

function statusTagFor(s?: string) {
  return statusMap[s || '00'] || { label: s || '-', type: 'info' }
}

function scoreTagType(s?: number): any {
  if (!s) return 'info'
  if (s >= 90) return 'success'
  if (s >= 80) return 'warning'
  return 'danger'
}

const agriTags = computed(() => (current.agriComponentTags || '').split(',').filter(Boolean))

const renderedReport = computed(() => {
  const md = current.aiReviewReport || ''
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

async function getList() {
  listLoading.value = true
  try {
    const res: any = await listUed(queryParams)
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

function onSelect(row: Ued | null) {
  if (row) Object.assign(current, row)
  else Object.keys(current).forEach(k => delete (current as any)[k])
}

function openAdd() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

async function loadUed(row: Ued) {
  if (!row.uedId) return
  const res: any = await getUed(row.uedId)
  if (res.code === 200 && res.data) {
    Object.assign(form, res.data)
    Object.assign(current, res.data)
    dialogVisible.value = true
  }
}

function toggleComponentTag(tag: string) {
  const tags = (form.agriComponentTags || '').split(',').filter(Boolean)
  const idx = tags.indexOf(tag)
  if (idx >= 0) tags.splice(idx, 1)
  else tags.push(tag)
  form.agriComponentTags = tags.join(',')
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.uedId) {
      await updateUed(form)
      ElMessage.success('更新成功')
    } else {
      await addUed(form)
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

async function runAiReview() {
  if (!current.uedId) {
    ElMessage.warning('请先选择一个设计稿')
    return
  }
  aiLoading.value = true
  try {
    const res: any = await aiReviewUed(current.uedId)
    if (res.code === 200 && res.data) {
      Object.assign(current, res.data)
      ElMessage.success('AI 评审报告已生成')
      await getList()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || 'AI 评审失败')
  } finally {
    aiLoading.value = false
  }
}

async function handleDelete(row: Ued) {
  if (!row.uedId) return
  await ElMessageBox.confirm(`确认删除设计稿 "${row.title}"?`, '提示', { type: 'warning' })
  await delUed(row.uedId)
  ElMessage.success('删除成功')
  if (current.uedId === row.uedId) {
    Object.keys(current).forEach(k => delete (current as any)[k])
  }
  await getList()
}

function openFigmaSyncDialog() {
  figmaSyncUrl.value = ''
  figmaSyncProject.value = projectOptions.value[0]?.id || null
  figmaSyncVisible.value = true
}

async function syncFromFigma() {
  if (!figmaSyncUrl.value || !figmaSyncProject.value) {
    ElMessage.warning('请填写 Figma URL 和关联项目')
    return
  }
  // Mock 同步: 解析 URL 取 file key,写库 + versionLabel=v1.0
  const fileKey = figmaSyncUrl.value.match(/figma\.com\/file\/([^/]+)/)?.[1] || 'unknown'
  try {
    await addUed({
      projectId: figmaSyncProject.value,
      title: `Figma 同步 ${fileKey}`,
      figmaUrl: figmaSyncUrl.value,
      figmaFileKey: fileKey,
      versionLabel: 'v1.0',
      designerUserId: 1
    })
    ElMessage.success('Figma 同步完成 (mock,实接入留待 v0.6)')
    figmaSyncVisible.value = false
    await getList()
  } catch (e: any) {
    ElMessage.error(e?.msg || '同步失败')
  }
}

onMounted(() => {
  getList()
  loadProjects()
})
</script>

<style scoped>
.ued-page { padding: 20px; }
.page-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 20px;
}
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.title-cell { display: flex; gap: 6px; align-items: center; }
.muted { color: #9ca3af; }
.figma-sync-btn {
  margin-top: 14px; padding: 10px 16px; border: 1px dashed #d1d5db; border-radius: 8px;
  text-align: center; cursor: pointer; transition: all .15s; color: #4b5563; font-size: 13px;
}
.figma-sync-btn:hover { border-color: #2d7a4f; color: #2d7a4f; background: #e8f5ee; }
.empty-state, .loading-state, .ai-not-yet {
  text-align: center; padding: 50px 20px; color: #6b7280;
}
.empty-state p, .loading-state p, .ai-not-yet p { margin: 12px 0; }
.rotate { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.ai-content { font-size: 13px; }
.review-card { min-height: 540px; }
.markdown-body { line-height: 1.7; padding: 10px 0; }
:deep(.markdown-body h2) { font-size: 16px; margin: 12px 0 8px; color: #111827; }
:deep(.markdown-body h3) { font-size: 14px; margin: 10px 0 6px; color: #1f2937; }
:deep(.markdown-body h4) { font-size: 13px; margin: 8px 0 4px; color: #374151; }
:deep(.markdown-body ul) { margin: 6px 0; padding-left: 22px; }
.agri-components { display: flex; gap: 12px; flex-wrap: wrap; }
.agri-comp-card {
  width: 130px; padding: 14px 10px; text-align: center;
  border: 1px solid #e5e7eb; border-radius: 8px; cursor: pointer; transition: all .15s;
}
.agri-comp-card:hover { border-color: #2d7a4f; background: #e8f5ee; }
.agri-comp-card.selected { border-color: #2d7a4f; background: #e8f5ee; }
.agri-comp-icon { font-size: 24px; margin-bottom: 5px; }
.agri-comp-label { font-size: 12px; color: #4b5563; }
.agri-tip { color: #6b7280; font-size: 12px; margin-top: 12px; }
.agri-quick { display: flex; flex-wrap: wrap; }
</style>
