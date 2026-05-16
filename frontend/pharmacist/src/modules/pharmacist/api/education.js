import request from '@/utils/request'

const backendV2BaseUrl = import.meta.env.VITE_BACKEND_V2_API_BASE_URL || ''

export function getEducationCards(params) {
  return request({
    baseURL: backendV2BaseUrl,
    url: '/admin-api/education/cards',
    method: 'get',
    params
  })
}

export function importEducationCards(file, dryRun = true) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('dryRun', String(dryRun))
  return request({
    baseURL: backendV2BaseUrl,
    url: '/admin-api/education/cards/imports',
    method: 'post',
    data: formData
  })
}
