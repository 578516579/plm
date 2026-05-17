<template>
  <div class="app-container">

    <!-- 顶部汇总卡片 -->
    <el-row :gutter="16" class="mb16">
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">今日总调用</div>
          <div class="stat-value purple">{{ totalCallsToday }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">运行中</div>
          <div class="stat-value green">{{ runningCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">平均成功率</div>
          <div class="stat-value blue">{{ avgSuccessRate }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 研发全链路流程条 -->
    <el-card shadow="never" class="pipeline-card mb16">
      <div class="pipeline-title">🔗 Agent 协作流程（研发全链路）</div>
      <div class="pipeline-flow">
        <span class="pipeline-node">📋 需求分析Agent</span>
        <span class="pipeline-arrow">→</span>
        <span class="pipeline-node">📄 PRD生成(Dify)</span>
        <span class="pipeline-arrow">→</span>
        <span class="pipeline-node">🔍 代码审查Agent</span>
        <span class="pipeline-arrow">→</span>
        <span class="pipeline-node">🧪 测试用例Agent</span>
        <span class="pipeline-arrow">→</span>
        <span class="pipeline-node">🚀 发布评审Agent</span>
        <span class="pipeline-arrow">→</span>
        <span class="pipeline-node">🛡️ 运维巡检Agent</span>
      </div>
    </el-card>

    <!-- 搜索条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="Agent编号" prop="agentNo">
        <el-input v-model="queryParams.agentNo" placeholder="请输入Agent编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="Agent名称" prop="agentName">
        <el-input v-model="queryParams.agentName" placeholder="请输入Agent名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="适用岗位" prop="agentRole">
        <el-select v-model="queryParams.agentRole" placeholder="全部岗位" clearable style="width: 140px">
          <el-option v-for="dict in agent_role_options" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="Agent类型" prop="agentType">
        <el-select v-model="queryParams.agentType" placeholder="全部类型" clearable style="width: 140px">
          <el-option v-for="dict in agent_type_options" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="dict in agent_status_options" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:ai-agent:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:ai-agent:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:ai-agent:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:ai-agent:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button value="card">卡片</el-radio-button>
          <el-radio-button value="table">列表</el-radio-button>
        </el-radio-group>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 卡片视图 -->
    <el-row v-if="viewMode === 'card'" :gutter="16" v-loading="loading">
      <el-col v-for="item in list" :key="item.id" :xs="24" :sm="12" :lg="8" class="mb16">
        <el-card shadow="hover" class="agent-card">
          <div class="agent-header">
            <div>
              <div class="agent-name">{{ item.agentName }}</div>
              <div class="agent-desc">{{ item.description }}</div>
            </div>
            <el-tag :type="statusTagType(item.status!)" size="small" effect="dark">
              <span class="status-dot" :class="statusDotClass(item.status!)"></span>
              {{ statusLabel(item.status!) }}
            </el-tag>
          </div>

          <div class="agent-tags">
            <el-tag type="info" size="small" effect="plain">{{ item.modelName }}</el-tag>
            <el-tag v-if="item.agentRole" size="small" effect="plain" class="ml4">
              <dict-tag :options="agent_role_options" :value="item.agentRole" />
            </el-tag>
            <el-tag v-if="item.agentType" size="small" effect="plain" class="ml4">
              <dict-tag :options="agent_type_options" :value="item.agentType" />
            </el-tag>
          </div>

          <el-row class="agent-metrics">
            <el-col :span="8" class="metric">
              <div class="metric-val purple">{{ item.callsToday ?? 0 }}</div>
              <div class="metric-lbl">今日调用</div>
            </el-col>
            <el-col :span="8" class="metric">
              <div class="metric-val green">{{ item.successRate ?? 100 }}%</div>
              <div class="metric-lbl">成功率</div>
            </el-col>
            <el-col :span="8" class="metric">
              <div class="metric-val amber">{{ item.avgLatency ?? '-' }}</div>
              <div class="metric-lbl">均响应</div>
            </el-col>
          </el-row>

          <div class="agent-actions">
            <el-button
              v-if="item.status === '0'"
              size="small" plain
              @click="handleToggleStatus(item, '1')"
              v-hasPermi="['business:ai-agent:edit']"
            >⏸ 暂停</el-button>
            <el-button
              v-else
              size="small" type="primary" plain
              @click="handleToggleStatus(item, '0')"
              v-hasPermi="['business:ai-agent:edit']"
            >▶ 启动</el-button>
            <el-button size="small" @click="handleUpdate(item)" v-hasPermi="['business:ai-agent:edit']">⚙️ 配置</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(item)" v-hasPermi="['business:ai-agent:remove']">删除</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="!loading && list.length === 0" :span="24">
        <el-empty description="暂无 Agent，点击「新增」添加" />
      </el-col>
    </el-row>

    <!-- 列表视图 -->
    <template v-if="viewMode === 'table'">
      <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="编号" align="center" prop="agentNo" width="140" />
        <el-table-column label="Agent名称" align="center" prop="agentName" :show-overflow-tooltip="true" />
        <el-table-column label="适用岗位" align="center" prop="agentRole" width="110">
          <template #default="scope">
            <dict-tag :options="agent_role_options" :value="scope.row.agentRole" />
          </template>
        </el-table-column>
        <el-table-column label="类型" align="center" prop="agentType" width="100">
          <template #default="scope">
            <dict-tag :options="agent_type_options" :value="scope.row.agentType" />
          </template>
        </el-table-column>
        <el-table-column label="AI模型" align="center" prop="modelName" width="160" :show-overflow-tooltip="true" />
        <el-table-column label="状态" align="center" prop="status" width="90">
          <template #default="scope">
            <dict-tag :options="agent_status_options" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="今日调用" align="center" prop="callsToday" width="90" />
        <el-table-column label="成功率" align="center" width="90">
          <template #default="scope">{{ scope.row.successRate }}%</template>
        </el-table-column>
        <el-table-column label="均响应" align="center" prop="avgLatency" width="90" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === '0'"
              link type="warning"
              @click="handleToggleStatus(scope.row, '1')"
              v-hasPermi="['business:ai-agent:edit']"
            >暂停</el-button>
            <el-button
              v-else
              link type="success"
              @click="handleToggleStatus(scope.row, '0')"
              v-hasPermi="['business:ai-agent:edit']"
            >启动</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:ai-agent:edit']">配置</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:ai-agent:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </template>

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="Agent编号" prop="agentNo">
              <el-input v-model="form.agentNo" placeholder="留空自动生成 AGT-YYYY-NNNN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Agent名称" prop="agentName">
              <el-input v-model="form.agentName" placeholder="请输入Agent名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用岗位" prop="agentRole">
              <el-select v-model="form.agentRole" placeholder="请选择岗位" style="width: 100%">
                <el-option v-for="dict in agent_role_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Agent类型" prop="agentType">
              <el-select v-model="form.agentType" placeholder="请选择类型" style="width: 100%">
                <el-option v-for="dict in agent_type_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="AI模型" prop="modelName">
              <el-select v-model="form.modelName" placeholder="请选择模型" allow-create filterable style="width: 100%">
                <el-option label="DeepSeek-V3" value="DeepSeek-V3" />
                <el-option label="DeepSeek-R1" value="DeepSeek-R1" />
                <el-option label="Claude Sonnet 4.6" value="Claude Sonnet 4.6" />
                <el-option label="Claude Opus 4.7" value="Claude Opus 4.7" />
                <el-option label="GPT-4o" value="GPT-4o" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                <el-option v-for="dict in agent_status_options" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="Dify工作流ID" prop="difyFlowId">
              <el-input v-model="form.difyFlowId" placeholder="如 requirements-flow" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="工具列表" prop="toolsJson">
              <el-input v-model="form.toolsJson" placeholder='如 ["agrikb_search","req_template"]' />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="平均响应" prop="avgLatency">
              <el-input v-model="form.avgLatency" placeholder="如 1.8s" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成功率(%)" prop="successRate">
              <el-input-number v-model="form.successRate as number" :min="0" :max="100" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { listAiAgent, getAiAgent, addAiAgent, updateAiAgent, delAiAgent, changeAgentStatus } from '../api'
import type { AiAgentForm, AiAgentQuery } from '../types'

defineOptions({ name: 'AiAgent' })

const { proxy } = getCurrentInstance() as any
const {
  biz_agent_role: agent_role_options,
  biz_agent_type: agent_type_options,
  biz_agent_status: agent_status_options
} = toRefs<any>(proxy.useDict('biz_agent_role', 'biz_agent_type', 'biz_agent_status'))

const list = ref<AiAgentForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const viewMode = ref<'card' | 'table'>('card')

const dialog = reactive({ title: '', visible: false })
const form = ref<AiAgentForm>({})

const queryParams = ref<AiAgentQuery>({
  pageNum: 1,
  pageSize: 20,
  agentNo: undefined,
  agentName: undefined,
  agentRole: undefined,
  agentType: undefined,
  status: undefined
})

const rules = {
  agentName: [{ required: true, message: 'Agent名称不能为空', trigger: 'blur' }],
  modelName: [{ required: true, message: 'AI模型不能为空', trigger: 'change' }]
}

// 汇总指标（基于当前列表）
const totalCallsToday = computed(() => list.value.reduce((s, a) => s + (a.callsToday ?? 0), 0))
const runningCount = computed(() => list.value.filter(a => a.status === '0').length)
const avgSuccessRate = computed(() => {
  if (!list.value.length) return 0
  const avg = list.value.reduce((s, a) => s + Number(a.successRate ?? 100), 0) / list.value.length
  return Math.round(avg)
})

function getList() {
  loading.value = true
  listAiAgent(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = { agentName: undefined, agentRole: undefined, agentType: undefined, modelName: undefined, status: '1', description: undefined }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection: AiAgentForm[]) {
  ids.value = selection.map(item => item.id!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); dialog.title = '新增 AI Agent'; dialog.visible = true }

function handleUpdate(row?: AiAgentForm) {
  reset()
  const id = row?.id ?? ids.value[0]
  getAiAgent(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '配置 AI Agent'
    dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.id ? updateAiAgent : addAiAgent
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.id ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: AiAgentForm) {
  const toDelete = row?.id ? [row.id] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 个 Agent？').then(() =>
    delAiAgent(toDelete as number[])
  ).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('business/ai-agent/export', { ...queryParams.value }, 'AI_Agent_' + new Date().getTime() + '.xlsx')
}

function handleToggleStatus(row: AiAgentForm, newStatus: string) {
  const label = newStatus === '0' ? '启动' : '暂停'
  proxy.$modal.confirm(`确认${label} Agent「${row.agentName}」？`).then(() =>
    changeAgentStatus(row.id!, newStatus)
  ).then(() => {
    row.status = newStatus
    proxy.$modal.msgSuccess(`${label}成功`)
  }).catch(() => {})
}

// 状态辅助
function statusLabel(status: string) {
  const map: Record<string, string> = { '0': '运行中', '1': '待机', '2': '异常' }
  return map[status] ?? status
}
function statusTagType(status: string): '' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<string, '' | 'success' | 'warning' | 'info' | 'danger'> = { '0': 'success', '1': 'info', '2': 'danger' }
  return map[status] ?? 'info'
}
function statusDotClass(status: string) {
  return { 'dot-running': status === '0', 'dot-idle': status === '1', 'dot-error': status === '2' }
}

getList()
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.ml4 { margin-left: 4px; }

/* 汇总卡片 */
.stat-card { text-align: center; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 6px; }
.stat-value { font-size: 28px; font-weight: 800; }
.stat-value.purple { color: #7c3aed; }
.stat-value.green  { color: #10b981; }
.stat-value.blue   { color: #3b82f6; }

/* 全链路流程 */
.pipeline-card { background: linear-gradient(135deg, #1e1b4b, #1e3a5f); }
.pipeline-title { color: #fff; font-weight: 700; margin-bottom: 12px; }
.pipeline-flow { display: flex; gap: 6px; flex-wrap: wrap; align-items: center; font-size: 12px; }
.pipeline-node { background: rgba(255,255,255,.12); border-radius: 7px; padding: 6px 10px; color: #fff; }
.pipeline-arrow { color: rgba(255,255,255,.4); }

/* Agent 卡片 */
.agent-card { border-top: 3px solid #7c3aed; }
.agent-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; }
.agent-name { font-weight: 700; font-size: 14px; margin-bottom: 3px; }
.agent-desc { font-size: 12px; color: #909399; }
.agent-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 12px; }

/* 状态指示点 */
.status-dot { display: inline-block; width: 7px; height: 7px; border-radius: 50%; background: currentColor; margin-right: 3px; }

/* 指标行 */
.agent-metrics { text-align: center; margin-bottom: 12px; border-top: 1px solid #f0f0f0; padding-top: 10px; }
.metric-val { font-size: 20px; font-weight: 800; }
.metric-val.purple { color: #7c3aed; }
.metric-val.green  { color: #10b981; }
.metric-val.amber  { color: #f59e0b; }
.metric-lbl { font-size: 11px; color: #909399; }

.agent-actions { display: flex; gap: 6px; flex-wrap: wrap; }
</style>
