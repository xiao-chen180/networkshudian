<template>
  <div class="orders-page">
    <div class="hero-section">
      <h1 class="page-title">📋 我的订单</h1>
    </div>

    <div class="orders-container">
      <!-- 状态筛选 -->
      <div class="filter-tabs">
        <button v-for="tab in tabs" :key="tab.key" :class="['tab-btn', { active: activeTab === tab.key }]" @click="activeTab = tab.key">
          {{ tab.label }}
        </button>
      </div>

      <!-- 订单列表 -->
      <div v-if="filteredOrders.length > 0" class="order-list">
        <div v-for="order in filteredOrders" :key="order.id" class="order-card">
          <div class="order-header">
            <span class="order-no">订单号：SG{{ String(order.id).padStart(8, '0') }}</span>
            <span :class="['status-badge', order.status.toLowerCase()]">{{ getStatusName(order.status) }}</span>
          </div>
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <img :src="getCover(item)" class="item-cover" />
            <div class="item-info">
              <span class="item-name">{{ item.bookName }}</span>
              <span class="item-qty">x{{ item.quantity }}</span>
            </div>
            <span class="item-price">¥{{ formatPrice(item.price) }}</span>
          </div>
          <div class="order-footer">
            <span class="order-total">合计：<strong>¥{{ formatPrice(order.totalPrice) }}</strong></span>
            <div class="order-actions">
              <button v-if="order.status === 'PENDING'" class="action-btn danger" @click="cancelOrder(order.id)">取消</button>
              <button v-if="order.status === 'PENDING'" class="action-btn primary" @click="payOrder(order.id)">去支付</button>
              <button v-if="order.status === 'SHIPPED'" class="action-btn primary" @click="confirmReceive(order.id)">确认收货</button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">📭</div>
        <p>暂无订单</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { orderAPI } from '../api'
import { ElMessage } from 'element-plus'

const orders = ref([])
const activeTab = ref('ALL')

const tabs = [
  { key: 'ALL', label: '全部' },
  { key: 'PENDING', label: '待付款' },
  { key: 'PAID', label: '已付款' },
  { key: 'SHIPPED', label: '已发货' },
  { key: 'COMPLETED', label: '已完成' },
  { key: 'CANCELLED', label: '已取消' }
]

const filteredOrders = computed(() => {
  if (activeTab.value === 'ALL') return orders.value
  return orders.value.filter(o => o.status === activeTab.value)
})

const loadOrders = async () => {
  try {
    const res = await orderAPI.getAll()
    orders.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const cancelOrder = async (id) => {
  try {
    await orderAPI.updateStatus(id, 'CANCELLED')
    ElMessage.success('已取消')
    loadOrders()
  } catch (e) {
    ElMessage.error('取消失败')
  }
}

const payOrder = async (id) => {
  try {
    await orderAPI.updateStatus(id, 'PAID')
    ElMessage.success('支付成功')
    loadOrders()
  } catch (e) {
    ElMessage.error('支付失败')
  }
}

const confirmReceive = async (id) => {
  try {
    await orderAPI.updateStatus(id, 'COMPLETED')
    ElMessage.success('已确认收货')
    loadOrders()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const getStatusName = (status) => {
  const map = { PENDING: '待付款', PAID: '已付款', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status
}

const getCover = (item) => `https://picsum.photos/seed/${item.bookIsbn || item.bookId}/200/280.jpg`
const formatPrice = (p) => typeof p === 'string' ? p : Number(p).toFixed(2)

onMounted(() => loadOrders())
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  background: var(--bg-page);
}

.hero-section {
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  padding: 20px;
  text-align: center;
}

.page-title {
  color: white;
  font-family: var(--font-serif);
  font-size: 22px;
  margin: 0;
}

.orders-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  margin-bottom: 16px;
  padding-bottom: 4px;
}

.tab-btn {
  flex-shrink: 0;
  padding: 8px 16px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
}

.tab-btn.active {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.order-no {
  font-size: 13px;
  color: var(--text-secondary);
}

.status-badge {
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending { background: #fff7e6; color: #d48806; }
.status-badge.paid { background: #e6f7ff; color: #1890ff; }
.status-badge.shipped { background: #f6ffed; color: #52c41a; }
.status-badge.completed { background: #f6ffed; color: #52c41a; }
.status-badge.cancelled { background: #f5f5f5; color: #999; }

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}

.item-cover {
  width: 60px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
}

.item-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
}

.item-name {
  font-size: 14px;
  color: var(--text-primary);
}

.item-qty {
  font-size: 13px;
  color: var(--text-secondary);
}

.item-price {
  font-size: 14px;
  color: var(--accent);
  font-weight: 500;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

.order-total {
  font-size: 14px;
}

.order-total strong {
  color: var(--accent);
  font-size: 16px;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 13px;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
}

.action-btn.primary {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
}

.action-btn.danger {
  color: var(--accent);
  border-color: var(--accent);
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}
</style>
