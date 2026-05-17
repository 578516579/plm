<!--
  AI PRD 生成 — PRD §F2.2 + 原型 prd.html
  布局: 左 form (5 字段) + 右 4 段 AI 报告 (背景/用户故事/核心功能/验收标准) + 完整度评分
-->
<template>
  <div class="app-container prd-workspace">
    <div class="ph">
      <div>
        <div class="pt">AI PRD 生成</div>
        <div class="ps">基于 AgriKB 知识库, AI 一键生成完整 PRD (背景/用户故事/核心功能/验收标准)</div>
      </div>
      <el-button-group>
        <el-button icon="Document" @click="openList">📁 我的 PRD</el-button>
        <el-button type="primary" icon="Plus" @click="newPrd">📄 新建 PRD</el-button>
      </el-button-group>
    </div>

    <el-row :gutter="20" style="margin-top: 16px">
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            📝 PRD 信息录入
            <span v-if="form.prdNo" style="color:#909399;font-size:12px">({{ form.prdNo }})</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-form-item label="项目 *" prop="projectId">
              <el-input-number v-model="form.projectId" :min="1" style="width:100%" placeholder="项目 ID" />
            </el-form-item>
            <el-form-item label="功能名称 *" prop="title">
              <el-input v-model="form.title" placeholder="例: AI 灌溉推荐引擎" />
            </el-form-item>
            <el-form-item label="需求描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="4"
                        placeholder="痛点/目标用户/期望功能..." />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="业务场景" prop="sceneTemplate">
                  <el-input v-model="form.sceneTemplate" placeholder="如: 智慧灌溉" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="目标用户" prop="targetUser">
                  <el-input v-model="form.targetUser" placeholder="如: 农场主" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-button type="primary" plain :loading="aiLoading" :disabled="!form.title"
                       icon="MagicStick" style="width:100%" @click="handleAi">
              ✨ AI 一键生成 PRD
            </el-button>
            <el-button v-if="form.aiGenerated === 'Y'" plain icon="Edit"
                       style="width:100%;margin-top:8px" @click="handleSave">💾 保存</el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <span>📋 PRD 预览</span>
            <el-tag v-if="form.completenessScore" type="success" size="small" style="margin-left:12px">
              完整度 {{ form.completenessScore }}%
            </el-tag>
            <span v-if="form.aiGeneratedAt" style="color:#909399;font-size:12px;margin-left:8px">
              ✓ 已生成 {{ form.aiGeneratedAt }}
            </span>
          </template>
          <div v-if="form.aiGenerated !== 'Y'" style="text-align:center;padding:40px;color:#909399">
            <div style="font-size:40px;margin-bottom:10px">📄</div>
            <div>填写左侧后, 点击"AI 一键生成 PRD"</div>
          </div>
          <div v-else class="prd-report">
            <h3 style="margin-top:0">📄 {{ form.title }} · PRD v{{ form.version || '1.0' }}</h3>

            <h4>一、背景与目标</h4>
            <p>{{ form.aiBackground }}</p>

            <h4>二、用户故事</h4>
            <div v-for="(s, i) in userStories" :key="i" style="margin-bottom:8px;padding:8px;background:#f5f7fa;border-radius:6px">
              作为 <b>{{ s.role }}</b>, 我想要 <b>{{ s.want }}</b>, 以便 <b>{{ s.why }}</b>
            </div>

            <h4>三、核心功能</h4>
            <el-table :data="coreFeatures" size="small">
              <el-table-column prop="code" label="编号" width="70" align="center">
                <template #default="{ row }">
                  <el-tag size="small" type="primary">{{ row.code }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="功能" width="160" />
              <el-table-column prop="description" label="描述" />
            </el-table>

            <h4>四、验收标准</h4>
            <el-table :data="acceptance" size="small">
              <el-table-column prop="category" label="维度" width="120">
                <template #default="{ row }">
                  <el-tag size="small">{{ row.category }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="criterion" label="标准" />
              <el-table-column prop="target" label="目标" width="120" align="center">
                <template #default="{ row }">
                  <el-tag size="small" type="success">{{ row.target }}</el-tag>
                </template>
              </el-table-column>
            </el-table>

            <div style="margin-top:16px;padding-top:12px;border-top:1px solid #ebeef5">
              当前状态: <dict-tag :options="biz_prd_status" :value="form.status" />
              <el-button v-if="form.status === '00'" type="primary" size="small" style="margin-left:12px" @click="changeStatus('01')">📤 送评审</el-button>
              <el-button v-if="form.status === '01'" type="warning" size="small" style="margin-left:12px" @click="changeStatus('00')">↩️ 打回</el-button>
              <el-button v-if="form.status === '01'" type="success" size="small" style="margin-left:8px" @click="changeStatus('02')">✅ 确认</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="listOpen" title="📁 我的 PRD 文档" width="900px">
      <el-table :data="list" @row-click="loadPrd">
        <el-table-column prop="prdNo" label="编号" width="160" />
        <el-table-column prop="title" label="功能名称" min-width="200" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column label="完整度" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="(row.completenessScore || 0) >= 80 ? 'success' : 'warning'">
              {{ row.completenessScore || 0 }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><dict-tag :options="biz_prd_status" :value="row.status" /></template>
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
  listPrd, getPrd, addPrd, updatePrd, aiGeneratePrd,
  parseUserStories, parseCoreFeatures, parseAcceptance,
  type Prd
} from '@/api/business/prd'

const { proxy } = getCurrentInstance() as ComponentInternalInstance
const { biz_prd_status } = (proxy as any).useDict('biz_prd_status')

const aiLoading = ref(false)
const listOpen = ref(false)
const list = ref<Prd[]>([])

const initForm = (): Prd => ({
  projectId: 1,
  title: '',
  version: 'v1.0',
  authorUserId: (proxy as any).$store?.state?.user?.id || 1,
  status: '00'
})
const form = ref<Prd>(initForm())

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'blur' }],
  title: [{ required: true, message: '功能名称必填', trigger: 'blur' }]
}

const userStories = computed(() => parseUserStories(form.value))
const coreFeatures = computed(() => parseCoreFeatures(form.value))
const acceptance = computed(() => parseAcceptance(form.value))

function newPrd() { form.value = initForm() }

async function openList() {
  const res: any = await listPrd({ pageSize: 100 })
  list.value = res.rows
  listOpen.value = true
}
async function loadPrd(row: Prd) {
  if (!row.prdId) return
  const res: any = await getPrd(row.prdId)
  form.value = res.data
  listOpen.value = false
}

async function handleAi() {
  if (!form.value.prdId) {
    await addPrd(form.value)
    const r: any = await listPrd({ title: form.value.title, pageSize: 5 })
    const fresh = r.rows.find((x: Prd) => x.title === form.value.title)
    if (fresh) form.value.prdId = fresh.prdId
  } else {
    await updatePrd(form.value)
  }
  if (!form.value.prdId) return
  aiLoading.value = true
  try {
    const ai: any = await aiGeneratePrd(form.value.prdId)
    if (ai.code === 200) {
      form.value = ai.data
      ;(proxy as any).$modal.msgSuccess(`PRD 生成, 完整度 ${ai.data.completenessScore}%`)
    }
  } finally { aiLoading.value = false }
}

async function handleSave() {
  if (!form.value.prdId) return
  await updatePrd(form.value)
  ;(proxy as any).$modal.msgSuccess('已保存')
}

async function changeStatus(next: string) {
  if (!form.value.prdId) return
  await updatePrd({ ...form.value, status: next as any })
  form.value.status = next as any
  ;(proxy as any).$modal.msgSuccess('状态已更新')
}
</script>

<style scoped>
.prd-workspace .ph { display: flex; align-items: center; justify-content: space-between; }
.prd-workspace .pt { font-size: 18px; font-weight: 700; }
.prd-workspace .ps { font-size: 13px; color: #909399; margin-top: 4px; }
.prd-report h4 { color: #303133; margin-top: 16px; margin-bottom: 8px; font-size: 14px; }
.prd-report p { color: #606266; font-size: 13.5px; line-height: 1.7; }
</style>
