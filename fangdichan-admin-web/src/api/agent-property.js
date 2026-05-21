import request from './request'

export function getMyPropertyList(params) {
  return request.get('/agent/property/list', { params }).then((res) => res.data)
}

export function createProperty(data) {
  return request.post('/agent/property', data).then((res) => res.data)
}

export function updateProperty(id, data) {
  return request.put(`/agent/property/${id}`, data).then((res) => res.data)
}

export function takeOffProperty(id) {
  return request.put(`/agent/property/${id}/off-market`).then((res) => res.data)
}
