import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

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
app.mount('#app')