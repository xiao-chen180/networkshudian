<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><span>优惠券</span></div>
      <div class="pg-head">
        <div>
          <h1>优惠券中心</h1>
          <div class="sub">领取后在结算页选择使用，可与会员折扣叠加</div>
        </div>
      </div>

      <div class="tabs">
        <button :class="{ on: tab === 'available' }" @click="tab = 'available'">可领取 <em>{{ available.length }}</em></button>
        <button :class="{ on: tab === 'mine' }" @click="tab = 'mine'">我的券 <em>{{ mine.length }}</em></button>
      </div>

      <!-- 可领取 -->
      <div v-if="tab === 'available'">
        <div class="clist" v-if="available.length">
          <div class="ticket" v-for="c in available" :key="c.id">
            <div class="left">
              <b><i>¥</i>{{ c.type === 'PERCENT' ? c.value : c.value }}</b>
              <span v-if="Number(c.minAmount) > 0">满 {{ c.minAmount }} 可用</span>
              <span v-else>无门槛</span>
            </div>
            <div class="right">
              <div class="t">{{ c.name }}</div>
              <div class="d">有效期至 {{ fmtDate(c.endTime) }} · 剩余 {{ c.remainNum ?? '—' }} 张</div>
              <button class="btn-line sm" :disabled="Number(c.remainNum) <= 0" @click="claim(c.id)">
                {{ Number(c.remainNum) > 0 ? '领取' : '已领完' }}
              </button>
            </div>
          </div>
        </div>
        <div class="blank" v-else>暂无可领取的优惠券</div>
      </div>

      <!-- 我的券 -->
      <div v-else>
        <div class="clist" v-if="mine.length">
          <div class="ticket" v-for="c in mine" :key="c.id" :class="{ used: c.status === 'USED' }">
            <div class="left">
              <b><i>¥</i>{{ c.value }}</b>
              <span>{{ Number(c.minAmount) > 0 ? `满 ${c.minAmount} 可用` : '无门槛' }}</span>
            </div>
            <div class="right">
              <div class="t">{{ c.couponName }}</div>
              <div class="d">有效期至 {{ fmtDate(c.endTime) }}</div>
              <span class="tag" :class="{ gold: c.status === 'UNUSED' }">{{ statusText(c.status) }}</span>
            </div>
          </div>
        </div>
        <div class="blank" v-else>还没有优惠券，去领一张吧</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { couponAPI } from '../api'
import { ElMessage } from 'element-plus'

const tab = ref('available')
const available = ref([])
const mine = ref([])

const fmtDate = (t) => (t ? new Date(t).toLocaleDateString('zh-CN') : '—')
const statusText = (s) => ({ UNUSED: '未使用', USED: '已使用', EXPIRED: '已过期' }[s] || s)

async function load() {
  try {
    const [a, m] = await Promise.all([couponAPI.getAvailable(), couponAPI.getMyCoupons()])
    available.value = a.data || []
    mine.value = m.data || []
  } catch {}
}

async function claim(id) {
  try {
    await couponAPI.claim(id)
    ElMessage.success('领取成功')
    load()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '领取失败')
  }
}

onMounted(load)
</script>

<style scoped>
.clist { display: flex; flex-direction: column; gap: 12px; }
.ticket {
  display: flex; border: 1px solid var(--color-line); background: var(--color-surface);
}
.ticket.used { opacity: .55; }
.left {
  width: 168px; flex: none; padding: 22px 0; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 6px;
  border-right: 1px dashed var(--color-line-strong);
}
.left b { font-family: var(--font-display); font-size: 30px; font-weight: 400; color: var(--color-gold); }
.left b i { font-style: normal; font-size: 13px; margin-right: 2px; }
.left span { font-size: 10px; color: var(--color-ink-faint); letter-spacing: .06em; }
.right { flex: 1; padding: 18px 22px; display: flex; align-items: center; gap: 20px; }
.right .t { font-family: var(--font-display); font-size: 14px; letter-spacing: .06em; }
.right .d { font-size: 10px; color: var(--color-ink-faint); letter-spacing: .05em; margin-right: auto; }
</style>