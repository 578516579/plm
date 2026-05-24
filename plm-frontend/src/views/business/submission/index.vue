<!--
  提测管理 — PRD §F4.4 + 原型 submit.html
  AI 质量门禁 4 项 + 5 态状态机 (含反向边 04→00) + 错误码 708
-->
<template>
  <div class="app-container submission-page">
    <!-- 顶栏 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">📤 提测管理</h2>
        <p class="page-subtitle">AI 质量门禁检查,标准化提测流程</p>
      </div>
      <el-button type="primary" @click="newSubmission">
        <el-icon><Plus /></el-icon>&nbsp;新建提测单
      </el-button>
    </div>

    <!-- 🔒 AI 质量门禁标准 (静态说明,对齐原型 4 块卡片) -->
    <el-card shadow="never" class="gate-standard">
      <template #header>
        <span class="card-title">🔒 AI 质量门禁标准 (PRD §F4.4)</span>
      </template>
      <el-row :gutter="12">
        <el-col :span="6" v-for="(g, i) in gateStandards" :key="i">
          <div class="gate-card" :class="g.cls">
            <div class="gate-icon">{{ g.icon }}</div>
            <div class="gate-text">{{ g.text }}</div>
            <div class="gate-hint">{{ g.hint }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" class="mt-md">
      <!-- 左:表单 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">
                {{ current.submissionId ? `📝 提测单 ${current.submissionNo}` : '➕ 新建提测单' }}
              </span>
              <el-tag :type="statusTag.type" size="small">{{ statusTag.label }}</el-tag>
            </div>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-form-item label="关联项目" prop="projectId" required>
              <el-select v-model="form.projectId" placeholder="选择项目" filterable style="width: 100%">
                <el-option
                  v-for="p in projects" :key="p.id"
                  :label="p.projectName" :value="p.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="提测标题" prop="title" required>
              <el-input
                v-model="form.title"
                placeholder="如:智慧灌溉 v2.1 Sprint 3 提测"
                maxlength="200"
              />
            </el-form-item>

            <el-form-item label="提测范围" prop="scope">
              <el-input
                v-model="form.scope"
                type="textarea"
                :rows="3"
                placeholder="说明本次提测功能范围、变更点……"
              />
            </el-form-item>

            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="测试环境" prop="environment">
                  <el-select v-model="form.environment" placeholder="选择环境" style="width: 100%">
                    <el-option label="开发 (dev)" value="dev" />
                    <el-option label="测试 (test)" value="test" />
                    <el-option label="预发 (staging)" value="staging" />
                    <el-option label="生产 (prod)" value="prod" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="期望周期(天)" prop="expectedTestDays">
                  <el-input-number v-model="form.expectedTestDays" :min="1" :max="60" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="风险说明" prop="riskNotes">
              <el-input
                v-model="form.riskNotes"
                type="textarea"
                :rows="2"
                placeholder="依赖、回归范围、提前通知项等"
              />
            </el-form-item>

            <el-divider content-position="left">🔒 质量门禁数据</el-divider>

            <el-form-item label="单测覆盖率" prop="unitTestCoverage">
              <div style="display: flex; align-items: center; gap: 12px; width: 100%">
                <el-input-number
                  v-model="form.unitTestCoverage"
                  :min="0" :max="100" :precision="2"
                  controls-position="right" style="width: 180px"
                />
                <span style="color: #6b7280; font-size: 12px">% (PRD 要求 ≥ 60%)</span>
                <el-tag v-if="form.unitTestCoverage != null"
                  :type="(form.unitTestCoverage as number) >= 60 ? 'success' : 'danger'"
                  size="small">
                  {{ (form.unitTestCoverage as number) >= 60 ? '✓ 达标' : '✗ 不达标' }}
                </el-tag>
              </div>
            </el-form-item>

            <el-row :gutter="10">
              <el-col :span="8">
                <el-form-item label="代码扫描" prop="codeScanPassed">
                  <el-switch
                    v-model="form.codeScanPassed"
                    active-value="Y" inactive-value="N"
                    active-text="0 高危" inactive-text="未通过"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="PRD 完整" prop="prdCompleted">
                  <el-switch
                    v-model="form.prdCompleted"
                    active-value="Y" inactive-value="N"
                    active-text="完整" inactive-text="缺失"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="API 文档" prop="apiDocUpdated">
                  <el-switch
                    v-model="form.apiDocUpdated"
                    active-value="Y" inactive-value="N"
                    active-text="已更新" inactive-text="未更新"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="退回原因" v-if="form.status === '04' || pendingReject">
              <el-input
                v-model="form.rejectReason"
                type="textarea" :rows="2"
                placeholder="status=04 时必填,否则后端返回 602"
              />
            </el-form-item>

            <el-form-item>
              <div class="btn-row">
                <el-button type="primary" :loading="saving" @click="handleSubmit">
                  <el-icon><DocumentChecked /></el-icon>&nbsp;{{ current.submissionId ? '更新' : '保存草稿' }}
                </el-button>
                <template v-if="current.submissionId">
                  <el-button v-if="canTransition('01')" type="success" @click="transition('01')">
                    <el-icon><Promotion /></el-icon>&nbsp;提交 (00→01)
                  </el-button>
                  <el-button v-if="canTransition('02')" type="warning" @click="transition('02')">
                    🔒 进入门禁 (01→02)
                  </el-button>
                  <el-button v-if="canTransition('03')" type="success" @click="transition('03')">
                    ✅ 通过 (02→03)
                  </el-button>
                  <el-button v-if="canRejectFrom()" type="danger" @click="openReject">
                    ↩ 退回
                  </el-button>
                  <el-button v-if="canTransition('00') && current.status === '04'" type="info" @click="transition('00')">
                    📝 重写草稿 (04→00 反向)
                  </el-button>
                </template>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右:门禁状态可视化 -->
      <el-col :span="10">
        <el-card shadow="never" class="gate-status-card">
          <template #header>
            <div class="card-header-flex">
              <span class="card-title">🛡️ 门禁状态</span>
              <el-tag :type="gateOverall.type" size="default">{{ gateOverall.label }}</el-tag>
            </div>
          </template>

          <div class="gate-items">
            <div v-for="item in gateItems" :key="item.key" class="gate-item">
              <div class="gate-bullet" :class="item.passed ? 'pass' : 'fail'">
                {{ item.passed ? '✓' : '✗' }}
              </div>
              <div class="gate-info">
                <div class="gate-name">{{ item.name }}</div>
                <div class="gate-detail">{{ item.detail }}</div>
              </div>
            </div>
          </div>

          <el-divider />

          <div class="overall-status">
            <div v-if="gateOverall.label.includes('通过')" style="color: #10b981">
              <el-icon :size="48"><CircleCheck /></el-icon>
              <p>所有 4 项门禁达标,可进入「02→03 通过」</p>
            </div>
            <div v-else style="color: #ef4444">
              <el-icon :size="48"><WarningFilled /></el-icon>
              <p>{{ gateOverall.label }},尝试进入 03 会返回 ServiceException(708)</p>
            </div>
          </div>

          <el-divider />

          <h4 class="time-title">📅 关键时间线</h4>
          <el-timeline>
            <el-timeline-item
              type="info"
              :timestamp="current.submittedAt || '—'"
              placement="top"
              :hide-timestamp="!current.submittedAt"
            >
              <strong>提交时间</strong>
              <p>00→01 时自动填写</p>
            </el-timeline-item>
            <el-timeline-item
              type="success"
              :timestamp="current.approvedAt || '—'"
              placement="top"
              :hide-timestamp="!current.approvedAt"
            >
              <strong>批准时间</strong>
              <p>02→03 时自动填写 (需所有门禁通过)</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部:提测单列表 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">📋 提测单列表 ({{ total }})</span>
          <el-input
            v-model="queryParams.title"
            placeholder="搜索标题"
            style="width: 240px"
            clearable
            @clear="getList" @keyup.enter="getList"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </template>
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="submissionNo" width="160" />
        <el-table-column label="提测标题" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="环境" prop="environment" width="80" align="center" />
        <el-table-column label="单测%" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.unitTestCoverage != null"
              :style="{ color: row.unitTestCoverage >= 60 ? '#10b981' : '#ef4444' }">
              {{ Number(row.unitTestCoverage).toFixed(1) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="门禁" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.qualityGatePassed === 'Y'" type="success" size="small">通过</el-tag>
            <el-tag v-else type="info" size="small">未达</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadSubmission(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 退回原因 Dialog -->
    <el-dialog v-model="rejectDialog" title="↩ 退回提测单" width="500">
      <p style="color: #6b7280; margin-bottom: 12px">
        退回需必填原因 (后端硬规则,缺失返回 ServiceException(602))
      </p>
      <el-input v-model="rejectReasonInput" type="textarea" :rows="4" placeholder="说明退回原因…" />
      <template #footer>
        <el-button @click="rejectDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认退回 (→04)</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, DocumentChecked, Promotion, Search, CircleCheck, WarningFilled
} from '@element-plus/icons-vue'
import {
  listSubmission, getSubmission, addSubmission, updateSubmission, delSubmission,
  listProjectsForSelect, type Submission, type SubmissionQuery
} from '@/api/business/submission'

const formRef = ref()
const saving = ref(false)
const listLoading = ref(false)
const projects = ref<any[]>([])
const list = ref<Submission[]>([])
const total = ref(0)

const emptyForm = (): Submission => ({
  title: '', environment: 'staging', expectedTestDays: 7,
  unitTestCoverage: undefined, codeScanPassed: 'N', prdCompleted: 'N', apiDocUpdated: 'N',
  submitterUserId: 1
})
const form = reactive<Submission>(emptyForm())
const current = reactive<Submission>({ title: '' })
const queryParams = reactive<SubmissionQuery>({ pageNum: 1, pageSize: 10, title: '' })

const rejectDialog = ref(false)
const rejectReasonInput = ref('')
const pendingReject = ref(false)

const rules = {
  title: [{ required: true, message: '请输入提测标题', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择关联项目', trigger: 'change' }]
}

// === 状态机 ===
const statusMap: Record<string, { label: string; type: any }> = {
  '00': { label: '草稿',     type: 'info' },
  '01': { label: '已提交',   type: 'warning' },
  '02': { label: '质量门禁中', type: 'primary' },
  '03': { label: '已通过',   type: 'success' },
  '04': { label: '已退回',   type: 'danger' }
}
function statusTagFor(s?: string) { return statusMap[s || '00'] || { label: s, type: 'info' } }
const statusTag = computed(() => statusTagFor(current.status))

// 5 态合法转换 (含反向边 04→00)
const TRANSITIONS: Record<string, string[]> = {
  '00': ['01'], '01': ['02','04'], '02': ['03','04'], '03': [], '04': ['00']
}
function canTransition(to: string): boolean {
  const from = current.status || '00'
  return TRANSITIONS[from]?.includes(to) || false
}
function canRejectFrom(): boolean {
  return canTransition('04')
}

// === 质量门禁 ===
const gateStandards = [
  { icon: '✅', text: '单测覆盖率 ≥60%', hint: 'unit_test_coverage', cls: 'pass' },
  { icon: '✅', text: '代码扫描 0 高危', hint: 'code_scan_passed = Y', cls: 'pass' },
  { icon: '⚠️', text: 'PRD 文档完整', hint: 'prd_completed = Y', cls: 'warn' },
  { icon: '✅', text: '接口文档已更新', hint: 'api_doc_updated = Y', cls: 'pass' }
]

const gateItems = computed(() => {
  const cov = Number(form.unitTestCoverage ?? 0)
  return [
    { key: 'cov', name: '单测覆盖率', detail: `${cov.toFixed(1)}% / 要求 ≥60%`, passed: cov >= 60 },
    { key: 'scan', name: '代码扫描', detail: form.codeScanPassed === 'Y' ? '已通过' : '未通过', passed: form.codeScanPassed === 'Y' },
    { key: 'prd', name: 'PRD 完整', detail: form.prdCompleted === 'Y' ? '已完整' : '缺失', passed: form.prdCompleted === 'Y' },
    { key: 'api', name: 'API 文档', detail: form.apiDocUpdated === 'Y' ? '已更新' : '未更新', passed: form.apiDocUpdated === 'Y' }
  ]
})

const gateOverall = computed(() => {
  const items = gateItems.value
  const passedCount = items.filter(i => i.passed).length
  if (passedCount === 4) return { label: '全部通过 (qualityGatePassed=Y)', type: 'success' as const }
  return { label: `${passedCount}/4 通过`, type: 'danger' as const }
})

// === CRUD ===
async function getList() {
  listLoading.value = true
  try {
    const res: any = await listSubmission(queryParams)
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

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (current.submissionId) {
      await updateSubmission({ ...form, submissionId: current.submissionId })
      ElMessage.success('更新成功')
    } else {
      const res: any = await addSubmission(form)
      if (res.code === 200) {
        ElMessage.success('保存成功')
        await getList()
        const latest = list.value.find(x => x.title === form.title)
        if (latest?.submissionId) Object.assign(current, latest)
      }
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function transition(to: string) {
  if (!current.submissionId) return
  try {
    const res: any = await updateSubmission({ submissionId: current.submissionId, title: current.title, status: to })
    if (res.code === 200) {
      ElMessage.success(`状态切换至 ${statusTagFor(to).label}`)
      const r = await getSubmission(current.submissionId)
      if (r.code === 200) {
        Object.assign(current, r.data)
        Object.assign(form, r.data)
      }
      await getList()
    } else if (res.code === 708) {
      ElMessage.error('❌ 质量门禁未通过 (708):需 4 项全部 Y 才能进入「已通过」')
    } else {
      ElMessage.error(res.msg || '状态转换失败')
    }
  } catch (e: any) {
    const code = e?.code
    if (code === 708) {
      ElMessage.error('❌ 质量门禁未通过 (708)')
    } else if (code === 602) {
      ElMessage.error('❌ 必填字段缺失 (602)')
    } else {
      ElMessage.error(e?.msg || '状态转换失败')
    }
  }
}

function openReject() {
  rejectReasonInput.value = ''
  rejectDialog.value = true
  pendingReject.value = true
}

async function confirmReject() {
  if (!rejectReasonInput.value.trim()) {
    ElMessage.warning('请填写退回原因')
    return
  }
  if (!current.submissionId) return
  try {
    const res: any = await updateSubmission({
      submissionId: current.submissionId,
      title: current.title,
      status: '04',
      rejectReason: rejectReasonInput.value
    })
    if (res.code === 200) {
      ElMessage.success('已退回 (→04)')
      rejectDialog.value = false
      pendingReject.value = false
      const r = await getSubmission(current.submissionId)
      if (r.code === 200) {
        Object.assign(current, r.data)
        Object.assign(form, r.data)
      }
      await getList()
    }
  } catch (e: any) {
    ElMessage.error(e?.msg || '退回失败')
  }
}

async function loadSubmission(row: Submission) {
  if (!row.submissionId) return
  const res: any = await getSubmission(row.submissionId)
  if (res.code === 200 && res.data) {
    Object.assign(form, res.data)
    Object.assign(current, res.data)
    ElMessage.info(`已载入 ${res.data.submissionNo}`)
  }
}

async function handleDelete(row: Submission) {
  if (!row.submissionId) return
  await ElMessageBox.confirm(`确认删除提测单 "${row.submissionNo}"?`, '提示', { type: 'warning' })
  await delSubmission(row.submissionId)
  ElMessage.success('删除成功')
  if (current.submissionId === row.submissionId) newSubmission()
  await getList()
}

function newSubmission() {
  Object.assign(form, emptyForm())
  Object.keys(current).forEach(k => delete (current as any)[k])
  formRef.value?.clearValidate()
}

onMounted(async () => {
  await loadProjects()
  await getList()
})
</script>

<style scoped>
.submission-page { padding: 20px; }
.page-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 20px;
}
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.btn-row { display: flex; gap: 10px; flex-wrap: wrap; }
.mt-md { margin-top: 16px; }

.gate-standard { margin-bottom: 12px; }
.gate-card {
  border-radius: 8px; padding: 14px; text-align: center;
  border: 1px solid transparent;
}
.gate-card.pass { background: #ecfdf5; border-color: #d1fae5; }
.gate-card.warn { background: #fef3c7; border-color: #fde68a; }
.gate-icon { font-size: 24px; margin-bottom: 6px; }
.gate-text { font-weight: 600; font-size: 13px; }
.gate-hint { font-size: 11px; color: #6b7280; margin-top: 4px; font-family: monospace; }

.gate-status-card { min-height: 600px; }
.gate-items { display: flex; flex-direction: column; gap: 12px; }
.gate-item { display: flex; gap: 12px; align-items: center; }
.gate-bullet {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-weight: bold; font-size: 16px; color: white;
}
.gate-bullet.pass { background: #10b981; }
.gate-bullet.fail { background: #ef4444; }
.gate-info { flex: 1; }
.gate-name { font-weight: 600; font-size: 13px; }
.gate-detail { color: #6b7280; font-size: 12px; }
.overall-status { text-align: center; padding: 8px 0; }
.overall-status p { margin: 8px 0 0; font-size: 12px; }
.time-title { font-size: 13px; margin: 12px 0 8px; color: #374151; }
</style>
