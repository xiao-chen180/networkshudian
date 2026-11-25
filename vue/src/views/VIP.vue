<template>
  <div class="vip-container">
    <!-- 顶部会员信息 -->
    <section class="vip-hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="member-card-hero">
          <div class="card-bg-pattern"></div>
          <div class="card-inner">
            <div class="vip-icon">👑</div>
            <div class="member-info-hero">
              <span class="level-name">{{ currentLevel }}</span>
              <span class="discount-tag">专享 {{ discount }} 折优惠</span>
            </div>
            <div class="member-meta">
              <span>会员ID: #{{ memberId }}</span>
              <span>加入时间: {{ joinDate }}</span>
            </div>
          </div>
        </div>

        <div class="growth-section-hero">
          <div class="growth-header-hero">
            <h3>成长进度</h3>
            <div class="growth-numbers">
              <span class="current-points">{{ growthPoints }}</span>
              <span class="divider">/</span>
              <span class="next-points">{{ nextLevelPoints }}</span>
            </div>
          </div>
          <div class="progress-wrapper">
            <div class="progress-bar-hero">
              <div class="progress-fill-hero" :style="{ width: progressPercent + '%' }"></div>
            </div>
            <div class="level-markers">
              <div
                v-for="(level, index) in levels"
                :key="level.name"
                class="level-marker"
                :class="{ active: growthPoints >= level.points }"
                :style="{ left: (index / (levels.length - 1) * 100) + '%' }"
              >
                <div class="marker-dot"></div>
                <span class="marker-text">{{ level.name.split('会员')[0] }}</span>
              </div>
            </div>
          </div>
          <div class="upgrade-hint" v-if="nextPurchase > 0">
            再消费 <strong>¥{{ nextPurchase }}</strong> 即可升级到 <strong>{{ nextLevel }}</strong>
          </div>
          <div class="upgrade-hint success" v-else>
            🎉 恭喜您已达到最高等级！
          </div>
        </div>
      </div>
    </section>

    <!-- 主要内容 -->
    <main class="vip-content">
      <!-- 会员权益 -->
      <section class="content-block">
        <div class="block-header">
          <h3>🎁 会员权益</h3>
        </div>
        <div class="benefits-grid">
          <div
            v-for="(benefit, index) in benefits"
            :key="index"
            class="benefit-card-flip"
            :style="{ '--accent': benefit.color, '--delay': (index * 0.1) + 's' }"
          >
            <div class="flip-inner">
              <div class="flip-front">
                <div class="benefit-icon-bg">
                  <span>{{ benefit.icon }}</span>
                </div>
                <h4>{{ benefit.title }}</h4>
              </div>
              <div class="flip-back">
                <p>{{ benefit.desc }}</p>
                <div class="flip-icon">{{ benefit.icon }}</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 会员等级 -->
      <section class="content-block">
        <div class="block-header">
          <h3>📊 会员等级</h3>
        </div>
        <div class="levels-flow">
          <div
            v-for="level in levels"
            :key="level.name"
            class="level-node"
            :class="{ current: level.name === currentLevel, achieved: growthPoints >= level.points }"
          >
            <div class="node-icon">{{ level.icon }}</div>
            <div class="node-info">
              <span class="node-name">{{ level.name }}</span>
              <span class="node-require">≥{{ level.points }}成长值</span>
              <span class="node-discount">{{ level.discount }}折</span>
            </div>
            <div class="node-status" v-if="level.points <= growthPoints">✓</div>
          </div>
          <div class="level-connector" v-for="i in 4" :key="'c'+i"></div>
        </div>
      </section>

      <!-- 消费统计 -->
      <section class="content-block">
        <div class="block-header">
          <h3>📈 消费统计</h3>
        </div>
        <div class="stats-row">
          <div class="stat-box">
            <span class="stat-num">¥{{ totalSpent.toLocaleString() }}</span>
            <span class="stat-desc">累计消费</span>
          </div>
          <div class="stat-box">
            <span class="stat-num">{{ orderCount }}</span>
            <span class="stat-desc">订单数量</span>
          </div>
          <div class="stat-box">
            <span class="stat-num">¥{{ savedAmount }}</span>
            <span class="stat-desc">已省金额</span>
          </div>
          <div class="stat-box">
            <span class="stat-num">{{ growthPoints }}</span>
            <span class="stat-desc">成长值</span>
          </div>
        </div>
      </section>

      <!-- 最近订单 -->
      <section class="content-block">
        <div class="block-header">
          <h3>🛒 最近订单</h3>
          <button class="link-btn" @click="viewOrders">查看全部 →</button>
        </div>
        <div class="orders-list" v-if="recentOrders.length > 0">
          <div
            v-for="order in recentOrders"
            :key="order.id"
            class="order-row"
            @click="viewOrders"
          >
            <div class="order-left">
              <span class="order-id">订单号: {{ formatOrderNo(order.id) }}</span>
              <span class="order-time">{{ formatDate(order.createdAt) }}</span>
            </div>
            <div class="order-right">
              <span class="order-price">¥{{ formatPrice(order.totalPrice) }}</span>
              <span class="order-badge" :class="'badge-' + order.status">{{ getStatusText(order.status) }}</span>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <span>📭</span>
          <p>暂无订单记录</p>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderAPI } from '../api'

const router = useRouter()

const username = ref(localStorage.getItem('username') || '会员用户')
const memberId = ref('VIP' + Math.floor(Math.random() * 100000).toString().padStart(5, '0'))
const joinDate = ref('2024-01-15')
const discount = ref(localStorage.getItem('discount') ? (parseFloat(localStorage.getItem('discount')) * 100).toFixed(0) : '95')
const growthPoints = ref(2860)
const totalSpent = ref(2860)
const orderCount = ref(15)
const savedAmount = ref(143)
const recentOrders = ref([])

const levels = ref([
  { name: '普通会员', icon: '🌟', points: 0, discount: '95' },
  { name: '白银会员', icon: '🥈', points: 1000, discount: '92' },
  { name: '黄金会员', icon: '🥇', points: 3000, discount: '88' },
  { name: '铂金会员', icon: '💎', points: 5000, discount: '85' },
  { name: '钻石会员', icon: '💠', points: 10000, discount: '80' }
])

const benefits = ref([
  { icon: '💰', title: '专属折扣', desc: '全场图书享受最低8折优惠', color: '#ffd700' },
  { icon: '🚚', title: '免费包邮', desc: '订单满99元享免费配送', color: '#4dabf7' },
  { icon: '🎫', title: '积分兑换', desc: '消费积分可兑换精美礼品', color: '#51cf66' },
  { icon: '📖', title: '新书抢先读', desc: '优先获取新书上架通知', color: '#9775fa' },
  { icon: '🎂', title: '生日礼包', desc: '生日当月领取专属礼包', color: '#f783ac' },
  { icon: '💬', title: '专属客服', desc: '一对一VIP专属服务', color: '#ffa94d' }
])

const currentLevel = computed(() => {
  let level = '普通会员'
  for (let i = levels.value.length - 1; i >= 0; i--) {
    if (growthPoints.value >= levels.value[i].points) {
      level = levels.value[i].name
      break
    }
  }
  return level
})

const nextLevel = computed(() => {
  for (const level of levels.value) {
    if (level.points > growthPoints.value) {
      return level.name
    }
  }
  return '钻石会员'
})

const nextLevelPoints = computed(() => {
  for (const level of levels.value) {
    if (level.points > growthPoints.value) {
      return level.points
    }
  }
  return growthPoints.value
})

const progressPercent = computed(() => {
  const current = growthPoints.value
  const next = nextLevelPoints.value
  const prevLevel = levels.value.find(l => l.name === currentLevel.value)
  const prevPoints = prevLevel ? prevLevel.points : 0

  if (current >= nextLevelPoints.value) return 100
  return Math.min(100, ((current - prevPoints) / (next - prevPoints)) * 100)
})

const nextPurchase = computed(() => {
  const diff = nextLevelPoints.value - growthPoints.value
  return diff > 0 ? diff : 0
})

const goShopping = () => router.push('/books')
const viewOrders = () => router.push('/orders')

const formatOrderNo = (id) => {
  return `SG${String(id).padStart(8, '0')}`
}

const formatPrice = (price) => {
  return price ? price.toFixed(2) : '0.00'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const getStatusText = (status) => {
  const map = { 'PENDING': '待付款', 'PAID': '已付款', 'SHIPPED': '已发货', 'COMPLETED': '已完成', 'CANCELLED': '已取消' }
  return map[status] || status
}

onMounted(() => loadOrderStats())

const loadOrderStats = async () => {
  try {
    const response = await orderAPI.getAll()
    const orders = response.data || []
    orderCount.value = orders.length
    totalSpent.value = orders.reduce((sum, o) => sum + (Number(o.totalPrice) || 0), 0)
    savedAmount.value = Math.round(totalSpent.value * 0.05)
    growthPoints.value = totalSpent.value
    recentOrders.value = orders.slice(0, 3)
  } catch (error) {
    console.error('获取订单统计失败:', error)
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.vip-container {
  min-height: 100vh;
  background: #f5f0e8;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 顶部英雄区域 */
.vip-hero {
  background: linear-gradient(135deg, #2c1810 0%, #4a3728 50%, #2c1810 100%);
  padding: 50px 40px 40px;
  position: relative;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffd700' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 30px;
  align-items: stretch;
  position: relative;
  z-index: 1;
}

/* 会员卡片 */
.member-card-hero {
  width: 300px;
  background: linear-gradient(135deg, #3d2817 0%, #5a4232 100%);
  border-radius: 20px;
  padding: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  transition: transform 0.3s ease;
}

.member-card-hero:hover {
  transform: perspective(800px) rotateY(-5deg) rotateX(2deg);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
}

.card-bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 80% 20%, rgba(255, 215, 0, 0.15) 0%, transparent 50%);
}

.card-inner {
  position: relative;
  z-index: 1;
  text-align: center;
}

.vip-icon {
  font-size: 56px;
  margin-bottom: 16px;
  animation: float 3s ease-in-out infinite;
}

.member-info-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.level-name {
  font-size: 26px;
  font-weight: 700;
  color: #ffd700;
}

.discount-tag {
  background: linear-gradient(135deg, #ffd700, #ff8c00);
  color: #2c1810;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.member-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 215, 0, 0.2);
}

.member-meta span {
  font-size: 12px;
  color: #a89070;
}

/* 成长值区域 */
.growth-section-hero {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.growth-header-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.growth-header-hero h3 {
  font-size: 18px;
  color: #2c1810;
  font-weight: 600;
}

.growth-numbers {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.current-points {
  font-size: 24px;
  font-weight: 700;
  color: #c21500;
}

.divider {
  font-size: 16px;
  color: #8b7355;
}

.next-points {
  font-size: 16px;
  color: #8b7355;
}

.progress-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.progress-bar-hero {
  height: 12px;
  background: #f0ebe3;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 30px;
}

.progress-fill-hero {
  height: 100%;
  background: linear-gradient(90deg, #ffd700, #ff8c00);
  border-radius: 6px;
  transition: width 0.8s ease;
  position: relative;
}

.progress-fill-hero::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  animation: shimmer 2s infinite;
}

.level-markers {
  position: relative;
  height: 24px;
}

.level-marker {
  position: absolute;
  transform: translateX(-50%);
  text-align: center;
}

.marker-dot {
  width: 10px;
  height: 10px;
  background: #d4c4a8;
  border-radius: 50%;
  margin: 0 auto 6px;
  transition: all 0.3s ease;
}

.level-marker.active .marker-dot {
  background: #ffd700;
  box-shadow: 0 0 12px rgba(255, 215, 0, 0.6);
}

.marker-text {
  font-size: 11px;
  color: #6b5344;
  white-space: nowrap;
}

.upgrade-hint {
  text-align: center;
  padding: 12px;
  background: #faf7f2;
  border-radius: 10px;
  font-size: 14px;
  color: #6b5344;
  margin-top: auto;
}

.upgrade-hint strong {
  color: #c21500;
}

.upgrade-hint.success {
  background: linear-gradient(135deg, rgba(81, 207, 102, 0.1), rgba(81, 207, 102, 0.05));
  color: #2b8a3e;
}

/* 主内容区域 */
.vip-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 40px 60px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-block {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.block-header h3 {
  font-size: 18px;
  color: #2c1810;
  font-weight: 600;
}

.link-btn {
  background: none;
  border: none;
  color: #c21500;
  font-size: 14px;
  cursor: pointer;
}

.link-btn:hover {
  text-decoration: underline;
}

/* 权益网格 */
.benefits-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

/* 3D 翻转卡片 */
.benefit-card-flip {
  perspective: 800px;
  height: 140px;
  cursor: pointer;
}

.flip-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  transform-style: preserve-3d;
}

.benefit-card-flip:hover .flip-inner {
  transform: rotateY(180deg);
}

.flip-front,
.flip-back {
  position: absolute;
  inset: 0;
  backface-visibility: hidden;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px;
  text-align: center;
}

.flip-front {
  background: color-mix(in srgb, var(--accent) 8%, white);
  border: 1px solid color-mix(in srgb, var(--accent) 20%, transparent);
}

.flip-back {
  background: linear-gradient(135deg, var(--accent), color-mix(in srgb, var(--accent) 80%, black));
  transform: rotateY(180deg);
  color: white;
}

.flip-back p {
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 10px;
}

.flip-icon {
  font-size: 24px;
  opacity: 0.8;
}

.benefit-icon-bg {
  width: 56px;
  height: 56px;
  margin: 0 auto 10px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.flip-front h4 {
  font-size: 16px;
  color: #2c1810;
  margin: 0;
  font-weight: 600;
}

/* 等级流程 */
.levels-flow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
}

.level-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 12px;
  background: #faf7f2;
  min-width: 120px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  cursor: pointer;
}

.level-node:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.level-node.current {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.15), rgba(255, 140, 0, 0.1));
  border: 2px solid #ffd700;
  animation: currentGlow 2s ease-in-out infinite;
}

@keyframes currentGlow {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 215, 0, 0.3); }
  50% { box-shadow: 0 0 20px 4px rgba(255, 215, 0, 0.2); }
}

.level-node.achieved:not(.current) {
  background: #f0fff4;
}

.node-icon {
  font-size: 32px;
}

.node-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.node-name {
  font-size: 14px;
  font-weight: 600;
  color: #2c1810;
}

.node-require {
  font-size: 11px;
  color: #8b7355;
}

.node-discount {
  font-size: 13px;
  color: #c21500;
  font-weight: 600;
}

.node-status {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 20px;
  height: 20px;
  background: #51cf66;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.level-connector {
  width: 40px;
  height: 2px;
  background: #d4c4a8;
  flex-shrink: 0;
}

/* 统计行 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-box {
  background: #faf7f2;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-box:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.stat-num {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #c21500;
  margin-bottom: 4px;
  transition: transform 0.3s var(--ease-bounce);
}

.stat-box:hover .stat-num {
  transform: scale(1.1);
}

.stat-desc {
  font-size: 13px;
  color: #8b7355;
}

/* 订单列表 */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #faf7f2;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.order-row:hover {
  background: #f5f0e8;
  transform: translateX(6px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.order-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-id {
  font-size: 14px;
  color: #2c1810;
  font-weight: 500;
}

.order-time {
  font-size: 12px;
  color: #8b7355;
}

.order-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.order-price {
  font-size: 18px;
  font-weight: 700;
  color: #c21500;
}

.order-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.badge-PENDING { background: #fff3cd; color: #856404; }
.badge-PAID { background: #d4edda; color: #155724; }
.badge-SHIPPED { background: #cce5ff; color: #004085; }
.badge-COMPLETED { background: #d4edda; color: #155724; }
.badge-CANCELLED { background: #f8d7da; color: #721c24; }

.empty-state {
  text-align: center;
  padding: 40px;
  color: #8b7355;
}

.empty-state span {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

/* 动画 */
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* 响应式 */
@media (max-width: 1000px) {
  .hero-content {
    flex-direction: column;
  }

  .member-card-hero {
    width: 100%;
  }

  .benefits-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .levels-flow {
    flex-wrap: wrap;
    gap: 12px;
    justify-content: center;
  }

  .level-connector {
    display: none;
  }

  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .vip-hero {
    padding: 30px 20px;
  }

  .vip-content {
    padding: 20px;
  }

  .benefits-grid {
    grid-template-columns: 1fr;
  }

  .stats-row {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
