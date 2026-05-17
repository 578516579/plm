<!--
  AI Agent 编排 — PRD §F3.5 + 原型 aiagents.html
  V2 多 Provider 集成 (2026-05-18):
    - 顶部加 "AI 集成总览" 徽章 (调 /ai/health)
    - 表单加 provider/modelName/difyWorkflowId 字段
    - 列表卡片展示 provider + model
-->
<template>
  <div class="app-container agent-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🤖 AI Agent 编排</h2>
        <p class="page-subtitle">多 Agent 协作,贯穿研发全链路:需求分析 → PRD 生成 → 代码审查 → 测试生成 → 发布评审 → 运维巡检</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="openAdd"><el-icon><Plus /></el-icon>&nbsp;新建 Agent</el-button>
        <el-button type="primary" @click="$message.info('Agent 调度中心,统一管理所有 Agent 启停和监控 (v0.6 接入)')">
          📡 Agent 调度中心
        </el-button>
      </div>
    </div>

    <!-- AI 集成总览 (V2) -->
    <el-card v-if="aiHealth" shadow="never" class="ai-health-card">
      <div class="ai-health-row">
        <span class="ai-health-label">🔌 AI 集成状态</span>
        <el-tag
          v-for="(usable, name) in aiHealth.providers"
          :key="name"
          :type="usable ? 'success' : 'info'"
          :effect="usable ? 'dark' : 'plain'"
          size="small"
          style="margin-right: 6px"
        >
          {{ providerLabel(name) }}: {{ usable ? '✓ 可用' : '· 未配置' }}
        </el-tag>
        <span class="ai-health-default">默认 provider: <strong>{{ aiHealth.defaultProvider }}</strong></span>
        <el-link type="primary" :underline="false" style="margin-left: 12px" @click="loadHealth">
          🔄 刷新
        </el-link>
      </div>
      <div v-if="aiHealth.openaiEnabled || aiHealth.anthropicEnabled" class="ai-health-detail">
        <span v-if="aiHealth.openaiEnabled">
          OpenAI 兼容: {{ aiHealth.openaiBaseUrl }} (默认模型 {{ aiHealth.openaiModel }})
        </span>
        <span v-if="aiHealth.anthropicEnabled" style="margin-left: 16px">
          Anthropic: {{ aiHealth.anthropicBaseUrl }} (默认模型 {{ aiHealth.anthropicModel }})
        </span>
      </div>
    </el-card>

    <!-- 3 统计卡 -->
    <el-row :gutter="14" class="stat-row">
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">今日总调用</div>
          <div class="stat-value pu">{{ totalCalls }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">运行中</div>
          <div class="stat-value success">{{ runningCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">平均成功率</div>
          <div class="stat-value blue">{{ avgSuccessRate }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Agent grid -->
    <el-row :gutter="14" style="margin-top: 14px">
      <el-col v-for="a in list" :key="a.agentId" :span="12" style="margin-bottom: 14px">
        <el-card shadow="hover" class="agent-card">
          <div class="agent-head">
            <div class="agent-icon">{{ agentIcon(a.agentType) }}</div>
            <div class="agent-meta">
              <div class="agent-name">{{ a.agentName }}</div>
              <div class="agent-type">
                {{ agentTypeLabel(a.agentType) }}
                ·
                <el-tag size="small" :type="providerTag(a.provider)" effect="plain">
                  {{ providerLabel(a.provider) }}
                </el-tag>
                <span v-if="a.modelName" class="model-chip">{{ a.modelName }}</span>
                <span v-else-if="a.provider === 'dify' && a.difyWorkflowId" class="model-chip">{{ a.difyWorkflowId }}</span>
              </div>
            </div>
            <el-tag :type="agentStatusTag(a.status)" size="small">{{ agentStatusLabel(a.status) }}</el-tag>
          </div>
          <div class="agent-desc">{{ a.description || '(无描述)' }}</div>
          <el-row :gutter="10" class="agent-stats">
            <el-col :span="12"><div class="stat-mini">调用 <strong>{{ a.totalCalls || 0 }}</strong></div></el-col>
            <el-col :span="12"><div class="stat-mini">成功率 <strong>{{ a.successRate || 0 }}%</strong></div></el-col>
          </el-row>
          <div class="agent-actions">
            <el-button link type="primary" @click="loadAgent(a)">编辑</el-button>
            <el-button link type="success" @click="invokeNow(a)">▶ 调用</el-button>
            <el-button link type="danger" @click="handleDelete(a)">删除</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 协作流程图 -->
    <el-card shadow="never" class="flow-card">
      <template #header><span style="color:#fff" class="card-title">🔗 Agent 协作流程 (研发全链路)</span></template>
      <div class="flow-row">
        <div v-for="(step, i) in flowSteps" :key="step" class="flow-step">
          <div class="flow-box">{{ step }}</div>
          <span v-if="i < flowSteps.length - 1" class="flow-arrow">→</span>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.agentId ? '编辑 Agent' : '新建 AI Agent'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="Agent 名称" prop="agentName">
          <el-input v-model="form.agentName" placeholder="如:需求分析 Agent" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="类型" prop="agentType">
              <el-select v-model="form.agentType" style="width: 100%">
                <el-option label="📋 需求分析" value="requirement" />
                <el-option label="📝 PRD 生成"  value="prd" />
                <el-option label="🔍 代码审查" value="code" />
                <el-option label="🧪 测试生成" value="test" />
                <el-option label="🚀 发布评审" value="release" />
                <el-option label="🛠️ 运维巡检" value="ops" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="运行中" value="00" />
                <el-option label="已停止" value="01" />
                <el-option label="错误"   value="02" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- V2: provider 选择 -->
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="AI Provider" prop="provider">
              <el-select v-model="form.provider" style="width: 100%" @change="onProviderChange">
                <el-option label="🟢 Mock (占位/降级)" value="mock" />
                <el-option label="🔷 Dify (workflow 编排)" value="dify" />
                <el-option label="🟡 OpenAI 兼容 (DeepSeek/通义/Moonshot...)" value="openai" />
                <el-option label="🟠 Anthropic Claude" value="anthropic" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 非 dify provider:输入模型名 -->
            <el-form-item v-if="form.provider !== 'dify'" label="模型名" prop="modelName">
              <el-input v-model="form.modelName" :placeholder="modelHint" clearable />
            </el-form-item>
            <!-- dify provider:输入 workflow id -->
            <el-form-item v-else label="Workflow ID" prop="difyWorkflowId">
              <el-input v-model="form.difyWorkflowId" placeholder="如 wf-xxxxxxxx" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <div v-if="modelExamples" class="provider-hint">
          <span class="hint-label">💡 推荐:</span>
          <el-link
            v-for="m in modelExamples"
            :key="m"
            type="primary"
            :underline="false"
            style="margin-right: 8px"
            @click="form.modelName = m"
          >{{ m }}</el-link>
        </div>

        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="一句话说明此 Agent 用途" />
        </el-form-item>
        <el-form-item label="System Prompt" prop="promptTemplate">
          <el-input
            v-model="form.promptTemplate"
            type="textarea" :rows="4"
            placeholder="System Prompt 模板,定义 Agent 行为(如:你是 PLM 资深需求分析师,擅长把模糊业务需求拆解为可落地的用户故事...)"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">{{ form.agentId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  listAiAgent, addAiAgent, updateAiAgent, delAiAgent, getAiAgent, invokeAiAgent,
  getAiHealth,
  type AiAgent, type AiHealthInfo
} from '@/api/business/ai-agent'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()
const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const aiHealth = ref<AiHealthInfo | null>(null)

const emptyForm = (): AiAgent => ({
  agentName: '',
  agentType: 'requirement',
  provider: 'mock',
  modelName: '',
  difyWorkflowId: '',
  description: '',
  promptTemplate: '',
  status: '00',
  authorUserId: Number(userStore.id) || 1
})
const form = reactive<AiAgent>(emptyForm())
const rules = {
  agentName: [{ required: true, message: '请输入 Agent 名称', trigger: 'blur' }],
  agentType: [{ required: true, message: '请选择 Agent 类型', trigger: 'change' }],
  provider:  [{ required: true, message: '请选择 AI Provider', trigger: 'change' }]
}

const list = ref<AiAgent[]>([])
const flowSteps = ['📋 需求分析', '📝 PRD 生成', '🔍 代码审查', '🧪 测试生成', '🚀 发布评审', '🛠️ 运维巡检']

const totalCalls = computed(() => list.value.reduce((s, x) => s + (x.totalCalls || 0), 0))
const runningCount = computed(() => list.value.filter(x => x.status === '00').length)
const avgSuccessRate = computed(() => {
  const r = list.value.map(x => Number(x.successRate) || 0).filter(x => x > 0)
  if (!r.length) return 0
  return Math.round(r.reduce((s, n) => s + n, 0) / r.length)
})

/** 不同 provider 的推荐模型名(点击填入) */
const modelExamples = computed<string[] | null>(() => {
  switch (form.provider) {
    case 'openai': return ['gpt-4o-mini', 'gpt-4o', 'deepseek-chat', 'deepseek-reasoner', 'qwen-max', 'moonshot-v1-8k', 'glm-4-plus']
    case 'anthropic': return ['claude-sonnet-4-5', 'claude-opus-4', 'claude-haiku-4']
    case 'mock': return null
    case 'dify': return null
    default: return null
  }
})
const modelHint = computed(() => {
  switch (form.provider) {
    case 'openai':    return '如 gpt-4o-mini / deepseek-chat / qwen-max'
    case 'anthropic': return '如 claude-sonnet-4-5'
    case 'mock':      return '(mock 模式可不填)'
    default:          return ''
  }
})

function onProviderChange() {
  // 切换 provider 时清空 model 字段,避免误填
  if (form.provider === 'dify') {
    form.modelName = ''
  } else {
    form.difyWorkflowId = ''
  }
}

function agentIcon(t?: string) {
  return ({ requirement: '📋', prd: '📝', code: '🔍', test: '🧪', release: '🚀', ops: '🛠️' } as Record<string,string>)[t || ''] || '🤖'
}
function agentTypeLabel(t?: string) {
  return ({ requirement: '需求分析', prd: 'PRD 生成', code: '代码审查', test: '测试生成', release: '发布评审', ops: '运维巡检' } as Record<string,string>)[t || ''] || t || '-'
}
function providerLabel(p?: string | number) {
  return ({ mock: 'Mock', dify: 'Dify', openai: 'OpenAI 兼容', anthropic: 'Anthropic' } as Record<string,string>)[String(p || '')] || String(p || '-')
}
function providerTag(p?: string): any {
  return ({ mock: 'info', dify: 'primary', openai: 'success', anthropic: 'warning' } as Record<string,string>)[p || ''] || 'info'
}
function agentStatusLabel(s?: string) {
  return ({ '00': '运行中', '01': '已停止', '02': '错误' } as Record<string,string>)[s || ''] || '-'
}
function agentStatusTag(s?: string): any {
  return ({ '00': 'success', '01': 'info', '02': 'danger' } as Record<string,string>)[s || ''] || 'info'
}

async function loadHealth() {
  try {
    const res: any = await getAiHealth()
    if (res.code === 200) {
      aiHealth.value = {
        defaultProvider:   res.defaultProvider,
        providers:         res.providers || {},
        openaiEnabled:     res.openaiEnabled,
        openaiBaseUrl:     res.openaiBaseUrl,
        openaiModel:       res.openaiModel,
        anthropicEnabled:  res.anthropicEnabled,
        anthropicBaseUrl:  res.anthropicBaseUrl,
        anthropicModel:    res.anthropicModel,
        difyUsable:        res.difyUsable
      }
    }
  } catch { /* health 端点失败不阻塞列表 */ }
}

async function getList() {
  try { const res: any = await listAiAgent({ pageSize: 100 }); list.value = res.rows || [] } catch {}
}

function openAdd() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function loadAgent(a: AiAgent) {
  if (!a.agentId) return
  const res: any = await getAiAgent(a.agentId)
  if (res.code === 200 && res.data) { Object.assign(form, res.data); dialogVisible.value = true }
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.agentId) { await updateAiAgent(form); ElMessage.success('更新成功') }
    else { await addAiAgent(form); ElMessage.success('创建成功') }
    dialogVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') } finally { saving.value = false }
}

async function invokeNow(a: AiAgent) {
  if (!a.agentId) return
  try {
    await invokeAiAgent(a.agentId, {})
    ElMessage.success(`${a.agentName} 调用成功 (provider=${a.provider || 'mock'})`)
    await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '调用失败') }
}

async function handleDelete(a: AiAgent) {
  if (!a.agentId) return
  await ElMessageBox.confirm(`删除 Agent "${a.agentName}"?`, '提示', { type: 'warning' })
  await delAiAgent(a.agentId!); ElMessage.success('删除成功'); await getList()
}

onMounted(async () => { await Promise.all([getList(), loadHealth()]) })
</script>

<style scoped>
.agent-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 14px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; }
.card-title { font-weight: 600; }

.ai-health-card { margin-bottom: 14px; background: linear-gradient(135deg, #f0fdfa 0%, #ecfdf5 100%); border: 1px solid #d1fae5; }
.ai-health-card :deep(.el-card__body) { padding: 12px 16px; }
.ai-health-row { display: flex; align-items: center; flex-wrap: wrap; gap: 4px; }
.ai-health-label { font-weight: 600; color: #047857; margin-right: 8px; }
.ai-health-default { color: #6b7280; font-size: 13px; margin-left: 10px; }
.ai-health-default strong { color: #111827; }
.ai-health-detail { margin-top: 8px; color: #4b5563; font-size: 12px; }

.stat-row :deep(.el-card__body) { padding: 16px; text-align: center; }
.stat-label { color: #6b7280; font-size: 12px; margin-bottom: 6px; }
.stat-value { font-size: 28px; font-weight: 700; }
.stat-value.pu { color: #7c3aed; }
.stat-value.success { color: #10b981; }
.stat-value.blue { color: #3b82f6; }

.agent-card { min-height: 200px; }
.agent-head { display: flex; gap: 10px; align-items: center; margin-bottom: 10px; }
.agent-icon { font-size: 28px; }
.agent-meta { flex: 1; overflow: hidden; }
.agent-name { font-weight: 700; font-size: 14px; color: #111827; }
.agent-type { color: #6b7280; font-size: 12px; margin-top: 2px; display: flex; gap: 6px; align-items: center; flex-wrap: wrap; }
.model-chip { font-family: 'Courier New', monospace; font-size: 11px; color: #4b5563; background: #f3f4f6; padding: 2px 6px; border-radius: 4px; }
.agent-desc { color: #4b5563; font-size: 12.5px; line-height: 1.6; margin-bottom: 10px; min-height: 30px; }
.agent-stats { padding: 8px 0; border-top: 1px solid #f3f4f6; }
.stat-mini { font-size: 12px; color: #6b7280; }
.stat-mini strong { color: #2d7a4f; font-size: 13px; }
.agent-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 8px; }

.provider-hint { margin: -10px 0 14px 120px; font-size: 12px; color: #6b7280; }
.hint-label { margin-right: 8px; }

.flow-card { background: linear-gradient(135deg, #1e1b4b, #1e3a5f); margin-top: 14px; }
.flow-card :deep(.el-card__header), .flow-card :deep(.el-card__body) { color: #fff; }
.flow-row { display: flex; gap: 6px; flex-wrap: wrap; align-items: center; }
.flow-step { display: flex; gap: 6px; align-items: center; }
.flow-box { background: rgba(255,255,255,.12); border-radius: 7px; padding: 7px 12px; font-size: 12.5px; }
.flow-arrow { color: rgba(255,255,255,.5); font-size: 16px; }
</style>
