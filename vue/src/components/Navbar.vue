<template>
  <div class="navbar-wrapper">
    <header class="navbar" :class="{ scrolled: isScrolled }">
      <div class="navbar-container">
        <!-- 品牌Logo -->
        <router-link to="/" class="brand">
          <div class="brand-logo">
            <div class="logo-icon">
              <span class="logo-emoji">📚</span>
            </div>
            <div class="brand-text">
              <span class="brand-name">书香阁</span>
              <span class="brand-en">BOOKSTORE</span>
            </div>
          </div>
        </router-link>

        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <router-link to="/" class="nav-link" :class="{ active: currentRoute === '/' }">
            <span class="link-icon">🏠</span>
            <span class="link-text">首页</span>
          </router-link>
          <router-link to="/books" class="nav-link" :class="{ active: currentRoute === '/books' }">
            <span class="link-icon">📖</span>
            <span class="link-text">典籍</span>
          </router-link>
          <router-link to="/cart" class="nav-link" :class="{ active: currentRoute === '/cart' }" v-if="isLoggedIn">
            <span class="link-icon">🛒</span>
            <span class="link-text">书篮</span>
            <span v-if="cartCount > 0" class="badge">{{ cartCount }}</span>
          </router-link>
          <router-link to="/rankings" class="nav-link" :class="{ active: currentRoute === '/rankings' }">
            <span class="link-icon">🏆</span>
            <span class="link-text">排行</span>
          </router-link>
          <router-link to="/notes" class="nav-link" :class="{ active: currentRoute === '/notes' }" v-if="isLoggedIn">
            <span class="link-icon">📝</span>
            <span class="link-text">笔记</span>
          </router-link>
          <!-- 管理员后台入口 -->
          <router-link to="/admin" class="nav-link admin-link" :class="{ active: currentRoute.startsWith('/admin') }" v-if="isAdmin">
            <span class="link-icon">⚙️</span>
            <span class="link-text">管理</span>
          </router-link>
        </nav>

        <!-- 用户区域 -->
        <div class="user-area">
          <!-- 深色模式切换 -->
          <DarkModeToggle />

          <template v-if="isLoggedIn">
            <div class="user-trigger" @mouseenter="showDropdown = true" @mouseleave="showDropdown = false">
              <div class="avatar-wrapper">
                <div class="avatar">{{ (account || username).charAt(0) }}</div>
                <div class="avatar-badge">👑</div>
              </div>
              <div class="user-brief">
                <span class="brief-name">{{ username }}</span>
                <span class="brief-level">{{ discount }}折会员</span>
              </div>
              <svg class="dropdown-arrow" :class="{ open: showDropdown }" viewBox="0 0 24 24" width="16" height="16">
                <path fill="currentColor" d="M7 10l5 5 5-5z"/>
              </svg>
            </div>

            <transition name="dropdown">
              <div v-if="showDropdown" class="dropdown-menu" @mouseenter="showDropdown = true" @mouseleave="showDropdown = false">
                <div class="dropdown-user">
                  <div class="dropdown-avatar">{{ (account || username).charAt(0) }}</div>
                  <div class="dropdown-info">
                    <span class="dropdown-name">{{ username }}</span>
                    <span class="dropdown-level">
                      <span class="level-icon">👑</span>
                      {{ discount }}折会员
                    </span>
                  </div>
                </div>

                <div class="dropdown-links">
                  <router-link to="/profile" class="dropdown-link" @click="showDropdown = false">
                    <span class="link-icon">👤</span>
                    <span>个人中心</span>
                  </router-link>
                  <router-link to="/orders" class="dropdown-link" @click="showDropdown = false">
                    <span class="link-icon">📋</span>
                    <span>我的订单</span>
                  </router-link>
                  <router-link to="/messages" class="dropdown-link" @click="showDropdown = false">
                    <span class="link-icon">🔔</span>
                    <span>消息中心</span>
                    <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
                  </router-link>
                  <router-link v-if="isAdmin" to="/admin" class="dropdown-link admin-dropdown-link" @click="showDropdown = false">
                    <span class="link-icon">⚙️</span>
                    <span>管理员后台</span>
                  </router-link>
                </div>

                <div class="dropdown-footer">
                  <button class="logout-btn" @click="logout">
                    <span class="link-icon">🚪</span>
                    <span>退出登录</span>
                  </button>
                </div>
              </div>
            </transition>
          </template>

          <template v-else>
            <router-link to="/login" class="auth-btn login">登录</router-link>
            <router-link to="/register" class="auth-btn register">注册</router-link>
          </template>
        </div>
      </div>
    </header>
    <div class="navbar-spacer"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { cartAPI, messageAPI } from '../api'
import { useCartCount } from '../utils/cartCount'
import DarkModeToggle from './DarkModeToggle.vue'

const router = useRouter()
const route = useRoute()

const showDropdown = ref(false)
const isScrolled = ref(false)
const { cartCount, setCartCount } = useCartCount()
const unreadCount = ref(0)

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

const account = computed(() => localStorage.getItem('account') || '')
const username = computed(() => localStorage.getItem('username') || '')
const discount = computed(() => {
  const d = localStorage.getItem('discount')
  return d ? (parseFloat(d) * 100).toFixed(0) : '95'
})
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const isAdmin = computed(() => localStorage.getItem('role') === 'admin')
const currentRoute = computed(() => route.path)

const loadCartCount = async () => {
  if (!isLoggedIn.value) {
    setCartCount(0)
    return
  }
  try {
    const response = await cartAPI.getCart()
    const items = response.data || []
    setCartCount(items.length)
  } catch {
    setCartCount(0)
  }
}

const loadUnreadCount = async () => {
  if (!isLoggedIn.value) {
    unreadCount.value = 0
    return
  }
  try {
    const res = await messageAPI.getUnreadCount()
    unreadCount.value = res.data.count || 0
  } catch {
    unreadCount.value = 0
  }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('account')
  localStorage.removeItem('username')
  localStorage.removeItem('userId')
  localStorage.removeItem('discount')
  localStorage.removeItem('role')
  showDropdown.value = false
  setCartCount(0)
  router.push('/login')
  window.location.reload()
}

onMounted(() => {
  loadCartCount()
  loadUnreadCount()
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

watch(route, () => {
  loadCartCount()
  loadUnreadCount()
  showDropdown.value = false
})
watch(isLoggedIn, loadCartCount)
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 90px;
  background: linear-gradient(135deg, rgba(74, 55, 40, 0.92) 0%, rgba(107, 68, 35, 0.92) 50%, rgba(74, 55, 40, 0.92) 100%);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 4px 20px rgba(74, 55, 40, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.navbar.scrolled {
  height: 64px;
  background: linear-gradient(135deg, rgba(74, 55, 40, 0.96) 0%, rgba(107, 68, 35, 0.96) 50%, rgba(74, 55, 40, 0.96) 100%);
  box-shadow: 0 2px 30px rgba(74, 55, 40, 0.4);
}

.navbar.scrolled .logo-icon {
  width: 36px;
  height: 36px;
}

.navbar.scrolled .logo-emoji {
  font-size: 20px;
}

.navbar.scrolled .brand-name {
  font-size: 18px;
}

.navbar.scrolled .brand-en {
  font-size: 9px;
}

.navbar.scrolled .navbar-spacer {
  height: 64px;
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 品牌Logo */
.brand {
  text-decoration: none;
  flex-shrink: 0;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #d4a574, #c19a6b);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(139, 90, 43, 0.4);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.brand:hover .logo-icon {
  transform: rotate(-8deg) scale(1.08);
  box-shadow: 0 6px 25px rgba(212, 165, 116, 0.6);
  background: linear-gradient(135deg, #e8c9a0, #d4a574);
}

.logo-emoji {
  font-size: 24px;
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-name {
  font-size: 22px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 2px;
  line-height: 1.2;
}

.brand-en {
  font-size: 10px;
  color: rgba(212, 165, 116, 0.9);
  letter-spacing: 3px;
  font-weight: 500;
}

/* 导航菜单 */
.nav-menu {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  border-radius: 10px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.nav-link::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 3px;
  background: linear-gradient(90deg, #d4a574, #ffd700);
  border-radius: 2px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateX(-50%);
}

.nav-link:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

.nav-link:hover .link-icon {
  transform: scale(1.15);
  transition: transform 0.3s var(--ease-bounce);
}

.nav-link.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.15);
}

.nav-link.active::before {
  width: 24px;
}

.link-icon {
  font-size: 16px;
}

.badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  background: linear-gradient(135deg, #c21500, #e63900);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 4px;
  animation: badgeBounce 0.5s var(--ease-bounce);
}

@keyframes badgeBounce {
  0% { transform: scale(0); }
  60% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

/* 管理员后台链接 */
.admin-link {
  background: rgba(255, 193, 7, 0.15) !important;
  border: 1px solid rgba(255, 193, 7, 0.3);
}

.admin-link:hover {
  background: rgba(255, 193, 7, 0.25) !important;
}

.admin-link.active {
  background: rgba(255, 193, 7, 0.3) !important;
}

/* 用户区域 */
.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
}

.user-trigger:hover {
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 2px 15px rgba(212, 165, 116, 0.2);
}

.user-trigger:hover .avatar {
  box-shadow: 0 0 0 3px rgba(212, 165, 116, 0.4);
}

.avatar-wrapper {
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.avatar {
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, #d4a574, #c19a6b);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: #4a3728;
}

.avatar-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  font-size: 14px;
  background: #4a3728;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-brief {
  display: flex;
  flex-direction: column;
}

.brief-name {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  line-height: 1.2;
}

.brief-level {
  font-size: 11px;
  color: #d4a574;
  font-weight: 500;
}

.dropdown-arrow {
  color: rgba(255, 255, 255, 0.6);
  transition: transform 0.3s ease;
}

.dropdown-arrow.open {
  transform: rotate(180deg);
}

/* 下拉菜单 */
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 10px;
  width: 260px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(74, 55, 40, 0.2);
  overflow: hidden;
  z-index: 1001;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

.dropdown-user {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f0e8, #ebe3d5);
}

.dropdown-avatar {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #d4a574, #c19a6b);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  color: #4a3728;
  box-shadow: 0 4px 15px rgba(139, 90, 43, 0.3);
}

.dropdown-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dropdown-name {
  font-size: 16px;
  font-weight: 700;
  color: #4a3728;
}

.dropdown-level {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #8b5a2b;
  font-weight: 500;
}

.level-icon {
  font-size: 12px;
  color: #8b5a2b;
}

.dropdown-links {
  padding: 8px 0;
}

.dropdown-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  color: #4a3728;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.dropdown-link:hover {
  background: #f5f0e8;
  color: #6b4423;
  padding-left: 24px;
}

.admin-dropdown-link {
  border-top: 1px solid #e0d6c8;
  color: #b8860b;
}

.admin-dropdown-link:hover {
  background: linear-gradient(135deg, rgba(184, 134, 11, 0.08), rgba(184, 134, 11, 0.15));
  color: #8b6914;
}

.dropdown-link .link-icon {
  width: 24px;
  text-align: center;
  font-size: 16px;
  color: #8b5a2b;
}

.unread-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #ee0a24;
  color: white;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: auto;
}

.dropdown-footer {
  padding: 8px 0;
  border-top: 1px solid #e0d6c8;
}

.logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  background: none;
  border: none;
  color: #6b5344;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.logout-btn:hover {
  background: #f5f0e8;
  color: #c21500;
}

.logout-btn .link-icon {
  width: 24px;
  text-align: center;
  font-size: 16px;
}

/* 认证按钮 */
.auth-btn {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 8px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.auth-btn.login {
  color: #fff;
  background: transparent;
}

.auth-btn.login:hover {
  background: rgba(255, 255, 255, 0.12);
}

.auth-btn.register {
  color: #fff;
  background: linear-gradient(135deg, #6b4423, #8b5a2b);
  position: relative;
  overflow: hidden;
}

.auth-btn.register::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -60%;
  width: 40%;
  height: 200%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
  transform: rotate(25deg);
  transition: left 0.5s ease;
}

.auth-btn.register:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(139, 90, 43, 0.5);
}

.auth-btn.register:hover::after {
  left: 120%;
}

.navbar-spacer {
  height: 90px;
  transition: height 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
</style>
