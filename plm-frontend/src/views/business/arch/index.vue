<!--
  架构设计 — PRD §F3.1 + 原型 archdesign.html (6 select + genArchDesign 4 步 + NFR 4 项)
-->
<template>
  <div class="app-container arch-workspace">
    <div class="ph">
      <div>
        <div class="pt">系统架构设计</div>
        <div class="ps">AI 基于业务场景推荐架构方案 (4 步骤 timeline + C4 容器图 + NFR 4 维度)</div>
      </div>
      <el-button-group>
        <el-button icon="Document" @click="openList">📁 我的方案</el-button>
        <el-button type="primary" icon="Plus" @click="newArch">🏗️ 新建方案</el-button>
      </el-button-group>
    </div>

    <el-row :gutter="20" style="margin-top:16px">
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            🛠️ 架构配置选项
            <span v-if="form.archNo" style="color:#909399;font-size:12px">({{ form.archNo }})</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-form-item label="项目 *" prop="projectId">
              <el-input-number v-model="form.projectId" :min="1" style="width:100%" />
            </el-form-item>
            <el-form-item label="方案标题 *" prop="title">
              <el-input v-model="form.title" placeholder="例: AgriPLM 微服务架构方案" />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="架构模式">
                  <el-select v-model="form.archMode" placeholder="选择模式" style="width:100%">
                    <el-option v-for="d in biz_arch_mode" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="技术语言栈">
                  <el-select v-model="form.techStack" placeholder="选择技术栈" style="width:100%">
                    <el-option v-for="d in biz_arch_tech" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="数据库方案">
                  <el-select v-model="form.dbStack" placeholder="选择数据库" style="width:100%">
                    <el-option v-for="d in biz_arch_db" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="AI 编排">
                  <el-select v-model="form.aiOrchestration" placeholder="选择编排" style="width:100%">
                    <el-option v-for="d in biz_arch_ai" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="部署模式">
                  <el-select v-model="form.deployMode" placeholder="选择部署" style="width:100%">
                    <el-option v-for="d in biz_arch_deploy" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="IoT 协议">
                  <el-select v-model="form.iotProtocol" placeholder="选择协议" style="width:100%">
                    <el-option v-for="d in biz_arch_iot" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-button type="primary" plain :loading="aiLoading" :disabled="!form.title"
                       icon="MagicStick" style="width:100%" @click="handleAi">
              ✨ AI 推荐架构方案
            </el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>📐 AI 架构方案 (4 步骤 + NFR)</template>
          <div v-if="form.aiGenerated !== 'Y'" style="text-align:center;padding:40px;color:#909399">
            <div style="font-size:40px;margin-bottom:10px">🏗️</div>
            <div>勾选左侧 6 个维度, 点击"AI 推荐架构方案"</div>
          </div>
          <div v-else>
            <!-- 4 步骤 timeline -->
            <el-steps :active="4" finish-status="success" align-center style="margin-bottom:20px">
              <el-step v-for="s in timeline" :key="s.step" :title="s.name" :description="s.description" />
            </el-steps>

            <!-- NFR 4 项卡片 -->
            <el-divider content-position="left">⚙️ 非功能需求 (NFR)</el-divider>
            <el-row :gutter="12">
              <el-col :span="12">
                <div :style="nfrStyle('#409EFF')">
                  <div style="font-weight:700;margin-bottom:4px">⚡ 性能</div>
                  <div style="font-size:13px;color:#606266">{{ form.nfrPerformance }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div :style="nfrStyle('#67C23A')">
                  <div style="font-weight:700;margin-bottom:4px">✅ 可用性</div>
                  <div style="font-size:13px;color:#606266">{{ form.nfrAvailability }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div :style="nfrStyle('#F56C6C')">
                  <div style="font-weight:700;margin-bottom:4px">🔒 安全</div>
                  <div style="font-size:13px;color:#606266">{{ form.nfrSecurity }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div :style="nfrStyle('#E6A23C')">
                  <div style="font-weight:700;margin-bottom:4px">📈 扩展性</div>
                  <div style="font-size:13px;color:#606266">{{ form.nfrScalability }}</div>
                </div>
              </el-col>
            </el-row>

            <el-divider content-position="left">📄 AI 综合报告</el-divider>
            <el-input v-if="form.reviewReport" :model-value="form.reviewReport" type="textarea" :rows="8"
                      readonly style="font-family:Consolas,monospace;font-size:12px" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="listOpen" title="📁 我的架构方案" width="900px">
      <el-table :data="list" @row-click="loadArch">
        <el-table-column prop="archNo" label="编号" width="160" />
        <el-table-column prop="title" label="方案标题" min-width="200" />
        <el-table-column label="架构模式" width="120">
          <template #default="{ row }"><dict-tag :options="biz_arch_mode" :value="row.archMode" /></template>
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
  listArch, getArch, addArch, updateArch, aiRecommendArch, parseTimeline,
  type Arch
} from '@/api/business/arch'

const { proxy } = getCurrentInstance() as ComponentInternalInstance
const { biz_arch_mode, biz_arch_tech, biz_arch_db, biz_arch_ai, biz_arch_deploy, biz_arch_iot } =
  (proxy as any).useDict('biz_arch_mode', 'biz_arch_tech', 'biz_arch_db', 'biz_arch_ai', 'biz_arch_deploy', 'biz_arch_iot')

const aiLoading = ref(false)
const listOpen = ref(false)
const list = ref<Arch[]>([])

const initForm = (): Arch => ({
  projectId: 1,
  title: '',
  authorUserId: (proxy as any).$store?.state?.user?.id || 1,
  status: '00'
})
const form = ref<Arch>(initForm())

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'blur' }],
  title: [{ required: true, message: '方案标题必填', trigger: 'blur' }]
}

const timeline = computed(() => parseTimeline(form.value))

function nfrStyle(color: string) {
  return `padding:12px;border-radius:8px;margin-bottom:8px;background:${color}10;border-left:3px solid ${color};`
}

function newArch() { form.value = initForm() }
async function openList() {
  const res: any = await listArch({ pageSize: 100 })
  list.value = res.rows
  listOpen.value = true
}
async function loadArch(row: Arch) {
  if (!row.archId) return
  const res: any = await getArch(row.archId)
  form.value = res.data
  listOpen.value = false
}

async function handleAi() {
  if (!form.value.archId) {
    await addArch(form.value)
    const r: any = await listArch({ title: form.value.title, pageSize: 5 })
    const fresh = r.rows.find((x: Arch) => x.title === form.value.title)
    if (fresh) form.value.archId = fresh.archId
  } else {
    await updateArch(form.value)
  }
  if (!form.value.archId) return
  aiLoading.value = true
  try {
    const ai: any = await aiRecommendArch(form.value.archId)
    if (ai.code === 200) {
      form.value = ai.data
      ;(proxy as any).$modal.msgSuccess('架构方案推荐完成')
    }
  } finally { aiLoading.value = false }
}
</script>

<style scoped>
.arch-workspace .ph { display: flex; align-items: center; justify-content: space-between; }
.arch-workspace .pt { font-size: 18px; font-weight: 700; }
.arch-workspace .ps { font-size: 13px; color: #909399; margin-top: 4px; }
</style>
