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
      component: () => import('@/components/AppLayout.vue'),
      redirect: '/students',
      children: [
        {
          path: 'students',
          name: 'StudentList',
          component: () => import('@/views/StudentList.vue'),
        },
        {
          path: 'classes',
          name: 'ClassList',
          component: () => import('@/views/ClassList.vue'),
        },
        {
          path: 'courses',
          name: 'CourseList',
          component: () => import('@/views/CourseList.vue'),
        },
        {
          path: 'enrollment-changes',
          name: 'EnrollmentChangeList',
          component: () => import('@/views/EnrollmentChangeList.vue'),
        },
        {
          path: 'enrollment-changes/timeline/:studentId',
          name: 'EnrollmentChangeTimeline',
          component: () => import('@/views/EnrollmentChangeTimeline.vue'),
        },
        {
          path: 'users',
          name: 'Users',
          component: () => import('@/views/UserList.vue'),
        },
        {
          path: 'users/:id',
          name: 'UserDetail',
          component: () => import('@/views/UserDetail.vue'),
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.name !== 'Login' && !token) {
    return { name: 'Login' }
  }
  if (to.name === 'Login' && token) {
    return '/'
  }
})

export default router
