<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><router-link to="/profile">个人中心</router-link><i>/</i><span>每日签到</span></div>
      <div class="pg-head">
        <div>
          <h1>每日签到</h1>
          <div class="sub">连续签到得积分，满 7 天解锁抽奖</div>
        </div>
      </div>

      <div class="layout">
        <section class="panel checkin">
          <div class="days">
            <span class="stat-big">{{ status.consecutiveDays || 0 }}</span>
            <span class="unit">天</span>
          </div>
          <p class="label">连续签到</p>
          <button class="btn-gold wide" :disabled="status.checkedInToday" @click="doCheckIn">
            {{ status.checkedInToday ? '今日已签到' : '立即签到 +10 积分' }}
          </button>
          <div class="tips">
            <span v-if="status.checkedInToday">明天再来，连续天数不会中断</span>
            <span v-else>今日签到可获得 10 积分</span>
          </div>
        </section>

        <section class="panel rewards">
          <div class="panel-h"><h3>连续签到奖励</h3></div>
          <div class="rgrid">
            <div class="ritem" :class="{ done: (status.consecutiveDays || 0) >= 7 }">
              <b>7 天</b><span>+50 积分</span><em>解锁抽奖</em>
            </div>
            <div class="ritem" :class="{ done: (status.consecutiveDays || 0) >= 15 }">
              <b>15 天</b><span>+150 积分</span><em>加赠运费券</em>
            </div>
            <div class="ritem" :class="{ done: (status.consecutiveDays || 0) >= 30 }">
              <b>30 天</b><span>+500 积分</span><em>年度读书礼</em>
            </div>
          </div>

          <div class="lottery" v-if="(status.consecutiveDays || 0) >= 7" @click="showLottery = true">
            <div>
              <b>每日抽奖</b>
              <span>连续签到 7 天，获得一次抽奖机会</span>
            </div>
            <span class="arrow">→</span>
          </div>
        </section>
      </div>
    </div>

    <DailyLottery
      :visible="showLottery"
      :consecutive-days="status.consecutiveDays || 0"
      :drawn="!!status.lotteryDrawn"
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

const onPrize = async (prize) => {
  ElMessage.success(`恭喜获得：${prize.name}`)
  try {
    const res = await checkInAPI.getStatus()
    status.value = res.data || status.value
  } catch {}
}

async function load() {
  try {
    const res = await checkInAPI.getStatus()
    status.value = res.data || {}
  } catch {}
}

async function doCheckIn() {
  try {
    const res = await checkInAPI.checkIn()
    status.value = res.data || status.value
    ElMessage.success('签到成功，+10 积分')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '签到失败')
  }
}

onMounted(load)
</script>

<style scoped>
.layout { display: flex; gap: 18px; align-items: flex-start; }
.checkin { width: 340px; flex: none; text-align: center; padding: 34px 24px; }
.days { display: flex; align-items: baseline; justify-content: center; gap: 6px; }
.unit { font-family: var(--font-display); font-size: 15px; color: var(--color-ink-muted); }
.label { margin-top: 10px; font-size: 10.5px; letter-spacing: .18em; color: var(--color-ink-faint); }
.wide { width: 100%; margin-top: 26px; height: 44px; }
.tips { margin-top: 14px; font-size: 10px; color: var(--color-ink-faint); letter-spacing: .05em; }

.rewards { flex: 1; }
.rgrid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.ritem { border: 1px solid var(--color-line); padding: 18px 16px; display: flex; flex-direction: column; gap: 5px; }
.ritem b { font-family: var(--font-display); font-size: 17px; font-weight: 400; letter-spacing: .08em; }
.ritem span { font-size: 11.5px; color: var(--color-gold); }
.ritem em { font-style: normal; font-size: 9.5px; color: var(--color-ink-faint); letter-spacing: .06em; }
.ritem.done { border-color: var(--color-gold-line); background: rgba(211,174,106,.05); }

.lottery {
  margin-top: 16px; border: 1px solid var(--color-gold-line); padding: 16px 18px;
  display: flex; align-items: center; justify-content: space-between; cursor: pointer;
  background: rgba(211,174,106,.04);
}
.lottery:hover { background: rgba(211,174,106,.08); }
.lottery b { display: block; font-family: var(--font-display); font-size: 13px; font-weight: 400; letter-spacing: .14em; }
.lottery span { display: block; margin-top: 5px; font-size: 10px; color: var(--color-ink-muted); }
.lottery .arrow { color: var(--color-gold); font-size: 14px; }

@media (max-width: 900px) {
  .layout { flex-direction: column; }
  .checkin { width: 100%; }
}
</style>