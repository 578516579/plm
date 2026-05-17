<!--
  立项管理 (Inception) — PRD §F1.1 + 原型 inception.html
  PLM 业务模块前端模板 — 复制改字段就能用,其余 30 个模块按这个套路生成。

  组件结构 (跟 system/role/index.vue 同源 RuoYi v3 套路):
    1. 搜索表单 (queryParams)
    2. 操作工具栏 (新增/修改/删除/导出 + 权限指令 v-hasPermi)
    3. 数据表格 (el-table 列定义)
    4. 编辑/新增 modal (el-dialog + el-form + el-form-item)
    5. <script setup> useDict + ref/reactive + onMounted 自动 getList()
-->
<template>
  <div class="app-container">
    <!-- 1. 搜索 -->
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" :inline="true" label-width="80px">
      <el-form-item label="立项编号" prop="inceptionNo">
        <el-input v-model="queryParams.inceptionNo" placeholder="INC-2026-NNNN" clearable
                  style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目名称" prop="projectName">
        <el-input v-model="queryParams.projectName" placeholder="请输入项目名称" clearable
                  style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="业务线" prop="businessLine">
        <el-select v-model="queryParams.businessLine" placeholder="请选择业务线" clearable style="width: 180px">
          <el-option v-for="d in biz_inception_business_line" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 160px">
          <el-option v-for="d in biz_inception_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 2. 工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
                   v-hasPermi="['business:inception:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate"
                   v-hasPermi="['business:inception:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete"
                   v-hasPermi="['business:inception:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport"
                   v-hasPermi="['business:inception:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 3. 表格 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="立项编号" prop="inceptionNo" width="140" />
      <el-table-column label="项目名称" prop="projectName" :show-overflow-tooltip="true" min-width="180" />
      <el-table-column label="业务线" prop="businessLine" width="120">
        <template #default="{ row }">
          <dict-tag :options="biz_inception_business_line" :value="row.businessLine" />
        </template>
      </el-table-column>
      <el-table-column label="项目类型" prop="inceptionType" width="120">
        <template #default="{ row }">
          <dict-tag :options="biz_inception_type" :value="row.inceptionType" />
        </template>
      </el-table-column>
      <el-table-column label="预计工期(月)" prop="estimatedDurationMonths" width="120" align="center" />
      <el-table-column label="AI 生成" prop="aiGenerated" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.aiGenerated === 'Y' ? 'success' : 'info'" effect="plain" size="small">
            {{ row.aiGenerated === 'Y' ? '✓' : '—' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="100">
        <template #default="{ row }">
          <dict-tag :options="biz_inception_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="240" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button link type="primary" icon="MagicStick" @click="handleAi(row)"
                     v-hasPermi="['business:inception:edit']">AI 助手</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)"
                     v-hasPermi="['business:inception:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(row)"
                     v-hasPermi="['business:inception:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total"
                v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize"
                @pagination="getList" />

    <!-- 4. 新增/修改 dialog -->
    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="项目名称" prop="projectName">
              <el-input v-model="form.projectName" placeholder="例: AgriPLM 农情大屏 v2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务线" prop="businessLine">
              <el-select v-model="form.businessLine" placeholder="请选择" style="width: 100%">
                <el-option v-for="d in biz_inception_business_line" :key="d.value"
                           :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="项目类型" prop="inceptionType">
              <el-select v-model="form.inceptionType" placeholder="请选择" style="width: 100%">
                <el-option v-for="d in biz_inception_type" :key="d.value"
                           :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计工期(月)" prop="estimatedDurationMonths">
              <el-input-number v-model="form.estimatedDurationMonths" :min="1" :max="60" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="团队规模" prop="estimatedTeam">
          <el-input v-model="form.estimatedTeam" placeholder="例: 8 人 (前端 2 / 后端 3 / 测试 2 / PM 1)" />
        </el-form-item>
        <el-form-item label="背景描述" prop="background">
          <el-input v-model="form.background" type="textarea" :rows="4"
                    placeholder="为什么要做这个项目?业务痛点是什么?" />
        </el-form-item>
        <el-form-item v-if="form.aiProposalContent" label="AI 建议书">
          <el-input v-model="form.aiProposalContent" type="textarea" :rows="6" readonly
                    style="background: #f5f7fa" />
        </el-form-item>
        <el-form-item v-if="form.aiRisks" label="AI 风险识别">
          <el-input v-model="form.aiRisks" type="textarea" :rows="4" readonly
                    style="background: #fef0f0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, getCurrentInstance } from 'vue'
import type { ComponentInternalInstance } from 'vue'
import {
  listInception, getInception, addInception, updateInception,
  delInception, aiGenerateInception, exportInception,
  type Inception, type InceptionQuery
} from '@/api/business/inception'

const { proxy } = getCurrentInstance() as ComponentInternalInstance
const { biz_inception_business_line, biz_inception_type, biz_inception_status } =
  (proxy as any).useDict('biz_inception_business_line', 'biz_inception_type', 'biz_inception_status')

// === State ===
const list = ref<Inception[]>([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<number[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const queryParams = reactive<InceptionQuery>({
  pageNum: 1, pageSize: 10,
  inceptionNo: undefined, projectName: undefined,
  businessLine: undefined, inceptionType: undefined, status: undefined
})

const initForm = (): Inception => ({
  projectName: '',
  estimatedDurationMonths: 6,
  submitterUserId: (proxy as any).$store?.state?.user?.id || 1
})
const form = ref<Inception>(initForm())

const rules = {
  projectName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
  businessLine: [{ required: false }],
  inceptionType: [{ required: false }],
  estimatedDurationMonths: [{ type: 'number', min: 1, max: 60, message: '工期 1-60 月', trigger: 'change' }]
}

// === Actions ===
function getList() {
  loading.value = true
  listInception(queryParams).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { (proxy as any).resetForm('queryRef'); handleQuery() }
function handleSelectionChange(sel: Inception[]) {
  ids.value = sel.map(x => x.inceptionId!).filter(Boolean)
  single.value = sel.length !== 1
  multiple.value = !sel.length
}

function reset() { form.value = initForm() }
function cancel() { open.value = false; reset() }

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增立项'
}

function handleUpdate(row?: Inception) {
  reset()
  const id = row?.inceptionId || ids.value[0]
  getInception(id).then((res: any) => {
    form.value = res.data
    open.value = true
    title.value = '修改立项'
  })
}

function submitForm() {
  ;(proxy as any).$refs.formRef.validate((valid: boolean) => {
    if (!valid) return
    const save = form.value.inceptionId ? updateInception : addInception
    save(form.value).then(() => {
      ;(proxy as any).$modal.msgSuccess(form.value.inceptionId ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

function handleDelete(row?: Inception) {
  const targets = row?.inceptionId ? [row.inceptionId] : ids.value
  ;(proxy as any).$modal.confirm(`确认删除立项编号 ${targets.join(', ')} ?`).then(() =>
    delInception(targets)
  ).then(() => {
    getList()
    ;(proxy as any).$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  ;(proxy as any).download('/business/inception/export', { ...queryParams }, `inception_${Date.now()}.xlsx`)
}

/** PRD §F1.1 — AI 立项助手:调用 /ai/generate/{id} */
function handleAi(row: Inception) {
  if (!row.inceptionId) return
  ;(proxy as any).$modal.loading('AI 生成立项建议中...')
  aiGenerateInception(row.inceptionId).then((res: any) => {
    ;(proxy as any).$modal.closeLoading()
    ;(proxy as any).$modal.msgSuccess('AI 建议已生成')
    form.value = res.data
    title.value = `AI 立项助手 — ${res.data.projectName}`
    open.value = true
  }).catch(() => (proxy as any).$modal.closeLoading())
}

getList()
</script>
