import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false, title: '药师登录' }
  },
  {
    path: '/mobile/workorders',
    name: 'MobileWorkOrders',
    component: () => import('@/views/mobile/MobileWorkOrders.vue'),
    meta: { requiresAuth: true, title: '移动工单' }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        alias: ['/index'],
        name: 'Dashboard',
        component: () => import('@/views/PharmacistDashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'patient/list',
        alias: ['/patients', '/patient/list'],
        name: 'PatientList',
        component: () => import('@/views/patients/PatientList.vue'),
        meta: { title: '患者管理' }
      },
      {
        path: 'patient/info',
        alias: ['/patient/info'],
        name: 'PatientDetail',
        component: () => import('@/views/patients/PatientDetail.vue'),
        meta: { title: '患者详情' }
      },
      {
        path: 'prescriptions',
        alias: ['/prescription/prescriptions', '/prescription/prescriptionDetail'],
        name: 'Prescriptions',
        component: () => import('@/views/Prescriptions.vue'),
        meta: { title: '处方审核' }
      },
      {
        path: 'workorders',
        alias: ['/system/help-order', '/system/help-order/detail'],
        name: 'WorkOrders',
        component: () => import('@/views/workorders/WorkOrders.vue'),
        meta: { title: '工单处理' }
      },
      {
        path: 'ddi/rules',
        alias: ['/ddi/rules'],
        name: 'DdiRules',
        component: () => import('@/views/DdiRules.vue'),
        meta: { title: 'DDI 规则管理' }
      },
      {
        path: 'education/content',
        alias: ['/education/content'],
        name: 'EducationContent',
        component: () => import('@/views/EducationContent.vue'),
        meta: { title: '用药教育内容' }
      },
      {
        path: 'profile',
        alias: ['/user/profile'],
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
  await userStore.restoreSession()
  document.title = `${to.meta.title || '药师工作台'} - 药按时`

  if (userStore.isLoggedIn && !userStore.userInfo.id) {
    await userStore.initUserInfo()
  }

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
    return
  }

  if (to.path === '/login' && userStore.isLoggedIn) {
    next('/dashboard')
    return
  }

  next()
})

export default router
