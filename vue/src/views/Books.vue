<template>
  <div class="books-page">
    <div class="wrap">
      <div class="pagehead">
        <div class="crumb"><router-link to="/">首页</router-link><i>/</i><span>全部图书</span></div>
        <div class="headrow">
          <div>
            <h1>全部图书</h1>
            <div class="sub">共 {{ totalCount || total }} 种在售 · 每周三上新 · 全部为正版直发</div>
          </div>
          <div class="searchbar">
            <svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="6.5"/><path d="m19 19-3.2-3.2"/></svg>
            <input
              v-model="keyword"
              placeholder="在全部图书中搜索：书名 / 作者 / ISBN"
              @keyup.enter="handleSearch"
            />
            <button v-if="keyword" class="clear-kw" @click="clearKeyword">×</button>
          </div>
        </div>
      </div>

      <div class="layout">
        <!-- 筛选 -->
        <aside class="filters">
          <div class="fg">
            <h5>分类</h5>
            <div class="fitem" :class="{ on: selectedCategoryId === null }" @click="selectCategory(null)">
              <span class="ck"></span>全部图书<em>{{ totalCount }}</em>
            </div>
            <div
              v-for="cat in categories"
              :key="cat.id"
              class="fitem"
              :class="{ on: selectedCategoryId === cat.id }"
              @click="selectCategory(cat.id)"
            >
              <span class="ck"></span>{{ cat.name }}<em>{{ catCounts[cat.id] || 0 }}</em>
            </div>
          </div>

          <div class="fg">
            <h5>排序</h5>
            <div class="fitem" :class="{ on: sortBy === 'default' }" @click="setSort('default')"><span class="ck"></span>综合</div>
            <div class="fitem" :class="{ on: sortBy === 'priceAsc' }" @click="setSort('priceAsc')"><span class="ck"></span>价格从低到高</div>
            <div class="fitem" :class="{ on: sortBy === 'priceDesc' }" @click="setSort('priceDesc')"><span class="ck"></span>价格从高到低</div>
            <div class="fitem" :class="{ on: sortBy === 'rating' }" @click="setSort('rating')"><span class="ck"></span>评分优先</div>
            <div class="fitem" :class="{ on: sortBy === 'sales' }" @click="setSort('sales')"><span class="ck"></span>销量优先</div>
          </div>

          <div class="fg">
            <h5>库存</h5>
            <div class="fitem" :class="{ on: inStockOnly }" @click="toggleStock"><span class="ck"></span>仅看现货</div>
          </div>

          <a class="fclear" @click="resetFilters">清除全部筛选</a>
        </aside>

        <!-- 列表 -->
        <main class="main">
          <div class="toolbar">
            <span class="result">共 {{ total }} 种</span>
            <div class="chips">
              <span v-if="keyword" class="chip">{{ keyword }} <i @click="clearKeyword">×</i></span>
              <span v-if="selectedCategoryId" class="chip">{{ currentCatName }} <i @click="selectCategory(null)">×</i></span>
              <span v-if="inStockOnly" class="chip">仅看现货 <i @click="toggleStock">×</i></span>
            </div>
          </div>

          <div v-if="loading" class="grid3">
            <BookCard v-for="n in 6" :key="n" loading :book="{}" />
          </div>

          <div v-else-if="displayBooks.length" class="grid3">
            <BookCard
              v-for="book in displayBooks"
              :key="book.id"
              :book="book"
              :show-action="isLoggedIn"
              @add="addToCart"
            />
          </div>

          <EmptyState
            v-else
            icon="◌"
            title="没有找到相关图书"
            description="换个关键词，或清除筛选条件再试一次"
            action-text="清除筛选"
            @action="resetFilters"
          />

          <div v-if="totalPages > 1" class="pager">
            <a :class="{ disabled: currentPage <= 1 }" @click="currentPage > 1 && goPage(currentPage - 1)">←</a>
            <a
              v-for="p in visiblePages"
              :key="p"
              :class="{ on: p === currentPage }"
              @click="goPage(p)"
            >{{ p }}</a>
            <a :class="{ disabled: currentPage >= totalPages }" @click="currentPage < totalPages && goPage(currentPage + 1)">→</a>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { bookAPI, categoryAPI, cartAPI } from '../api'
import { ElMessage } from 'element-plus'
import BookCard from '../components/common/BookCard.vue'
import EmptyState from '../components/common/EmptyState.vue'

const route = useRoute()
const router = useRouter()

const books = ref([])
const categories = ref([])
const catCounts = ref({})
const selectedCategoryId = ref(null)
const keyword = ref('')
const currentPage = ref(1)
const pageSize = 9
const total = ref(0)
const totalCount = ref(0)
const loading = ref(true)
const sortBy = ref('default')
const inStockOnly = ref(false)

const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))
const currentCatName = computed(() => categories.value.find(c => c.id === selectedCategoryId.value)?.name || '')

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const displayBooks = computed(() => {
  let list = [...books.value]
  if (inStockOnly.value) list = list.filter(b => Number(b.stock) > 0)
  const key = sortBy.value
  if (key === 'priceAsc') list.sort((a, b) => Number(a.price) - Number(b.price))
  if (key === 'priceDesc') list.sort((a, b) => Number(b.price) - Number(a.price))
  if (key === 'rating') list.sort((a, b) => Number(b.avgRating) - Number(a.avgRating))
  if (key === 'sales') list.sort((a, b) => Number(b.salesCount) - Number(a.salesCount))
  return list
})

async function loadCategories() {
  try {
    const res = await categoryAPI.getAll()
    categories.value = res.data || []
    categories.value.forEach(async (cat) => {
      try {
        const r = await bookAPI.getAll({ categoryId: cat.id, size: 1 })
        catCounts.value[cat.id] = r.data.totalElements || 0
      } catch {}
    })
  } catch {}
}

async function loadTotal() {
  try {
    const res = await bookAPI.getAll({ size: 1 })
    totalCount.value = res.data.totalElements || 0
  } catch {}
}

async function loadBooks() {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, size: pageSize }
    if (selectedCategoryId.value !== null) params.categoryId = selectedCategoryId.value
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const res = await bookAPI.getAll(params)
    books.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch {
    books.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function selectCategory(id) {
  selectedCategoryId.value = id
  currentPage.value = 1
  router.replace({ query: id ? { category: id } : {} })
  loadBooks()
}
const handleSearch = () => { currentPage.value = 1; loadBooks() }
const clearKeyword = () => { keyword.value = ''; currentPage.value = 1; loadBooks() }
const setSort = (key) => { sortBy.value = key }
const toggleStock = () => { inStockOnly.value = !inStockOnly.value }
const goPage = (p) => { currentPage.value = p; loadBooks(); window.scrollTo({ top: 0, behavior: 'smooth' }) }

function resetFilters() {
  selectedCategoryId.value = null
  keyword.value = ''
  sortBy.value = 'default'
  inStockOnly.value = false
  currentPage.value = 1
  router.replace({ query: {} })
  loadBooks()
}

async function addToCart(book) {
  try {
    await cartAPI.addToCart({ bookId: book.id, quantity: 1 })
    ElMessage.success('已加入购物袋')
  } catch {
    ElMessage.error('加入失败，请先登录')
  }
}

onMounted(() => {
  const catId = route.query.category
  if (catId) selectedCategoryId.value = Number(catId)
  loadCategories()
  loadTotal()
  loadBooks()
})

watch(() => route.query.category, (v) => {
  const id = v ? Number(v) : null
  if (id !== selectedCategoryId.value) {
    selectedCategoryId.value = id
    currentPage.value = 1
    loadBooks()
  }
})
</script>

<style scoped>
.books-page { min-height: 100vh; padding-bottom: 40px; }
.wrap { max-width: var(--container); margin: 0 auto; padding: 0 var(--gutter); }

.pagehead { padding: 40px 0 28px; }
.crumb { font-size: 11px; color: var(--color-ink-faint); letter-spacing: .08em; display: flex; gap: 8px; }
.crumb a { color: var(--color-ink-muted); }
.crumb i { font-style: normal; opacity: .5; }

.headrow { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; margin-top: 14px; }
h1 { font-family: var(--font-display); font-size: var(--fs-h1); font-weight: 400; letter-spacing: .12em; }
.sub { margin-top: 8px; font-size: 11.5px; color: var(--color-ink-muted); letter-spacing: .08em; }

.searchbar {
  width: 320px; height: 40px; border: 1px solid var(--color-line); background: var(--color-surface);
  display: flex; align-items: center; gap: 10px; padding: 0 12px;
}
.searchbar svg { width: 14px; height: 14px; stroke: var(--color-ink-muted); fill: none; stroke-width: 1.6; flex: none; }
.searchbar input {
  flex: 1; border: 0; background: transparent; outline: 0; color: var(--color-ink);
  font-family: inherit; font-size: 12px; letter-spacing: .04em;
}
.searchbar input::placeholder { color: var(--color-ink-faint); }
.clear-kw { border: 0; background: none; color: var(--color-ink-faint); cursor: pointer; font-size: 14px; }

.layout { display: flex; gap: 40px; align-items: flex-start; }

/* 筛选 */
.filters { width: 210px; flex: none; }
.fg { border-top: 1px solid var(--color-line); padding: 20px 0; }
.fg:first-child { border-top: 0; padding-top: 0; }
.fg h5 { font-family: var(--font-display); font-size: 13px; font-weight: 400; letter-spacing: .18em; margin-bottom: 14px; }
.fitem {
  display: flex; align-items: center; gap: 9px; padding: 6px 0; font-size: 11.5px;
  color: var(--color-ink-muted); letter-spacing: .04em; cursor: pointer; transition: color var(--dur-fast);
}
.fitem:hover { color: var(--color-ink-soft); }
.fitem em { margin-left: auto; font-style: normal; font-size: 9.5px; color: var(--color-ink-faint); }
.ck { width: 12px; height: 12px; border: 1px solid var(--color-line-strong); flex: none; position: relative; }
.fitem.on { color: var(--color-ink); }
.fitem.on .ck { background: var(--color-gold); border-color: var(--color-gold); }
.fitem.on .ck::after {
  content: ""; position: absolute; left: 3.5px; top: 1px; width: 3px; height: 6px;
  border: 1px solid #14150f; border-top: 0; border-left: 0; transform: rotate(42deg);
}
.fitem.on em { color: var(--color-gold); }
.fclear { display: inline-block; margin-top: 18px; font-size: 10.5px; color: var(--color-ink-faint); border-bottom: 1px solid var(--color-line-strong); padding-bottom: 2px; cursor: pointer; }

/* 主区 */
.main { flex: 1; min-width: 0; }
.toolbar {
  display: flex; align-items: center; gap: 16px; height: 44px;
  border-top: 1px solid var(--color-line); border-bottom: 1px solid var(--color-line); margin-bottom: 22px;
}
.result { font-size: 11px; color: var(--color-ink-muted); letter-spacing: .08em; flex: none; }
.chips { display: flex; gap: 8px; flex-wrap: wrap; }
.chip {
  display: inline-flex; align-items: center; gap: 7px; font-size: 10px; letter-spacing: .08em;
  color: var(--color-ink-soft); border: 1px solid var(--color-line-strong); padding: 3px 8px;
}
.chip i { font-style: normal; color: var(--color-ink-faint); cursor: pointer; }

.grid3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }

.pager { display: flex; align-items: center; justify-content: center; gap: 6px; margin-top: 40px; }
.pager a {
  min-width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
  border: 1px solid var(--color-line); font-size: 11.5px; color: var(--color-ink-muted); cursor: pointer;
}
.pager a:hover { border-color: var(--color-line-strong); color: var(--color-ink); }
.pager a.on { background: var(--color-gold); border-color: var(--color-gold); color: #14150f; }
.pager a.disabled { opacity: .35; cursor: not-allowed; }

@media (max-width: 1024px) {
  .layout { flex-direction: column; }
  .filters { width: 100%; display: flex; gap: 28px; flex-wrap: wrap; }
  .fg { border-top: 0; padding: 0; }
  .grid3 { grid-template-columns: repeat(2, 1fr); }
}
</style>