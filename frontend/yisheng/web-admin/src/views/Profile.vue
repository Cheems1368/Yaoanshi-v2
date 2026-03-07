<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h2>个人资料</h2>
        <p>保留医生端 MVP 所需的账号信息与默认入口说明。</p>
      </div>
    </header>

    <div class="profile-card">
      <div class="profile-top">
        <el-avatar :size="64" :src="userStore.userInfo.avatar">
          <el-icon><UserFilled /></el-icon>
        </el-avatar>
        <div>
          <h3>{{ userStore.userInfo.name || '医生账号' }}</h3>
          <p>{{ userStore.userInfo.username || '未获取到用户名' }}</p>
        </div>
      </div>

      <dl class="profile-grid">
        <div>
          <dt>当前角色</dt>
          <dd>{{ userStore.userRole || 'doctor' }}</dd>
        </div>
        <div>
          <dt>默认入口</dt>
          <dd>/workorders</dd>
        </div>
        <div>
          <dt>菜单范围</dt>
          <dd>升级工单、个人资料</dd>
        </div>
      </dl>
    </div>
  </section>
</template>

<script setup>
import { UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
</script>

<style lang="scss" scoped>
.page-shell {
  display: grid;
  gap: 24px;
}

.page-header h2,
.profile-top h3 {
  margin: 0;
  color: var(--pro-text-primary);
}

.page-header p,
.profile-top p,
.profile-grid dt {
  margin: 0;
  color: var(--pro-text-secondary);
}

.profile-card {
  padding: 24px;
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-lg);
  background: var(--pro-bg-primary);
  box-shadow: var(--pro-shadow-sm);
}

.profile-top {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin: 0;
}

.profile-grid dd {
  margin: 6px 0 0;
  color: var(--pro-text-primary);
}

@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
