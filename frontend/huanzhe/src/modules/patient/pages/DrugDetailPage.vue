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
      <div class="container" v-if="item">
        <ion-card class="hero-card">
          <ion-card-content>
            <p class="topic">{{ item.categoryName }} / {{ item.topicName }}</p>
            <h2>{{ item.name }}</h2>
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
  --background: #2979ff;
  --color: #ffffff;
}

.page-content {
  --background: linear-gradient(180deg, #eff6ff 0%, #ffffff 35%, #f8fafc 100%);
}

.container {
  padding: 16px;
}

.hero-card {
  border-radius: 14px;
  margin-bottom: 12px;
  border: 1px solid #dbeafe;
}

.topic {
  margin: 0;
  color: #2563eb;
  font-size: 13px;
}

.hero-card h2 {
  margin: 8px 0 4px;
  color: #0f172a;
}

.generic-name {
  margin: 0;
  color: #64748b;
}

.detail-card {
  border-radius: 14px;
  margin-bottom: 10px;
}

.detail-card ion-card-title {
  color: #1e293b;
  font-size: 17px;
}

.detail-card ion-card-content {
  color: #334155;
  line-height: 1.6;
}

.warning ion-card-title {
  color: #b45309;
}

.safe-note {
  margin-top: 12px;
  border-radius: 14px;
  border: 1px solid #fed7aa;
  background: #fff7ed;
  padding: 14px;
}

.safe-note h3 {
  margin: 0 0 8px;
  color: #9a3412;
}

.safe-note p {
  margin: 0;
  color: #7c2d12;
  line-height: 1.6;
}

.error-card {
  border-radius: 14px;
  text-align: center;
}

.error-card p {
  color: #64748b;
}
</style>
