<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="事件"><el-input v-model="queryParams.eventType" placeholder="feishu.* / gitlab.*" clearable style="width:220px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="验签">
        <el-select v-model="queryParams.signatureVerified" clearable style="width:120px">
          <el-option label="通过" value="1" />
          <el-option label="失败" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.processStatus" clearable style="width:140px">
          <el-option v-for="d in biz_webhook_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:integration:webhook:export']">导出</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="时间" prop="createTime" width="170" />
      <el-table-column label="连接器" prop="connectorId" width="100" />
      <el-table-column label="事件类型" prop="eventType" />
      <el-table-column label="外部 ID" prop="externalEventId" :show-overflow-tooltip="true" />
      <el-table-column label="验签" prop="signatureVerified" width="80">
        <template #default="s">
          <el-tag v-if="s.row.signatureVerified === '1'" type="success" size="small">通过</el-tag>
          <el-tag v-else type="danger" size="small">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="processStatus" width="100">
        <template #default="s"><dict-tag :options="biz_webhook_status" :value="s.row.processStatus" /></template>
      </el-table-column>
      <el-table-column label="重试" prop="retryCount" width="60" align="center" />
      <el-table-column label="错误" prop="processError" :show-overflow-tooltip="true" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="s">
          <el-button v-if="s.row.processStatus === '3'" link type="primary" icon="RefreshRight" @click="handleRetry(s.row)" v-hasPermi="['business:integration:webhook:retry']">重试</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="IntegrationWebhook" lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listEvents, retryEvent, exportEvents, type WebhookEvent } from '@/api/business/integration/webhook'

const { biz_webhook_status } = (window as any).proxy?.useDict?.('biz_webhook_status') ?? { biz_webhook_status: ref([]) }

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref<WebhookEvent[]>([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, eventType: '', signatureVerified: '', processStatus: '' })

const getList = async () => {
  loading.value = true
  try {
    const r: any = await listEvents(queryParams)
    dataList.value = r.rows ?? []
    total.value = r.total ?? 0
  } finally { loading.value = false }
}
const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { Object.assign(queryParams, { pageNum: 1, pageSize: 10, eventType: '', signatureVerified: '', processStatus: '' }); getList() }
const handleRetry = async (row: WebhookEvent) => {
  await retryEvent(row.id!)
  ElMessage.success('已触发重试')
  getList()
}
const handleExport = () => exportEvents(queryParams)
onMounted(getList)
</script>
