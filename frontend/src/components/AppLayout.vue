<template>
  <el-container class="app-layout">
    <el-aside class="app-sidebar" width="220px">
      <div class="brand">学生管理系统</div>
      <el-menu
        class="nav-menu"
        :default-active="activeMenu"
        router
        background-color="#1f2937"
        text-color="#d1d5db"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/students">
          <el-icon><UserFilled /></el-icon>
          <span>学生管理</span>
        </el-menu-item>
        <el-menu-item index="/classes">
          <el-icon><School /></el-icon>
          <span>班级管理</span>
        </el-menu-item>
        <el-menu-item index="/courses">
          <el-icon><Collection /></el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/enrollment-changes">
          <el-icon><Switch /></el-icon>
          <span>学籍异动</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><Avatar /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="page-title">{{ pageTitle }}</div>
        <el-button :icon="SwitchButton" @click="logout">退出登录</el-button>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Avatar, Collection, School, Switch, SwitchButton, UserFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const menuTitles: Record<string, string> = {
  '/students': '学生管理',
  '/classes': '班级管理',
  '/courses': '课程管理',
  '/enrollment-changes': '学籍异动',
  '/users': '用户管理',
}

const activeMenu = computed(() => {
  const match = Object.keys(menuTitles).find((path) => route.path === path || route.path.startsWith(`${path}/`))
  return match ?? '/students'
})

const pageTitle = computed(() => menuTitles[activeMenu.value])

function logout() {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: #f5f7fb;
}

.app-sidebar {
  background: #1f2937;
}

.brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.nav-menu {
  border-right: none;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.app-main {
  padding: 20px;
  min-width: 0;
}

@media (max-width: 768px) {
  .app-layout {
    display: block;
  }

  .app-sidebar {
    width: 100% !important;
  }

  .brand {
    justify-content: center;
  }

  .app-header {
    padding: 0 12px;
  }

  .app-main {
    padding: 12px;
  }
}
</style>
