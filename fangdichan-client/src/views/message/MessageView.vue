<template>
  <div style="display: flex; height: 70vh">
    <div style="width: 280px; border-right: 1px solid #dcdfe6; overflow-y: auto">
      <div
        v-for="c in conversations"
        :key="c.id"
        style="padding: 12px; cursor: pointer; border-bottom: 1px solid #eee"
        :class="{ active: selected?.id === c.id }"
        @click="selectConversation(c)"
      >
        <div>
          <strong>会话 #{{ c.id }}</strong>
        </div>
        <div style="font-size: 12px; color: #999">
          {{ c.propertyId ? '房源ID:' + c.propertyId : '' }}
        </div>
      </div>
      <div v-if="!conversations.length" style="padding: 12px; color: #999; text-align: center">
        暂无会话
      </div>
    </div>
    <div style="flex: 1; display: flex; flex-direction: column">
      <div v-loading="loadingMessages" style="flex: 1; padding: 16px; overflow-y: auto">
        <div v-for="m in messages" :key="m.id" style="margin-bottom: 8px">
          <strong>{{ m.senderRole === 'CUSTOMER' ? '我' : '房地产商' }}:</strong> {{ m.content }}
        </div>
        <div v-if="!selected" style="color: #999; text-align: center; margin-top: 40px">
          选择一个会话
        </div>
      </div>
      <div v-if="selected" style="display: flex; padding: 8px; border-top: 1px solid #dcdfe6">
        <el-input
          v-model="newMsg"
          placeholder="输入消息"
          style="margin-right: 8px"
          @keyup.enter="sendMessage"
        />
        <el-button type="primary" :loading="sending" @click="sendMessage">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import request from '../../api/request'

const route = useRoute()
const conversations = ref([])
const selected = ref(null)
const messages = ref([])
const newMsg = ref('')
const loadingMessages = ref(false)
const sending = ref(false)
let ws = null
let reconnectTimer = null
let reconnectAttempts = 0

const fetchConversations = async () => {
  const res = await request.get('/customer/conversation/list')
  conversations.value = res.data.list || res.data
  // Auto-select conversation if conversationId is in URL
  const targetId = route.query.conversationId
  if (targetId) {
    const target = conversations.value.find((c) => c.id === Number(targetId))
    if (target) {
      await nextTick()
      selectConversation(target)
    }
  }
}

const selectConversation = async (c) => {
  selected.value = c
  loadingMessages.value = true
  try {
    const res = await request.get(`/customer/conversation/${c.id}/messages`)
    messages.value = res.data
  } catch {
    messages.value = []
  } finally {
    loadingMessages.value = false
  }
}

const sendMessage = async () => {
  if (!newMsg.value.trim()) return
  const content = newMsg.value
  sending.value = true
  try {
    await request.post(`/customer/conversation/${selected.value.id}/message`, { content })
    messages.value.push({ content, senderRole: 'CUSTOMER', id: Date.now() })
    newMsg.value = ''
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ conversationId: selected.value.id, content }))
    }
  } catch {
    // message send failed, will be shown via ElMessage from interceptor
  } finally {
    sending.value = false
  }
}

const connectWs = () => {
  const token = localStorage.getItem('token')
  if (!token) return
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  ws = new WebSocket(`${protocol}//${host}/ws?token=${token}`)
  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      if (selected.value?.id === msg.conversationId) {
        messages.value.push(msg)
      }
    } catch {}
  }
  ws.onclose = () => {
    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000)
    reconnectAttempts++
    reconnectTimer = setTimeout(connectWs, delay)
  }
  ws.onopen = () => {
    reconnectAttempts = 0
  }
}

onMounted(() => {
  fetchConversations()
  connectWs()
})
onUnmounted(() => {
  if (ws) ws.close()
  if (reconnectTimer) clearTimeout(reconnectTimer)
})
</script>

<style scoped>
.active {
  background: #ecf5ff;
}
</style>
