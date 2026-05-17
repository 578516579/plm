<!--
  需求管理 — PRD §F2.1 + 原型 requirements.html
  严格对齐: 4 Tab 状态分组 + AI 优先级评估 + 新建需求 modal
-->
<template>
  <div class="app-container req-page">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">📋 需求管理</h2>
        <p class="page-subtitle">全链路需求追踪,AI 辅助需求分析与优先级评估</p>
      </div>
      <div class="header-actions">
        <el-button type="success" :loading="aiLoading" @click="aiAnalyzeAll">
          <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 分析优先级
        </el-button>
        <el-button type="primary" @click="openAdd">
          <el-icon><Plus /></el-icon>&nbsp;新增需求
        </el-button>
      </div>
    </div>

    <!-- Tab 切换 (对齐原型 4 个 tab) -->
    <el-tabs v-model="activeTab" class="status-tabs" @tab-change="getList">
      <el-tab-pane :label="`全部需求 (${total})`" name="" />
      <el-tab-pane :label="`待评审 (${statusCount('00')})`" name="00" />
      <el-tab-pane :label="`开发中 (${statusCount('01')})`" name="01" />
      <el-tab-pane :label="`已完成 (${statusCount('02')})`" name="02" />
    </el-tabs>

    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">📋 需求列表</span>
          <el-input v-model="queryParams.title" placeholder="搜索标题" style="width: 240px" clearable @clear="getList" @keyup.enter="getList" />
        </div>
      </template>
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="requirementNo" width="160" />
        <el-table-column label="需求标题" prop="title" min-width="220" show-overflow-tooltip />
        <el-table-column label="来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="sourceTag(row.source)" size="small">{{ sourceLabel(row.source) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="priorityTag(row.priority)" size="small" effect="dark">{{ row.priority || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="AI 评估" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.aiEvaluation" :type="aiEvalTag(row.aiEvaluation)" size="small">
              {{ aiEvalLabel(row.aiEvaluation) }}
            </el-tag>
            <span v-else class="muted">未评估</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadReq(row)">编辑</el-button>
            <el-button link type="success" @click="quickAi(row)">AI 评估</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 新建/编辑 Dialog (对齐原型 modal-newreq) -->
    <el-dialog v-model="dialogVisible" :title="form.requirementId ? '编辑需求' : '+ 新建需求'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联项目" prop="projectId" required>
          <el-select v-model="form.projectId" placeholder="选择项目" style="width: 100%">
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="需求标题" prop="title" required>
          <el-input v-model="form.title" placeholder="简要描述需求" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="详细描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4"
            placeholder="详细说明需求背景、目标用户、期望功能..." maxlength="2000" show-word-limit />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="来源" prop="source">
              <el-select v-model="form.source" style="width: 100%">
                <el-option label="客户反馈" value="customer" />
                <el-option label="内部提案" value="internal" />
                <el-option label="竞品分析" value="competitive" />
                <el-option label="运营数据" value="data" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" style="width: 100%">
                <el-option label="P0 - 紧急" value="P0" />
                <el-option label="P1 - 重要" value="P1" />
                <el-option label="P2 - 一般" value="P2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="form.requirementId" label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="待评审" value="00" />
            <el-option label="开发中" value="01" />
            <el-option label="已完成" value="02" />
            <el-option label="已取消" value="03" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="success" :loading="aiLoading" @click="aiEvalCurrent" v-if="form.requirementId">
          <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 评估
        </el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ form.requirementId ? '保存' : '✅ 提交需求' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Plus } from '@element-plus/icons-vue'
import {
  listRequirement, addRequirement, updateRequirement, delRequirement,
  aiEvaluateRequirement, getRequirement, listProjectsForSelect,
  type Requirement, type RequirementQuery
} from '@/api/business/requirement'

const activeTab = ref('')
const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)

const emptyForm = (): Requirement => ({
  projectId: 0, title: '', description: '', source: 'customer', priority: 'P1', status: '00'
})
const form = reactive<Requirement>(emptyForm())
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入需求标题', trigger: 'blur' }]
}

const list = ref<Requirement[]>([])
const total = ref(0)
const statusCounts = ref<Record<string, number>>({})
const queryParams = reactive<RequirementQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

const statusMap: Record<string, { label: string; type: any }> = {
  '00': { label: '待评审', type: 'warning' },
  '01': { label: '开发中', type: 'primary' },
  '02': { label: '已完成', type: 'success' },
  '03': { label: '已取消', type: 'danger' }
}
const statusTagFor = (s?: string) => statusMap[s || ''] || { label: s || '-', type: 'info' as any }
const statusCount = (s: string) => statusCounts.value[s] || 0

function sourceLabel(v?: string) {
  return ({ customer: '客户反馈', internal: '内部提案', competitive: '竞品分析', data: '运营数据' } as Record<string,string>)[v || ''] || v || '-'
}
function sourceTag(v?: string): any {
  return ({ customer: 'primary', internal: 'success', competitive: 'warning', data: 'info' } as Record<string,string>)[v || ''] || 'info'
}
function priorityTag(p?: string): any {
  return ({ P0: 'danger', P1: 'warning', P2: 'info' } as Record<string,string>)[p || ''] || 'info'
}
function aiEvalLabel(v?: string) {
  return ({ high: '高价值', medium: '中价值', low: '低价值' } as Record<string,string>)[v || ''] || v || '-'
}
function aiEvalTag(v?: string): any {
  return ({ high: 'success', medium: 'warning', low: 'info' } as Record<string,string>)[v || ''] || 'info'
}

async function getList() {
  listLoading.value = true
  try {
    const params: any = { ...queryParams }
    if (activeTab.value) params.status = activeTab.value
    const res: any = await listRequirement(params)
    list.value = res.rows || []; total.value = res.total || 0
    // Re-compute status counts
    if (!activeTab.value) {
      statusCounts.value = list.value.reduce((acc, r) => {
        const s = r.status || '00'
        acc[s] = (acc[s] || 0) + 1
        return acc
      }, {} as Record<string, number>)
    }
  } finally { listLoading.value = false }
}

async function loadProjects() {
  try { const res: any = await listProjectsForSelect(); projectOptions.value = res.rows || [] } catch {}
}

function openAdd() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function loadReq(row: Requirement) {
  if (!row.requirementId) return
  const res: any = await getRequirement(row.requirementId)
  if (res.code === 200 && res.data) { Object.assign(form, res.data); dialogVisible.value = true }
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.requirementId) { await updateRequirement(form); ElMessage.success('更新成功') }
    else { await addRequirement(form); ElMessage.success('提交成功') }
    dialogVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') } finally { saving.value = false }
}

async function aiAnalyzeAll() {
  if (!list.value.length) { ElMessage.warning('暂无需求,先创建需求'); return }
  aiLoading.value = true
  try {
    // 批量评估前 5 个未评估的
    const pending = list.value.filter(r => !r.aiEvaluation).slice(0, 5)
    for (const r of pending) {
      if (r.requirementId) await aiEvaluateRequirement(r.requirementId)
    }
    ElMessage.success(`AI 已评估 ${pending.length} 个需求的优先级`)
    await getList()
  } catch (e: any) { ElMessage.error(e?.msg || 'AI 失败') } finally { aiLoading.value = false }
}

async function aiEvalCurrent() {
  if (!form.requirementId) return
  aiLoading.value = true
  try {
    const res: any = await aiEvaluateRequirement(form.requirementId)
    if (res.code === 200 && res.data) {
      Object.assign(form, res.data); ElMessage.success('AI 评估完成')
    }
  } catch (e: any) { ElMessage.error(e?.msg || 'AI 失败') } finally { aiLoading.value = false }
}

async function quickAi(row: Requirement) {
  if (!row.requirementId) return
  aiLoading.value = true
  try { await aiEvaluateRequirement(row.requirementId); ElMessage.success('已评估'); await getList() } finally { aiLoading.value = false }
}

async function handleDelete(row: Requirement) {
  if (!row.requirementId) return
  await ElMessageBox.confirm(`确认删除 "${row.title}"?`, '提示', { type: 'warning' })
  await delRequirement(row.requirementId); ElMessage.success('删除成功'); await getList()
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.req-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.status-tabs { background: #fff; padding: 0 16px; border-radius: 8px; margin-bottom: 14px; }
.muted { color: #9ca3af; font-size: 12px; }
</style>
