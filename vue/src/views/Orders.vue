<template>
  <div class="orders-page">
    <div class="wrap">
      <div class="pagehead">
        <div class="crumb"><router-link to="/">首页</router-link><i>/</i><router-link to="/profile">个人中心</router-link><i>/</i><span>我的订单</span></div>
        <div class="row">
          <div>
            <h1>我的订单</h1>
            <div class="sub">共 {{ orders.length }} 笔订单</div>
          </div>
        </div>
      </div>

      <div class="tabs">
        <a v-for="t in tabs" :key="t.key" :class="{ on: activeTab === t.key }" @click="activeTab = t.key">
          {{ t.label }} <em>{{ countOf(t.key) }}</em>
        </a>
      </div>

      <template v-if="filteredOrders.length">
        <div class="order" v-for="order in filteredOrders" :key="order.id">
          <div class="order-h">
            <span class="no">订单号 {{ orderNo(order) }}</span>
            <span class="time" v-if="order.createdAt">{{ formatTime(order.createdAt) }}</span>
            <span class="st"><OrderStatusBadge :status="order.status" /></span>
          </div>

          <div class="order-b">
            <div class="items">
              <div class="oitem" v-for="item in order.items" :key="item.id">
                <BookCover :src="item.imageUrl || item.bookImageUrl" :title="item.bookName" class="oi-cover" compact ratio="3:4" />
                <div class="oi-meta">
                  <b>{{ item.bookName }}</b>
                  <span>{{ item.author || item.bookAuthor || '书香阁' }}</span>
                  <span class="qty">× {{ item.quantity }}</span>
                </div>
                <div class="oi-price">¥{{ formatPrice(item.price) }}</div>
              </div>
            </div>
            <div class="ototal">
              <div class="l">{{ amountLabel(order) }}</div>
              <div class="v">¥{{ formatPrice(order.totalPrice) }}</div>
              <div class="n">共 {{ totalQty(order) }} 件</div>
              <div class="n" v-if="Number(order.couponDiscount) > 0">券抵 ¥{{ formatPrice(order.couponDiscount) }}</div>
            </div>
          </div>

          <div class="order-f">
            <span class="hint" :class="{ urgent: order.status === 'PENDING' && remainMs(order) > 0, expired: order.status === 'PENDING' && remainMs(order) === 0 }">
              {{ footerHint(order) }}
            </span>
            <div class="acts">
              <template v-if="order.status === 'PENDING'">
                <span class="expired-note" v-if="remainMs(order) === 0">已超时</span>
                <button class="obtn" @click="cancelOrder(order.id)">取消订单</button>
                <button class="obtn gold" v-if="remainMs(order) > 0" @click="payOrder(order)">去支付</button>
                <button class="obtn gold" v-else @click="goBook(order.items?.[0]?.bookId)">重新下单</button>
              </template>
              <button class="obtn gold" v-if="order.status === 'SHIPPED'" @click="confirmReceive(order.id)">确认收货</button>
              <button class="obtn gold" v-if="order.status === 'COMPLETED'" @click="goBook(order.items?.[0]?.bookId)">再次购买</button>
              <button class="obtn" v-if="order.status === 'COMPLETED'">申请退换</button>
            </div>
          </div>
        </div>
      </template>

      <EmptyState
        v-else
        icon="◌"
        :title="activeTab === 'ALL' ? '还没有订单' : '这个状态下没有订单'"
        description="挑几本喜欢的书，下第一单吧"
        action-text="去逛书店"
        :action-to="'/books'"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderAPI } from '../api'
import { ElMessage } from 'element-plus'
import BookCover from '../components/common/BookCover.vue'
import OrderStatusBadge from '../components/common/OrderStatusBadge.vue'
import EmptyState from '../components/common/EmptyState.vue'

const router = useRouter()
const orders = ref([])
const activeTab = ref('ALL')
const now = ref(Date.now())
let timer = null

const tabs = [
  { key: 'ALL', label: '全部' },
  { key: 'PENDING', label: '待付款' },
  { key: 'PAID', label: '待发货' },
  { key: 'SHIPPED', label: '待收货' },
  { key: 'COMPLETED', label: '已完成' },
  { key: 'CANCELLED', label: '已取消' }
]

const filteredOrders = computed(() =>
  activeTab.value === 'ALL' ? orders.value : orders.value.filter(o => o.status === activeTab.value)
)

const countOf = (key) => (key === 'ALL' ? orders.value.length : orders.value.filter(o => o.status === key).length)
const orderNo = (o) => `SG${String(o.id).padStart(8, '0')}`
const formatPrice = (p) => (typeof p === 'string' ? p : Number(p || 0).toFixed(2))
const formatTime = (t) => new Date(t).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
const totalQty = (o) => (o.items || []).reduce((s, i) => s + (i.quantity || 0), 0)

const hintOf = (o) => {
  const map = {
    PENDING: '30 分钟内未支付将自动取消并释放库存',
    PAID: '已付款，等待仓库发货',
    SHIPPED: '已发出，注意查收',
    COMPLETED: '已完成，评价可得积分',
    CANCELLED: '订单已取消'
  }
  return map[o.status] || ''
}

/* 待付款订单的 30 分钟支付倒计时 */
const PAY_WINDOW = 30 * 60 * 1000
const remainMs = (o) => {
  if (o.status !== 'PENDING' || !o.createdAt) return 0
  return Math.max(0, new Date(o.createdAt).getTime() + PAY_WINDOW - now.value)
}
const remainText = (o) => {
  const ms = remainMs(o)
  const m = String(Math.floor(ms / 60000)).padStart(2, '0')
  const s = String(Math.floor((ms % 60000) / 1000)).padStart(2, '0')
  return `${m}:${s}`
}
const footerHint = (o) => {
  if (o.status === 'PENDING') {
    if (!o.createdAt) return hintOf(o)
    return remainMs(o) > 0
      ? `${remainText(o)} 后自动取消并释放库存`
      : '支付已超时，订单即将自动取消'
  }
  return hintOf(o)
}

/* 金额标签：未支付叫应付，已取消叫订单金额，其余为实付 */
const amountLabel = (o) => {
  if (o.status === 'PENDING') return '应付金额'
  if (o.status === 'CANCELLED') return '订单金额'
  return '实付金额'
}

async function loadOrders() {
  try {
    const res = await orderAPI.getAll()
    orders.value = res.data || []
  } catch {}
}

async function cancelOrder(id) {
  try {
    await orderAPI.updateStatus(id, 'CANCELLED')
    ElMessage.success('已取消')
    loadOrders()
  } catch {
    ElMessage.error('取消失败')
  }
}

function payOrder(order) {
  const totalQuantity = totalQty(order)
  const total = Number(order.totalPrice) || 0
  router.push({ path: '/payment', query: { orderId: order.id, totalQuantity, totalPrice: total, shippingFee: 0, finalPrice: total } })
}

async function confirmReceive(id) {
  try {
    await orderAPI.updateStatus(id, 'COMPLETED')
    ElMessage.success('已确认收货')
    loadOrders()
  } catch {
    ElMessage.error('操作失败')
  }
}

const goBook = (id) => id && router.push(`/books/${id}`)

onMounted(() => {
  loadOrders()
  timer = setInterval(() => { now.value = Date.now() }, 1000)
})
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.orders-page { min-height: 100vh; padding-bottom: 40px; }
.wrap { max-width: var(--container); margin: 0 auto; padding: 0 var(--gutter); }

.pagehead { padding: 40px 0 22px; }
.crumb { font-size: 11px; color: var(--color-ink-faint); letter-spacing: .08em; display: flex; gap: 8px; }
.crumb a { color: var(--color-ink-muted); }
.crumb i { font-style: normal; opacity: .5; }
h1 { margin-top: 14px; font-family: var(--font-display); font-size: var(--fs-h1); font-weight: 400; letter-spacing: .12em; }
.sub { margin-top: 8px; font-size: 11.5px; color: var(--color-ink-muted); letter-spacing: .08em; }

.tabs { display: flex; gap: 30px; border-bottom: 1px solid var(--color-line-strong); margin-bottom: 6px; }
.tabs a { padding-bottom: 13px; font-size: 12.5px; letter-spacing: .1em; color: var(--color-ink-muted); cursor: pointer; position: relative; }
.tabs a.on { color: var(--color-ink); }
.tabs a.on::after { content: ""; position: absolute; left: 0; right: 0; bottom: -1px; height: 1px; background: var(--color-gold); }
.tabs em { font-style: normal; font-size: 9px; color: var(--color-ink-faint); margin-left: 4px; }

.order { border: 1px solid var(--color-line); background: var(--color-surface); margin-top: 18px; }
.order-h { display: flex; align-items: center; gap: 24px; padding: 12px 20px; border-bottom: 1px solid var(--color-line); font-size: 10.5px; color: var(--color-ink-faint); letter-spacing: .08em; }
.order-h .no { color: var(--color-ink-soft); }
.order-h .st { margin-left: auto; }

.order-b { display: flex; padding: 4px 20px; }
.items { flex: 1; }
.oitem { display: flex; gap: 14px; align-items: center; padding: 13px 0; border-bottom: 1px solid var(--color-line); }
.oitem:last-child { border-bottom: 0; }
.oi-cover { width: 42px; height: 56px; flex: none; }
.oi-meta { flex: 1; min-width: 0; }
.oi-meta b { display: block; font-family: var(--font-display); font-size: 13px; font-weight: 400; letter-spacing: .05em; }
.oi-meta span { display: block; margin-top: 4px; font-size: 9.5px; color: var(--color-ink-faint); letter-spacing: .06em; }
.oi-meta .qty { color: var(--color-ink-muted); }
.oi-price { font-size: 12px; color: var(--color-ink-muted); }

.ototal { width: 180px; flex: none; border-left: 1px solid var(--color-line); margin: 12px 0 12px 20px; padding-left: 20px; display: flex; flex-direction: column; justify-content: center; }
.ototal .l { font-size: 9.5px; color: var(--color-ink-faint); letter-spacing: .1em; }
.ototal .v { margin-top: 6px; font-family: var(--font-display); font-size: 21px; color: var(--color-gold); }
.ototal .n { margin-top: 6px; font-size: 9.5px; color: var(--color-ink-faint); }

.order-f { display: flex; align-items: center; padding: 12px 20px; border-top: 1px solid var(--color-line); }
.hint { font-size: 9.5px; color: var(--color-ink-faint); letter-spacing: .06em; }
.hint.urgent { color: var(--color-gold); font-variant-numeric: tabular-nums; }
.hint.expired { color: var(--color-danger); }
.acts { margin-left: auto; display: flex; gap: 8px; }

.obtn {
  height: 30px; padding: 0 14px; border: 1px solid var(--color-line-strong); background: transparent;
  color: var(--color-ink-soft); font-family: inherit; font-size: 10.5px; letter-spacing: .1em; cursor: pointer;
}
.obtn:hover { color: var(--color-ink); border-color: var(--color-ink-muted); }
.obtn.gold { border-color: var(--color-gold-line); color: var(--color-gold); }
.obtn.gold:hover { background: var(--color-gold-soft); }
.expired-note { font-size: 9.5px; letter-spacing: .08em; color: var(--color-danger); margin-right: 4px; }

@media (max-width: 767px) {
  .order-b { flex-direction: column; }
  .ototal { width: auto; border-left: 0; margin: 0; padding-left: 0; padding-top: 12px; border-top: 1px solid var(--color-line); }
  .tabs { gap: 16px; overflow-x: auto; }
}
</style>
