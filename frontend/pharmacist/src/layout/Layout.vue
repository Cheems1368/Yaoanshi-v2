<template>
  <div class="layout-container">
    <aside :class="['sidebar', { collapsed: isCollapse }]">
      <div class="brand">
        <div class="brand-mark">药</div>
        <div v-if="!isCollapse" class="brand-copy">
          <strong>药师工作台</strong>
          <span>患者管理、处方审核与工单协作入口</span>
        </div>
      </div>

      <el-menu :default-active="activeMenu" router :collapse="isCollapse" class="sidebar-menu">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="workspace">
      <header class="header">
        <div class="header-copy">
          <el-button text class="collapse-btn" @click="isCollapse = !isCollapse">
            <el-icon><Expand v-if="isCollapse" /><Fold v-else /></el-icon>
          </el-button>
          <div>
            <h1>{{ currentPageTitle }}</h1>
            <p>保持登录走 legacy，患者资料维护与 ABCD2 快照由 backend-v2 承载。</p>
          </div>
        </div>

        <el-dropdown @command="handleCommand">
          <div class="user-entry">
            <el-avatar :size="34" :src="userStore.userInfo.avatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <div class="user-meta">
              <strong>{{ userStore.userInfo.name || '药师' }}</strong>
              <span>{{ userStore.userRole || 'pharmacist' }}</span>
            </div>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  Document,
  Expand,
  Fold,
  HomeFilled,
  List,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const menuItems = [
  { path: '/dashboard', title: '工作台', icon: HomeFilled },
  { path: '/patient/list', title: '患者管理', icon: User },
  { path: '/prescriptions', title: '处方审核', icon: Document },
  { path: '/workorders', title: '工单处理', icon: List },
  { path: '/profile', title: '个人资料', icon: UserFilled }
]

const activeMenu = computed(() => {
  if (route.path.startsWith('/patient/')) {
    return '/patient/list'
  }
  const current = menuItems.find((item) => route.path.startsWith(item.path))
  return current?.path || route.path
})

const currentPageTitle = computed(() => {
  if (route.path.startsWith('/patient/info')) {
    return '患者详情'
  }
  const current = menuItems.find((item) => route.path.startsWith(item.path))
  return current?.title || '药师工作台'
})

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
    return
  }

  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确认退出当前登录状态吗？', '退出登录', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userStore.logoutAction()
      router.push('/login')
    } catch {
      return
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px 1fr;
  background: var(--pro-bg-secondary);
}

.sidebar {
  border-right: 1px solid var(--pro-border-color);
  background: linear-gradient(180deg, #ffffff 0%, #f5f7fa 100%);
}

.brand {
  height: 72px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid var(--pro-border-color);
}

.brand-mark {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: var(--pro-brand-primary);
  color: #ffffff;
  font-family: var(--pro-title-font-family);
  font-weight: 700;
  box-shadow: var(--pro-shadow-sm);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-copy strong {
  font-size: 16px;
  color: var(--pro-text-primary);
}

.brand-copy span {
  font-size: 12px;
  color: var(--pro-text-secondary);
}

.sidebar-menu {
  border-right: 0;
  padding: 16px 12px;
  background: transparent;
}

.sidebar-menu :deep(.el-menu-item) {
  margin-bottom: 8px;
  border-radius: 12px;
  color: var(--pro-text-secondary);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  color: var(--pro-brand-primary);
  background: var(--pro-brand-secondary);
}

.workspace {
  min-width: 0;
}

.header {
  height: 72px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--pro-border-color);
  background: rgba(255, 255, 255, 0.96);
}

.header-copy {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
}

.header-copy h1 {
  margin: 0;
  font-size: 20px;
  color: var(--pro-text-primary);
  font-family: var(--pro-title-font-family);
}

.header-copy p {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--pro-text-secondary);
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 14px;
  cursor: pointer;
}

.user-entry:hover {
  background: var(--pro-brand-secondary);
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.user-meta strong {
  font-size: 14px;
  color: var(--pro-text-primary);
}

.user-meta span {
  font-size: 12px;
  color: var(--pro-text-secondary);
  text-transform: uppercase;
}

.main-content {
  padding: 24px;
}

@media (max-width: 1024px) {
  .layout-container {
    grid-template-columns: 72px 1fr;
  }
}

@media (max-width: 768px) {
  .header,
  .main-content {
    padding-left: 16px;
    padding-right: 16px;
  }

  .header-copy p,
  .user-meta span {
    display: none;
  }
}
</style>
