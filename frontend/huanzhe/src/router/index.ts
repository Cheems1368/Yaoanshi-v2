import { createRouter, createWebHistory } from '@ionic/vue-router'
import type { RouteRecordRaw } from 'vue-router'
import TabsPage from '@/modules/app/pages/TabsPage.vue'
import { patientSession } from '@/app/session'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    component: () => import('@/modules/auth/pages/LoginPage.vue'),
    meta: { public: true }
  },
  {
    path: '/realname',
    component: () => import('@/modules/auth/pages/RealnamePage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/questionnaire',
    component: () => import('@/modules/auth/pages/QuestionnairePage.vue'),
    meta: { requiresAuth: true, requiresBinding: true }
  },
  {
    path: '/tabs/',
    component: TabsPage,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/tabs/home'
      },
      {
        path: 'home',
        component: () => import('@/modules/patient/pages/HomePage.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'medication',
        component: () => import('@/modules/patient/pages/MedicationTabPage.vue'),
        meta: { requiresAuth: true, requiresBinding: true }
      },
      {
        path: 'help-order',
        component: () => import('@/modules/patient/pages/HelpOrderPage.vue'),
        meta: { requiresAuth: true, requiresBinding: true }
      },
      {
        path: 'profile',
        component: () => import('@/modules/patient/pages/ProfilePage.vue'),
        meta: { requiresAuth: true }
      }
    ]
  },
  {
    path: '/tabs/prescription',
    redirect: '/prescription'
  },
  {
    path: '/prescription',
    component: () => import('@/modules/patient/pages/PrescriptionPage.vue'),
    meta: { requiresAuth: true, requiresBinding: true }
  },
  {
    path: '/medication-reminder',
    component: () => import('@/modules/patient/pages/MedicationReminderPage.vue'),
    meta: { requiresAuth: true, requiresBinding: true }
  },
  {
    path: '/help-order/:id',
    component: () => import('@/modules/patient/pages/HelpOrderDetailPage.vue'),
    meta: { requiresAuth: true, requiresBinding: true }
  },
  {
    path: '/medication-record',
    component: () => import('@/modules/patient/pages/MedicationRecordPage.vue'),
    meta: { requiresAuth: true, requiresBinding: true }
  },
  {
    path: '/rational-medication',
    component: () => import('@/modules/patient/pages/RationalMedicationPage.vue'),
    meta: { requiresAuth: true, requiresBinding: true }
  },
  {
    path: '/education/cards/:id',
    component: () => import('@/modules/patient/pages/EducationCardDetailPage.vue'),
    meta: { requiresAuth: true, requiresBinding: true }
  },
  {
    path: '/drug-detail/:id',
    component: () => import('@/modules/patient/pages/DrugDetailPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/drug-category/:id',
    component: () => import('@/modules/patient/pages/DrugCategoryPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/ai-assistant',
    component: () => import('@/modules/patient/pages/AiAssistantPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/self-reported-medications',
    component: () => import('@/modules/patient/pages/SelfReportedMedicationPage.vue'),
    meta: { requiresAuth: true, requiresBinding: true }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

const resolveAuthenticatedHome = () => {
  const gateState = patientSession.getGateState()
  if (!gateState.isBound) {
    return '/realname'
  }
  return '/tabs/home'
}

router.beforeEach((to, _from, next) => {
  const gateState = patientSession.getGateState()
  const isPublicRoute = Boolean(to.meta?.public)
  const requiresAuth = Boolean(to.meta?.requiresAuth)
  const requiresBinding = Boolean(to.meta?.requiresBinding)

  if (to.path === '/login' && gateState.loggedIn) {
    next(resolveAuthenticatedHome())
    return
  }

  if (isPublicRoute) {
    next()
    return
  }

  if (requiresAuth && !gateState.loggedIn) {
    next('/login')
    return
  }

  if (requiresBinding && !gateState.isBound) {
    next('/realname')
    return
  }

  next()
})

export default router
