import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false, title: '医生登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/workorders',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'workorders',
        alias: ['/doctor-dashboard'],
        name: 'WorkOrders',
        component: () => import('@/views/workorders/WorkOrders.vue'),
        meta: { title: '升级工单' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人资料' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  document.title = `${to.meta.title || '医生工作台'} - 医生工作台`

  if (userStore.isLoggedIn && !userStore.userInfo.id) {
    await userStore.initUserInfo()
  }

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
    return
  }

  if (to.path === '/login' && userStore.isLoggedIn) {
    next('/workorders')
    return
  }

  next()
})

export default router
