import request from '@/utils/request'

const backendV2BaseUrl = import.meta.env.VITE_BACKEND_V2_API_BASE_URL || ''

export function createMedicationPlan(data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: '/admin-api/medication-plans',
    method: 'post',
    data
  })
}

export function updateMedicationPlan(id, data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/medication-plans/${id}`,
    method: 'put',
    data
  })
}

export function getMedicationPlan(id) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/medication-plans/${id}`,
    method: 'get'
  })
}

export function activateMedicationPlan(id) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/medication-plans/${id}/activate`,
    method: 'post'
  })
}

export function getMedicationPlanTasks(id, params) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/medication-plans/${id}/tasks`,
    method: 'get',
    params
  })
}

export function getMedicationPlanAdherenceReport(id, params) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/medication-plans/${id}/adherence-report`,
    method: 'get',
    params
  })
}
