import { Capacitor, registerPlugin } from '@capacitor/core'

interface SecureMedicalCachePlugin {
  read(): Promise<{ value: string }>
  write(options: { value: string }): Promise<void>
  clear(): Promise<void>
}

export const secureMedicalCache = registerPlugin<SecureMedicalCachePlugin>('SecureMedicalCache')

export const isNativeMedicalCache = () => Capacitor.isNativePlatform()
