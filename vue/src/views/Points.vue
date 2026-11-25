<template>
  <div class="points-page">
    <div class="hero-section">
      <h1 class="page-title">💰 积分中心</h1>
    </div>
    <div class="points-container">
      <div class="points-card">
        <span class="points-label">我的积分</span>
        <span class="points-value">{{ totalPoints }}</span>
        <span class="points-tip">100积分 = 1元</span>
      </div>
      <div class="section">
        <h2 class="section-title">积分规则</h2>
        <div class="rules-grid">
          <div class="rule-item"><span>🛒</span><span>购物=1积分/元</span></div>
          <div class="rule-item"><span>📅</span><span>签到=10积分</span></div>
          <div class="rule-item"><span>💬</span><span>评论=5积分</span></div>
          <div class="rule-item"><span>❤️</span><span>收藏=2积分</span></div>
        </div>
      </div>
      <div class="section">
        <h2 class="section-title">积分明细</h2>
        <div v-if="pointsLog.length > 0" class="log-list">
          <div v-for="log in pointsLog" :key="log.id" class="log-item">
            <span class="log-desc">{{ log.description }}</span>
            <span :class="['log-amount', log.type==='EARN'?'earn':'spend']">{{ log.type==='EARN'?'+':'-' }}{{ log.amount }}</span>
          </div>
        </div>
        <p v-else class="empty">暂无积分记录</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { pointsAPI } from '../api'

const totalPoints = ref(0)
const pointsLog = ref([])

const load = async () => {
  try {
    const [p, l] = await Promise.all([pointsAPI.getMyPoints(), pointsAPI.getLog()])
    totalPoints.value = p.data.points
    pointsLog.value = l.data
  } catch (e) {}
}

onMounted(() => load())
</script>

<style scoped>
.points-page { min-height: 100vh; background: var(--bg-page); }
.hero-section { background: linear-gradient(135deg, var(--primary), var(--primary-light)); padding: 20px; text-align: center; }
.page-title { color: white; font-family: var(--font-serif); font-size: 22px; margin: 0; }
.points-container { max-width: 800px; margin: 0 auto; padding: 16px; }
.points-card { background: linear-gradient(135deg, var(--primary), var(--primary-light)); border-radius: 16px; padding: 30px; text-align: center; color: white; margin-bottom: 20px; }
.points-label { display: block; font-size: 14px; opacity: 0.9; }
.points-value { display: block; font-size: 48px; font-weight: bold; margin: 8px 0; }
.points-tip { font-size: 13px; opacity: 0.7; }
.section-title { font-size: 18px; color: var(--text-primary); margin-bottom: 12px; }
.rules-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 20px; }
.rule-item { display: flex; align-items: center; gap: 8px; background: white; padding: 12px; border-radius: 10px; font-size: 14px; }
.log-list { background: white; border-radius: 12px; overflow: hidden; }
.log-item { display: flex; justify-content: space-between; padding: 12px 16px; border-bottom: 1px solid #f5f5f5; font-size: 14px; }
.log-amount { font-weight: bold; }
.log-amount.earn { color: var(--success); }
.log-amount.spend { color: var(--accent); }
.empty { text-align: center; padding: 40px; color: var(--text-secondary); }
</style>
