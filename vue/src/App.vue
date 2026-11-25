<template>
  <div id="app" class="app-container">
    <Navbar v-if="showNavbar" />
    <main class="main-content" :class="{ 'no-navbar': !showNavbar }">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 回到顶部按钮 -->
    <transition name="fade">
      <button v-show="showBackTop" class="back-top" @click="scrollToTop">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 15l-6-6-6 6"/>
        </svg>
      </button>
    </transition>

    <!-- 全局加载指示器 -->
    <transition name="fade">
      <div v-if="isLoading" class="loading-overlay">
        <div class="loading-spinner"></div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from './components/Navbar.vue'

const route = useRoute()
const showBackTop = ref(false)
const isLoading = ref(false)

const noNavbarRoutes = ['AdminLogin', 'Admin']
const showNavbar = computed(() => !noNavbarRoutes.includes(route.name))

const handleScroll = () => {
  showBackTop.value = window.scrollY > 300
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 监听路由切换显示加载状态
watch(() => route.path, () => {
  isLoading.value = true
  setTimeout(() => { isLoading.value = false }, 300)
})

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  scroll-behavior: smooth;
}

html, body {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
}

#app {
  min-height: 100vh;
  width: 100%;
}

body {
  font-family: var(--font-sans);
  background: var(--bg-primary);
  color: var(--text-primary);
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  background: linear-gradient(180deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
}

.main-content.no-navbar {
  background: transparent;
  flex: 1;
}

/* 页面过渡动画 - 向上滑入 */
.page-enter-active {
  animation: pageSlideIn 0.35s var(--ease-out);
}

.page-leave-active {
  animation: pageSlideOut 0.25s var(--ease-in);
}

@keyframes pageSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pageSlideOut {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-10px);
  }
}

/* 淡入淡出过渡 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 回到顶部按钮 */
.back-top {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: white;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(139, 90, 43, 0.3);
  transition: all 0.3s ease;
  z-index: 999;
}

.back-top:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(139, 90, 43, 0.4);
}

/* 全局加载指示器 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  z-index: 9999;
}

.loading-spinner {
  height: 100%;
  background: linear-gradient(90deg, var(--primary), var(--secondary), var(--primary));
  background-size: 200% 100%;
  animation: loadingSlide 1s ease-in-out infinite;
}

@keyframes loadingSlide {
  0% { width: 0%; margin-left: 0; }
  50% { width: 60%; margin-left: 20%; }
  100% { width: 0%; margin-left: 100%; }
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: var(--secondary);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--primary);
}

/* 全局选中文本样式 */
::selection {
  background: var(--primary);
  color: white;
}

/* 全局按钮点击效果 */
.btn-ripple {
  position: relative;
  overflow: hidden;
}

.btn-ripple::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.4s, height 0.4s;
}

.btn-ripple:active::after {
  width: 200px;
  height: 200px;
}

/* 卡片悬浮效果 */
.hover-lift {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.hover-lift:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

/* 数字滚动动画 */
@keyframes countUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.count-animate {
  animation: countUp 0.4s ease-out;
}

/* 涟漪效果 */
.ripple {
  position: relative;
  overflow: hidden;
}

.ripple::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  padding-top: 100%;
  border-radius: 50%;
  background: rgba(139, 90, 43, 0.15);
  transform: translate(-50%, -50%) scale(0);
  opacity: 0;
  transition: transform 0.5s, opacity 0.5s;
}

.ripple:active::after {
  transform: translate(-50%, -50%) scale(4);
  opacity: 1;
  transition: 0s;
}
</style>
