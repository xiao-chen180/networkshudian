import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { ElMessage } from 'element-plus'

/* 全局样式 */
import './styles/tokens.css'
import './styles/theme.css'
import './styles/variables.css'
import './styles/typography.css'
import './styles/animations.css'
import './styles/pages.css'

/* 自定义指令 */
import { lazy } from './directives/lazy'

const app = createApp(App)
app.use(router)
app.directive('lazy', lazy)
/* 全局错误兜底：组件渲染异常与未捕获的 Promise 异常统一提示，避免白屏无反馈 */
app.config.errorHandler = (err, instance, info) => {
  console.error('[app error]', info, err)
  ElMessage?.error?.('页面出现异常，请刷新重试')
}
window.addEventListener('unhandledrejection', (event) => {
  const msg = event?.reason?.message || ''
  if (/Network Error|timeout/i.test(msg)) {
    ElMessage?.warning?.('网络不太稳定，请稍后重试')
    event.preventDefault()
  }
})

app.mount('#app')