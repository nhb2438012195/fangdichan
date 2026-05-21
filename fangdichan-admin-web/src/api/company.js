import request from './request'

export function getCompanyInfo() {
  return request.get('/agent/company').then((res) => res.data)
}

export function updateCompanyInfo(data) {
  return request.put('/agent/company', data).then((res) => res.data)
}
