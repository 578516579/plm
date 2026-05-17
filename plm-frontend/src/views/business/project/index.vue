<!--
  项目管理 — PRD §F1.2 + 原型 projects.html
  默认表格视图 (兼容历史 E2E) + 卡片视图切换 + 5 态状态机
-->
<template>
  <div class="app-container project-page">
    <!-- 顶栏 (对齐原型 .ph) -->
    <div class="page-header">
      <div>
        <h2 class="page-title">📁 项目列表</h2>
        <p class="page-subtitle">共 <strong class="hl">{{ activeCount }}</strong> 个在办项目 / 总 {{ total }} 个</p>
      </div>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>&nbsp;新增项目
      </el-button>
    </div>

    <!-- 筛选条 -->
    <el-card shadow="never" class="filter-bar">
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="项目名称">
          <el-input
            v-model="queryParams.projectName"
            placeholder="请输入项目名称"
            clearable style="width: 220px"
            @clear="getList" @keyup.enter="getList"
          />
        </el-form-item>
        <el-form-item label="项目类型">
          <el-select v-model="queryParams.projectType" placeholder="全部" clearable style="width: 160px" @change="getList">
            <el-option label="研发" value="rnd" />
            <el-option label="运维" value="ops" />
            <el-option label="咨询" value="consulting" />
            <el-option label="实施" value="implementation" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px" @change="getList">
            <el-option v-for="(v, k) in statusMap" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-radio-group v-model="viewMode" size="default">
            <el-radio-button value="table">📊 表格</el-radio-button>
            <el-radio-button value="card">📋 卡片</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="getList">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格视图 (默认) -->
    <el-card v-if="viewMode === 'table'" shadow="never">
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="projectNo" width="160" />
        <el-table-column label="项目名称" prop="projectName" min-width="220" show-overflow-tooltip />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.projectType).type">{{ typeTag(row.projectType).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="负责人 ID" prop="managerUserId" width="90" align="center" />
        <el-table-column label="起止" width="200" align="center">
          <template #default="{ row }">{{ formatDate(row.startDate) }} ~ {{ formatDate(row.endDate) }}</template>
        </el-table-column>
        <el-table-column label="预算" width="100" align="right">
          <template #default="{ row }">{{ row.budget != null ? `¥${Number(row.budget).toFixed(1)}万` : '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="success" v-if="canStart(row)" @click="quickTransition(row, '1')">▶ 启动</el-button>
            <el-button link type="warning" v-if="canPause(row)" @click="quickTransition(row, '2')">⏸ 暂停</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 卡片视图 (对齐原型 projects.html 卡片风格) -->
    <div v-else v-loading="listLoading" class="card-grid">
      <el-empty v-if="!list.length" description="暂无项目" />
      <el-card v-for="p in list" :key="p.id" shadow="hover" class="proj-card" @click="openEdit(p)">
        <div class="proj-card-header">
          <code class="proj-no">{{ p.projectNo }}</code>
          <el-tag :type="statusTagFor(p.status).type" size="small">{{ statusTagFor(p.status).label }}</el-tag>
        </div>
        <h3 class="proj-name">{{ p.projectName }}</h3>
        <div class="proj-meta">
          <div><el-icon><Calendar /></el-icon>&nbsp;{{ formatDate(p.startDate) }} → {{ formatDate(p.endDate) }}</div>
          <div v-if="p.budget != null"><el-icon><Money /></el-icon>&nbsp;预算 ¥{{ Number(p.budget).toFixed(1) }} 万</div>
        </div>
        <el-progress
          :percentage="calcProgress(p)"
          :stroke-width="8"
          :status="progressStatus(p)"
          :format="(v: number) => `${v}%`"
        />
        <div class="proj-desc" v-if="p.description">{{ p.description }}</div>
      </el-card>
    </div>

    <!-- 新建/编辑 Dialog -->
    <el-dialog v-model="formDialog" :title="form.id ? `编辑项目 #${form.projectNo}` : '新增项目'" width="600">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目名称" prop="projectName" required>
          <el-input v-model="form.projectName" placeholder="输入项目名称" maxlength="128" show-word-limit />
        </el-form-item>

        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="项目类型" prop="projectType">
              <el-select v-model="form.projectType" placeholder="选择类型" style="width: 100%">
                <el-option label="研发" value="rnd" />
                <el-option label="运维" value="ops" />
                <el-option label="咨询" value="consulting" />
                <el-option label="实施" value="implementation" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人 ID" prop="managerUserId">
              <el-input-number v-model="form.managerUserId" :min="1" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="form.startDate" value-format="YYYY-MM-DD" type="date" placeholder="开始日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止日期" prop="endDate">
              <el-date-picker v-model="form.endDate" value-format="YYYY-MM-DD" type="date" placeholder="截止日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="预算(万)" prop="budget">
          <el-input-number v-model="form.budget" :min="0" :precision="1" style="width: 200px" controls-position="right" />
        </el-form-item>

        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="项目简介、目标、范围……" maxlength="1000" show-word-limit />
        </el-form-item>

        <el-form-item v-if="form.id" label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button v-for="(v, k) in statusMap" :key="k" :value="k">{{ v.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ form.id ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Calendar, Money } from '@element-plus/icons-vue'
import {
  listProject, getProject, addProject, updateProject, delProject,
  type Project, type ProjectQuery
} from '@/api/business/project'

const formRef = ref()
const saving = ref(false)
const listLoading = ref(false)
const formDialog = ref(false)
const viewMode = ref<'card' | 'table'>('table')

const list = ref<Project[]>([])
const total = ref(0)

const emptyForm = (): Project => ({
  projectName: '', projectType: 'rnd',
  managerUserId: 1, status: '0'
})
const form = reactive<Project>(emptyForm())
const queryParams = reactive<ProjectQuery>({ pageNum: 1, pageSize: 12, projectName: '' })

const rules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
}

// === 状态机 5 态 ===
const statusMap: Record<string, { label: string; type: any }> = {
  '0': { label: '未启动', type: 'info' },
  '1': { label: '进行中', type: 'primary' },
  '2': { label: '暂停',   type: 'warning' },
  '3': { label: '已完成', type: 'success' },
  '4': { label: '已取消', type: 'danger' }
}
function statusTagFor(s?: string) { return statusMap[s || '0'] || { label: s, type: 'info' } }

const activeCount = computed(() => list.value.filter(p => p.status === '1' || p.status === '2').length)

function typeTag(t?: string) {
  return ({
    rnd:            { label: '研发', type: 'primary' as const },
    ops:            { label: '运维', type: 'success' as const },
    consulting:     { label: '咨询', type: 'warning' as const },
    implementation: { label: '实施', type: 'info'    as const }
  } as any)[t || ''] || { label: t || '-', type: 'info' as const }
}

function calcProgress(p: Project): number {
  return ({ '0': 5, '1': 50, '2': 30, '3': 100, '4': 0 } as any)[p.status || '0'] ?? 50
}
function progressStatus(p: Project): any {
  if (p.status === '3') return 'success'
  if (p.status === '4') return 'exception'
  if (p.status === '2') return 'warning'
  return undefined
}

function canStart(p: Project) { return p.status === '0' || p.status === '2' }
function canPause(p: Project) { return p.status === '1' }

function formatDate(d?: string) {
  if (!d) return '-'
  return d.length > 10 ? d.slice(0, 10) : d
}

// === CRUD ===
async function getList() {
  listLoading.value = true
  try {
    const res: any = await listProject(queryParams)
    list.value = res.rows || []
    total.value = res.total || 0
  } finally {
    listLoading.value = false
  }
}

function resetQuery() {
  queryParams.projectName = ''
  queryParams.projectType = undefined
  queryParams.status = undefined
  queryParams.pageNum = 1
  getList()
}

function openCreate() {
  Object.assign(form, emptyForm())
  delete form.id
  formDialog.value = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

async function openEdit(p: Project) {
  const res: any = await getProject(p.id!)
  if (res.code === 200 && res.data) {
    Object.assign(form, res.data)
    formDialog.value = true
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      const res: any = await updateProject(form)
      if (res.code === 200) {
        ElMessage.success('项目已更新')
        formDialog.value = false
        await getList()
      }
    } else {
      const res: any = await addProject(form)
      if (res.code === 200) {
        ElMessage.success(`项目创建成功`)
        formDialog.value = false
        await getList()
      }
    }
  } catch (e: any) {
    if (e?.code === 601) ElMessage.error('状态转换不合法 (601)')
    else if (e?.code === 602) ElMessage.error('必填字段缺失 (602)')
    else ElMessage.error(e?.msg || e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function quickTransition(p: Project, to: string) {
  try {
    const res: any = await updateProject({ ...p, status: to })
    if (res.code === 200) {
      ElMessage.success(`项目状态切至 ${statusTagFor(to).label}`)
      await getList()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || '状态切换失败')
  }
}

async function handleDelete(p: Project) {
  await ElMessageBox.confirm(`确认删除项目 "${p.projectName}"?所有关联数据会受影响`, '提示', { type: 'warning' })
  await delProject(p.id!)
  ElMessage.success('删除成功')
  await getList()
}

onMounted(getList)
</script>

<style scoped>
.project-page { padding: 20px; }
.page-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 16px;
}
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.hl { color: #3b82f6; }
.filter-bar { margin-bottom: 16px; }
.filter-bar :deep(.el-card__body) { padding: 12px 16px; }

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
.proj-card {
  cursor: pointer; transition: all 0.15s;
  border-top: 3px solid #3b82f6;
}
.proj-card:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.proj-card-header {
  display: flex; justify-content: space-between; gap: 6px; align-items: center;
}
.proj-no {
  background: #f4f4f5; padding: 2px 6px; border-radius: 3px;
  font-family: 'Consolas', monospace; font-size: 11px;
  color: #6b7280;
}
.proj-name {
  font-size: 16px; font-weight: 600; margin: 10px 0 8px;
  color: #111827; line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.proj-meta {
  font-size: 12px; color: #6b7280; display: flex; flex-direction: column; gap: 4px;
  margin-bottom: 10px;
}
.proj-meta div { display: flex; align-items: center; }
.proj-desc {
  font-size: 12px; color: #9ca3af; margin-top: 8px; line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
