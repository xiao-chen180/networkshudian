import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 引入全局样式
import './styles/variables.css'
import './styles/animations.css'

// 引入动画库
import 'animate.css'

// 引入自定义指令
import { lazy } from './directives/lazy'

const app = createApp(App)
app.use(router)
app.use(ElementPlus)

// 注册全局指令
app.directive('lazy', lazy)

app.mount('#app')
