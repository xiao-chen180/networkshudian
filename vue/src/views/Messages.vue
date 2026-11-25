<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><router-link to="/profile">个人中心</router-link><i>/</i><span>消息中心</span></div>
      <div class="pg-head">
        <div>
          <h1>消息中心</h1>
          <div class="sub">共 {{ messages.length }} 条 · 未读 {{ unread }}</div>
        </div>
        <button class="btn-line sm" :disabled="!unread" @click="markAll">全部标为已读</button>
      </div>

      <div class="mlist" v-if="messages.length">
        <div class="msg" v-for="msg in messages" :key="msg.id" :class="{ unread: !msg.isRead }" @click="markRead(msg)">
          <span class="type">{{ typeText(msg.type) }}</span>
          <div class="content">
            <b>{{ msg.title }}</b>
            <p>{{ msg.content }}</p>
            <span class="time">{{ fmtTime(msg.createdAt) }}</span>
          </div>
          <span class="dot" v-if="!msg.isRead"></span>
        </div>
      </div>
      <div class="blank" v-else>暂无消息</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { messageAPI } from '../api'

const messages = ref([])
const unread = computed(() => messages.value.filter(m => !m.isRead).length)

const typeText = (t) => ({ ORDER: '订单', PROMOTION: '活动', SYSTEM: '系统' }[t] || '通知')
const fmtTime = (t) => (t ? new Date(t).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '')

async function load() {
  try {
    const res = await messageAPI.getAll()
    messages.value = res.data || []
  } catch {}
}

async function markRead(msg) {
  if (msg.isRead) return
  try {
    await messageAPI.markAsRead(msg.id)
    msg.isRead = true
  } catch {}
}

async function markAll() {
  try {
    await messageAPI.markAllAsRead()
    messages.value.forEach(m => { m.isRead = true })
  } catch {}
}

onMounted(load)
</script>

<style scoped>
.mlist { border-top: 1px solid var(--color-line-strong); }
.msg {
  display: flex; gap: 18px; padding: 18px 4px; border-bottom: 1px solid var(--color-line);
  cursor: pointer; transition: background var(--dur-fast);
}
.msg:hover { background: var(--color-surface); }
.type {
  width: 44px; flex: none; font-size: 9.5px; letter-spacing: .12em; color: var(--color-ink-faint);
  padding-top: 3px;
}
.msg.unread .type { color: var(--color-gold); }
.content { flex: 1; min-width: 0; }
.content b { display: block; font-family: var(--font-display); font-size: 13.5px; font-weight: 400; letter-spacing: .05em; }
.msg.unread .content b { color: var(--color-ink); }
.content p { margin-top: 7px; font-size: 11.5px; line-height: 1.9; color: var(--color-ink-muted); letter-spacing: .03em; }
.time { display: block; margin-top: 9px; font-size: 9.5px; color: var(--color-ink-faint); }
.dot { width: 5px; height: 5px; border-radius: 50%; background: var(--color-gold); flex: none; margin-top: 8px; }
</style>