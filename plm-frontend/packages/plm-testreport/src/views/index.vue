<template>
  <div class="app-container">
    <el-result icon="info" title="测试报告" sub-title="生成器输出, views 待补充业务交互">
      <template #extra><el-button type="primary" @click="$router.back()">返回</el-button></template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'TestReport' })
</script>
