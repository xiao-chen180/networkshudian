<template>
  <div id="app" class="app-shell">
    <!-- 全局顶栏 -->
    <header class="hdr" v-if="!isFullscreen">
      <div class="hdr-wrap">
        <router-link to="/" class="brand">
          <b>书香阁</b>
          <span>EST. 2026</span>
        </router-link>
        <nav class="nav">
          <router-link to="/" :class="{ on: route.path === '/' }">首页</router-link>
          <router-link to="/books" :class="{ on: route.path.startsWith('/books') }">全部图书</router-link>
          <router-link to="/rankings" :class="{ on: route.path === '/rankings' }">排行榜</router-link>
          <router-link to="/vip" :class="{ on: route.path === '/vip' }" v-if="isLoggedIn">会员</router-link>
        </nav>
        <div class="tools">
          <template v-if="isLoggedIn">
            <router-link to="/messages" class="ttl msg-link">
              消息
              <i v-if="unreadCount" class="dot-badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</i>
            </router-link>
            <span class="sep"></span>
            <router-link to="/favorites" class="ttl">收藏</router-link>
            <span class="sep"></span>
            <router-link to="/cart" class="ttl cart-bag">购物袋
              <i v-if="cartCount">{{ cartCount }}</i>
            </router-link>
            <UserMenu />
          </template>
          <template v-else>
            <router-link to="/login" class="ttl">登录</router-link>
            <span class="sep"></span>
            <router-link to="/register" class="ttl">注册</router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- 全局公告 -->
    <div class="notice" v-if="showNotice">
      <div class="notice-inner">
        <span>满 99 元免运费</span><b>·</b>
        <span>正版直发</span><b>·</b>
        <span>新人注册享 95 折</span><b>·</b>
        <span>每日签到赢好书</span>
      </div>
    </div>

    <main class="main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="foot" v-if="showFooter">
      <div class="wrap">
        <div class="fgrid">
          <div class="fbrand">
            <b>书香阁</b>
            <p>学习与实训示例项目 · Spring Boot 3 + Vue 3 前后端分离。<br>图书信息、评分与价格均为演示数据。</p>
          </div>
          <div class="fcol">
            <h5>逛书店</h5>
            <router-link to="/books">全部图书</router-link>
            <router-link to="/rankings">排行榜</router-link>
            <router-link to="/search">搜索</router-link>
          </div>
          <div class="fcol">
            <h5>我的</h5>
            <router-link to="/orders">订单中心</router-link>
            <router-link to="/favorites">收藏</router-link>
            <router-link to="/notes">读书笔记</router-link>
            <router-link to="/coupons">优惠券</router-link>
          </div>
          <div class="fcol">
            <h5>帮助</h5>
            <span>配送：48 小时内发货</span>
            <span>退换：7 天无理由</span>
            <span>支付：支付宝 / 微信（演示）</span>
          </div>
        </div>
        <div class="fbar"><span>© 2026 书香阁 BOOKSTORE</span><span>深夜书房</span></div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { cartAPI, messageAPI } from './api'
import UserMenu from './components/UserMenu.vue'

const route = useRoute()

const cartCount = ref(0)
const unreadCount = ref(0)
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const isAdmin = computed(() => localStorage.getItem('role') === 'admin')

// 不显示顶栏和页脚的路由
const fullscreenRoutes = ['AdminLogin', 'Admin']
const isFullscreen = computed(() => fullscreenRoutes.includes(route.name))
const showNotice = computed(() => !isFullscreen.value)
const showFooter = computed(() => !isFullscreen.value)

async function loadCartCount() {
  if (!isLoggedIn.value) { cartCount.value = 0; return }
  try {
    const res = await cartAPI.getCart()
    cartCount.value = (res.data || []).length
  } catch { cartCount.value = 0 }
}

async function loadUnread() {
  if (!isLoggedIn.value) { unreadCount.value = 0; return }
  try {
    const res = await messageAPI.getUnreadCount()
    unreadCount.value = res.data?.count || 0
  } catch { unreadCount.value = 0 }
}

onMounted(() => { loadCartCount(); loadUnread() })
watch(route, () => { loadCartCount(); loadUnread() })
watch(isLoggedIn, () => { loadCartCount(); loadUnread() })
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }

html, body { height: 100%; }

#app {
  min-height: 100vh;
  background: var(--color-bg);
  color: var(--color-ink);
}

.app-shell { display: flex; flex-direction: column; min-height: 100vh; }

.main { flex: 1; }

/* ===== 顶栏 ===== */
.hdr { height: 72px; border-bottom: 1px solid var(--color-line); background: var(--color-bg); position: sticky; top: 0; z-index: var(--z-nav); }
.hdr-wrap { max-width: var(--container); margin: 0 auto; height: 100%; padding: 0 var(--gutter); display: flex; align-items: center; gap: 44px; }

.brand { display: flex; align-items: baseline; gap: 12px; flex: none; color: inherit; }
.brand b { font-family: var(--font-display); font-size: 24px; font-weight: 500; letter-spacing: .30em; }
.brand span { font-size: 8px; letter-spacing: .36em; color: var(--color-ink-faint); }

.nav { display: flex; gap: 30px; flex: none; }
.nav a { font-size: 13px; color: var(--color-ink-soft); letter-spacing: .08em; padding: 4px 0; position: relative; }
.nav a.on { color: var(--color-gold); }

.tools { margin-left: auto; display: flex; align-items: center; gap: 16px; font-size: 12.5px; color: var(--color-ink-soft); letter-spacing: .06em; }
.sep { width: 1px; height: 14px; background: var(--color-line-strong); }
.ttl { color: inherit; }
.ttl:hover { color: var(--color-gold); }
.msg-link { position: relative; display: inline-flex; align-items: center; gap: 4px; }
.dot-badge {
  font-style: normal; min-width: 15px; height: 15px; padding: 0 4px; border-radius: 8px;
  background: var(--color-danger); color: #fff; font-size: 9px; line-height: 15px; text-align: center;
}
.cart-bag { display: flex; align-items: center; gap: 7px; color: var(--color-ink); }
.cart-bag i {
  display: inline-flex; width: 18px; height: 18px; border-radius: 50%; background: var(--color-gold); color: #14150f;
  font-style: normal; font-size: 10px; align-items: center; justify-content: center; font-weight: 600;
}

/* ===== 公告 ===== */
.notice {
  height: 30px; display: flex; align-items: center; justify-content: center;
  border-bottom: 1px solid var(--color-line); font-size: 11px; letter-spacing: .16em; color: var(--color-ink-muted);
}
.notice-inner { display: flex; gap: 24px; }
.notice-inner b { color: var(--color-gold); font-weight: 400; }

/* ===== 页脚 ===== */
.foot { padding: 48px 0 28px; border-top: 1px solid var(--color-line); margin-top: 64px; }
.wrap { max-width: var(--container); margin: 0 auto; padding: 0 var(--gutter); }
.fgrid { display: flex; gap: 80px; }
.fbrand { width: 320px; }
.fbrand b { font-family: var(--font-display); font-size: 18px; letter-spacing: .3em; font-weight: 500; }
.fbrand p { margin-top: 14px; font-size: 11px; line-height: 1.9; color: var(--color-ink-muted); letter-spacing: .04em; }
.fcol h5 { font-size: 10px; letter-spacing: .22em; color: var(--color-gold); margin-bottom: 14px; font-weight: 400; }
.fcol a, .fcol span { display: block; font-size: 11px; color: var(--color-ink-muted); line-height: 2.1; letter-spacing: .05em; }
.fcol a:hover { color: var(--color-gold); }
.fcol span { color: var(--color-ink-faint); }
.fbar { display: flex; justify-content: space-between; margin-top: 38px; padding-top: 18px; border-top: 1px solid var(--color-line); font-size: 10px; color: var(--color-ink-faint); letter-spacing: .1em; }
</style>