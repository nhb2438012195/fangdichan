import request from './request'

export function getReportList() {
  return request.get('/admin/report/list').then((res) => res.data)
}

export function handleReport(id, status) {
  return request
    .put(`/admin/report/${id}/status`, null, { params: { status } })
    .then((res) => res.data)
}
