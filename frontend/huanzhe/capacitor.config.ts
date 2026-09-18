import type { CapacitorConfig } from '@capacitor/cli'

const localAndroidHttp = process.env.CAPACITOR_ANDROID_LOCAL_HTTP === 'true'

const config: CapacitorConfig = {
  appId: 'com.yaoanshi.patient',
  appName: '药按时',
  webDir: 'dist',
  plugins: {
    CapacitorCookies: {
      enabled: true
    }
  },
  ...(localAndroidHttp ? { server: { androidScheme: 'http', hostname: 'localhost' } } : {})
}

export default config
