<template>
  <ion-page>
    <ion-header class="page-header">
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/tabs/home" />
        </ion-buttons>
        <ion-title>合理用药</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="page-content">
      <div class="container">
        <section class="intro-card">
          <h2>用药知识导航</h2>
          <p>按疾病或药物主题快速查看核心用法、风险提示和联合用药注意事项。</p>
        </section>

        <ion-searchbar
          v-model="searchText"
          placeholder="搜索分类、主题或药品"
          class="search-bar"
          show-clear-button="focus"
        />

        <section v-if="filteredCategories.length" class="category-grid">
          <article
            v-for="category in filteredCategories"
            :key="category.id"
            class="category-card"
            :data-testid="`rational-category-${category.id}`"
            @click="openCategory(category.id)"
          >
            <div class="category-head">
              <h3>{{ category.name }}</h3>
              <ion-badge>{{ category.topics.length }} 个主题</ion-badge>
            </div>
            <p>{{ category.description }}</p>
            <div class="tag-row">
              <ion-chip v-for="tag in category.tags" :key="`${category.id}-${tag}`" size="small">{{ tag }}</ion-chip>
            </div>
          </article>
        </section>

        <ion-card v-else class="empty-card">
          <ion-card-content>
            <h3>没有匹配内容</h3>
            <p>请尝试更换关键词，或直接浏览全部分类。</p>
            <ion-button fill="outline" @click="resetSearch">重置搜索</ion-button>
          </ion-card-content>
        </ion-card>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  IonBackButton,
  IonBadge,
  IonButton,
  IonButtons,
  IonCard,
  IonCardContent,
  IonChip,
  IonContent,
  IonHeader,
  IonPage,
  IonSearchbar,
  IonTitle,
  IonToolbar
} from '@ionic/vue'
import { rationalMedicationData } from '@/modules/patient/data/rationalMedication'

const router = useRouter()
const searchText = ref('')

const filteredCategories = computed(() => {
  return rationalMedicationData.searchCategories(searchText.value)
})

const openCategory = (categoryId: string) => {
  router.push(`/drug-category/${categoryId}`)
}

const resetSearch = () => {
  searchText.value = ''
}
</script>

<style scoped>
.page-header {
  --background: #2979ff;
  --color: #ffffff;
}

.page-content {
  --background: linear-gradient(180deg, #eff6ff 0%, #ffffff 36%, #f8fafc 100%);
}

.container {
  padding: 16px;
}

.intro-card {
  padding: 18px;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.08);
  margin-bottom: 14px;
}

.intro-card h2 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #0f172a;
}

.intro-card p {
  margin: 0;
  color: #475569;
  line-height: 1.6;
}

.search-bar {
  --background: #ffffff;
  --border-radius: 12px;
  --box-shadow: 0 2px 10px rgba(15, 23, 42, 0.08);
  margin-bottom: 16px;
}

.category-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.category-card {
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #dbeafe;
  padding: 14px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
}

.category-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.category-card h3 {
  margin: 0;
  color: #1e293b;
  font-size: 18px;
}

.category-card p {
  margin: 0 0 10px;
  color: #64748b;
  line-height: 1.5;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

ion-chip {
  --background: #dbeafe;
  --color: #1d4ed8;
  height: 28px;
}

.empty-card {
  border-radius: 14px;
  text-align: center;
}

.empty-card h3 {
  color: #0f172a;
}

.empty-card p {
  color: #64748b;
}
</style>
