<template>
  <div class="payment-container">
    <div class="payment-header">
      <div class="header-decoration">
        <span class="decor-line"></span>
        <span class="decor-dot"></span>
        <span class="decor-line"></span>
      </div>
      <h2>选择支付方式</h2>
      <div class="header-decoration">
        <span class="decor-line"></span>
        <span class="decor-dot"></span>
        <span class="decor-line"></span>
      </div>
    </div>

    <div class="payment-content animate-fade-up">
      <!-- 扫描线效果 -->
      <div class="scan-line"></div>

      <div class="order-summary">
        <h3>📋 订单信息</h3>
        <div class="summary-item">
          <span class="label">订单号</span>
          <span class="value">{{ orderId }}</span>
        </div>
        <div class="summary-item">
          <span class="label">商品数量</span>
          <span class="value">{{ totalQuantity }} 件</span>
        </div>
        <div class="summary-item">
          <span class="label">商品总额</span>
          <span class="value">¥{{ formatPrice(totalPrice) }}</span>
        </div>
        <div class="summary-item">
          <span class="label">运费</span>
          <span class="value">¥{{ formatPrice(shippingFee) }}</span>
        </div>
        <div class="summary-item total">
          <span class="label">应付金额</span>
          <span class="value highlight">¥{{ formatPrice(finalPrice) }}</span>
        </div>
      </div>

      <div class="payment-methods">
        <h3>💳 支付方式</h3>
        <div
          class="payment-method"
          :class="{ selected: paymentMethod === 'alipay' }"
          @click="paymentMethod = 'alipay'"
        >
          <div class="method-icon alipay">💙</div>
          <div class="method-info">
            <span class="method-name">支付宝</span>
            <span class="method-desc">推荐使用支付宝支付</span>
          </div>
          <div class="method-check" v-if="paymentMethod === 'alipay'">✓</div>
        </div>

        <div
          class="payment-method"
          :class="{ selected: paymentMethod === 'wechat' }"
          @click="paymentMethod = 'wechat'"
        >
          <div class="method-icon wechat">💚</div>
          <div class="method-info">
            <span class="method-name">微信支付</span>
            <span class="method-desc">安全快捷的支付方式</span>
          </div>
          <div class="method-check" v-if="paymentMethod === 'wechat'">✓</div>
        </div>
      </div>

      <div class="payment-tips">
        <div class="tip-item">
          <span class="tip-icon">🔒</span>
          <span class="tip-text">支付安全有保障</span>
        </div>
        <div class="tip-item">
          <span class="tip-icon">⚡</span>
          <span class="tip-text">支付后立即发货</span>
        </div>
        <div class="tip-item">
          <span class="tip-icon">📞</span>
          <span class="tip-text">7天无理由退换</span>
        </div>
      </div>

      <div class="payment-actions">
        <button class="btn btn-back" @click="goBack">返回</button>
        <button
          class="btn btn-pay"
          @click="handlePayment"
          :disabled="isProcessing"
        >
          {{ isProcessing ? '处理中...' : `确认支付 ¥${formatPrice(finalPrice)}` }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const orderId = ref('')
const totalQuantity = ref(0)
const totalPrice = ref(0)
const shippingFee = ref(0)
const finalPrice = ref(0)
const paymentMethod = ref('alipay')
const isProcessing = ref(false)

const formatPrice = (price) => {
  if (typeof price === 'number') {
    return price.toFixed(2)
  }
  return parseFloat(price).toFixed(2)
}

const loadOrderInfo = () => {
  const orderData = route.query
  orderId.value = orderData.orderId || 'ORDER' + Date.now()
  totalQuantity.value = parseInt(orderData.totalQuantity) || 0
  totalPrice.value = parseFloat(orderData.totalPrice) || 0
  shippingFee.value = parseFloat(orderData.shippingFee) || 0
  finalPrice.value = parseFloat(orderData.finalPrice) || 0
}

const handlePayment = () => {
  if (isProcessing.value) return
  
  isProcessing.value = true
  
  const query = {
    ...route.query,
    paymentMethod: paymentMethod.value
  }
  
  router.push({
    path: '/scan-payment',
    query
  })
  
  isProcessing.value = false
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadOrderInfo()
})
</script>

<style scoped>
.payment-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 30px 20px;
}

.payment-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
}

.header-decoration {
  display: flex;
  align-items: center;
  gap: 8px;
}

.decor-line {
  width: 60px;
  height: 2px;
  background: linear-gradient(to right, transparent, #8B4513);
}

.decor-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #8B4513;
}

.payment-header h2 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.payment-content {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 28px;
  position: relative;
  overflow: hidden;
  opacity: 0;
}

/* 扫描线效果 */
.scan-line {
  position: absolute;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, transparent, rgba(139, 69, 19, 0.5), transparent);
  animation: scanLine 3s ease-in-out infinite;
  pointer-events: none;
  z-index: 10;
}

@keyframes scanLine {
  0% { top: -5%; }
  100% { top: 105%; }
}

.order-summary {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #eee;
}

.order-summary h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #333;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.summary-item .label {
  color: #666;
}

.summary-item .value {
  color: #333;
  font-weight: 500;
}

.summary-item.total {
  border-top: 1px solid #eee;
  margin-top: 8px;
  padding-top: 16px;
}

.summary-item.total .label {
  font-size: 16px;
  font-weight: 600;
}

.summary-item.total .value {
  font-size: 20px;
  color: #8B4513;
  font-weight: bold;
}

.payment-methods {
  margin-bottom: 24px;
}

.payment-methods h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #333;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  border: 2px solid #eee;
  border-radius: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.payment-method::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, transparent, rgba(139, 69, 19, 0.03));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.payment-method:hover {
  border-color: #8B4513;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(139, 69, 19, 0.1);
}

.payment-method:hover::before {
  opacity: 1;
}

.payment-method.selected {
  border-color: #8B4513;
  background: #FAF8F5;
  box-shadow: 0 2px 12px rgba(139, 69, 19, 0.15);
}

.method-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  transition: transform 0.3s var(--ease-bounce);
}

.payment-method:hover .method-icon {
  transform: scale(1.1);
}

.method-icon.alipay {
  background: linear-gradient(135deg, #1677FF, #0958D9);
  color: #fff;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.method-icon.wechat {
  background: linear-gradient(135deg, #07C160, #06AD56);
  color: #fff;
  box-shadow: 0 4px 12px rgba(7, 193, 96, 0.3);
}

.method-info {
  flex: 1;
}

.method-name {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.method-desc {
  display: block;
  font-size: 13px;
  color: #999;
}

.method-check {
  font-size: 24px;
  color: #8B4513;
  font-weight: bold;
}

.payment-tips {
  display: flex;
  justify-content: space-around;
  margin-bottom: 24px;
  padding: 16px;
  background: #FAFAFA;
  border-radius: 8px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
}

.tip-icon {
  font-size: 16px;
}

.payment-actions {
  display: flex;
  gap: 16px;
}

.btn {
  flex: 1;
  padding: 14px 32px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.btn-back {
  background: #f0f0f0;
  color: #666;
}

.btn-back:hover {
  background: #e0e0e0;
}

.btn-pay {
  background: linear-gradient(135deg, #8B4513, #A0522D);
  color: #fff;
  position: relative;
  overflow: hidden;
}

.btn-pay::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -60%;
  width: 40%;
  height: 200%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
  transform: rotate(25deg);
  transition: left 0.5s ease;
}

.btn-pay:hover:not(:disabled) {
  background: linear-gradient(135deg, #A0522D, #8B4513);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(139, 69, 19, 0.4);
}

.btn-pay:hover:not(:disabled)::after {
  left: 120%;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>