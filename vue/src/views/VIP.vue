<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><span>会员中心</span></div>
      <div class="pg-head">
        <div>
          <h1>会员中心</h1>
          <div class="sub">等级由累计消费决定 · 折扣在结算时自动生效</div>
        </div>
        <router-link to="/books" class="btn-line sm">继续购书提升等级</router-link>
      </div>

      <!-- 等级卡 -->
      <section class="hero-card">
        <div class="hc-left">
          <span class="k">MEMBERSHIP</span>
          <div class="level">{{ currentLevel }}</div>
          <div class="meta">
            <span>会员编号 {{ memberNo }}</span>
            <span>{{ discountValue }} 折权益</span>
          </div>
        </div>
        <div class="hc-right">
          <div class="grow-line">
            <span>成长值 <b class="mono">{{ growthPoints }}</b></span>
            <span v-if="nextLevel">距 {{ nextLevel }} 还差 <b class="mono">{{ Math.max(0, nextLevelPoints - growthPoints) }}</b></span>
            <span v-else>已达最高等级</span>
          </div>
          <div class="bar"><i :style="{ width: progressPercent + '%' }"></i></div>
          <div class="marks">
            <span v-for="(l, i) in levels" :key="l.name" :class="{ on: growthPoints >= l.points }"
              :style="{ left: (i / (levels.length - 1) * 100) + '%' }">{{ l.name }}</span>
          </div>
        </div>
      </section>

      <!-- 数据 -->
      <div class="stats">
        <div class="stat"><b class="mono">{{ fmt(totalSpent) }}</b><span>累计消费（元）</span></div>
        <div class="stat"><b class="mono">{{ orderCount }}</b><span>累计订单</span></div>
        <div class="stat"><b class="mono">{{ points }}</b><span>当前积分</span></div>
        <div class="stat"><b class="mono">{{ fmt(savedAmount) }}</b><span>会员节省（元）</span></div>
      </div>

      <!-- 等级流 -->
      <section class="panel">
        <div class="panel-h"><h3>等级与折扣</h3></div>
        <div class="levels">
          <div class="lv" v-for="l in levels" :key="l.name"
            :class="{ current: l.name === currentLevel, achieved: growthPoints >= l.points }">
            <div class="lv-name">{{ l.name }}</div>
            <div class="lv-req">≥ {{ l.points }} 成长值</div>
            <div class="lv-dis">{{ l.discount }} 折</div>
            <div class="lv-state" v-if="l.name === currentLevel">当前等级</div>
            <div class="lv-state done" v-else-if="growthPoints >= l.points">已达成</div>
          </div>
        </div>
      </section>

      <!-- 权益 -->
      <section class="panel">
        <div class="panel-h"><h3>会员权益</h3></div>
        <div class="benefits">
          <div class="benefit"><b>会员折扣</b><span>结算时按等级自动抵扣，可与优惠券叠加</span></div>
          <div class="benefit"><b>每日签到</b><span>连续签到得积分，7 天解锁抽奖</span></div>
          <div class="benefit"><b>积分抵运费</b><span>500 积分兑换 1 张运费券</span></div>
          <div class="benefit"><b>新书优先</b><span>周三上新提前 24 小时开放购买</span></div>
        </div>
      </section>

      <!-- 最近订单 -->
      <section class="panel">
        <div class="panel-h">
          <h3>最近订单</h3>
          <router-link to="/orders" class="act">全部订单 →</router-link>
        </div>
        <div v-if="recentOrders.length" class="orders">
          <div class="orow" v-for="o in recentOrders" :key="o.id">
            <span class="no">SG{{ String(o.id).padStart(8, '0') }}</span>
            <span class="items">{{ (o.items || []).map(i => i.bookName).join(' / ') }}</span>
            <span class="amt">¥{{ fmt(o.totalPrice) }}</span>
            <OrderStatusBadge :status="o.status" />
          </div>
        </div>
        <div class="blank" v-else>还没有订单</div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { orderAPI, pointsAPI } from '../api'
import OrderStatusBadge from '../components/common/OrderStatusBadge.vue'

const username = localStorage.getItem('username') || '会员用户'
const userId = localStorage.getItem('userId') || ''
const discount = Number(localStorage.getItem('discount') || 0.95)
const memberNo = `VIP${String(userId).padStart(6, '0')}`

const growthPoints = ref(0)
const totalSpent = ref(0)
const orderCount = ref(0)
const points = ref(0)
const recentOrders = ref([])

const levels = [
  { name: '普通会员', points: 0, discount: 10 },
  { name: '银卡会员', points: 500, discount: 9.5 },
  { name: '金卡会员', points: 2000, discount: 9 },
  { name: '黑卡会员', points: 5000, discount: 8.5 }
]

const discountValue = computed(() => (discount * 10).toFixed(1).replace('.0', ''))
/* 等级以账号真实折扣为准，成长值只用于展示进度 */
const currentLevel = computed(() => {
  const matched = levels.find(l => Math.abs(l.discount / 10 - discount) < 0.001)
  return matched ? matched.name : levels[0].name
})
const currentLevelPoints = computed(() => levels.find(l => l.name === currentLevel.value)?.points || 0)
const nextLevel = computed(() => {
  const idx = levels.findIndex(l => l.name === currentLevel.value)
  return levels[idx + 1]?.name || ''
})
const nextLevelPoints = computed(() => levels.find(l => l.name === nextLevel.value)?.points || growthPoints.value)
const progressPercent = computed(() => {
  const next = levels.find(l => l.points > currentLevelPoints.value)
  if (!next) return 100
  const span = next.points - currentLevelPoints.value
  return Math.min(100, Math.max(0, ((growthPoints.value - currentLevelPoints.value) / span) * 100))
})
const savedAmount = computed(() => totalSpent.value * (1 - discount) )

const fmt = (n) => Number(n || 0).toFixed(2)

onMounted(async () => {
  const [ordersRes, pointsRes] = await Promise.all([
    orderAPI.getAll().catch(() => ({ data: [] })),
    pointsAPI.getMyPoints().catch(() => ({ data: { points: 0 } }))
  ])
  const orders = ordersRes.data || []
  orderCount.value = orders.length
  totalSpent.value = orders
    .filter(o => o.status === 'COMPLETED' || o.status === 'PAID' || o.status === 'SHIPPED')
    .reduce((s, o) => s + Number(o.totalPrice || 0), 0)
  growthPoints.value = Math.round(totalSpent.value)
  points.value = pointsRes.data?.points || 0
  recentOrders.value = orders.slice(0, 3)
})
</script>

<style scoped>
.hero-card {
  display: flex; gap: 40px; align-items: center;
  border: 1px solid var(--color-gold-line); padding: 28px 30px;
  background:
    radial-gradient(100% 120% at 0% 0%, rgba(211,174,106,.14), transparent 58%),
    var(--color-surface);
}
.hc-left { width: 280px; flex: none; }
.hc-left .k { font-size: 9px; letter-spacing: .3em; color: var(--color-gold); }
.hc-left .level { margin-top: 12px; font-family: var(--font-display); font-size: 30px; letter-spacing: .16em; }
.hc-left .meta { margin-top: 12px; display: flex; gap: 18px; font-size: 10px; color: var(--color-ink-muted); letter-spacing: .06em; }

.hc-right { flex: 1; }
.grow-line { display: flex; justify-content: space-between; font-size: 10.5px; color: var(--color-ink-muted); letter-spacing: .06em; }
.grow-line b { font-family: var(--font-display); color: var(--color-gold); font-weight: 400; font-size: 13px; }
.bar { height: 3px; background: var(--color-surface-3); margin-top: 12px; position: relative; }
.bar i { position: absolute; left: 0; top: 0; bottom: 0; background: var(--color-gold); }
.marks { position: relative; height: 20px; margin-top: 10px; }
.marks span {
  position: absolute; transform: translateX(-50%); font-size: 9px; letter-spacing: .06em;
  color: var(--color-ink-faint); white-space: nowrap;
}
.marks span.on { color: var(--color-gold); }

.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-top: 16px; }
.stat { border: 1px solid var(--color-line); background: var(--color-surface); padding: 18px; }
.stat b { display: block; font-family: var(--font-display); font-size: 22px; font-weight: 400; color: var(--color-gold); }
.stat span { display: block; margin-top: 7px; font-size: 10px; letter-spacing: .1em; color: var(--color-ink-muted); }

.levels { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.lv { border: 1px solid var(--color-line); padding: 18px 16px; position: relative; }
.lv.achieved { border-color: var(--color-line-strong); }
.lv.current { border-color: var(--color-gold-line); background: rgba(211,174,106,.05); }
.lv-name { font-family: var(--font-display); font-size: 15px; letter-spacing: .1em; }
.lv-req { margin-top: 8px; font-size: 10px; color: var(--color-ink-faint); letter-spacing: .05em; }
.lv-dis { margin-top: 10px; font-size: 13px; color: var(--color-gold); }
.lv-state { margin-top: 12px; font-size: 9px; letter-spacing: .14em; color: var(--color-gold); }
.lv-state.done { color: var(--color-ink-faint); }

.benefits { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.benefit { border: 1px solid var(--color-line); padding: 16px 18px; }
.benefit b { display: block; font-size: 12px; font-weight: 400; color: var(--color-ink); letter-spacing: .08em; }
.benefit span { display: block; margin-top: 8px; font-size: 10.5px; line-height: 1.8; color: var(--color-ink-muted); }

.orders { display: flex; flex-direction: column; }
.orow { display: grid; grid-template-columns: 130px 1fr 90px 80px; gap: 16px; align-items: center; padding: 13px 0; border-bottom: 1px solid var(--color-line); font-size: 11px; color: var(--color-ink-muted); }
.orow:last-child { border-bottom: 0; }
.orow .no { font-variant-numeric: tabular-nums; }
.orow .items { color: var(--color-ink-soft); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.orow .amt { color: var(--color-gold); text-align: right; }

@media (max-width: 1024px) {
  .hero-card { flex-direction: column; align-items: flex-start; gap: 22px; }
  .hc-left { width: 100%; }
  .stats { grid-template-columns: repeat(2, 1fr); }
  .levels { grid-template-columns: repeat(2, 1fr); }
  .benefits { grid-template-columns: 1fr; }
  .orow { grid-template-columns: 1fr auto; }
  .orow .no, .orow .items { display: none; }
}
</style>