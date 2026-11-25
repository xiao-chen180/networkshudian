<template>
  <div class="home-container">
    <!-- Hero区域 -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="particle particle-1"></div>
        <div class="particle particle-2"></div>
        <div class="particle particle-3"></div>
        <div class="particle particle-4"></div>
        <div class="particle particle-5"></div>
      </div>
      <div class="hero-content">
        <div class="brand-badge animate-fade-down">
          <span>书香</span>
        </div>
        <h1 class="main-title">
          <span class="char" v-for="(char, index) in '书香阁'" :key="index" :style="{ animationDelay: `${0.2 + index * 0.15}s` }">
            {{ char }}
          </span>
        </h1>
        <p class="sub-title animate-fade-up" style="animation-delay: 0.8s">品书香 · 悟人生</p>
        <p class="description animate-fade-up" style="animation-delay: 1s">传承千年文化，品味经典名著</p>
        <button class="explore-btn btn-ripple animate-fade-up" style="animation-delay: 1.2s" @click="goBooks">
          <span>踏入书海</span>
          <span class="arrow">→</span>
        </button>
      </div>
      <div class="scroll-indicator">
        <div class="mouse">
          <div class="wheel"></div>
        </div>
        <span>向下滚动</span>
      </div>
    </section>

    <!-- 分类区域 -->
    <section class="categories-section">
      <div class="section-header">
        <h2 class="section-title scroll-animate">
          <span class="decor">◆</span>
          <span>典籍分类</span>
          <span class="decor">◆</span>
        </h2>
        <div class="divider scroll-animate"></div>
      </div>
      <div class="categories-grid">
        <div
          v-for="(category, index) in categories"
          :key="category.id"
          class="category-card scroll-animate"
          :style="{ animationDelay: `${index * 0.1}s` }"
          @click="goCategory(category.id)"
        >
          <div class="card-inner">
            <div class="category-icon">{{ getCategoryIcon(category.id) }}</div>
            <h3 class="category-name">{{ category.name }}</h3>
            <p class="category-desc">{{ category.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 精选图书 -->
    <section class="featured-section">
      <div class="section-header">
        <h2 class="section-title scroll-animate">
          <span class="decor">◇</span>
          <span>精选典籍</span>
          <span class="decor">◇</span>
        </h2>
        <div class="divider scroll-animate"></div>
      </div>
      <div class="books-showcase">
        <div
          v-for="(book, index) in hotBooks"
          :key="book.id"
          class="book-card scroll-animate"
          :style="{ animationDelay: `${index * 0.1}s` }"
          @click="goBookDetail(book.id)"
        >
          <div class="book-cover-wrapper">
            <img :src="getBookCover(book)" :alt="book.name" class="book-cover" loading="lazy">
            <div class="book-spine"></div>
            <div class="book-overlay">
              <span class="view-btn">查看详情</span>
            </div>
          </div>
          <div class="book-info">
            <h4 class="book-title">{{ book.name }}</h4>
            <p class="book-author">〔{{ book.author }}〕</p>
            <p class="book-price">
              <span class="currency">¥</span>
              <span class="amount">{{ formatPrice(book.price) }}</span>
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- 销量排行 -->
    <section class="ranking-section" v-if="salesRanking.length > 0">
      <div class="section-header">
        <h2 class="section-title scroll-animate">
          <span class="decor">◇</span>
          <span>销量排行</span>
          <span class="decor">◇</span>
        </h2>
        <router-link to="/rankings" class="more-link">查看更多 →</router-link>
      </div>
      <div class="ranking-list">
        <div v-for="(book, index) in salesRanking" :key="book.id" class="ranking-item scroll-animate" @click="goBookDetail(book.id)">
          <span :class="['rank-num', index < 3 ? 'top' : '']">{{ index + 1 }}</span>
          <img :src="getBookCover(book)" class="ranking-cover" />
          <div class="ranking-info">
            <h4>{{ book.name }}</h4>
            <p>{{ book.author }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 猜你喜欢 -->
    <section class="recommend-section" v-if="recommendBooks.length > 0">
      <div class="section-header">
        <h2 class="section-title scroll-animate">
          <span class="decor">◇</span>
          <span>猜你喜欢</span>
          <span class="decor">◇</span>
        </h2>
      </div>
      <div class="books-showcase">
        <div v-for="(book, index) in recommendBooks" :key="book.id" class="book-card scroll-animate" :style="{ animationDelay: `${index * 0.1}s` }" @click="goBookDetail(book.id)">
          <div class="book-cover-wrapper">
            <img :src="getBookCover(book)" :alt="book.name" class="book-cover" loading="lazy">
            <div class="book-overlay">
              <span class="view-btn">查看详情</span>
            </div>
          </div>
          <div class="book-info">
            <h4 class="book-title">{{ book.name }}</h4>
            <p class="book-author">〔{{ book.author }}〕</p>
            <p class="book-price">
              <span class="currency">¥</span>
              <span class="amount">{{ formatPrice(book.price) }}</span>
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- 名言引用 -->
    <section class="quote-section scroll-animate">
      <div class="quote-content" v-if="currentQuote">
        <span class="quote-start">「</span>
        <p class="quote-text">{{ currentQuote.content }}</p>
        <span class="quote-end">」</span>
        <p class="quote-source" v-if="currentQuote.authorName">—— {{ currentQuote.authorName }}</p>
      </div>
      <div class="quote-content" v-else>
        <span class="quote-start">「</span>
        <p class="quote-text">书中自有黄金屋，书中自有颜如玉</p>
        <span class="quote-end">」</span>
        <p class="quote-source">—— 《劝学诗》</p>
      </div>
    </section>

    <!-- 页脚装饰 -->
    <div class="footer-decoration">
      <div class="cloud cloud-1"></div>
      <div class="cloud cloud-2"></div>
      <div class="cloud cloud-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { categoryAPI, bookAPI, bannerAPI, quoteAPI, rankingAPI, recommendAPI } from '../api'

const router = useRouter()
const categories = ref([])
const hotBooks = ref([])
const banners = ref([])
const currentQuote = ref(null)
const salesRanking = ref([])
const recommendBooks = ref([])

const mockBooks = [
  { id: 1, name: '红楼梦', author: '曹雪芹', price: 98.00, isbn: '9787020002200', categoryId: 1 },
  { id: 2, name: '活着', author: '余华', price: 45.00, isbn: '9787544250894', categoryId: 1 },
  { id: 3, name: '三体', author: '刘慈欣', price: 68.00, isbn: '9787536692930', categoryId: 1 },
  { id: 4, name: '百年孤独', author: '加西亚·马尔克斯', price: 55.00, isbn: '9787544244909', categoryId: 1 },
  { id: 5, name: '三国演义', author: '罗贯中', price: 78.00, isbn: '9787020000804', categoryId: 1 },
  { id: 6, name: '平凡的世界', author: '路遥', price: 108.00, isbn: '9787020024727', categoryId: 1 },
  { id: 7, name: '追风筝的人', author: '卡勒德·胡赛尼', price: 39.00, isbn: '9787208061644', categoryId: 1 },
  { id: 8, name: '围城', author: '钱钟书', price: 35.00, isbn: '9787020002217', categoryId: 1 }
]

// 滚动动画观察器
let observer = null

const initScrollAnimation = () => {
  observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible')
      }
    })
  }, { threshold: 0.1, rootMargin: '100px 0px' })

  document.querySelectorAll('.scroll-animate').forEach(el => {
    observer.observe(el)
  })

  // Fallback: make all elements visible after a short delay
  setTimeout(() => {
    document.querySelectorAll('.scroll-animate').forEach(el => {
      el.classList.add('visible')
    })
  }, 1000)
}

const loadCategories = async () => {
  try {
    const response = await categoryAPI.getAll()
    categories.value = response.data.slice(0, 6)
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const loadHotBooks = async () => {
  try {
    const response = await bookAPI.getAll({ limit: 8 })
    if (response.data && response.data.content && response.data.content.length > 0) {
      hotBooks.value = response.data.content.slice(0, 8)
    } else {
      hotBooks.value = mockBooks
    }
  } catch (error) {
    console.error('Failed to load books:', error)
    hotBooks.value = mockBooks
  }
}

const loadBanners = async () => {
  try {
    const res = await bannerAPI.getActive()
    banners.value = res.data || []
  } catch (e) {}
}

const loadQuote = async () => {
  try {
    const res = await quoteAPI.getRandom()
    if (res.data) currentQuote.value = res.data
  } catch (e) {}
}

const loadRanking = async () => {
  try {
    const res = await rankingAPI.getSales()
    salesRanking.value = (res.data || []).slice(0, 5)
  } catch (e) {}
}

const loadRecommend = async () => {
  try {
    const res = await recommendAPI.guessYouLike()
    recommendBooks.value = (res.data || []).slice(0, 6)
  } catch (e) {}
}

const goBooks = () => router.push('/books')
const goCategory = (id) => router.push(`/books?category=${id}`)
const goBookDetail = (id) => router.push(`/books/${id}`)

const getCategoryIcon = (id) => {
  const icons = ['📜', '📕', '📗', '📘', '📙', '📔']
  return icons[(id - 1) % icons.length]
}

const getBookCover = (book) => {
  return `https://picsum.photos/seed/${book.isbn}/200/280.jpg`
}

const formatPrice = (price) => {
  return typeof price === 'string' ? price : price.toFixed(2)
}

onMounted(() => {
  loadCategories()
  loadHotBooks()
  loadBanners()
  loadQuote()
  loadRanking()
  loadRecommend()
  setTimeout(() => {
    initScrollAnimation()
  }, 100)
})

onUnmounted(() => {
  if (observer) {
    observer.disconnect()
  }
})
</script>

<style scoped>
.home-container {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, var(--bg-primary) 0%, var(--bg-secondary) 50%, #e8e1d5 100%);
  position: relative;
  overflow-x: hidden;
  overflow-y: auto;
}

/* Hero区域 */
.hero-section {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at top, rgba(139, 90, 43, 0.1) 0%, transparent 50%),
    url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M0 30 Q15 20 30 30 T60 30' stroke='rgba(139, 90, 43, 0.1)' fill='none' stroke-width='1'/%3E%3C/svg%3E");
  background-size: 60px 60px;
}

/* 粒子效果 */
.particle {
  position: absolute;
  width: 8px;
  height: 8px;
  background: var(--primary);
  border-radius: 50%;
  opacity: 0.3;
  animation: particleFloat 6s ease-in-out infinite;
}

.particle-1 { top: 20%; left: 10%; animation-delay: 0s; }
.particle-2 { top: 60%; left: 20%; animation-delay: 1s; }
.particle-3 { top: 30%; right: 15%; animation-delay: 2s; }
.particle-4 { top: 70%; right: 25%; animation-delay: 3s; }
.particle-5 { top: 40%; left: 50%; animation-delay: 4s; }

@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-30px) rotate(180deg);
    opacity: 0.6;
  }
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  width: 100%;
  max-width: 800px;
  padding: 0 20px;
}

.brand-badge {
  display: inline-block;
  padding: 8px 24px;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: white;
  border-radius: 20px;
  font-size: 14px;
  letter-spacing: 4px;
  margin-bottom: 20px;
}

.main-title {
  font-family: var(--font-serif);
  font-size: 72px;
  font-weight: bold;
  color: var(--text-primary);
  margin: 20px 0;
  display: flex;
  justify-content: center;
  gap: 20px;
}

.char {
  position: relative;
  text-shadow: 2px 2px 4px rgba(139, 90, 43, 0.2);
  animation: charAppear 0.8s ease-out backwards;
  display: inline-block;
  transition: transform 0.3s ease;
}

.char:hover {
  transform: scale(1.1) rotate(-5deg);
  color: var(--primary);
}

.char::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 3px;
  background: var(--accent);
  transition: width 0.3s ease;
}

.char:hover::after {
  width: 100%;
}

@keyframes charAppear {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.8);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.sub-title {
  font-family: var(--font-serif);
  font-size: 28px;
  color: var(--primary);
  margin: 15px 0;
  letter-spacing: 10px;
  animation: fadeSlideUp 0.8s ease-out 0.8s backwards;
}

.description {
  font-size: 16px;
  color: var(--text-secondary);
  margin-top: 20px;
  letter-spacing: 2px;
  animation: fadeSlideUp 0.8s ease-out 1s backwards;
}

@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.explore-btn {
  margin-top: 40px;
  padding: 18px 50px;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  border: none;
  border-radius: 50px;
  color: white;
  font-family: var(--font-serif);
  font-size: 20px;
  letter-spacing: 4px;
  box-shadow: 0 8px 30px rgba(139, 90, 43, 0.4);
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  animation: fadeSlideUp 0.8s ease-out 1.2s backwards;
}

.explore-btn:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 12px 40px rgba(139, 90, 43, 0.5);
}

.arrow {
  font-size: 24px;
  transition: transform 0.3s ease;
}

.explore-btn:hover .arrow {
  transform: translateX(8px);
}

/* 滚动指示器 */
.scroll-indicator {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  animation: bounce 2s ease-in-out infinite;
}

.mouse {
  width: 24px;
  height: 38px;
  border: 2px solid var(--primary);
  border-radius: 12px;
  position: relative;
}

.wheel {
  width: 4px;
  height: 8px;
  background: var(--primary);
  border-radius: 2px;
  position: absolute;
  top: 6px;
  left: 50%;
  transform: translateX(-50%);
  animation: scrollWheel 1.5s ease-in-out infinite;
}

@keyframes scrollWheel {
  0%, 100% {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
  50% {
    opacity: 0.5;
    transform: translateX(-50%) translateY(10px);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateX(-50%) translateY(0);
  }
  50% {
    transform: translateX(-50%) translateY(-10px);
  }
}

.scroll-indicator span {
  font-size: 12px;
  color: var(--text-secondary);
  letter-spacing: 2px;
}

/* 分类区域 */
.categories-section, .featured-section {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 80px 40px;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-family: var(--font-serif);
  font-size: 40px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.decor {
  color: var(--accent);
  font-size: 24px;
  animation: sparkle 1.5s ease-in-out infinite;
}

.decor:nth-child(3) {
  animation-delay: 0.75s;
}

@keyframes sparkle {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.2);
  }
}

.divider {
  width: 100px;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--primary), transparent);
  margin: 20px auto 0;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 24px;
}

.category-card {
  background: var(--bg-card);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 40px 20px;
  text-align: center;
  transition: all 0.4s var(--ease-out);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.category-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--accent), var(--secondary), var(--accent));
  transform: scaleX(0);
  transition: transform 0.4s var(--ease-out);
}

.category-card:hover {
  transform: translateY(-8px) rotate(1deg);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary);
}

.category-card:hover::before {
  transform: scaleX(1);
}

.category-icon {
  font-size: 56px;
  margin-bottom: 16px;
  transition: transform 0.4s var(--ease-bounce);
}

.category-card:hover .category-icon {
  transform: scale(1.2) rotate(5deg);
}

.category-name {
  font-family: var(--font-serif);
  font-size: 22px;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.category-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* 精选图书 */
.books-showcase {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
}

.book-card {
  cursor: pointer;
  transition: all 0.4s var(--ease-out);
  text-align: center;
}

.book-card:hover {
  transform: translateY(-12px);
}

.book-cover-wrapper {
  position: relative;
  margin-bottom: 16px;
  display: flex;
  justify-content: center;
  perspective: 1000px;
}

.book-cover {
  width: 180px;
  height: 250px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  box-shadow:
    5px 5px 15px rgba(0, 0, 0, 0.2),
    -2px -2px 10px rgba(255, 255, 255, 0.5);
  transition: all 0.4s var(--ease-out);
}

.book-card:hover .book-cover {
  transform: rotateY(-15deg) rotateX(5deg);
  box-shadow:
    10px 10px 30px rgba(0, 0, 0, 0.3),
    -2px -2px 10px rgba(255, 255, 255, 0.5);
}

.book-spine {
  position: absolute;
  right: -8px;
  top: 0;
  bottom: 0;
  width: 12px;
  background: linear-gradient(90deg, var(--primary), var(--primary-light));
  border-radius: 0 3px 3px 0;
  transform: rotateY(-90deg);
  transform-origin: left;
  opacity: 0;
  transition: opacity 0.4s ease;
}

.book-card:hover .book-spine {
  opacity: 1;
}

.book-overlay {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 180px;
  height: 250px;
  background: rgba(139, 90, 43, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: var(--radius-sm);
}

.book-card:hover .book-overlay {
  opacity: 1;
}

.view-btn {
  padding: 12px 24px;
  background: white;
  color: var(--primary);
  border-radius: 20px;
  font-weight: 600;
  transform: translateY(20px);
  transition: transform 0.3s ease;
}

.book-card:hover .view-btn {
  transform: translateY(0);
}

.book-info {
  text-align: center;
}

.book-title {
  font-family: var(--font-serif);
  font-size: 20px;
  color: var(--text-primary);
  margin-bottom: 8px;
  height: 40px;
  overflow: hidden;
}

.book-author {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.book-price {
  font-size: 24px;
  color: var(--accent);
  font-weight: 700;
}

.currency {
  font-size: 16px;
}

/* 名言引用 */
.quote-section {
  padding: 100px 40px;
  text-align: center;
  background: linear-gradient(135deg, var(--primary-dark) 0%, var(--primary) 100%);
  position: relative;
  overflow: hidden;
}

.quote-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M0 30 Q15 20 30 30 T60 30' stroke='rgba(255, 255, 255, 0.1)' fill='none' stroke-width='1'/%3E%3C/svg%3E");
  background-size: 60px 60px;
}

.quote-content {
  position: relative;
  z-index: 1;
}

.quote-start, .quote-end {
  font-size: 60px;
  color: rgba(255, 255, 255, 0.3);
  font-family: var(--font-serif);
}

.quote-text {
  font-family: var(--font-serif);
  font-size: 32px;
  color: white;
  margin: 20px 0;
  letter-spacing: 4px;
}

/* 排行榜 */
.ranking-section {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 80px 40px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.ranking-item:hover {
  transform: translateX(8px);
  box-shadow: var(--shadow-md);
}

.rank-num {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 50%;
  font-size: 16px;
  font-weight: bold;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.rank-num.top {
  background: linear-gradient(135deg, #ffd700, #ffb800);
  color: white;
}

.ranking-cover {
  width: 60px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}

.ranking-info h4 {
  font-size: 16px;
  margin: 0 0 4px;
}

.ranking-info p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.more-link {
  color: var(--text-secondary);
  font-size: 14px;
  text-decoration: none;
}

.more-link:hover {
  color: var(--primary);
}

/* 猜你喜欢 */
.recommend-section {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px 80px;
}

.quote-source {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 20px;
}

/* 页脚装饰 */
.footer-decoration {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100px;
  pointer-events: none;
  z-index: -1;
}

.cloud {
  position: absolute;
  background: rgba(139, 90, 43, 0.05);
  border-radius: 50%;
  animation: float 6s ease-in-out infinite;
}

.cloud-1 {
  width: 200px;
  height: 60px;
  bottom: 20px;
  left: 10%;
  animation-delay: 0s;
}

.cloud-2 {
  width: 150px;
  height: 50px;
  bottom: 40px;
  left: 50%;
  animation-delay: 2s;
}

.cloud-3 {
  width: 180px;
  height: 55px;
  bottom: 30px;
  right: 10%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

/* 滚动动画 */
.scroll-animate {
  opacity: 1;
  transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .categories-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .books-showcase {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .main-title {
    font-size: 56px;
  }

  .categories-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .books-showcase {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .main-title {
    font-size: 42px;
    gap: 12px;
  }

  .sub-title {
    font-size: 20px;
    letter-spacing: 6px;
  }

  .categories-grid {
    grid-template-columns: 1fr;
  }

  .books-showcase {
    grid-template-columns: 1fr;
  }

  .section-title {
    font-size: 28px;
  }

  .quote-text {
    font-size: 22px;
  }
}
</style>
