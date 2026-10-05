<template>
  <router-view />
</template>

<script setup>
import { onBeforeUnmount, onMounted, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { createAdminNotificationClient } from '@/utils/adminNotifications'

const userStore = useUserStore()
const notificationClient = createAdminNotificationClient(() => userStore.token)

const notificationIdentity = () => {
  if (!userStore.token || !userStore.userInfo.id) return ''
  try {
    const payload = JSON.parse(atob(userStore.token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    return `${payload.tenantId}:${payload.userId}:${payload.effectiveRole || userStore.userRole}`
  } catch {
    return `${userStore.userInfo.id}:${userStore.userRole}:${[...userStore.userInfo.roles].sort().join(',')}`
  }
}

onMounted(() => {
  userStore.initUserInfo()
  notificationClient.connect(notificationIdentity())
})

watch(
  notificationIdentity,
  (identity) => {
    if (identity) {
      notificationClient.connect(identity)
      return
    }
    notificationClient.disconnect()
  }
)

onBeforeUnmount(() => {
  notificationClient.disconnect()
})
</script>
