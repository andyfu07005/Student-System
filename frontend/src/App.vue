<template>
  <el-container class="layout-container">
    <el-header class="app-header">
      <h1>学生管理系统</h1>
    </el-header>
    <el-main>
      <HealthCheck />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import HealthCheck from './components/HealthCheck.vue'
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.layout-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.app-header {
  background-color: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.app-header h1 {
  margin: 0;
  font-size: 20px;
}
</style>
