<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><span>个人中心</span></div>
      <div class="pg-head">
        <div>
          <h1>个人中心</h1>
          <div class="sub">会员等级与权益、订单与地址都在这里</div>
        </div>
        <button class="btn-line sm" @click="handleLogout">退出登录</button>
      </div>

      <div class="layout">
        <!-- 左：用户卡 -->
        <aside class="side">
          <div class="usercard">
            <div class="avatar">{{ initial }}</div>
            <div class="name">{{ username }}</div>
            <div class="account">{{ account }}</div>
            <div class="badge">{{ discountLabel }} 会员</div>
            <button class="edit" @click="editProfile">编辑资料</button>
            <button class="edit ghost" @click="showPasswordModal = true">修改密码</button>
          </div>

          <div class="checkin-card">
            <div class="ck-top">
              <span>已连续签到</span>
              <b class="mono">{{ consecutiveDays }}<i>天</i></b>
            </div>
            <div class="ck-note">连续 7 天解锁签到抽奖</div>
            <button class="ck-btn" :disabled="todayChecked" @click="doCheckIn">
              {{ todayChecked ? '今日已签到 · +10 积分' : '立即签到 +10 积分' }}
            </button>
          </div>
        </aside>

        <!-- 右 -->
        <main class="main">
          <section class="panel">
            <div class="panel-h"><h3>资产概览</h3></div>
            <div class="stats">
              <router-link to="/orders" class="stat">
                <b class="mono">{{ orderStats.total }}</b><span>全部订单</span>
              </router-link>
              <router-link to="/orders" class="stat">
                <b class="mono">{{ orderStats.pending }}</b><span>待付款</span>
              </router-link>
              <router-link to="/points" class="stat">
                <b class="mono">{{ points }}</b><span>积分</span>
              </router-link>
              <router-link to="/coupons" class="stat">
                <b class="mono">{{ couponCount }}</b><span>可用优惠券</span>
              </router-link>
            </div>
          </section>

          <section class="panel">
            <div class="panel-h"><h3>常用入口</h3></div>
            <div class="links">
              <router-link v-for="l in links" :key="l.to" :to="l.to" class="link">
                <span>{{ l.label }}</span><em>→</em>
              </router-link>
            </div>
          </section>

          <section class="panel">
            <div class="panel-h">
              <h3>收货地址</h3>
              <router-link to="/addresses" class="act">管理地址 →</router-link>
            </div>
            <div v-if="addresses.length" class="addrs">
              <div class="addr" v-for="a in addresses" :key="a.id">
                <div class="who">{{ a.name }}<span>{{ maskPhone(a.phone) }}</span>
                  <i class="tag gold" v-if="a.isDefault">默认</i>
                </div>
                <div class="where">{{ a.province }}{{ a.city }}{{ a.district || '' }}{{ a.detail }}</div>
              </div>
            </div>
            <div class="blank" v-else>还没有收货地址</div>
          </section>
        </main>
      </div>
    </div>

    <!-- 编辑资料 -->
    <el-dialog v-model="showEditModal" title="编辑资料" width="440px" class="nv-dialog">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-position="top">
        <el-form-item label="昵称" prop="username"><el-input v-model="editForm.username" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="editForm.email" /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="editForm.phone" /></el-form-item>
      </el-form>
      <template #footer>
        <button class="dlg-btn" @click="showEditModal = false">取消</button>
        <button class="dlg-btn gold" @click="saveProfile">保存</button>
      </template>
    </el-dialog>

    <!-- 修改密码 -->
    <el-dialog v-model="showPasswordModal" title="修改密码" width="440px" class="nv-dialog">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
        <el-form-item label="原密码" prop="oldPassword"><el-input type="password" v-model="passwordForm.oldPassword" /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"><el-input type="password" v-model="passwordForm.newPassword" /></el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword"><el-input type="password" v-model="passwordForm.confirmPassword" /></el-form-item>
      </el-form>
      <template #footer>
        <button class="dlg-btn" @click="showPasswordModal = false">取消</button>
        <button class="dlg-btn gold" @click="savePassword">确认修改</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderAPI, addressAPI, couponAPI, pointsAPI, checkInAPI, authAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()

const username = ref(localStorage.getItem('username') || '读者')
const account = ref(localStorage.getItem('account') || '')
const discount = ref(localStorage.getItem('discount') || '0.95')
const initial = computed(() => (username.value || '读').trim().charAt(0))
const discountLabel = computed(() => {
  const d = parseFloat(discount.value)
  return Number.isFinite(d) ? `${(d * 10).toFixed(1).replace('.0', '')} 折` : '95 折'
})

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
  username: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (r, v, cb) => (v !== passwordForm.newPassword ? cb(new Error('两次输入不一致')) : cb()), trigger: 'blur' }
  ]
}

const links = [
  { to: '/orders', label: '我的订单' },
  { to: '/favorites', label: '我的收藏' },
  { to: '/notes', label: '读书笔记' },
  { to: '/coupons', label: '优惠券' },
  { to: '/points', label: '积分中心' },
  { to: '/checkin', label: '每日签到' },
  { to: '/messages', label: '消息中心' },
  { to: '/after-sales', label: '售后服务' },
  { to: '/vip', label: '会员中心' }
]

const maskPhone = (p) => (p ? String(p).replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '')

async function loadAll() {
  const [ordersRes, addrRes, couponRes, pointsRes, checkinRes] = await Promise.all([
    orderAPI.getAll().catch(() => ({ data: [] })),
    addressAPI.getAll().catch(() => ({ data: [] })),
    couponAPI.getMyCoupons().catch(() => ({ data: [] })),
    pointsAPI.getMyPoints().catch(() => ({ data: { points: 0 } })),
    checkInAPI.getStatus().catch(() => ({ data: {} }))
  ])
  const orders = ordersRes.data || []
  orderStats.total = orders.length
  orderStats.pending = orders.filter(o => o.status === 'PENDING').length
  orderStats.completed = orders.filter(o => o.status === 'COMPLETED').length
  addresses.value = (addrRes.data || []).slice(0, 3)
  couponCount.value = (couponRes.data || []).filter(c => c.status === 'UNUSED').length
  points.value = pointsRes.data?.points || 0
  consecutiveDays.value = checkinRes.data?.consecutiveDays || 0
  todayChecked.value = !!checkinRes.data?.checkedInToday
}

async function doCheckIn() {
  try {
    await checkInAPI.checkIn()
    todayChecked.value = true
    consecutiveDays.value += 1
    points.value += 10
    ElMessage.success('签到成功，+10 积分')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '签到失败')
  }
}

function editProfile() {
  editForm.username = username.value
  showEditModal.value = true
}

async function saveProfile() {
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await authAPI.updateUser(editForm)
    username.value = editForm.username
    localStorage.setItem('username', editForm.username)
    showEditModal.value = false
    ElMessage.success('资料已更新')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  }
}

async function savePassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await authAPI.changePassword(passwordForm)
    showPasswordModal.value = false
    ElMessage.success('密码已修改，请重新登录')
    setTimeout(() => { localStorage.clear(); router.push('/login') }, 1200)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  }
}

function handleLogout() {
  localStorage.clear()
  router.push('/login')
  window.location.reload()
}

onMounted(loadAll)
</script>

<style scoped>
.layout { display: flex; gap: 20px; align-items: flex-start; }
.side { width: 300px; flex: none; }

.usercard {
  border: 1px solid var(--color-gold-line); background:
    radial-gradient(120% 90% at 0% 0%, rgba(211,174,106,.12), transparent 60%), var(--color-surface);
  padding: 30px 22px 22px; text-align: center;
}
.avatar {
  width: 62px; height: 62px; border-radius: 50%; border: 1px solid var(--color-gold-line);
  display: flex; align-items: center; justify-content: center; margin: 0 auto;
  font-family: var(--font-display); font-size: 24px; color: var(--color-gold);
}
.name { margin-top: 14px; font-family: var(--font-display); font-size: 18px; letter-spacing: .1em; }
.account { margin-top: 5px; font-size: 10.5px; color: var(--color-ink-faint); letter-spacing: .06em; }
.badge { margin-top: 12px; display: inline-block; font-size: 9.5px; letter-spacing: .14em; color: var(--color-gold); border: 1px solid var(--color-gold-line); padding: 3px 9px; }
.edit {
  width: 100%; height: 36px; margin-top: 18px; border: 1px solid var(--color-gold-line);
  background: transparent; color: var(--color-gold); font-family: inherit; font-size: 11px;
  letter-spacing: .12em; cursor: pointer;
}
.edit.ghost { margin-top: 8px; border-color: var(--color-line-strong); color: var(--color-ink-muted); }
.edit:hover { background: var(--color-gold-soft); }

.checkin-card { border: 1px solid var(--color-line); background: var(--color-surface); padding: 18px; margin-top: 16px; }
.ck-top { display: flex; align-items: baseline; justify-content: space-between; }
.ck-top span { font-size: 10px; letter-spacing: .14em; color: var(--color-ink-faint); }
.ck-top b { font-family: var(--font-display); font-size: 24px; color: var(--color-gold); font-weight: 400; }
.ck-top b i { font-style: normal; font-size: 11px; color: var(--color-ink-muted); margin-left: 3px; }
.ck-note { margin-top: 6px; font-size: 9.5px; color: var(--color-ink-faint); letter-spacing: .05em; }
.ck-btn {
  width: 100%; height: 34px; margin-top: 14px; border: 1px solid var(--color-gold);
  background: var(--color-gold); color: #14150f; font-family: inherit; font-size: 11px; letter-spacing: .12em; cursor: pointer;
}
.ck-btn:disabled { background: transparent; color: var(--color-ink-faint); border-color: var(--color-line-strong); cursor: default; }

.main { flex: 1; min-width: 0; }
.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat { border: 1px solid var(--color-line); padding: 16px; display: flex; flex-direction: column; gap: 7px; }
.stat:hover { border-color: var(--color-gold-line); }
.stat b { font-family: var(--font-display); font-size: 24px; font-weight: 400; color: var(--color-gold); }
.stat span { font-size: 10px; letter-spacing: .1em; color: var(--color-ink-muted); }

.links { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
.link {
  display: flex; align-items: center; justify-content: space-between; padding: 12px 14px;
  border: 1px solid var(--color-line); font-size: 11.5px; letter-spacing: .06em; color: var(--color-ink-soft);
}
.link em { font-style: normal; color: var(--color-ink-faint); }
.link:hover { border-color: var(--color-gold-line); color: var(--color-gold); }

.addrs { display: flex; flex-direction: column; gap: 10px; }
.addr { border: 1px solid var(--color-line); padding: 12px 14px; }
.who { font-size: 12px; color: var(--color-ink); display: flex; align-items: center; gap: 10px; }
.who span { font-size: 10.5px; color: var(--color-ink-muted); }
.where { margin-top: 6px; font-size: 10.5px; color: var(--color-ink-muted); }

/* 弹窗 */
.nv-dialog :deep(.el-dialog) { background: var(--color-surface); border: 1px solid var(--color-line); }
.nv-dialog :deep(.el-dialog__title) { font-family: var(--font-display); letter-spacing: .12em; color: var(--color-ink); }
.nv-dialog :deep(.el-form-item__label) { font-size: 10.5px; letter-spacing: .12em; color: var(--color-ink-muted); }
.nv-dialog :deep(.el-input__wrapper) { background: var(--color-bg); box-shadow: 0 0 0 1px var(--color-line) inset; border-radius: 0; }
.dlg-btn {
  height: 34px; padding: 0 18px; border: 1px solid var(--color-line-strong); background: transparent;
  color: var(--color-ink-soft); font-family: inherit; font-size: 11px; letter-spacing: .1em; cursor: pointer; margin-left: 8px;
}
.dlg-btn.gold { background: var(--color-gold); border-color: var(--color-gold); color: #14150f; }

@media (max-width: 1024px) {
  .layout { flex-direction: column; }
  .side, .main { width: 100%; }
  .stats, .links { grid-template-columns: repeat(2, 1fr); }
}
</style>