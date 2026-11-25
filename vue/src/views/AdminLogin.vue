<template>
  <div class="admin-login">
    <div class="panel">
      <div class="badge">ADMIN CONSOLE</div>
      <h1>管理者登录</h1>
      <p class="sub">书香阁运营后台 · 仅限管理员账号</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="form">
        <el-form-item label="管理员账号" prop="account">
          <el-input v-model="form.account" size="large" placeholder="请输入管理员账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input :type="showPassword ? 'text' : 'password'" v-model="form.password" size="large" placeholder="请输入密码" @keyup.enter="login">
            <template #suffix>
              <span class="eye" @click="showPassword = !showPassword">{{ showPassword ? '隐藏' : '显示' }}</span>
            </template>
          </el-input>
        </el-form-item>

        <button type="button" class="submit" :disabled="loading" @click="login">
          {{ loading ? '登录中…' : '进入后台' }}
        </button>
      </el-form>

      <div class="links">
        <router-link to="/">← 返回商城</router-link>
        <router-link to="/login">普通用户登录</router-link>
      </div>
    </div>

    <div class="side">
      <div class="brand">书香阁<span>EST. 2026</span></div>
      <p class="quote">「运营不是把书卖出去，<br>而是把对的书交到对的人手里。」</p>
      <div class="tips">
        <span>· 后台仅管理员可访问</span>
        <span>· 订单发货后用户会收到通知</span>
        <span>· 调价与库存变更实时生效</span>
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
  account: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
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

    ElMessage.success('登录成功')
    setTimeout(() => router.push('/admin'), 500)
  } catch (error) {
    const msg = error.response?.data?.message
    ElMessage.error(msg || (error.message?.includes('Network') ? '网络连接失败' : '账号或密码不正确'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh; display: flex; align-items: center; justify-content: center; gap: 80px;
  padding: 40px; position: relative;
  background:
    radial-gradient(90% 70% at 20% 0%, rgba(211,174,106,.10), transparent 60%),
    var(--color-bg);
}
.panel {
  width: 420px; border: 1px solid var(--color-line); background: var(--color-surface);
  padding: 40px 40px 30px;
}
.badge { font-size: 8.5px; letter-spacing: .34em; color: var(--color-gold); }
h1 { margin-top: 14px; font-family: var(--font-display); font-size: 27px; font-weight: 400; letter-spacing: .14em; }
.sub { margin-top: 9px; font-size: 10.5px; color: var(--color-ink-muted); letter-spacing: .08em; }

.form { margin-top: 28px; }
.form :deep(.el-form-item__label) { font-size: 10.5px; letter-spacing: .16em; color: var(--color-ink-muted); padding-bottom: 4px; }
.form :deep(.el-input__wrapper) { background: var(--color-bg); box-shadow: 0 0 0 1px var(--color-line) inset; border-radius: 0; }
.form :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px var(--color-gold) inset; }
.form :deep(.el-input__inner) { color: var(--color-ink); }
.eye { font-size: 10px; color: var(--color-ink-faint); cursor: pointer; letter-spacing: .1em; }

.submit {
  width: 100%; height: 44px; margin-top: 6px; border: 1px solid var(--color-gold);
  background: var(--color-gold); color: #14150f; font-family: inherit; font-size: 12.5px;
  letter-spacing: .22em; cursor: pointer;
}
.submit:disabled { opacity: .45; cursor: not-allowed; }

.links { display: flex; justify-content: space-between; margin-top: 20px; font-size: 10px; letter-spacing: .06em; }
.links a { color: var(--color-ink-faint); }
.links a:hover { color: var(--color-gold); }

.side { width: 340px; }
.brand { font-family: var(--font-display); font-size: 24px; letter-spacing: .3em; }
.brand span { display: block; margin-top: 8px; font-size: 8px; letter-spacing: .4em; color: var(--color-ink-faint); }
.quote { margin-top: 30px; font-family: var(--font-display); font-size: 15px; line-height: 2.1; color: var(--color-ink-soft); letter-spacing: .06em; }
.tips { margin-top: 36px; display: flex; flex-direction: column; gap: 9px; font-size: 10.5px; color: var(--color-ink-faint); letter-spacing: .04em; }

@media (max-width: 900px) { .side { display: none; } .panel { width: 100%; max-width: 440px; } }
</style>