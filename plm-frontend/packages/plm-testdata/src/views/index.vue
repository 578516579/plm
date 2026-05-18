<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="任务标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入任务标题" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="目标表" prop="targetTable">
        <el-select v-model="queryParams.targetTable" placeholder="全部" clearable style="width:140px">
          <el-option v-for="d in biz_testdata_table" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="输出格式" prop="outputFormat">
        <el-select v-model="queryParams.outputFormat" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in biz_testdata_format" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
          <el-option v-for="d in biz_testdata_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" v-hasPermi="['business:testdata:add']" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" v-hasPermi="['business:testdata:remove']" @click="handleDelete()">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" v-hasPermi="['business:testdata:export']" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="testdataNo" width="160" />
      <el-table-column label="任务标题" align="center" prop="title" min-width="160" />
      <el-table-column label="目标表" align="center" prop="targetTable" width="130">
        <template #default="{ row }">
          <dict-tag :options="biz_testdata_table" :value="row.targetTable" />
        </template>
      </el-table-column>
      <el-table-column label="生成数量" align="center" prop="generateCount" width="90" />
      <el-table-column label="输出格式" align="center" prop="outputFormat" width="100">
        <template #default="{ row }">
          <dict-tag :options="biz_testdata_format" :value="row.outputFormat" />
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
          <dict-tag :options="biz_testdata_status" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          <el-button link type="primary" icon="Edit" v-hasPermi="['business:testdata:edit']" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" v-hasPermi="['business:testdata:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="任务标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入任务标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标表" prop="targetTable">
              <el-select v-model="form.targetTable" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_testdata_table" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="输出格式" prop="outputFormat">
              <el-select v-model="form.outputFormat" placeholder="请选择" style="width:100%">
                <el-option v-for="d in biz_testdata_format" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生成数量" prop="generateCount">
              <el-input-number v-model="form.generateCount" :min="1" :max="100000" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="含异常值" prop="ruleIncludeOutliers">
              <el-switch v-model="form.ruleIncludeOutliers" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="字段语义" prop="fieldSemantics">
              <el-input v-model="form.fieldSemantics" type="textarea" :rows="3" placeholder="字段语义描述（JSON 或 Markdown）" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="中国坐标规则" prop="ruleChinaCoord">
              <el-switch v-model="form.ruleChinaCoord" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="时序连续规则" prop="ruleTimeContinuity">
              <el-switch v-model="form.ruleTimeContinuity" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="传感器范围" prop="ruleSensorRange">
              <el-switch v-model="form.ruleSensorRange" active-value="Y" inactive-value="N" />
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
    <el-dialog title="测试数据详情" v-model="detailVisible" width="860px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="编号">{{ detail.testdataNo }}</el-descriptions-item>
        <el-descriptions-item label="任务标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="目标表">
          <dict-tag :options="biz_testdata_table" :value="detail.targetTable" />
        </el-descriptions-item>
        <el-descriptions-item label="生成数量">{{ detail.generateCount }}</el-descriptions-item>
        <el-descriptions-item label="输出格式">
          <dict-tag :options="biz_testdata_format" :value="detail.outputFormat" />
        </el-descriptions-item>
        <el-descriptions-item label="含异常值">{{ detail.ruleIncludeOutliers === 'Y' ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="中国坐标">{{ detail.ruleChinaCoord === 'Y' ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="时序连续">{{ detail.ruleTimeContinuity === 'Y' ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="传感器范围">{{ detail.ruleSensorRange === 'Y' ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :options="biz_testdata_status" :value="detail.status" />
        </el-descriptions-item>
        <el-descriptions-item label="生成时间" :span="2">{{ detail.generatedAt }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detail.generatedContent" style="margin-top:16px">
        <div class="section-title">生成数据预览</div>
        <el-card shadow="never" style="margin-top:8px;max-height:280px;overflow-y:auto">
          <pre style="white-space:pre-wrap;font-size:12px;margin:0;font-family:monospace">{{ detail.generatedContent }}</pre>
        </el-card>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="warning" icon="MagicStick" :loading="aiLoading" @click="handleAiGenerate">AI 生成数据</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listTestdata, getTestdata, addTestdata, updateTestdata, delTestdata, aiGenerateTestdata } from '../api'
import type { TestdataForm, TestdataQuery } from '../types'

defineOptions({ name: 'Testdata' })

const { proxy } = getCurrentInstance()!
const { biz_testdata_status, biz_testdata_format, biz_testdata_table } =
  proxy.useDict('biz_testdata_status', 'biz_testdata_format', 'biz_testdata_table')

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

const queryParams = reactive<TestdataQuery>({ pageNum: 1, pageSize: 10 })
const form = ref<TestdataForm>({ title: '', generateCount: 100, ruleIncludeOutliers: 'N', ruleChinaCoord: 'Y', ruleTimeContinuity: 'N', ruleSensorRange: 'N' })
const rules = {
  title: [{ required: true, message: '任务标题不能为空', trigger: 'blur' }],
  targetTable: [{ required: true, message: '请选择目标表', trigger: 'change' }],
  outputFormat: [{ required: true, message: '请选择输出格式', trigger: 'change' }]
}

function getList() {
  loading.value = true
  listTestdata(queryParams).then(res => {
    dataList.value = res.rows; total.value = res.total
  }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleSelectionChange(selection: any[]) {
  ids.value = selection.map(r => r.testdataId); multiple.value = !ids.value.length
}
function handleAdd() {
  form.value = { title: '', generateCount: 100, ruleIncludeOutliers: 'N', ruleChinaCoord: 'Y', ruleTimeContinuity: 'N', ruleSensorRange: 'N' }
  dialogTitle.value = '新增测试数据任务'; dialogVisible.value = true
}
function handleEdit(row: any) {
  getTestdata(row.testdataId).then(res => { form.value = res.data; dialogTitle.value = '编辑测试数据任务'; dialogVisible.value = true })
}
function handleDetail(row: any) {
  getTestdata(row.testdataId).then(res => { detail.value = res.data; detailVisible.value = true })
}
function handleAiGenerate() {
  aiLoading.value = true
  aiGenerateTestdata(detail.value.testdataId).then(() => {
    ElMessage.success('AI 生成完成')
    getTestdata(detail.value.testdataId).then(r => { detail.value = r.data }); getList()
  }).catch(() => ElMessage.error('AI 生成失败')).finally(() => { aiLoading.value = false })
}
function handleDelete(row?: any) {
  const delIds = row ? [row.testdataId] : ids.value
  ElMessageBox.confirm('确认删除选中测试数据任务？', '警告', { type: 'warning' }).then(() => {
    delTestdata(delIds).then(() => { ElMessage.success('删除成功'); getList() })
  })
}
function handleExport() { proxy.download('business/testdata/export', { ...queryParams }, 'testdata.xlsx') }
function submitForm() {
  formRef.value?.validate((valid: boolean) => {
    if (!valid) return
    const api = form.value.testdataId ? updateTestdata : addTestdata
    api(form.value).then(() => {
      ElMessage.success(form.value.testdataId ? '修改成功' : '新增成功')
      dialogVisible.value = false; getList()
    })
  })
}

getList()
</script>

<style scoped>
.section-title { font-weight: 600; font-size: 14px; color: #303133; }
</style>
