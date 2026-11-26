<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="nf">
        <div class="nf-k">ERROR 404 · PAGE NOT FOUND</div>
        <div class="nf-num">404</div>
        <h1>这一页翻丢了</h1>
        <p>你访问的页面不存在，或者书已经被下架了。不妨回首页看看今晚推荐。</p>
        <div class="nf-acts">
          <router-link to="/" class="btn-gold sm">回到首页</router-link>
          <router-link to="/books" class="btn-line sm">去逛书店</router-link>
          <button class="btn-line sm" @click="goBack">返回上一页</button>
        </div>
        <div class="nf-sug" v-if="suggestions.length">
          <span class="sug-k">也许你在找</span>
          <router-link v-for="b in suggestions" :key="b.id" :to="`/books/${b.id}`" class="sug">
            <BookCover :src="b.imageUrl" :title="b.name" compact ratio="3:4" class="sug-cover" />
            <span>{{ b.name }}</span>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { bookAPI } from '../api'
import BookCover from '../components/common/BookCover.vue'

const router = useRouter()
const suggestions = ref([])

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/')
}

onMounted(async () => {
  try {
    const res = await bookAPI.getAll({ page: 0, size: 4 })
    const data = res.data || []
    suggestions.value = (Array.isArray(data) ? data : data.content || []).slice(0, 4)
  } catch {}
})
</script>

<style scoped>
.nf { padding: 60px 0 80px; text-align: center; }
.nf-k { font-size: 8.5px; letter-spacing: .3em; color: var(--color-gold); }
.nf-num {
  margin-top: 8px; font-family: var(--font-display); font-size: 132px; line-height: 1;
  font-weight: 400; letter-spacing: .06em; color: var(--color-surface-3);
}
.nf h1 { margin-top: 6px; font-family: var(--font-display); font-size: 30px; font-weight: 400; letter-spacing: .14em; }
.nf p { margin: 14px auto 0; max-width: 420px; font-size: 11.5px; line-height: 1.9; color: var(--color-ink-muted); letter-spacing: .04em; }
.nf-acts { margin-top: 28px; display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; }

.nf-sug { margin-top: 46px; padding-top: 26px; border-top: 1px solid var(--color-line); }
.sug-k { display: block; font-size: 9px; letter-spacing: .22em; color: var(--color-ink-faint); margin-bottom: 16px; }
.sug { display: inline-flex; flex-direction: column; align-items: center; gap: 8px; width: 96px; margin: 0 10px; vertical-align: top; }
.sug-cover { width: 64px; height: 88px; }
.sug span { font-size: 10.5px; color: var(--color-ink-muted); letter-spacing: .04em; line-height: 1.5; }
.sug:hover span { color: var(--color-gold); }

@media (max-width: 767px) { .nf-num { font-size: 92px; } }
</style>