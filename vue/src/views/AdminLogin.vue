<template>
  <div class="admin-login-page">
    <!-- 左侧装饰区域 -->
    <div class="left-panel">
      <!-- 浮动粒子 -->
      <div class="particles-bg">
        <div v-for="i in 8" :key="i" class="particle" :style="particleStyle(i)"></div>
      </div>
      <div class="brand-section">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <span>📚</span>
          </div>
          <h1 class="brand-name">书香阁</h1>
          <p class="brand-subtitle">BOOKSTORE MANAGEMENT</p>
        </div>
        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">📊</div>
            <div class="feature-text">
              <h3>数据概览</h3>
              <p>实时监控销售数据</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">📦</div>
            <div class="feature-text">
              <h3>库存管理</h3>
              <p>智能库存预警系统</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">👥</div>
            <div class="feature-text">
              <h3>用户管理</h3>
              <p>全面的用户权限控制</p>
            </div>
          </div>
        </div>
      </div>
      <div class="copyright">© 2024 书香阁管理系统</div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="right-panel">
      <div class="login-container">
        <div class="login-header">
          <h2>管理员登录</h2>
          <p>请使用管理员账号登录系统</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
          <el-form-item prop="account">
            <div class="input-wrapper">
              <span class="input-icon">👤</span>
              <el-input v-model="form.account" placeholder="请输入管理员账号" size="large"></el-input>
            </div>
          </el-form-item>
          <el-form-item prop="password">
            <div class="input-wrapper">
              <span class="input-icon">🔒</span>
              <el-input :type="showPassword ? 'text' : 'password'" v-model="form.password" placeholder="请输入密码" size="large">
                <template #suffix>
                  <span class="eye-btn" @click="showPassword = !showPassword">
                    <svg v-if="showPassword" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                    <svg v-else viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                  </span>
                </template>
              </el-input>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="login" class="login-btn" :loading="loading" size="large">
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <router-link to="/login" class="back-link">
            ← 返回用户登录
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const showPassword = ref(false)

const form = reactive({
  account: '',
  password: ''
})

const rules = {
  account: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const login = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return

  loading.value = true

  try {
    const response = await authAPI.login(form)
    const { accessToken, account, username, userId, discount, role } = response.data

    const userRole = role ? role.toLowerCase() : 'user'

    if (userRole !== 'admin') {
      ElMessage.error('该账号不是管理员账号')
      return
    }

    localStorage.setItem('token', accessToken)
    localStorage.setItem('account', account)
    localStorage.setItem('username', username)
    localStorage.setItem('userId', userId)
    localStorage.setItem('discount', discount)
    localStorage.setItem('role', userRole)

    ElMessage.success('登录成功，欢迎回来！')
    setTimeout(() => {
      router.push('/admin')
    }, 1000)
  } catch (error) {
    const response = error.response?.data
    if (response?.message) {
      ElMessage.error(response.message)
    } else if (error.message?.includes('Network Error')) {
      ElMessage.error('网络连接失败，请检查网络后重试')
    } else {
      ElMessage.error('登录失败，请检查账号和密码')
    }
  } finally {
    loading.value = false
  }
}

const particleStyle = (i) => {
  const size = 3 + Math.random() * 6
  const left = Math.random() * 100
  const duration = 10 + Math.random() * 15
  const delay = Math.random() * 8
  const opacity = 0.1 + Math.random() * 0.15
  return {
    width: size + 'px',
    height: size + 'px',
    left: left + '%',
    animationDuration: duration + 's',
    animationDelay: delay + 's',
    opacity: opacity
  }
}
</script>

<style scoped>
.admin-login-page {
  display: flex;
  min-height: 100vh;
  background: #f5f0e8;
}

/* 左侧装饰面板 */
.left-panel {
  flex: 0 0 38.2%;
  background: linear-gradient(135deg, #4a3728 0%, #6b4423 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
  position: relative;
  overflow: hidden;
}

.left-panel::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: pulse 8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

/* 粒子背景 */
.particles-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.particles-bg .particle {
  position: absolute;
  bottom: -10px;
  background: rgba(212, 165, 116, 0.4);
  border-radius: 50%;
  animation: particleRise linear infinite;
}

@keyframes particleRise {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 0;
  }
  10% { opacity: 0.2; }
  90% { opacity: 0.2; }
  100% {
    transform: translateY(-100vh) rotate(720deg);
    opacity: 0;
  }
}

.brand-section {
  text-align: center;
  z-index: 1;
}

.logo-wrapper {
  margin-bottom: 50px;
}

.logo-icon {
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  margin: 0 auto 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.brand-name {
  font-size: 42px;
  color: white;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: 4px;
}

.brand-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 6px;
  text-transform: uppercase;
}

.features {
  margin-top: 40px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  margin-bottom: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
  animation: featureSlideIn 0.6s ease forwards;
}

.feature-item:nth-child(1) { animation-delay: 0.3s; }
.feature-item:nth-child(2) { animation-delay: 0.45s; }
.feature-item:nth-child(3) { animation-delay: 0.6s; }

@keyframes featureSlideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.18);
  transform: translateX(10px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 28px;
}

.feature-text h3 {
  color: white;
  font-size: 16px;
  margin: 0 0 4px;
}

.feature-text p {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  margin: 0;
}

.copyright {
  position: absolute;
  bottom: 30px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
}

/* 右侧登录面板 */
.right-panel {
  flex: 0 0 61.8%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: white;
}

.login-container {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h2 {
  font-size: 28px;
  color: #4a3728;
  font-weight: 700;
  margin: 0 0 12px;
}

.login-header p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.login-form {
  width: 100%;
}

.input-wrapper {
  display: flex;
  align-items: center;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 0 12px;
  transition: all 0.3s;
  width: 100%;
}

.input-wrapper:hover {
  border-color: #8b5a2b;
}

.input-wrapper:focus-within {
  border-color: #8b5a2b;
  box-shadow: 0 0 0 2px rgba(139, 90, 43, 0.1);
}

.input-icon {
  font-size: 18px;
  margin-right: 8px;
}

.input-wrapper :deep(.el-input__wrapper) {
  box-shadow: none !important;
  border: none;
  padding: 0;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #8b5a2b 0%, #a0522d 100%);
  border: none;
  letter-spacing: 8px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.login-btn::after {
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

.login-btn:hover {
  background: linear-gradient(135deg, #a0522d 0%, #c21500 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(139, 90, 43, 0.4);
}

.login-btn:hover::after {
  left: 120%;
}

.login-footer {
  text-align: center;
  margin-top: 30px;
}

.back-link {
  color: #8b7355;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.back-link:hover {
  color: #8b5a2b;
}

.eye-btn {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #999;
  transition: color 0.2s;
}

.eye-btn:hover {
  color: #8b5a2b;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  color: #4a3728;
}
</style>
