<!--
  缺陷管理 — PRD §F4.6 + 原型 defects.html
  严格对齐: 4 统计卡 (总/严重/修复中/已关闭) + 缺陷列表 + AI 相似检测
-->
<template>
  <div class="app-container defect-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🐛 缺陷管理</h2>
        <p class="page-subtitle">AI 辅助缺陷描述规范化,智能相似缺陷匹配</p>
      </div>
      <el-button type="primary" @click="openAdd">
        <el-icon><Plus /></el-icon>&nbsp;新增缺陷
      </el-button>
    </div>

    <!-- 4 统计卡 (对齐原型 .grid4) -->
    <el-row :gutter="14" class="stat-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">总缺陷数</div>
          <div class="stat-value red">{{ total }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">P0/P1 严重</div>
          <div class="stat-value red">{{ criticalCount }}</div>
          <div class="stat-detail dn">需立即处理</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">修复中</div>
          <div class="stat-value am">{{ fixingCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">已关闭</div>
          <div class="stat-value success">{{ closedCount }}</div>
          <div class="stat-detail up">本迭代</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 14px">
      <template #header>
        <div class="card-header-flex">
          <span class="card-title">🐛 缺陷列表</span>
          <div style="display: flex; gap: 8px">
            <el-select v-model="queryParams.severity" placeholder="严重级别" clearable style="width: 140px" @change="getList">
              <el-option label="P0 - 致命" value="P0" />
              <el-option label="P1 - 严重" value="P1" />
              <el-option label="P2 - 一般" value="P2" />
              <el-option label="P3 - 轻微" value="P3" />
            </el-select>
            <el-input v-model="queryParams.title" placeholder="搜索标题" style="width: 200px" clearable @clear="getList" @keyup.enter="getList" />
          </div>
        </div>
      </template>
      <el-table v-loading="listLoading" :data="list" stripe>
        <el-table-column label="编号" prop="defectNo" width="160" />
        <el-table-column label="缺陷标题" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="严重级别" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="severityTag(row.severity)" size="small" effect="dark">{{ row.severity || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagFor(row.status).type" size="small">{{ statusTagFor(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100" align="center">
          <template #default="{ row }">{{ categoryLabel(row.category) }}</template>
        </el-table-column>
        <el-table-column label="指派" width="100" align="center">
          <template #default="{ row }">{{ row.assigneeUserId ? '用户#' + row.assigneeUserId : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadDefect(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-if="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 新建/编辑 Dialog (对齐原型 modal-newdefect) -->
    <el-dialog v-model="dialogVisible" :title="form.defectId ? '编辑缺陷' : '+ 新建缺陷'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="关联项目" prop="projectId" required>
          <el-select v-model="form.projectId" style="width: 100%">
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="缺陷标题" prop="title" required>
          <el-input v-model="form.title" placeholder="简要描述缺陷现象" maxlength="200" />
        </el-form-item>
        <!-- AI 相似检测结果 -->
        <el-alert v-if="similarDefects.length" type="warning" :closable="false" show-icon style="margin-bottom: 12px">
          <template #default>
            <strong>🔍 检测到 {{ similarDefects.length }} 个相似缺陷:</strong>
            <ul style="margin: 6px 0 0 18px; font-size: 12px">
              <li v-for="d in similarDefects.slice(0, 3)" :key="d.defectId">
                {{ d.defectNo }} - {{ d.title }} (相似度 {{ d.similarity || '?' }}%)
              </li>
            </ul>
          </template>
        </el-alert>
        <el-form-item label="复现步骤" prop="reproduceSteps">
          <el-input v-model="form.reproduceSteps" type="textarea" :rows="3"
            placeholder="1. 打开页面...&#10;2. 点击按钮...&#10;3. 出现异常..." />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="预期结果" prop="expectedResult">
              <el-input v-model="form.expectedResult" placeholder="正确行为" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际结果" prop="actualResult">
              <el-input v-model="form.actualResult" placeholder="错误现象" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="严重级别" prop="severity">
              <el-select v-model="form.severity" style="width: 100%">
                <el-option label="P0 - 致命" value="P0" />
                <el-option label="P1 - 严重" value="P1" />
                <el-option label="P2 - 一般" value="P2" />
                <el-option label="P3 - 轻微" value="P3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="category">
              <el-select v-model="form.category" style="width: 100%">
                <el-option label="功能缺陷" value="functional" />
                <el-option label="性能问题" value="performance" />
                <el-option label="UI/UX" value="ui" />
                <el-option label="安全" value="security" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="form.defectId" label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="新建" value="00" />
            <el-option label="已确认" value="01" />
            <el-option label="修复中" value="02" />
            <el-option label="待验证" value="03" />
            <el-option label="已关闭" value="04" />
            <el-option label="重开" value="05" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.defectId" label="解决方案" prop="resolution">
          <el-input v-model="form.resolution" type="textarea" :rows="2" placeholder="修复方案或验证要点" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="!form.defectId" type="success" :loading="aiLoading" @click="aiCheck">
          <el-icon><MagicStick /></el-icon>&nbsp;✨ AI 相似缺陷检测
        </el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ form.defectId ? '保存' : '✅ 提交缺陷' }}
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
  listDefect, addDefect, updateDefect, delDefect, getDefect, aiMatchDefect, listProjectsForSelect,
  type Defect, type DefectQuery
} from '@/api/business/defect'

const dialogVisible = ref(false)
const formRef = ref()
const saving = ref(false)
const aiLoading = ref(false)
const listLoading = ref(false)
const similarDefects = ref<any[]>([])

const emptyForm = (): Defect => ({ projectId: 0, title: '', severity: 'P2', category: 'functional', status: '00' })
const form = reactive<Defect>(emptyForm())
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入缺陷标题', trigger: 'blur' }]
}

const list = ref<Defect[]>([])
const total = ref(0)
const queryParams = reactive<DefectQuery>({ pageNum: 1, pageSize: 10 })
const projectOptions = ref<Array<{ id: number; projectName: string }>>([])

const criticalCount = computed(() => list.value.filter(d => d.severity === 'P0' || d.severity === 'P1').length)
const fixingCount = computed(() => list.value.filter(d => d.status === '02').length)
const closedCount = computed(() => list.value.filter(d => d.status === '04').length)

const statusMap: Record<string, { label: string; type: any }> = {
  '00': { label: '新建', type: 'info' },
  '01': { label: '已确认', type: 'warning' },
  '02': { label: '修复中', type: 'primary' },
  '03': { label: '待验证', type: 'warning' },
  '04': { label: '已关闭', type: 'success' },
  '05': { label: '重开', type: 'danger' }
}
const statusTagFor = (s?: string) => statusMap[s || ''] || { label: s || '-', type: 'info' as any }

function severityTag(s?: string): any {
  return ({ P0: 'danger', P1: 'danger', P2: 'warning', P3: 'info' } as Record<string,string>)[s || ''] || 'info'
}

function categoryLabel(v?: string) {
  return ({ functional: '功能', performance: '性能', ui: 'UI/UX', security: '安全' } as Record<string,string>)[v || ''] || v || '-'
}

async function getList() {
  listLoading.value = true
  try { const res: any = await listDefect(queryParams); list.value = res.rows || []; total.value = res.total || 0 } finally { listLoading.value = false }
}

async function loadProjects() {
  try { const res: any = await listProjectsForSelect(); projectOptions.value = res.rows || [] } catch {}
}

function openAdd() { Object.assign(form, emptyForm()); similarDefects.value = []; dialogVisible.value = true }

async function loadDefect(row: Defect) {
  if (!row.defectId) return
  const res: any = await getDefect(row.defectId)
  if (res.code === 200 && res.data) { Object.assign(form, res.data); dialogVisible.value = true }
}

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.defectId) { await updateDefect(form); ElMessage.success('更新成功') }
    else { await addDefect(form); ElMessage.success('提交成功') }
    dialogVisible.value = false; await getList()
  } catch (e: any) { ElMessage.error(e?.msg || '保存失败') } finally { saving.value = false }
}

async function aiCheck() {
  if (!form.title.trim()) { ElMessage.warning('请先填写缺陷标题'); return }
  aiLoading.value = true
  try {
    const res: any = await aiMatchDefect({ title: form.title, description: form.description })
    similarDefects.value = res?.data || res?.rows || []
    if (!similarDefects.value.length) ElMessage.success('未检测到相似缺陷')
    else ElMessage.warning(`检测到 ${similarDefects.value.length} 个相似缺陷`)
  } catch (e: any) {
    similarDefects.value = []
    ElMessage.error(e?.msg || 'AI 检测失败')
  } finally { aiLoading.value = false }
}

async function handleDelete(row: Defect) {
  if (!row.defectId) return
  await ElMessageBox.confirm(`确认删除 "${row.defectNo}"?`, '提示', { type: 'warning' })
  await delDefect(row.defectId); ElMessage.success('删除成功'); await getList()
}

onMounted(() => { getList(); loadProjects() })
</script>

<style scoped>
.defect-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; }
.page-subtitle { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.card-title { font-weight: 600; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.stat-row :deep(.el-card__body) { padding: 14px 16px; text-align: left; }
.stat-label { color: #6b7280; font-size: 12px; margin-bottom: 5px; }
.stat-value { font-size: 26px; font-weight: 700; }
.stat-value.red { color: #ef4444; }
.stat-value.am { color: #f59e0b; }
.stat-value.success { color: #10b981; }
.stat-detail { font-size: 12px; color: #6b7280; margin-top: 3px; }
.stat-detail.up { color: #10b981; }
.stat-detail.dn { color: #ef4444; }
</style>
