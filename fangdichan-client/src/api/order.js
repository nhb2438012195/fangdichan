import request from './request'

export function getOrderList(params) {
  return request.get('/customer/order/list', { params }).then((res) => res.data)
}

export function createOrder(propertyId, message) {
  const body = new URLSearchParams()
  body.append('propertyId', propertyId)
  body.append('message', message)
  return request
    .post('/customer/order', body, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
    .then((res) => res.data)
}

export function cancelOrder(id) {
  return request.put(`/customer/order/${id}/cancel`).then((res) => res.data)
}
