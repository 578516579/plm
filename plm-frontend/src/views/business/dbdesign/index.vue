<!--
  数据库设计 — PRD §F3.2 + 原型 dbdesign.html
  严格对齐: ER 图 + 数据字典双卡片 + AI 生成 + 建表 SQL 折叠卡
-->
<template>
  <div class="app-container db-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🗄️ 数据库设计</h2>
        <p class="page-subtitle">AI 自动生成 ER 图、建表 SQL、数据字典</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="openAdd"><el-icon><Plus /></el-icon>&nbsp;新增方案</el-button>
        <el-button
          type="success"
          :loading="aiLoading"
          :disabled="!current.dbdesignId"
          @click="triggerAi"
        >
          <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成数据库设计
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never" class="diagram-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">📊 ER 实体关系图</span>
              <el-tag v-if="current.dbEngine" size="small" type="info">{{ dbEngineLabel(current.dbEngine) }}</el-tag>
            </div>
          </template>

          <div v-if="!current.erDiagramContent" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><Coin /></el-icon>
            <p>选择左侧方案后点击「AI 生成数据库设计」</p>
          </div>
          <pre v-else class="mermaid-code">{{ current.erDiagramContent }}</pre>
          <el-alert
            v-if="current.erDiagramContent"
            type="info"
            :closable="false"
            show-icon
            title="Mermaid erDiagram - 贴入 mermaid.live 即可可视化"
            style="margin-top: 8px"
          />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="diagram-card">
          <template #header><span class="card-title">📋 数据字典</span></template>
          <div v-if="!current.dataDictionary" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><Document /></el-icon>
            <p>生成后将展示数据字典 (字段名 + 类型 + 注释)</p>
          </div>
          <div v-else class="markdown-body" v-html="renderedDict" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 建表 SQL -->
    <el-card v-if="current.ddlScript" shadow="never" style="margin-top: 14px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">💻 建表 SQL (AI 生成)</span>
          <el-button size="small" plain @click="copyDdl">
            <el-icon><CopyDocument /></el-icon>&nbsp;复制
          </el-button>
        </div>
      </template>
      <pre class="sql-code">{{ current.ddlScript }}</pre>
    </el-card>

    <!-- 规范检查 (AI 生成) -->
    <el-card v-if="current.normalizationCheck" shadow="never" style="margin-top: 14px">
      <template #header><span class="card-title">🔍 规范检查 (命名 / 索引 / 范式)</span></template>
      <div class="norm-check">
        <el-tag
          v-for="(v, k) in normalizationItems"
          :key="k"
          :type="normTagType(v)"
          size="large"
          style="margin: 0 10px 8px 0"
        >
          {{ normLabel(k) }}:{{ v }}
        </el-tag>
      </div>
    </el-card>

    <!-- 历史方案 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">📚 历史 DB 设计 ({{ total }})</span>
          <el-input
            v-model="queryParams.title"
            placeholder="搜索"
            style="width: 240px"
            clearable
            @clear="getList"
            @keyup.enter="getList"
          />
        </div>
      </template>
      <el-table v-loading="listLoading" :data="list" stripe @row-click="loadDb" row-class-name="clickable-row">
        <el-table-column label="编号" prop="dbdesignNo" width="160" />
        <el-table-column label="方案名" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="DB 引擎" width="100" align="center">
          <template #default="{ row }">{{ dbEngineLabel(row.dbEngine) }}</template>
        </el-table-column>
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
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="loadDb(row)">详情</el-button>
            <AiButton link @click.stop="quickAi(row)">AI 生成</AiButton>
            <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.dbdesignId ? '编辑数据库设计' : '新增数据库设计'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联项目" prop="projectId" required>
          <el-select v-model="form.projectId" placeholder="选择" style="width: 100%">
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="方案名称" prop="title" required>
          <el-input v-model="form.title" placeholder="如:智慧灌溉 DB v1" />
        </el-form-item>
        <el-form-item label="DB 引擎" prop="dbEngine">
          <el-select v-model="form.dbEngine" style="width: 100%">
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgresql" />
            <el-option label="人大金仓" value="kingbase" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">{{ form.dbdesignId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick, Coin, Document, CopyDocument } from '@element-plus/icons-vue'
import {
  listDbDesign, addDbDesign, updateDbDesign, delDbDesign, aiGenerateDbDesign, getDbDesign, listProjectsForSelect,
  type DbDesign, type DbDesignQuery
} from '@/api/business/dbdesign'

const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)

const emptyForm = (): DbDesign => ({
  projectId: 0, title: '', dbEngine: 'mysql', authorUserId: 1
})
const form = reactive<DbDesign>(emptyForm())
const current = reactive<DbDesign>({ projectId: 0, title: '' })
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const list = ref<DbDesign[]>([])
const total = ref(0)
const queryParams = reactive<DbDesignQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

const statusMap: Record<string, { label: string; type: any }> = {
  '00': { label: '草稿', type: 'info' }, '01': { label: '评审中', type: 'warning' },
  '02': { label: '已确认', type: 'success' }, '03': { label: '已废弃', type: 'danger' }
}
const statusTagFor = (s?: string) => statusMap[s || '00'] || { label: s || '-', type: 'info' as any }

const dbEngineLabel = (v?: string) =>
  ({ mysql: 'MySQL', postgresql: 'PostgreSQL', kingbase: '人大金仓' } as Record<string, string>)[v || ''] || v || '-'

const normLabelMap: Record<string, string> = { naming: '命名', index: '索引', normalization: '范式' }
const normLabel = (k: string) => normLabelMap[k] || k
const normTagType = (v: string): 'success' | 'warning' | 'info' =>
  v?.startsWith('pass') ? 'success' : v?.startsWith('warn') ? 'warning' : 'info'
const normalizationItems = computed<Record<string, string>>(() => {
  const raw = current.normalizationCheck
  if (!raw) return {}
  try { return JSON.parse(raw) } catch { return { 原始: raw } }
})

const renderedDict = computed(() => {
  const md = current.dataDictionary || ''
  return md
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/^### (.+)$/gm, '<h4>$1</h4>')
    .replace(/^## (.+)$/gm, '<h3>$1</h3>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>\n?)+/g, m => '<ul>' + m + '</ul>')
    .replace(/\n\n/g, '<br/>')
})

async function getList() {
  listLoading.value = true
  try {
    const res: any = await listDbDesign(queryParams)
    list.value = res.rows || []; total.value = res.total || 0
  } finally { listLoading.value = false }
}

async function loadProjects() {
  try { const res: any = await listProjectsForSelect(); projectOptions.value = res.rows || [] } catch { /* */ }
}

function openAdd() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function loadDb(row: DbDesign) {
  if (!row.dbdesignId) return
  const res: any = await getDbDesign(row.dbdesignId)
  if (res.code === 200 && res.data) Object.assign(current, res.data)
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.dbdesignId) { await updateDbDesign(form); ElMessage.success('更新成功') }
    else { await addDbDesign(form); ElMessage.success('创建成功') }
    dialogVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') }
  finally { saving.value = false }
}

async function triggerAi() {
  if (!current.dbdesignId) { ElMessage.warning('请先选择方案'); return }
  aiLoading.value = true
  try {
    const res: any = await aiGenerateDbDesign(current.dbdesignId)
    if (res.code === 200 && res.data) {
      Object.assign(current, res.data); ElMessage.success('AI ER 图 + DDL 生成成功'); await getList()
    }
  } catch (e: any) { ElMessage.error(e?.msg || 'AI 失败') }
  finally { aiLoading.value = false }
}

async function quickAi(row: DbDesign) {
  if (!row.dbdesignId) return
  aiLoading.value = true
  try { await aiGenerateDbDesign(row.dbdesignId); ElMessage.success(`${row.dbdesignNo} 已生成`); await getList() }
  finally { aiLoading.value = false }
}

async function handleDelete(row: DbDesign) {
  if (!row.dbdesignId) return
  await ElMessageBox.confirm(`确认删除 "${row.dbdesignNo}"?`, '提示', { type: 'warning' })
  await delDbDesign(row.dbdesignId); ElMessage.success('删除成功')
  if (current.dbdesignId === row.dbdesignId) Object.keys(current).forEach(k => delete (current as any)[k])
  await getList()
}

function copyDdl() {
  navigator.clipboard.writeText(current.ddlScript || '').then(() => ElMessage.success('已复制 DDL'))
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.db-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.clickable-row { cursor: pointer; }
.empty-state { text-align: center; padding: 60px 20px; color: #6b7280; }
.empty-state p { margin: 12px 0; }
.diagram-card { min-height: 360px; }
.mermaid-code, .sql-code {
  background: #1e1e2e; color: #cdd6f4; padding: 14px 16px; border-radius: 8px;
  font-family: 'Consolas', monospace; font-size: 12px; line-height: 1.6;
  overflow-x: auto; white-space: pre-wrap;
}
.sql-code { color: #a3e635; }
.markdown-body { line-height: 1.7; font-size: 13px; }
</style>
