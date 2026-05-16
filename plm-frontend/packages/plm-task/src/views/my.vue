<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="80px">
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="dict in status_options" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级">
        <el-select v-model="queryParams.priority" placeholder="全部" clearable>
          <el-option v-for="dict in priority_options" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="任务编号" align="center" prop="taskNo" width="160" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="迭代" align="center" prop="sprintId" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="status_options" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="优先级" align="center" prop="priority" width="100">
        <template #default="scope">
          <dict-tag :options="priority_options" :value="scope.row.priority" />
        </template>
      </el-table-column>
      <el-table-column label="预估" align="center" prop="estimatedHours" width="70" />
      <el-table-column label="MR" align="center" prop="mrUrl" width="220" :show-overflow-tooltip="true">
        <template #default="scope">
          <el-link v-if="scope.row.mrUrl" :href="scope.row.mrUrl" target="_blank" type="primary">{{ scope.row.mrBranch || '查看 MR' }}</el-link>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="MyTasks" lang="ts">
import { myTasks } from '../api'
import type { TaskForm, TaskQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const {
  biz_task_status: status_options,
  biz_task_priority: priority_options
} = toRefs<any>(proxy.useDict('biz_task_status', 'biz_task_priority'))

const list = ref<TaskForm[]>([])
const loading = ref(true)
const total = ref(0)

const queryParams = ref<TaskQuery>({
  pageNum: 1,
  pageSize: 10,
  status: undefined,
  priority: undefined
})

function getList() {
  loading.value = true
  myTasks(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

getList()
</script>
