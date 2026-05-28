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
        <AiButton :loading="aiLoading" @click="aiAnalyzeAll">AI 分析优先级</AiButton>
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
            <el-tag :type="priorityTag(row.priority)" size="small" effect="dark">{{ priorityLabel(row.priority) }}</el-tag>
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
        <el-table-column label="操作" width="320" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadReq(row)">编辑</el-button>
            <el-button link type="warning" @click="openReview(row)">评审</el-button>
            <el-button link type="info" @click="openDetail(row)">详情</el-button>
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
                <el-option label="客户反馈" value="01" />
                <el-option label="内部提案" value="02" />
                <el-option label="运营数据" value="03" />
                <el-option label="竞品分析" value="04" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" style="width: 100%">
                <el-option label="P0 - 紧急" value="00" />
                <el-option label="P1 - 重要" value="01" />
                <el-option label="P2 - 一般" value="02" />
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
        <AiButton v-if="form.requirementId" :loading="aiLoading" @click="aiEvalCurrent">AI 评估</AiButton>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ form.requirementId ? '保存' : '✅ 提交需求' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 评审对话框 (PRD §F2.4 需求评审管理) -->
    <el-dialog v-model="reviewDialogVisible" title="📝 提交需求评审" width="560px">
      <el-alert v-if="currentReviewReq" type="info" :closable="false" show-icon class="mb12">
        正在评审: <strong>{{ currentReviewReq.requirementNo }}</strong> — {{ currentReviewReq.title }}
      </el-alert>
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="100px">
        <el-form-item label="评审结果" prop="reviewResult" required>
          <el-radio-group v-model="reviewForm.reviewResult">
            <el-radio value="00">✅ 通过 (允许推进到「开发中」)</el-radio>
            <el-radio value="01">❌ 打回 (需求方修改后重新评审)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="评审意见" prop="reviewComment"
          :required="reviewForm.reviewResult === '01'">
          <el-input v-model="reviewForm.reviewComment" type="textarea" :rows="4"
            :placeholder="reviewForm.reviewResult === '01' ? '打回必须填写打回原因' : '可选,记录评审重点'"
            maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewSubmitting" @click="handleSubmitReview">
          提交评审
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情/关联对话框 — 评审历史 + 关联 PRD/UED/任务 -->
    <el-dialog v-model="detailDialogVisible" title="🔗 需求详情与关联" width="800px">
      <el-descriptions v-if="currentDetailReq" :column="2" border size="small" class="mb12">
        <el-descriptions-item label="编号">{{ currentDetailReq.requirementNo }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentDetailReq.title }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagFor(currentDetailReq.status).type" size="small">
            {{ statusTagFor(currentDetailReq.status).label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="priorityTag(currentDetailReq.priority)" size="small" effect="dark">
            {{ priorityLabel(currentDetailReq.priority) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-tabs v-model="detailTab">
        <el-tab-pane name="reviews" :label="`📝 评审记录 (${reviewsList.length})`">
          <el-empty v-if="!reviewsList.length" description="暂无评审记录,点击「评审」按钮提交首次评审" :image-size="80" />
          <el-table v-else :data="reviewsList" stripe size="small">
            <el-table-column label="评审结果" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.reviewResult === '00' ? 'success' : 'danger'" size="small">
                  {{ row.reviewResult === '00' ? '✅ 通过' : '❌ 打回' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="评审意见" prop="reviewComment" min-width="220" show-overflow-tooltip />
            <el-table-column label="评审时间" prop="reviewAt" width="160" />
            <el-table-column label="评审人" prop="createBy" width="100" />
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button link type="danger" size="small" @click="handleDeleteReview(row)">撤回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane name="prd" :label="`📄 关联 PRD (${prdList.length})`">
          <el-empty v-if="!prdList.length" description="暂无关联 PRD" :image-size="80" />
          <el-table v-else :data="prdList" stripe size="small">
            <el-table-column label="编号" prop="prdNo" width="160" />
            <el-table-column label="功能名称" prop="title" min-width="200" show-overflow-tooltip />
            <el-table-column label="状态" prop="status" width="100" align="center">
              <template #default="{ row }">
                {{ ({ '00':'草稿','01':'评审中','02':'已确认','03':'已废弃' } as any)[row.status] || row.status }}
              </template>
            </el-table-column>
            <el-table-column label="完整度" prop="completenessScore" width="90" align="center" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane name="ued" :label="`🎨 关联 UED (${uedList.length})`">
          <el-empty v-if="!uedList.length" description="暂无关联 UED" :image-size="80" />
          <el-table v-else :data="uedList" stripe size="small">
            <el-table-column label="编号" prop="uedNo" width="160" />
            <el-table-column label="标题" prop="title" min-width="200" show-overflow-tooltip />
            <el-table-column label="状态" prop="status" width="100" align="center" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane name="tasks" :label="`✅ 关联任务 (${tasksList.length})`">
          <el-empty v-if="!tasksList.length" description="暂无关联任务" :image-size="80" />
          <el-table v-else :data="tasksList" stripe size="small">
            <el-table-column label="编号" prop="taskNo" width="140" />
            <el-table-column label="标题" prop="title" min-width="200" show-overflow-tooltip />
            <el-table-column label="状态" prop="status" width="100" align="center" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AiButton from '@/components/AiButton/index.vue'
import {
  listRequirement, addRequirement, updateRequirement, delRequirement,
  aiEvaluateRequirement, getRequirement, listProjectsForSelect,
  listRequirementReviews, submitRequirementReview, deleteRequirementReviews,
  listPrdByRequirementId, listUedByRequirementId, listTasksByRequirementId,
  type Requirement, type RequirementQuery, type RequirementReview
} from '@/api/business/requirement'
import {
  sourceLabel, sourceTag, priorityLabel, priorityTag,
  statusTagFor, aiEvalLabel, aiEvalTag
} from './requirementDict'

const activeTab = ref('')
const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)

const emptyForm = (): Requirement => ({
  projectId: 0, title: '', description: '', source: '01', priority: '01', status: '00'
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

const statusCount = (s: string) => statusCounts.value[s] || 0

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

// ─────────────────────────────────────────────────────────────────────
// 需求评审 (PRD §F2.4, 2026-05-25 新增)
// ─────────────────────────────────────────────────────────────────────
const reviewDialogVisible = ref(false)
const reviewFormRef = ref()
const reviewSubmitting = ref(false)
const currentReviewReq = ref<Requirement | null>(null)
const reviewForm = reactive<RequirementReview>({ reviewResult: '00', reviewComment: '' })
const reviewRules = {
  reviewResult: [{ required: true, message: '请选择评审结果', trigger: 'change' }],
  reviewComment: [{
    validator: (_r: any, v: any, cb: any) => {
      if (reviewForm.reviewResult === '01' && (!v || !v.trim())) cb(new Error('打回评审必须填写意见'))
      else cb()
    },
    trigger: 'blur'
  }]
}

function openReview(row: Requirement) {
  if (!row.requirementId) return
  currentReviewReq.value = row
  reviewForm.reviewResult = '00'
  reviewForm.reviewComment = ''
  reviewDialogVisible.value = true
}

async function handleSubmitReview() {
  if (!currentReviewReq.value?.requirementId) return
  await reviewFormRef.value.validate()
  reviewSubmitting.value = true
  try {
    await submitRequirementReview(currentReviewReq.value.requirementId, { ...reviewForm })
    ElMessage.success(reviewForm.reviewResult === '00' ? '✅ 评审通过' : '❌ 已打回')
    reviewDialogVisible.value = false
    // 若刚才正在看详情对话框, 刷新评审历史
    if (detailDialogVisible.value && currentDetailReq.value?.requirementId === currentReviewReq.value.requirementId) {
      await loadReviewsList(currentReviewReq.value.requirementId)
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || '评审提交失败')
  } finally { reviewSubmitting.value = false }
}

// ─────────────────────────────────────────────────────────────────────
// 详情/关联对话框 (评审历史 + PRD/UED/任务)
// ─────────────────────────────────────────────────────────────────────
const detailDialogVisible = ref(false)
const detailTab = ref('reviews')
const currentDetailReq = ref<Requirement | null>(null)
const reviewsList = ref<RequirementReview[]>([])
const prdList = ref<any[]>([])
const uedList = ref<any[]>([])
const tasksList = ref<any[]>([])

async function loadReviewsList(reqId: number) {
  try { const res: any = await listRequirementReviews(reqId); reviewsList.value = res.data || [] }
  catch { reviewsList.value = [] }
}
async function loadPrdList(reqId: number) {
  try { const res: any = await listPrdByRequirementId(reqId); prdList.value = res.rows || [] }
  catch { prdList.value = [] }
}
async function loadUedList(reqId: number) {
  try { const res: any = await listUedByRequirementId(reqId); uedList.value = res.rows || [] }
  catch { uedList.value = [] }
}
async function loadTasksList(reqId: number) {
  try { const res: any = await listTasksByRequirementId(reqId); tasksList.value = res.rows || [] }
  catch { tasksList.value = [] }
}

async function openDetail(row: Requirement) {
  if (!row.requirementId) return
  currentDetailReq.value = row
  detailTab.value = 'reviews'
  reviewsList.value = []; prdList.value = []; uedList.value = []; tasksList.value = []
  detailDialogVisible.value = true
  await Promise.all([
    loadReviewsList(row.requirementId),
    loadPrdList(row.requirementId),
    loadUedList(row.requirementId),
    loadTasksList(row.requirementId)
  ])
}

async function handleDeleteReview(row: RequirementReview) {
  if (!row.reviewId) return
  await ElMessageBox.confirm('确认撤回这条评审记录?', '提示', { type: 'warning' })
  await deleteRequirementReviews(row.reviewId)
  ElMessage.success('已撤回')
  if (currentDetailReq.value?.requirementId) await loadReviewsList(currentDetailReq.value.requirementId)
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
.mb12 { margin-bottom: 12px; }
</style>
