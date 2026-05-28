<!--
  AiButton — 统一 AI 紫渐变按钮（P0028-P0-4）
  使用：
    <AiButton :loading="aiLoading" @click="aiAction">AI 分析优先级</AiButton>
    <AiButton plain :disabled="!current.id" @click="aiReview">AI 发布评审</AiButton>
  ✨ 图标自带，slot 内只放文字。
-->
<template>
  <el-button
    class="ai-button"
    :class="{ 'ai-button--plain': plain }"
    :loading="loading"
    :disabled="disabled"
    :size="size"
    @click="$emit('click', $event)"
  >
    <span class="ai-icon">✨</span>
    <slot />
  </el-button>
</template>

<script setup lang="ts">
defineProps<{
  loading?: boolean
  disabled?: boolean
  plain?: boolean
  size?: 'large' | 'default' | 'small'
}>()
defineEmits<{ (e: 'click', ev: MouseEvent): void }>()
</script>

<style scoped>
.ai-button {
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  color: #fff;
  border: none;
  transition: opacity 0.2s ease;
}
.ai-button:hover:not(:disabled) {
  opacity: 0.88;
  color: #fff;
}
.ai-button:active:not(:disabled) {
  opacity: 0.95;
}
.ai-button:disabled {
  opacity: 0.5;
  color: #fff;
}
.ai-button--plain {
  background: #ffffff;
  color: #6366f1;
  border: 1px solid #6366f1;
}
.ai-button--plain:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.08) 0%, rgba(99, 102, 241, 0.08) 100%);
  color: #6366f1;
}
.ai-icon {
  margin-right: 4px;
  display: inline-block;
}
</style>
