<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><router-link to="/cart">购物袋</router-link><i>/</i><span>支付</span></div>
      <div class="pg-head">
        <div>
          <h1>支付</h1>
          <div class="sub">订单提交后 30 分钟内完成支付，超时自动取消</div>
        </div>
      </div>

      <div class="steps">
        <div class="step done"><b>✓</b>购物袋</div>
        <div class="stepline"></div>
        <div class="step done"><b>✓</b>收货地址</div>
        <div class="stepline"></div>
        <div class="step on"><b>3</b>支付</div>
        <div class="stepline"></div>
        <div class="step"><b>4</b>完成</div>
      </div>

      <div class="layout">
        <div class="main">
          <section class="panel">
            <div class="panel-h"><h3>订单信息</h3></div>
            <div class="info">
              <div><span>商品数量</span><b>{{ totalQuantity }} 件</b></div>
              <div><span>商品金额</span><b>¥{{ fmt(totalPrice) }}</b></div>
              <div><span>运费</span><b>{{ Number(shippingFee) > 0 ? `¥${fmt(shippingFee)}` : '¥0.00' }}</b></div>
              <div v-if="Number(couponDiscount) > 0" class="disc"><span>优惠券抵扣</span><b>− ¥{{ fmt(couponDiscount) }}</b></div>
              <div class="addr" v-if="address"><span>收货地址</span><b>{{ address }}</b></div>
            </div>
          </section>

          <section class="panel">
            <div class="panel-h"><h3>支付方式</h3></div>
            <div class="methods">
              <div class="method" :class="{ on: paymentMethod === 'alipay' }" @click="paymentMethod = 'alipay'">
                <span class="radio"></span>
                <div class="mm"><b>支付宝</b><span>扫码支付，支持花呗</span></div>
                <span class="badge">推荐</span>
              </div>
              <div class="method" :class="{ on: paymentMethod === 'wechat' }" @click="paymentMethod = 'wechat'">
                <span class="radio"></span>
                <div class="mm"><b>微信支付</b><span>微信扫码支付</span></div>
              </div>
            </div>
            <div class="notice-line">本项目为教学演示，支付为模拟流程：点击「去支付」后可在扫码页直接完成支付。</div>
          </section>
        </div>

        <aside class="aside">
          <div class="summary">
            <div class="l">应付金额</div>
            <div class="v">¥{{ fmt(finalPrice) }}</div>
            <button class="submit" :disabled="!canPay" @click="goPay">
              去支付
            </button>
            <button class="back" @click="router.push('/cart')">返回购物袋</button>
            <div class="tip">支付成功后可在订单中心查看物流与状态</div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const totalQuantity = ref(0)
const totalPrice = ref(0)
const shippingFee = ref(0)
const finalPrice = ref(0)
const couponDiscount = ref(0)
const address = ref('')
const paymentMethod = ref('alipay')

const canPay = computed(() => finalPrice.value >= 0 && (route.query.ids || route.query.bookId))
const fmt = (n) => Number(n || 0).toFixed(2)

onMounted(() => {
  const q = route.query
  totalQuantity.value = parseInt(q.totalQuantity) || 0
  totalPrice.value = parseFloat(q.totalPrice) || 0
  shippingFee.value = parseFloat(q.shippingFee) || 0
  finalPrice.value = parseFloat(q.finalPrice) || 0
  couponDiscount.value = parseFloat(q.couponDiscount) || 0
  address.value = q.address || ''
})

function goPay() {
  router.push({
    path: '/scan-payment',
    query: { ...route.query, paymentMethod: paymentMethod.value }
  })
}
</script>

<style scoped>
.steps { display: flex; align-items: center; margin: 8px 0 28px; border-top: 1px solid var(--color-line); padding-top: 22px; }
.step { display: flex; align-items: center; gap: 9px; font-size: 11px; letter-spacing: .1em; color: var(--color-ink-faint); }
.step b {
  width: 20px; height: 20px; border-radius: 50%; border: 1px solid var(--color-line-strong);
  display: flex; align-items: center; justify-content: center; font-size: 9px; font-weight: 400; font-family: var(--font-display);
}
.step.done { color: var(--color-ink-muted); }
.step.done b { border-color: var(--color-gold-line); color: var(--color-gold); }
.step.on { color: var(--color-ink); }
.step.on b { background: var(--color-gold); border-color: var(--color-gold); color: #14150f; }
.stepline { flex: 1; height: 1px; background: var(--color-line); margin: 0 18px; }

.layout { display: flex; gap: 36px; align-items: flex-start; }
.main { width: 700px; flex: none; }
.aside { flex: 1; }

.info { display: flex; flex-direction: column; }
.info > div { display: flex; justify-content: space-between; align-items: baseline; padding: 10px 0; border-bottom: 1px solid var(--color-line); gap: 24px; }
.info > div:last-child { border-bottom: 0; }
.info span { font-size: 11px; color: var(--color-ink-muted); letter-spacing: .05em; flex: none; }
.info b { font-size: 12px; font-weight: 400; color: var(--color-ink-soft); text-align: right; }
.info .disc b { color: var(--color-gold); }
.info .addr b { font-size: 11px; line-height: 1.8; }

.methods { display: flex; flex-direction: column; gap: 10px; }
.method { display: flex; align-items: center; gap: 12px; border: 1px solid var(--color-line); padding: 14px 16px; cursor: pointer; }
.method.on { border-color: var(--color-gold-line); background: rgba(211,174,106,.04); }
.radio { width: 13px; height: 13px; border-radius: 50%; border: 1px solid var(--color-line-strong); flex: none; position: relative; }
.method.on .radio { border-color: var(--color-gold); }
.method.on .radio::after { content: ""; position: absolute; inset: 3px; border-radius: 50%; background: var(--color-gold); }
.mm b { display: block; font-size: 12.5px; font-weight: 400; color: var(--color-ink); letter-spacing: .06em; }
.mm span { display: block; margin-top: 4px; font-size: 10px; color: var(--color-ink-faint); }
.badge { margin-left: auto; font-size: 9px; letter-spacing: .12em; color: var(--color-gold); border: 1px solid var(--color-gold-line); padding: 2px 6px; }
.notice-line { margin-top: 14px; font-size: 10px; color: var(--color-ink-faint); line-height: 1.8; letter-spacing: .03em; }

.summary {
  border: 1px solid var(--color-gold-line); background: var(--color-surface); padding: 26px 24px;
  position: sticky; top: 92px; text-align: center;
}
.summary .l { font-size: 10px; letter-spacing: .2em; color: var(--color-ink-faint); }
.summary .v { margin-top: 12px; font-family: var(--font-display); font-size: 38px; color: var(--color-gold); }
.submit {
  width: 100%; height: 44px; margin-top: 20px; border: 1px solid var(--color-gold);
  background: var(--color-gold); color: #14150f; font-family: inherit; font-size: 12.5px; letter-spacing: .2em; cursor: pointer;
}
.submit:disabled { opacity: .4; cursor: not-allowed; }
.back {
  width: 100%; height: 36px; margin-top: 10px; border: 1px solid var(--color-line-strong);
  background: transparent; color: var(--color-ink-muted); font-family: inherit; font-size: 11px; letter-spacing: .12em; cursor: pointer;
}
.tip { margin-top: 14px; font-size: 9.5px; color: var(--color-ink-faint); line-height: 1.8; }

@media (max-width: 1024px) {
  .layout { flex-direction: column; }
  .main, .aside { width: 100%; }
  .summary { position: static; }
}
</style>