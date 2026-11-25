<template>
  <div class="books-page">
    <!-- 顶部搜索栏 -->
    <div style="background:#fff;padding:20px 40px;border-bottom:1px solid #e8e0d5;">
      <div style="max-width:1400px;margin:0 auto;display:flex;justify-content:center;">
        <div style="display:flex;gap:12px;width:600px;">
          <input
            v-model="keyword"
            type="text"
            placeholder="输入书名、作者或ISBN进行搜索..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button
            @click="handleSearch"
            class="search-btn"
          >搜索</button>
        </div>
      </div>
    </div>

    <div class="content-wrapper">
      <!-- 左侧分类导航 -->
      <aside class="category-sidebar">
        <div class="sidebar-title">
          <span class="title-icon">📚</span>
          <span>图书分类</span>
        </div>
        <nav class="category-nav">
          <div
            class="category-item"
            :class="{ active: selectedCategoryId === null }"
            @click="selectCategory(null)"
          >
            <span class="item-icon">📋</span>
            <span class="item-name">全部图书</span>
            <span class="item-count">{{ totalCount }}</span>
          </div>
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="category-item"
            :class="{ active: selectedCategoryId === cat.id }"
            @click="selectCategory(cat.id)"
          >
            <span class="item-icon">{{ getCategoryIcon(cat.id) }}</span>
            <span class="item-name">{{ cat.name }}</span>
            <span class="item-count">{{ getCategoryCount(cat.id) }}</span>
          </div>
        </nav>
      </aside>

      <!-- 右侧书籍列表 -->
      <main class="books-main">
        <!-- 书籍网格 -->
        <div v-if="books.length > 0" class="books-grid">
          <div
            v-for="(book, index) in books"
            :key="book.id"
            class="book-card scroll-animate"
            :style="{ animationDelay: (index * 0.08) + 's' }"
            @click="goDetail(book.id)"
          >
            <div class="book-cover-wrap">
              <img :src="getCover(book)" :alt="book.name" class="book-cover" />
              <div class="cover-overlay">
                <span class="view-detail">查看详情</span>
              </div>
              <div v-if="book.stock <= 10" class="stock-badge low">库存紧张</div>
            </div>
            <div class="book-info">
              <h3 class="book-name">{{ book.name }}</h3>
              <p class="book-author">{{ book.author }}</p>
              <div class="book-meta">
                <span class="book-category">{{ book.categoryName || '未分类' }}</span>
              </div>
              <div class="book-bottom">
                <div class="book-price">
                  <span class="price-symbol">¥</span>
                  <span class="price-value">{{ formatPrice(book.price) }}</span>
                </div>
                <span class="stock-info">库存 {{ book.stock }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">📭</div>
          <h3>暂无相关图书</h3>
          <p>换个关键词试试吧</p>
        </div>

        <!-- 分页 -->
        <div v-if="total > pageSize" class="pagination-wrap">
          <button
            class="page-btn"
            :disabled="currentPage <= 1"
            @click="goPage(currentPage - 1)"
          >
            ← 上一页
          </button>
          <div class="page-numbers">
            <button
              v-for="p in visiblePages"
              :key="p"
              class="page-num"
              :class="{ active: p === currentPage }"
              @click="goPage(p)"
            >
              {{ p }}
            </button>
          </div>
          <button
            class="page-btn"
            :disabled="currentPage >= totalPages"
            @click="goPage(currentPage + 1)"
          >
            下一页 →
          </button>
          <div class="page-jump">
            <span>跳至</span>
            <input
              v-model.number="jumpPageNum"
              type="number"
              :min="1"
              :max="totalPages"
              class="page-jump-input"
              @keyup.enter="jumpToPage"
            />
            <span>页</span>
            <button class="page-btn page-jump-btn" @click="jumpToPage">确定</button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { bookAPI, categoryAPI } from '../api'

const router = useRouter()
const route = useRoute()

const books = ref([])
const categories = ref([])
const selectedCategoryId = ref(null)
const keyword = ref('')
const currentPage = ref(1)
const jumpPageNum = ref(null)
const pageSize = 12
const total = ref(0)
const totalCount = ref(0)
const categoryCounts = ref({})

const totalPages = computed(() => Math.ceil(total.value / pageSize))

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

const getCategoryIcon = (id) => {
  const icons = { 1: '📖', 2: '💻', 3: '📜', 4: '💼', 5: '🎓', 6: '🧘' }
  return icons[id] || '📚'
}

const getCategoryCount = (catId) => categoryCounts.value[catId] || 0

const loadCategories = async () => {
  try {
    const response = await categoryAPI.getAll()
    categories.value = response.data || []
    // Load counts for each category
    for (const cat of categories.value) {
      const res = await bookAPI.getAll({ categoryId: cat.id, size: 1 })
      categoryCounts.value[cat.id] = res.data.totalElements || 0
    }
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const loadBooks = async () => {
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize
    }
    if (selectedCategoryId.value !== null) {
      params.categoryId = selectedCategoryId.value
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    const response = await bookAPI.getAll(params)
    books.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    console.error('Failed to load books:', error)
    books.value = []
    total.value = 0
  }
}

const loadTotalCount = async () => {
  try {
    const response = await bookAPI.getAll({ size: 1 })
    totalCount.value = response.data.totalElements || 0
  } catch (error) {
    console.error('Failed to load total count:', error)
  }
}

const selectCategory = (catId) => {
  selectedCategoryId.value = catId
  currentPage.value = 1
  loadBooks()
}

const handleSearch = () => {
  currentPage.value = 1
  loadBooks()
}

const goPage = (page) => {
  currentPage.value = page
  loadBooks()
}

const jumpToPage = () => {
  const page = parseInt(jumpPageNum.value)
  if (isNaN(page) || page < 1 || page > totalPages.value) return
  currentPage.value = page
  jumpPageNum.value = null
  loadBooks()
}

const goDetail = (id) => router.push(`/books/${id}`)

const getCover = (book) => `https://picsum.photos/seed/${book.isbn}/300/400.jpg`

const formatPrice = (price) => {
  if (typeof price === 'string') return price
  return Number(price).toFixed(2)
}

let observer = null

const initScrollAnimations = () => {
  nextTick(() => {
    observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible')
          observer.unobserve(entry.target)
        }
      })
    }, { threshold: 0.1, rootMargin: '100px 0px' })

    document.querySelectorAll('.books-grid .scroll-animate').forEach(el => {
      observer.observe(el)
    })

    // Fallback: make all elements visible after a short delay
    setTimeout(() => {
      document.querySelectorAll('.books-grid .scroll-animate').forEach(el => {
        el.classList.add('visible')
      })
    }, 1000)
  })
}

onMounted(() => {
  const catId = route.query.category
  if (catId) {
    selectedCategoryId.value = Number(catId)
  }
  loadCategories()
  loadTotalCount()
  loadBooks().then(() => initScrollAnimations())
})

onUnmounted(() => {
  if (observer) observer.disconnect()
})
</script>

<style scoped>
.books-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #faf7f2 0%, #f5f0e8 100%);
}

/* 搜索栏 */
.search-header {
  background: white;
  padding: 24px 40px;
  border-bottom: 1px solid #e8e0d5;
}

.search-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
}

.search-box {
  display: flex;
  gap: 12px;
  width: 600px;
}

.search-input {
  flex: 1;
  height: 48px;
  padding: 0 20px;
  border: 2px solid #d4c4a8;
  border-radius: 10px;
  font-size: 15px;
  color: #333;
  outline: none;
  transition: all 0.3s ease;
}

.search-input:focus {
  border-color: #8b5a2b;
  box-shadow: 0 0 0 3px rgba(139, 90, 43, 0.1);
}

.search-input::placeholder {
  color: #aaa;
}

.search-btn {
  height: 48px;
  padding: 0 32px;
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.search-btn::after {
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

.search-btn:hover {
  background: linear-gradient(135deg, #a0522d, #c21500);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(139, 90, 43, 0.3);
}

.search-btn:hover::after {
  left: 120%;
}

/* 内容区域 */
.content-wrapper {
  display: flex;
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 40px;
  gap: 30px;
}

/* 左侧分类 */
.category-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(74, 55, 40, 0.08);
  overflow: hidden;
  height: fit-content;
  position: sticky;
  top: 110px;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 22px 28px;
  background: linear-gradient(135deg, #f5f0e8, #ebe3d5);
  font-size: 18px;
  font-weight: 700;
  color: #4a3728;
  border-bottom: 1px solid #e8e0d5;
}

.title-icon {
  font-size: 20px;
}

.category-nav {
  padding: 12px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 4px;
}

.category-item:hover {
  background: #f5f0e8;
  transform: translateX(4px);
}

.category-item.active {
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  color: white;
  box-shadow: 0 4px 16px rgba(139, 90, 43, 0.35);
  transform: translateX(4px);
}

.item-icon {
  font-size: 20px;
  width: 28px;
  text-align: center;
}

.item-name {
  flex: 1;
  font-size: 16px;
  font-weight: 500;
}

.item-count {
  font-size: 13px;
  padding: 3px 10px;
  background: rgba(139, 90, 43, 0.1);
  border-radius: 10px;
  color: #8b5a2b;
}

.category-item.active .item-count {
  background: rgba(255, 255, 255, 0.25);
  color: white;
}

/* 右侧书籍 */
.books-main {
  flex: 1;
  min-width: 0;
}

/* 书籍网格 */
.books-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.book-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 1;
}

.book-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 40px rgba(74, 55, 40, 0.2);
}

.book-cover-wrap {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: #f5f0e8;
}

.book-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.book-card:hover .book-cover {
  transform: scale(1.1);
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(74, 55, 40, 0.75), rgba(107, 68, 35, 0.65));
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  backdrop-filter: blur(2px);
}

.book-card:hover .cover-overlay {
  opacity: 1;
}

.view-detail {
  padding: 10px 24px;
  background: white;
  color: #4a3728;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 600;
  transform: translateY(10px);
  transition: transform 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.book-card:hover .view-detail {
  transform: translateY(0);
}

.stock-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
}

.stock-badge.low {
  background: #fee2e2;
  color: #dc2626;
}

.book-info {
  padding: 16px;
}

.book-name {
  font-size: 15px;
  font-weight: 600;
  color: #2c1810;
  margin-bottom: 6px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-author {
  font-size: 13px;
  color: #8b7355;
  margin-bottom: 8px;
}

.book-meta {
  margin-bottom: 12px;
}

.book-category {
  font-size: 11px;
  padding: 3px 8px;
  background: #f5f0e8;
  color: #8b5a2b;
  border-radius: 4px;
}

.book-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.book-price {
  color: #c21500;
  font-weight: 700;
}

.price-symbol {
  font-size: 13px;
}

.price-value {
  font-size: 20px;
}

.stock-info {
  font-size: 12px;
  color: #aaa;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 20px;
  color: #4a3728;
  margin-bottom: 8px;
}

.empty-state p {
  color: #8b7355;
  font-size: 14px;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 40px;
  padding: 20px;
}

.page-btn {
  padding: 10px 20px;
  background: white;
  border: 1px solid #d4c4a8;
  border-radius: 8px;
  color: #4a3728;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-btn:hover:not(:disabled) {
  background: #8b5a2b;
  color: white;
  border-color: #8b5a2b;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 90, 43, 0.3);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 6px;
}

.page-num {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 1px solid #d4c4a8;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-num:hover {
  border-color: #8b5a2b;
  color: #8b5a2b;
}

.page-num.active {
  background: #8b5a2b;
  color: white;
  border-color: #8b5a2b;
}

.page-jump {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 10px;
  font-size: 14px;
  color: #6b5744;
}

.page-jump-input {
  width: 50px;
  height: 36px;
  text-align: center;
  border: 1px solid #d4c4a8;
  border-radius: 6px;
  font-size: 14px;
  color: #4a3728;
  outline: none;
}

.page-jump-input:focus {
  border-color: #8b5a2b;
}

.page-jump-btn {
  padding: 7px 14px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .books-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .content-wrapper {
    flex-direction: column;
    padding: 20px;
  }

  .category-sidebar {
    width: 100%;
    position: static;
  }

  .category-nav {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 12px;
  }

  .category-item {
    padding: 10px 16px;
    margin-bottom: 0;
  }

  .books-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .books-grid {
    grid-template-columns: 1fr;
  }
}
</style>
