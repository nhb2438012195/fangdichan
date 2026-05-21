<template>
  <div class="message-layout">
    <div class="conv-sidebar">
      <div
        v-for="c in conversations"
        :key="c.id"
        class="conv-item"
        :class="{ 'active-conv': selected?.id === c.id }"
        @click="selectConversation(c)"
      >
        <div>
          <strong>会话 #{{ c.id }}</strong>
        </div>
        <div class="conv-meta">
          {{ c.propertyId ? `房源ID: ${c.propertyId}` : '' }}
        </div>
      </div>
    </div>
    <div class="msg-area">
      <div class="msg-list">
        <div v-for="m in messages" :key="m.id" class="msg-item">
          <strong>{{ m.senderRole === 'CUSTOMER' ? '客户' : '房地产商' }}:</strong> {{ m.content }}
        </div>
        <div v-if="!selected" class="no-selection">选择一个会话</div>
      </div>
      <div v-if="selected" class="input-bar">
        <el-input v-model="newMsg" placeholder="输入消息" class="msg-input" />
        <el-button type="primary" @click="handleSendMessage">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConversationList, getMessages, sendMessage } from '../../api/message'

const conversations = ref([])
const selected = ref(null)
const messages = ref([])
const newMsg = ref('')

const fetchConversations = async () => {
  try {
    conversations.value = await getConversationList()
  } catch {
    conversations.value = []
  }
}

const selectConversation = async (c) => {
  selected.value = c
  try {
    messages.value = await getMessages(c.id)
  } catch {
    messages.value = []
  }
}

const handleSendMessage = async () => {
  if (!newMsg.value.trim()) return
  try {
    await sendMessage(selected.value.id, newMsg.value)
    messages.value.push({ content: newMsg.value, senderRole: 'AGENT', id: Date.now() })
    newMsg.value = ''
  } catch {
    ElMessage.error('发送失败')
  }
}

onMounted(fetchConversations)
</script>

<style scoped>
.message-layout {
  display: flex;
  height: 70vh;
}
.conv-sidebar {
  width: 300px;
  border-right: 1px solid #dcdfe6;
  overflow-y: auto;
}
.conv-item {
  padding: 12px;
  cursor: pointer;
  border-bottom: 1px solid #eee;
}
.conv-meta {
  font-size: 12px;
  color: #999;
}
.msg-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.msg-list {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}
.msg-item {
  margin-bottom: 8px;
}
.no-selection {
  color: #999;
  text-align: center;
  margin-top: 40px;
}
.input-bar {
  display: flex;
  padding: 8px;
  border-top: 1px solid #dcdfe6;
}
.msg-input {
  margin-right: 8px;
}
.active-conv {
  background: #ecf5ff;
}
</style>
