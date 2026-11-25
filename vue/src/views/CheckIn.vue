<template>
  <div class="checkin-page">
    <div class="hero-section">
      <h1 class="page-title">📅 每日签到</h1>
    </div>
    <div class="checkin-container">
      <div class="checkin-card">
        <div class="consecutive">{{ status.consecutiveDays || 0 }}<span>天</span></div>
        <p class="consecutive-label">连续签到</p>
        <button class="checkin-btn" :class="{ checked: status.checkedInToday }" @click="doCheckIn" :disabled="status.checkedInToday">
          {{ status.checkedInToday ? '✅ 今日已签到' : '立即签到' }}
        </button>
      </div>

      <!-- 每日抽奖入口 -->
      <div class="lottery-entry" v-if="(status.consecutiveDays || 0) >= 7" @click="showLottery = true">
        <span class="lottery-icon">🎰</span>
        <div class="lottery-info">
          <h3>每日抽奖</h3>
          <p>连续签到7天，获得抽奖机会！</p>
        </div>
        <span class="lottery-arrow">→</span>
      </div>

      <div class="rewards">
        <h3>连续签到奖励</h3>
        <div class="reward-grid">
          <div class="reward-item" :class="{ done: (status.consecutiveDays||0)>=7 }"><strong>7天</strong><span>+50积分</span></div>
          <div class="reward-item" :class="{ done: (status.consecutiveDays||0)>=15 }"><strong>15天</strong><span>+150积分</span></div>
          <div class="reward-item" :class="{ done: (status.consecutiveDays||0)>=30 }"><strong>30天</strong><span>+500积分</span></div>
        </div>
      </div>
    </div>

    <!-- 抽奖弹窗 -->
    <DailyLottery
      :visible="showLottery"
      :consecutive-days="status.consecutiveDays || 0"
      @close="showLottery = false"
      @prize="onPrize"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { checkInAPI } from '../api'
import { ElMessage } from 'element-plus'
import DailyLottery from '../components/DailyLottery.vue'

const status = ref({ checkedInToday: false, consecutiveDays: 0 })
const showLottery = ref(false)

const onPrize = (prize) => {
  ElMessage.success(`恭喜获得：${prize.name}`)
}

const load = async () => {
  try {
    const res = await checkInAPI.getStatus()
    status.value = res.data
  } catch (e) {}
}

const doCheckIn = async () => {
  try {
    const res = await checkInAPI.checkIn()
    status.value = res.data
    ElMessage.success('签到成功！+10积分')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '签到失败')
  }
}

onMounted(() => load())
</script>

<style scoped>
.checkin-page { min-height: 100vh; background: var(--bg-page); }
.hero-section { background: linear-gradient(135deg, var(--primary), var(--primary-light)); padding: 20px; text-align: center; }
.page-title { color: white; font-family: var(--font-serif); font-size: 22px; margin: 0; }
.checkin-container { max-width: 500px; margin: 0 auto; padding: 16px; }
.checkin-card { background: white; border-radius: 16px; padding: 40px; text-align: center; margin-bottom: 20px; }
.consecutive { font-size: 64px; font-weight: bold; color: var(--accent); }
.consecutive span { font-size: 18px; color: var(--text-secondary); }
.consecutive-label { color: var(--text-secondary); margin: 8px 0 24px; }
.checkin-btn { padding: 14px 48px; background: var(--primary); color: white; border: none; border-radius: 24px; font-size: 16px; cursor: pointer; }
.checkin-btn.checked { background: var(--success); }
.checkin-btn:disabled { opacity: 0.7; cursor: not-allowed; }
.rewards { background: white; border-radius: 16px; padding: 20px; }
.rewards h3 { font-size: 16px; margin-bottom: 12px; }
.reward-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.reward-item { text-align: center; padding: 16px 8px; background: #f9f9f9; border-radius: 10px; }
.reward-item.done { background: #f6ffed; }
.reward-item strong { display: block; font-size: 18px; color: var(--text-primary); }
.reward-item span { font-size: 12px; color: var(--accent); }
</style>
