<template>
  <div class="voice-recorder">
    <ion-button 
      :fill="isRecording ? 'solid' : 'outline'"
      :color="isRecording ? 'danger' : 'primary'"
      @click="toggleRecording"
      class="voice-button"
    >
      <ion-icon :icon="isRecording ? stop : mic" slot="start"></ion-icon>
      {{ isRecording ? '停止录音' : '语音记录' }}
    </ion-button>
    
    <div v-if="audioUrl" class="audio-player">
      <audio :src="audioUrl" controls class="audio-control"></audio>
      <ion-button fill="clear" @click="removeAudio" class="remove-audio">
        <ion-icon :icon="trash" slot="icon-only"></ion-icon>
      </ion-button>
    </div>
    
    <div v-if="isRecording" class="recording-indicator">
      <div class="pulse"></div>
      <span>正在录音...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { IonButton, IonIcon } from '@ionic/vue';
import { mic, stop, trash } from 'ionicons/icons';

const emit = defineEmits<{
  audioRecorded: [audioUrl: string]
  audioRemoved: []
}>();

const isRecording = ref(false);
const audioUrl = ref('');
let mediaRecorder: MediaRecorder | null = null;
let audioChunks: Blob[] = [];

const toggleRecording = async () => {
  if (isRecording.value) {
    stopRecording();
  } else {
    await startRecording();
  }
};

const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorder = new MediaRecorder(stream);
    audioChunks = [];
    
    mediaRecorder.ondataavailable = (event) => {
      audioChunks.push(event.data);
    };
    
    mediaRecorder.onstop = () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/wav' });
      audioUrl.value = URL.createObjectURL(audioBlob);
      emit('audioRecorded', audioUrl.value);
      
      // 停止所有音频轨道
      stream.getTracks().forEach(track => track.stop());
    };
    
    mediaRecorder.start();
    isRecording.value = true;
  } catch (error) {
    console.error('录音失败:', error);
    alert('无法访问麦克风，请检查权限设置');
  }
};

const stopRecording = () => {
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop();
    isRecording.value = false;
  }
};

const removeAudio = () => {
  if (audioUrl.value) {
    URL.revokeObjectURL(audioUrl.value);
    audioUrl.value = '';
    emit('audioRemoved');
  }
};
</script>

<style scoped>
.voice-recorder {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.voice-button {
  width: 100%;
  height: 48px;
  font-weight: 600;
}

.audio-player {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  border: 1px solid rgba(41, 121, 255, 0.1);
}

.audio-control {
  flex: 1;
  height: 32px;
}

.remove-audio {
  --color: var(--ion-color-danger);
}

.recording-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: rgba(245, 61, 61, 0.1);
  border-radius: 8px;
  color: var(--ion-color-danger);
  font-size: 14px;
  font-weight: 500;
}

.pulse {
  width: 12px;
  height: 12px;
  background: var(--ion-color-danger);
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
