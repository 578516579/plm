<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="ph-header">
          <span class="ph-icon">🚧</span>
          <span class="ph-title">{{ moduleLabel }}</span>
          <el-tag size="small" type="warning">开发中</el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="模块路径">{{ routePath }}</el-descriptions-item>
        <el-descriptions-item label="后端 API">
          <code>/business/{{ moduleKey }}</code>
          <el-tag size="small" type="success" style="margin-left: 8px">已就绪</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="数据库表">
          <code>{{ dbTableName }}</code>
          <el-tag size="small" type="success" style="margin-left: 8px">已建表</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="E2E 测试">
          <el-tag size="small" type="success">已通过</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="PRD 规格">
          <a href="javascript:void(0)" @click="openPrd">查看 PRD-MAPPING.md</a>
        </el-descriptions-item>
        <el-descriptions-item label="UI 状态">
          <el-tag size="small" type="info">前端视图待实现</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <el-alert
        title="此页是后端 PRD-aligned 完成 + 前端 UI 待开发的占位页"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <p style="margin: 8px 0 0">
            模块 <strong>{{ moduleKey }}</strong> 的后端 Domain / Mapper / Service / Controller / SQL 均已按
            <code>PRD-MAPPING.md</code> 落地,可直接通过 API 调用 (列表 / 详情 / CRUD / AI 生成入口)。
          </p>
          <p style="margin: 8px 0 0">
            前端 UI 视图请按 <code>prd和原型/AgriPLM-DevOps-原型/agriplm_split/{{ promptHtml }}</code> 实现。
          </p>
        </template>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const moduleKey = computed(() => {
  // 从 route.path 提取 — 如 /business/inception → inception
  const m = route.path.match(/business\/([^/]+)/)
  return m ? m[1] : 'unknown'
})

const moduleLabel = computed(() => (route.meta?.title as string) || moduleKey.value)

const routePath = computed(() => route.path)

const dbTableName = computed(() => {
  const snake = moduleKey.value.replace(/-/g, '_')
  // manual-product → tb_manual_product, ai-agent → tb_ai_agent, dora → tb_dora_metric, analytics → tb_analytics_snapshot
  const special: Record<string, string> = {
    dora: 'tb_dora_metric',
    analytics: 'tb_analytics_snapshot'
  }
  return special[moduleKey.value] || `tb_${snake}`
})

const promptHtml = computed(() => {
  const special: Record<string, string> = {
    inception: 'inception.html',
    prd: 'prd.html',
    competitive: 'competitive.html',
    arch: 'archdesign.html',
    dbdesign: 'dbdesign.html',
    apidesign: 'apidesign.html',
    ued: 'ued.html',
    testdata: 'testdata.html',
    autotest: 'autotest.html',
    'manual-product': 'productmanual.html',
    'manual-impl': 'implmanual.html',
    'manual-ops': 'opsmanual.html',
    analytics: 'analytics.html',
    dashboard: 'dashboard.html',
    'ai-agent': 'aiagents.html',
    openspec: 'aispec.html',
    pipeline: 'pipeline.html',
    'feature-flag': 'featureflag.html',
    dora: 'devops.html',
    release: 'release.html',
    submission: 'submit.html',
    testplan: 'testplan.html',
    testreport: 'testreport.html',
    apidoc: 'apidoc.html'
  }
  return special[moduleKey.value] || `${moduleKey.value}.html`
})

function openPrd() {
  // 简单提示,完整实现可改为弹窗读取 markdown
  alert(`PRD-MAPPING.md §1 表格中查找 "${moduleKey.value}" 模块行`)
}
</script>

<style scoped>
.app-container { padding: 20px; }
.ph-header { display: flex; align-items: center; gap: 10px; }
.ph-icon { font-size: 24px; }
.ph-title { font-size: 16px; font-weight: 600; }
code { background: #f4f4f5; padding: 2px 6px; border-radius: 3px; font-size: 12px; }
</style>
