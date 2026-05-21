import request from './request'

export function getProfile() {
  return request.get('/customer/profile').then((res) => res.data)
}

export function updateProfile(data) {
  return request.put('/customer/profile', data).then((res) => res.data)
}

export function changePassword(oldPassword, newPassword) {
  return request
    .put('/customer/profile/password', { oldPassword, newPassword })
    .then((res) => res.data)
}
