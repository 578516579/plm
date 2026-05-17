<!--
  立项管理 (Inception) — PRD §F1.1 + 原型 inception.html
  改写时间: 2026-05-17 - 跟原型 inception.html line 132-165 + JS runInceptionAI line 696-723 对齐

  原型布局 (不是 admin CRUD!):
    左栏:
      ┌─ 📝 立项信息录入 (form) ─┐
      │ 项目名称 * / 业务线 / 项目类型           │
      │ 背景与诉求 (textarea)                  │
      │ 预计工期(月) / 团队规模                  │
      │ [✨ AI 分析并生成立项建议书]            │
      └───────────────────────────┘
      ┌─ ⚠️ AI 风险识别 (条件显示) ────┐ 多条 risk:
      │ {level=warning|critical,        │
      │  title, description}            │
      └───────────────────────────┘
    右栏:
      ┌─ 📋 立项建议书预览 ────────┐
      │ 一、项目背景                  │
      │ 二、市场机会 (含 marketSize…)  │
      │ 三、ROI 预估 (4 个数值卡片)    │
      │ 四、建议决策                  │
      │ [✅ 提交立项审批 → competitive] │
      └─────────────────────┘

  辅助列表 modal: 仅在右上角"我的立项单"按钮触发,弹 list dialog 选择载入。
-->
<template>
  <div class="app-container inception-workspace">
    <!-- 顶部: 标题 + 单选立项单的按钮 -->
    <div class="ph">
      <div>
        <div class="pt">项目立项</div>
        <div class="ps">AI 辅助完成立项建议书,从想法到立项报告</div>
      </div>
      <el-button-group>
        <el-button icon="Document" @click="openList">📁 我的立项单</el-button>
        <el-button type="primary" icon="Plus" @click="newInception">📝 新建立项</el-button>
      </el-button-group>
    </div>

    <!-- 主体 2 栏布局 -->
    <el-row :gutter="20" style="margin-top: 16px">
      <!-- 左栏: form + (条件) AI 风险卡 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>📝 立项信息录入 <span v-if="form.inceptionNo" style="color: #909399; font-size: 12px">({{ form.inceptionNo }})</span></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" label-position="top">
            <el-form-item label="项目名称 *" prop="projectName">
              <el-input v-model="form.projectName" placeholder="例: 农业病虫害智能识别与防治决策系统" />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="业务线" prop="businessLine">
                  <el-select v-model="form.businessLine" placeholder="选择业务线" style="width: 100%">
                    <el-option v-for="d in biz_inception_biz_line" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="项目类型" prop="inceptionType">
                  <el-select v-model="form.inceptionType" placeholder="选择类型" style="width: 100%">
                    <el-option v-for="d in biz_inception_type" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="背景与诉求" prop="background">
              <el-input v-model="form.background" type="textarea" :rows="5"
                        placeholder="项目背景、业务痛点、用户场景..." />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="预计工期(月)" prop="estimatedDurationMonths">
                  <el-input-number v-model="form.estimatedDurationMonths" :min="1" :max="60" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="预计团队规模" prop="estimatedTeam">
                  <el-input v-model="form.estimatedTeam" placeholder="如: 产品×1 前端×2 后端×3 测试×2 AI×2" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-button type="primary" plain :loading="aiLoading" :disabled="!form.projectName"
                       icon="MagicStick" style="width: 100%" @click="handleAiGenerate">
              ✨ AI 分析并生成立项建议书
            </el-button>
            <el-button v-if="form.aiGenerated === 'Y'" plain
                       icon="Edit" style="width: 100%; margin-top: 8px" @click="handleSave">
              💾 保存当前修改
            </el-button>
          </el-form>
        </el-card>

        <!-- AI 风险卡 (条件显示) -->
        <el-card v-if="risks.length > 0" shadow="hover" style="margin-top: 16px">
          <template #header>⚠️ AI 风险识别 ({{ risks.length }} 项)</template>
          <div v-for="(r, i) in risks" :key="i"
               :style="riskStyle(r.level)">
            <div style="font-weight: 700">
              {{ r.level === 'critical' ? '🔴' : '⚠️' }} {{ r.title }}
            </div>
            <div style="color: #606266; margin-top: 4px; font-size: 13px">{{ r.description }}</div>
          </div>
        </el-card>
      </el-col>

      <!-- 右栏: AI 建议书预览 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            📋 立项建议书预览
            <span v-if="form.aiGeneratedAt" style="color: #909399; font-size: 12px; margin-left: 8px">
              ✓ 已生成 {{ form.aiGeneratedAt }}
            </span>
          </template>
          <!-- 未生成 placeholder -->
          <div v-if="form.aiGenerated !== 'Y'" style="text-align: center; padding: 40px; color: #909399">
            <div style="font-size: 40px; margin-bottom: 10px">🚀</div>
            <div>点击左侧 "AI 分析并生成立项建议书" 开始</div>
          </div>
          <!-- 已生成 4 段报告 -->
          <div v-else class="ai-report">
            <h3 style="margin-top: 0">📋 立项建议书 · {{ form.projectName }}</h3>

            <h4>一、项目背景</h4>
            <p>{{ form.aiBackground }}</p>

            <h4>二、市场机会</h4>
            <p>{{ form.aiMarketOpportunity }}</p>
            <el-row :gutter="8" style="margin: 8px 0">
              <el-col :span="12">
                <el-statistic title="市场规模" :value="form.marketSize" :precision="0" suffix=" 亿" />
              </el-col>
              <el-col :span="12">
                <el-statistic title="数字化渗透率" :value="form.digitalPenetration" :precision="1" suffix=" %" />
              </el-col>
            </el-row>

            <h4>三、ROI 预估</h4>
            <p style="white-space: pre-line">{{ form.aiRoiEstimate }}</p>
            <el-row :gutter="8" style="margin: 8px 0">
              <el-col :span="8">
                <el-statistic title="开发成本" :value="form.devCostEstimate" :precision="0" suffix=" 万" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="首年营收" :value="form.firstYearRevenue" :precision="0" suffix=" 万" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="ROI" :value="form.roiMultiple" :precision="1" suffix=" 倍"
                              :value-style="{ color: '#67C23A' }" />
              </el-col>
            </el-row>

            <h4>四、建议决策</h4>
            <el-alert :title="form.aiRecommendDecision" type="success" :closable="false" show-icon />
            <el-row :gutter="8" style="margin: 12px 0">
              <el-col :span="8">
                <el-tag size="large" type="warning">优先级 {{ form.recommendedPriority }}</el-tag>
              </el-col>
              <el-col :span="8">
                <el-tag size="large" type="info">{{ form.recommendedStartQuarter }}</el-tag>
              </el-col>
              <el-col :span="8">
                <el-tag size="large">分 {{ form.deliveryPhases }} 期交付</el-tag>
              </el-col>
            </el-row>

            <!-- 状态机操作: 草稿可提交,审批中可批准/驳回,已批准可"转项目" -->
            <div style="margin-top: 16px; padding-top: 12px; border-top: 1px solid #ebeef5">
              <div style="margin-bottom: 8px; color: #909399; font-size: 13px">
                当前状态: <dict-tag :options="biz_inception_status" :value="form.status" />
              </div>
              <el-button v-if="form.status === '00'" type="primary" @click="submitForApproval">
                📤 提交审批 → 飞书推送审批人
              </el-button>
              <el-button v-if="form.status === '03'" type="success" @click="convertToProject">
                ✅ 转项目 (跳转 projects 页)
              </el-button>
              <el-button v-if="form.status === '04'" type="warning" @click="reworkRejected">
                ✏️ 打回重写
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 我的立项单 list dialog -->
    <el-dialog v-model="listOpen" title="📁 我的立项单" width="900px">
      <el-table :data="list" @row-click="loadInception">
        <el-table-column prop="inceptionNo" label="编号" width="160" />
        <el-table-column prop="projectName" label="项目名称" min-width="200" />
        <el-table-column label="业务线" width="120">
          <template #default="{ row }"><dict-tag :options="biz_inception_biz_line" :value="row.businessLine" /></template>
        </el-table-column>
        <el-table-column label="AI" width="60" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.aiGenerated === 'Y' ? 'success' : 'info'">
              {{ row.aiGenerated === 'Y' ? '✓' : '—' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }"><dict-tag :options="biz_inception_status" :value="row.status" /></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, getCurrentInstance } from 'vue'
import type { ComponentInternalInstance } from 'vue'
import {
  listInception, getInception, addInception, updateInception,
  aiGenerateInception, parseRisks,
  type Inception
} from '@/api/business/inception'

const { proxy } = getCurrentInstance() as ComponentInternalInstance
const { biz_inception_biz_line, biz_inception_type, biz_inception_status, biz_inception_priority } =
  (proxy as any).useDict('biz_inception_biz_line', 'biz_inception_type', 'biz_inception_status', 'biz_inception_priority')

const aiLoading = ref(false)
const listOpen = ref(false)
const list = ref<Inception[]>([])

const initForm = (): Inception => ({
  projectName: '',
  estimatedDurationMonths: 6,
  submitterUserId: (proxy as any).$store?.state?.user?.id || 1,
  status: '00'
})
const form = ref<Inception>(initForm())

const rules = {
  projectName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }]
}

const risks = computed(() => parseRisks(form.value))

function riskStyle(level: string) {
  const base = 'padding: 9px 12px; border-radius: 7px; margin-bottom: 8px; font-size: 13px;'
  if (level === 'critical') return base + ' background: #fef0f0; border-left: 3px solid #f56c6c;'
  return base + ' background: #fdf6ec; border-left: 3px solid #e6a23c;'
}

function newInception() {
  form.value = initForm()
  ;(proxy as any).$modal.msgSuccess('已开始新立项')
}

async function openList() {
  const res: any = await listInception({ pageSize: 100 })
  list.value = res.rows
  listOpen.value = true
}

async function loadInception(row: Inception) {
  if (!row.inceptionId) return
  const res: any = await getInception(row.inceptionId)
  form.value = res.data
  listOpen.value = false
}

async function handleAiGenerate() {
  // 1. 没存过 → 先 add (status='00')
  if (!form.value.inceptionId) {
    const res: any = await addInception(form.value)
    if (res.code !== 200) return
    // 拿回 id (后端 addInception 应 return code+data,但 RuoYi 通常 return rowsAffected)
    // 这里走 list 查找方式获取最新
    const lst: any = await listInception({ projectName: form.value.projectName, pageSize: 5 })
    const fresh = lst.rows.find((r: Inception) => r.projectName === form.value.projectName)
    if (fresh) form.value.inceptionId = fresh.inceptionId
  } else {
    // 2. 存过 → 先 update (保存最新 form 内容)
    await updateInception(form.value)
  }
  if (!form.value.inceptionId) return

  // 3. 调 AI 端点
  aiLoading.value = true
  try {
    const ai: any = await aiGenerateInception(form.value.inceptionId)
    if (ai.code === 200 && ai.data) {
      form.value = ai.data
      ;(proxy as any).$modal.msgSuccess('立项建议书已生成,共识别 ' + risks.value.length + ' 项风险')
    }
  } finally {
    aiLoading.value = false
  }
}

async function handleSave() {
  if (!form.value.inceptionId) return
  await updateInception(form.value)
  ;(proxy as any).$modal.msgSuccess('已保存')
}

async function submitForApproval() {
  if (!form.value.inceptionId) return
  // 状态机 00 → 01
  await updateInception({ ...form.value, status: '01' })
  ;(proxy as any).$modal.msgSuccess('立项申请已提交审批,将通过飞书推送给审批人')
  // 原型: 0.5s 跳 competitive 页
  setTimeout(() => { (proxy as any).$router.push('/business/competitive') }, 500)
}

async function convertToProject() {
  // 03 → 转项目 (创建 tb_project 记录,需要后端额外端点)
  ;(proxy as any).$modal.msgWarning('转项目功能待后端 POST /business/inception/convert/{id} 接入')
}

async function reworkRejected() {
  // 反向边 04 → 00
  await updateInception({ ...form.value, status: '00' })
  form.value.status = '00'
  ;(proxy as any).$modal.msgSuccess('已恢复为草稿,可继续编辑后重新提交')
}
</script>

<style scoped>
.inception-workspace .ph {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.inception-workspace .pt { font-size: 18px; font-weight: 700; }
.inception-workspace .ps { font-size: 13px; color: #909399; margin-top: 4px; }
.ai-report h4 { color: #303133; margin-top: 16px; margin-bottom: 8px; font-size: 14px; }
.ai-report p { color: #606266; font-size: 13.5px; line-height: 1.7; }
</style>
