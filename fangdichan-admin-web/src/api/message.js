import request from './request'

export function getConversationList() {
  return request.get('/agent/conversation/list').then((res) => res.data)
}

export function getMessages(conversationId) {
  return request.get(`/agent/conversation/${conversationId}/messages`).then((res) => res.data)
}

export function sendMessage(conversationId, content) {
  return request
    .post(`/agent/conversation/${conversationId}/message`, { content })
    .then((res) => res.data)
}
