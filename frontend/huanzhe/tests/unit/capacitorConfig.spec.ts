import { describe, expect, test } from 'vitest'
import capacitorConfig from '../../capacitor.config'

describe('Capacitor native transport config', () => {
  test('keeps native cookie support without patching XMLHttpRequest', () => {
    expect(capacitorConfig.plugins?.CapacitorCookies).toEqual({ enabled: true })
    expect(capacitorConfig.plugins?.CapacitorHttp).toBeUndefined()
  })
})
