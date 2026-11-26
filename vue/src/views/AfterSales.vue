<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb">
        <router-link to="/">首页</router-link><i>/</i>
        <router-link to="/profile">个人中心</router-link><i>/</i>
        <span>售后服务</span>
      </div>
      <div class="pg-head">
        <div>
          <h1>售后服务</h1>
          <div class="sub">退货退款、换货申请与处理进度都在这里</div>
        </div>
        <div class="head-actions">
          <button class="btn-line sm" @click="$router.push('/orders')">去订单列表申请</button>
        </div>
      </div>

      <div class="tickets" v-if="list.length">
        <div class="ticket" v-for="t in list" :key="t.id">
          <div class="t-head">
            <div class="t-id">
              <span class="tag">#{{ t.id }}</span>
              <b>{{ t.typeText }}</b>
              <span class="t-order">{{ t.orderNo }}</span>
            </div>
            <span class="tag" :class="statusClass(t.status)">{{ t.statusText }}</span>
          </div>

          <div class="t-body">
            <div class="t-goods" v-if="t.bookName">
              <span>{{ t.bookName }}</span>
              <span class="qty">× {{ t.quantity }}</span>
            </div>
            <div class="t-lines">
              <div v-if="t.reason"><span>问题</span><b>{{ t.reason }}</b></div>
              <div v-if="t.description"><span>说明</span><b>{{ t.description }}</b></div>
              <div v-if="t.adminRemark"><span>客服回复</span><b class="reply">{{ t.adminRemark }}</b></div>
            </div>
          </div>

          <div class="t-foot">
            <span>申请于 {{ formatTime(t.createdAt) }}</span>
            <span v-if="t.handledAt">处理于 {{ formatTime(t.handledAt) }}</span>
            <span class="amount" v-if="t.refundAmount">涉及金额 ¥{{ fmt(t.refundAmount) }}</span>
          </div>
        </div>
      </div>

      <EmptyState
        v-else-if="!loading"
        icon="◌"
        title="还没有售后申请"
        description="订单列表中已付款的订单可以发起退换"
        action-text="查看我的订单"
        action-to="/orders"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { afterSaleAPI } from '../api'
import EmptyState from '../components/common/EmptyState.vue'

const list = ref([])
const loading = ref(true)

const statusClass = (s) => ({
  PENDING: 'gold',
  APPROVED: 'sage',
  COMPLETED: 'sage',
  REJECTED: 'danger'
}[s] || '')

const fmt = (v) => Number(v || 0).toFixed(2)
const formatTime = (t) => {
  if (!t) return '—'
  const d = new Date(t)
  const p = (n) => String(n).padStart(2, '0')
  return `${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

onMounted(async () => {
  try {
    const res = await afterSaleAPI.getMy()
    list.value = res.data || []
  } catch {} finally {
    loading.value = false
  }
})
</script>

<style scoped>
.pg-head { display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; }
.head-actions { padding-bottom: 18px; }
.tickets { display: flex; flex-direction: column; gap: 12px; }

.ticket { border: 1px solid var(--color-line); background: var(--color-surface); }
.t-head { display: flex; align-items: center; justify-content: space-between; padding: 12px 18px; border-bottom: 1px solid var(--color-line); }
.t-id { display: flex; align-items: center; gap: 12px; }
.t-id b { font-family: var(--font-display); font-size: 14px; font-weight: 400; letter-spacing: .06em; }
.t-order { font-size: 10px; color: var(--color-ink-faint); letter-spacing: .05em; font-variant-numeric: tabular-nums; }

.t-body { padding: 14px 18px; display: flex; flex-direction: column; gap: 10px; }
.t-goods { display: flex; align-items: baseline; gap: 8px; font-size: 11.5px; color: var(--color-ink-soft); }
.t-goods .qty { font-size: 10px; color: var(--color-ink-faint); }
.t-lines { display: flex; flex-direction: column; gap: 6px; }
.t-lines > div { display: flex; gap: 12px; font-size: 10.5px; }
.t-lines span { color: var(--color-ink-faint); flex: none; width: 48px; }
.t-lines b { font-weight: 400; color: var(--color-ink-muted); line-height: 1.8; }
.t-lines b.reply { color: var(--color-gold); }

.t-foot {
  display: flex; gap: 18px; padding: 10px 18px; border-top: 1px solid var(--color-line);
  font-size: 9.5px; color: var(--color-ink-faint); letter-spacing: .05em;
}
.t-foot .amount { margin-left: auto; color: var(--color-ink-muted); }
</style>