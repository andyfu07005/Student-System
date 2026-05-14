import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
    },
    {
      path: '/',
      redirect: '/users',
    },
    {
      path: '/users',
      name: 'Users',
      component: () => import('@/views/UserList.vue'),
    },
    {
      path: '/users/:id',
      name: 'UserDetail',
      component: () => import('@/views/UserDetail.vue'),
    },
    {
      path: '/enrollment-changes',
      name: 'EnrollmentChangeList',
      component: () => import('@/views/EnrollmentChangeList.vue'),
    },
    {
      path: '/enrollment-changes/timeline/:studentId',
      name: 'EnrollmentChangeTimeline',
      component: () => import('@/views/EnrollmentChangeTimeline.vue'),
    },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.name !== 'Login' && !token) {
    return { name: 'Login' }
  }
})

export default router
