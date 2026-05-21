import request from './request'

export function getPendingList() {
  return request.get('/admin/property/pending').then((res) => res.data)
}

export function approveProperty(id) {
  return request.put(`/admin/property/${id}/approve`).then((res) => res.data)
}

export function rejectProperty(id) {
  return request.put(`/admin/property/${id}/reject`).then((res) => res.data)
}
