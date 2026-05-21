<template>
  <div style="display:flex;height:70vh">
    <div style="width:300px;border-right:1px solid #dcdfe6;overflow-y:auto">
      <div v-for="c in conversations" :key="c.id" @click="selectConversation(c)"
           style="padding:12px;cursor:pointer;border-bottom:1px solid #eee"
           :class="{ 'active-conv': selected?.id === c.id }">
        <div><strong>会话 #{{ c.id }}</strong></div>
        <div style="font-size:12px;color:#999">{{ c.propertyId ? `房源ID: ${c.propertyId}` : '' }}</div>
      </div>
    </div>
    <div style="flex:1;display:flex;flex-direction:column">
      <div style="flex:1;padding:16px;overflow-y:auto">
        <div v-for="m in messages" :key="m.id" style="margin-bottom:8px">
          <strong>{{ m.senderRole === 'CUSTOMER' ? '客户' : '房地产商' }}:</strong> {{ m.content }}
        </div>
        <div v-if="!selected" style="color:#999;text-align:center;margin-top:40px">选择一个会话</div>
      </div>
      <div v-if="selected" style="display:flex;padding:8px;border-top:1px solid #dcdfe6">
        <el-input v-model="newMsg" placeholder="输入消息" style="margin-right:8px" />
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'
import { ElMessage } from 'element-plus'

const conversations = ref([])
const selected = ref(null)
const messages = ref([])
const newMsg = ref('')

const fetchConversations = async () => {
  const res = await request.get('/agent/conversation/list')
  conversations.value = res.data.list || res.data
}

const selectConversation = async (c) => {
  selected.value = c
  const res = await request.get(`/agent/conversation/${c.id}/messages`)
  messages.value = res.data
}

const sendMessage = async () => {
  if (!newMsg.value.trim()) return
  await request.post(`/agent/conversation/${selected.value.id}/message`, { content: newMsg.value })
  messages.value.push({ content: newMsg.value, senderRole: 'AGENT', id: Date.now() })
  newMsg.value = ''
}

onMounted(fetchConversations)
</script>

<style scoped>
.active-conv { background: #ecf5ff; }
</style>
