<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="工具名"><el-input v-model="queryParams.toolName" clearable style="width:200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="调用方"><el-input v-model="queryParams.callerType" clearable style="width:140px" placeholder="user/agent/system" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="结果">
        <el-select v-model="queryParams.resultStatus" clearable style="width:140px">
          <el-option v-for="d in biz_audit_result" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:mcp:audit:export']">导出</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="时间" prop="callTime" width="170" />
      <el-table-column label="工具" prop="toolName" />
      <el-table-column label="调用方类型" prop="callerType" width="110" />
      <el-table-column label="调用方" prop="callerId" />
      <el-table-column label="结果" prop="resultStatus" width="100">
        <template #default="s"><dict-tag :options="biz_audit_result" :value="s.row.resultStatus" /></template>
      </el-table-column>
      <el-table-column label="耗时(ms)" prop="latencyMs" width="100" align="right" />
      <el-table-column label="摘要" prop="resultBrief" :show-overflow-tooltip="true" />
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="McpAudit" lang="ts">
import { useDict } from '@/utils/dict'
import { listAudits, exportAudit, type McpToolAudit } from '@/api/business/mcp/audit'

const { biz_audit_result } = useDict('biz_audit_result')

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref<McpToolAudit[]>([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, toolName: '', callerType: '', resultStatus: '' })

const getList = async () => {
  loading.value = true
  try {
    const r: any = await listAudits(queryParams)
    dataList.value = r.rows ?? []
    total.value = r.total ?? 0
  } finally { loading.value = false }
}
const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { Object.assign(queryParams, { pageNum: 1, pageSize: 10, toolName: '', callerType: '', resultStatus: '' }); getList() }
const handleExport = () => exportAudit(queryParams)
onMounted(getList)
</script>
