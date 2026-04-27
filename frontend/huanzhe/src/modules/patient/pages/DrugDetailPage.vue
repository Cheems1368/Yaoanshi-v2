<template>
  <ion-page>
    <ion-header class="page-header">
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button :default-href="backHref" />
        </ion-buttons>
        <ion-title>药品详情</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="page-content">
      <div class="container" v-if="item" data-testid="rational-detail-page">
        <ion-card class="hero-card">
          <ion-card-content>
            <p class="topic">{{ item.categoryName }} / {{ item.topicName }}</p>
            <h2 data-testid="rational-detail-title">{{ item.name }}</h2>
            <p class="generic-name">通用名：{{ item.genericName }}</p>
          </ion-card-content>
        </ion-card>

        <ion-card class="detail-card">
          <ion-card-header>
            <ion-card-title>适应症</ion-card-title>
          </ion-card-header>
          <ion-card-content>{{ item.indications }}</ion-card-content>
        </ion-card>

        <ion-card class="detail-card">
          <ion-card-header>
            <ion-card-title>用法用量</ion-card-title>
          </ion-card-header>
          <ion-card-content>{{ item.dosage }}</ion-card-content>
        </ion-card>

        <ion-card class="detail-card warning">
          <ion-card-header>
            <ion-card-title>注意事项</ion-card-title>
          </ion-card-header>
          <ion-card-content>{{ item.precautions }}</ion-card-content>
        </ion-card>

        <ion-card class="detail-card">
          <ion-card-header>
            <ion-card-title>常见不良反应</ion-card-title>
          </ion-card-header>
          <ion-card-content>{{ item.sideEffects }}</ion-card-content>
        </ion-card>

        <ion-card class="detail-card">
          <ion-card-header>
            <ion-card-title>联合用药提醒</ion-card-title>
          </ion-card-header>
          <ion-card-content>{{ item.interactions }}</ion-card-content>
        </ion-card>

        <ion-card class="detail-card">
          <ion-card-header>
            <ion-card-title>储存方式</ion-card-title>
          </ion-card-header>
          <ion-card-content>{{ item.storage }}</ion-card-content>
        </ion-card>

        <section class="safe-note">
          <h3>安全提示</h3>
          <p>本页面为健康科普内容，不构成个体化诊疗建议。请结合医生/药师指导用药。</p>
        </section>
      </div>

      <div class="container" v-else>
        <ion-card class="error-card">
          <ion-card-content>
            <h3>未找到药品详情</h3>
            <p>可能是药品已下线或链接失效，请返回分类页重新选择。</p>
            <ion-button fill="outline" @click="goBack">返回上一页</ion-button>
          </ion-card-content>
        </ion-card>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  IonBackButton,
  IonButton,
  IonButtons,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardTitle,
  IonContent,
  IonHeader,
  IonPage,
  IonTitle,
  IonToolbar
} from '@ionic/vue'
import { rationalMedicationData } from '@/modules/patient/data/rationalMedication'

const route = useRoute()
const router = useRouter()

const itemId = computed(() => String(route.params.id || ''))
const categoryId = computed(() => String(route.query.categoryId || ''))
const item = computed(() => rationalMedicationData.getItemById(itemId.value))

const backHref = computed(() => {
  if (categoryId.value) {
    return `/drug-category/${categoryId.value}`
  }
  return '/rational-medication'
})

const goBack = () => {
  router.push(backHref.value)
}
</script>

<style scoped>
.page-header {
  --background: var(--patient-primary);
  --color: var(--patient-text-inverse);
}

.page-content {
  --background: linear-gradient(180deg, var(--patient-primary-light) 0%, var(--patient-bg-card) 35%, var(--patient-bg-page) 100%);
}

.container {
  padding: var(--patient-space-4);
}

.hero-card {
  border-radius: var(--patient-radius-lg);
  margin-bottom: var(--patient-space-3);
  border: 1px solid var(--patient-border-light);
}

.topic {
  margin: 0;
  color: var(--patient-primary);
  font-size: var(--patient-font-caption);
}

.hero-card h2 {
  margin: var(--patient-space-2) 0 var(--patient-space-1);
  color: var(--patient-text-primary);
}

.generic-name {
  margin: 0;
  color: var(--patient-text-muted);
}

.detail-card {
  border-radius: var(--patient-radius-lg);
  margin-bottom: var(--patient-space-3);
}

.detail-card ion-card-title {
  color: var(--patient-text-primary);
  font-size: var(--patient-font-body);
}

.detail-card ion-card-content {
  color: var(--patient-text-primary);
  line-height: var(--patient-leading-relaxed);
}

.warning ion-card-title {
  color: var(--patient-warning);
}

.safe-note {
  margin-top: var(--patient-space-3);
  border-radius: var(--patient-radius-lg);
  border: 1px solid rgba(245, 158, 11, 0.3);
  background: rgba(245, 158, 11, 0.08);
  padding: var(--patient-space-4);
}

.safe-note h3 {
  margin: 0 0 var(--patient-space-2);
  color: var(--patient-warning);
}

.safe-note p {
  margin: 0;
  color: var(--patient-text-primary);
  line-height: var(--patient-leading-relaxed);
}

.error-card {
  border-radius: var(--patient-radius-lg);
  text-align: center;
}

.error-card p {
  color: var(--patient-text-muted);
}
</style>
