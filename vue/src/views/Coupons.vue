<template>
  <div class="coupons-page">
    <div class="hero-section">
      <h1 class="page-title">🎫 优惠券中心</h1>
    </div>
    <div class="coupons-container">
      <div class="section">
        <h2 class="section-title">可领取</h2>
        <div v-if="availableCoupons.length > 0" class="coupon-list">
          <div v-for="c in availableCoupons" :key="c.id" class="coupon-card">
            <div class="coupon-left">
              <span v-if="c.type==='FIXED'" class="coupon-value">¥{{ c.value }}</span>
              <span v-else-if="c.type==='PERCENT'" class="coupon-value">{{ (100-c.value*10)/10 }}折</span>
              <span v-else class="coupon-value">减¥{{ c.value }}</span>
              <span class="coupon-condition" v-if="c.minAmount>0">满{{ c.minAmount }}可用</span>
            </div>
            <div class="coupon-right">
              <h3>{{ c.name }}</h3>
              <button class="claim-btn" @click="claim(c.id)" :disabled="c.remainNum<=0">{{ c.remainNum>0?'领取':'已领完' }}</button>
            </div>
          </div>
        </div>
        <p v-else class="empty">暂无可用优惠券</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { couponAPI } from '../api'
import { ElMessage } from 'element-plus'

const availableCoupons = ref([])

const load = async () => {
  try {
    const res = await couponAPI.getAvailable()
    availableCoupons.value = res.data
  } catch (e) {}
}

const claim = async (id) => {
  try {
    await couponAPI.claim(id)
    ElMessage.success('领取成功')
    load()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '领取失败')
  }
}

onMounted(() => load())
</script>

<style scoped>
.coupons-page { min-height: 100vh; background: var(--bg-page); }
.hero-section { background: linear-gradient(135deg, var(--primary), var(--primary-light)); padding: 20px; text-align: center; }
.page-title { color: white; font-family: var(--font-serif); font-size: 22px; margin: 0; }
.coupons-container { max-width: 800px; margin: 0 auto; padding: 16px; }
.section-title { font-size: 18px; color: var(--text-primary); margin-bottom: 12px; }
.coupon-list { display: flex; flex-direction: column; gap: 12px; }
.coupon-card { display: flex; background: white; border-radius: 12px; overflow: hidden; }
.coupon-left { width: 120px; background: linear-gradient(135deg, var(--primary), var(--primary-light)); display: flex; flex-direction: column; align-items: center; justify-content: center; color: white; }
.coupon-value { font-size: 24px; font-weight: bold; }
.coupon-condition { font-size: 11px; opacity: 0.8; margin-top: 4px; }
.coupon-right { flex: 1; padding: 16px; display: flex; align-items: center; justify-content: space-between; }
.coupon-right h3 { font-size: 15px; margin: 0; }
.claim-btn { padding: 6px 16px; background: var(--primary); color: white; border: none; border-radius: 14px; font-size: 13px; cursor: pointer; }
.claim-btn:disabled { background: #ccc; }
.empty { text-align: center; padding: 40px; color: var(--text-secondary); }
</style>
