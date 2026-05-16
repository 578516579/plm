<template>
  <div class="stub-container">
    <el-result icon="info" title="竞品情报" sub-title="本模块为空壳骨架 (stub),计划版本待启动。">
      <template #extra>
        <el-button type="primary" @click="$router.back()">返回</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'Competitive' })
</script>

<style scoped>
.stub-container {
  padding: 40px;
  display: flex;
  justify-content: center;
}
</style>
