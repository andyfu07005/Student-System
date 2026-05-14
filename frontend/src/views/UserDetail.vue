<template>
  <div class="page-container">
    <el-page-header @back="$router.push('/users')" content="用户详情" />
    <el-card v-loading="loading" style="margin-top: 16px">
      <el-descriptions v-if="user" :column="2" border>
        <el-descriptions-item label="ID">{{ user.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ user.realName }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ user.email }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ user.phone }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag>{{ roleLabel(user.roleCode) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="user.status === 1 ? 'success' : 'danger'">
            {{ user.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ user.lastLogin }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ user.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getUser, type User } from '../api/user'

const route = useRoute()
const user = ref<User | null>(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getUser(Number(route.params.id))
    user.value = res.data.data
  } finally {
    loading.value = false
  }
})

function roleLabel(code: string) {
  const map: Record<string, string> = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
  return map[code] || code
}
</script>

<style scoped>
.page-container {
  padding: 16px;
  max-width: 800px;
}
</style>
