<!--
  AI Agent 编排 — 原型 aiagents.html
  3 统计卡 + Agent grid + 协作流程图
-->
<template>
  <div class="app-container agent-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🤖 AI Agent 编排</h2>
        <p class="page-subtitle">多 Agent 协作,贯穿研发全链路:需求分析 → 代码审查 → 测试生成 → 发布评审 → 运维巡检</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="openAdd"><el-icon><Plus /></el-icon>&nbsp;新建 Agent</el-button>
        <el-button type="primary" @click="$message.info('Agent 调度中心,统一管理所有 Agent 启停和监控 (v0.6 接入)')">
          📡 Agent 调度中心
        </el-button>
      </div>
    </div>

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
              <div class="agent-type">{{ agentTypeLabel(a.agentType) }} · {{ providerLabel(a.modelProvider) }}</div>
            </div>
            <el-tag :type="agentStatusTag(a.status)" size="small">{{ agentStatusLabel(a.status) }}</el-tag>
          </div>
          <div class="agent-desc">{{ a.description || '(无描述)' }}</div>
          <el-row :gutter="10" class="agent-stats">
            <el-col :span="8"><div class="stat-mini">调用 <strong>{{ a.totalCalls || 0 }}</strong></div></el-col>
            <el-col :span="8"><div class="stat-mini">成功率 <strong>{{ a.successRate || 0 }}%</strong></div></el-col>
            <el-col :span="8"><div class="stat-mini">延迟 <strong>{{ a.avgLatencyMs || 0 }}ms</strong></div></el-col>
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

    <el-dialog v-model="dialogVisible" :title="form.agentId ? '编辑 Agent' : '新建 AI Agent'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="Agent 名称" prop="agentName" required>
          <el-input v-model="form.agentName" placeholder="如:需求分析 Agent" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="类型" prop="agentType">
              <el-select v-model="form.agentType" style="width: 100%">
                <el-option label="需求分析" value="req_analyzer" />
                <el-option label="代码审查" value="code_reviewer" />
                <el-option label="测试生成" value="test_gen" />
                <el-option label="发布评审" value="release_reviewer" />
                <el-option label="运维巡检" value="ops_inspector" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模型" prop="modelProvider">
              <el-select v-model="form.modelProvider" style="width: 100%">
                <el-option label="DeepSeek-V3" value="deepseek" />
                <el-option label="Claude 4.x" value="claude" />
                <el-option label="ChatGLM" value="chatglm" />
                <el-option label="GPT-4" value="gpt4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="System Prompt" prop="systemPrompt">
          <el-input v-model="form.systemPrompt" type="textarea" :rows="4" placeholder="System Prompt 模板,定义 Agent 行为" />
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
import { listAiAgent, addAiAgent, updateAiAgent, delAiAgent, getAiAgent, invokeAiAgent, type AiAgent } from '@/api/business/ai-agent'

const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)

const emptyForm = (): AiAgent => ({ agentName: '', agentType: 'req_analyzer', modelProvider: 'deepseek', description: '', systemPrompt: '' })
const form = reactive<AiAgent>(emptyForm())
const rules = {
  agentName: [{ required: true, message: '请输入 Agent 名称', trigger: 'blur' }]
}

const list = ref<AiAgent[]>([])
const flowSteps = ['📋 需求分析', '✏️ 代码生成', '🔍 代码审查', '🧪 测试生成', '🚀 发布评审', '🛠️ 运维巡检']

const totalCalls = computed(() => list.value.reduce((s, x) => s + (x.totalCalls || 0), 0))
const runningCount = computed(() => list.value.filter(x => x.status === '01').length)
const avgSuccessRate = computed(() => {
  const r = list.value.map(x => x.successRate || 0).filter(x => x > 0)
  if (!r.length) return 0
  return Math.round(r.reduce((s, n) => s + n, 0) / r.length)
})

function agentIcon(t?: string) {
  return ({ req_analyzer: '📋', code_reviewer: '🔍', test_gen: '🧪', release_reviewer: '🚀', ops_inspector: '🛠️' } as Record<string,string>)[t || ''] || '🤖'
}
function agentTypeLabel(t?: string) {
  return ({ req_analyzer: '需求分析', code_reviewer: '代码审查', test_gen: '测试生成', release_reviewer: '发布评审', ops_inspector: '运维巡检' } as Record<string,string>)[t || ''] || t || '-'
}
function providerLabel(p?: string) {
  return ({ deepseek: 'DeepSeek-V3', claude: 'Claude', chatglm: 'ChatGLM', gpt4: 'GPT-4' } as Record<string,string>)[p || ''] || p || '-'
}
function agentStatusLabel(s?: string) {
  return ({ '00': '草稿', '01': '运行中', '02': '已停用' } as Record<string,string>)[s || ''] || '-'
}
function agentStatusTag(s?: string): any {
  return ({ '00': 'info', '01': 'success', '02': 'warning' } as Record<string,string>)[s || ''] || 'info'
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
    await invokeAiAgent(a.agentId, { input: '手动调用测试' })
    ElMessage.success(`${a.agentName} 调用成功`); await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '调用失败') }
}

async function handleDelete(a: AiAgent) {
  if (!a.agentId) return
  await ElMessageBox.confirm(`删除 Agent "${a.agentName}"?`, '提示', { type: 'warning' })
  await delAiAgent(a.agentId); ElMessage.success('删除成功'); await getList()
}

onMounted(getList)
</script>

<style scoped>
.agent-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; }
.card-title { font-weight: 600; }
.stat-row :deep(.el-card__body) { padding: 16px; text-align: center; }
.stat-label { color: #6b7280; font-size: 12px; margin-bottom: 6px; }
.stat-value { font-size: 28px; font-weight: 700; }
.stat-value.pu { color: #7c3aed; }
.stat-value.success { color: #10b981; }
.stat-value.blue { color: #3b82f6; }
.agent-card { min-height: 200px; }
.agent-head { display: flex; gap: 10px; align-items: center; margin-bottom: 10px; }
.agent-icon { font-size: 28px; }
.agent-meta { flex: 1; }
.agent-name { font-weight: 700; font-size: 14px; color: #111827; }
.agent-type { color: #6b7280; font-size: 12px; margin-top: 2px; }
.agent-desc { color: #4b5563; font-size: 12.5px; line-height: 1.6; margin-bottom: 10px; min-height: 30px; }
.agent-stats { padding: 8px 0; border-top: 1px solid #f3f4f6; }
.stat-mini { font-size: 12px; color: #6b7280; }
.stat-mini strong { color: #2d7a4f; font-size: 13px; }
.agent-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 8px; }
.flow-card { background: linear-gradient(135deg, #1e1b4b, #1e3a5f); margin-top: 14px; }
.flow-card :deep(.el-card__header), .flow-card :deep(.el-card__body) { color: #fff; }
.flow-row { display: flex; gap: 6px; flex-wrap: wrap; align-items: center; }
.flow-step { display: flex; gap: 6px; align-items: center; }
.flow-box { background: rgba(255,255,255,.12); border-radius: 7px; padding: 7px 12px; font-size: 12.5px; }
.flow-arrow { color: rgba(255,255,255,.5); font-size: 16px; }
</style>
