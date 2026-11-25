<template>
  <div class="scan-payment-container">
    <div class="payment-header">
      <h2>扫码支付</h2>
    </div>

    <div class="payment-card">
      <div class="payment-info">
        <div class="method-badge" :class="paymentMethod">
          <span class="method-icon">{{ paymentMethod === 'alipay' ? '💙' : '💚' }}</span>
          <span class="method-text">{{ paymentMethod === 'alipay' ? '支付宝支付' : '微信支付' }}</span>
        </div>
        <div class="amount-section">
          <span class="amount-label">支付金额</span>
          <span class="amount-value">¥{{ formatPrice(finalPrice) }}</span>
        </div>
      </div>

      <div class="qr-container">
        <div class="qr-wrapper">
          <img
            v-if="paymentMethod === 'wechat'"
            src="/images/wx.jpg"
            alt="微信支付二维码"
            class="qr-code"
          />
          <img
            v-else
            src="/images/zfb.jpg"
            alt="支付宝二维码"
            class="qr-code"
          />
        </div>
        <div class="qr-tip">
          <span class="tip-icon">📱</span>
          <span class="tip-text">请使用{{ paymentMethod === 'alipay' ? '支付宝' : '微信' }}扫码支付</span>
        </div>
      </div>

      <div class="payment-hint">
        <div class="hint-item">
          <span class="hint-icon">⏱️</span>
          <span class="hint-text">支付有效期 15 分钟</span>
        </div>
        <div class="hint-item">
          <span class="hint-icon">🔔</span>
          <span class="hint-text">支付成功后将自动跳转</span>
        </div>
      </div>

      <button 
        class="complete-btn" 
        @click="completePayment"
        :disabled="isProcessing"
      >
        {{ isProcessing ? '处理中...' : '我已完成支付' }}
      </button>

      <button 
        class="back-cart-btn" 
        @click="goBackToCart"
      >
        <span class="btn-icon">🛒</span>
        <span>返回书篮</span>
      </button>
    </div>

    <div class="safe-tips">
      <div class="safe-icon">🛡️</div>
      <div class="safe-content">
        <span class="safe-title">安全提示</span>
        <span class="safe-text">请确认支付金额，保护您的账户安全</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderAPI, cartAPI } from '../api'

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

const formatPrice = (price) => {
  if (typeof price === 'number') {
    return price.toFixed(2)
  }
  return parseFloat(price).toFixed(2)
}

const loadOrderInfo = async () => {
  const orderData = route.query
  totalQuantity.value = parseInt(orderData.totalQuantity) || 0
  totalPrice.value = parseFloat(orderData.totalPrice) || 0
  shippingFee.value = parseFloat(orderData.shippingFee) || 0
  finalPrice.value = parseFloat(orderData.finalPrice) || 0
  paymentMethod.value = orderData.paymentMethod || 'alipay'

  // 如果没有orderId，说明是新订单，需要创建待支付订单
  if (!orderData.orderId) {
    await createPendingOrder(orderData)
  } else {
    orderId.value = orderData.orderId
  }
}

const createPendingOrder = async (orderData) => {
  isCreatingOrder.value = true
  try {
    let items = []

    // 支持两种方式：1. 购物车选中商品（ids参数）2. 直接购买（bookId参数）
    if (orderData.ids) {
      // 购物车选中商品
      const cartIds = orderData.ids.split(',').map(id => parseInt(id))
      const cartResponse = await cartAPI.getCart()
      const allItems = cartResponse.data || []
      const selectedItems = allItems.filter(item => cartIds.includes(item.id))
      items = selectedItems.map(item => ({
        bookId: item.bookId,
        quantity: item.quantity,
        price: (typeof item.price === 'number' ? item.price : parseFloat(item.price)) * 0.95
      }))
    } else if (orderData.bookId) {
      // 直接购买（BuyNow）
      items = [{
        bookId: parseInt(orderData.bookId),
        quantity: parseInt(orderData.quantity) || 1,
        price: 0 // 价格由后端计算
      }]
    }

    if (items.length === 0) {
      ElMessage.error('没有选中的商品')
      router.push('/cart')
      return
    }

    // 创建待支付订单
    const response = await orderAPI.createPendingOrder({
      shippingAddress: orderData.address || '',
      phone: orderData.phone || '',
      items: items,
      totalPrice: totalPrice.value
    })

    orderId.value = response.data.id
    console.log('创建待支付订单成功，订单ID:', orderId.value)
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error(error.response?.data?.message || '创建订单失败')
    router.push('/cart')
  } finally {
    isCreatingOrder.value = false
  }
}

const completePayment = async () => {
  if (isProcessing.value || !orderId.value) return

  isProcessing.value = true

  try {
    await orderAPI.updateStatus(orderId.value, 'PAID')

    // 支付成功后清空购物车
    try {
      await cartAPI.clearCart()
    } catch (e) {
      // 清空购物车失败不影响支付结果
    }

    ElMessage.success('支付成功！')
    setTimeout(() => {
      router.push('/orders')
    }, 1500)
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error(error.response?.data?.message || '支付失败')
    isProcessing.value = false
  }
}

const goBackToCart = () => {
  // 返回购物车时不删除商品，直接跳转
  router.push('/cart')
}

onMounted(() => {
  loadOrderInfo()
})
</script>

<style scoped>
.scan-payment-container {
  max-width: 480px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  background: #f5f5f5;
}

.payment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  margin-bottom: 20px;
}

.back-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
}

.back-icon {
  font-size: 20px;
  color: #333;
}

.payment-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.placeholder {
  width: 44px;
}

.payment-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
  animation: scanCardIn 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes scanCardIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.payment-info {
  text-align: center;
  margin-bottom: 24px;
}

.method-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  margin-bottom: 16px;
  animation: badgeSlideIn 0.4s ease forwards;
}

@keyframes badgeSlideIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.method-badge.alipay {
  background: linear-gradient(135deg, rgba(22, 119, 255, 0.1), rgba(9, 88, 217, 0.1));
}

.method-badge.wechat {
  background: linear-gradient(135deg, rgba(7, 193, 96, 0.1), rgba(6, 173, 86, 0.1));
}

.method-icon {
  font-size: 20px;
}

.method-text {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.amount-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.amount-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}

.amount-value {
  font-size: 36px;
  font-weight: bold;
  color: #8B4513;
  animation: amountPulse 1.5s ease-in-out infinite;
}

@keyframes amountPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.03); }
}

.qr-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

.qr-wrapper {
  width: 220px;
  height: 220px;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  animation: qrPulse 2s ease-in-out infinite;
}

@keyframes qrPulse {
  0%, 100% { box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1); }
  50% { box-shadow: 0 4px 25px rgba(139, 90, 43, 0.2); }
}

/* 扫描线效果 */
.qr-wrapper::after {
  content: '';
  position: absolute;
  left: 10%;
  width: 80%;
  height: 3px;
  background: linear-gradient(90deg, transparent, rgba(139, 90, 43, 0.6), transparent);
  animation: scanQr 2s ease-in-out infinite;
  z-index: 10;
}

@keyframes scanQr {
  0% { top: 10%; }
  100% { top: 90%; }
}

.qr-code {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  object-fit: contain;
}

.qr-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
}

.tip-icon {
  font-size: 16px;
}

.tip-text {
  font-size: 14px;
  color: #666;
}

.payment-hint {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 24px;
  padding: 16px;
  background: #FAFAFA;
  border-radius: 12px;
}

.hint-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.hint-icon {
  font-size: 14px;
}

.hint-text {
  font-size: 12px;
  color: #999;
}

.complete-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #8B4513, #A0522D);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.complete-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #A0522D, #8B4513);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(139, 69, 19, 0.4);
}

.complete-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.back-cart-btn {
  width: 100%;
  padding: 14px;
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.back-cart-btn:hover {
  background: #eee;
  border-color: #ccc;
}

.back-cart-btn .btn-icon {
  font-size: 18px;
}

.safe-tips {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
  padding: 16px;
  background: rgba(7, 193, 96, 0.1);
  border-radius: 12px;
}

.safe-icon {
  font-size: 24px;
}

.safe-content {
  display: flex;
  flex-direction: column;
}

.safe-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.safe-text {
  font-size: 12px;
  color: #666;
}
</style>