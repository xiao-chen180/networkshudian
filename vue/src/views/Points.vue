<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><router-link to="/profile">个人中心</router-link><i>/</i><span>积分中心</span></div>
      <div class="pg-head">
        <div>
          <h1>积分中心</h1>
          <div class="sub">积分来自签到与消费，可用于兑换运费券</div>
        </div>
      </div>

      <div class="summary">
        <div class="sum-main">
          <span class="k">当前积分</span>
          <span class="stat-big mono">{{ totalPoints }}</span>
          <span class="tip">500 积分 = 1 张运费券</span>
        </div>
        <div class="sum-side">
          <div><span>本期获得</span><b class="mono">+{{ earned }}</b></div>
          <div><span>本期消耗</span><b class="mono">−{{ spent }}</b></div>
        </div>
      </div>

      <section class="panel" style="margin-top:18px">
        <div class="panel-h"><h3>积分规则</h3></div>
        <div class="rules">
          <div><b>每日签到</b><span>+10 积分 · 连续 7 天额外 +50</span></div>
          <div><b>购物消费</b><span>1 元 = 1 积分，订单完成后到账</span></div>
          <div><b>发表书评</b><span>+5 积分，被标为有用再 +2</span></div>
          <div><b>发布笔记</b><span>+8 积分，获赞累计上限 100</span></div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-h"><h3>积分明细</h3><span class="act plain">最近 50 条</span></div>
        <div class="logs" v-if="pointsLog.length">
          <div class="log" v-for="log in pointsLog" :key="log.id">
            <div class="lm">
              <b>{{ log.description || log.reason || '积分变动' }}</b>
              <span>{{ fmtTime(log.createdAt) }}</span>
            </div>
            <span class="amt" :class="{ earn: log.type === 'EARN' || Number(log.amount) > 0 }">
              {{ log.type === 'EARN' || Number(log.amount) > 0 ? '+' : '−' }}{{ Math.abs(Number(log.amount)) }}
            </span>
          </div>
        </div>
        <div class="blank" v-else>暂无积分记录</div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { pointsAPI } from '../api'

const totalPoints = ref(0)
const pointsLog = ref([])

const earned = computed(() => pointsLog.value.filter(l => l.type === 'EARN' || Number(l.amount) > 0)
  .reduce((s, l) => s + Math.abs(Number(l.amount)), 0))
const spent = computed(() => pointsLog.value.filter(l => !(l.type === 'EARN' || Number(l.amount) > 0))
  .reduce((s, l) => s + Math.abs(Number(l.amount)), 0))

const fmtTime = (t) => (t ? new Date(t).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '')

onMounted(async () => {
  try {
    const [p, l] = await Promise.all([pointsAPI.getMyPoints(), pointsAPI.getLog()])
    totalPoints.value = p.data?.points || 0
    pointsLog.value = l.data || []
  } catch {}
})
</script>

<style scoped>
.summary {
  display: flex; align-items: center; border: 1px solid var(--color-gold-line);
  background:
    radial-gradient(120% 90% at 0% 0%, rgba(211,174,106,.10), transparent 60%),
    var(--color-surface);
  padding: 26px 28px; gap: 30px;
}
.sum-main { display: flex; flex-direction: column; gap: 8px; }
.sum-main .k { font-size: 10px; letter-spacing: .22em; color: var(--color-ink-faint); }
.sum-main .tip { font-size: 10px; color: var(--color-ink-muted); letter-spacing: .06em; }
.sum-side { margin-left: auto; display: flex; gap: 34px; }
.sum-side div { display: flex; flex-direction: column; gap: 6px; }
.sum-side span { font-size: 10px; letter-spacing: .1em; color: var(--color-ink-faint); }
.sum-side b { font-family: var(--font-display); font-size: 18px; font-weight: 400; color: var(--color-ink-soft); }

.rules { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0 30px; }
.rules div { display: flex; align-items: baseline; gap: 14px; padding: 12px 0; border-bottom: 1px solid var(--color-line); }
.rules b { width: 84px; flex: none; font-size: 11.5px; font-weight: 400; color: var(--color-ink); letter-spacing: .06em; }
.rules span { font-size: 10.5px; color: var(--color-ink-muted); letter-spacing: .04em; }

.logs { display: flex; flex-direction: column; }
.log { display: flex; align-items: center; padding: 13px 0; border-bottom: 1px solid var(--color-line); }
.log:last-child { border-bottom: 0; }
.lm b { display: block; font-size: 12px; font-weight: 400; color: var(--color-ink-soft); letter-spacing: .04em; }
.lm span { display: block; margin-top: 4px; font-size: 9.5px; color: var(--color-ink-faint); }
.amt { margin-left: auto; font-family: var(--font-display); font-size: 15px; color: var(--color-ink-muted); }
.amt.earn { color: var(--color-gold); }

@media (max-width: 900px) {
  .rules { grid-template-columns: 1fr; }
  .summary { flex-direction: column; align-items: flex-start; }
  .sum-side { margin-left: 0; }
}
</style>