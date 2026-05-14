<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :span="24">
        <h2>欢迎使用 PLM 项目全生命周期管理系统</h2>
        <p>
          本系统正在建设中。请在左侧菜单进入对应业务模块开始使用。
        </p>
        <el-divider />
        <el-descriptions title="环境信息" :column="2" border>
          <el-descriptions-item label="系统名称">PLM</el-descriptions-item>
          <el-descriptions-item label="当前版本">v{{ version }}</el-descriptions-item>
          <el-descriptions-item label="技术栈（前端）">Vue 3 + Vite + Element Plus + TypeScript</el-descriptions-item>
          <el-descriptions-item label="技术栈（后端）">Spring Boot + MyBatis + JWT + Redis</el-descriptions-item>
        </el-descriptions>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
const version = ref<string>('0.1.0')
</script>

<style scoped lang="scss">
.home {
  blockquote {
    padding: 10px 20px;
    margin: 0 0 20px;
    font-size: 17.5px;
    border-left: 5px solid #eee;
  }
  h2 {
    margin-top: 0;
    font-size: 24px;
    line-height: 1.5;
  }
}
</style>
