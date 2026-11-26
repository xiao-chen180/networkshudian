<template>
  <div class="err-boundary" v-if="hasError">
    <div class="eb-inner">
      <span class="eb-k">SOMETHING WENT WRONG</span>
      <h2>这一页出错了</h2>
      <p>{{ message }}</p>
      <div class="eb-acts">
        <button class="btn-gold sm" @click="retry">重试</button>
        <router-link to="/" class="btn-line sm">回到首页</router-link>
      </div>
    </div>
  </div>
  <slot v-else />
</template>

<script setup>
import { ref, onErrorCaptured, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const hasError = ref(false)
const message = ref('页面渲染时出现了意料之外的问题，你可以重试或返回首页。')

/* 捕获子组件（页面）抛出的渲染/生命周期错误，避免整站白屏 */
onErrorCaptured((err) => {
  hasError.value = true
  message.value = err?.message ? String(err.message).slice(0, 120) : '页面渲染时出现了意料之外的问题。'
  // 返回 false 阻止错误继续向上冒泡
  return false
})

/* 路由变化时自动恢复 */
watch(() => route.fullPath, () => { hasError.value = false })

function retry() {
  hasError.value = false
  // 触发一次刷新以重新拉取数据
  window.location.reload()
}
</script>

<style scoped>
.err-boundary { min-height: 52vh; display: flex; align-items: center; justify-content: center; padding: 60px 24px; }
.eb-inner { max-width: 460px; text-align: center; }
.eb-k { font-size: 8.5px; letter-spacing: .3em; color: var(--color-danger); }
.eb-inner h2 {
  margin-top: 14px; font-family: var(--font-display); font-size: 28px; font-weight: 400; letter-spacing: .14em;
}
.eb-inner p {
  margin-top: 12px; font-size: 11.5px; line-height: 1.9; color: var(--color-ink-muted); letter-spacing: .04em;
  word-break: break-word;
}
.eb-acts { margin-top: 26px; display: flex; gap: 10px; justify-content: center; }
</style>