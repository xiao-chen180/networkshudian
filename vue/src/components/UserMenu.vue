<template>
  <div class="user-menu" ref="rootRef" @mouseenter="openOnHover" @mouseleave="closeOnLeave">
    <!-- 触发区 -->
    <button
      type="button"
      class="trigger"
      :class="{ active: open }"
      :aria-expanded="open"
      aria-haspopup="menu"
      @click="toggle"
      @keydown.esc="close"
    >
      <span class="avatar">{{ initial }}</span>
      <span class="who">
        <b>{{ username }}</b>
        <span class="level">{{ levelLabel }}</span>
      </span>
      <svg class="caret" :class="{ up: open }" viewBox="0 0 24 24" aria-hidden="true">
        <path d="m6 9 6 6 6-6" stroke="currentColor" fill="none" stroke-width="1.6" />
      </svg>
    </button>

    <!-- 下拉面板 -->
    <transition name="um">
      <div v-if="open" class="panel" role="menu" @keydown.esc="close">
        <div class="panel-head">
          <span class="avatar lg">{{ initial }}</span>
          <div class="ph-info">
            <b>{{ username }}</b>
            <span>{{ account }}</span>
          </div>
          <span class="badge">{{ levelLabel }}</span>
        </div>

        <div class="group">
          <span class="group-label">我的</span>
          <router-link v-for="item in primary" :key="item.to" :to="item.to" class="item" role="menuitem" @click="close">
            <span class="ico" v-html="item.icon"></span>
            <span class="label">{{ item.label }}</span>
            <span class="count" v-if="item.key === 'orders' && orderCount">{{ orderCount }}</span>
            <span class="count" v-else-if="item.key === 'coupons' && couponCount">{{ couponCount }}</span>
          </router-link>
        </div>

        <div class="group">
          <span class="group-label">设置</span>
          <router-link v-for="item in secondary" :key="item.to" :to="item.to" class="item" role="menuitem" @click="close">
            <span class="ico" v-html="item.icon"></span>
            <span class="label">{{ item.label }}</span>
          </router-link>
          <router-link v-if="isAdmin" to="/admin" class="item admin" role="menuitem" @click="close">
            <span class="ico" v-html="icons.admin"></span>
            <span class="label">管理后台</span>
            <span class="hint">ADMIN</span>
          </router-link>
        </div>

        <button type="button" class="logout" role="menuitem" @click="handleLogout">
          <span class="ico" v-html="icons.logout"></span>
          退出登录
        </button>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { orderAPI, couponAPI } from '../api'

const router = useRouter()
const route = useRoute()
const rootRef = ref(null)
const open = ref(false)
const orderCount = ref(0)
const couponCount = ref(0)
let closeTimer = null

const username = computed(() => localStorage.getItem('username') || '读者')
const account = computed(() => localStorage.getItem('account') || '')
const initial = computed(() => (username.value || '读').trim().charAt(0).toUpperCase())
const isAdmin = computed(() => (localStorage.getItem('role') || '').toLowerCase() === 'admin')
const levelLabel = computed(() => {
  const d = Number(localStorage.getItem('discount') || 0.95)
  if (!Number.isFinite(d) || d <= 0 || d > 1) return '会员'
  return `${(d * 10).toFixed(1).replace('.0', '')} 折会员`
})

const icons = {
  user: '<svg viewBox="0 0 24 24"><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></svg>',
  order: '<svg viewBox="0 0 24 24"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/><path d="M3 6h18M16 10a4 4 0 0 1-8 0"/></svg>',
  heart: '<svg viewBox="0 0 24 24"><path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1-1.1a5.5 5.5 0 0 0-7.8 7.8l8.8 8.8 8.8-8.8a5.5 5.5 0 0 0 0-7.8Z"/></svg>',
  ticket: '<svg viewBox="0 0 24 24"><path d="M3 9h18M6 3h12l3 6H3l3-6Z"/><path d="M5 9v10a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V9"/></svg>',
  coin: '<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><path d="M12 8v8M9.5 10.5h4M9.5 13.5h4"/></svg>',
  pin: '<svg viewBox="0 0 24 24"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg>',
  note: '<svg viewBox="0 0 24 24"><path d="M4 4h16v16H4z"/><path d="M8 9h8M8 13h6"/></svg>',
  msg: '<svg viewBox="0 0 24 24"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.7 21a2 2 0 0 1-3.4 0"/></svg>',
  admin: '<svg viewBox="0 0 24 24"><path d="M12 3 4 6v6c0 5 3.5 8 8 9 4.5-1 8-4 8-9V6l-8-3Z"/><path d="m9 12 2 2 4-4"/></svg>',
  refund: '<svg viewBox="0 0 24 24"><path d="M3 12a9 9 0 1 0 3-6.7"/><path d="M3 4v5h5"/><path d="M12 8v4l3 2"/></svg>',
  logout: '<svg viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5M21 12H9"/></svg>'
}

const primary = [
  { key: 'profile', to: '/profile', label: '个人中心', icon: icons.user },
  { key: 'orders', to: '/orders', label: '我的订单', icon: icons.order },
  { key: 'favorites', to: '/favorites', label: '我的收藏', icon: icons.heart },
  { key: 'coupons', to: '/coupons', label: '优惠券', icon: icons.ticket }
]

const secondary = [
  { to: '/points', label: '积分中心', icon: icons.coin },
  { to: '/notes', label: '读书笔记', icon: icons.note },
  { to: '/messages', label: '消息中心', icon: icons.msg },
  { to: '/after-sales', label: '售后服务', icon: icons.refund },
  { to: '/addresses', label: '收货地址', icon: icons.pin }
]

function toggle() { open.value = !open.value }
function close() { open.value = false }
function openOnHover() {
  if (closeTimer) { clearTimeout(closeTimer); closeTimer = null }
  open.value = true
}
function closeOnLeave() {
  closeTimer = setTimeout(() => { open.value = false }, 180)
}

function handleLogout() {
  close()
  localStorage.clear()
  router.push('/login')
  window.location.reload()
}

function onDocClick(e) {
  if (rootRef.value && !rootRef.value.contains(e.target)) open.value = false
}

async function loadBadges() {
  if (!localStorage.getItem('token')) return
  try {
    const res = await orderAPI.getAll()
    const list = res.data || []
    orderCount.value = list.filter(o => o.status === 'PENDING' || o.status === 'SHIPPED').length
  } catch {}
  try {
    const res = await couponAPI.getMyCoupons()
    couponCount.value = (res.data || []).filter(c => c.status === 'UNUSED').length
  } catch {}
}

onMounted(() => {
  document.addEventListener('click', onDocClick)
  loadBadges()
})
onUnmounted(() => document.removeEventListener('click', onDocClick))

watch(() => route.path, () => { open.value = false })
</script>

<style scoped>
.user-menu { position: relative; }

/* 触发器 */
.trigger {
  display: flex; align-items: center; gap: 9px; height: 36px; padding: 0 10px 0 4px;
  border: 1px solid transparent; background: transparent; cursor: pointer;
  font-family: inherit; color: var(--color-ink-soft);
  transition: border-color var(--dur-fast), background var(--dur-fast);
}
.trigger:hover, .trigger.active { border-color: var(--color-line-strong); background: var(--color-surface); }

.avatar {
  width: 28px; height: 28px; border-radius: 50%; flex: none;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid var(--color-gold-line); background: var(--color-surface-2);
  font-family: var(--font-display); font-size: 12px; color: var(--color-gold);
}
.avatar.lg { width: 38px; height: 38px; font-size: 15px; }

.who { display: flex; flex-direction: column; align-items: flex-start; gap: 1px; line-height: 1.2; }
.who b { font-size: 12px; font-weight: 400; color: var(--color-ink); letter-spacing: .04em; max-width: 96px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.who .level { font-size: 9px; color: var(--color-ink-faint); letter-spacing: .06em; }

.caret { width: 12px; height: 12px; color: var(--color-ink-faint); transition: transform var(--dur-fast); flex: none; }
.caret.up { transform: rotate(180deg); }

/* 面板 */
.panel {
  position: absolute; right: 0; top: calc(100% + 8px); width: 252px; z-index: var(--z-dropdown);
  background: var(--color-surface); border: 1px solid var(--color-line-strong);
  box-shadow: var(--shadow-popover);
}

.panel-head {
  display: flex; align-items: center; gap: 11px; padding: 14px 14px 13px;
  border-bottom: 1px solid var(--color-line);
  background: linear-gradient(120deg, rgba(211,174,106,.10), transparent 70%);
}
.ph-info { flex: 1; min-width: 0; }
.ph-info b { display: block; font-family: var(--font-display); font-size: 14px; font-weight: 400; letter-spacing: .06em; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ph-info span { display: block; margin-top: 3px; font-size: 9.5px; color: var(--color-ink-faint); letter-spacing: .05em; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.badge { font-size: 8.5px; letter-spacing: .1em; color: var(--color-gold); border: 1px solid var(--color-gold-line); padding: 2px 6px; white-space: nowrap; }

.group { padding: 8px 0 6px; }
.group + .group { border-top: 1px solid var(--color-line); }
.group-label { display: block; padding: 2px 14px 6px; font-size: 8.5px; letter-spacing: .24em; color: var(--color-ink-faint); }

.item {
  display: flex; align-items: center; gap: 10px; height: 34px; padding: 0 14px;
  font-size: 11.5px; color: var(--color-ink-soft); letter-spacing: .04em;
  transition: background var(--dur-fast), color var(--dur-fast);
}
.item:hover { background: var(--color-surface-2); color: var(--color-ink); }
.ico { width: 14px; height: 14px; flex: none; color: var(--color-ink-muted); display: flex; }
.ico :deep(svg) { width: 14px; height: 14px; stroke: currentColor; fill: none; stroke-width: 1.5; }
.item:hover .ico { color: var(--color-gold); }
.label { flex: 1; }
.count {
  min-width: 16px; height: 16px; padding: 0 4px; border-radius: 8px;
  background: var(--color-gold); color: #14150f; font-size: 9px;
  display: flex; align-items: center; justify-content: center;
}
.hint { font-size: 8px; letter-spacing: .14em; color: var(--color-gold); }
.item.admin .label { color: var(--color-gold); }

.logout {
  width: 100%; height: 38px; display: flex; align-items: center; justify-content: center; gap: 8px;
  border: 0; border-top: 1px solid var(--color-line); background: transparent;
  font-family: inherit; font-size: 11px; letter-spacing: .1em; color: var(--color-ink-faint); cursor: pointer;
}
.logout:hover { background: rgba(201,116,92,.08); color: var(--color-danger); }
.logout .ico { color: inherit; }

/* 过渡 */
.um-enter-active, .um-leave-active { transition: opacity .16s ease, transform .16s ease; }
.um-enter-from, .um-leave-to { opacity: 0; transform: translateY(-6px); }

@media (max-width: 1024px) {
  .who { display: none; }
}
</style>