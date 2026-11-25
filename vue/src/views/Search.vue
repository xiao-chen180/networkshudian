<template>
  <div class="search-page">
    <div class="search-header">
      <div class="search-input-wrapper">
        <input v-model="keyword" type="text" placeholder="搜索书名、作者" @keyup.enter="doSearch" autofocus />
        <button v-if="keyword" class="clear-btn" @click="keyword=''">×</button>
      </div>
      <button class="search-btn" @click="doSearch">搜索</button>
    </div>
    <div class="search-body" v-if="hasSearched">
      <div v-if="results.length > 0" class="results">
        <div v-for="book in results" :key="book.id" class="result-card" @click="router.push(`/books/${book.id}`)">
          <img :src="getCover(book)" class="result-cover" />
          <div class="result-info">
            <h3>{{ book.name }}</h3>
            <p>{{ book.author }}</p>
            <span class="result-price">¥{{ formatPrice(book.price) }}</span>
          </div>
        </div>
      </div>
      <div v-else class="empty"><p>未找到相关图书</p></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { bookAPI } from '../api'

const router = useRouter()
const keyword = ref('')
const results = ref([])
const hasSearched = ref(false)

const doSearch = async () => {
  if (!keyword.value.trim()) return
  hasSearched.value = true
  try {
    const res = await bookAPI.getAll({ keyword: keyword.value.trim(), size: 50 })
    results.value = res.data.content || []
  } catch (e) {}
}

const getCover = (b) => `https://picsum.photos/seed/${b.isbn||b.id}/200/280.jpg`
const formatPrice = (p) => typeof p === 'string' ? p : p.toFixed(2)
</script>

<style scoped>
.search-page { min-height: 100vh; background: var(--bg-page); }
.search-header { display: flex; gap: 10px; padding: 12px 16px; background: white; border-bottom: 1px solid #eee; position: sticky; top: 0; z-index: 10; }
.search-input-wrapper { flex: 1; display: flex; align-items: center; background: #f5f5f5; border-radius: 8px; padding: 0 12px; }
.search-input-wrapper input { flex: 1; border: none; outline: none; background: transparent; padding: 10px 0; font-size: 14px; }
.clear-btn { background: none; border: none; font-size: 18px; color: #999; cursor: pointer; }
.search-btn { background: var(--primary); color: white; border: none; padding: 0 20px; border-radius: 8px; font-size: 14px; cursor: pointer; }
.search-body { padding: 16px; max-width: 800px; margin: 0 auto; }
.results { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.result-card { display: flex; gap: 12px; background: white; border-radius: 12px; padding: 12px; cursor: pointer; }
.result-cover { width: 70px; height: 90px; object-fit: cover; border-radius: 8px; flex-shrink: 0; }
.result-info { flex: 1; }
.result-info h3 { font-size: 15px; margin: 0 0 4px; }
.result-info p { font-size: 12px; color: var(--text-secondary); margin: 0 0 8px; }
.result-price { color: var(--accent); font-weight: bold; }
.empty { text-align: center; padding: 80px 0; color: var(--text-secondary); }
</style>
