<template>
  <div class="rankings-page">
    <div class="hero-section">
      <h1 class="page-title">🏆 排行榜</h1>
    </div>
    <div class="rankings-container">
      <div class="filter-tabs">
        <button v-for="tab in tabs" :key="tab.key" :class="['tab-btn', { active: activeTab === tab.key }]" @click="switchTab(tab.key)">{{ tab.label }}</button>
      </div>
      <div v-if="books.length > 0" class="ranking-list">
        <div v-for="(book, index) in books" :key="book.id" class="ranking-item" @click="router.push(`/books/${book.id}`)">
          <span :class="['rank', index < 3 ? 'top' : '']">{{ index + 1 }}</span>
          <img :src="getCover(book)" class="book-cover" />
          <div class="book-info">
            <h3 class="book-name">{{ book.name }}</h3>
            <p class="book-author">{{ book.author }}</p>
            <div class="book-meta">
              <span class="book-price">¥{{ formatPrice(book.price) }}</span>
              <span class="book-sales" v-if="activeTab==='sales'">{{ book.salesCount || 0 }}人付款</span>
              <span class="book-rating" v-if="activeTab==='rating'">{{ book.avgRating || 0 }}分</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty"><p>暂无数据</p></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { rankingAPI } from '../api'

const router = useRouter()
const books = ref([])
const activeTab = ref('sales')
const tabs = [{ key: 'sales', label: '销量榜' }, { key: 'rating', label: '好评榜' }, { key: 'new', label: '新品榜' }]

const switchTab = async (key) => {
  activeTab.value = key
  if (key === 'sales') { const r = await rankingAPI.getSales(); books.value = r.data }
  else if (key === 'rating') { const r = await rankingAPI.getRating(); books.value = r.data }
  else { const r = await rankingAPI.getNew(); books.value = r.data }
}

const getCover = (b) => `https://picsum.photos/seed/${b.isbn||b.id}/200/280.jpg`
const formatPrice = (p) => typeof p === 'string' ? p : Number(p).toFixed(2)

onMounted(() => switchTab('sales'))
</script>

<style scoped>
.rankings-page { min-height: 100vh; background: var(--bg-page); }
.hero-section { background: linear-gradient(135deg, var(--primary), var(--primary-light)); padding: 20px; text-align: center; }
.page-title { color: white; font-family: var(--font-serif); font-size: 22px; margin: 0; }
.rankings-container { max-width: 800px; margin: 0 auto; padding: 16px; }
.filter-tabs { display: flex; gap: 10px; margin-bottom: 16px; }
.tab-btn { flex: 1; padding: 10px; background: white; border: 1px solid #ddd; border-radius: 10px; font-size: 14px; cursor: pointer; }
.tab-btn.active { background: var(--primary); border-color: var(--primary); color: white; }
.ranking-list { display: flex; flex-direction: column; gap: 10px; }
.ranking-item { display: flex; align-items: center; gap: 12px; background: white; padding: 12px; border-radius: 12px; cursor: pointer; }
.rank { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; border-radius: 50%; background: #f0f0f0; font-size: 14px; font-weight: bold; color: var(--text-secondary); flex-shrink: 0; }
.rank.top { background: linear-gradient(135deg, #ffd700, #ffb800); color: white; }
.book-cover { width: 60px; height: 80px; object-fit: cover; border-radius: 6px; flex-shrink: 0; }
.book-info { flex: 1; }
.book-name { font-size: 15px; margin: 0 0 4px; }
.book-author { font-size: 12px; color: var(--text-secondary); margin: 0 0 6px; }
.book-meta { display: flex; gap: 12px; align-items: center; }
.book-price { color: var(--accent); font-weight: bold; font-size: 15px; }
.book-sales, .book-rating { font-size: 12px; color: var(--text-secondary); }
.empty { text-align: center; padding: 60px 0; color: var(--text-secondary); }
</style>
