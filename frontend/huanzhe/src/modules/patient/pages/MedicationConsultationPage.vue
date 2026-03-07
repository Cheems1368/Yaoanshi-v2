<template>
  <ion-page>
    <ion-header class="chat-header">
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/tabs/home"></ion-back-button>
        </ion-buttons>
        <div class="header-info">
          <div class="doctor-avatar">
            <span class="avatar-text">{{ currentDoctor.name.charAt(0) }}</span>
          </div>
          <div class="doctor-details">
            <h2 class="doctor-name">{{ currentDoctor.name }}</h2>
            <p class="doctor-title">{{ currentDoctor.title }}</p>
          </div>
        </div>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="showDoctorInfo">
            <ion-icon :icon="informationCircleOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
      <div class="status-bar">
        <span class="status-indicator" :class="currentDoctor.status">
          {{ currentDoctor.statusText }}
        </span>
        <span class="consultation-status">{{ consultationStatusText }}</span>
      </div>
    </ion-header>

    <ion-content class="chat-content" ref="chatContent">
      <div class="chat-container">
        <div v-if="consultationStatus === 'waiting'" class="waiting-section">
          <div class="waiting-animation">
            <ion-spinner name="crescent"></ion-spinner>
          </div>
          <h3>正在为您匹配药师/医生</h3>
          <p>请稍候，我们将为您安排专业的药师</p>
        </div>

        <div v-else class="messages-container">
          <div v-for="message in messages" :key="message.id" class="message-item">
            <div v-if="message.sender === 'doctor'" class="message-doctor-wrapper">
              <div class="doctor-avatar-small">
                <span class="avatar-text">{{ currentDoctor.name.charAt(0) }}</span>
              </div>
              <div class="message-content">
                <div class="message-bubble doctor-bubble">
                  <div class="message-text">{{ message.content }}</div>
                </div>
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              </div>
            </div>

            <div v-else class="message-patient-wrapper">
              <div class="message-content">
                <div class="message-bubble patient-bubble">
                  <div class="message-text">{{ message.content }}</div>
                </div>
                <div class="message-status">
                  <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                  <ion-icon v-if="message.status === 'sent'" :icon="checkmarkOutline" class="status-icon sent"></ion-icon>
                  <ion-icon v-else-if="message.status === 'delivered'" :icon="checkmarkDoneOutline" class="status-icon delivered"></ion-icon>
                  <ion-icon v-else-if="message.status === 'read'" :icon="checkmarkDoneOutline" class="status-icon read"></ion-icon>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="showQuickReplies && consultationStatus === 'active'" class="quick-replies">
          <div class="quick-reply-item" v-for="reply in quickReplies" :key="reply.id" @click="sendQuickReply(reply.text)">
            {{ reply.text }}
          </div>
        </div>
      </div>
    </ion-content>

    <div class="input-container" v-if="consultationStatus === 'active'">
      <div class="input-toolbar">
        <ion-button fill="clear" class="attach-btn" @click="showAttachmentOptions">
          <ion-icon :icon="addOutline"></ion-icon>
        </ion-button>
        <div class="input-wrapper">
          <ion-textarea 
            v-model="inputMessage" 
            placeholder="输入消息..."
            :rows="1"
            :auto-grow="true"
            class="message-input"
            @keydown.enter.prevent="sendMessage"
          ></ion-textarea>
        </div>
        <ion-button 
          fill="clear" 
          class="send-btn" 
          :disabled="!inputMessage.trim()"
          @click="sendMessage"
        >
          <ion-icon :icon="sendOutline"></ion-icon>
        </ion-button>
      </div>
    </div>

    <ion-popover :is-open="showAttachmentPopover" @did-dismiss="showAttachmentPopover = false">
      <ion-content>
        <ion-list>
          <ion-item button @click="selectImage">
            <ion-icon :icon="imageOutline" slot="start"></ion-icon>
            <ion-label>图片</ion-label>
          </ion-item>
          <ion-item button @click="selectPrescription">
            <ion-icon :icon="documentTextOutline" slot="start"></ion-icon>
            <ion-label>处方</ion-label>
          </ion-item>
        </ion-list>
      </ion-content>
    </ion-popover>
  </ion-page>
</template>
<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { 
  IonPage, 
  IonHeader, 
  IonToolbar, 
  IonContent,
  IonButton,
  IonButtons,
  IonBackButton,
  IonIcon,
  IonTextarea,
  IonSpinner,
  IonPopover,
  IonList,
  IonItem,
  IonLabel,
  toastController
} from '@ionic/vue';
import { 
  informationCircleOutline,
  addOutline,
  sendOutline,
  checkmarkOutline,
  checkmarkDoneOutline,
  imageOutline,
  documentTextOutline
} from 'ionicons/icons';

const chatContent = ref();
const inputMessage = ref('');
const showAttachmentPopover = ref(false);
const showQuickReplies = ref(false);

const consultationStatus = ref<'waiting' | 'active' | 'ended'>('waiting');

const currentDoctor = ref({
  id: 1,
  name: '张药师',
  title: '执业药师',
  avatar: '',
  status: 'online',
  statusText: '在线'
});

const messages = ref([
  {
    id: 1,
    sender: 'doctor',
    type: 'text',
    content: '您好，我是专业的药师。药师能够为您提供专业的用药咨询服务。',
    timestamp: new Date(Date.now() - 300000),
    status: 'read'
  },
  {
    id: 2,
    sender: 'patient',
    type: 'text',
    content: '您好，我想咨询一下布洛芬的服用方法',
    timestamp: new Date(Date.now() - 240000),
    status: 'read'
  },
  {
    id: 3,
    sender: 'doctor',
    type: 'text',
    content: '布洛芬是常用的解热镇痛药物，成人每次200-400mg，每日3-4次，饭后服用。儿童用量为10-15mg/kg，每日3-4次。',
    timestamp: new Date(Date.now() - 180000),
    status: 'read'
  }
]);

const quickReplies = ref([
  { id: 1, text: '我的药什么时候吃？' },
  { id: 2, text: '这个药有什么副作用？' },
  { id: 3, text: '怀孕期间能吃药吗？' },
  { id: 4, text: '能跟其他药一起吃吗？' },
  { id: 5, text: '药品怎么保存？' }
]);

const consultationStatusText = computed(() => {
  switch (consultationStatus.value) {
    case 'waiting':
      return '等待匹配中...';
    case 'active':
      return '咨询进行中';
    case 'ended':
      return '咨询已结束';
    default:
      return '';
  }
});

const formatTime = (timestamp: Date) => {
  const now = new Date();
  const diff = now.getTime() - timestamp.getTime();
  const minutes = Math.floor(diff / 60000);
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return minutes + '分钟前';
  
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return hours + '小时前';
  
  return timestamp.toLocaleDateString();
};

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return;
  
  const newMessage = {
    id: Date.now(),
    sender: 'patient' as const,
    type: 'text' as const,
    content: inputMessage.value.trim(),
    timestamp: new Date(),
    status: 'sent' as const
  };
  
  messages.value.push(newMessage);
  inputMessage.value = '';
  
  await nextTick();
  scrollToBottom();
  
  setTimeout(() => {
    const messageIndex = messages.value.findIndex(m => m.id === newMessage.id);
    if (messageIndex !== -1) {
      messages.value[messageIndex].status = 'delivered';
    }
  }, 1000);
  
  setTimeout(() => {
    const messageIndex = messages.value.findIndex(m => m.id === newMessage.id);
    if (messageIndex !== -1) {
      messages.value[messageIndex].status = 'read';
    }
  }, 2000);
  
  setTimeout(() => {
    const doctorReply = {
      id: Date.now() + 1,
      sender: 'doctor' as const,
      type: 'text' as const,
      content: '收到您的消息，我正在为您查询相关信息，请稍候...',
      timestamp: new Date(),
      status: 'read' as const
    };
    messages.value.push(doctorReply);
    scrollToBottom();
  }, 3000);
};

const sendQuickReply = (text: string) => {
  inputMessage.value = text;
  sendMessage();
  showQuickReplies.value = false;
};

const scrollToBottom = () => {
  if (chatContent.value) {
    chatContent.value.scrollToBottom(300);
  }
};

const showAttachmentOptions = () => {
  showAttachmentPopover.value = true;
};

const selectImage = () => {
  showAttachmentPopover.value = false;
  const toast = toastController.create({
    message: '图片选择功能开发中...',
    duration: 2000,
    position: 'top'
  });
  toast.then(t => t.present());
};

const selectPrescription = () => {
  showAttachmentPopover.value = false;
  const toast = toastController.create({
    message: '处方选择功能开发中...',
    duration: 2000,
    position: 'top'
  });
  toast.then(t => t.present());
};

const showDoctorInfo = () => {
  const toast = toastController.create({
    message: '医生信息功能开发中...',
    duration: 2000,
    position: 'top'
  });
  toast.then(t => t.present());
};

onMounted(() => {
  setTimeout(() => {
    consultationStatus.value = 'active';
    showQuickReplies.value = true;
    scrollToBottom();
  }, 2000);
});

watch(messages, () => {
  nextTick(() => {
    scrollToBottom();
  });
}, { deep: true });
</script>
<style scoped>
.chat-header {
  --background: #2979FF;
  --color: white;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.doctor-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar-text {
  color: white;
  font-size: 16px;
  font-weight: bold;
}

.doctor-details {
  flex: 1;
}

.doctor-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: white;
}

.doctor-title {
  font-size: 12px;
  margin: 0;
  color: rgba(255, 255, 255, 0.8);
}

.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 12px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-indicator.online {
  color: #4CAF50;
}

.status-indicator.offline {
  color: #F44336;
}

.status-indicator::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.consultation-status {
  color: rgba(255, 255, 255, 0.8);
}

.chat-content {
  --background: #f5f5f5;
}

.chat-container {
  padding: 16px;
  min-height: 100%;
}

.waiting-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  text-align: center;
}

.waiting-animation {
  margin-bottom: 24px;
}

.waiting-section h3 {
  color: #333;
  margin-bottom: 8px;
}

.waiting-section p {
  color: #666;
  font-size: 14px;
}

.messages-container {
  padding-bottom: 20px;
}

.message-item {
  margin-bottom: 16px;
}

.message-doctor-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.doctor-avatar-small {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.doctor-avatar-small .avatar-text {
  color: #666;
  font-size: 14px;
  font-weight: bold;
}

.message-patient-wrapper {
  display: flex;
  justify-content: flex-end;
}

.message-content {
  max-width: 70%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  word-wrap: break-word;
  position: relative;
}

.doctor-bubble {
  background: #8A2BE2;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  color: #000000;
}

.patient-bubble {
  background: #8A2BE2;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-text {
  font-size: 16px;
  line-height: 1.4;
  color: #ffffff;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  text-align: left;
}

.message-patient-wrapper .message-time {
  text-align: right;
}

.message-status {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 4px;
}

.status-icon {
  font-size: 16px;
}

.status-icon.sent {
  color: #999;
}

.status-icon.delivered {
  color: #999;
}

.status-icon.read {
  color: #2979FF;
}

.quick-replies {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.quick-reply-item {
  padding: 8px 16px;
  background: #f0f0f0;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
  color: #000000;
}

.quick-reply-item:hover {
  background: #e0e0e0;
}

.input-container {
  background: white;
  border-top: 1px solid #e0e0e0;
  padding: 8px 16px;
  padding-bottom: calc(8px + env(safe-area-inset-bottom));
}

.input-toolbar {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.attach-btn,
.send-btn {
  --color: #2979FF;
  flex-shrink: 0;
}

.input-wrapper {
  flex: 1;
  min-height: 40px;
}

.message-input {
  --background: #f5f5f5;
  --border-radius: 20px;
  --padding-start: 16px;
  --padding-end: 16px;
  --padding-top: 8px;
  --padding-bottom: 8px;
  --color: #000000;
  border-radius: 20px;
  max-height: 120px;
}

@media (max-width: 768px) {
  .chat-container {
    padding: 12px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .quick-replies {
    padding: 12px;
  }
  
  .quick-reply-item {
    font-size: 13px;
    padding: 6px 12px;
  }
}
</style>
