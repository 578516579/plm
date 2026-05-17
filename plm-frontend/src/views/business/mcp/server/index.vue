<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="编码" prop="serverCode">
        <el-input v-model="queryParams.serverCode" placeholder="编码" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="名称" prop="serverName">
        <el-input v-model="queryParams.serverName" placeholder="名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="协议" prop="protocol">
        <el-select v-model="queryParams.protocol" placeholder="协议" clearable style="width: 160px">
          <el-option v-for="d in biz_mcp_protocol" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
          <el-option v-for="d in biz_mcp_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:mcp:server:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:mcp:server:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:mcp:server:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:mcp:server:export']">导出</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="40" align="center" />
      <el-table-column label="编码" prop="serverCode" />
      <el-table-column label="名称" prop="serverName" />
      <el-table-column label="协议" prop="protocol">
        <template #default="s"><dict-tag :options="biz_mcp_protocol" :value="s.row.protocol" /></template>
      </el-table-column>
      <el-table-column label="鉴权" prop="authType">
        <template #default="s"><dict-tag :options="biz_mcp_auth" :value="s.row.authType" /></template>
      </el-table-column>
      <el-table-column label="状态" prop="status">
        <template #default="s"><dict-tag :options="biz_mcp_status" :value="s.row.status" /></template>
      </el-table-column>
      <el-table-column label="最后心跳" prop="lastHealthAt" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="s">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(s.row)" v-hasPermi="['business:mcp:server:edit']">编辑</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(s.row)" v-hasPermi="['business:mcp:server:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="640px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="编码" prop="serverCode"><el-input v-model="form.serverCode" placeholder="如 plm-core" /></el-form-item>
        <el-form-item label="名称" prop="serverName"><el-input v-model="form.serverName" /></el-form-item>
        <el-form-item label="协议" prop="protocol">
          <el-select v-model="form.protocol" placeholder="选择协议">
            <el-option v-for="d in biz_mcp_protocol" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="端点" prop="endpoint"><el-input v-model="form.endpoint" placeholder="/mcp 或 https://..." /></el-form-item>
        <el-form-item label="鉴权" prop="authType">
          <el-select v-model="form.authType">
            <el-option v-for="d in biz_mcp_auth" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="OAuth Client ID"><el-input v-model="form.oauthClientId" /></el-form-item>
        <el-form-item label="OAuth Secret">
          <el-input v-model="form.oauthClientSecretPlain" type="password" show-password
                    placeholder="留空表示不修改;新建/轮换时填明文" />
          <div style="font-size:11px;color:#909399">提示:后端会用 MCP_ENCRYPT_KEY (AES-256-GCM) 加密后存储</div>
        </el-form-item>
        <el-form-item label="工具集 JSON"><el-input v-model="form.toolsJson" type="textarea" :rows="3" placeholder="JSON Schema 工具数组 (选填)" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="d in biz_mcp_status" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
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

<script setup name="McpServer" lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import { listServers, getServer, addServer, updateServer, delServer, exportServer, type McpServer } from '@/api/business/mcp/server'

const { biz_mcp_protocol, biz_mcp_auth, biz_mcp_status } = useDict('biz_mcp_protocol', 'biz_mcp_auth', 'biz_mcp_status')

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref<McpServer[]>([])
const total = ref(0)
const ids = ref<number[]>([])
const single = ref(true)
const multiple = ref(true)

const queryParams = reactive({ pageNum: 1, pageSize: 10, serverCode: '', serverName: '', protocol: '', status: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive<McpServer>({ status: '0', protocol: 'http', authType: 'token' })
const rules = {
  serverCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  serverName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const r: any = await listServers(queryParams)
    dataList.value = r.rows ?? []
    total.value = r.total ?? 0
  } finally { loading.value = false }
}

const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => {
  Object.assign(queryParams, { pageNum: 1, pageSize: 10, serverCode: '', serverName: '', protocol: '', status: '' })
  getList()
}
const handleSelectionChange = (sel: McpServer[]) => {
  ids.value = sel.map(s => s.id!).filter(Boolean) as number[]
  single.value = sel.length !== 1
  multiple.value = !sel.length
}
const resetForm = () => { Object.assign(form, { id: undefined, serverCode: '', serverName: '', protocol: 'http', endpoint: '', authType: 'token', oauthClientId: '', oauthClientSecretPlain: '', toolsJson: '', status: '0', description: '', remark: '' }) }
const handleAdd = () => { resetForm(); dialogTitle.value = '新增 MCP Server'; dialogVisible.value = true }
const handleUpdate = async (row?: McpServer) => {
  resetForm()
  const id = row?.id ?? ids.value[0]
  const r: any = await getServer(id!)
  Object.assign(form, r.data ?? {})
  form.oauthClientSecretPlain = ''   // 编辑时不显示已加密密文
  dialogTitle.value = '修改 MCP Server'
  dialogVisible.value = true
}
const submitForm = async () => {
  await formRef.value?.validate()
  if (form.id) { await updateServer(form); ElMessage.success('修改成功') }
  else { await addServer(form); ElMessage.success('新增成功') }
  dialogVisible.value = false
  getList()
}
const handleDelete = async (row?: McpServer) => {
  const delIds = (row?.id ? [row.id] : ids.value) as number[]
  await ElMessageBox.confirm(`确认删除 ${delIds.length} 个 MCP Server?`, '提示', { type: 'warning' })
  await delServer(delIds)
  ElMessage.success('删除成功')
  getList()
}
const handleExport = () => { exportServer(queryParams) }

onMounted(getList)
</script>
