import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { beforeEach, describe, expect, test, vi } from 'vitest'

const httpMocks = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn()
}))

vi.mock('@/shared/http/client', () => ({
  httpService: httpMocks
}))

import { aiConsentService } from '@/modules/patient/api/mvp'

describe('AI consent data boundary', () => {
  beforeEach(() => {
    httpMocks.get.mockReset()
    httpMocks.post.mockReset()
  })

  test('loads disclosure and grants the exact policy version', async () => {
    httpMocks.get.mockResolvedValue({ data: { policyVersion: 'sec-005-v2' } })
    httpMocks.post.mockResolvedValue({ data: true })

    await aiConsentService.getDisclosure()
    await aiConsentService.grantConsent('sec-005-v2')

    expect(httpMocks.get).toHaveBeenCalledWith('/app-api/ai/disclosure')
    expect(httpMocks.post).toHaveBeenCalledWith('/app-api/ai/consent', { policyVersion: 'sec-005-v2' })
  })

  test('does not cache consent in browser storage', () => {
    const pageSource = readFileSync(
      resolve(process.cwd(), 'src/modules/patient/pages/AiAssistantPage.vue'),
      'utf8'
    )

    expect(pageSource).not.toContain('AI_CONSENT_KEY')
    expect(pageSource).not.toContain('localStorage')
    expect(pageSource).toContain('getDisclosure')
  })
})
