import request from '@/utils/request'

export function confirmPatientBinding(id) {
  return request({ baseURL: backendV2BaseUrl, url: `/admin-api/patients/${id}/confirm-binding`, method: 'post' })
}

const backendV2BaseUrl = import.meta.env.VITE_BACKEND_V2_API_BASE_URL || ''

export function getPatients(params) {
  return request({
    baseURL: backendV2BaseUrl,
    url: '/admin-api/patients',
    method: 'get',
    params
  })
}

export function getPatientDetail(id) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/patients/${id}`,
    method: 'get'
  })
}

export function updatePatient(id, data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/patients/${id}`,
    method: 'put',
    data
  })
}

export function remindKnowledgeAssessment(id) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/patients/${id}/knowledge-assessments/remind`,
    method: 'post'
  })
}

export function getPharmacistOptions() {
  return request({
    baseURL: backendV2BaseUrl,
    url: '/admin-api/pharmacists/options',
    method: 'get'
  })
}

export function reassignPatient(id, data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/patients/${id}/responsible-pharmacist`,
    method: 'put',
    data
  })
}

export function downloadPatientAdherence(id, days) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/patients/${id}/adherence-report.csv`,
    method: 'get',
    params: { days },
    responseType: 'blob'
  })
}
