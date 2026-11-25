<template>
  <div class="register">
    <!-- 浮动粒子背景 -->
    <div class="particles-bg">
      <div v-for="i in 15" :key="i" class="particle" :style="particleStyle(i)"></div>
    </div>

    <div class="register-card animate-fade-up">
      <div class="card-header">
        <div class="seal-icon animate-scale-in">书</div>
        <h2>用户注册</h2>
        <p class="subtitle">注册即享会员专属折扣</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="stagger-form">
        <el-form-item label="账号" prop="account" class="stagger-item" style="--delay: 0.1s">
          <el-input v-model="form.account" placeholder="请输入账号，用于登录"></el-input>
        </el-form-item>
        <el-form-item label="用户名" prop="username" class="stagger-item" style="--delay: 0.15s">
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" class="stagger-item" style="--delay: 0.2s">
          <el-input :type="showPassword ? 'text' : 'password'" v-model="form.password" placeholder="至少6个字符，建议包含字母和数字">
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
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" label-width="100px" class="stagger-item" style="--delay: 0.25s">
          <el-input :type="showConfirmPassword ? 'text' : 'password'" v-model="form.confirmPassword" placeholder="请再次输入密码">
            <template #suffix>
              <span class="eye-btn" @click="showConfirmPassword = !showConfirmPassword">
                <svg v-if="showConfirmPassword" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
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
        </el-form-item>
        <el-form-item class="stagger-item" style="--delay: 0.3s">
          <el-button type="primary" @click="register" class="register-submit-btn" :loading="loading">
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
          <el-button @click="goLogin" class="login-btn">已有账号？立即登录</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 管理员后台按钮 - 页面右下角 -->
    <div class="admin-link">
      <router-link to="/admin-login" class="admin-btn">
        <span class="admin-icon">⚙️</span>
        <span>管理员后台</span>
      </router-link>
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
const showConfirmPassword = ref(false)

const form = reactive({
  account: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' },
    { max: 100, message: '密码长度不能超过100位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value !== form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ]
}

const register = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return

  loading.value = true

  try {
    const registerData = {
      account: form.account,
      username: form.username,
      email: '',
      phone: '',
      password: form.password,
      adminSecret: ''
    }

    await authAPI.register(registerData)

    ElMessage.success('注册成功！即将跳转到登录页面...')
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    const response = error.response?.data
    if (response) {
      if (response.errors && typeof response.errors === 'object') {
        const errorMessages = []
        for (const [field, msg] of Object.entries(response.errors)) {
          const fieldLabels = {
            username: '用户名',
            email: '邮箱',
            phone: '手机号',
            password: '密码'
          }
          errorMessages.push(`${fieldLabels[field] || field}：${msg}`)
        }
        ElMessage.error(errorMessages.join('；'))
      } else if (response.message) {
        ElMessage.error(response.message)
      } else {
        ElMessage.error('注册失败，请稍后重试')
      }
    } else if (error.message) {
      if (error.message.includes('Network Error')) {
        ElMessage.error('网络连接失败，请检查网络后重试')
      } else {
        ElMessage.error(`注册失败：${error.message}`)
      }
    } else {
      ElMessage.error('注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const goLogin = () => router.push('/login')

const particleStyle = (i) => {
  const size = 4 + Math.random() * 8
  const left = Math.random() * 100
  const duration = 10 + Math.random() * 15
  const delay = Math.random() * 12
  const opacity = 0.12 + Math.random() * 0.2
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
.register {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(180deg, #faf7f2 0%, #f0ebe3 100%);
  position: relative;
  overflow: hidden;
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
  background: linear-gradient(135deg, #d4a574, #8b5a2b);
  border-radius: 50%;
  animation: particleRise linear infinite;
}

@keyframes particleRise {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 0.25;
  }
  90% {
    opacity: 0.25;
  }
  100% {
    transform: translateY(-85vh) rotate(720deg);
    opacity: 0;
  }
}

.register-card {
  width: 450px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 2px solid rgba(212, 196, 168, 0.6);
  border-radius: 20px;
  box-shadow:
    0 10px 40px rgba(139, 90, 43, 0.12),
    inset 0 0 20px rgba(139, 90, 43, 0.03);
  position: relative;
  z-index: 1;
  opacity: 0;
}

/* 交错动画 */
.stagger-form .stagger-item {
  opacity: 0;
  animation: staggerFadeUp 0.5s var(--ease-out) forwards;
  animation-delay: var(--delay);
}

@keyframes staggerFadeUp {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
  position: relative;
}

.seal-icon {
  position: absolute;
  top: -25px;
  right: -25px;
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #c21500, #ffc500);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 24px;
  color: white;
  box-shadow: 0 4px 15px rgba(194, 21, 0, 0.4);
  animation: sealSpin 0.8s var(--ease-bounce);
}

@keyframes sealSpin {
  0% { transform: scale(0) rotate(-180deg); }
  100% { transform: scale(1) rotate(0deg); }
}

.card-header h2 {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 32px;
  color: #4a3728;
  margin: 35px 0 10px;
}

.subtitle {
  font-family: 'STSong', 'SimSun', serif;
  font-size: 14px;
  color: #8b7355;
}

.register-submit-btn {
  width: 100%;
  margin-bottom: 15px;
  padding: 14px;
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  border: none;
  border-radius: 10px;
  color: white;
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 18px;
  letter-spacing: 2px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.register-submit-btn::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -60%;
  width: 40%;
  height: 200%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transform: rotate(25deg);
  transition: left 0.6s ease;
}

.register-submit-btn:hover {
  background: linear-gradient(135deg, #a0522d, #c21500);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(139, 90, 43, 0.4);
}

.register-submit-btn:hover::after {
  left: 120%;
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: transparent;
  border: 1px solid #8b5a2b;
  border-radius: 10px;
  color: #8b5a2b;
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 16px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: rgba(139, 90, 43, 0.1);
  color: #8b5a2b;
  transform: translateY(-1px);
}

.admin-link {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1000;
}

.admin-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  color: white;
  border-radius: 20px;
  font-size: 13px;
  text-decoration: none;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(139, 90, 43, 0.3);
}

.admin-btn:hover {
  background: linear-gradient(135deg, #a0522d, #8b5a2b);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 90, 43, 0.4);
}

.admin-icon {
  font-size: 14px;
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

:deep(.el-form-item__label) {
  font-family: 'STKaiti', 'KaiTi', serif;
  color: #4a3728;
  font-size: 15px;
}

:deep(.el-input__wrapper) {
  border: 1px solid #d4c4a8;
  border-radius: 4px;
}

:deep(.el-input__wrapper:hover) {
  border-color: #8b5a2b;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(139, 90, 43, 0.1);
  border-color: #8b5a2b;
}

:deep(.el-form-item__error) {
  color: #c21500;
  font-family: 'STSong', 'SimSun', serif;
}
</style>
