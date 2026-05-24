<!--
  我的任务 — 顶级菜单 /mytask (parent_id=0)
  按当前登录用户筛选 assigneeUserId
-->
<template>
  <div class="app-container my-task-page">
    <div class="page-header">
      <h2 class="page-title">✅ 我的任务</h2>
      <p class="page-subtitle">仅显示分配给当前用户的任务</p>
    </div>

    <el-card shadow="never">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">📋 任务列表 ({{ total }})</span>
          <el-input v-model="queryParams.title" placeholder="搜索标题" style="width: 220px" clearable @clear="getList" @keyup.enter="getList" />
        </div>
      </template>

      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="taskNo" width="160" />
        <el-table-column label="标题" prop="title" min-width="220" show-overflow-tooltip />
        <el-table-column label="优先级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="priorityTag(row.priority).type" size="small">{{ priorityTag(row.priority).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预估工时" width="100" align="center">
          <template #default="{ row }">{{ row.estimatedHours ? row.estimatedHours + 'h' : '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

const list = ref<any[]>([])
const total = ref(0)
const listLoading = ref(false)
const queryParams = reactive<any>({ pageNum: 1, pageSize: 15, title: '', assigneeUserId: 1 })

const priorityMap: any = {
  '00': { label: 'P0', type: 'danger' },
  '01': { label: 'P1', type: 'warning' },
  '02': { label: 'P2', type: 'info' }
}
function priorityTag(p?: string) { return priorityMap[p || '02'] || { label: p, type: 'info' } }

const statusMap: any = {
  '00': { label: '待开发', type: 'info' },
  '01': { label: '开发中', type: 'primary' },
  '02': { label: '代码评审', type: 'warning' },
  '03': { label: '测试中', type: 'warning' },
  '04': { label: '已完成', type: 'success' }
}
function statusTagFor(s?: string) { return statusMap[s || '00'] || { label: s, type: 'info' } }

async function getList() {
  listLoading.value = true
  try {
    const res: any = await request({ url: '/business/task/list', method: 'get', params: queryParams })
    list.value = res.rows || []
    total.value = res.total || 0
  } finally {
    listLoading.value = false
  }
}

onMounted(getList)
</script>

<style scoped>
.my-task-page { padding: 20px; }
.page-header { margin-bottom: 16px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
</style>
