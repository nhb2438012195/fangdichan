import request from './request'

export function submitReport(propertyId, reason) {
  const params = new URLSearchParams()
  params.append('propertyId', propertyId)
  params.append('reason', reason)
  return request
    .post('/customer/report', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
    .then((res) => res.data)
}
