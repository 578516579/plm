<!--
  系统概要设计 (HLD) — PRD §F3.1 + 原型 archdesign.html
  严格对齐: 左卡片 6 类技术选型表单 + 右卡片 C4 容器图 + 底部 NFR 映射
-->
<template>
  <div class="app-container arch-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🏗️ 系统概要设计 (HLD)</h2>
        <p class="page-subtitle">AI 根据 PRD 推荐技术架构,生成 C4 模型容器图</p>
      </div>
      <el-button
        type="success"
        :loading="aiLoading"
        :disabled="!current.archId"
        @click="triggerAi"
      >
        <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成架构方案
      </el-button>
    </div>

    <el-row :gutter="20">
      <!-- 左:技术架构选型 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">🏗️ 技术架构选型</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
            <el-form-item label="关联项目" prop="projectId" required>
              <el-select v-model="form.projectId" placeholder="选择项目" style="width: 100%">
                <el-option
                  v-for="p in projectOptions"
                  :key="p.id"
                  :label="p.projectName"
                  :value="p.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="架构方案名称" prop="title" required>
              <el-input v-model="form.title" placeholder="如:智慧灌溉 HLD v1" maxlength="200" />
            </el-form-item>
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="架构模式" prop="archMode">
                  <el-select v-model="form.archMode" style="width: 100%">
                    <el-option label="微服务架构" value="microservice" />
                    <el-option label="单体架构" value="monolith" />
                    <el-option label="Serverless" value="serverless" />
                    <el-option label="分层架构" value="layered" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="主语言/框架" prop="primaryStack">
                  <el-select v-model="form.primaryStack" style="width: 100%">
                    <el-option label="Java (SpringBoot3)" value="java_sb3" />
                    <el-option label="Go (Gin)" value="go_gin" />
                    <el-option label="Python (FastAPI)" value="python_fastapi" />
                    <el-option label="Node.js" value="nodejs" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="数据库" prop="databaseChoice">
                  <el-select v-model="form.databaseChoice" style="width: 100%">
                    <el-option label="PostgreSQL + Redis" value="pg_redis" />
                    <el-option label="MySQL + Redis" value="mysql_redis" />
                    <el-option label="人大金仓 (国产化)" value="kingbase" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="AI 编排" prop="aiOrchestration">
                  <el-select v-model="form.aiOrchestration" style="width: 100%">
                    <el-option label="Dify + DeepSeek-V3" value="dify_deepseek" />
                    <el-option label="Dify + ChatGLM" value="dify_chatglm" />
                    <el-option label="自建 LangChain" value="self_langchain" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="部署方式" prop="deploymentType">
                  <el-select v-model="form.deploymentType" style="width: 100%">
                    <el-option label="Kubernetes" value="k8s" />
                    <el-option label="Docker Compose" value="docker_compose" />
                    <el-option label="裸机部署" value="baremetal" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="IoT 协议" prop="iotProtocol">
                  <el-select v-model="form.iotProtocol" style="width: 100%">
                    <el-option label="MQTT (EMQ X)" value="mqtt" />
                    <el-option label="HTTP Long-polling" value="http_longpoll" />
                    <el-option label="WebSocket" value="websocket" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSubmit(false)">
                <el-icon><DocumentAdd /></el-icon>&nbsp;保存草稿
              </el-button>
              <el-button type="success" :loading="saving || aiLoading" @click="handleSubmit(true)">
                <el-icon><MagicStick /></el-icon>&nbsp;保存并 AI 生成
              </el-button>
              <el-button v-if="current.archId" plain @click="resetForm">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右:C4 容器图 -->
      <el-col :span="12">
        <el-card shadow="never" class="diagram-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">📐 系统架构图 (C4 容器图)</span>
              <el-tag :type="statusTag.type" size="small">{{ statusTag.label }}</el-tag>
            </div>
          </template>

          <div v-if="!current.archId" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><Grid /></el-icon>
            <p>配置左侧选项后,点击「保存并 AI 生成」生成 C4 容器图</p>
          </div>

          <div v-else-if="aiLoading" class="loading-state">
            <el-icon class="rotate" :size="36" color="#3b82f6"><Loading /></el-icon>
            <p>AI 正在生成 C4 容器图,预计 8-15 秒……</p>
          </div>

          <div v-else-if="current.c4DiagramContent" class="ai-content">
            <el-tabs v-model="diagramTab">
              <el-tab-pane label="📐 C4 容器图 (Mermaid)" name="c4">
                <pre class="mermaid-code">{{ current.c4DiagramContent }}</pre>
                <el-alert
                  type="info"
                  :closable="false"
                  show-icon
                  title="将上述 Mermaid 代码贴入 https://mermaid.live 即可可视化预览"
                  style="margin-top: 8px"
                />
              </el-tab-pane>
              <el-tab-pane label="📝 方案描述" name="design">
                <div class="markdown-body" v-html="renderedDesign" />
              </el-tab-pane>
            </el-tabs>
          </div>

          <div v-else class="ai-not-yet">
            <el-icon :size="40" color="#f59e0b"><InfoFilled /></el-icon>
            <p>架构草稿已保存 ({{ current.archNo }}),点击下方按钮触发 AI 生成</p>
            <el-button type="success" :loading="aiLoading" @click="triggerAi">
              <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成 C4 容器图
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- NFR 映射 -->
    <el-card v-if="current.nfrMapping" shadow="never" style="margin-top: 14px">
      <template #header>
        <span class="card-title">⚡ 非功能需求映射 (NFR)</span>
      </template>
      <div class="nfr-text">{{ current.nfrMapping }}</div>
    </el-card>

    <!-- 历史 HLD 列表 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">📚 历史架构方案 ({{ total }})</span>
          <el-input
            v-model="queryParams.title"
            placeholder="搜索"
            style="width: 240px"
            clearable
            @clear="getList"
            @keyup.enter="getList"
          />
        </div>
      </template>
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="archNo" width="160" />
        <el-table-column label="方案名称" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="架构模式" width="100" align="center">
          <template #default="{ row }">{{ enumLabel('arch', row.archMode) }}</template>
        </el-table-column>
        <el-table-column label="技术栈" width="140" align="center">
          <template #default="{ row }">{{ enumLabel('stack', row.primaryStack) }}</template>
        </el-table-column>
        <el-table-column label="AI" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.aiGenerated === 'Y'" type="success" size="small">已生成</el-tag>
            <el-tag v-else type="info" size="small">未生成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadArch(row)">载入</el-button>
            <el-button link type="success" @click="quickAi(row)">AI 生成</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-if="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, DocumentAdd, Grid, Loading, InfoFilled } from '@element-plus/icons-vue'
import {
  listArch, addArch, updateArch, delArch, aiGenerateArch, getArch, listProjectsForSelect,
  type Arch, type ArchQuery
} from '@/api/business/arch'
import { statusTagFor, enumLabel } from './archDict'

const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)
const diagramTab = ref('c4')

const emptyForm = (): Arch => ({
  projectId: 0,
  title: '',
  archMode: 'microservice',
  primaryStack: 'java_sb3',
  databaseChoice: 'pg_redis',
  aiOrchestration: 'dify_deepseek',
  deploymentType: 'k8s',
  iotProtocol: 'mqtt',
  authorUserId: 1
})

const form = reactive<Arch>(emptyForm())
const current = reactive<Arch>({ projectId: 0, title: '' })

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入架构方案名称', trigger: 'blur' }]
}

const list = ref<Arch[]>([])
const total = ref(0)
const queryParams = reactive<ArchQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

const statusTag = computed(() => statusTagFor(current.status))

const renderedDesign = computed(() => {
  const md = current.designContent || ''
  return md
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/^### (.+)$/gm, '<h4>$1</h4>')
    .replace(/^## (.+)$/gm, '<h3>$1</h3>')
    .replace(/^# (.+)$/gm, '<h2>$1</h2>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>\n?)+/g, m => '<ul>' + m + '</ul>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/^([^<\n].+)$/gm, '<p>$1</p>')
})

async function getList() {
  listLoading.value = true
  try {
    const res: any = await listArch(queryParams)
    list.value = res.rows || []
    total.value = res.total || 0
  } finally {
    listLoading.value = false
  }
}

async function loadProjects() {
  try {
    const res: any = await listProjectsForSelect()
    projectOptions.value = res.rows || []
  } catch (e) { /* */ }
}

async function handleSubmit(triggerAiAfter: boolean) {
  await formRef.value.validate()
  saving.value = true
  try {
    if (current.archId) {
      await updateArch({ ...form, archId: current.archId })
      ElMessage.success('更新成功')
    } else {
      await addArch(form)
      ElMessage.success('保存成功')
      await getList()
      const latest = list.value.find(x => x.title === form.title)
      if (latest?.archId) Object.assign(current, latest)
    }
    if (triggerAiAfter && current.archId) await triggerAi()
  } catch (e: any) {
    ElMessage.error(e?.msg || '保存失败')
  } finally {
    saving.value = false
  }
}

async function triggerAi() {
  if (!current.archId) { ElMessage.warning('请先保存草稿'); return }
  aiLoading.value = true
  try {
    const res: any = await aiGenerateArch(current.archId)
    if (res.code === 200 && res.data) {
      Object.assign(current, res.data)
      ElMessage.success('AI 架构方案 + C4 容器图已生成')
      await getList()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || 'AI 生成失败')
  } finally {
    aiLoading.value = false
  }
}

async function quickAi(row: Arch) {
  if (!row.archId) return
  aiLoading.value = true
  try {
    await aiGenerateArch(row.archId)
    ElMessage.success(`${row.archNo} AI 生成完成`)
    await getList()
  } finally {
    aiLoading.value = false
  }
}

async function loadArch(row: Arch) {
  if (!row.archId) return
  const res: any = await getArch(row.archId)
  if (res.code === 200 && res.data) {
    Object.assign(form, res.data)
    Object.assign(current, res.data)
    ElMessage.info(`已载入 ${res.data.archNo}`)
  }
}

async function handleDelete(row: Arch) {
  if (!row.archId) return
  await ElMessageBox.confirm(`确认删除 "${row.archNo}"?`, '提示', { type: 'warning' })
  await delArch(row.archId)
  ElMessage.success('删除成功')
  if (current.archId === row.archId) resetForm()
  await getList()
}

function resetForm() {
  Object.assign(form, emptyForm())
  Object.keys(current).forEach(k => delete (current as any)[k])
  formRef.value?.clearValidate()
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.arch-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.empty-state, .loading-state, .ai-not-yet { text-align: center; padding: 50px 20px; color: #6b7280; }
.empty-state p, .loading-state p, .ai-not-yet p { margin: 12px 0; }
.rotate { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.diagram-card { min-height: 540px; }
.mermaid-code {
  background: #1e1e2e; color: #cdd6f4; padding: 14px 16px; border-radius: 8px;
  font-family: 'Consolas', monospace; font-size: 12px; line-height: 1.6;
  overflow-x: auto; white-space: pre-wrap;
}
.nfr-text {
  white-space: pre-wrap; font-size: 13px; line-height: 1.8;
  background: #f9fafb; padding: 12px 14px; border-radius: 6px; border-left: 3px solid #4caf78;
}
.markdown-body { line-height: 1.7; font-size: 13px; }
:deep(.markdown-body h2) { font-size: 16px; margin: 12px 0 8px; }
:deep(.markdown-body h3) { font-size: 14px; margin: 10px 0 6px; }
:deep(.markdown-body ul) { margin: 6px 0; padding-left: 22px; }
</style>
