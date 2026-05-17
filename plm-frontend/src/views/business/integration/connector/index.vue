<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="编码"><el-input v-model="queryParams.connectorCode" clearable style="width:200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="类型">
        <el-select v-model="queryParams.connectorType" clearable style="width:160px">
          <el-option v-for="d in biz_integration_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" clearable style="width:140px">
          <el-option v-for="d in biz_integration_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:integration:connector:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:integration:connector:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:integration:connector:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:integration:connector:export']">导出</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="40" />
      <el-table-column label="编码" prop="connectorCode" />
      <el-table-column label="名称" prop="connectorName" />
      <el-table-column label="类型" prop="connectorType" width="120">
        <template #default="s"><dict-tag :options="biz_integration_type" :value="s.row.connectorType" /></template>
      </el-table-column>
      <el-table-column label="鉴权" prop="authType" width="120">
        <template #default="s"><dict-tag :options="biz_integration_auth" :value="s.row.authType" /></template>
      </el-table-column>
      <el-table-column label="端点" prop="endpoint" :show-overflow-tooltip="true" />
      <el-table-column label="状态" prop="status" width="100">
        <template #default="s"><dict-tag :options="biz_integration_status" :value="s.row.status" /></template>
      </el-table-column>
      <el-table-column label="最后同步" prop="lastSyncAt" width="160" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="s">
          <el-button link type="success" icon="Connection" @click="handleTest(s.row)" v-hasPermi="['business:integration:connector:test']">测试</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(s.row)" v-hasPermi="['business:integration:connector:edit']">编辑</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(s.row)" v-hasPermi="['business:integration:connector:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="编码" prop="connectorCode"><el-input v-model="form.connectorCode" placeholder="如 FEISHU-MAIN" /></el-form-item>
        <el-form-item label="名称" prop="connectorName"><el-input v-model="form.connectorName" /></el-form-item>
        <el-form-item label="类型" prop="connectorType">
          <el-select v-model="form.connectorType" @change="onTypeChange" placeholder="选择">
            <el-option v-for="d in biz_integration_type" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="端点 endpoint"><el-input v-model="form.endpoint" :placeholder="endpointPlaceholder" /></el-form-item>
        <el-form-item label="鉴权" prop="authType">
          <el-select v-model="form.authType">
            <el-option v-for="d in biz_integration_auth" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="凭据 JSON (明文)">
          <el-input v-model="form.credentialJsonPlain" type="textarea" :rows="4" :placeholder="credentialPlaceholder" />
          <div style="font-size:11px;color:#909399">说明:本字段仅本次写入,后端 AES-256-GCM 加密后存储,响应中不返回。编辑时留空表示不修改。</div>
        </el-form-item>
        <el-form-item label="Webhook Secret">
          <el-input v-model="form.webhookSecret" placeholder="HMAC 验签密钥,例 GitLab Secret Token / 飞书 verification_token" />
        </el-form-item>
        <el-form-item label="配置 JSON"><el-input v-model="form.configJson" type="textarea" :rows="3" placeholder="如 {chatId:'oc_xxx'} 飞书机器人发消息目标" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="d in biz_integration_status" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="IntegrationConnector" lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import {
  listConnectors, getConnector, addConnector, updateConnector, delConnector,
  testConnector, exportConnector, type IntegrationConnector
} from '@/api/business/integration/connector'

const { biz_integration_type, biz_integration_auth, biz_integration_status } = useDict(
  'biz_integration_type', 'biz_integration_auth', 'biz_integration_status'
)

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref<IntegrationConnector[]>([])
const total = ref(0)
const ids = ref<number[]>([])
const single = ref(true)
const multiple = ref(true)

const queryParams = reactive({ pageNum: 1, pageSize: 10, connectorCode: '', connectorName: '', connectorType: '', status: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive<IntegrationConnector>({ status: '0', authType: 'app_secret' })
const rules = {
  connectorCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  connectorName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  connectorType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const endpointPlaceholder = computed(() => {
  if (form.connectorType === 'gitlab') return 'https://gitlab.com (或自建 https://gitlab.your-domain.com)'
  if (form.connectorType === 'feishu') return 'https://open.feishu.cn/open-apis (留空走默认)'
  return ''
})
const credentialPlaceholder = computed(() => {
  if (form.connectorType === 'feishu') return '{"appId":"cli_xxx","appSecret":"...","verificationToken":"...","encryptKey":""}'
  if (form.connectorType === 'gitlab') return '{"token":"glpat-xxxxxxxxxxxxxxxxxxxx"}'
  return '{}'
})
const onTypeChange = () => {
  if (form.connectorType === 'gitlab') form.authType = 'pat'
  else if (form.connectorType === 'feishu') form.authType = 'app_secret'
}

const getList = async () => {
  loading.value = true
  try {
    const r: any = await listConnectors(queryParams)
    dataList.value = r.rows ?? []
    total.value = r.total ?? 0
  } finally { loading.value = false }
}
const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => { Object.assign(queryParams, { pageNum: 1, pageSize: 10, connectorCode: '', connectorName: '', connectorType: '', status: '' }); getList() }
const handleSelectionChange = (sel: IntegrationConnector[]) => {
  ids.value = sel.map(s => s.id!).filter(Boolean) as number[]
  single.value = sel.length !== 1
  multiple.value = !sel.length
}
const resetForm = () => { Object.assign(form, { id: undefined, connectorCode: '', connectorName: '', connectorType: '', endpoint: '', authType: 'app_secret', credentialJsonPlain: '', webhookSecret: '', configJson: '', status: '0', remark: '' }) }
const handleAdd = () => { resetForm(); dialogTitle.value = '新增连接器'; dialogVisible.value = true }
const handleUpdate = async (row?: IntegrationConnector) => {
  resetForm()
  const id = row?.id ?? ids.value[0]
  const r: any = await getConnector(id!)
  Object.assign(form, r.data ?? {})
  form.credentialJsonPlain = ''
  dialogTitle.value = '修改连接器'
  dialogVisible.value = true
}
const submitForm = async () => {
  await formRef.value?.validate()
  if (form.id) { await updateConnector(form); ElMessage.success('修改成功') }
  else { await addConnector(form); ElMessage.success('新增成功') }
  dialogVisible.value = false
  getList()
}
const handleDelete = async (row?: IntegrationConnector) => {
  const delIds = (row?.id ? [row.id] : ids.value) as number[]
  await ElMessageBox.confirm(`确认删除 ${delIds.length} 个连接器?`, '提示', { type: 'warning' })
  await delConnector(delIds)
  ElMessage.success('删除成功')
  getList()
}
const handleTest = async (row: IntegrationConnector) => {
  const r: any = await testConnector(row.id!)
  if (r.code === 200 || r.data?.ok) {
    ElMessage.success(`连通 OK · ${r.data?.latencyMs}ms · ${r.data?.detail}`)
  } else {
    ElMessage.error(`连通失败 · ${r.msg ?? r.data?.detail}`)
  }
}
const handleExport = () => exportConnector(queryParams)

onMounted(getList)
</script>
