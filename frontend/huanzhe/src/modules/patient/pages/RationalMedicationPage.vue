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
  --background: var(--patient-primary);
  --color: var(--patient-text-inverse);
}

.page-content {
  --background: linear-gradient(180deg, var(--patient-primary-light) 0%, var(--patient-bg-card) 36%, var(--patient-bg-page) 100%);
}

.container {
  padding: var(--patient-space-4);
}

.intro-card {
  padding: var(--patient-space-5);
  border-radius: var(--patient-radius-lg);
  background: var(--patient-bg-card);
  box-shadow: var(--patient-shadow-md);
  margin-bottom: var(--patient-space-4);
}

.intro-card h2 {
  margin: 0 0 var(--patient-space-2);
  font-size: var(--patient-font-subtitle);
  color: var(--patient-text-primary);
}

.intro-card p {
  margin: 0;
  color: var(--patient-text-secondary);
  line-height: var(--patient-leading-relaxed);
}

.search-bar {
  --background: var(--patient-bg-card);
  --border-radius: var(--patient-radius-md);
  --box-shadow: var(--patient-shadow-sm);
  margin-bottom: var(--patient-space-4);
}

.category-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--patient-space-3);
}

.category-card {
  background: var(--patient-bg-card);
  border-radius: var(--patient-radius-lg);
  border: 1px solid var(--patient-border-light);
  padding: var(--patient-space-4);
  box-shadow: var(--patient-shadow-sm);
}

.category-head {
  display: flex;
  justify-content: space-between;
  gap: var(--patient-space-3);
  margin-bottom: var(--patient-space-2);
}

.category-card h3 {
  margin: 0;
  color: var(--patient-text-primary);
  font-size: var(--patient-font-subtitle);
}

.category-card p {
  margin: 0 0 var(--patient-space-3);
  color: var(--patient-text-muted);
  line-height: var(--patient-leading-normal);
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--patient-space-2);
}

ion-chip {
  --background: var(--patient-primary-light);
  --color: var(--patient-primary);
  height: 28px;
}

.empty-card {
  border-radius: var(--patient-radius-lg);
  text-align: center;
}

.empty-card h3 {
  color: var(--patient-text-primary);
}

.empty-card p {
  color: var(--patient-text-muted);
}
</style>
