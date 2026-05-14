<template>
  <el-card class="health-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>服务健康状态</span>
        <el-button type="primary" size="small" @click="checkHealth" :loading="loading">
          刷新
        </el-button>
      </div>
    </template>
    <div v-if="healthData" class="health-info">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="状态">
          <el-tag :type="healthData.status === 'UP' ? 'success' : 'danger'">
            {{ healthData.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="服务名称">{{ healthData.service }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ healthData.timestamp }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getHealth, type HealthData } from '../api/health'

const healthData = ref<HealthData | null>(null)
const loading = ref(false)

async function checkHealth() {
  loading.value = true
  try {
    healthData.value = await getHealth()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  checkHealth()
})
</script>

<style scoped>
.health-card {
  max-width: 600px;
  margin: 40px auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.health-info {
  margin-top: 8px;
}
</style>
