<!--
  UED 设计协同 — PRD §F2.3 + 原型 ued.html (Figma 同步 + AI 规范评审)
  布局: 左 设计稿信息 + Figma 同步按钮, 右 AI 规范评审 timeline (评分卡 + 评审项列表)
-->
<template>
  <div class="app-container ued-workspace">
    <div class="ph">
      <div>
        <div class="pt">UED 设计协同</div>
        <div class="ps">Figma 同步 + AI 规范评审 (色彩/字体/间距/无障碍 6 维度), 评分 ≥80 通过</div>
      </div>
      <el-button-group>
        <el-button icon="Document" @click="openList">📁 我的设计稿</el-button>
        <el-button type="primary" icon="Plus" @click="newUed">🎨 新建设计稿</el-button>
      </el-button-group>
    </div>

    <el-row :gutter="20" style="margin-top:16px">
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            🎨 设计稿信息
            <span v-if="form.uedNo" style="color:#909399;font-size:12px">({{ form.uedNo }})</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-form-item label="项目 *" prop="projectId">
              <el-input-number v-model="form.projectId" :min="1" style="width:100%" />
            </el-form-item>
            <el-form-item label="设计稿名称 *" prop="title">
              <el-input v-model="form.title" placeholder="例: 农情大屏设计 v2" />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="设计类型" prop="designType">
                  <el-select v-model="form.designType" placeholder="选择类型" style="width:100%">
                    <el-option v-for="d in biz_ued_design_type" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="目标平台" prop="platform">
                  <el-select v-model="form.platform" placeholder="选择平台" style="width:100%">
                    <el-option v-for="d in biz_ued_platform" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="Figma File Key">
              <el-input v-model="form.figmaFileKey" placeholder="如 abc123def456">
                <template #append>
                  <el-button :disabled="!form.figmaFileKey" @click="syncFigma">📤 同步</el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="Figma URL">
              <el-input v-model="form.figmaUrl" placeholder="https://figma.com/file/..." />
            </el-form-item>
            <el-form-item label="版本" prop="version">
              <el-input v-model="form.version" placeholder="v1.0" />
            </el-form-item>
            <el-form-item label="设计说明">
              <el-input v-model="form.description" type="textarea" :rows="3" />
            </el-form-item>
            <el-button type="primary" plain :loading="aiLoading" :disabled="!form.title"
                       icon="MagicStick" style="width:100%" @click="handleAi">
              ✨ AI 规范评审
            </el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            🔍 AI 规范评审报告
            <el-tag v-if="form.complianceScore"
                    :type="form.complianceScore >= 80 ? 'success' : 'warning'"
                    size="small" style="margin-left:12px">
              {{ form.complianceScore >= 80 ? '✅ 通过' : '⚠️ 不通过' }} · {{ form.complianceScore }}/100
            </el-tag>
          </template>
          <div v-if="form.aiGenerated !== 'Y'" style="text-align:center;padding:40px;color:#909399">
            <div style="font-size:40px;margin-bottom:10px">🎨</div>
            <div>填写左侧设计稿信息后, 点击"AI 规范评审"</div>
          </div>
          <div v-else>
            <!-- Timeline: 评审项 -->
            <div style="margin-bottom:16px;display:flex;gap:16px;flex-wrap:wrap">
              <el-statistic title="✅ 通过" :value="passCount" :value-style="{ color: '#67C23A' }" />
              <el-statistic title="⚠️ 警告" :value="warningCount" :value-style="{ color: '#E6A23C' }" />
              <el-statistic title="❌ 失败" :value="failCount" :value-style="{ color: '#F56C6C' }" />
            </div>
            <div v-for="(item, i) in reviewItems" :key="i" :style="itemStyle(item.status)">
              <div style="font-weight:700">
                {{ statusIcon(item.status) }} {{ item.category }} — {{ item.message }}
              </div>
              <div v-if="item.suggestion" style="color:#606266;margin-top:4px;font-size:13px">
                💡 建议: {{ item.suggestion }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="listOpen" title="📁 我的设计稿" width="900px">
      <el-table :data="list" @row-click="loadUed">
        <el-table-column prop="uedNo" label="编号" width="160" />
        <el-table-column prop="title" label="名称" min-width="200" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column label="评分" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="(row.complianceScore || 0) >= 80 ? 'success' : 'warning'">
              {{ row.complianceScore || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, getCurrentInstance } from 'vue'
import type { ComponentInternalInstance } from 'vue'
import {
  listUed, getUed, addUed, updateUed, aiReviewUed, parseReviewItems,
  type Ued
} from '@/api/business/ued'

const { proxy } = getCurrentInstance() as ComponentInternalInstance
const { biz_ued_design_type, biz_ued_platform } = (proxy as any).useDict('biz_ued_design_type', 'biz_ued_platform')

const aiLoading = ref(false)
const listOpen = ref(false)
const list = ref<Ued[]>([])

const initForm = (): Ued => ({
  projectId: 1,
  title: '',
  version: 'v1.0',
  designerUserId: (proxy as any).$store?.state?.user?.id || 1,
  status: '00'
})
const form = ref<Ued>(initForm())

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'blur' }],
  title: [{ required: true, message: '设计稿名称必填', trigger: 'blur' }]
}

const reviewItems = computed(() => parseReviewItems(form.value))
const passCount = computed(() => reviewItems.value.filter(x => x.status === 'pass').length)
const warningCount = computed(() => reviewItems.value.filter(x => x.status === 'warning').length)
const failCount = computed(() => reviewItems.value.filter(x => x.status === 'fail').length)

function statusIcon(s: string) {
  return s === 'pass' ? '✅' : s === 'warning' ? '⚠️' : '❌'
}
function itemStyle(s: string) {
  const base = 'padding:9px 12px;border-radius:7px;margin-bottom:8px;font-size:13px;'
  if (s === 'fail') return base + 'background:#fef0f0;border-left:3px solid #f56c6c;'
  if (s === 'warning') return base + 'background:#fdf6ec;border-left:3px solid #e6a23c;'
  return base + 'background:#f0f9eb;border-left:3px solid #67c23a;'
}

function newUed() { form.value = initForm() }
async function openList() {
  const res: any = await listUed({ pageSize: 100 })
  list.value = res.rows
  listOpen.value = true
}
async function loadUed(row: Ued) {
  if (!row.uedId) return
  const res: any = await getUed(row.uedId)
  form.value = res.data
  listOpen.value = false
}

async function syncFigma() {
  ;(proxy as any).$modal.msgWarning('Figma 同步功能待接 MCP figma plugin')
}

async function handleAi() {
  if (!form.value.uedId) {
    await addUed(form.value)
    const r: any = await listUed({ title: form.value.title, pageSize: 5 })
    const fresh = r.rows.find((x: Ued) => x.title === form.value.title)
    if (fresh) form.value.uedId = fresh.uedId
  } else {
    await updateUed(form.value)
  }
  if (!form.value.uedId) return
  aiLoading.value = true
  try {
    const ai: any = await aiReviewUed(form.value.uedId)
    if (ai.code === 200) {
      form.value = ai.data
      ;(proxy as any).$modal.msgSuccess(`评审完成: ${passCount.value} 通过, ${warningCount.value} 警告`)
    }
  } finally { aiLoading.value = false }
}
</script>

<style scoped>
.ued-workspace .ph { display: flex; align-items: center; justify-content: space-between; }
.ued-workspace .pt { font-size: 18px; font-weight: 700; }
.ued-workspace .ps { font-size: 13px; color: #909399; margin-top: 4px; }
</style>
