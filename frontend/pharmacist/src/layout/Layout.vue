<template>
  <div :class="['layout-container', { 'sidebar-collapsed': isCollapse }]">
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
          <div class="header-copy-body">
            <h1>{{ currentPageTitle }}</h1>
            <p>保持登录走 legacy，患者资料维护与 ABCD2 快照由 backend-v2 承载。</p>
          </div>
        </div>

        <el-button text :aria-pressed="soundEnabled" @click="toggleSound">
          {{ soundEnabled ? '通知声音：开' : '通知声音：关' }}
        </el-button>
        <el-dropdown class="user-dropdown" @command="handleCommand">
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
  Reading,
  User,
  UserFilled,
  WarningFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { isNotificationSoundEnabled, setNotificationSoundEnabled } from '@/utils/adminNotifications'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const soundEnabled = ref(isNotificationSoundEnabled())
const toggleSound = () => {
  soundEnabled.value = !soundEnabled.value
  setNotificationSoundEnabled(soundEnabled.value)
}

const menuItems = [
  { path: '/dashboard', title: '工作台', icon: HomeFilled },
  { path: '/patient/list', title: '患者管理', icon: User },
  { path: '/prescriptions', title: '处方审核', icon: Document },
  { path: '/workorders', title: '工单处理', icon: List },
  { path: '/ddi/rules', title: 'DDI 规则管理', icon: WarningFilled },
  { path: '/education/content', title: '用药教育内容', icon: Reading },
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
  --sidebar-width: 240px;
  min-height: 100vh;
  min-width: 0;
  display: flex;
  background: var(--pro-bg-secondary);
}

.layout-container.sidebar-collapsed {
  --sidebar-width: 88px;
}

.sidebar {
  width: var(--sidebar-width);
  min-width: var(--sidebar-width);
  flex: 0 0 var(--sidebar-width);
  border-right: 1px solid var(--pro-border-color);
  background: linear-gradient(180deg, var(--pro-bg-primary) 0%, var(--pro-bg-secondary) 100%);
  overflow: hidden;
  transition:
    width 160ms ease,
    min-width 160ms ease,
    flex-basis 160ms ease;
}

.brand {
  height: 72px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid var(--pro-border-color);
}

.sidebar.collapsed .brand {
  justify-content: center;
  padding: 0 12px;
}

.brand-mark {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: var(--pro-brand-primary);
  color: var(--pro-text-inverse);
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

.sidebar.collapsed .sidebar-menu {
  padding-left: 8px;
  padding-right: 8px;
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
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
}

.header {
  min-width: 0;
  min-height: 72px;
  padding: 0 24px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid var(--pro-border-color);
  background: rgba(255, 255, 255, 0.96);
}

.header-copy {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-copy-body {
  min-width: 0;
  flex: 1 1 auto;
}

.collapse-btn {
  flex: 0 0 auto;
  width: 40px;
  height: 40px;
  border-radius: 12px;
}

.header-copy h1 {
  margin: 0;
  font-size: 20px;
  color: var(--pro-text-primary);
  font-family: var(--pro-title-font-family);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-copy p {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--pro-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-dropdown {
  min-width: 0;
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  max-width: 240px;
  padding: 8px 12px;
  border-radius: 14px;
  cursor: pointer;
}

.user-entry:hover {
  background: var(--pro-brand-secondary);
}

.user-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.user-meta strong {
  font-size: 14px;
  color: var(--pro-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-meta span {
  font-size: 12px;
  color: var(--pro-text-secondary);
  text-transform: uppercase;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-content {
  min-width: 0;
  flex: 1 1 auto;
  padding: 24px;
  overflow-y: auto;
  overflow-x: hidden;
}

@media (max-width: 1024px) {
  .layout-container {
    --sidebar-width: 72px;
  }
}

@media (max-width: 768px) {
  .header {
    grid-template-columns: 1fr;
    align-items: start;
    padding-top: 12px;
    padding-bottom: 12px;
  }

  .header,
  .main-content {
    padding-left: 16px;
    padding-right: 16px;
  }

  .header-copy p,
  .user-meta span {
    display: none;
  }

  .user-entry {
    max-width: none;
  }
}
</style>
