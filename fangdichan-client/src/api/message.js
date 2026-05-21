import request from './request'

export function getConversationList() {
  return request.get('/customer/conversation/list').then((res) => res.data)
}

export function getMessages(conversationId) {
  return request.get(`/customer/conversation/${conversationId}/messages`).then((res) => res.data)
}

export function sendMessage(conversationId, content) {
  return request
    .post(`/customer/conversation/${conversationId}/message`, { content })
    .then((res) => res.data)
}

export function createConversation(companyId, propertyId) {
  return request
    .post('/customer/conversation', null, {
      params: { companyId, propertyId }
    })
    .then((res) => res.data)
}
