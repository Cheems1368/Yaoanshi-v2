<template>
  <ion-modal :is-open="reminderState.active.length > 0" :backdrop-dismiss="false">
    <ion-header><ion-toolbar><ion-title>该服药了</ion-title></ion-toolbar></ion-header>
    <ion-content class="ion-padding">
      <p>按已确认的用药计划记录服药情况</p>
      <ion-card v-for="task in reminderState.active" :key="task.id">
        <ion-card-header><ion-card-title>{{ task.drugName }}</ion-card-title></ion-card-header>
        <ion-card-content>
          <p>{{ task.dosage }} · {{ beijingTime(task.scheduledAt!) }}（北京时间）</p>
          <p v-if="task.instructions">{{ task.instructions }}</p>
          <ion-button expand="block" color="success" :disabled="reminderState.busy" @click="recordTaken(task)">已服药</ion-button>
        </ion-card-content>
      </ion-card>
      <ion-button expand="block" fill="outline" @click="closeReminder">关闭</ion-button>
      <p>关闭提醒不会记录为已服药。</p>
    </ion-content>
  </ion-modal>
  <ion-modal :is-open="reminderState.settingsOpen" @did-dismiss="reminderState.settingsOpen = false">
    <ion-header><ion-toolbar><ion-title>用药提醒设置</ion-title>
      <ion-buttons slot="end"><ion-button @click="reminderState.settingsOpen = false">完成</ion-button></ion-buttons>
    </ion-toolbar></ion-header>
    <ion-content class="ion-padding">
      <ion-item><ion-toggle :checked="reminderState.options.sound" @ion-change="setReminderOption('sound', $event.detail.checked)">声音提醒</ion-toggle></ion-item>
      <ion-item><ion-toggle :checked="reminderState.options.vibration" @ion-change="setReminderOption('vibration', $event.detail.checked)">震动提醒</ion-toggle></ion-item>
      <p>{{ reminderState.capability }}</p>
      <ion-button expand="block" @click="enableReminders">启用提醒</ion-button>
      <p>联网后加载七天任务；原生离线缓存及队列仍采用既有48小时有效期。</p>
      <p role="status">{{ reminderState.message }}</p>

    </ion-content>
  </ion-modal>
</template>

<script setup lang="ts">
import { IonModal, IonHeader, IonToolbar, IonTitle, IonContent, IonCard, IonCardHeader,
  IonCardTitle, IonCardContent, IonButton, IonButtons, IonItem, IonToggle } from '@ionic/vue'
import { beijingTime } from './model'
import { reminderState, closeReminder, recordTaken, enableReminders, setReminderOption } from './service'
</script>
