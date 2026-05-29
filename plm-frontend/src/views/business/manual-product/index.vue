<!--
  产品手册 — PRD §F5.1 + 原型 productmanual.html
  左卡生成配置 (产品版本/5 包含模块/截图上传) + 右卡 AI 生成手册预览
-->
<template>
  <div class="app-container mp-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📖 产品手册</h2>
        <p class="page-subtitle">AI 一键生成,支持截图上传自动描述,多格式导出</p>
      </div>
      <el-button type="success" :loading="aiLoading" :disabled="!current.manualproductId" @click="triggerAi">
        <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成产品手册
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">⚙️ 生成配置</span></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-form-item label="关联项目" prop="projectId" required>
              <el-select v-model="form.projectId" style="width: 100%">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="手册名称" prop="title" required>
              <el-input v-model="form.title" placeholder="如:智慧灌溉决策平台 v2.1 产品手册" />
            </el-form-item>
            <el-form-item label="产品版本" prop="productVersion">
              <el-input v-model="form.productVersion" placeholder="v2.1" />
            </el-form-item>
            <el-form-item label="包含模块">
              <div class="modules-checklist">
                <el-checkbox v-model="modOverview">系统概述与功能清单</el-checkbox>
                <el-checkbox v-model="modQuickStart">快速上手指南</el-checkbox>
                <el-checkbox v-model="modFeatureDetail">功能详细说明</el-checkbox>
                <el-checkbox v-model="modFaq">常见问题 FAQ</el-checkbox>
                <el-checkbox v-model="modVideo">视频教程链接</el-checkbox>
              </div>
            </el-form-item>
            <el-form-item label="界面截图">
              <div class="upload-area" @click="openScreenshotUpload">
                <el-icon :size="22" color="#9ca3af"><Picture /></el-icon>
                &nbsp;📸 上传截图 (支持批量,AI 自动生成说明)
              </div>
              <div v-if="screenshotCount" class="screenshot-info">
                已上传 {{ screenshotCount }} 张截图
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSubmit(false)">保存草稿</el-button>
              <el-button type="success" :loading="saving || aiLoading" @click="handleSubmit(true)">
                <el-icon><MagicStick /></el-icon>&nbsp;✨ 开始生成
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="preview-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">📖 手册预览</span>
              <el-tag :type="statusTag.type" size="small">{{ statusTag.label }}</el-tag>
            </div>
          </template>
          <div v-if="!current.manualproductId" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><Notebook /></el-icon>
            <p>选择模块后点击「开始生成」</p>
          </div>
          <div v-else-if="aiLoading" class="loading-state">
            <el-icon class="rotate" :size="36" color="#3b82f6"><Loading /></el-icon>
            <p>AI 正在生成产品手册,预计 10-25 秒……</p>
          </div>
          <div v-else-if="current.content" class="manual-content">
            <el-descriptions :column="2" size="small" border style="margin-bottom: 12px">
              <el-descriptions-item label="编号"><code>{{ current.manualproductNo }}</code></el-descriptions-item>
              <el-descriptions-item label="版本">{{ current.productVersion || '-' }}</el-descriptions-item>
            </el-descriptions>
            <el-divider content-position="left">📝 内容</el-divider>
            <div class="markdown-body" v-html="renderedManual" />
            <el-divider />
            <strong>导出:</strong>
            <el-button-group style="margin-top: 8px">
              <el-button size="small" @click="exportFormat('pdf')">📄 PDF</el-button>
              <el-button size="small" @click="exportFormat('docx')">📝 Word</el-button>
              <el-button size="small" @click="exportFormat('html')">🌐 HTML</el-button>
            </el-button-group>
          </div>
          <div v-else class="ai-not-yet">
            <el-icon :size="40" color="#f59e0b"><InfoFilled /></el-icon>
            <p>草稿已保存 ({{ current.manualproductNo }}),点击生成</p>
            <el-button type="success" :loading="aiLoading" @click="triggerAi">✨ 立即生成</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header><span class="card-title">📚 历史产品手册 ({{ total }})</span></template>
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="manualproductNo" width="160" />
        <el-table-column label="手册名" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="版本" prop="productVersion" width="100" align="center" />
        <el-table-column label="AI" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.aiGenerated === 'Y'" type="success" size="small">已生成</el-tag>
            <el-tag v-else type="info" size="small">未生成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadM(row)">载入</el-button>
            <el-button link type="success" @click="quickAi(row)">AI</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 截图上传 Dialog -->
    <el-dialog v-model="uploadVisible" title="📸 截图上传 (AI 自动生成说明)" width="520px">
      <el-upload
        v-model:file-list="screenshotList"
        action="#"
        :auto-upload="false"
        list-type="picture-card"
        multiple
        :on-change="onScreenshotChange"
      >
        <el-icon><Plus /></el-icon>
      </el-upload>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="本期 mock: 仅记录截图数量,实际 AI 图像理解留待 v0.6 接入 GPT-4V / Claude Vision"
        style="margin-top: 14px"
      />
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmScreenshots">确认 ({{ screenshotList.length }} 张)</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Picture, Notebook, Loading, InfoFilled, Plus } from '@element-plus/icons-vue'
import {
  listManualProduct, addManualProduct, updateManualProduct, delManualProduct,
  aiGenerateManualProduct, getManualProduct, listProjectsForSelect,
  type ManualProduct, type ManualProductQuery
} from '@/api/business/manual-product'
import { statusTagFor } from './manualProductDict'

const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)
const uploadVisible = ref(false)
const screenshotList = ref<any[]>([])

// 5 包含模块 checkbox 独立绑定 → 合成 includeModules CSV
const modOverview = ref(true)
const modQuickStart = ref(true)
const modFeatureDetail = ref(true)
const modFaq = ref(true)
const modVideo = ref(false)

const emptyForm = (): ManualProduct => ({
  projectId: 0, title: '', productVersion: 'v1.0',
  includeModules: 'overview,quickstart,feature_detail,faq',
  outputFormats: 'word,pdf', authorUserId: 1
})
const form = reactive<ManualProduct>(emptyForm())
const current = reactive<ManualProduct>({ title: '' })
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

watch([modOverview, modQuickStart, modFeatureDetail, modFaq, modVideo], () => {
  form.includeModules = [
    modOverview.value && 'overview',
    modQuickStart.value && 'quickstart',
    modFeatureDetail.value && 'feature_detail',
    modFaq.value && 'faq',
    modVideo.value && 'video'
  ].filter(Boolean).join(',')
}, { immediate: true })

const list = ref<ManualProduct[]>([])
const total = ref(0)
const queryParams = reactive<ManualProductQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

const screenshotCount = computed(() => (form.screenshotsUrls || '').split(',').filter(Boolean).length)

const statusTag = computed(() => statusTagFor(current.status))

const renderedManual = computed(() => {
  const md = current.content || ''
  return md
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/^### (.+)$/gm, '<h4>$1</h4>')
    .replace(/^## (.+)$/gm, '<h3>$1</h3>')
    .replace(/^# (.+)$/gm, '<h2>$1</h2>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>\n?)+/g, m => '<ul>' + m + '</ul>')
    .replace(/\n\n/g, '</p><p>').replace(/^([^<\n].+)$/gm, '<p>$1</p>')
})

async function getList() {
  listLoading.value = true
  try { const res: any = await listManualProduct(queryParams); list.value = res.rows || []; total.value = res.total || 0 } finally { listLoading.value = false }
}
async function loadProjects() {
  try { const res: any = await listProjectsForSelect(); projectOptions.value = res.rows || [] } catch {}
}

async function handleSubmit(triggerAfter: boolean) {
  await formRef.value.validate()
  saving.value = true
  try {
    if (current.manualproductId) { await updateManualProduct({ ...form, manualproductId: current.manualproductId }); ElMessage.success('更新成功') }
    else {
      await addManualProduct(form); ElMessage.success('保存成功'); await getList()
      const latest = list.value.find(x => x.title === form.title)
      if (latest?.manualproductId) Object.assign(current, latest)
    }
    if (triggerAfter && current.manualproductId) await triggerAi()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') } finally { saving.value = false }
}

async function triggerAi() {
  if (!current.manualproductId) return
  aiLoading.value = true
  try {
    const res: any = await aiGenerateManualProduct(current.manualproductId)
    if (res.code === 200 && res.data) { Object.assign(current, res.data); ElMessage.success('产品手册已生成'); await getList() }
  } catch (e: any) { ElMessage.error(e?.msg || 'AI 失败') } finally { aiLoading.value = false }
}

async function quickAi(row: ManualProduct) {
  if (!row.manualproductId) return
  aiLoading.value = true
  try { await aiGenerateManualProduct(row.manualproductId); ElMessage.success('已生成'); await getList() } finally { aiLoading.value = false }
}

async function loadM(row: ManualProduct) {
  if (!row.manualproductId) return
  const res: any = await getManualProduct(row.manualproductId)
  if (res.code === 200 && res.data) {
    Object.assign(form, res.data); Object.assign(current, res.data)
    const mods = (res.data.includeModules || '').split(',')
    modOverview.value = mods.includes('overview'); modQuickStart.value = mods.includes('quickstart')
    modFeatureDetail.value = mods.includes('feature_detail'); modFaq.value = mods.includes('faq')
    modVideo.value = mods.includes('video')
    ElMessage.info(`已载入 ${res.data.manualproductNo}`)
  }
}

async function handleDelete(row: ManualProduct) {
  if (!row.manualproductId) return
  await ElMessageBox.confirm(`删除 "${row.manualproductNo}"?`, '提示', { type: 'warning' })
  await delManualProduct(row.manualproductId); ElMessage.success('删除成功')
  if (current.manualproductId === row.manualproductId) Object.keys(current).forEach(k => delete (current as any)[k])
  await getList()
}

function openScreenshotUpload() {
  uploadVisible.value = true
}

function onScreenshotChange(file: any) {
  // Mock: 仅记录文件名
  if (file && file.name) {
    const urls = (form.screenshotsUrls || '').split(',').filter(Boolean)
    urls.push(file.name)
    form.screenshotsUrls = urls.join(',')
  }
}

function confirmScreenshots() {
  form.screenshotsUrls = screenshotList.value.map(x => x.name || x.url || '').filter(Boolean).join(',')
  uploadVisible.value = false
  ElMessage.success(`已记录 ${screenshotList.value.length} 张截图`)
}

function exportFormat(fmt: string) {
  if (!current.content) return
  const blob = new Blob([current.content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${current.manualproductNo}.${fmt === 'docx' ? 'md' : fmt === 'html' ? 'html' : 'pdf'}.md`
  a.click(); URL.revokeObjectURL(url)
  ElMessage.success(`导出 ${fmt.toUpperCase()} (mock,实际转换 v0.6)`)
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.mp-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.modules-checklist { display: flex; flex-direction: column; gap: 6px; }
.upload-area {
  padding: 12px 14px; border: 1px dashed #d1d5db; border-radius: 8px;
  cursor: pointer; text-align: center; transition: all .15s; font-size: 13px; color: #4b5563;
}
.upload-area:hover { border-color: #2d7a4f; background: #e8f5ee; color: #2d7a4f; }
.screenshot-info { font-size: 12px; color: #2d7a4f; margin-top: 6px; }
.preview-card { min-height: 540px; }
.empty-state, .loading-state, .ai-not-yet { text-align: center; padding: 50px 20px; color: #6b7280; }
.empty-state p, .loading-state p, .ai-not-yet p { margin: 12px 0; }
.rotate { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.manual-content { font-size: 13px; }
.markdown-body { line-height: 1.7; padding: 10px 0; }
:deep(.markdown-body h2) { font-size: 16px; margin: 12px 0 8px; }
:deep(.markdown-body h3) { font-size: 14px; margin: 10px 0 6px; }
:deep(.markdown-body ul) { margin: 6px 0; padding-left: 22px; }
code { background: #f4f4f5; padding: 2px 6px; border-radius: 3px; font-family: monospace; font-size: 12px; }
</style>
