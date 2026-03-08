<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>数据同步测试</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content>
      <div class="container">
        <h2>用户数据服务测试</h2>
        
        <ion-card>
          <ion-card-header>
            <ion-card-title>当前用户数据</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <div class="info-item">
              <strong>姓名:</strong> {{ userProfile.name }}
            </div>
            <div class="info-item">
              <strong>手机号:</strong> {{ userProfile.phone }}
            </div>
            <div class="info-item">
              <strong>实名材料:</strong> {{ userProfile.identityMaterialArchived ? '已归档' : '未归档' }}
            </div>
            <div class="info-item">
              <strong>证件类型:</strong> {{ userProfile.idType }}
            </div>
            <div class="info-item">
              <strong>民族:</strong> {{ userProfile.ethnicity }}
            </div>
            <div class="info-item">
              <strong>实名认证状态:</strong> 
              <span :class="userProfile.isRealNameVerified ? 'verified' : 'not-verified'">
                {{ userProfile.isRealNameVerified ? '已认证' : '未认证' }}
              </span>
            </div>
          </ion-card-content>
        </ion-card>

        <ion-card>
          <ion-card-header>
            <ion-card-title>测试操作</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <ion-button expand="block" @click="testUpdate" class="test-button">
              测试更新数据
            </ion-button>
            <ion-button expand="block" @click="goToRealname" class="test-button">
              去实名认证页面
            </ion-button>
            <ion-button expand="block" @click="goToProfile" class="test-button">
              去个人资料页面
            </ion-button>
            <ion-button expand="block" @click="clearData" class="test-button">
              清除数据
            </ion-button>
          </ion-card-content>
        </ion-card>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonButton } from '@ionic/vue'
import { userDataService, type UserProfile } from '@/services/userDataService'

const router = useRouter()
const userProfile = ref<UserProfile>(userDataService.getUserProfile())

const testUpdate = () => {
  // 测试更新数据
  userDataService.updateRealNameInfo({
    realName: '测试用户',
    idType: '身份证',
    ethnicity: '汉族',
    mobile: '13812345678',
    identityMaterialArchived: true
  })
  
  // 更新本地显示
  userProfile.value = userDataService.getUserProfile()
  alert('测试数据已更新！')
}

const goToRealname = () => {
  router.push('/realname')
}

const goToProfile = () => {
  router.push('/tabs/profile')
}

const clearData = () => {
  userDataService.clearUserData()
  userProfile.value = userDataService.getUserProfile()
  alert('数据已清除！')
}

onMounted(() => {
  // 添加数据变化监听器
  userDataService.addListener((newProfile) => {
    userProfile.value = newProfile
  })
})
</script>

<style scoped>
.container {
  padding: 16px;
}

.info-item {
  margin-bottom: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.info-item:last-child {
  border-bottom: none;
}

.verified {
  color: #4CAF50;
  font-weight: bold;
}

.not-verified {
  color: #f44336;
  font-weight: bold;
}

.test-button {
  margin-bottom: 8px;
}
</style>
