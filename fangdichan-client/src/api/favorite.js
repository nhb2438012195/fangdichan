import request from './request'

export function getFavoriteList(params) {
  return request.get('/customer/favorite/list', { params }).then((res) => res.data)
}

export function toggleFavorite(propertyId) {
  return request.post(`/customer/favorite/${propertyId}`).then((res) => res.data)
}

export function checkFavorite(propertyId) {
  return request.get(`/customer/favorite/check/${propertyId}`).then((res) => res.data)
}
