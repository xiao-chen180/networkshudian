<template>
  <div class="login-page">
    <div class="card">
      <!-- 左：品牌与引言 -->
      <aside class="aside">
        <div class="brand"><b>书香阁</b><span>EST. 2026</span></div>
        <div class="quote">
          <p>「一本书的意义，<br>往往在合上之后的<br>第三个夜晚才出现。」</p>
          <cite>—— 本期编辑手记</cite>
        </div>
        <div class="aside-foot">登录后可同步你的收藏、笔记与订单</div>
      </aside>

      <!-- 右：表单 -->
      <div class="form-side">
        <h1>欢迎回来</h1>
        <p class="sub">登录书香阁，继续你的阅读</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="form">
          <el-form-item label="账号" prop="account">
            <el-input v-model="form.account" placeholder="手机号 / 邮箱 / 账号" size="large" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              :type="showPassword ? 'text' : 'password'"
              v-model="form.password"
              placeholder="请输入密码"
              size="large"
              @keyup.enter="login"
            >
              <template #suffix>
                <span class="eye" @click="showPassword = !showPassword">{{ showPassword ? '隐藏' : '显示' }}</span>
              </template>
            </el-input>
          </el-form-item>

          <button type="button" class="submit" :disabled="loading" @click="login">
            {{ loading ? '登录中…' : '登录' }}
          </button>
        </el-form>

        <div class="links">
          <router-link to="/register">还没有账号？立即注册</router-link>
          <router-link to="/admin-login" class="admin">管理员入口 →</router-link>
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

const form = reactive({ account: '', password: '' })

const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function login() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await authAPI.login(form)
    const { accessToken, account, username, userId, discount, role } = res.data
    const userRole = role ? role.toLowerCase() : 'user'

    if (userRole === 'admin') {
      ElMessage.error('管理员请使用管理员后台登录')
      return
    }

    localStorage.setItem('token', accessToken)
    localStorage.setItem('account', account)
    localStorage.setItem('username', username)
    localStorage.setItem('userId', userId)
    localStorage.setItem('discount', discount)
    localStorage.setItem('role', userRole)

    ElMessage.success('登录成功，欢迎回来')
    setTimeout(() => { router.push('/'); window.location.reload() }, 600)
  } catch (error) {
    const msg = error.response?.data?.message
    ElMessage.error(msg || (error.message?.includes('Network') ? '网络连接失败' : '账号或密码不正确'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 102px); display: flex; align-items: center; justify-content: center;
  padding: 60px 24px; position: relative;
}
.login-page::before {
  content: ""; position: absolute; left: 50%; top: 10%; width: 900px; height: 600px;
  transform: translateX(-50%);
  background: radial-gradient(ellipse, rgba(211,174,106,.07), transparent 62%);
  pointer-events: none;
}

.card {
  display: flex; width: 900px; border: 1px solid var(--color-line); background: var(--color-surface);
  position: relative; z-index: 1;
}

/* 左侧 */
.aside {
  width: 380px; flex: none; padding: 44px 40px; display: flex; flex-direction: column;
  border-right: 1px solid var(--color-line);
  background:
    radial-gradient(120% 80% at 0% 0%, rgba(211,174,106,.10), transparent 60%),
    var(--color-bg);
}
.brand { display: flex; align-items: baseline; gap: 12px; }
.brand b { font-family: var(--font-display); font-size: 22px; letter-spacing: .3em; font-weight: 500; }
.brand span { font-size: 8px; letter-spacing: .36em; color: var(--color-ink-faint); }
.quote { margin-top: auto; }
.quote p { font-family: var(--font-display); font-size: 16px; line-height: 2.1; color: var(--color-ink-soft); letter-spacing: .06em; }
.quote cite { display: block; margin-top: 16px; font-style: normal; font-size: 10px; letter-spacing: .14em; color: var(--color-ink-faint); }
.aside-foot { margin-top: 40px; padding-top: 16px; border-top: 1px solid var(--color-line); font-size: 10.5px; letter-spacing: .06em; color: var(--color-ink-faint); }

/* 右侧 */
.form-side { flex: 1; padding: 56px 56px 44px; }
h1 { font-family: var(--font-display); font-size: 30px; font-weight: 400; letter-spacing: .14em; }
.sub { margin-top: 10px; font-size: 11.5px; color: var(--color-ink-muted); letter-spacing: .08em; }

.form { margin-top: 32px; }
.form :deep(.el-form-item__label) {
  font-size: 10.5px; letter-spacing: .16em; color: var(--color-ink-muted); padding-bottom: 4px;
}
.form :deep(.el-input__wrapper) {
  background: var(--color-bg); box-shadow: 0 0 0 1px var(--color-line) inset; border-radius: 0;
}
.form :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px var(--color-gold) inset; }
.form :deep(.el-input__inner) { color: var(--color-ink); letter-spacing: .04em; }
.eye { font-size: 10px; color: var(--color-ink-faint); cursor: pointer; letter-spacing: .1em; }

.submit {
  width: 100%; height: 44px; margin-top: 10px; border: 1px solid var(--color-gold);
  background: var(--color-gold); color: #14150f; font-family: inherit; font-size: 13px;
  letter-spacing: .22em; cursor: pointer; transition: opacity var(--dur-fast);
}
.submit:hover:not(:disabled) { opacity: .88; }
.submit:disabled { opacity: .45; cursor: not-allowed; }

.links { display: flex; justify-content: space-between; margin-top: 22px; font-size: 10.5px; letter-spacing: .06em; }
.links a { color: var(--color-ink-muted); }
.links a:hover { color: var(--color-gold); }
.links .admin { color: var(--color-ink-faint); }

@media (max-width: 900px) {
  .card { flex-direction: column; width: 100%; max-width: 460px; }
  .aside { width: 100%; border-right: 0; border-bottom: 1px solid var(--color-line); padding: 28px; }
  .quote, .aside-foot { display: none; }
  .form-side { padding: 32px 28px; }
}
</style>