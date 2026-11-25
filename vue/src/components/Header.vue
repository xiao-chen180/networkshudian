<template>
  <el-header class="header">
    <router-link to="/" class="logo">
      <span class="logo-text">书香阁</span>
      <span class="logo-sub">Bookstore</span>
    </router-link>
    <el-menu :default-active="activeMenu" mode="horizontal" class="menu" router>
      <el-menu-item index="/">
        <span class="menu-icon">🏠</span>
        <span>首页</span>
      </el-menu-item>
      <el-menu-item index="/books">
        <span class="menu-icon">📚</span>
        <span>典籍</span>
      </el-menu-item>
      <el-menu-item index="/cart" v-if="isLoggedIn">
        <span class="menu-icon">📖</span>
        <span>书篮</span>
        <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
      </el-menu-item>
      <el-menu-item index="/orders" v-if="isLoggedIn">
        <span class="menu-icon">📋</span>
        <span>订单</span>
      </el-menu-item>
    </el-menu>
    <div class="auth">
      <router-link to="/login" v-if="!isLoggedIn" class="auth-btn-link">
        <el-button class="auth-btn">登录</el-button>
      </router-link>
      <router-link to="/register" v-if="!isLoggedIn" class="auth-btn-link">
        <el-button class="auth-btn primary">注册</el-button>
      </router-link>
      <div v-if="isLoggedIn" class="user-info">
        <div class="user-avatar-wrapper">
          <div class="user-avatar">
            <span class="avatar-text">{{ username.charAt(0) }}</span>
          </div>
          <div class="user-dropdown">
            <span class="dropdown-username">{{ username }}</span>
            <span class="dropdown-discount">会员折扣: {{ discount }}折</span>
          </div>
        </div>
        <el-button @click="logout" class="logout-btn">退出</el-button>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartCount } from '../utils/cartCount'
import { cartAPI } from '../api'

const router = useRouter()
const route = useRoute()

const isLoggedIn = ref(!!localStorage.getItem('token'))
const username = ref(localStorage.getItem('username') || '')
const discount = ref(localStorage.getItem('discount') ? (parseFloat(localStorage.getItem('discount')) * 100).toFixed(0) : '95')
const { cartCount, setCartCount } = useCartCount()

const activeMenu = computed(() => route.path)

const fetchCartCount = async () => {
  if (!isLoggedIn.value) return
  try {
    const response = await cartAPI.getCart()
    const items = response.data || []
    setCartCount(items.length)
  } catch (error) {
    console.error('获取购物车数量失败:', error)
  }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userId')
  localStorage.removeItem('discount')
  isLoggedIn.value = false
  username.value = ''
  setCartCount(0)
  router.push('/')
}

onMounted(() => {
  fetchCartCount()
})


</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 30px;
  background: linear-gradient(135deg, #4a3728 0%, #2c1810 100%);
  color: #f5f0e6;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  display: flex;
  flex-direction: column;
  text-decoration: none;
}

.logo-text {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 28px;
  font-weight: bold;
  color: #f5f0e6;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.logo-sub {
  font-family: 'STSong', 'SimSun', serif;
  font-size: 10px;
  color: rgba(212, 175, 55, 0.7);
  letter-spacing: 3px;
  text-align: center;
}

.menu {
  flex: 1;
  justify-content: center;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  color: #f5f0e6;
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 16px;
  margin: 0 15px;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(212, 175, 55, 0.2);
  color: #d4af37;
}

.menu :deep(.el-menu-item.is-active) {
  color: #d4af37;
  border-bottom: 2px solid #d4af37;
}

.menu-icon {
  margin-right: 8px;
}

.auth {
  display: flex;
  align-items: center;
  gap: 15px;
}

.auth-btn-link {
  text-decoration: none;
}

.auth-btn {
  padding: 8px 20px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 25px;
  color: #f5f0e6;
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 14px;
  transition: all 0.3s ease;
}

.auth-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
}

.auth-btn.primary {
  background: linear-gradient(135deg, #c21500, #d4af37);
  border-color: transparent;
}

.auth-btn.primary:hover {
  background: linear-gradient(135deg, #d43a00, #e8c047);
  box-shadow: 0 4px 15px rgba(194, 21, 0, 0.4);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #d4af37, #b8860b);
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 10px rgba(212, 175, 55, 0.4);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.avatar-text {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 18px;
  font-weight: bold;
  color: #2c1810;
}

.user-dropdown {
  display: flex;
  flex-direction: column;
}

.dropdown-username {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 16px;
  color: #f5f0e6;
}

.dropdown-discount {
  font-family: 'STSong', 'SimSun', serif;
  font-size: 12px;
  color: #d4af37;
}

.logout-btn {
  padding: 8px 16px;
  background: rgba(194, 21, 0, 0.2);
  border: 1px solid rgba(194, 21, 0, 0.5);
  border-radius: 25px;
  color: #f5f0e6;
  font-family: 'STSong', 'SimSun', serif;
  font-size: 14px;
}

.logout-btn:hover {
  background: rgba(194, 21, 0, 0.3);
}

.cart-badge {
  position: absolute;
  top: -5px;
  right: -10px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: linear-gradient(135deg, #c21500, #ff6b6b);
  border-radius: 10px;
  font-size: 12px;
  color: white;
  font-weight: bold;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 8px rgba(194, 21, 0, 0.5);
}

.menu :deep(.el-menu-item) {
  position: relative;
}
</style>