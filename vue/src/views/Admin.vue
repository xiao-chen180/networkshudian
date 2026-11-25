<template>
  <div class="admin-dashboard">
    <!-- 顶部导航栏 -->
    <header class="top-header">
      <div class="header-left">
        <div class="logo">
          <span class="logo-icon">📚</span>
          <span class="logo-text">书香阁管理系统</span>
        </div>
      </div>
      <div class="header-right">
        <div class="admin-info">
          <span class="admin-avatar">{{ adminName.charAt(0) }}</span>
          <span class="admin-name">{{ adminName }}</span>
        </div>
        <button class="logout-btn" @click="logout">
          <span>🚪</span>
          <span>退出登录</span>
        </button>
      </div>
    </header>

    <div class="main-layout">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <nav class="sidebar-nav">
          <button
            v-for="menu in menus"
            :key="menu.key"
            class="nav-item"
            :class="{ active: activeMenu === menu.key }"
            @click="activeMenu = menu.key"
          >
            <span class="nav-icon">{{ menu.icon }}</span>
            <span class="nav-text">{{ menu.label }}</span>
            <span v-if="menu.badge" class="nav-badge">{{ menu.badge }}</span>
          </button>
        </nav>
      </aside>

      <!-- 主内容区 -->
      <main class="content-area">
        <!-- ========== 数据概览 ========== -->
        <div v-if="activeMenu === 'overview'" class="page-content">
          <div class="page-header">
            <h2>数据概览</h2>
            <p class="page-desc">实时监控书店运营数据</p>
          </div>

          <!-- 统计卡片 -->
          <div class="stats-grid">
            <div class="stat-card" v-for="stat in statsCards" :key="stat.key">
              <div class="stat-icon" :style="{ background: stat.bgColor }">
                <span>{{ stat.icon }}</span>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ stat.value }}</span>
                <span class="stat-label">{{ stat.label }}</span>
              </div>
              <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
                {{ stat.trend > 0 ? '↑' : '↓' }} {{ Math.abs(stat.trend) }}%
              </div>
            </div>
          </div>

          <!-- 图表区域 -->
          <div class="charts-row">
            <div class="chart-card">
              <h3>订单状态分布</h3>
              <div class="chart-content">
                <div class="donut-chart">
                  <div class="donut-center">
                    <span class="donut-value">{{ stats.totalOrders || 0 }}</span>
                    <span class="donut-label">总订单</span>
                  </div>
                  <svg viewBox="0 0 100 100" class="donut-svg">
                    <circle v-for="(item, index) in orderStatusData" :key="index"
                      cx="50" cy="50" r="40"
                      fill="none"
                      :stroke="item.color"
                      stroke-width="12"
                      :stroke-dasharray="item.dashArray"
                      :stroke-dashoffset="item.dashOffset"
                      :style="{ transition: 'all 0.6s ease' }"
                    />
                  </svg>
                </div>
                <div class="chart-legend">
                  <div v-for="item in orderStatusLegend" :key="item.key" class="legend-item">
                    <span class="legend-dot" :style="{ background: item.color }"></span>
                    <span class="legend-label">{{ item.label }}</span>
                    <span class="legend-value">{{ item.value }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="chart-card">
              <h3>库存预警</h3>
              <div class="stock-alerts">
                <div v-if="lowStockBooks.length === 0" class="empty-alert">
                  <span class="empty-icon">✅</span>
                  <p>库存充足，无需补货</p>
                </div>
                <div v-else class="alert-list">
                  <div v-for="book in lowStockBooks" :key="book.id" class="alert-item">
                    <div class="alert-book-info">
                      <span class="book-name">{{ book.name }}</span>
                      <span class="book-author">{{ book.author }}</span>
                    </div>
                    <div class="alert-stock">
                      <span class="stock-count" :class="{ 'critical': book.stock < 5 }">
                        {{ book.stock }}
                      </span>
                      <span class="stock-label">件</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 最近订单 -->
          <div class="recent-section">
            <div class="section-header">
              <h3>最近订单</h3>
              <button class="view-all-btn" @click="activeMenu = 'orders'">查看全部 →</button>
            </div>
            <div class="recent-orders">
              <div v-for="order in recentOrders" :key="order.id" class="order-card">
                <div class="order-header">
                  <span class="order-id">#{{ order.id }}</span>
                  <span class="status-tag" :class="'status-' + order.status.toLowerCase()">
                    {{ statusText(order.status) }}
                  </span>
                </div>
                <div class="order-body">
                  <div class="order-user">
                    <span class="user-avatar">{{ (order.username || 'U').charAt(0) }}</span>
                    <span>{{ order.username }}</span>
                  </div>
                  <div class="order-items">
                    <span v-for="(item, idx) in order.items.slice(0, 2)" :key="idx" class="item-tag">
                      {{ item.bookName }} x{{ item.quantity }}
                    </span>
                    <span v-if="order.items.length > 2" class="more-items">+{{ order.items.length - 2 }}</span>
                  </div>
                </div>
                <div class="order-footer">
                  <span class="order-price">¥{{ order.totalPrice?.toFixed(2) }}</span>
                  <span class="order-time">{{ formatDate(order.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ========== 书籍管理 ========== -->
        <div v-if="activeMenu === 'books'" class="page-content">
          <div class="page-header">
            <div>
              <h2>书籍管理</h2>
              <p class="page-desc">管理所有书籍信息</p>
            </div>
            <div class="header-actions">
              <button class="btn btn-outline" @click="showPriceDialog = true">批量调价</button>
              <button class="btn btn-primary" @click="openBookDialog(null)">+ 上架新书</button>
            </div>
          </div>

          <div class="search-filter-bar">
            <div class="search-box">
              <span class="search-icon">🔍</span>
              <input v-model="bookSearch" placeholder="搜索书名、作者或ISBN..." />
            </div>
            <select v-model="bookCategoryFilter" class="filter-select">
              <option value="">全部分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </div>

          <div class="data-table-card">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>书名</th>
                  <th>作者</th>
                  <th>价格</th>
                  <th>库存</th>
                  <th>分类</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="book in filteredBooks" :key="book.id">
                  <td>{{ book.id }}</td>
                  <td class="cell-bold">{{ book.name }}</td>
                  <td>{{ book.author }}</td>
                  <td class="cell-price">¥{{ book.price }}</td>
                  <td>
                    <span class="stock-badge" :class="book.stock < 10 ? 'low' : 'ok'">
                      {{ book.stock }}
                    </span>
                  </td>
                  <td>{{ book.categoryName }}</td>
                  <td class="cell-actions">
                    <button class="action-btn edit" @click="openBookDialog(book)">编辑</button>
                    <button class="action-btn delete" @click="deleteBook(book)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="filteredBooks.length === 0" class="empty-state">
              <span class="empty-icon">📚</span>
              <p>暂无书籍数据</p>
            </div>
          </div>
        </div>

        <!-- ========== 订单管理 ========== -->
        <div v-if="activeMenu === 'orders'" class="page-content">
          <div class="page-header">
            <h2>订单管理</h2>
            <p class="page-desc">处理和跟踪所有订单</p>
          </div>

          <div class="order-filters">
            <button
              v-for="filter in orderFilters"
              :key="filter.key"
              class="filter-btn"
              :class="{ active: orderFilter === filter.key }"
              @click="orderFilter = filter.key"
            >
              <span>{{ filter.label }}</span>
              <span class="filter-count">{{ filter.count }}</span>
            </button>
          </div>

          <div class="orders-list">
            <div v-for="order in filteredOrders" :key="order.id" class="order-detail-card">
              <div class="order-detail-header">
                <div class="order-id-section">
                  <span class="order-id">#{{ order.id }}</span>
                  <span class="status-tag" :class="'status-' + order.status.toLowerCase()">
                    {{ statusText(order.status) }}
                  </span>
                </div>
                <span class="order-date">{{ formatDate(order.createdAt) }}</span>
              </div>
              <div class="order-detail-body">
                <div class="order-customer">
                  <span class="customer-avatar">{{ (order.username || 'U').charAt(0) }}</span>
                  <div class="customer-info">
                    <span class="customer-name">{{ order.username }}</span>
                    <span class="customer-account">{{ order.account }}</span>
                  </div>
                </div>
                <div class="order-product-list">
                  <div v-for="(item, idx) in order.items" :key="idx" class="product-item">
                    <span class="product-name">{{ item.bookName }}</span>
                    <span class="product-qty">x{{ item.quantity }}</span>
                    <span class="product-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                  </div>
                </div>
                <div class="order-total">
                  <span>订单总额</span>
                  <span class="total-price">¥{{ order.totalPrice?.toFixed(2) }}</span>
                </div>
              </div>
              <div class="order-detail-footer">
                <div class="order-actions">
                  <button v-if="order.status === 'PAID'" class="action-btn primary" @click="shipOrder(order.id)">
                    📦 发货
                  </button>
                  <button v-if="order.status === 'SHIPPED'" class="action-btn success" @click="completeOrder(order.id)">
                    ✅ 完成
                  </button>
                  <button v-if="order.status !== 'CANCELLED' && order.status !== 'COMPLETED'" class="action-btn danger" @click="cancelOrder(order.id)">
                    ❌ 取消
                  </button>
                </div>
              </div>
            </div>
            <div v-if="filteredOrders.length === 0" class="empty-state">
              <span class="empty-icon">📋</span>
              <p>暂无订单数据</p>
            </div>
          </div>
        </div>

        <!-- ========== 用户管理 ========== -->
        <div v-if="activeMenu === 'users'" class="page-content">
          <div class="page-header">
            <h2>用户管理</h2>
            <p class="page-desc">管理所有注册用户</p>
          </div>

          <div class="users-grid">
            <div v-for="user in users" :key="user.id" class="user-card">
              <div class="user-card-header">
                <div class="user-avatar-large" :class="user.role === 'ADMIN' ? 'admin' : 'user'">
                  {{ user.username.charAt(0) }}
                </div>
                <div class="user-role-badge" :class="user.role === 'ADMIN' ? 'admin' : 'user'">
                  {{ user.role === 'ADMIN' ? '管理员' : '用户' }}
                </div>
              </div>
              <div class="user-card-body">
                <h4 class="user-name">{{ user.username }}</h4>
                <p class="user-account">@{{ user.account }}</p>
                <div class="user-stats">
                  <div class="user-stat">
                    <span class="stat-num">{{ user.orderCount || 0 }}</span>
                    <span class="stat-text">订单</span>
                  </div>
                  <div class="user-stat">
                    <span class="stat-num">{{ formatDiscount(user.discount) }}</span>
                    <span class="stat-text">折扣</span>
                  </div>
                </div>
              </div>
              <div class="user-card-footer">
                <span class="join-date">注册于 {{ formatDate(user.createdAt) }}</span>
                <button
                  v-if="user.role === 'USER'"
                  class="action-btn small primary"
                  @click="setAdmin(user)"
                >
                  设为管理员
                </button>
                <button
                  v-if="user.role === 'ADMIN' && user.account !== adminAccount"
                  class="action-btn small danger"
                  @click="setUser(user)"
                >
                  取消管理员
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 书籍编辑弹窗 -->
    <div v-if="showBookDialog" class="modal-overlay" @click.self="showBookDialog = false">
      <div class="modal-content book-modal">
        <div class="modal-header">
          <h3>{{ editingBook.id ? '编辑书籍' : '上架新书' }}</h3>
          <button class="close-btn" @click="showBookDialog = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-grid">
            <div class="form-group">
              <label>书名 *</label>
              <input v-model="editingBook.name" placeholder="请输入书名" />
            </div>
            <div class="form-group">
              <label>作者 *</label>
              <input v-model="editingBook.author" placeholder="请输入作者" />
            </div>
            <div class="form-group">
              <label>出版社</label>
              <input v-model="editingBook.publisher" placeholder="请输入出版社" />
            </div>
            <div class="form-group">
              <label>ISBN *</label>
              <input v-model="editingBook.isbn" placeholder="请输入ISBN" />
            </div>
            <div class="form-group">
              <label>价格 *</label>
              <input v-model="editingBook.price" type="number" step="0.01" placeholder="0.00" />
            </div>
            <div class="form-group">
              <label>库存 *</label>
              <input v-model="editingBook.stock" type="number" placeholder="0" />
            </div>
            <div class="form-group">
              <label>分类</label>
              <select v-model="editingBook.categoryId">
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>页数</label>
              <input v-model="editingBook.pages" type="number" placeholder="0" />
            </div>
          </div>
          <div class="form-group full-width">
            <label>简介</label>
            <textarea v-model="editingBook.summary" rows="3" placeholder="请输入书籍简介"></textarea>
          </div>
          <div class="form-group full-width">
            <label>目录</label>
            <textarea v-model="editingBook.catalog" rows="3" placeholder="请输入目录"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showBookDialog = false">取消</button>
          <button class="btn btn-primary" @click="saveBook" :disabled="savingBook">保存</button>
        </div>
      </div>
    </div>

    <!-- 批量调价弹窗 -->
    <div v-if="showPriceDialog" class="modal-overlay" @click.self="showPriceDialog = false">
      <div class="modal-content price-modal">
        <div class="modal-header">
          <h3>批量调整价格</h3>
          <button class="close-btn" @click="showPriceDialog = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>调整范围</label>
            <select v-model="priceForm.categoryId">
              <option :value="null">全部书籍</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>调整方式</label>
            <div class="radio-group">
              <label class="radio-label">
                <input type="radio" v-model="priceForm.type" value="fixed" /> 固定金额
              </label>
              <label class="radio-label">
                <input type="radio" v-model="priceForm.type" value="percent" /> 百分比
              </label>
            </div>
          </div>
          <div class="form-group">
            <label>{{ priceForm.type === 'fixed' ? '调整金额' : '调整百分比' }}</label>
            <input v-model="priceForm.adjustment" type="number" step="0.01" placeholder="0" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showPriceDialog = false">取消</button>
          <button class="btn btn-warning" @click="batchUpdatePrice">确认调价</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI, categoryAPI } from '../api'

const router = useRouter()

const activeMenu = ref('overview')
const menus = [
  { key: 'overview', icon: '📊', label: '数据概览' },
  { key: 'books', icon: '📚', label: '书籍管理' },
  { key: 'orders', icon: '📋', label: '订单管理' },
  { key: 'users', icon: '👥', label: '用户管理' }
]

const adminName = ref(localStorage.getItem('username') || '管理员')
const adminAccount = ref(localStorage.getItem('account') || '')

// ========== 数据 ==========
const stats = ref({})
const books = ref([])
const orders = ref([])
const users = ref([])
const categories = ref([])

const bookSearch = ref('')
const bookCategoryFilter = ref('')
const orderFilter = ref('ALL')

// ========== 弹窗 ==========
const showBookDialog = ref(false)
const showPriceDialog = ref(false)
const savingBook = ref(false)
const editingBook = ref({})
const priceForm = reactive({
  categoryId: null,
  type: 'fixed',
  adjustment: 0
})

// ========== 计算属性 ==========
const statsCards = computed(() => [
  {
    key: 'revenue',
    icon: '💰',
    label: '总营收',
    value: '¥' + (stats.value.totalRevenue?.toFixed(2) || '0.00'),
    bgColor: 'linear-gradient(135deg, #8b5a2b 0%, #a0522d 100%)',
    trend: 12.5
  },
  {
    key: 'orders',
    icon: '📦',
    label: '总订单',
    value: stats.value.totalOrders || 0,
    bgColor: 'linear-gradient(135deg, #6b4423 0%, #8b5a2b 100%)',
    trend: 8.2
  },
  {
    key: 'users',
    icon: '👥',
    label: '注册用户',
    value: stats.value.totalUsers || 0,
    bgColor: 'linear-gradient(135deg, #4a3728 0%, #6b4423 100%)',
    trend: 15.3
  },
  {
    key: 'books',
    icon: '📚',
    label: '书籍总数',
    value: stats.value.totalBooks || 0,
    bgColor: 'linear-gradient(135deg, #d4a574 0%, #8b5a2b 100%)',
    trend: -2.1
  }
])

const filteredBooks = computed(() => {
  let result = books.value
  if (bookCategoryFilter.value) {
    result = result.filter(b => b.categoryId == bookCategoryFilter.value)
  }
  if (bookSearch.value) {
    const kw = bookSearch.value.toLowerCase()
    result = result.filter(b =>
      b.name.toLowerCase().includes(kw) ||
      b.author.toLowerCase().includes(kw) ||
      (b.isbn && b.isbn.includes(kw))
    )
  }
  return result
})

const filteredOrders = computed(() => {
  if (orderFilter.value === 'ALL') return orders.value
  return orders.value.filter(o => o.status === orderFilter.value)
})

const recentOrders = computed(() => orders.value.slice(0, 5))

const lowStockBooks = computed(() => books.value.filter(b => b.stock < 10).slice(0, 5))

const orderFilters = computed(() => [
  { key: 'ALL', label: '全部', count: orders.value.length },
  { key: 'PENDING', label: '待付款', count: orders.value.filter(o => o.status === 'PENDING').length },
  { key: 'PAID', label: '已付款', count: orders.value.filter(o => o.status === 'PAID').length },
  { key: 'SHIPPED', label: '已发货', count: orders.value.filter(o => o.status === 'SHIPPED').length },
  { key: 'COMPLETED', label: '已完成', count: orders.value.filter(o => o.status === 'COMPLETED').length },
  { key: 'CANCELLED', label: '已取消', count: orders.value.filter(o => o.status === 'CANCELLED').length }
])

const orderStatusData = computed(() => {
  const total = stats.value.totalOrders || 1
  const items = [
    { key: 'pending', value: stats.value.pendingOrders || 0, color: '#faad14' },
    { key: 'paid', value: stats.value.paidOrders || 0, color: '#4facfe' },
    { key: 'shipped', value: stats.value.shippedOrders || 0, color: '#9254de' },
    { key: 'completed', value: stats.value.completedOrders || 0, color: '#43e97b' },
    { key: 'cancelled', value: stats.value.cancelledOrders || 0, color: '#ff4d4f' }
  ]
  const circumference = 2 * Math.PI * 40
  let offset = 0
  return items.map(item => {
    const percent = item.value / total
    const dashArray = `${circumference * percent} ${circumference * (1 - percent)}`
    const dashOffset = -offset
    offset += circumference * percent
    return { ...item, dashArray, dashOffset }
  })
})

const orderStatusLegend = computed(() => [
  { key: 'pending', label: '待付款', value: stats.value.pendingOrders || 0, color: '#faad14' },
  { key: 'paid', label: '已付款', value: stats.value.paidOrders || 0, color: '#4facfe' },
  { key: 'shipped', label: '已发货', value: stats.value.shippedOrders || 0, color: '#9254de' },
  { key: 'completed', label: '已完成', value: stats.value.completedOrders || 0, color: '#43e97b' },
  { key: 'cancelled', label: '已取消', value: stats.value.cancelledOrders || 0, color: '#ff4d4f' }
])

// ========== 方法 ==========
const statusText = (status) => {
  const map = { PENDING: '待付款', PAID: '已付款', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const logout = () => {
  localStorage.clear()
  router.push('/admin-login')
}

// ========== 加载数据 ==========
const loadDashboard = async () => {
  try {
    const res = await adminAPI.getDashboard()
    stats.value = res.data
  } catch (e) {
    console.error('加载仪表板失败:', e)
  }
}

const loadBooks = async () => {
  try {
    const res = await adminAPI.getAllBooks()
    books.value = Array.isArray(res.data) ? res.data : (res.data.content || [])
  } catch (e) {
    console.error('加载书籍失败:', e)
  }
}

const loadOrders = async () => {
  try {
    const res = await adminAPI.getAllOrders()
    orders.value = res.data || []
  } catch (e) {
    console.error('加载订单失败:', e)
  }
}

const loadUsers = async () => {
  try {
    const res = await adminAPI.getAllUsers()
    users.value = res.data || []
  } catch (e) {
    console.error('加载用户失败:', e)
  }
}

const loadCategories = async () => {
  try {
    const res = await categoryAPI.getAll()
    categories.value = res.data || []
  } catch (e) {
    console.error('加载分类失败:', e)
  }
}

const loadData = () => {
  loadDashboard()
  loadBooks()
  loadOrders()
  loadUsers()
  loadCategories()
}

// ========== 书籍操作 ==========
const openBookDialog = (book) => {
  if (book) {
    editingBook.value = { ...book }
  } else {
    editingBook.value = { name: '', author: '', publisher: '', isbn: '', price: 0, stock: 0, pages: 0, summary: '', catalog: '', categoryId: null }
  }
  showBookDialog.value = true
}

const formatDiscount = (value) => {
  const num = Number(value)
  return Number.isFinite(num) && num > 0 ? `${Math.round(num * 100)}折` : '—'
}

const saveBook = async () => {
  if (savingBook.value) return
  if (!editingBook.value.name || !editingBook.value.author || !editingBook.value.isbn) {
    ElMessage.warning('请填写必要字段')
    return
  }
  savingBook.value = true
  try {
    if (editingBook.value.id) {
      await adminAPI.updateBook(editingBook.value.id, editingBook.value)
      ElMessage.success('书籍更新成功')
    } else {
      await adminAPI.createBook(editingBook.value)
      ElMessage.success('书籍上架成功')
    }
    showBookDialog.value = false
    loadBooks()
    loadDashboard()
  } catch (e) {
    ElMessage.error('操作失败: ' + (e.response?.data?.message || e.message))
  } finally {
    savingBook.value = false
  }
}

const deleteBook = async (book) => {
  try {
    await ElMessageBox.confirm(`确定要删除《${book.name}》吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminAPI.deleteBook(book.id)
    ElMessage.success('删除成功')
    loadBooks()
    loadDashboard()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const batchUpdatePrice = async () => {
  if (!priceForm.adjustment) {
    ElMessage.warning('请输入调整金额')
    return
  }
  try {
    const res = await adminAPI.batchUpdatePrice(priceForm)
    ElMessage.success(res.data.message)
    showPriceDialog.value = false
    loadBooks()
  } catch (e) {
    ElMessage.error('调价失败')
  }
}

// ========== 订单操作 ==========
const shipOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认发货？', '发货确认', { type: 'info' })
    await adminAPI.shipOrder(orderId)
    ElMessage.success('发货成功')
    loadOrders()
    loadDashboard()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const completeOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认订单已完成？', '确认完成', { type: 'info' })
    await adminAPI.completeOrder(orderId)
    ElMessage.success('订单已完成')
    loadOrders()
    loadDashboard()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const cancelOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '取消订单', { type: 'warning' })
    await adminAPI.cancelOrder(orderId)
    ElMessage.success('订单已取消')
    loadOrders()
    loadDashboard()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

// ========== 用户操作 ==========
const setAdmin = async (user) => {
  try {
    await ElMessageBox.confirm(`确定将 ${user.username} 设为管理员吗？`, '确认操作', { type: 'warning' })
    await adminAPI.updateUserRole(user.id, 'ADMIN')
    ElMessage.success('操作成功')
    loadUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const setUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定取消 ${user.username} 的管理员权限吗？`, '确认操作', { type: 'warning' })
    await adminAPI.updateUserRole(user.id, 'USER')
    ElMessage.success('操作成功')
    loadUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background: #f0f2f5;
}

/* 顶部导航栏 */
.top-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: linear-gradient(135deg, #4a3728 0%, #6b4423 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left .logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: white;
  letter-spacing: 2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
}

.admin-avatar {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.admin-name {
  font-size: 14px;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 主布局 */
.main-layout {
  display: flex;
  min-height: calc(100vh - 64px);
}

/* 侧边栏 */
.sidebar {
  width: 220px;
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  padding: 16px 0;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 0 12px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 16px;
  background: none;
  border: none;
  border-radius: 8px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: left;
  position: relative;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: #8b5a2b;
  border-radius: 0 2px 2px 0;
  transition: height 0.3s ease;
}

.nav-item:hover {
  background: #f5f5f5;
  color: #333;
  transform: translateX(4px);
}

.nav-item:hover .nav-icon {
  transform: scale(1.15);
  transition: transform 0.3s var(--ease-bounce);
}

.nav-item.active {
  background: linear-gradient(135deg, #8b5a2b 0%, #a0522d 100%);
  color: white;
  font-weight: 600;
}

.nav-item.active::before {
  height: 60%;
}

.nav-icon {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.nav-text {
  flex: 1;
}

.nav-badge {
  background: #ff4d4f;
  color: white;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
  animation: badgeBounce 0.5s var(--ease-bounce);
}

@keyframes badgeBounce {
  0% { transform: scale(0); }
  60% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

/* 内容区 */
.content-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.page-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  color: #4a3728;
  margin: 0 0 4px;
}

.page-desc {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
  transform: translateY(20px);
  animation: adminCardEnter 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.stat-card:nth-child(1) { animation-delay: 0.1s; }
.stat-card:nth-child(2) { animation-delay: 0.2s; }
.stat-card:nth-child(3) { animation-delay: 0.3s; }
.stat-card:nth-child(4) { animation-delay: 0.4s; }

@keyframes adminCardEnter {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  transition: transform 0.3s var(--ease-bounce);
}

.stat-card:hover .stat-icon {
  transform: scale(1.12) rotate(-3deg);
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  transition: transform 0.3s var(--ease-bounce);
}

.stat-card:hover .stat-value {
  transform: scale(1.05);
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

.stat-trend {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 6px;
  transition: transform 0.3s ease;
}

.stat-card:hover .stat-trend {
  transform: scale(1.05);
}

.stat-trend.up {
  color: #52c41a;
  background: #f6ffed;
}

.stat-trend.down {
  color: #ff4d4f;
  background: #fff1f0;
}

/* 图表区域 */
.charts-row {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  opacity: 0;
  animation: adminCardEnter 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  animation-delay: 0.5s;
}

.chart-card:nth-child(2) {
  animation-delay: 0.6s;
}

.chart-card h3 {
  font-size: 16px;
  color: #333;
  margin: 0 0 20px;
}

.chart-content {
  display: flex;
  align-items: center;
  gap: 30px;
}

.donut-chart {
  position: relative;
  width: 160px;
  height: 160px;
}

.donut-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.donut-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #333;
  animation: countFadeIn 0.8s ease forwards;
}

@keyframes countFadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.donut-label {
  font-size: 12px;
  color: #999;
}

.donut-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.donut-svg circle {
  transition: stroke-dashoffset 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.chart-legend {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.legend-item:hover {
  background: rgba(0, 0, 0, 0.03);
  transform: translateX(4px);
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 4px;
  transition: transform 0.3s var(--ease-bounce);
}

.legend-item:hover .legend-dot {
  transform: scale(1.3);
}

.legend-label {
  flex: 1;
  font-size: 13px;
  color: #666;
}

.legend-value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

/* 库存预警 */
.stock-alerts {
  min-height: 200px;
}

.empty-alert {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #999;
}

.empty-alert .empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #faf7f2;
  border-radius: 8px;
  border-left: 3px solid #8b5a2b;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.alert-item:hover {
  background: #f5f0e8;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(139, 90, 43, 0.1);
}

.alert-book-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.book-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.book-author {
  font-size: 12px;
  color: #999;
}

.alert-stock {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stock-count {
  font-size: 24px;
  font-weight: 700;
  color: #8b5a2b;
}

.stock-count.critical {
  color: #c21500;
}

.stock-label {
  font-size: 12px;
  color: #999;
}

/* 最近订单 */
.recent-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  font-size: 16px;
  color: #333;
  margin: 0;
}

.view-all-btn {
  background: none;
  border: none;
  color: #8b5a2b;
  font-size: 14px;
  cursor: pointer;
}

.view-all-btn:hover {
  text-decoration: underline;
}

.recent-orders {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.order-card {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
  animation: adminCardEnter 0.4s ease forwards;
}

.order-card:nth-child(1) { animation-delay: 0.7s; }
.order-card:nth-child(2) { animation-delay: 0.78s; }
.order-card:nth-child(3) { animation-delay: 0.86s; }
.order-card:nth-child(4) { animation-delay: 0.94s; }
.order-card:nth-child(5) { animation-delay: 1.02s; }

.order-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.order-id {
  font-weight: 600;
  color: #333;
}

.order-body {
  margin-bottom: 10px;
}

.order-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.user-avatar {
  width: 24px;
  height: 24px;
  background: #8b5a2b;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.order-items {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.item-tag {
  font-size: 11px;
  padding: 2px 6px;
  background: #e8e8e8;
  border-radius: 4px;
  color: #666;
}

.more-items {
  font-size: 11px;
  color: #999;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-price {
  font-weight: 700;
  color: #8b5a2b;
}

.order-time {
  font-size: 11px;
  color: #999;
}

/* 搜索过滤栏 */
.search-filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  flex: 1;
  max-width: 400px;
}

.search-icon {
  color: #999;
}

.search-box input {
  border: none;
  outline: none;
  flex: 1;
  font-size: 14px;
}

.filter-select {
  padding: 10px 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  outline: none;
}

/* 数据表格 */
.data-table-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  padding: 14px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: #666;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.data-table td {
  padding: 14px 16px;
  font-size: 14px;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
}

.data-table tr:hover td {
  background: #fafafa;
}

.cell-bold { font-weight: 600; }
.cell-price { font-weight: 700; color: #8b5a2b; }

.cell-actions {
  display: flex;
  gap: 8px;
}

.stock-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.stock-badge.low {
  background: #fff1f0;
  color: #ff4d4f;
}

.stock-badge.ok {
  background: #f6ffed;
  color: #52c41a;
}

/* 操作按钮 */
.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.edit {
  background: #e6f7ff;
  color: #1890ff;
}

.action-btn.edit:hover { background: #bae7ff; }

.action-btn.delete {
  background: #fff1f0;
  color: #ff4d4f;
}

.action-btn.delete:hover { background: #ffccc7; }

.action-btn.primary {
  background: #8b5a2b;
  color: white;
}

.action-btn.primary:hover { background: #a0522d; }

.action-btn.success {
  background: #52c41a;
  color: white;
}

.action-btn.success:hover { background: #73d13d; }

.action-btn.danger {
  background: #ff4d4f;
  color: white;
}

.action-btn.danger:hover { background: #ff7875; }

.action-btn.small {
  padding: 4px 10px;
  font-size: 11px;
}

/* 订单过滤器 */
.order-filters {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  border-color: #8b5a2b;
  color: #8b5a2b;
}

.filter-btn.active {
  background: #8b5a2b;
  border-color: #8b5a2b;
  color: white;
}

.filter-count {
  font-size: 11px;
  padding: 1px 6px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 10px;
}

.filter-btn.active .filter-count {
  background: rgba(255, 255, 255, 0.2);
}

/* 订单列表 */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-detail-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.order-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.order-id-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.order-id {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.order-date {
  font-size: 13px;
  color: #999;
}

.order-detail-body {
  padding: 20px;
}

.order-customer {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.customer-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

.customer-info {
  display: flex;
  flex-direction: column;
}

.customer-name {
  font-weight: 600;
  color: #333;
}

.customer-account {
  font-size: 12px;
  color: #999;
}

.order-product-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-name {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.product-qty {
  font-size: 13px;
  color: #666;
}

.product-price {
  font-weight: 600;
  color: #333;
}

.order-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px dashed #e8e8e8;
}

.total-price {
  font-size: 18px;
  font-weight: 700;
  color: #8b5a2b;
}

.order-detail-footer {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}

.order-actions {
  display: flex;
  gap: 10px;
}

/* 用户网格 */
.users-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.user-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: transform 0.2s;
}

.user-card:hover {
  transform: translateY(-4px);
}

.user-card-header {
  position: relative;
  padding: 24px;
  background: linear-gradient(135deg, #4a3728 0%, #6b4423 100%);
  text-align: center;
}

.user-avatar-large {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
  color: white;
  margin: 0 auto 12px;
  border: 3px solid white;
}

.user-avatar-large.admin {
  background: linear-gradient(135deg, #8b5a2b 0%, #a0522d 100%);
}

.user-avatar-large.user {
  background: linear-gradient(135deg, #6b4423 0%, #8b5a2b 100%);
}

.user-role-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.user-role-badge.admin {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.user-role-badge.user {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.user-card-body {
  padding: 20px;
  text-align: center;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px;
}

.user-account {
  font-size: 13px;
  color: #999;
  margin: 0 0 16px;
}

.user-stats {
  display: flex;
  justify-content: center;
  gap: 30px;
}

.user-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.stat-text {
  font-size: 12px;
  color: #999;
}

.user-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
}

.join-date {
  font-size: 11px;
  color: #999;
}

/* 状态标签 */
.status-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.status-pending { background: #fff7e6; color: #d48806; }
.status-paid { background: #e6f7ff; color: #1890ff; }
.status-shipped { background: #f9f0ff; color: #722ed1; }
.status-completed { background: #f6ffed; color: #52c41a; }
.status-cancelled { background: #fff1f0; color: #ff4d4f; }

/* 按钮 */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn:active { transform: scale(0.97); }

.btn-primary {
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  color: white;
}

.btn-primary:hover {
  box-shadow: 0 4px 12px rgba(139, 90, 43, 0.4);
}

.btn-outline {
  background: white;
  border: 1px solid #d9d9d9;
  color: #333;
}

.btn-outline:hover {
  border-color: #8b5a2b;
  color: #8b5a2b;
}

.btn-warning {
  background: linear-gradient(135deg, #faad14, #d48806);
  color: white;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state .empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.2s ease;
}

.modal-content {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-height: 85vh;
  overflow-y: auto;
  animation: slideUp 0.25s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.book-modal { width: 640px; }
.price-modal { width: 440px; }

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover { color: #333; }

.modal-body { padding: 24px; }

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #8b5a2b;
  box-shadow: 0 0 0 2px rgba(139, 90, 43, 0.1);
}

.radio-group {
  display: flex;
  gap: 20px;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
}
</style>
