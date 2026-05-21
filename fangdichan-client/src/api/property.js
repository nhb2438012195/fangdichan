import request from './request'

export function searchProperties(params) {
  return request.get('/customer/property/search', { params }).then((res) => res.data)
}

export function getRecommended() {
  return request.get('/customer/property/recommended').then((res) => res.data)
}

export function getPropertyDetail(id) {
  return request.get(`/customer/property/${id}`).then((res) => res.data)
}
