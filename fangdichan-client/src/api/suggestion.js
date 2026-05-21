import request from './request'

export function submitSuggestion(data) {
  const params = new URLSearchParams()
  Object.entries(data).forEach(([k, v]) => {
    if (v !== null && v !== '') params.append(k, v)
  })
  return request
    .post('/customer/suggestion', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
    .then((res) => res.data)
}
