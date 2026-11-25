<template>
  <div class="login-page">
    <div class="card">
      <div class="form-side">
        <h1>加入书香阁</h1>
        <p class="sub">注册即享会员 95 折，连续签到还能抽好书</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="form">
          <el-form-item label="账号" prop="account">
            <el-input v-model="form.account" placeholder="用于登录，建议手机号或邮箱前缀" size="large" />
          </el-form-item>
          <el-form-item label="昵称" prop="username">
            <el-input v-model="form.username" placeholder="显示在书评与笔记上" size="large" />
          </el-form-item>
          <div class="grid2">
            <el-form-item label="密码" prop="password">
              <el-input :type="showPassword ? 'text' : 'password'" v-model="form.password" placeholder="至少 6 位" size="large">
                <template #suffix><span class="eye" @click="showPassword = !showPassword">{{ showPassword ? '隐藏' : '显示' }}</span></template>
              </el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input :type="showConfirm ? 'text' : 'password'" v-model="form.confirmPassword" placeholder="再输入一次" size="large">
                <template #suffix><span class="eye" @click="showConfirm = !showConfirm">{{ showConfirm ? '隐藏' : '显示' }}</span></template>
              </el-input>
            </el-form-item>
          </div>

          <button type="button" class="submit" :disabled="loading" @click="register">
            {{ loading ? '注册中…' : '注册' }}
          </button>
        </el-form>

        <div class="links">
          <router-link to="/login">已有账号？去登录</router-link>
          <router-link to="/admin-login" class="admin">管理员入口 →</router-link>
        </div>
      </div>

      <aside class="aside">
        <div class="brand"><b>书香阁</b><span>EST. 2026</span></div>
        <div class="perks">
          <div><b>95 折</b><span>注册即享会员折扣</span></div>
          <div><b>+10 积分</b><span>每日签到即可获得</span></div>
          <div><b>满 99 免邮</b><span>全部图书正版直发</span></div>
        </div>
        <div class="aside-foot">注册后可同步收藏、笔记与订单</div>
      </aside>
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
const showConfirm = ref(false)

const form = reactive({ account: '', username: '', password: '', confirmPassword: '' })

const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  username: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => (value !== form.password ? callback(new Error('两次输入不一致')) : callback()),
      trigger: 'blur'
    }
  ]
}

async function register() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authAPI.register({
      account: form.account,
      username: form.username,
      email: '',
      phone: '',
      password: form.password,
      adminSecret: ''
    })
    ElMessage.success('注册成功，去登录吧')
    setTimeout(() => router.push('/login'), 900)
  } catch (error) {
    const res = error.response?.data
    const msg = res?.message
      || (res?.errors ? Object.values(res.errors).join('；') : '')
      || '注册失败，请稍后再试'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 102px); display: flex; align-items: center; justify-content: center;
  padding: 50px 24px; position: relative;
}
.login-page::before {
  content: ""; position: absolute; left: 50%; top: 6%; width: 900px; height: 600px;
  transform: translateX(-50%);
  background: radial-gradient(ellipse, rgba(211,174,106,.07), transparent 62%);
  pointer-events: none;
}
.card { display: flex; width: 900px; border: 1px solid var(--color-line); background: var(--color-surface); position: relative; z-index: 1; }
.form-side { flex: 1; padding: 48px 52px 40px; }
h1 { font-family: var(--font-display); font-size: 30px; font-weight: 400; letter-spacing: .14em; }
.sub { margin-top: 10px; font-size: 11.5px; color: var(--color-ink-muted); letter-spacing: .08em; }

.form { margin-top: 26px; }
.form :deep(.el-form-item__label) { font-size: 10.5px; letter-spacing: .16em; color: var(--color-ink-muted); padding-bottom: 4px; }
.form :deep(.el-input__wrapper) { background: var(--color-bg); box-shadow: 0 0 0 1px var(--color-line) inset; border-radius: 0; }
.form :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px var(--color-gold) inset; }
.form :deep(.el-input__inner) { color: var(--color-ink); letter-spacing: .04em; }
.grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 0 14px; }
.eye { font-size: 10px; color: var(--color-ink-faint); cursor: pointer; letter-spacing: .1em; }

.submit {
  width: 100%; height: 44px; margin-top: 8px; border: 1px solid var(--color-gold);
  background: var(--color-gold); color: #14150f; font-family: inherit; font-size: 13px;
  letter-spacing: .22em; cursor: pointer;
}
.submit:disabled { opacity: .45; cursor: not-allowed; }
.links { display: flex; justify-content: space-between; margin-top: 20px; font-size: 10.5px; letter-spacing: .06em; }
.links a { color: var(--color-ink-muted); }
.links a:hover { color: var(--color-gold); }
.links .admin { color: var(--color-ink-faint); }

.aside {
  width: 320px; flex: none; padding: 44px 36px; border-left: 1px solid var(--color-line);
  background: radial-gradient(120% 80% at 100% 0%, rgba(211,174,106,.10), transparent 60%), var(--color-bg);
  display: flex; flex-direction: column;
}
.brand { display: flex; align-items: baseline; gap: 12px; }
.brand b { font-family: var(--font-display); font-size: 22px; letter-spacing: .3em; font-weight: 500; }
.brand span { font-size: 8px; letter-spacing: .36em; color: var(--color-ink-faint); }
.perks { margin-top: auto; display: flex; flex-direction: column; gap: 18px; }
.perks b { display: block; font-family: var(--font-display); font-size: 20px; font-weight: 400; color: var(--color-gold); letter-spacing: .06em; }
.perks span { display: block; margin-top: 5px; font-size: 10.5px; color: var(--color-ink-muted); letter-spacing: .04em; }
.aside-foot { margin-top: 32px; padding-top: 16px; border-top: 1px solid var(--color-line); font-size: 10.5px; color: var(--color-ink-faint); }

@media (max-width: 900px) {
  .card { flex-direction: column; width: 100%; max-width: 460px; }
  .aside { width: 100%; border-left: 0; border-top: 1px solid var(--color-line); padding: 28px; }
  .perks { margin-top: 20px; }
  .aside-foot { display: none; }
  .form-side { padding: 32px 28px; }
  .grid2 { grid-template-columns: 1fr; }
}
</style>