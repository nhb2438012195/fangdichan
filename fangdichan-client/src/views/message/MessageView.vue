<template>
  <div class="message-layout">
    <div class="conversation-sidebar">
      <div
        v-for="c in conversations"
        :key="c.id"
        class="conversation-item"
        :class="{ active: selected?.id === c.id }"
        @click="selectConversation(c)"
      >
        <div>
          <strong>会话 #{{ c.id }}</strong>
        </div>
        <div class="conversation-meta">
          {{ c.propertyId ? '房源ID:' + c.propertyId : '' }}
        </div>
      </div>
      <div v-if="!conversations.length" class="empty-conversations">暂无会话</div>
    </div>
    <div class="messages-panel">
      <div v-loading="loadingMessages" class="messages-area">
        <div v-for="m in messages" :key="m.id" class="message-item">
          <strong>{{ m.senderRole === 'CUSTOMER' ? '我' : '房地产商' }}:</strong> {{ m.content }}
        </div>
        <div v-if="!selected" class="placeholder-text">选择一个会话</div>
      </div>
      <div v-if="selected" class="input-bar">
        <el-input
          v-model="newMsg"
          placeholder="输入消息"
          class="input-field"
          @keyup.enter="handleSendMessage"
        />
        <el-button type="primary" :loading="sending" @click="handleSendMessage">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getConversationList, getMessages, sendMessage } from '../../api/message'
import { useWebSocket } from '../../composables/useWebSocket'

const route = useRoute()
const conversations = ref([])
const selected = ref(null)
const messages = ref([])
const newMsg = ref('')
const loadingMessages = ref(false)
const sending = ref(false)

const onWsMessage = (msg) => {
  if (selected.value?.id === msg.conversationId) {
    messages.value.push(msg)
  }
}
const { connect: connectWs, send: wsSend } = useWebSocket(onWsMessage)

const fetchConversations = async () => {
  const data = await getConversationList()
  conversations.value = data.list || data
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
    messages.value = await getMessages(c.id)
  } catch {
    messages.value = []
  } finally {
    loadingMessages.value = false
  }
}

const handleSendMessage = async () => {
  if (!newMsg.value.trim() || !selected.value) return
  const content = newMsg.value
  sending.value = true
  try {
    await sendMessage(selected.value.id, content)
    messages.value.push({ content, senderRole: 'CUSTOMER', id: Date.now() })
    newMsg.value = ''
    wsSend({ conversationId: selected.value.id, content })
  } catch {
    // error handled by interceptor
  } finally {
    sending.value = false
  }
}

onMounted(() => {
  fetchConversations()
  connectWs()
})
</script>

<style scoped>
.active {
  background: #ecf5ff;
}
.message-layout {
  display: flex;
  height: 70vh;
}
.conversation-sidebar {
  width: 280px;
  border-right: 1px solid #dcdfe6;
  overflow-y: auto;
}
.conversation-item {
  padding: 12px;
  cursor: pointer;
  border-bottom: 1px solid #eee;
}
.conversation-meta {
  font-size: 12px;
  color: #999;
}
.empty-conversations {
  padding: 12px;
  color: #999;
  text-align: center;
}
.messages-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.messages-area {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}
.message-item {
  margin-bottom: 8px;
}
.placeholder-text {
  color: #999;
  text-align: center;
  margin-top: 40px;
}
.input-bar {
  display: flex;
  padding: 8px;
  border-top: 1px solid #dcdfe6;
}
.input-field {
  margin-right: 8px;
}
@media (max-width: 768px) {
  .message-layout {
    flex-direction: column;
    height: auto;
  }
  .conversation-sidebar {
    width: 100%;
    max-height: 200px;
  }
}
</style>
