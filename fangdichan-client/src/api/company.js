import request from './request'

export function getCompanyList() {
  return request.get('/customer/company/list').then((res) => res.data)
}

export function getCompanyDetail(id) {
  return request.get(`/customer/company/${id}`).then((res) => res.data)
}
