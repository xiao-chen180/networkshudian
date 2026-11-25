<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><span>排行榜</span></div>
      <div class="pg-head">
        <div>
          <h1>排行榜</h1>
          <div class="sub">每周一更新 · 数据来自真实订单与评分</div>
        </div>
      </div>

      <div class="tabs">
        <button v-for="t in tabs" :key="t.key" :class="{ on: activeTab === t.key }" @click="switchTab(t.key)">
          {{ t.label }}<em>{{ t.en }}</em>
        </button>
      </div>

      <div class="rows" v-if="books.length">
        <div class="row" v-for="(book, i) in books" :key="book.id" :class="{ top: i < 3 }" @click="go(book.id)">
          <span class="no">{{ pad(i + 1) }}</span>
          <BookCover :src="book.imageUrl" :title="book.name" compact class="rc" ratio="3:4" />
          <div class="info">
            <b>{{ book.name }}</b>
            <span>{{ book.author }} · {{ book.categoryName || '综合' }}</span>
          </div>
          <div class="meta">
            <span v-if="activeTab === 'sales'">销量 {{ book.salesCount || 0 }}</span>
            <span v-else-if="activeTab === 'rating'">评分 {{ Number(book.avgRating || 0).toFixed(1) }}</span>
            <span v-else>新书上架</span>
          </div>
          <div class="p">¥{{ fmt(book.price) }}</div>
        </div>
      </div>
      <div class="blank" v-else>暂无数据</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { rankingAPI } from '../api'
import BookCover from '../components/common/BookCover.vue'

const router = useRouter()
const books = ref([])
const activeTab = ref('sales')
const tabs = [
  { key: 'sales', label: '销量榜', en: 'SALES' },
  { key: 'rating', label: '好评榜', en: 'RATING' },
  { key: 'new', label: '新品榜', en: 'NEW' }
]

const pad = (n) => String(n).padStart(2, '0')
const fmt = (p) => (typeof p === 'string' ? p : Number(p || 0).toFixed(2))
const go = (id) => router.push(`/books/${id}`)

async function switchTab(key) {
  activeTab.value = key
  try {
    const api = key === 'sales' ? rankingAPI.getSales() : key === 'rating' ? rankingAPI.getRating() : rankingAPI.getNew()
    const res = await api
    books.value = res.data || []
  } catch {
    books.value = []
  }
}

onMounted(() => switchTab('sales'))
</script>

<style scoped>
.rows { border-top: 1px solid var(--color-line-strong); }
.row {
  display: grid; grid-template-columns: 52px 46px 1fr 120px 90px; gap: 18px;
  align-items: center; padding: 14px 0; border-bottom: 1px solid var(--color-line);
  cursor: pointer; transition: background var(--dur-fast);
}
.row:hover { background: var(--color-surface); }
.no { font-family: var(--font-display); font-size: 21px; color: var(--color-ink-faint); }
.row.top .no { color: var(--color-gold); }
.rc { width: 46px; height: 62px; }
.info b { display: block; font-family: var(--font-display); font-size: 15px; font-weight: 400; letter-spacing: .06em; }
.info span { display: block; margin-top: 5px; font-size: 10px; color: var(--color-ink-muted); letter-spacing: .05em; }
.meta { font-size: 10.5px; color: var(--color-ink-muted); letter-spacing: .04em; }
.p { text-align: right; font-size: 13.5px; color: var(--color-gold); }

@media (max-width: 900px) {
  .row { grid-template-columns: 36px 36px 1fr auto; gap: 10px; }
  .meta { display: none; }
}
</style>