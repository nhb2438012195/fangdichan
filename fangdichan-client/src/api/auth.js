import request from './request'

export function loginApi(username, password) {
  return request.post('/public/login', { username, password }).then((res) => res.data)
}

export function registerApi(username, password, role) {
  return request
    .post('/public/register', { username, password }, { params: { role } })
    .then((res) => res.data)
}
