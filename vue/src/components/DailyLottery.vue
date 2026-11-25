<template>
  <div class="lottery-modal" v-if="visible" @click.self="close">
    <div class="lottery-content">
      <div class="lottery-header">
        <div>
          <div class="k">DAILY LOTTERY</div>
          <h3>签到抽奖</h3>
        </div>
        <button class="close-btn" @click="close">×</button>
      </div>

      <div class="wheel" :class="{ spinning: isSpinning }">
        <div class="wheel-inner" :style="{ transform: `rotate(${rotateDegree}deg)` }">
          <div
            v-for="(prize, index) in prizes"
            :key="index"
            class="sector"
            :style="{ transform: `rotate(${index * 45}deg)` }"
          >
            <span class="label">{{ prize.label }}</span>
          </div>
        </div>
        <div class="center" :class="{ disabled: !canSpin }" @click="spin">
          {{ isSpinning ? '…' : '抽奖' }}
        </div>
      </div>

      <div class="info">
        <span>连续签到 <b>{{ consecutiveDays }}</b> 天</span>
        <span v-if="drawn">今日已抽奖</span>
        <span v-else-if="consecutiveDays >= 7">今日可抽 1 次</span>
        <span v-else class="hint">连续签到满 7 天可抽奖</span>
      </div>

      <div class="result" v-if="result">
        <p>恭喜获得 <b>{{ result.name }}</b></p>
        <button class="confirm" @click="close">收下</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { checkInAPI } from '../api'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: Boolean,
  consecutiveDays: { type: Number, default: 0 },
  drawn: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'prize'])

const isSpinning = ref(false)
const rotateDegree = ref(0)
const result = ref(null)
const localDrawn = ref(false)

const prizes = [
  { label: '10 积分', points: 10 },
  { label: '20 积分', points: 20 },
  { label: '50 积分', points: 50 },
  { label: '10 积分', points: 10 },
  { label: '100 积分', points: 100 },
  { label: '20 积分', points: 20 },
  { label: '200 积分', points: 200 },
  { label: '50 积分', points: 50 }
]

const drawn = computed(() => localDrawn.value || props.drawn)
const canSpin = computed(() => !isSpinning.value && !drawn.value && props.consecutiveDays >= 7)

async function spin() {
  if (!canSpin.value) return
  isSpinning.value = true

  try {
    // 以服务端结果为准：先请求，再让指针停在中奖扇区
    const res = await checkInAPI.drawLottery()
    const points = Number(res.data?.points || 10)
    const idx = Math.max(0, prizes.findIndex(p => p.points === points))
    const targetDegree = 360 * 5 + (360 - idx * 45 - 22.5)
    rotateDegree.value = targetDegree

    setTimeout(() => {
      result.value = { name: res.data?.name || `${points} 积分`, points }
      localDrawn.value = true
      isSpinning.value = false
      emit('prize', result.value)
    }, 3200)
  } catch (e) {
    isSpinning.value = false
    ElMessage.warning(e.response?.data?.message || '抽奖失败')
  }
}

function close() {
  result.value = null
  rotateDegree.value = 0
  emit('close')
}
</script>

<style scoped>
.lottery-modal {
  position: fixed; inset: 0; background: rgba(0, 0, 0, .72);
  display: flex; align-items: center; justify-content: center; z-index: 2000;
  backdrop-filter: blur(3px);
}
.lottery-content {
  width: 400px; background: var(--color-surface); border: 1px solid var(--color-gold-line);
  padding: 26px 26px 24px; text-align: center;
}
.lottery-header { display: flex; align-items: flex-start; justify-content: space-between; text-align: left; }
.lottery-header .k { font-size: 8.5px; letter-spacing: .3em; color: var(--color-gold); }
.lottery-header h3 { margin-top: 8px; font-family: var(--font-display); font-size: 20px; font-weight: 400; letter-spacing: .16em; }
.close-btn { background: none; border: 0; color: var(--color-ink-faint); font-size: 20px; cursor: pointer; line-height: 1; }
.close-btn:hover { color: var(--color-ink); }

.wheel {
  width: 264px; height: 264px; margin: 26px auto 20px; position: relative; border-radius: 50%;
  border: 1px solid var(--color-gold-line);
  background: conic-gradient(
    rgba(211,174,106,.16) 0 45deg, rgba(255,255,255,.03) 45deg 90deg,
    rgba(211,174,106,.16) 90deg 135deg, rgba(255,255,255,.03) 135deg 180deg,
    rgba(211,174,106,.16) 180deg 225deg, rgba(255,255,255,.03) 225deg 270deg,
    rgba(211,174,106,.16) 270deg 315deg, rgba(255,255,255,.03) 315deg 360deg);
  padding: 10px;
}
.wheel-inner {
  width: 100%; height: 100%; border-radius: 50%; position: relative;
  transition: transform 3.2s cubic-bezier(.17,.67,.12,.99);
  background: var(--color-bg);
}
.sector {
  position: absolute; top: 50%; left: 50%; width: 50%; height: 50%;
  transform-origin: 0% 100%; display: flex; justify-content: center;
}
.sector .label {
  margin-top: 14px; font-size: 10px; letter-spacing: .06em; color: var(--color-ink-soft);
}
.center {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  width: 66px; height: 66px; border-radius: 50%; background: var(--color-gold); color: #14150f;
  display: flex; align-items: center; justify-content: center;
  font-family: var(--font-display); font-size: 14px; letter-spacing: .12em;
  cursor: pointer; z-index: 10; transition: opacity var(--dur-fast);
}
.center.disabled { background: var(--color-surface-3); color: var(--color-ink-faint); cursor: not-allowed; }
.spinning .center { pointer-events: none; }

.info { display: flex; justify-content: center; gap: 18px; font-size: 10.5px; letter-spacing: .06em; color: var(--color-ink-muted); }
.info b { color: var(--color-gold); font-weight: 400; }
.info .hint { color: var(--color-ink-faint); }

.result { margin-top: 20px; padding-top: 18px; border-top: 1px solid var(--color-line); }
.result p { font-size: 12px; color: var(--color-ink-muted); letter-spacing: .06em; }
.result b { font-family: var(--font-display); font-size: 18px; color: var(--color-gold); font-weight: 400; margin-left: 6px; }
.confirm {
  margin-top: 16px; height: 36px; padding: 0 26px; border: 1px solid var(--color-gold);
  background: var(--color-gold); color: #14150f; font-family: inherit; font-size: 11px; letter-spacing: .16em; cursor: pointer;
}
</style>