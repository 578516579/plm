<!--
  迭代 Sprint 管理 — PRD §F3.4 + 原型 kanban.html (modal-sprint)
  迭代列表 + 新建迭代 + 关联任务跳转看板
-->
<template>
  <div class="app-container sprint-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">📅 迭代 / Sprint 管理</h2>
        <p class="page-subtitle">迭代周期管理,关联任务与里程碑</p>
      </div>
      <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon>&nbsp;+ 新建迭代</el-button>
    </div>

    <el-row :gutter="14" class="stat-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">迭代总数</div>
          <div class="stat-value g">{{ total }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">进行中</div>
          <div class="stat-value blue">{{ countByStatus('01') }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">已完成</div>
          <div class="stat-value success">{{ countByStatus('02') }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">草稿</div>
          <div class="stat-value gray">{{ countByStatus('00') }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 14px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">📅 迭代列表</span>
          <el-input v-model="queryParams.name" placeholder="搜索迭代名" style="width: 240px" clearable @clear="getList" @keyup.enter="getList" />
        </div>
      </template>
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="sprintNo" width="160" />
        <el-table-column label="迭代名" prop="name" min-width="160" show-overflow-tooltip />
        <el-table-column label="迭代目标" prop="goal" min-width="200" show-overflow-tooltip />
        <el-table-column label="计划周期" min-width="180">
          <template #default="{ row }">
            <span v-if="row.plannedStartDate && row.plannedEndDate" class="muted">
              {{ formatDate(row.plannedStartDate) }} → {{ formatDate(row.plannedEndDate) }}
            </span>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="工期" width="80" align="center">
          <template #default="{ row }">{{ row.durationDays || '-' }}d</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadSprint(row)">编辑</el-button>
            <el-button link type="success" @click="gotoKanban(row)">看板 →</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 新建/编辑 Dialog (对齐原型 modal-sprint) -->
    <el-dialog v-model="dialogVisible" :title="form.sprintId ? '编辑迭代' : '+ 新建迭代'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="关联项目" prop="projectId" required>
          <el-select v-model="form.projectId" placeholder="选择项目" style="width: 100%">
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="迭代名称" prop="name" required>
          <el-input v-model="form.name" placeholder="如:Sprint 4" />
        </el-form-item>
        <el-form-item label="迭代目标" prop="goal">
          <el-input v-model="form.goal" type="textarea" :rows="2" placeholder="本迭代核心目标" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="plannedStartDate">
              <el-date-picker v-model="form.plannedStartDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="plannedEndDate">
              <el-date-picker v-model="form.plannedEndDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="工期 (天)" prop="durationDays">
          <el-input-number v-model="form.durationDays" :min="1" :max="60" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.sprintId" label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="草稿" value="00" />
            <el-option label="进行中" value="01" />
            <el-option label="已完成" value="02" />
            <el-option label="已取消" value="03" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ form.sprintId ? '保存' : '✅ 创建迭代' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {
  listSprint, addSprint, updateSprint, delSprint, getSprint, listProjectsForSelect,
  type Sprint, type SprintQuery
} from '@/api/business/sprint'

const router = useRouter()
const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const listLoading = ref(false)

const emptyForm = (): Sprint => ({ projectId: 0, name: '', goal: '', durationDays: 14, status: '00' })
const form = reactive<Sprint>(emptyForm())
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  name: [{ required: true, message: '请输入迭代名', trigger: 'blur' }]
}

const list = ref<Sprint[]>([])
const total = ref(0)
const queryParams = reactive<SprintQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

const statusMap: Record<string, { label: string; type: any }> = {
  '00': { label: '草稿', type: 'info' },
  '01': { label: '进行中', type: 'primary' },
  '02': { label: '已完成', type: 'success' },
  '03': { label: '已取消', type: 'danger' }
}
const statusTagFor = (s?: string) => statusMap[s || ''] || { label: s || '-', type: 'info' as any }
const countByStatus = (s: string) => list.value.filter(x => x.status === s).length

function formatDate(d?: string) {
  if (!d) return '-'
  return d.slice(0, 10)
}

async function getList() {
  listLoading.value = true
  try { const res: any = await listSprint(queryParams); list.value = res.rows || []; total.value = res.total || 0 } finally { listLoading.value = false }
}
async function loadProjects() {
  try { const res: any = await listProjectsForSelect(); projectOptions.value = res.rows || [] } catch {}
}

function openAdd() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function loadSprint(row: Sprint) {
  if (!row.sprintId) return
  const res: any = await getSprint(row.sprintId)
  if (res.code === 200 && res.data) { Object.assign(form, res.data); dialogVisible.value = true }
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.sprintId) { await updateSprint(form); ElMessage.success('更新成功') }
    else { await addSprint(form); ElMessage.success('创建成功') }
    dialogVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') } finally { saving.value = false }
}

async function handleDelete(row: Sprint) {
  if (!row.sprintId) return
  await ElMessageBox.confirm(`确认删除迭代 "${row.name}"?`, '提示', { type: 'warning' })
  await delSprint(row.sprintId); ElMessage.success('删除成功'); await getList()
}

function gotoKanban(row: Sprint) {
  router.push({ path: '/business/task', query: { sprintId: String(row.sprintId) } })
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.sprint-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.stat-row :deep(.el-card__body) { padding: 14px 16px; text-align: left; }
.stat-label { color: #6b7280; font-size: 12px; margin-bottom: 5px; }
.stat-value { font-size: 26px; font-weight: 700; }
.stat-value.g { color: #2d7a4f; }
.stat-value.blue { color: #3b82f6; }
.stat-value.success { color: #10b981; }
.stat-value.gray { color: #9ca3af; }
.muted { color: #6b7280; font-size: 12px; }
</style>
