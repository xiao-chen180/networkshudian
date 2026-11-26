<template>
  <div class="pg">
    <div class="pg-wrap narrow">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><span>扫码支付</span></div>

      <div class="paycard">
        <div class="pc-head">
          <span class="k">{{ paymentMethod === 'alipay' ? 'ALIPAY' : 'WECHAT PAY' }}</span>
          <h1>{{ paymentMethod === 'alipay' ? '支付宝扫码支付' : '微信扫码支付' }}</h1>
          <p class="sub">请使用{{ paymentMethod === 'alipay' ? '支付宝' : '微信' }}扫描下方二维码完成支付</p>
        </div>

        <div class="amount">
          <span>应付金额</span>
          <b>¥{{ fmt(finalPrice) }}</b>
        </div>

        <div class="qr-wrap">
          <div class="qr" :class="{ paid: isProcessing }">
            <span v-for="(c, i) in qrCells" :key="i" :class="{ on: c }"></span>
          </div>
          <div class="qr-tip" v-if="!isProcessing">二维码有效期 {{ countdownText }}</div>
          <div class="qr-tip done" v-else>支付处理中…</div>
        </div>

        <div class="meta">
          <div><span>商品数量</span><b>{{ totalQuantity }} 件</b></div>
          <div><span>商品金额</span><b>¥{{ fmt(totalPrice) }}</b></div>
          <div><span>运费</span><b>{{ Number(shippingFee) > 0 ? `¥${fmt(shippingFee)}` : '¥0.00' }}</b></div>
          <div v-if="Number(route.query.couponDiscount) > 0"><span>优惠券</span><b class="disc">− ¥{{ fmt(route.query.couponDiscount) }}</b></div>
        </div>

        <div class="acts">
          <button class="btn-gold wide" :disabled="isProcessing || isCreatingOrder || !orderId" @click="completePayment">
            {{ isCreatingOrder ? '正在创建订单…' : isProcessing ? '处理中…' : '我已完成支付' }}
          </button>
          <button class="btn-line wide" :disabled="isProcessing" @click="goBackToCart">返回购物袋</button>
        </div>

        <div class="demo-note">教学演示：本项目未接入真实支付网关，点击「我已完成支付」将直接把订单状态改为已付款。</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { orderAPI, cartAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const orderId = ref('')
const totalQuantity = ref(0)
const totalPrice = ref(0)
const shippingFee = ref(0)
const finalPrice = ref(0)
const paymentMethod = ref('alipay')
const isProcessing = ref(false)
const isCreatingOrder = ref(false)
const seconds = ref(15 * 60)
let timer = null

const fmt = (n) => Number(n || 0).toFixed(2)
const countdownText = computed(() => {
  const m = String(Math.floor(seconds.value / 60)).padStart(2, '0')
  const s = String(seconds.value % 60).padStart(2, '0')
  return `${m}:${s}`
})

/* 生成稳定的伪二维码图案（不依赖外部图片） */
const qrCells = computed(() => {
  const size = 21
  const cells = []
  const seed = String(orderId.value || 'bookstore')
  let hash = 0
  for (let i = 0; i < seed.length; i++) hash = (hash * 31 + seed.charCodeAt(i)) >>> 0
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      const corner =
        (x < 7 && y < 7) || (x > size - 8 && y < 7) || (x < 7 && y > size - 8)
      if (corner) {
        const edge = x === 0 || y === 0 || x === 6 || y === 6 || x === size - 1 || y === size - 1
        const inner = (x >= 2 && x <= 4 && y >= 2 && y <= 4) ||
          (x >= size - 5 && x <= size - 3 && y >= 2 && y <= 4) ||
          (x >= 2 && x <= 4 && y >= size - 5 && y <= size - 3)
        cells.push(edge || inner)
      } else {
        hash = (hash * 1103515245 + 12345) >>> 0
        cells.push(((hash >> (x % 16)) & 1) === 1)
      }
    }
  }
  return cells
})

async function loadOrderInfo() {
  const q = route.query
  totalQuantity.value = parseInt(q.totalQuantity) || 0
  totalPrice.value = parseFloat(q.totalPrice) || 0
  shippingFee.value = parseFloat(q.shippingFee) || 0
  finalPrice.value = parseFloat(q.finalPrice) || 0
  paymentMethod.value = q.paymentMethod || 'alipay'

  if (q.orderId) {
    orderId.value = q.orderId
  } else {
    await createPendingOrder(q)
  }
}

async function createPendingOrder(orderData) {
  isCreatingOrder.value = true
  try {
    let items = []
    if (orderData.ids) {
      const cartIds = String(orderData.ids).split(',').map(Number)
      const res = await cartAPI.getCart()
      const all = res.data || []
      items = all.filter(i => cartIds.includes(i.id)).map(i => ({
        bookId: i.bookId,
        quantity: i.quantity,
        price: Number(i.price) * 0.95
      }))
    } else if (orderData.bookId) {
      items = [{ bookId: Number(orderData.bookId), quantity: Number(orderData.quantity) || 1, price: 0 }]
    }

    if (!items.length) {
      ElMessage.error('没有选中的商品')
      router.push('/cart')
      return
    }

    const res = await orderAPI.createPendingOrder({
      shippingAddress: orderData.address || '',
      phone: orderData.phone || '',
      receiverName: orderData.receiverName || '',
      items,
      totalPrice: Number(orderData.finalPrice) || totalPrice.value,
      userCouponId: orderData.couponId ? Number(orderData.couponId) : null
    })
    orderId.value = res.data.id
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建订单失败')
    router.push('/cart')
  } finally {
    isCreatingOrder.value = false
  }
}

async function removePurchasedCartItems() {
  const idsParam = route.query.ids
  if (!idsParam) return
  const ids = String(idsParam).split(',').map(Number).filter(Number.isFinite)
  if (!ids.length) return
  await Promise.allSettled(ids.map(id => cartAPI.removeFromCart(id)))
}

async function completePayment() {
  if (isProcessing.value || !orderId.value) return
  isProcessing.value = true
  try {
    await orderAPI.updateStatus(orderId.value, 'PAID')
    try { await removePurchasedCartItems() } catch {}
    ElMessage.success('支付成功')
    setTimeout(() => router.push('/orders'), 900)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '支付失败')
    isProcessing.value = false
  }
}

const goBackToCart = () => router.push('/cart')

onMounted(() => {
  loadOrderInfo()
  timer = setInterval(() => { if (seconds.value > 0) seconds.value -= 1 }, 1000)
})
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.pg-wrap.narrow { max-width: 560px; }
.paycard { border: 1px solid var(--color-line); background: var(--color-surface); padding: 30px 34px 26px; margin-top: 18px; }

.pc-head { text-align: center; }
.pc-head .k { font-size: 8.5px; letter-spacing: .34em; color: var(--color-gold); }
.pc-head h1 { margin-top: 12px; font-family: var(--font-display); font-size: 24px; font-weight: 400; letter-spacing: .12em; }
.pc-head .sub { margin-top: 9px; font-size: 10.5px; color: var(--color-ink-muted); letter-spacing: .04em; }

.amount { text-align: center; margin-top: 24px; padding: 18px 0; border-top: 1px solid var(--color-line); border-bottom: 1px solid var(--color-line); }
.amount span { display: block; font-size: 10px; letter-spacing: .2em; color: var(--color-ink-faint); }
.amount b { display: block; margin-top: 10px; font-family: var(--font-display); font-size: 40px; font-weight: 400; color: var(--color-gold); }

.qr-wrap { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 26px 0 20px; }
.qr {
  width: 176px; height: 176px; display: grid;
  grid-template-columns: repeat(21, 1fr); grid-template-rows: repeat(21, 1fr);
  padding: 10px; background: #f3efe7; border: 1px solid var(--color-gold-line);
  transition: opacity var(--dur-normal);
}
.qr span { background: transparent; }
.qr span.on { background: #14150f; }
.qr.paid { opacity: .4; }
.qr-tip { font-size: 10px; letter-spacing: .1em; color: var(--color-ink-faint); }
.qr-tip.done { color: var(--color-gold); }

.meta { display: flex; flex-direction: column; }
.meta > div { display: flex; justify-content: space-between; padding: 9px 0; border-bottom: 1px solid var(--color-line); }
.meta span { font-size: 10.5px; color: var(--color-ink-muted); }
.meta b { font-size: 11.5px; font-weight: 400; color: var(--color-ink-soft); }
.meta b.disc { color: var(--color-gold); }

.acts { display: flex; flex-direction: column; gap: 10px; margin-top: 22px; }
.wide { width: 100%; height: 42px; }
.demo-note { margin-top: 16px; font-size: 9.5px; line-height: 1.8; color: var(--color-ink-faint); text-align: center; }
</style>