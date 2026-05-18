<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="架构标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入架构标题" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="架构模式" prop="archMode">
        <el-select v-model="queryParams.archMode" placeholder="全部" clearable style="width:130px">
          <el-option v-for="d in biz_arch_mode" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="部署方式" prop="deploymentType">
        <el-select v-model="queryParams.deploymentType" placeholder="全部" clearable style="width:130px">
          <el-option v-for="d in biz_arch_deployment" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in biz_arch_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:arch:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:arch:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:arch:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="archNo" width="160" />
      <el-table-column label="架构标题" align="center" prop="title" min-width="160" />
      <el-table-column label="架构模式" align="center" prop="archMode" width="110">
        <template #default="{ row }">
          <dict-tag :options="biz_arch_mode" :value="row.archMode" />
        </template>
      </el-table-column>
      <el-table-column label="技术栈" align="center" prop="primaryStack" width="110">
        <template #default="{ row }">
          <dict-tag :options="biz_arch_stack" :value="row.primaryStack" />
        </template>
      </el-table-column>
      <el-table-column label="数据库" align="center" prop="databaseChoice" width="100">
        <template #default="{ row }">
          <dict-tag :options="biz_arch_database" :value="row.databaseChoice" />
        </template>
      </el-table-column>
      <el-table-column label="部署方式" align="center" prop="deploymentType" width="110">
        <template #default="{ row }">
          <dict-tag :options="biz_arch_deployment" :value="row.deploymentType" />
        </template>
      </el-table-column>
      <el-table-column label="AI生成" align="center" width="85">
        <template #default="{ row }">
          <el-tag v-if="row.aiGenerated === 'Y'" type="warning" size="small">AI</el-tag>
          <span v-else style="color:#c0c4cc;font-size:12px">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="{ row }">
          <dict-tag :options="biz_arch_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:arch:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:arch:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="架构标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入架构标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="架构模式" prop="archMode">
              <el-select v-model="form.archMode" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_arch_mode" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="技术栈" prop="primaryStack">
              <el-select v-model="form.primaryStack" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_arch_stack" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据库" prop="databaseChoice">
              <el-select v-model="form.databaseChoice" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_arch_database" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部署方式" prop="deploymentType">
              <el-select v-model="form.deploymentType" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_arch_deployment" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="AI编排" prop="aiOrchestration">
              <el-select v-model="form.aiOrchestration" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_arch_ai_engine" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="IoT协议" prop="iotProtocol">
              <el-select v-model="form.iotProtocol" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_arch_iot_protocol" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="设计内容" prop="designContent">
              <el-input v-model="form.designContent" type="textarea" :rows="5" placeholder="架构设计说明（Markdown）" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="NFR 映射" prop="nfrMapping">
              <el-input v-model="form.nfrMapping" type="textarea" :rows="3" placeholder="非功能需求映射（JSON）" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="系统架构详情" v-model="detailVisible" width="840px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="编号">{{ detail.archNo }}</el-descriptions-item>
        <el-descriptions-item label="架构标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="架构模式">
          <dict-tag :options="biz_arch_mode" :value="detail.archMode" />
        </el-descriptions-item>
        <el-descriptions-item label="技术栈">
          <dict-tag :options="biz_arch_stack" :value="detail.primaryStack" />
        </el-descriptions-item>
        <el-descriptions-item label="数据库">
          <dict-tag :options="biz_arch_database" :value="detail.databaseChoice" />
        </el-descriptions-item>
        <el-descriptions-item label="部署方式">
          <dict-tag :options="biz_arch_deployment" :value="detail.deploymentType" />
        </el-descriptions-item>
        <el-descriptions-item label="AI编排">
          <dict-tag :options="biz_arch_ai_engine" :value="detail.aiOrchestration" />
        </el-descriptions-item>
        <el-descriptions-item label="IoT协议">
          <dict-tag :options="biz_arch_iot_protocol" :value="detail.iotProtocol" />
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :options="biz_arch_status" :value="detail.status" />
        </el-descriptions-item>
        <el-descriptions-item label="AI生成">
          <el-tag v-if="detail.aiGenerated === 'Y'" type="warning" size="small">是</el-tag>
          <span v-else>否</span>
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="detail.designContent" style="margin-top:16px">
        <div class="section-title">架构设计内容</div>
        <el-card shadow="never" style="margin-top:8px;max-height:300px;overflow-y:auto">
          <pre style="white-space:pre-wrap;font-size:13px;margin:0">{{ detail.designContent }}</pre>
        </el-card>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="warning" icon="MagicStick" :loading="aiLoading" @click="handleAiGenerate">AI 生成架构</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listArch, getArch, addArch, updateArch, delArch, aiGenerateArch } from '../api'
import type { ArchForm, ArchQuery } from '../types'

defineOptions({ name: 'Arch' })

const { proxy } = getCurrentInstance()!
const { biz_arch_status, biz_arch_mode, biz_arch_stack, biz_arch_database, biz_arch_deployment, biz_arch_ai_engine, biz_arch_iot_protocol } =
  proxy.useDict('biz_arch_status', 'biz_arch_mode', 'biz_arch_stack', 'biz_arch_database', 'biz_arch_deployment', 'biz_arch_ai_engine', 'biz_arch_iot_protocol')

const loading = ref(false)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref<any[]>([])
const ids = ref<(number | string)[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const detailVisible = ref(false)
const aiLoading = ref(false)
const detail = ref<any>({})
const queryRef = ref()
const formRef = ref()

const queryParams = reactive<ArchQuery>({ pageNum: 1, pageSize: 10 })
const form = ref<ArchForm>({ title: '' })
const rules = { title: [{ required: true, message: '架构标题不能为空', trigger: 'blur' }] }

function getList() {
  loading.value = true
  listArch(queryParams).then(res => {
    dataList.value = res.rows; total.value = res.total
  }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleSelectionChange(selection: any[]) {
  ids.value = selection.map(r => r.archId); multiple.value = !ids.value.length
}
function handleAdd() { form.value = { title: '' }; dialogTitle.value = '新增架构设计'; dialogVisible.value = true }
function handleEdit(row: any) {
  getArch(row.archId).then(res => { form.value = res.data; dialogTitle.value = '编辑架构设计'; dialogVisible.value = true })
}
function handleDetail(row: any) {
  getArch(row.archId).then(res => { detail.value = res.data; detailVisible.value = true })
}
function handleAiGenerate() {
  aiLoading.value = true
  aiGenerateArch(detail.value.archId).then(() => {
    ElMessage.success('AI 生成完成')
    getArch(detail.value.archId).then(r => { detail.value = r.data }); getList()
  }).catch(() => ElMessage.error('AI 生成失败')).finally(() => { aiLoading.value = false })
}
function handleDelete(row?: any) {
  const delIds = row ? [row.archId] : ids.value
  ElMessageBox.confirm('确认删除选中架构设计？', '警告', { type: 'warning' }).then(() => {
    delArch(delIds).then(() => { ElMessage.success('删除成功'); getList() })
  })
}
function handleExport() { proxy.download('business/arch/export', { ...queryParams }, 'arch.xlsx') }
function submitForm() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.value.archId ? updateArch : addArch
    api(form.value).then(() => {
      ElMessage.success(form.value.archId ? '修改成功' : '新增成功')
      dialogVisible.value = false; getList()
    })
  })
}

getList()
</script>

<style scoped>
.section-title { font-weight: 600; font-size: 14px; color: #303133; }
</style>
