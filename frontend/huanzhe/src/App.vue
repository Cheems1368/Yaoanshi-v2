<template>
  <ion-app>
    <ion-router-outlet />
    <ion-toast
      :is-open="offlineConflictToastOpen"
      :message="offlineConflictMessage"
      :duration="4200"
      color="warning"
      @did-dismiss="offlineConflictToastOpen = false"
    />
  </ion-app>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { IonApp, IonRouterOutlet, IonToast } from '@ionic/vue';
import {
  consumeOfflineCheckinConflictCount,
  consumeOfflineCheckinCancelledCount,
  OFFLINE_CHECKIN_CONFLICT_EVENT
} from '@/modules/patient/services/offlineMedication'

const offlineConflictToastOpen = ref(false)
const offlineConflictMessage = ref('')

const showOfflineConflict = () => {
  const count = consumeOfflineCheckinConflictCount()
  if (count < 1) return
  const cancelled = consumeOfflineCheckinCancelledCount()
  offlineConflictMessage.value = cancelled > 0
    ? `${cancelled} 条离线打卡因原计划剂次已取消而未生效；其他记录已继续处理。`
    : `${count} 条离线打卡已超过可提交或修改时限，未能同步；其他记录已继续处理。`
  offlineConflictToastOpen.value = true
}

onMounted(() => {
  window.addEventListener(OFFLINE_CHECKIN_CONFLICT_EVENT, showOfflineConflict)
  showOfflineConflict()
})

onBeforeUnmount(() => window.removeEventListener(OFFLINE_CHECKIN_CONFLICT_EVENT, showOfflineConflict))
</script>
