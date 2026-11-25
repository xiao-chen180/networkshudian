<template>
  <div class="lottery-modal" v-if="visible" @click.self="close">
    <div class="lottery-content">
      <div class="lottery-header">
        <h3>🎰 每日抽奖</h3>
        <button class="close-btn" @click="close">×</button>
      </div>

      <div class="lottery-body">
        <div class="lottery-wheel" :class="{ spinning: isSpinning }">
          <div class="wheel-inner" :style="{ transform: `rotate(${rotateDegree}deg)` }">
            <div v-for="(prize, index) in prizes" :key="index" class="prize-item" :style="{ transform: `rotate(${index * 45}deg)` }">
              <span class="prize-icon">{{ prize.icon }}</span>
              <span class="prize-name">{{ prize.name }}</span>
            </div>
          </div>
          <div class="wheel-center" @click="spin">
            <span>抽奖</span>
          </div>
        </div>

        <div class="lottery-info">
          <p>连续签到 {{ consecutiveDays }} 天</p>
          <p v-if="consecutiveDays >= 7">可抽奖次数：{{ lotteryCount }} 次</p>
          <p v-else class="hint">连续签到7天可获得抽奖机会</p>
        </div>
      </div>

      <div class="lottery-result" v-if="result">
        <p>🎉 恭喜获得：{{ result.name }}</p>
        <button class="confirm-btn" @click="close">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  visible: Boolean,
  consecutiveDays: { type: Number, default: 0 }
})

const emit = defineEmits(['close', 'prize'])

const isSpinning = ref(false)
const rotateDegree = ref(0)
const result = ref(null)

const prizes = [
  { name: '5积分', icon: '💰', type: 'points', value: 5 },
  { name: '10积分', icon: '💰', type: 'points', value: 10 },
  { name: '50积分', icon: '💰', type: 'points', value: 50 },
  { name: '8折券', icon: '🎫', type: 'coupon', value: 8 },
  { name: '免单券', icon: '🎁', type: 'coupon', value: 100 },
  { name: '100积分', icon: '💰', type: 'points', value: 100 },
  { name: '9折券', icon: '🎫', type: 'coupon', value: 9 },
  { name: '20积分', icon: '💰', type: 'points', value: 20 }
]

const lotteryCount = computed(() => Math.floor(props.consecutiveDays / 7))

const spin = () => {
  if (isSpinning.value || lotteryCount.value <= 0) return

  isSpinning.value = true
  const prizeIndex = Math.floor(Math.random() * prizes.length)
  const targetDegree = 360 * 5 + (360 - prizeIndex * 45 - 22.5)

  rotateDegree.value = targetDegree

  setTimeout(() => {
    result.value = prizes[prizeIndex]
    isSpinning.value = false
    emit('prize', result.value)
  }, 4000)
}

const close = () => {
  result.value = null
  rotateDegree.value = 0
  emit('close')
}
</script>

<style scoped>
.lottery-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.lottery-content {
  background: white;
  border-radius: 20px;
  padding: 24px;
  width: 360px;
  text-align: center;
}

.lottery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.lottery-header h3 {
  margin: 0;
  font-size: 20px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.lottery-wheel {
  width: 280px;
  height: 280px;
  margin: 0 auto 20px;
  position: relative;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  padding: 10px;
}

.wheel-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: white;
  position: relative;
  transition: transform 4s cubic-bezier(0.17, 0.67, 0.12, 0.99);
}

.prize-item {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 50%;
  height: 50%;
  transform-origin: 0% 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.prize-icon {
  font-size: 24px;
}

.prize-name {
  font-size: 11px;
  color: #333;
  margin-top: 4px;
}

.wheel-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(139, 90, 43, 0.4);
  z-index: 10;
}

.wheel-center:hover {
  transform: translate(-50%, -50%) scale(1.05);
}

.lottery-info p {
  font-size: 14px;
  color: #666;
  margin: 8px 0;
}

.hint {
  color: #999;
  font-size: 13px;
}

.lottery-result {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.lottery-result p {
  font-size: 18px;
  color: var(--primary);
  font-weight: bold;
  margin-bottom: 16px;
}

.confirm-btn {
  padding: 10px 40px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}
</style>
