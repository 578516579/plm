<!--
  文档中心 — PRD §F5.5
  12 doc_type 分类 + 状态过滤 + 文档预览
-->
<template>
  <div class="app-container doc-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📚 文档中心</h2>
        <p class="page-subtitle">统一管理 12 类项目文档 (PRD/HLD/LLD/DB/API/需求/架构/测试/手册/Changelog)</p>
      </div>
      <el-button type="primary" @click="openAdd">
        <el-icon><Plus /></el-icon>&nbsp;新增文档
      </el-button>
    </div>

    <!-- 类型 chip 筛选 -->
    <el-card shadow="never" class="filter-card">
      <div class="chip-label">📂 文档类型:</div>
      <div class="chip-row">
        <el-tag
          :type="!queryParams.docType ? 'primary' : 'info'"
          effect="plain"
          class="chip"
          @click="filterByType('')"
        >全部 ({{ total }})</el-tag>
        <el-tag
          v-for="t in docTypes"
          :key="t.value"
          :type="queryParams.docType === t.value ? 'primary' : 'info'"
          effect="plain"
          class="chip"
          @click="filterByType(t.value)"
        >
          {{ t.icon }} {{ t.label }} ({{ countByType(t.value) }})
        </el-tag>
      </div>
    </el-card>

    <el-card shadow="never" style="margin-top: 14px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">📚 文档列表</span>
          <el-input v-model="queryParams.title" placeholder="搜索标题" style="width: 240px" clearable @clear="getList" @keyup.enter="getList" />
        </div>
      </template>
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="documentNo" width="160" />
        <el-table-column label="文档标题" prop="title" min-width="220" show-overflow-tooltip />
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="docTypeTag(row.docType)" size="small">{{ docTypeLabel(row.docType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="版本" prop="version" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="160">
          <template #default="{ row }">
            <el-tag v-for="tag in (row.tags || '').split(',').filter(Boolean)" :key="tag" size="small" style="margin-right: 4px" effect="plain">
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadDoc(row)">查看</el-button>
            <el-button link type="warning" @click="loadDocEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 新建/编辑 Dialog -->
    <el-dialog v-model="dialogVisible" :title="form.documentId ? (readOnly ? '查看文档' : '编辑文档') : '+ 新建文档'" width="800px" top="6vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" :disabled="readOnly">
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId" required>
              <el-select v-model="form.projectId" style="width: 100%">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="文档类型" prop="docType" required>
              <el-select v-model="form.docType" style="width: 100%">
                <el-option v-for="t in docTypes" :key="t.value" :label="`${t.icon} ${t.label}`" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="文档标题" prop="title" required>
          <el-input v-model="form.title" placeholder="文档标题" maxlength="200" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="版本" prop="version">
              <el-input v-model="form.version" placeholder="v1.0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签" prop="tags">
              <el-input v-model="form.tags" placeholder="逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="文档内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="12"
            placeholder="支持 Markdown 格式..."
            class="md-input" />
        </el-form-item>
        <el-form-item v-if="form.documentId && !readOnly" label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="草稿" value="00" />
            <el-option label="待评审" value="01" />
            <el-option label="已发布" value="02" />
            <el-option label="已归档" value="03" />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 预览区 (查看模式) -->
      <div v-if="readOnly && form.content" class="preview-block">
        <el-divider content-position="left">📝 预览</el-divider>
        <div class="markdown-body" v-html="renderedContent" />
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="readOnly" type="primary" @click="readOnly = false">
          <el-icon><Edit /></el-icon>&nbsp;编辑
        </el-button>
        <el-button v-else type="primary" :loading="saving" @click="handleSubmit">
          {{ form.documentId ? '保存' : '✅ 创建文档' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit } from '@element-plus/icons-vue'
import {
  listDocument, addDocument, updateDocument, delDocument, getDocument, listProjectsForSelect,
  type Document, type DocumentQuery
} from '@/api/business/document'
import { statusTagFor, docTypeLabel, docTypeTag } from './documentDict'

const dialogVisible = ref(false)
const readOnly = ref(false)
const formRef = ref()
const saving = ref(false)
const listLoading = ref(false)

const docTypes = [
  { value: 'prd', icon: '📄', label: 'PRD' },
  { value: 'hld', icon: '🏗️', label: 'HLD' },
  { value: 'lld', icon: '🔌', label: 'LLD' },
  { value: 'db', icon: '🗄️', label: 'DB' },
  { value: 'api', icon: '📗', label: 'API' },
  { value: 'req', icon: '📋', label: '需求' },
  { value: 'arch', icon: '🏛️', label: '架构' },
  { value: 'test', icon: '🧪', label: '测试' },
  { value: 'manual', icon: '📖', label: '手册' },
  { value: 'changelog', icon: '📝', label: 'Changelog' },
  { value: 'other', icon: '📁', label: '其他' }
]

const emptyForm = (): Document => ({ projectId: 0, title: '', docType: 'other', version: 'v1.0', status: '00' })
const form = reactive<Document>(emptyForm())
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  docType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const list = ref<Document[]>([])
const total = ref(0)
const queryParams = reactive<DocumentQuery>({ pageNum: 1, pageSize: 10, docType: '' })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

function countByType(t: string) {
  return list.value.filter(d => d.docType === t).length
}

const renderedContent = computed(() => {
  const md = form.content || ''
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
  try { const res: any = await listDocument(queryParams); list.value = res.rows || []; total.value = res.total || 0 } finally { listLoading.value = false }
}
async function loadProjects() {
  try { const res: any = await listProjectsForSelect(); projectOptions.value = res.rows || [] } catch {}
}

function filterByType(t: string) { queryParams.docType = t; getList() }

function openAdd() { Object.assign(form, emptyForm()); readOnly.value = false; dialogVisible.value = true }

async function loadDoc(row: Document) {
  if (!row.documentId) return
  const res: any = await getDocument(row.documentId)
  if (res.code === 200 && res.data) { Object.assign(form, res.data); readOnly.value = true; dialogVisible.value = true }
}

async function loadDocEdit(row: Document) {
  await loadDoc(row); readOnly.value = false
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.documentId) { await updateDocument(form); ElMessage.success('更新成功') }
    else { await addDocument(form); ElMessage.success('创建成功') }
    dialogVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') } finally { saving.value = false }
}

async function handleDelete(row: Document) {
  if (!row.documentId) return
  await ElMessageBox.confirm(`确认删除 "${row.title}"?`, '提示', { type: 'warning' })
  await delDocument(row.documentId); ElMessage.success('删除成功'); await getList()
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.doc-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.filter-card :deep(.el-card__body) { padding: 12px 16px; }
.chip-row { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { cursor: pointer; transition: all .15s; }
.chip:hover { transform: translateY(-1px); }
.md-input :deep(.el-textarea__inner) { font-family: 'Consolas', monospace; font-size: 12px; }
.preview-block { margin-top: 14px; max-height: 320px; overflow-y: auto; padding: 8px; background: #f9fafb; border-radius: 6px; }
.markdown-body { line-height: 1.7; font-size: 13px; }
:deep(.markdown-body h2) { font-size: 16px; margin: 12px 0 8px; }
:deep(.markdown-body h3) { font-size: 14px; margin: 10px 0 6px; }
:deep(.markdown-body h4) { font-size: 13px; margin: 8px 0 4px; }
:deep(.markdown-body ul) { margin: 6px 0; padding-left: 22px; }
</style>
