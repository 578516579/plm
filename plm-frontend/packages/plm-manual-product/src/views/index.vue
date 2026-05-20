<template>
  <div class="app-container">
    <!-- 搜索条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="86px">
      <el-form-item label="手册编号" prop="manualproductNo">
        <el-input v-model="queryParams.manualproductNo" placeholder="MP-..." clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目ID" prop="projectId">
        <el-input v-model="queryParams.projectId" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手册标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="标题模糊" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="产品版本" prop="productVersion">
        <el-input v-model="queryParams.productVersion" placeholder="v1.0" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="AI 生成" prop="aiGenerated">
        <el-select v-model="queryParams.aiGenerated" placeholder="全部" clearable>
          <el-option label="是 (Y)" value="Y" />
          <el-option label="否 (N)" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="作者" prop="authorUserId">
        <el-input v-model="queryParams.authorUserId" placeholder="user_id" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:manualproduct:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['business:manualproduct:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['business:manualproduct:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:manualproduct:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="manualproductId" width="80" />
      <el-table-column label="编号" align="center" prop="manualproductNo" width="160" />
      <el-table-column label="标题" align="left" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" prop="projectId" width="80" />
      <el-table-column label="版本" align="center" prop="productVersion" width="100" />
      <el-table-column label="导出格式" align="center" prop="outputFormats" width="160" :show-overflow-tooltip="true" />
      <el-table-column label="截图数" align="center" prop="screenshotsCount" width="80">
        <template #default="scope">
          <span v-if="scope.row.screenshotsCount != null">{{ scope.row.screenshotsCount }}</span>
          <span v-else>0</span>
        </template>
      </el-table-column>
      <el-table-column label="AI" align="center" prop="aiGenerated" width="70">
        <template #default="scope">
          <el-tag v-if="scope.row.aiGenerated === 'Y'" type="warning" size="small">AI</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="status_options" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="生成时间" align="center" prop="generatedAt" width="160">
        <template #default="scope"><span>{{ parseTime(scope.row.generatedAt) }}</span></template>
      </el-table-column>
      <el-table-column label="作者" align="center" prop="authorUserId" width="80" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:manualproduct:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:manualproduct:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="840px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="手册编号" prop="manualproductNo">
              <el-input v-model="form.manualproductNo" placeholder="留空自动生成 MP-YYYY-NNNN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目ID" prop="projectId">
              <el-input v-model="form.projectId" placeholder="必填" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="手册标题" prop="title">
              <el-input v-model="form.title" placeholder="一句话描述本手册内容" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品版本" prop="productVersion">
              <el-input v-model="form.productVersion" placeholder="v1.0.0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" :disabled="!form.manualproductId" placeholder="请选择">
                <el-option v-for="d in status_options" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="包含模块" prop="includeModules">
              <el-input v-model="form.includeModules" placeholder="多个模块用,分隔,如: project,requirement,task" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="导出格式" prop="outputFormats">
              <el-input v-model="form.outputFormats" placeholder="多个格式用,分隔,如: word,pdf,html,h5" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="正文内容" prop="content">
              <el-input v-model="form.content" type="textarea" :rows="6" placeholder="Markdown 兼容,AI 可一键生成" />
            </el-form-item>
          </el-col>

          <!-- 截图配置 -->
          <el-col :span="24">
            <el-divider content-position="left">截图（AI 自动描述）</el-divider>
          </el-col>
          <el-col :span="24">
            <el-form-item label="截图 URLs" prop="screenshotsUrls">
              <el-input v-model="form.screenshotsUrls" type="textarea" :rows="3" placeholder="多个 URL 用,分隔或换行" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截图数" prop="screenshotsCount">
              <el-input-number v-model="form.screenshotsCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="AI 生成" prop="aiGenerated">
              <el-select v-model="form.aiGenerated" placeholder="Y/N">
                <el-option label="是 (Y)" value="Y" />
                <el-option label="否 (N)" value="N" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者 ID" prop="authorUserId">
              <el-input v-model="form.authorUserId" placeholder="user_id" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ManualProduct" lang="ts">
import { listManualProduct, getManualProduct, addManualProduct, updateManualProduct, delManualProduct } from '../api'
import type { ManualProductForm, ManualProductQuery } from '../types'

const { proxy } = getCurrentInstance() as any
const { biz_manualproduct_status: status_options } = toRefs<any>(proxy.useDict('biz_manualproduct_status'))

const list = ref<ManualProductForm[]>([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<(number | string)[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const dialog = reactive({ title: '', visible: false })
const form = ref<ManualProductForm>({})

const queryParams = ref<ManualProductQuery>({
  pageNum: 1, pageSize: 10,
  manualproductNo: undefined, projectId: undefined, title: undefined,
  productVersion: undefined, aiGenerated: undefined,
  status: undefined, authorUserId: undefined
})

const rules = {
  title: [{ required: true, message: '手册标题不能为空', trigger: 'blur' }],
  projectId: [{ required: true, message: '关联项目不能为空', trigger: 'blur' }],
  productVersion: [{ required: true, message: '产品版本不能为空', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listManualProduct(queryParams.value).then((res: any) => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    manualproductNo: undefined, projectId: undefined, title: undefined,
    productVersion: undefined, includeModules: undefined, content: undefined,
    screenshotsUrls: undefined, screenshotsCount: 0,
    outputFormats: 'pdf', aiGenerated: 'N',
    status: '00', authorUserId: undefined
  }
  proxy.resetForm('formRef')
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection: ManualProductForm[]) {
  ids.value = selection.map(item => item.manualproductId!)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); dialog.title = '新增产品手册'; dialog.visible = true }

function handleUpdate(row?: ManualProductForm) {
  reset()
  const id = row?.manualproductId ?? ids.value[0]
  getManualProduct(id as number).then((res: any) => {
    form.value = res.data
    dialog.title = '修改产品手册'
    dialog.visible = true
  })
}

function submitForm() {
  ;(proxy.$refs.formRef as any).validate((valid: boolean) => {
    if (!valid) return
    const fn = form.value.manualproductId ? updateManualProduct : addManualProduct
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.manualproductId ? '修改成功' : '新增成功')
      dialog.visible = false
      getList()
    })
  })
}

function cancel() { dialog.visible = false; reset() }

function handleDelete(row?: ManualProductForm) {
  const toDelete = row?.manualproductId ? [row.manualproductId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的 ' + toDelete.length + ' 项？')
    .then(() => delManualProduct(toDelete as number[]))
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => {})
}

function handleExport() {
  proxy.download('business/manualproduct/export', { ...queryParams.value }, '产品手册_' + new Date().getTime() + '.xlsx')
}

getList()
</script>
