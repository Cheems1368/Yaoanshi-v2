<template>
  <ion-page>
    <ion-tabs>
      <ion-router-outlet></ion-router-outlet>
      <ion-tab-bar slot="bottom">
        <ion-tab-button tab="home" href="/tabs/home">
          <ion-icon aria-hidden="true" :icon="home" />
          <ion-label>首页</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="help-order" href="/tabs/help-order">
          <ion-icon aria-hidden="true" :icon="helpCircle" />
          <ion-label>用药求助</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="prescription" href="/tabs/prescription">
          <ion-icon aria-hidden="true" :icon="documentText" />
          <ion-label>我的处方</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="profile" href="/tabs/profile">
          <ion-icon aria-hidden="true" :icon="person" />
          <ion-label>个人资料</ion-label>
        </ion-tab-button>
      </ion-tab-bar>
    </ion-tabs>
  </ion-page>
</template>

<script setup lang="ts">
import { IonTabBar, IonTabButton, IonTabs, IonLabel, IonIcon, IonPage, IonRouterOutlet } from '@ionic/vue';
import { home, helpCircle, documentText, person } from 'ionicons/icons';
</script>
