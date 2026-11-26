<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb">
        <router-link to="/">首页</router-link><i>/</i>
        <router-link to="/profile">个人中心</router-link><i>/</i>
        <router-link to="/orders">我的订单</router-link><i>/</i>
        <span>订单详情</span>
      </div>

      <div class="pg-head">
        <div>
          <h1>订单详情</h1>
          <div class="sub">
            订单号 {{ orderNo }}
            <template v-if="order.createdAt"> · 下单于 {{ formatTime(order.createdAt) }}</template>
          </div>
        </div>
        <div class="head-actions">
          <button class="btn-line sm" @click="$router.push('/orders')">返回订单列表</button>
        </div>
      </div>

      <!-- 状态条 -->
      <div class="status-bar" v-if="order.id">
        <div class="sb-main">
          <OrderStatusBadge :status="order.status" />
          <div class="sb-text">
            <b>{{ statusTitle }}</b>
            <span>{{ statusDesc }}</span>
          </div>
        </div>
        <div class="sb-acts">
          <template v-if="order.status === 'PENDING'">
            <span class="countdown" v-if="remainMs > 0">{{ remainText }} 后自动取消</span>
            <span class="countdown expired" v-else>支付已超时</span>
            <button class="btn-line sm" @click="cancelOrder">取消订单</button>
            <button class="btn-gold sm" v-if="remainMs > 0" @click="payNow">去支付</button>
          </template>
          <button class="btn-gold sm" v-if="order.status === 'SHIPPED'" @click="confirmReceive">确认收货</button>
          <button class="btn-line sm" v-if="order.status === 'COMPLETED'" @click="buyAgain">再次购买</button>
        </div>
      </div>

      <div class="detail-grid" v-if="order.id">
        <!-- 左栏 -->
        <div class="col-main">
          <section class="panel">
            <div class="panel-h">
              <h2>物流与进度</h2>
              <span class="hint" v-if="order.trackingNo">运单号 {{ order.trackingNo }}</span>
            </div>
            <ul class="timeline">
              <li v-for="(node, i) in timeline" :key="i" :class="{ done: node.done, current: node.current }">
                <span class="dot"></span>
                <div class="tl-body">
                  <div class="tl-head">
                    <b>{{ node.title }}</b>
                    <span class="tl-time">{{ node.time ? formatTime(node.time) : '—' }}</span>
                  </div>
                  <p v-if="node.desc">{{ node.desc }}</p>
                </div>
              </li>
            </ul>
          </section>

          <section class="panel">
            <div class="panel-h"><h2>商品明细</h2><span class="hint">共 {{ totalQty }} 件</span></div>
            <div class="goods">
              <div class="good" v-for="item in order.items || []" :key="item.id || item.bookId">
                <BookCover :src="item.imageUrl" :title="item.bookName" compact ratio="3:4" class="g-cover" />
                <div class="g-meta">
                  <router-link :to="`/books/${item.bookId}`" class="g-name">{{ item.bookName }}</router-link>
                  <span class="g-author">{{ item.author || '书香阁' }}</span>
                </div>
                <div class="g-price">¥{{ fmt(item.price) }}</div>
                <div class="g-qty">× {{ item.quantity }}</div>
                <div class="g-sum">¥{{ fmt(Number(item.price) * Number(item.quantity)) }}</div>
              </div>
            </div>
            <div class="amounts">
              <div class="arow"><span>商品合计</span><b>¥{{ fmt(goodsTotal) }}</b></div>
              <div class="arow" v-if="Number(order.couponDiscount) > 0">
                <span>优惠券抵扣</span><b class="minus">− ¥{{ fmt(order.couponDiscount) }}</b>
              </div>
              <div class="arow total"><span>应付金额</span><b class="gold">¥{{ fmt(order.totalPrice) }}</b></div>
            </div>
          </section>
        </div>

        <!-- 右栏 -->
        <aside class="col-side">
          <section class="panel">
            <div class="panel-h"><h2>收货信息</h2></div>
            <div class="info-row"><span>收货人</span><b>{{ receiverName }}</b></div>
            <div class="info-row"><span>联系电话</span><b>{{ order.phone || '—' }}</b></div>
            <div class="info-row block"><span>收货地址</span><b>{{ order.shippingAddress || '—' }}</b></div>
          </section>

          <section class="panel">
            <div class="panel-h"><h2>订单信息</h2></div>
            <div class="info-row"><span>订单号</span><b class="mono">{{ orderNo }}</b></div>
            <div class="info-row"><span>下单时间</span><b>{{ formatTime(order.createdAt) }}</b></div>
            <div class="info-row" v-if="order.paidAt"><span>支付时间</span><b>{{ formatTime(order.paidAt) }}</b></div>
            <div class="info-row" v-if="order.shippedAt"><span>发货时间</span><b>{{ formatTime(order.shippedAt) }}</b></div>
            <div class="info-row"><span>支付方式</span><b>在线支付（演示）</b></div>
          </section>

          <section class="panel tip">
            <div class="panel-h"><h2>需要帮助？</h2></div>
            <p>· 48 小时内发货，可在订单列表查看物流</p>
            <p>· 7 天无理由退换，破损可申请补发</p>
            <p>· 如需售后请联系客服或在订单列表发起</p>
          </section>
        </aside>
      </div>

      <EmptyState v-else-if="!loading" icon="◌" title="订单不存在" description="该订单可能已被删除，或不属于当前账号" action-text="回到订单列表" @action="$router.push('/orders')" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import BookCover from '../components/common/BookCover.vue'
import OrderStatusBadge from '../components/common/OrderStatusBadge.vue'
import EmptyState from '../components/common/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const order = ref({})
const loading = ref(true)
const now = ref(Date.now())
let timer = null
const PAY_WINDOW = 30 * 60 * 1000

const orderNo = computed(() => (order.value.id ? 'SH' + String(order.value.id).padStart(8, '0') : '—'))
const totalQty = computed(() => (order.value.items || []).reduce((s, i) => s + Number(i.quantity || 0), 0))
const goodsTotal = computed(() =>
  (order.value.items || []).reduce((s, i) => s + Number(i.price) * Number(i.quantity), 0)
)
const receiverName = computed(() => order.value.receiverName || '—')

const remainMs = computed(() => {
  if (order.value.status !== 'PENDING' || !order.value.createdAt) return 0
  return Math.max(0, new Date(order.value.createdAt).getTime() + PAY_WINDOW - now.value)
})
const remainText = computed(() => {
  const ms = remainMs.value
  const m = String(Math.floor(ms / 60000)).padStart(2, '0')
  const s = String(Math.floor((ms % 60000) / 1000)).padStart(2, '0')
  return `${m}:${s}`
})

const statusTitle = computed(() => ({
  PENDING: '等待付款',
  PAID: '已付款，待发货',
  SHIPPED: '已发货，运输中',
  COMPLETED: '交易完成',
  CANCELLED: '订单已取消'
}[order.value.status] || ''))

const statusDesc = computed(() => ({
  PENDING: '请在倒计时结束前完成支付，否则订单将自动取消并释放库存。',
  PAID: '仓库正在配货，预计 48 小时内发出。',
  SHIPPED: '包裹已发出，请留意物流信息并保持电话畅通。',
  COMPLETED: '感谢购买，写书评可得 5 积分。',
  CANCELLED: '订单已关闭，库存与优惠券已退回。'
}[order.value.status] || ''))

const timeline = computed(() => {
  const o = order.value
  if (o.status === 'CANCELLED') {
    return [
      { title: '提交订单', time: o.createdAt, desc: '订单已生成', done: true },
      { title: '订单已取消', time: o.cancelledAt, desc: '库存与优惠券已退回', done: true, current: true }
    ]
  }
  const nodes = [
    { title: '提交订单', time: o.createdAt, desc: '等待付款', done: !!o.createdAt },
    { title: '完成付款', time: o.paidAt, desc: '支付成功，等待发货', done: !!o.paidAt },
    { title: '商家发货', time: o.shippedAt, desc: o.trackingNo ? `运单号 ${o.trackingNo}` : '等待发货', done: !!o.shippedAt },
    { title: '确认收货', time: o.completedAt, desc: '交易完成', done: !!o.completedAt }
  ]
  const currentIndex = nodes.reduce((acc, n, i) => (n.done ? i : acc), 0)
  nodes[currentIndex].current = true
  return nodes
})

function formatTime(t) {
  if (!t) return '—'
  const d = new Date(t)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}
const fmt = (v) => Number(v || 0).toFixed(2)

async function load() {
  loading.value = true
  try {
    const res = await orderAPI.getById(route.params.id)
    order.value = res.data || {}
  } catch {
    order.value = {}
  } finally {
    loading.value = false
  }
}

function payNow() {
  router.push({
    path: '/payment',
    query: { orderId: order.value.id, totalQuantity: totalQty.value, totalPrice: goodsTotal.value, finalPrice: order.value.totalPrice, shippingFee: 0 }
  })
}

async function cancelOrder() {
  try {
    await ElMessageBox.confirm('取消后库存与优惠券将退回，确认取消该订单？', '取消订单', { type: 'warning' })
  } catch { return }
  try {
    await orderAPI.updateStatus(order.value.id, 'CANCELLED')
    ElMessage.success('订单已取消')
    load()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '取消失败')
  }
}

async function confirmReceive() {
  try {
    await orderAPI.updateStatus(order.value.id, 'COMPLETED')
    ElMessage.success('已确认收货')
    load()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

function buyAgain() {
  const first = order.value.items && order.value.items[0]
  if (first) router.push(`/books/${first.bookId}`)
  else router.push('/books')
}

onMounted(() => {
  load()
  timer = setInterval(() => { now.value = Date.now() }, 1000)
})
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.pg-head { display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; }
.head-actions { padding-bottom: 18px; }

.status-bar {
  display: flex; align-items: center; justify-content: space-between; gap: 20px;
  border: 1px solid var(--color-gold-line); background: linear-gradient(120deg, rgba(211,174,106,.10), transparent 65%), var(--color-surface);
  padding: 20px 22px; margin-bottom: 18px;
}
.sb-main { display: flex; align-items: center; gap: 16px; }
.sb-text b { display: block; font-family: var(--font-display); font-size: 17px; font-weight: 400; letter-spacing: .1em; }
.sb-text span { display: block; margin-top: 6px; font-size: 10.5px; color: var(--color-ink-muted); letter-spacing: .04em; }
.sb-acts { display: flex; align-items: center; gap: 10px; }
.countdown { font-size: 11px; color: var(--color-gold); font-variant-numeric: tabular-nums; letter-spacing: .06em; }
.countdown.expired { color: var(--color-danger); }

.detail-grid { display: grid; grid-template-columns: 1fr 320px; gap: 18px; align-items: start; }
.col-main, .col-side { display: flex; flex-direction: column; gap: 18px; }

/* 时间轴 */
.timeline { padding: 6px 0 2px; }
.timeline li { display: flex; gap: 14px; padding: 0 0 22px; position: relative; }
.timeline li:last-child { padding-bottom: 0; }
.timeline li::before {
  content: ""; position: absolute; left: 4px; top: 14px; bottom: 0; width: 1px; background: var(--color-line);
}
.timeline li:last-child::before { display: none; }
.timeline .dot {
  width: 9px; height: 9px; border-radius: 50%; flex: none; margin-top: 5px;
  border: 1px solid var(--color-line-strong); background: var(--color-bg); z-index: 1;
}
.timeline li.done .dot { background: var(--color-gold); border-color: var(--color-gold); }
.timeline li.current .dot { box-shadow: 0 0 0 3px rgba(211,174,106,.18); }
.tl-body { flex: 1; }
.tl-head { display: flex; align-items: baseline; justify-content: space-between; gap: 12px; }
.tl-head b { font-size: 12.5px; font-weight: 400; letter-spacing: .06em; color: var(--color-ink-soft); }
.timeline li.done .tl-head b { color: var(--color-ink); }
.tl-time { font-size: 9.5px; color: var(--color-ink-faint); font-variant-numeric: tabular-nums; }
.tl-body p { margin-top: 5px; font-size: 10px; color: var(--color-ink-faint); letter-spacing: .04em; }

/* 商品 */
.goods { border-top: 1px solid var(--color-line); }
.good {
  display: grid; grid-template-columns: 44px 1fr 80px 56px 88px; gap: 14px; align-items: center;
  padding: 13px 0; border-bottom: 1px solid var(--color-line);
}
.g-cover { width: 44px; height: 61px; }
.g-name { display: block; font-family: var(--font-display); font-size: 13px; letter-spacing: .05em; }
.g-name:hover { color: var(--color-gold); }
.g-author { display: block; margin-top: 4px; font-size: 9.5px; color: var(--color-ink-faint); }
.g-price, .g-qty { font-size: 11px; color: var(--color-ink-muted); text-align: right; font-variant-numeric: tabular-nums; }
.g-sum { font-size: 12px; color: var(--color-gold); text-align: right; font-variant-numeric: tabular-nums; }

.amounts { margin-top: 16px; padding-top: 14px; border-top: 1px solid var(--color-line-strong); display: flex; flex-direction: column; gap: 9px; }
.arow { display: flex; justify-content: space-between; font-size: 11px; color: var(--color-ink-muted); letter-spacing: .04em; }
.arow b { color: var(--color-ink-soft); font-weight: 400; font-variant-numeric: tabular-nums; }
.arow .minus { color: var(--color-sage); }
.arow.total { margin-top: 4px; padding-top: 10px; border-top: 1px solid var(--color-line); align-items: baseline; }
.arow.total span { font-size: 11px; color: var(--color-ink-soft); }
.arow.total .gold { font-family: var(--font-display); font-size: 22px; color: var(--color-gold); }

/* 右侧信息 */
.info-row { display: flex; justify-content: space-between; gap: 14px; padding: 7px 0; font-size: 11px; }
.info-row span { color: var(--color-ink-faint); letter-spacing: .04em; flex: none; }
.info-row b { font-weight: 400; color: var(--color-ink-soft); text-align: right; word-break: break-all; }
.info-row.block { flex-direction: column; align-items: flex-start; gap: 6px; }
.info-row.block b { text-align: left; line-height: 1.8; }
.panel.tip p { font-size: 10.5px; color: var(--color-ink-faint); line-height: 2; letter-spacing: .03em; }
.mono { font-variant-numeric: tabular-nums; letter-spacing: .06em; }

@media (max-width: 1024px) {
  .detail-grid { grid-template-columns: 1fr; }
  .status-bar { flex-direction: column; align-items: flex-start; }
}
</style>
