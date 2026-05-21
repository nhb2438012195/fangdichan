import request from './request'

export function getUserList() {
  return request.get('/admin/users').then((res) => res.data)
}

export function toggleUserStatus(id, status) {
  return request
    .put(`/admin/users/${id}/status`, { status: status ? 0 : 1 })
    .then((res) => res.data)
}
