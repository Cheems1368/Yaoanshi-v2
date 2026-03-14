import { randomUUID } from 'node:crypto'

const SUCCESS_CODES = new Set([0, 200])

export const tinyPngBuffer = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO2NQ1EAAAAASUVORK5CYII=',
  'base64'
)

export function createRunId() {
  return Date.now().toString().slice(-6)
}

export function createMobile(runId, prefix = '138') {
  return `${prefix}${runId.padStart(8, '0')}`.slice(0, 11)
}

export function createPatientIdentity(runId, label = 'Smoke Patient') {
  return {
    mobile: createMobile(runId),
    hospitalPatientId: `HZY-${runId}`,
    patientName: `${label} ${runId}`,
    deviceId: `patient-web-${runId}-${randomUUID().slice(0, 8)}`
  }
}

export function createFamilyIdentity(runId, prefix = '139') {
  return {
    mobile: createMobile(runId, prefix),
    deviceId: `family-web-${runId}-${randomUUID().slice(0, 8)}`
  }
}

export async function requestJson(url, { method = 'GET', headers = {}, body, allowFailure = false } = {}) {
  const response = await fetch(url, {
    method,
    headers: {
      ...(body ? { 'Content-Type': 'application/json' } : {}),
      ...headers
    },
    body: body ? JSON.stringify(body) : undefined
  })

  const rawText = await response.text()
  let payload = {}
  if (rawText) {
    try {
      payload = JSON.parse(rawText)
    } catch {
      payload = { message: rawText }
    }
  }
  const success = response.ok && SUCCESS_CODES.has(payload.code)

  if (!allowFailure && !success) {
    throw new Error(payload.msg || payload.message || `HTTP ${response.status}`)
  }

  return {
    response,
    payload,
    success
  }
}

export function getPayloadMessage(payload) {
  if (!payload || typeof payload !== 'object') {
    return undefined
  }
  return payload.msg || payload.message || payload.error || payload.errorMessage
}

export async function sendOtp(baseUrl, mobile) {
  return requestJson(`${baseUrl}/app-api/auth/send-otp`, {
    method: 'POST',
    body: {
      mobile,
      scene: 'APP_LOGIN'
    }
  })
}

export async function appLogin(baseUrl, { mobile, deviceId }) {
  await sendOtp(baseUrl, mobile)
  const login = await requestJson(`${baseUrl}/app-api/auth/login`, {
    method: 'POST',
    body: {
      mobile,
      otpCode: '123456',
      deviceId,
      deviceName: 'Playwright Smoke',
      platform: 'WEB'
    }
  })

  return {
    accessToken: login.payload.data.accessToken,
    refreshToken: login.payload.data.refreshToken,
    deviceId: login.payload.data.deviceId
  }
}

export async function bindPatient(baseUrl, session, { mobile, hospitalPatientId, patientName }) {
  return requestJson(`${baseUrl}/app-api/patients/bind`, {
    method: 'POST',
    headers: appHeaders(session.accessToken),
    body: {
      mobile,
      hospitalPatientId,
      name: patientName
    }
  })
}

export async function createPatientSession(baseUrl, identity) {
  const login = await appLogin(baseUrl, identity)
  await bindPatient(baseUrl, login, identity)
  return {
    ...identity,
    ...login
  }
}

export async function createFamilySession(baseUrl, identity) {
  const login = await appLogin(baseUrl, identity)
  return {
    ...identity,
    ...login
  }
}

export function appHeaders(accessToken) {
  return {
    Authorization: `Bearer ${accessToken}`,
    'tenant-id': '1'
  }
}

export async function professionalLogin(baseUrl, { username, password, role }) {
  const login = await requestJson(`${baseUrl}/admin-api/system/auth/login`, {
    method: 'POST',
    headers: {
      'tenant-id': '1'
    },
    body: {
      username,
      password,
      role
    }
  })

  return {
    accessToken: login.payload.data.accessToken,
    refreshToken: login.payload.data.refreshToken || ''
  }
}

export function adminHeaders(accessToken) {
  return {
    Authorization: `Bearer ${accessToken}`,
    'tenant-id': '1'
  }
}

export async function createPrescriptionUpload(baseUrl, session, filename = 'smoke.png') {
  const presign = await requestJson(`${baseUrl}/app-api/files/presign`, {
    method: 'POST',
    headers: appHeaders(session.accessToken),
    body: {
      filename,
      contentType: 'image/png',
      sizeBytes: tinyPngBuffer.length,
      businessType: 'PRESCRIPTION'
    }
  })

  const upload = await requestJson(`${baseUrl}/app-api/prescriptions/uploads`, {
    method: 'POST',
    headers: appHeaders(session.accessToken),
    body: {
      objectKey: presign.payload.data.objectKey,
      originalFilename: filename,
      contentType: 'image/png',
      sizeBytes: tinyPngBuffer.length
    }
  })

  return upload.payload.data
}

export async function primePatientPage(page, baseUrl, session, { bound = true, hasActivePlan = false } = {}) {
  await page.goto(baseUrl, { waitUntil: 'domcontentloaded' })
  await page.evaluate(
    ({ refreshToken, deviceId, mobile, bound, hasActivePlan }) => {
      sessionStorage.setItem('patient_refresh_token', refreshToken)
      localStorage.setItem('patient_device_id', deviceId)
      sessionStorage.setItem('patient_mobile', mobile)
      localStorage.setItem('patient_binding_status', bound ? 'BOUND' : 'PENDING')
      localStorage.setItem('patient_has_active_plan', hasActivePlan ? 'true' : 'false')
    },
    {
      refreshToken: session.refreshToken,
      deviceId: session.deviceId,
      mobile: session.mobile,
      bound,
      hasActivePlan
    }
  )
}

export async function primeProfessionalPage(page, baseUrl, session) {
  await page.goto(baseUrl, { waitUntil: 'domcontentloaded' })
  await page.evaluate(
    ({ refreshToken }) => {
      if (refreshToken) {
        sessionStorage.setItem('refreshToken', refreshToken)
      }
    },
    session
  )
  await page.reload({ waitUntil: 'networkidle' })
}

export async function expectCommonResultFailure(promiseFactory, expectedMessage) {
  const result = await promiseFactory()
  if (result.success) {
    throw new Error(`Expected failure: ${expectedMessage}`)
  }
  const actualMessage = getPayloadMessage(result.payload)
  if (expectedMessage && actualMessage !== expectedMessage) {
    throw new Error(`Expected ${expectedMessage}, received ${actualMessage}`)
  }
  return result.payload
}
