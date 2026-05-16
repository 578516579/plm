<template>
  <div class="app-container">
    <el-form :inline="true" class="kanban-toolbar">
      <el-form-item label="项目ID">
        <el-input v-model="projectId" placeholder="必填" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="迭代ID">
        <el-input v-model="sprintId" placeholder="可空，全部迭代" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="loadKanban">加载</el-button>
        <el-button icon="Refresh" @click="loadKanban">刷新</el-button>
      </el-form-item>
    </el-form>

    <div class="kanban-board" v-loading="loading">
      <div v-for="col in columns" :key="col.status" class="kanban-column">
        <div class="kanban-column-header">
          <span class="title">{{ col.label }}</span>
          <el-badge :value="col.count" class="badge" type="info" />
        </div>
        <div class="kanban-cards">
          <el-card
            v-for="task in col.tasks"
            :key="task.taskId"
            class="kanban-card"
            shadow="hover"
          >
            <div class="card-no">{{ task.taskNo }}</div>
            <div class="card-title">{{ task.title }}</div>
            <div class="card-meta">
              <dict-tag :options="priority_options" :value="task.priority" />
              <span class="hours">{{ task.estimatedHours || '-' }}h</span>
            </div>
          </el-card>
          <div v-if="col.tasks.length === 0" class="kanban-empty">暂无任务</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="TaskKanban" lang="ts">
import { kanbanTasks } from '../api'
import type { KanbanColumn } from '../types'

const { proxy } = getCurrentInstance() as any
const { biz_task_priority: priority_options } = toRefs<any>(proxy.useDict('biz_task_priority'))

const projectId = ref<string>('')
const sprintId = ref<string>('')
const loading = ref(false)
const columns = ref<KanbanColumn[]>([])

function loadKanban() {
  if (!projectId.value) {
    proxy.$modal.msgWarning('请先输入项目 ID')
    return
  }
  loading.value = true
  kanbanTasks(projectId.value, sprintId.value || undefined)
    .then((res: any) => {
      columns.value = res.data.columns
    })
    .finally(() => {
      loading.value = false
    })
}
</script>

<style scoped>
.kanban-toolbar {
  margin-bottom: 16px;
}

.kanban-board {
  display: flex;
  gap: 16px;
  overflow-x: auto;
}

.kanban-column {
  flex: 1;
  min-width: 240px;
  background: #f5f7fa;
  border-radius: 6px;
  padding: 12px;
}

.kanban-column-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 600;
}

.kanban-column-header .title {
  font-size: 15px;
}

.kanban-cards {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.kanban-card {
  cursor: pointer;
}

.card-no {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #606266;
}

.card-meta .hours {
  font-family: monospace;
}

.kanban-empty {
  text-align: center;
  color: #c0c4cc;
  padding: 16px 0;
  font-size: 13px;
}
</style>
