<template>
  <ion-page>
    <ion-tabs>
      <ion-router-outlet></ion-router-outlet>
      <ion-tab-bar slot="bottom">
        <ion-tab-button tab="home" href="/tabs/home">
          <ion-icon aria-hidden="true" :icon="home" />
          <ion-label>首页</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="medication" href="/tabs/medication">
          <ion-icon aria-hidden="true" :icon="medkit" />
          <ion-label>用药</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="help-order" href="/tabs/help-order">
          <ion-icon aria-hidden="true" :icon="helpCircle" />
          <ion-label>求助</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="profile" href="/tabs/profile">
          <ion-icon aria-hidden="true" :icon="person" />
          <ion-label>我的</ion-label>
        </ion-tab-button>
      </ion-tab-bar>
    </ion-tabs>
  </ion-page>
</template>

<script setup lang="ts">
import { IonTabBar, IonTabButton, IonTabs, IonLabel, IonIcon, IonPage, IonRouterOutlet } from '@ionic/vue';
import { home, helpCircle, medkit, person } from 'ionicons/icons';
</script>

<style scoped>
ion-tab-button {
  font-size: var(--patient-font-caption);
  min-height: 56px;
}

ion-tab-button ion-label {
  font-size: var(--patient-font-caption);
  font-weight: var(--patient-font-medium);
}
</style>
