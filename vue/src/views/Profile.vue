<template>
  <div class="profile-page">
    <div class="profile-container">
      <!-- 左侧：用户信息 -->
      <div class="profile-left">
        <div class="user-card">
          <div class="avatar">{{ username.charAt(0).toUpperCase() }}</div>
          <h2 class="user-name">{{ username }}</h2>
          <p class="user-level">{{ discount }}折会员</p>
          <button class="edit-btn" @click="editProfile">编辑资料</button>
        </div>

        <!-- 快捷入口 -->
        <div class="quick-menu">
          <div class="quick-item" @click="router.push('/favorites')">
            <span class="quick-icon">❤️</span>
            <span>我的收藏</span>
          </div>
          <div class="quick-item" @click="router.push('/orders')">
            <span class="quick-icon">📋</span>
            <span>我的订单</span>
          </div>
          <div class="quick-item" @click="router.push('/notes')">
            <span class="quick-icon">📝</span>
            <span>读书笔记</span>
          </div>
          <div class="quick-item" @click="router.push('/messages')">
            <span class="quick-icon">🔔</span>
            <span>消息中心</span>
          </div>
        </div>

        <!-- 账户设置 -->
        <div class="settings-menu">
          <div class="setting-item" @click="changePassword">
            <span class="setting-icon">🔒</span>
            <span>修改密码</span>
            <span class="setting-arrow">›</span>
          </div>
        </div>

        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>

      <!-- 右侧：功能区域 -->
      <div class="profile-right">
        <!-- 资产概览 -->
        <div class="content-card">
          <h3 class="card-title">我的资产</h3>
          <div class="asset-grid">
            <div class="asset-item" @click="router.push('/coupons')">
              <span class="asset-num">{{ couponCount }}</span>
              <span class="asset-text">优惠券</span>
            </div>
            <div class="asset-item" @click="router.push('/points')">
              <span class="asset-num">{{ points }}</span>
              <span class="asset-text">积分</span>
            </div>
            <div class="asset-item" @click="router.push('/checkin')">
              <span class="asset-num">{{ consecutiveDays }}天</span>
              <span class="asset-text">连续签到</span>
            </div>
          </div>
        </div>

        <!-- 今日签到 -->
        <div class="content-card">
          <div class="card-header">
            <h3 class="card-title">每日签到</h3>
            <button class="checkin-btn" :class="{ done: todayChecked }" @click="doCheckIn" :disabled="todayChecked">
              {{ todayChecked ? '✅ 今日已签到' : '签到 +10积分' }}
            </button>
          </div>
          <p class="checkin-desc">连续签到7天+50积分，15天+150积分，30天+500积分</p>
        </div>

        <!-- 订单概览 -->
        <div class="content-card">
          <div class="card-header">
            <h3 class="card-title">我的订单</h3>
            <span class="card-link" @click="router.push('/orders')">查看全部 →</span>
          </div>
          <div class="order-grid">
            <div class="order-item" @click="router.push('/orders')">
              <span class="order-icon">💳</span>
              <span class="order-text">待支付</span>
              <span class="order-badge" v-if="orderStats.pending > 0">{{ orderStats.pending }}</span>
            </div>
            <div class="order-item" @click="router.push('/orders')">
              <span class="order-icon">📦</span>
              <span class="order-text">待发货</span>
            </div>
            <div class="order-item" @click="router.push('/orders')">
              <span class="order-icon">🚚</span>
              <span class="order-text">待收货</span>
            </div>
            <div class="order-item" @click="router.push('/orders')">
              <span class="order-icon">✅</span>
              <span class="order-text">已完成</span>
            </div>
          </div>
        </div>

        <!-- 收货地址 -->
        <div class="content-card">
          <div class="card-header">
            <h3 class="card-title">收货地址</h3>
            <span class="card-link" @click="router.push('/addresses')">管理 →</span>
          </div>
          <div v-if="addresses.length > 0" class="address-list">
            <div v-for="addr in addresses.slice(0, 2)" :key="addr.id" class="address-item">
              <div class="addr-info">
                <span class="addr-name">{{ addr.name }}</span>
                <span class="addr-phone">{{ addr.phone }}</span>
                <span v-if="addr.isDefault" class="addr-tag">默认</span>
              </div>
              <p class="addr-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail }}</p>
            </div>
          </div>
          <p v-else class="empty-text">暂无收货地址</p>
        </div>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog title="编辑资料" v-model="showEditModal" width="400px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="70px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditModal = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog title="修改密码" v-model="showPasswordModal" width="400px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="70px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input type="password" v-model="passwordForm.oldPassword" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input type="password" v-model="passwordForm.newPassword" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input type="password" v-model="passwordForm.confirmPassword" show-password></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordModal = false">取消</el-button>
        <el-button type="primary" @click="savePassword">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI, orderAPI, addressAPI, couponAPI, pointsAPI, checkInAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()

const username = ref(localStorage.getItem('username') || '')
const discount = ref(localStorage.getItem('discount') ? (parseFloat(localStorage.getItem('discount')) * 100).toFixed(0) : '95')

const orderStats = reactive({ total: 0, pending: 0, completed: 0 })
const addresses = ref([])
const couponCount = ref(0)
const points = ref(0)
const consecutiveDays = ref(0)
const todayChecked = ref(false)

const showEditModal = ref(false)
const showPasswordModal = ref(false)
const editFormRef = ref()
const passwordFormRef = ref()

const editForm = reactive({ username: '', email: '', phone: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const editRules = {
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: (r, v, cb) => v !== passwordForm.newPassword ? cb(new Error('两次密码不一致')) : cb(), trigger: 'blur' }]
}

const loadAll = async () => {
  try {
    const [ordersRes, addrRes, couponRes, pointsRes, checkinRes] = await Promise.all([
      orderAPI.getAll().catch(() => ({ data: [] })),
      addressAPI.getAll().catch(() => ({ data: [] })),
      couponAPI.getMyCoupons().catch(() => ({ data: [] })),
      pointsAPI.getMyPoints().catch(() => ({ data: { points: 0 } })),
      checkInAPI.getStatus().catch(() => ({ data: { consecutiveDays: 0, checkedInToday: false } }))
    ])

    const orders = ordersRes.data || []
    orderStats.total = orders.length
    orderStats.pending = orders.filter(o => o.status === 'PENDING').length
    orderStats.completed = orders.filter(o => o.status === 'COMPLETED').length

    addresses.value = (addrRes.data || []).slice(0, 3)
    couponCount.value = (couponRes.data || []).filter(c => c.status === 'UNUSED').length
    points.value = pointsRes.data.points || 0
    consecutiveDays.value = checkinRes.data.consecutiveDays || 0
    todayChecked.value = checkinRes.data.checkedInToday || false
  } catch (e) {}
}

const doCheckIn = async () => {
  try {
    await checkInAPI.checkIn()
    todayChecked.value = true
    consecutiveDays.value++
    points.value += 10
    ElMessage.success('签到成功！+10积分')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '签到失败')
  }
}

const editProfile = () => {
  editForm.username = username.value
  showEditModal.value = true
}

const changePassword = () => { showPasswordModal.value = true }

const saveProfile = async () => {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await authAPI.updateUser(editForm)
    username.value = editForm.username
    localStorage.setItem('username', editForm.username)
    showEditModal.value = false
    ElMessage.success('修改成功')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  }
}

const savePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await authAPI.changePassword(passwordForm)
    showPasswordModal.value = false
    ElMessage.success('密码修改成功，请重新登录')
    setTimeout(() => {
      localStorage.clear()
      router.push('/login')
    }, 1500)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  }
}

const handleLogout = () => {
  localStorage.clear()
  router.push('/login')
  window.location.reload()
}

onMounted(() => loadAll())
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f0e8 0%, #ebe3d5 100%);
  padding: 24px;
}

.profile-container {
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
}

/* 左侧用户信息 */
.profile-left {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-card {
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  color: white;
}

.avatar {
  width: 72px;
  height: 72px;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: bold;
  margin: 0 auto 12px;
  border: 3px solid rgba(255,255,255,0.3);
}

.user-name { font-size: 22px; font-weight: bold; margin: 0 0 4px; }
.user-level { font-size: 14px; opacity: 0.8; margin: 0 0 16px; }

.edit-btn {
  width: 100%;
  padding: 10px;
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.4);
  border-radius: 8px;
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
}

.edit-btn:hover { background: rgba(255,255,255,0.3); }

.quick-menu {
  background: white;
  border-radius: 12px;
  padding: 8px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 8px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #333;
  transition: background 0.2s;
}

.quick-item:hover { background: #f5f5f5; }
.quick-icon { font-size: 24px; }

.settings-menu {
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.setting-item {
  display: flex;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.setting-item:hover { background: #f5f5f5; }
.setting-icon { font-size: 20px; width: 28px; }
.setting-item span:nth-child(2) { flex: 1; font-size: 15px; }
.setting-arrow { font-size: 18px; color: #ccc; }

.logout-btn {
  width: 100%;
  padding: 14px;
  background: white;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  color: #ee0a24;
  cursor: pointer;
  transition: background 0.2s;
}

.logout-btn:hover { background: #fff5f5; }

/* 右侧内容区 */
.profile-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.content-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.card-link {
  font-size: 13px;
  color: #999;
  cursor: pointer;
}

.card-link:hover { color: var(--primary); }

/* 资产 */
.asset-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  text-align: center;
}

.asset-item {
  padding: 12px 0;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
}

.asset-item:hover { background: #f5f5f5; }

.asset-num {
  display: block;
  font-size: 28px;
  font-weight: bold;
  color: var(--primary);
}

.asset-text {
  display: block;
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

/* 签到 */
.checkin-btn {
  padding: 8px 20px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.checkin-btn:hover:not(.done) { background: #06ae56; transform: scale(1.05); }
.checkin-btn.done { background: #ccc; cursor: default; }

.checkin-desc {
  font-size: 13px;
  color: #999;
  margin: 12px 0 0;
}

/* 订单 */
.order-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  text-align: center;
}

.order-item {
  position: relative;
  padding: 16px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.order-item:hover { background: #f5f5f5; }

.order-icon { font-size: 28px; display: block; margin-bottom: 8px; }
.order-text { font-size: 13px; color: #666; }

.order-badge {
  position: absolute;
  top: 8px;
  right: 20%;
  min-width: 18px;
  height: 18px;
  background: #ee0a24;
  color: white;
  font-size: 11px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 地址 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.addr-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.addr-name { font-weight: 600; font-size: 15px; }
.addr-phone { color: #666; font-size: 14px; }

.addr-tag {
  font-size: 11px;
  background: var(--primary);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
}

.addr-detail {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.empty-text {
  font-size: 14px;
  color: #ccc;
  text-align: center;
  padding: 20px;
  margin: 0;
}

/* 响应式：手机端 */
@media (max-width: 768px) {
  .profile-container {
    grid-template-columns: 1fr;
  }
}
</style>
