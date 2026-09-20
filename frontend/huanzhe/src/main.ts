import { createApp } from 'vue'
import App from './App.vue'
import router from './router';
import { appAuthService, patientService } from '@/modules/patient/api/mvp'
import { clearMedicalCache, replayPendingCheckins } from '@/modules/patient/services/offlineMedication'
import {
  cancelAllMedicationReminders,
  registerMedicationReminderActionHandler
} from '@/modules/patient/services/localMedicationReminders'
import { patientSession } from '@/app/session'
import { httpService } from '@/shared/http/client'

import { IonicVue } from '@ionic/vue';
import AppIcon from '@/shared/components/AppIcon.vue'
import EmptyState from '@/shared/components/EmptyState.vue'

/* Core CSS required for Ionic components to work properly */
import '@ionic/vue/css/core.css';

/* Basic CSS for apps built with Ionic */
import '@ionic/vue/css/normalize.css';
import '@ionic/vue/css/structure.css';
import '@ionic/vue/css/typography.css';

/* Optional CSS utils that can be commented out */
import '@ionic/vue/css/padding.css';
import '@ionic/vue/css/float-elements.css';
import '@ionic/vue/css/text-alignment.css';
import '@ionic/vue/css/text-transformation.css';
import '@ionic/vue/css/flex-utils.css';
import '@ionic/vue/css/display.css';

/**
 * Ionic Dark Mode
 * -----------------------------------------------------
 * For more info, please see:
 * https://ionicframework.com/docs/theming/dark-mode
 */

/* Theme variables */
import './theme/variables.css';

if (typeof window !== 'undefined') {
  window.addEventListener('patient-account-switched', () => {
    void clearMedicalCache()
    void cancelAllMedicationReminders()
  })
  window.addEventListener('patient-session-anonymous', () => {
    void clearMedicalCache()
    void cancelAllMedicationReminders()
  })
  const clearRevokedMedicalContext = async (expectedAccountSubject?: string) => {
    if (expectedAccountSubject && patientSession.getAccountSubject() !== expectedAccountSubject) return
    await appAuthService.logout().catch(() => undefined)
    if (expectedAccountSubject && patientSession.getAccountSubject() !== expectedAccountSubject) return
    await Promise.all([
      clearMedicalCache().catch(() => undefined),
      cancelAllMedicationReminders().catch(() => undefined)
    ])
    patientSession.clearAuthState()
    patientSession.clearGateState()
    await router.replace('/login')
    window.location.reload()
  }
  let familyAccessCheck: { accountSubject: string; promise: Promise<boolean> } | null = null
  const revalidateFamilyAccess = () => {
    const accountSubject = patientSession.getAccountSubject()
    if (patientSession.getAccountType() !== 'FAMILY'
      || !accountSubject
      || !patientSession.getAccessToken()
      || !navigator.onLine) return Promise.resolve(true)
    if (familyAccessCheck?.accountSubject === accountSubject) return familyAccessCheck.promise
    const attempt = (async () => {
      try {
        const currentPatient = await patientService.getCurrentPatient()
        if (patientSession.getAccountSubject() !== accountSubject) return false
        if (!currentPatient.data) {
          await clearRevokedMedicalContext(accountSubject)
          return false
        }
        return true
      } catch (error) {
        if (patientSession.getAccountSubject() !== accountSubject) return false
        const message = error instanceof Error ? error.message : ''
        if (message === 'PATIENT_BIND_CONFLICT' || message === 'PATIENT_NOT_FOUND') {
          await clearRevokedMedicalContext(accountSubject)
        }
        return false
      }
    })().finally(() => {
      if (familyAccessCheck?.promise === attempt) familyAccessCheck = null
    })
    familyAccessCheck = { accountSubject, promise: attempt }
    return attempt
  }
  const restoreAndReplay = async () => {
    if (await httpService.restoreSession() && patientSession.getAccessToken()) {
      if (!await revalidateFamilyAccess()) return
      await replayPendingCheckins()
    }
  }
  window.addEventListener('online', () => void restoreAndReplay())
  window.addEventListener('patient-session-authenticated', () => void restoreAndReplay())
  window.addEventListener('focus', () => void revalidateFamilyAccess())
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') void revalidateFamilyAccess()
  })
  window.setInterval(() => void revalidateFamilyAccess(), 60_000)
  void restoreAndReplay()
  void registerMedicationReminderActionHandler(async (route) => {
    await router.isReady()
    await router.push(route)
  })
}

if (import.meta.env.DEV && typeof window !== 'undefined') {
  const runtimeWindow = window as typeof window & {
    __patientDev?: {
      getCurrentAccount: typeof appAuthService.getCurrentAccount
      getCurrentPatient: typeof patientService.getCurrentPatient
    }
  }

  runtimeWindow.__patientDev = {
    getCurrentAccount: () => appAuthService.getCurrentAccount(),
    getCurrentPatient: () => patientService.getCurrentPatient()
  }
}

const app = createApp(App)
  .use(IonicVue)
  .use(router)
  .component('AppIcon', AppIcon)
  .component('EmptyState', EmptyState);

router.isReady().then(() => {
  app.mount('#app');
});
