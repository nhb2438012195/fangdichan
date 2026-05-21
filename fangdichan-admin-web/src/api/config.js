import request from './request'

export function getConfigList() {
  return request.get('/admin/config/list').then((res) => res.data)
}

export function updateConfig(key, value) {
  return request.put(`/admin/config/${key}`, { value }).then((res) => res.data)
}
