<!--
  测试方案 — PRD §F4.1 + 原型 testplan.html
  5 种测试类型 checkbox + AI 生成 + 4 态状态机
-->
<template>
  <div class="app-container testplan-page">
    <!-- 顶栏 (对齐原型 .ph) -->
    <div class="page-header">
      <div>
        <h2 class="page-title">📐 测试方案与计划</h2>
        <p class="page-subtitle">AI 生成测试策略、范围、资源分配计划</p>
      </div>
      <el-button type="primary" @click="newPlan">
        <el-icon><Plus /></el-icon>&nbsp;新建测试方案
      </el-button>
    </div>

    <el-row :gutter="20">
      <!-- 左卡:测试方案配置 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">
                {{ current.testplanId ? `📐 方案 ${current.testplanNo}` : '➕ 新建测试方案' }}
              </span>
              <el-tag :type="statusTag.type" size="small">{{ statusTag.label }}</el-tag>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
            <el-form-item label="关联项目" prop="projectId" required>
              <el-select v-model="form.projectId" placeholder="选择项目/迭代" filterable style="width: 100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>

            <el-form-item label="方案标题" prop="title" required>
              <el-input v-model="form.title" placeholder="如:Sprint 26W21 测试方案" maxlength="200" />
            </el-form-item>

            <el-form-item label="测试类型" prop="testTypesArr">
              <el-checkbox-group v-model="testTypesArr">
                <el-checkbox value="functional">功能测试</el-checkbox>
                <el-checkbox value="api">接口测试</el-checkbox>
                <el-checkbox value="performance">性能测试</el-checkbox>
                <el-checkbox value="automation">自动化测试</el-checkbox>
                <el-checkbox value="security">安全测试</el-checkbox>
              </el-checkbox-group>
              <div class="hint-text">已选 {{ testTypesArr.length }} 种,默认建议至少含「功能 + 接口 + 自动化」</div>
            </el-form-item>

            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="测试周期" prop="testCycleDays">
                  <el-input-number v-model="form.testCycleDays" :min="1" :max="60" style="width: 100%" controls-position="right" />
                  <span class="hint-suffix">天</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="作者" prop="authorUserId">
                  <el-input-number v-model="form.authorUserId" :min="1" style="width: 100%" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="测试范围" prop="scope">
              <el-input v-model="form.scope" type="textarea" :rows="2" placeholder="需求 REQ-001~005,接口 /api/v1/irrigation/*" />
            </el-form-item>

            <el-form-item label="测试策略" prop="strategy">
              <el-input v-model="form.strategy" type="textarea" :rows="3" placeholder="功能优先,自动化覆盖核心路径,性能压测目标 QPS=500……" />
            </el-form-item>

            <el-form-item label="推荐工具" prop="toolsRecommended">
              <el-input v-model="form.toolsRecommended" placeholder="CSV: playwright,jmeter,postman,selenium" />
            </el-form-item>

            <el-form-item label="资源分配" prop="resourcesPlan">
              <el-input v-model="form.resourcesPlan" type="textarea" :rows="2" placeholder="人员:测试×2 (5d) + AI×1 (3d) / 环境:staging cluster" />
            </el-form-item>

            <el-form-item label="风险评估" prop="riskAssessment">
              <el-input v-model="form.riskAssessment" type="textarea" :rows="2" placeholder="风险点 + 应对策略" />
            </el-form-item>

            <el-form-item>
              <div class="btn-row">
                <el-button type="primary" :loading="saving" @click="handleSubmit(false)">
                  <el-icon><DocumentChecked /></el-icon>&nbsp;{{ current.testplanId ? '更新' : '保存草稿' }}
                </el-button>
                <AiButton :loading="aiLoading" :saving="saving" @click="handleSubmit(true)">
                  保存并 AI 分析
                </AiButton>
                <template v-if="current.testplanId">
                  <el-button v-if="canTransition('01')" type="warning" @click="transition('01')">
                    📝 确认 (→01)
                  </el-button>
                  <el-button v-if="canTransition('02')" type="primary" @click="transition('02')">
                    ▶ 开始执行 (→02)
                  </el-button>
                  <el-button v-if="canTransition('03')" type="success" @click="transition('03')">
                    ✅ 完成 (→03)
                  </el-button>
                  <el-button v-if="canTransition('00') && current.status === '01'" type="info" @click="transition('00')">
                    ↩ 打回草稿 (01→00)
                  </el-button>
                </template>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右卡:AI 生成测试方案预览 -->
      <el-col :span="12">
        <el-card shadow="never" class="plan-preview-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">📋 测试方案预览</span>
              <el-tag v-if="current.aiGenerated === 'Y'" type="success" size="small">AI 已生成</el-tag>
              <el-tag v-else type="info" size="small">未生成</el-tag>
            </div>
          </template>

          <div v-if="!current.testplanId" class="empty-state">
            <el-icon :size="48" color="#9ca3af"><Document /></el-icon>
            <p>保存草稿后,点击「保存并 AI 分析」生成完整测试方案</p>
          </div>

          <div v-else-if="aiLoading" class="loading-state">
            <el-icon class="rotate" :size="36" color="#3b82f6"><Loading /></el-icon>
            <p>AI 正在分析测试范围,生成方案结构……</p>
          </div>

          <div v-else-if="current.strategy || current.scope" class="plan-content">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="编号">
                <code>{{ current.testplanNo }}</code>
              </el-descriptions-item>
              <el-descriptions-item label="测试周期">
                <el-tag size="small">{{ current.testCycleDays || 0 }} 天</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="测试类型">
                <el-tag v-for="t in testTypesDisplay" :key="t.value" size="small" :type="t.type" style="margin-right: 4px">
                  {{ t.label }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="推荐工具" v-if="current.toolsRecommended">
                <el-tag v-for="t in (current.toolsRecommended || '').split(',').filter(Boolean)" :key="t" size="small" effect="plain" style="margin-right: 4px">
                  {{ t.trim() }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <el-divider content-position="left">📐 测试策略</el-divider>
            <div class="strategy-block">{{ current.strategy || '—' }}</div>

            <el-divider content-position="left">📦 测试范围</el-divider>
            <div class="strategy-block">{{ current.scope || '—' }}</div>

            <el-divider content-position="left">👥 资源分配</el-divider>
            <div class="strategy-block">{{ current.resourcesPlan || '—' }}</div>

            <el-divider content-position="left">⚠️ 风险评估</el-divider>
            <el-alert
              v-if="current.riskAssessment"
              :title="current.riskAssessment"
              type="warning"
              :closable="false"
              show-icon
            />
            <div v-else class="strategy-block">—</div>
          </div>

          <div v-else class="plan-not-yet">
            <el-icon :size="40" color="#f59e0b"><InfoFilled /></el-icon>
            <p>草稿已保存,点击下方触发 AI 生成完整方案</p>
            <el-button type="primary" :loading="aiLoading" @click="triggerAi">
              <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成测试方案
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部:历史方案列表 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">📚 历史方案 ({{ total }})</span>
          <el-input v-model="queryParams.title" placeholder="搜索标题" style="width: 240px" clearable @clear="getList" @keyup.enter="getList">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </template>

      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="testplanNo" width="160" />
        <el-table-column label="方案标题" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="周期" prop="testCycleDays" width="80" align="center">
          <template #default="{ row }">{{ row.testCycleDays || '-' }} 天</template>
        </el-table-column>
        <el-table-column label="测试类型" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="t in (row.testTypes || '').split(',').filter(Boolean)" :key="t" size="small" style="margin-right: 4px">
              {{ testTypeLabel(t.trim()) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="AI" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.aiGenerated === 'Y'" type="success" size="small">Y</el-tag>
            <el-tag v-else type="info" size="small">N</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadPlan(row)">载入</el-button>
            <AiButton link :disabled="row.aiGenerated === 'Y'" @click="quickAi(row)">AI</AiButton>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, DocumentChecked, MagicStick, Search, Document, Loading, InfoFilled
} from '@element-plus/icons-vue'
import {
  listTestPlan, getTestPlan, addTestPlan, updateTestPlan, delTestPlan, aiGenerateTestPlan,
  listProjectsForSelect, type TestPlan, type TestPlanQuery
} from '@/api/business/testplan'
import { statusTagFor, testTypeLabel, testTypeInfo } from './testplanDict'

const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)
const projects = ref<any[]>([])
const list = ref<TestPlan[]>([])
const total = ref(0)

const emptyForm = (): TestPlan => ({
  title: '',
  testTypes: 'functional,api,automation',
  testCycleDays: 10,
  authorUserId: 1
})

const form = reactive<TestPlan>(emptyForm())
const current = reactive<TestPlan>({ title: '' })
const queryParams = reactive<TestPlanQuery>({ pageNum: 1, pageSize: 10, title: '' })

// === testTypes 双向绑定:数组 ↔ CSV ===
const testTypesArr = ref<string[]>(['functional', 'api', 'automation'])
watch(testTypesArr, val => { form.testTypes = val.join(',') })
watch(() => form.testTypes, val => {
  const arr = (val || '').split(',').filter(Boolean)
  if (JSON.stringify(arr) !== JSON.stringify(testTypesArr.value)) {
    testTypesArr.value = arr
  }
})

const rules = {
  title: [{ required: true, message: '请输入方案标题', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }]
}

// === 状态标签 (字典已抽到 testplanDict.ts,这里只剩组件态 computed) ===
const statusTag = computed(() => statusTagFor(current.status))

const TRANSITIONS: Record<string, string[]> = {
  '00': ['01'], '01': ['00', '02'], '02': ['03'], '03': []
}
function canTransition(to: string): boolean {
  const from = current.status || '00'
  return TRANSITIONS[from]?.includes(to) || false
}

// === 测试类型 (字典已抽到 testplanDict.ts) ===
const testTypesDisplay = computed(() =>
  testTypesArr.value.map(t => ({ value: t, ...testTypeInfo(t) }))
)

// === CRUD ===
async function getList() {
  listLoading.value = true
  try {
    const res: any = await listTestPlan(queryParams)
    list.value = res.rows || []
    total.value = res.total || 0
  } finally {
    listLoading.value = false
  }
}

async function loadProjects() {
  try {
    const res: any = await listProjectsForSelect()
    projects.value = res.rows || []
  } catch { /* ignore */ }
}

async function handleSubmit(triggerAiAfter: boolean) {
  await formRef.value.validate()
  saving.value = true
  try {
    if (current.testplanId) {
      await updateTestPlan({ ...form, testplanId: current.testplanId })
      ElMessage.success('更新成功')
    } else {
      const res: any = await addTestPlan(form)
      if (res.code === 200) {
        ElMessage.success('保存成功')
        await getList()
        const latest = list.value.find(x => x.title === form.title)
        if (latest?.testplanId) Object.assign(current, latest)
      }
    }
    if (triggerAiAfter && current.testplanId) {
      await triggerAi()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function triggerAi() {
  if (!current.testplanId) {
    ElMessage.warning('请先保存草稿')
    return
  }
  aiLoading.value = true
  try {
    const res: any = await aiGenerateTestPlan(current.testplanId)
    if (res.code === 200 && res.data) {
      Object.assign(current, res.data)
      Object.assign(form, res.data)
      ElMessage.success('AI 测试方案已生成')
      await getList()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || 'AI 生成失败')
  } finally {
    aiLoading.value = false
  }
}

async function quickAi(row: TestPlan) {
  if (!row.testplanId) return
  aiLoading.value = true
  try {
    const res: any = await aiGenerateTestPlan(row.testplanId)
    if (res.code === 200) {
      ElMessage.success(`${row.testplanNo} AI 完成`)
      await getList()
    }
  } finally {
    aiLoading.value = false
  }
}

async function transition(to: string) {
  if (!current.testplanId) return
  try {
    const res: any = await updateTestPlan({ testplanId: current.testplanId, title: current.title, status: to })
    if (res.code === 200) {
      ElMessage.success(`状态切换至 ${statusTagFor(to).label}`)
      const r = await getTestPlan(current.testplanId)
      if (r.code === 200) Object.assign(current, r.data)
      await getList()
    }
  } catch (e: any) {
    if (e?.code === 601) ElMessage.error('状态转换不合法 (601)')
    else ElMessage.error(e?.msg || '状态转换失败')
  }
}

async function loadPlan(row: TestPlan) {
  if (!row.testplanId) return
  const res: any = await getTestPlan(row.testplanId)
  if (res.code === 200 && res.data) {
    Object.assign(form, res.data)
    Object.assign(current, res.data)
    testTypesArr.value = (res.data.testTypes || '').split(',').filter(Boolean)
    ElMessage.info(`已载入 ${res.data.testplanNo}`)
  }
}

async function handleDelete(row: TestPlan) {
  if (!row.testplanId) return
  await ElMessageBox.confirm(`确认删除方案 "${row.testplanNo}"?`, '提示', { type: 'warning' })
  await delTestPlan(row.testplanId)
  ElMessage.success('删除成功')
  if (current.testplanId === row.testplanId) newPlan()
  await getList()
}

function newPlan() {
  Object.assign(form, emptyForm())
  testTypesArr.value = ['functional', 'api', 'automation']
  Object.keys(current).forEach(k => delete (current as any)[k])
  formRef.value?.clearValidate()
}

onMounted(async () => {
  await loadProjects()
  await getList()
})
</script>

<style scoped>
.testplan-page { padding: 20px; }
.page-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 20px;
}
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.btn-row { display: flex; gap: 10px; flex-wrap: wrap; }

.hint-text { font-size: 11px; color: #6b7280; margin-top: 4px; }
.hint-suffix { color: #9ca3af; font-size: 12px; margin-left: 6px; }

.plan-preview-card { min-height: 580px; }
.empty-state, .loading-state, .plan-not-yet {
  text-align: center; padding: 60px 16px; color: #6b7280;
}
.empty-state p, .loading-state p, .plan-not-yet p { margin: 12px 0; font-size: 13px; }
.rotate { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.plan-content { font-size: 13px; }
.strategy-block {
  white-space: pre-wrap;
  background: #f9fafb; padding: 10px 12px; border-radius: 6px;
  font-size: 12px; line-height: 1.6;
}
code {
  background: #f4f4f5; padding: 2px 6px; border-radius: 3px;
  font-family: 'Consolas', monospace; font-size: 12px;
}
</style>
