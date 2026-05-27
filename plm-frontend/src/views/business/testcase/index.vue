<!--
  测试用例管理 — PRD §F4.2 + 原型 testcase.html
  严格对齐: 4 统计卡 + AI 生成配置 (4 测试类型 checkbox) + 用例列表
-->
<template>
  <div class="app-container tc-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🧪 测试用例管理</h2>
        <p class="page-subtitle">AI 基于需求 + 边界分析自动生成用例,含农业专项场景</p>
      </div>
      <div class="header-actions">
        <el-button type="success" @click="toggleAiPanel">
          <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 生成测试用例
        </el-button>
        <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon>&nbsp;+ 手动添加用例</el-button>
      </div>
    </div>

    <!-- 4 统计卡 -->
    <el-row :gutter="14" class="stat-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">用例总数</div>
          <div class="stat-value g">{{ total }}</div>
          <div class="stat-detail up">AI 生成 {{ aiGenCount }} 条</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">已通过</div>
          <div class="stat-value success">{{ passedCount }}</div>
          <div class="stat-detail">通过率 {{ passRate }}%</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">未执行</div>
          <div class="stat-value am">{{ pendingCount }}</div>
          <div class="stat-detail">待安排</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">失败用例</div>
          <div class="stat-value red">{{ failedCount }}</div>
          <div class="stat-detail dn">需关注</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- AI 生成配置面板 (对齐原型 tcGenPanel) -->
    <el-card v-if="aiPanelVisible" shadow="never" style="margin-top: 14px">
      <template #header><span class="card-title">🤖 AI 生成配置</span></template>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form label-width="100px">
            <el-form-item label="关联项目">
              <el-select v-model="aiForm.projectId" placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="输入来源">
              <el-select v-model="aiForm.requirementId" clearable placeholder="选择需求 (可选)" style="width: 100%">
                <el-option v-for="r in requirementOptions" :key="r.requirementId" :label="r.title" :value="r.requirementId" />
              </el-select>
            </el-form-item>
            <el-form-item label="测试类型">
              <el-checkbox v-model="catFunctional">功能测试</el-checkbox>
              <el-checkbox v-model="catBoundary">边界值</el-checkbox>
              <el-checkbox v-model="catException">异常场景</el-checkbox>
              <el-checkbox v-model="catAgri">农业专项</el-checkbox>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <div style="display: flex; flex-direction: column; height: 100%; justify-content: center; align-items: center">
            <el-button type="success" size="large" :loading="aiLoading" @click="doAiGenerate" style="width: 240px">
              <el-icon><MagicStick /></el-icon>&nbsp;✨ 开始生成
            </el-button>
            <el-alert type="info" :closable="false" show-icon
              title="AI 将基于需求 + 字段语义 + AgriKB 自动生成 5-15 条用例"
              style="margin-top: 14px; width: 100%" />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" style="margin-top: 14px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">🧪 用例列表</span>
          <div style="display: flex; gap: 8px">
            <el-select v-model="queryParams.category" placeholder="测试类型" clearable style="width: 130px" @change="getList">
              <el-option label="功能测试" value="functional" />
              <el-option label="边界值" value="boundary" />
              <el-option label="异常场景" value="exception" />
              <el-option label="农业专项" value="agri" />
              <el-option label="性能测试" value="performance" />
            </el-select>
            <el-input v-model="queryParams.title" placeholder="搜索标题" style="width: 200px" clearable @clear="getList" @keyup.enter="getList" />
          </div>
        </div>
      </template>
      <el-table v-loading="listLoading" :data="list" stripe @selection-change="selection = $event">
        <el-table-column type="selection" width="50" />
        <el-table-column label="编号" prop="testcaseNo" width="140" />
        <el-table-column label="用例标题" prop="title" min-width="220" show-overflow-tooltip />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="categoryTag(row.category)" size="small">{{ categoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="priorityTag(row.priority)" size="small">{{ row.priority || 'P2' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="自动化" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isAutomated === 'Y'" type="success" size="small">已自动化</el-tag>
            <el-tag v-else type="info" size="small">手动</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadTc(row)">编辑</el-button>
            <el-button link type="warning" :loading="aiRowId === row.testcaseId" @click="aiEnrich(row)">
              <el-icon><MagicStick /></el-icon>&nbsp;AI补全
            </el-button>
            <el-button link type="success" @click="execTc(row, '03')">▶ 通过</el-button>
            <el-button link type="danger" @click="execTc(row, '04')">✗ 失败</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="selection.length" class="batch-bar">
        已选 <strong>{{ selection.length }}</strong> 条
        <el-button size="small" type="success" @click="batchExec('03')">批量通过</el-button>
        <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
      </div>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 新建/编辑 Dialog (对齐原型 modal-testcase-add) -->
    <el-dialog v-model="dialogVisible" :title="form.testcaseId ? '编辑用例' : '+ 手动添加用例'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联项目" prop="projectId" required>
          <el-select v-model="form.projectId" style="width: 100%">
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="用例标题" prop="title" required>
          <el-input v-model="form.title" placeholder="用例描述" maxlength="200" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="用例类型" prop="category">
              <el-select v-model="form.category" style="width: 100%">
                <el-option label="功能测试" value="functional" />
                <el-option label="边界测试" value="boundary" />
                <el-option label="异常场景" value="exception" />
                <el-option label="农业专项" value="agri" />
                <el-option label="性能测试" value="performance" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" style="width: 100%">
                <el-option label="P0" value="P0" />
                <el-option label="P1" value="P1" />
                <el-option label="P2" value="P2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="前置条件" prop="preconditions">
          <el-input v-model="form.preconditions" placeholder="执行本用例的前提条件" />
        </el-form-item>
        <el-form-item label="测试步骤" prop="steps">
          <el-input v-model="form.steps" type="textarea" :rows="3" placeholder="1. 操作步骤一&#10;2. 操作步骤二" />
        </el-form-item>
        <el-form-item label="预期结果" prop="expectedResult">
          <el-input v-model="form.expectedResult" type="textarea" :rows="2" placeholder="期望的系统响应/输出" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ form.testcaseId ? '保存' : '✅ 添加用例' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Plus } from '@element-plus/icons-vue'
import {
  listTestCase, addTestCase, updateTestCase, delTestCase, executeTestCase,
  getTestCase, aiGenerateTestCases, aiGenerateTestCaseElements,
  listProjectsForSelect, listRequirementsForSelect,
  type TestCase, type TestCaseQuery
} from '@/api/business/testcase'

const dialogVisible = ref(false)
const aiPanelVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const aiRowId = ref<number | null>(null)  // 行内 AI 补全 loading 锁定到具体行
const listLoading = ref(false)
const selection = ref<TestCase[]>([])

// AI 表单
const aiForm = reactive({ projectId: 0, requirementId: null as number | null })
const catFunctional = ref(true)
const catBoundary = ref(true)
const catException = ref(true)
const catAgri = ref(true)

const emptyForm = (): TestCase => ({ projectId: 0, title: '', category: 'functional', priority: 'P1', status: '00' })
const form = reactive<TestCase>(emptyForm())
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入用例标题', trigger: 'blur' }]
}

const list = ref<TestCase[]>([])
const total = ref(0)
const queryParams = reactive<TestCaseQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])
const requirementOptions = ref<Array<{ requirementId: number; title: string }>>([])

const aiGenCount = computed(() => list.value.filter(t => t.testcaseNo?.includes('AI')).length)
const passedCount = computed(() => list.value.filter(t => t.status === '03').length)
const failedCount = computed(() => list.value.filter(t => t.status === '04').length)
const pendingCount = computed(() => list.value.filter(t => t.status === '00').length)
const passRate = computed(() => {
  const exec = passedCount.value + failedCount.value
  if (exec === 0) return '—'
  return Math.round((passedCount.value / exec) * 100)
})

const statusMap: Record<string, { label: string; type: any }> = {
  '00': { label: '待执行', type: 'info' },
  '01': { label: '准备中', type: 'warning' },
  '02': { label: '执行中', type: 'primary' },
  '03': { label: '通过', type: 'success' },
  '04': { label: '失败', type: 'danger' }
}
const statusTagFor = (s?: string) => statusMap[s || ''] || { label: s || '-', type: 'info' as any }

const categoryLabel = (v?: string) =>
  ({ functional: '功能', boundary: '边界', exception: '异常', agri: '农业专项', performance: '性能' } as Record<string,string>)[v || ''] || v || '-'
const categoryTag = (v?: string): any =>
  ({ functional: 'primary', boundary: 'warning', exception: 'danger', agri: 'success', performance: 'info' } as Record<string,string>)[v || ''] || 'info'
const priorityTag = (p?: string): any =>
  ({ P0: 'danger', P1: 'warning', P2: 'info' } as Record<string,string>)[p || ''] || 'info'

async function getList() {
  listLoading.value = true
  try { const res: any = await listTestCase(queryParams); list.value = res.rows || []; total.value = res.total || 0 } finally { listLoading.value = false }
}
async function loadOpts() {
  try {
    const [pr, rr] = await Promise.all([listProjectsForSelect(), listRequirementsForSelect()])
    projectOptions.value = (pr as any).rows || []
    requirementOptions.value = (rr as any).rows || []
  } catch {}
}

function toggleAiPanel() {
  aiPanelVisible.value = !aiPanelVisible.value
  if (aiPanelVisible.value && !aiForm.projectId && projectOptions.value.length) {
    aiForm.projectId = projectOptions.value[0].id
  }
}

function openAdd() { Object.assign(form, emptyForm()); dialogVisible.value = true }

async function loadTc(row: TestCase) {
  if (!row.testcaseId) return
  const res: any = await getTestCase(row.testcaseId)
  if (res.code === 200 && res.data) { Object.assign(form, res.data); dialogVisible.value = true }
}

// 行内 AI 补全用例要素 — 调 /business/testcase/ai/generate/{id},回填后打开编辑框供确认
async function aiEnrich(row: TestCase) {
  if (!row.testcaseId) return
  aiRowId.value = row.testcaseId
  try {
    const res: any = await aiGenerateTestCaseElements(row.testcaseId)
    if (res.code === 200 && res.data) {
      Object.assign(form, res.data)
      dialogVisible.value = true
      ElMessage.success('AI 已补全用例要素,请确认后保存')
      await getList()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || 'AI 补全失败')
  } finally {
    aiRowId.value = null
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.testcaseId) { await updateTestCase(form); ElMessage.success('更新成功') }
    else { await addTestCase(form); ElMessage.success('添加成功') }
    dialogVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') } finally { saving.value = false }
}

async function doAiGenerate() {
  if (!aiForm.projectId) { ElMessage.warning('请选择项目'); return }
  const cats = [
    catFunctional.value && 'functional', catBoundary.value && 'boundary',
    catException.value && 'exception', catAgri.value && 'agri'
  ].filter(Boolean) as string[]
  if (!cats.length) { ElMessage.warning('至少选一种测试类型'); return }
  aiLoading.value = true
  try {
    const res: any = await aiGenerateTestCases({ projectId: aiForm.projectId, requirementId: aiForm.requirementId || undefined, categories: cats })
    const generated = res?.data?.length || res?.rows?.length || 0
    ElMessage.success(`AI 已生成 ${generated} 条测试用例`)
    aiPanelVisible.value = false
    await getList()
  } catch (e: any) { ElMessage.error(e?.msg || 'AI 失败') } finally { aiLoading.value = false }
}

async function execTc(row: TestCase, status: string) {
  if (!row.testcaseId) return
  try {
    await executeTestCase(row.testcaseId, { status })
    ElMessage.success(`已记录 ${status === '03' ? '通过' : '失败'}`)
    await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '执行失败') }
}

async function batchExec(status: string) {
  await Promise.all(selection.value.filter(t => t.testcaseId).map(t => executeTestCase(t.testcaseId!, { status })))
  ElMessage.success(`批量标记 ${selection.value.length} 条为${status === '03' ? '通过' : '失败'}`)
  selection.value = []; await getList()
}

async function batchDelete() {
  if (!selection.value.length) return
  await ElMessageBox.confirm(`确认删除 ${selection.value.length} 条用例?`, '提示', { type: 'warning' })
  const ids = selection.value.map(t => t.testcaseId).filter(Boolean) as number[]
  await delTestCase(ids); ElMessage.success('批量删除完成')
  selection.value = []; await getList()
}

onMounted(() => { getList(); loadOpts() })
</script>

<style scoped>
.tc-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.header-actions { display: flex; gap: 10px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.stat-row :deep(.el-card__body) { padding: 14px 16px; text-align: left; }
.stat-label { color: #6b7280; font-size: 12px; margin-bottom: 5px; }
.stat-value { font-size: 26px; font-weight: 700; }
.stat-value.g { color: #2d7a4f; }
.stat-value.success { color: #10b981; }
.stat-value.am { color: #f59e0b; }
.stat-value.red { color: #ef4444; }
.stat-detail { font-size: 12px; color: #6b7280; margin-top: 3px; }
.stat-detail.up { color: #10b981; }
.stat-detail.dn { color: #ef4444; }
.batch-bar {
  background: #2d7a4f; color: #fff; padding: 10px 14px; border-radius: 8px;
  display: flex; gap: 10px; align-items: center; margin-top: 14px;
}
</style>
