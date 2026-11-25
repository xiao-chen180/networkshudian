<template>
  <div class="messages-page">
    <div class="hero-section">
      <h1 class="page-title">🔔 消息中心</h1>
    </div>
    <div class="messages-container">
      <div v-if="messages.length > 0" class="message-list">
        <div v-for="msg in messages" :key="msg.id" :class="['message-card', { unread: !msg.isRead }]" @click="markRead(msg)">
          <div class="msg-icon">{{ msg.type === 'ORDER' ? '📦' : msg.type === 'PROMOTION' ? '🎁' : '📢' }}</div>
          <div class="msg-content">
            <h3>{{ msg.title }}</h3>
            <p>{{ msg.content }}</p>
            <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
          </div>
          <span v-if="!msg.isRead" class="unread-dot"></span>
        </div>
      </div>
      <div v-else class="empty">
        <div class="empty-icon">🔔</div>
        <p>暂无消息</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { messageAPI } from '../api'

const messages = ref([])

const load = async () => {
  try {
    const res = await messageAPI.getAll()
    messages.value = res.data
  } catch (e) {}
}

const markRead = async (msg) => {
  if (!msg.isRead) {
    try {
      await messageAPI.markAsRead(msg.id)
      msg.isRead = true
    } catch (e) {}
  }
}

const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''

onMounted(() => load())
</script>

<style scoped>
.messages-page { min-height: 100vh; background: var(--bg-page); }
.hero-section { background: linear-gradient(135deg, var(--primary), var(--primary-light)); padding: 20px; text-align: center; }
.page-title { color: white; font-family: var(--font-serif); font-size: 22px; margin: 0; }
.messages-container { max-width: 800px; margin: 0 auto; padding: 16px; }
.message-list { display: flex; flex-direction: column; gap: 10px; }
.message-card { display: flex; align-items: flex-start; gap: 12px; background: white; padding: 16px; border-radius: 12px; cursor: pointer; position: relative; }
.message-card.unread { background: #fffbe6; }
.msg-icon { font-size: 24px; flex-shrink: 0; }
.msg-content { flex: 1; }
.msg-content h3 { font-size: 15px; margin: 0 0 4px; }
.msg-content p { font-size: 13px; color: var(--text-secondary); margin: 0 0 6px; }
.msg-time { font-size: 11px; color: var(--text-secondary); }
.unread-dot { width: 8px; height: 8px; background: var(--accent); border-radius: 50%; flex-shrink: 0; margin-top: 4px; }
.empty { text-align: center; padding: 80px 0; color: var(--text-secondary); }
.empty-icon { font-size: 48px; margin-bottom: 16px; }
</style>
