<!--
  任务看板 — 5 列拖拽 (PRD §F3.4 kanban.html)
  按 status 分列:待开发 / 开发中 / 代码评审 / 测试中 / 已完成
-->
<template>
  <div class="app-container kanban-page">
    <div class="page-header">
      <h2 class="page-title">📌 任务看板</h2>
      <p class="page-subtitle">5 列 Kanban 视图,按状态分组展示</p>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true">
        <el-form-item label="项目 ID">
          <el-input v-model="filterProjectId" placeholder="必填或项目ID" clearable style="width: 180px" @change="getList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">加载看板</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="kanban-board" v-loading="loading">
      <div v-for="col in columns" :key="col.status" class="kanban-column">
        <div class="kanban-header" :class="col.cls">
          {{ col.icon }} {{ col.label }}
          <el-tag size="small" effect="dark">{{ groupedTasks[col.status]?.length || 0 }}</el-tag>
        </div>
        <div class="kanban-body">
          <el-card
            v-for="t in (groupedTasks[col.status] || [])"
            :key="t.taskId"
            shadow="hover"
            class="kanban-card"
          >
            <div class="task-no">{{ t.taskNo }}</div>
            <div class="task-title">{{ t.title }}</div>
            <div class="task-meta">
              <el-tag size="small" :type="priorityTag(t.priority).type">{{ priorityTag(t.priority).label }}</el-tag>
              <span v-if="t.estimatedHours" class="meta-text">{{ t.estimatedHours }}h</span>
              <span v-if="t.assigneeUserId" class="meta-text">👤 #{{ t.assigneeUserId }}</span>
            </div>
          </el-card>
          <el-empty v-if="!(groupedTasks[col.status]?.length)" :image-size="40" description="无任务" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'

const list = ref<any[]>([])
const loading = ref(false)
const filterProjectId = ref('')

const columns = [
  { status: '00', label: '待开发',   icon: '📥', cls: 'col-info' },
  { status: '01', label: '开发中',   icon: '💻', cls: 'col-primary' },
  { status: '02', label: '代码评审', icon: '🔍', cls: 'col-warning' },
  { status: '03', label: '测试中',   icon: '🧪', cls: 'col-warning2' },
  { status: '04', label: '已完成',   icon: '✅', cls: 'col-success' }
]

const groupedTasks = computed(() => {
  const g: Record<string, any[]> = {}
  for (const c of columns) g[c.status] = []
  for (const t of list.value) {
    const s = t.status || '00'
    if (g[s]) g[s].push(t)
  }
  return g
})

const priorityMap: any = {
  '00': { label: 'P0', type: 'danger' },
  '01': { label: 'P1', type: 'warning' },
  '02': { label: 'P2', type: 'info' }
}
function priorityTag(p?: string) { return priorityMap[p || '02'] || { label: p, type: 'info' } }

async function getList() {
  loading.value = true
  try {
    const params: any = { pageSize: 200 }
    if (filterProjectId.value) params.projectId = filterProjectId.value
    const res: any = await request({ url: '/business/task/list', method: 'get', params })
    list.value = res.rows || []
  } finally {
    loading.value = false
  }
}

onMounted(getList)
</script>

<style scoped>
.kanban-page { padding: 20px; }
.page-header { margin-bottom: 12px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.filter-card { margin-bottom: 12px; }
.filter-card :deep(.el-card__body) { padding: 12px 16px; }

.kanban-board {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  min-height: 500px;
}
.kanban-column {
  background: #f9fafb; border-radius: 8px; padding: 8px;
  min-height: 400px;
}
.kanban-header {
  font-weight: 600; font-size: 13px; padding: 8px;
  border-radius: 6px; margin-bottom: 10px;
  display: flex; justify-content: space-between; align-items: center;
}
.kanban-header.col-info     { background: #f3f4f6; color: #6b7280; }
.kanban-header.col-primary  { background: #dbeafe; color: #1e40af; }
.kanban-header.col-warning  { background: #fef3c7; color: #92400e; }
.kanban-header.col-warning2 { background: #fde68a; color: #92400e; }
.kanban-header.col-success  { background: #d1fae5; color: #065f46; }

.kanban-body { display: flex; flex-direction: column; gap: 8px; }
.kanban-card { cursor: pointer; }
.kanban-card :deep(.el-card__body) { padding: 10px 12px; }
.task-no { font-size: 11px; color: #9ca3af; font-family: monospace; }
.task-title { font-size: 13px; font-weight: 500; margin: 4px 0 6px; line-height: 1.4; }
.task-meta { display: flex; gap: 6px; align-items: center; }
.meta-text { font-size: 11px; color: #6b7280; }
</style>
