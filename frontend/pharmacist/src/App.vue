<template>
  <router-view />
</template>

<script setup>
import { onBeforeUnmount, onMounted, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { createAdminNotificationClient } from '@/utils/adminNotifications'

const userStore = useUserStore()
const notificationClient = createAdminNotificationClient(() => userStore.token)

onMounted(() => {
  userStore.initUserInfo()
  if (userStore.token) {
    notificationClient.connect()
  }
})

watch(
  () => userStore.token,
  (token) => {
    if (token) {
      notificationClient.connect()
      return
    }
    notificationClient.disconnect()
  }
)

onBeforeUnmount(() => {
  notificationClient.disconnect()
})
</script>
