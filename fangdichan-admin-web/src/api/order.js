import request from './request'

export function getOrderList(params) {
  return request.get('/agent/order/list', { params }).then((res) => res.data)
}

export function confirmOrder(id) {
  return request.put(`/agent/order/${id}/confirm`).then((res) => res.data)
}

export function cancelOrder(id) {
  return request.put(`/agent/order/${id}/cancel`).then((res) => res.data)
}
