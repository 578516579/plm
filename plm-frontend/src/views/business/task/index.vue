<!--
  研发看板 — PRD §F3.4 + 原型 kanban.html
  严格对齐: 5 列看板 (待开发/开发中/代码评审/测试中/已完成) + AI 拆分任务
-->
<template>
  <div class="app-container task-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📌 研发看板</h2>
        <p class="page-subtitle">AI 自动拆分任务,关联代码提交,智能预警</p>
      </div>
      <div class="header-actions">
        <el-select v-model="filterSprintId" placeholder="按迭代过滤" clearable style="width: 200px" @change="getList">
          <el-option v-for="s in sprintOptions" :key="s.sprintId" :label="s.name" :value="s.sprintId" />
        </el-select>
        <el-button type="success" :loading="aiLoading" @click="openAiSplit">
          <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 拆分任务
        </el-button>
        <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon>&nbsp;新增任务</el-button>
      </div>
    </div>

    <!-- 5 列看板 (对齐原型 .kb) -->
    <div class="kanban-board">
      <div v-for="col in kanbanColumns" :key="col.status" class="kanban-col">
        <div class="kanban-header">
          <span class="kanban-title">{{ col.label }}</span>
          <span class="kanban-count">{{ tasksByStatus(col.status).length }}</span>
        </div>
        <div class="kanban-list">
          <el-card
            v-for="t in tasksByStatus(col.status)"
            :key="t.taskId"
            shadow="hover"
            class="task-card"
            @click="loadTask(t)"
          >
            <div class="task-title">{{ t.title }}</div>
            <div class="task-meta">
              <el-tag size="small" :type="priorityTag(t.priority)" effect="dark">{{ t.priority || 'P2' }}</el-tag>
              <span v-if="t.taskNo" class="task-no">{{ t.taskNo }}</span>
            </div>
            <div class="task-foot">
              <span class="task-hours">⏱ {{ t.estimatedHours || 0 }}h</span>
              <span v-if="t.mrUrl" class="task-mr">
                <el-link :href="t.mrUrl" target="_blank" type="primary" :underline="false">🔗 MR</el-link>
              </span>
            </div>
          </el-card>
          <div v-if="tasksByStatus(col.status).length === 0" class="empty-col">
            <span class="muted">暂无任务</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建/编辑 Dialog (对齐原型 modal-newtask) -->
    <el-dialog v-model="dialogVisible" :title="form.taskId ? '编辑任务' : '+ 新建任务'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="任务标题" prop="title" required>
          <el-input v-model="form.title" placeholder="任务名称" maxlength="200" />
        </el-form-item>
        <el-form-item label="详细描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="任务目标、技术要点、验收标准..." />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId" required>
              <el-select v-model="form.projectId" style="width: 100%">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="迭代" prop="sprintId">
              <el-select v-model="form.sprintId" clearable style="width: 100%">
                <el-option v-for="s in sprintOptions" :key="s.sprintId" :label="s.name" :value="s.sprintId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="关联需求" prop="requirementId">
              <el-select v-model="form.requirementId" clearable style="width: 100%">
                <el-option v-for="r in requirementOptions" :key="r.requirementId" :label="r.title" :value="r.requirementId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" style="width: 100%">
                <el-option label="P0 - 致命" value="P0" />
                <el-option label="P1 - 严重" value="P1" />
                <el-option label="P2 - 一般" value="P2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="预估工时" prop="estimatedHours">
              <el-input-number v-model="form.estimatedHours" :min="0" :max="200" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.taskId" label="实际工时" prop="actualHours">
              <el-input-number v-model="form.actualHours" :min="0" :max="500" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="form.taskId" label="MR 链接" prop="mrUrl">
          <el-input v-model="form.mrUrl" placeholder="https://gitlab.com/.../merge_requests/123" />
        </el-form-item>
        <el-form-item v-if="form.taskId" label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="col in kanbanColumns" :key="col.status" :label="col.label" :value="col.status" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ form.taskId ? '保存' : '✅ 创建任务' }}
        </el-button>
        <el-button v-if="form.taskId" type="danger" link @click="handleDelete">🗑️ 删除</el-button>
      </template>
    </el-dialog>

    <!-- AI 拆分任务 Dialog -->
    <el-dialog v-model="aiSplitVisible" title="✨ AI 拆分任务" width="500px">
      <el-form label-width="100px">
        <el-form-item label="基于需求">
          <el-select v-model="aiSplitReqId" style="width: 100%" placeholder="选择需求">
            <el-option v-for="r in requirementOptions" :key="r.requirementId" :label="r.title" :value="r.requirementId" />
          </el-select>
        </el-form-item>
        <el-alert type="info" :closable="false" show-icon
          title="AI 将根据需求自动拆分为 3-8 个开发任务 (本期 mock)" />
      </el-form>
      <template #footer>
        <el-button @click="aiSplitVisible = false">取消</el-button>
        <el-button type="primary" :loading="aiLoading" @click="confirmAiSplit">
          <el-icon><MagicStick /></el-icon>&nbsp;✨ 立即拆分
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Plus } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import {
  listTask, addTask, updateTask, delTask, getTask, aiSplitTasks,
  listProjectsForSelect, listSprintsForSelect, listRequirementsForSelect,
  type Task, type TaskQuery
} from '@/api/business/task'

const route = useRoute()
const dialogVisible = ref(false)
const aiSplitVisible = ref(false)
const aiSplitReqId = ref<number | null>(null)
const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const filterSprintId = ref<number | null>(null)

const kanbanColumns = [
  { status: '00', label: '待开发' },
  { status: '01', label: '开发中' },
  { status: '02', label: '代码评审' },
  { status: '03', label: '测试中' },
  { status: '04', label: '已完成' }
]

const emptyForm = (): Task => ({ projectId: 0, title: '', priority: 'P1', estimatedHours: 8, status: '00' })
const form = reactive<Task>(emptyForm())
const rules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }]
}

const list = ref<Task[]>([])
const queryParams = reactive<TaskQuery>({ pageNum: 1, pageSize: 200 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])
const sprintOptions = ref<Array<{ sprintId: number; name: string }>>([])
const requirementOptions = ref<Array<{ requirementId: number; title: string }>>([])

function priorityTag(p?: string): any {
  return ({ P0: 'danger', P1: 'warning', P2: 'info' } as Record<string,string>)[p || ''] || 'info'
}

const tasksByStatus = (s: string) => list.value.filter(t => t.status === s)

async function getList() {
  try {
    const params: any = { ...queryParams }
    if (filterSprintId.value) params.sprintId = filterSprintId.value
    const res: any = await listTask(params)
    list.value = res.rows || []
  } catch {}
}
async function loadOpts() {
  try {
    const [pr, sr, rr] = await Promise.all([listProjectsForSelect(), listSprintsForSelect(), listRequirementsForSelect()])
    projectOptions.value = (pr as any).rows || []
    sprintOptions.value = (sr as any).rows || []
    requirementOptions.value = (rr as any).rows || []
  } catch {}
}

function openAdd() {
  Object.assign(form, emptyForm())
  if (filterSprintId.value) form.sprintId = filterSprintId.value
  dialogVisible.value = true
}

async function loadTask(row: Task) {
  if (!row.taskId) return
  const res: any = await getTask(row.taskId)
  if (res.code === 200 && res.data) { Object.assign(form, res.data); dialogVisible.value = true }
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.taskId) { await updateTask(form); ElMessage.success('更新成功') }
    else { await addTask(form); ElMessage.success('创建成功') }
    dialogVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') } finally { saving.value = false }
}

async function handleDelete() {
  if (!form.taskId) return
  await ElMessageBox.confirm(`确认删除任务 "${form.title}"?`, '提示', { type: 'warning' })
  await delTask(form.taskId); ElMessage.success('删除成功')
  dialogVisible.value = false; await getList()
}

function openAiSplit() {
  aiSplitReqId.value = requirementOptions.value[0]?.requirementId || null
  aiSplitVisible.value = true
}

async function confirmAiSplit() {
  if (!aiSplitReqId.value) { ElMessage.warning('请选择需求'); return }
  aiLoading.value = true
  try {
    await aiSplitTasks({ requirementId: aiSplitReqId.value })
    ElMessage.success('AI 已拆分任务 (mock,实际接入 v0.6)')
    aiSplitVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '拆分失败') } finally { aiLoading.value = false }
}

onMounted(() => {
  // 从路由 query 接受 sprintId 过滤 (sprint 模块跳转过来)
  if (route.query.sprintId) filterSprintId.value = Number(route.query.sprintId)
  getList(); loadOpts()
})
</script>

<style scoped>
.task-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; align-items: center; }
.kanban-board {
  display: flex; gap: 12px; overflow-x: auto; padding-bottom: 6px;
}
.kanban-col {
  min-width: 220px; max-width: 220px; background: #f9fafb; border-radius: 10px; padding: 10px;
  flex-shrink: 0;
}
.kanban-header {
  display: flex; align-items: center; justify-content: space-between;
  font-weight: 700; font-size: 13px; margin-bottom: 10px;
}
.kanban-count {
  background: #e5e7eb; color: #4b5563; border-radius: 20px;
  padding: 1px 8px; font-size: 11px;
}
.kanban-list { display: flex; flex-direction: column; gap: 7px; min-height: 200px; }
.task-card {
  cursor: pointer; transition: all .15s;
}
.task-card:hover { transform: translateY(-2px); }
.task-card :deep(.el-card__body) { padding: 10px 12px; }
.task-title { font-weight: 500; color: #1f2937; margin-bottom: 6px; font-size: 12.5px; line-height: 1.4; }
.task-meta { display: flex; align-items: center; gap: 6px; margin-bottom: 6px; }
.task-no { color: #6b7280; font-size: 11px; font-family: monospace; }
.task-foot { display: flex; justify-content: space-between; font-size: 11px; color: #6b7280; }
.empty-col { padding: 30px 8px; text-align: center; }
.muted { color: #9ca3af; font-size: 12px; }
</style>
