<template>
  <div class="chat-layout">
    <el-card shadow="never" class="session-list">
      <h4>会话列表</h4>
      <div v-for="s in sessions" :key="s.id" class="session" @click="openSession(s)">
        <el-avatar :size="40" icon="UserFilled" />
        <div style="margin-left:10px">
          <div style="font-weight:600">{{ s.fromUser === 4 ? 'HR-' + s.toUser : '求职者-' + s.fromUser }}</div>
          <div style="color:#999;font-size:12px">{{ s.content?.slice(0,15) }}</div>
        </div>
      </div>
    </el-card>
    <el-card shadow="never" class="chat-main">
      <div class="messages">
        <div v-for="m in messages" :key="m.id" class="msg" :class="{me: m.fromUser === 4}">
          <el-avatar :size="32" icon="UserFilled" />
          <div class="bubble">{{ m.content }}</div>
        </div>
      </div>
      <div class="input-bar">
        <el-input v-model="input" placeholder="输入消息..." @keyup.enter="send" />
        <el-button type="primary" @click="send">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api'

const sessions = ref([])
const messages = ref([])
const input = ref('')
const currentSession = ref('1_4')

onMounted(async () => {
  const res = await request.get('/chat/sessions/4')
  sessions.value = res.data
  await loadHistory()
})

const loadHistory = async () => {
  const res = await request.get('/chat/history/' + currentSession.value)
  messages.value = res.data
}
const openSession = async (s) => {
  currentSession.value = s.sessionId
  await loadHistory()
}
const send = async () => {
  if (!input.value.trim()) return
  await request.post('/chat/send', { fromUser: 4, toUser: 1, content: input.value })
  input.value = ''
  await loadHistory()
}
</script>

<style scoped>
.chat-layout { display: flex; gap: 16px; height: calc(100vh - 120px); }
.session-list { width: 280px; }
.session { display: flex; align-items: center; padding: 10px; border-radius: 8px; cursor: pointer; }
.session:hover { background: #f5f6f8; }
.chat-main { flex: 1; display: flex; flex-direction: column; }
.messages { flex: 1; overflow-y: auto; padding: 10px; }
.msg { display: flex; margin-bottom: 16px; align-items: flex-start; }
.msg.me { flex-direction: row-reverse; }
.bubble { background: #f0f2f5; padding: 10px 14px; border-radius: 8px; margin: 0 10px; max-width: 60%; }
.msg.me .bubble { background: #ff6b35; color: #fff; }
.input-bar { display: flex; gap: 10px; padding-top: 10px; border-top: 1px solid #eee; }
</style>
