<template>
  <div class="book-detail">
    <div v-if="book" class="detail-container">
      <div class="hero-section">
        <div class="section-decoration">
          <span class="decor-line"></span>
          <span class="decor-dot"></span>
          <span class="decor-line"></span>
        </div>
        <h1 class="page-title">图书详情</h1>
        <div class="section-decoration">
          <span class="decor-line"></span>
          <span class="decor-dot"></span>
          <span class="decor-line"></span>
        </div>
      </div>

      <div class="main-content">
        <div class="book-showcase animate-fade-up">
          <div class="book-cover-wrapper">
            <div class="cover-frame magnifier-container" @mousemove="handleMagnifier" @mouseleave="hideMagnifier" ref="magnifierContainer">
              <img :src="getCover(book)" :alt="book.name" class="book-cover" ref="magnifierImg">
              <div class="magnifier-lens" :style="lensStyle" v-show="showMagnifier"></div>
              <div class="book-shadow"></div>
            </div>
            <!-- 放大镜结果 -->
            <div class="magnifier-result" :style="resultStyle" v-show="showMagnifier"></div>
            <div class="book-badge" v-if="book.stock > 0">
                <span>有货</span>
              </div>
              <div class="book-badge out-of-stock" v-else>
                <span>缺货</span>
              </div>
          </div>

          <div class="book-info">
            <div class="book-header">
              <h2 class="book-title">{{ book.name }}</h2>
            </div>
            
            <div class="book-meta">
              <div class="meta-item">
                <span class="meta-label">作者</span>
                <span class="meta-value">{{ book.author }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">出版社</span>
                <span class="meta-value">{{ book.publisher }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">ISBN</span>
                <span class="meta-value">{{ book.isbn }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">页数</span>
                <span class="meta-value">{{ book.pages }} 页</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">分类</span>
                <span class="meta-value category-tag">{{ book.categoryName || '未分类' }}</span>
              </div>
            </div>

            <div class="price-section">
              <div class="current-price">
                <span class="currency">¥</span>
                <span class="amount">{{ formatPrice(book.price) }}</span>
              </div>
              <div class="stock-info">
                <span class="stock-label">库存:</span>
                <span :class="['stock-value', { 'low-stock': book.stock > 0 && book.stock <= 10 }]">
                  {{ book.stock }} 本
                </span>
              </div>
              <div class="quantity-selector">
                <span class="qty-label">数量:</span>
                <div class="qty-control">
                  <button 
                    class="qty-btn" 
                    @click="decreaseQuantity" 
                    :disabled="buyQuantity <= 1"
                  >-</button>
                  <input 
                    type="number" 
                    v-model="buyQuantity" 
                    class="qty-input"
                    :min="1"
                    :max="book.stock"
                  />
                  <button 
                    class="qty-btn" 
                    @click="increaseQuantity" 
                    :disabled="buyQuantity >= book.stock"
                  >+</button>
                </div>
              </div>
            </div>

            <div class="actions">
              <button
                class="action-btn primary"
                @click="addToCart"
                :disabled="book.stock <= 0"
              >
                <span class="btn-icon">🛒</span>
                <span>加入购物车</span>
              </button>
              <button
                class="action-btn secondary"
                @click="buyNow"
                :disabled="book.stock <= 0"
              >
                <span class="btn-icon">💰</span>
                <span>立即购买</span>
              </button>
              <button
                class="action-btn favorite-btn"
                :class="{ active: isFavorited }"
                @click="toggleFavorite"
              >
                <span class="btn-icon">{{ isFavorited ? '❤️' : '🤍' }}</span>
                <span>{{ isFavorited ? '已收藏' : '收藏' }}</span>
              </button>
              <button
                class="action-btn preview-btn"
                @click="showPreview = true"
              >
                <span class="btn-icon">📖</span>
                <span>试读</span>
              </button>
            </div>

            <!-- 评分统计 -->
            <div class="rating-summary" v-if="reviewStats.reviewCount > 0">
              <div class="rating-big">
                <span class="rating-number">{{ reviewStats.avgRating }}</span>
                <span class="rating-label">分</span>
              </div>
              <div class="rating-details">
                <StarRating :modelValue="reviewStats.avgRating" :readonly="true" :showValue="false" />
                <span class="review-count">{{ reviewStats.reviewCount }} 条评论</span>
              </div>
            </div>

            <!-- 标签 -->
            <div class="book-tags" v-if="bookTags.length > 0">
              <span
                v-for="tag in bookTags"
                :key="tag.id"
                class="tag-item"
                @click="router.push(`/books?tag=${tag.id}`)"
              >{{ tag.name }}</span>
            </div>
          </div>
        </div>

        <div class="book-details">
          <div class="detail-tabs">
            <button 
              v-for="tab in tabs" 
              :key="tab.key"
              :class="['tab-btn', { active: activeTab === tab.key }]"
              @click="activeTab = tab.key"
            >
              <span>{{ tab.icon }}</span>
              <span>{{ tab.label }}</span>
            </button>
          </div>

          <div class="tab-content">
            <div v-if="activeTab === 'summary'" class="tab-panel">
              <h3 class="panel-title">内容简介</h3>
              <div class="summary-content">
                <p>{{ book.summary || '暂无简介' }}</p>
              </div>
            </div>

            <div v-if="activeTab === 'catalog'" class="tab-panel">
              <h3 class="panel-title">图书目录</h3>
              <div class="catalog-content">
                <div 
                  v-for="(chapter, index) in catalogItems" 
                  :key="index"
                  class="chapter-item"
                >
                  <span class="chapter-number">{{ index + 1 }}</span>
                  <span class="chapter-title">{{ chapter }}</span>
                </div>
                <p v-if="!book.catalog" class="empty-catalog">暂无目录信息</p>
              </div>
            </div>

            <div v-if="activeTab === 'info'" class="tab-panel">
              <h3 class="panel-title">详细信息</h3>
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">书名</span>
                  <span class="info-value">{{ book.name }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">作者</span>
                  <span class="info-value">{{ book.author }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">出版社</span>
                  <span class="info-value">{{ book.publisher }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">ISBN</span>
                  <span class="info-value">{{ book.isbn }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">页数</span>
                  <span class="info-value">{{ book.pages }} 页</span>
                </div>
                <div class="info-item">
                  <span class="info-label">定价</span>
                  <span class="info-value">¥{{ formatPrice(book.price) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">库存</span>
                  <span class="info-value">{{ book.stock }} 本</span>
                </div>
                <div class="info-item">
                  <span class="info-label">分类</span>
                  <span class="info-value">{{ book.categoryName || '未分类' }}</span>
                </div>
              </div>
            </div>

            <div v-if="activeTab === 'reviews'" class="tab-panel">
              <h3 class="panel-title">用户评论</h3>

              <!-- 发表评论 -->
              <div class="review-form" v-if="isLoggedIn">
                <div class="form-header">
                  <span>发表评论</span>
                </div>
                <div class="form-rating">
                  <span class="form-label">评分：</span>
                  <StarRating v-model="newReview.rating" />
                </div>
                <textarea
                  v-model="newReview.content"
                  class="review-textarea"
                  placeholder="分享你的阅读感受..."
                  rows="4"
                ></textarea>
                <button class="submit-review-btn" @click="submitReview" :disabled="!newReview.rating">
                  发表评论
                </button>
              </div>
              <div class="login-hint" v-else>
                <p>请先 <router-link to="/login">登录</router-link> 后发表评论</p>
              </div>

              <!-- 评论列表 -->
              <div class="review-list" v-if="reviews.length > 0">
                <div v-for="review in reviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <div class="reviewer-info">
                      <div class="reviewer-avatar">{{ (review.username || '匿名').charAt(0) }}</div>
                      <div class="reviewer-detail">
                        <span class="reviewer-name">{{ review.username || '匿名用户' }}</span>
                        <span class="review-time">{{ formatTime(review.createdAt) }}</span>
                      </div>
                    </div>
                    <StarRating :modelValue="review.rating" :readonly="true" :showValue="false" />
                  </div>
                  <div class="review-content">{{ review.content }}</div>
                  <div class="review-actions" v-if="review.userId == currentUserId">
                    <button class="review-action-btn" @click="deleteReview(review.id)">删除</button>
                  </div>
                </div>
              </div>
              <div class="empty-reviews" v-else>
                <p>暂无评论，快来发表第一条评论吧！</p>
              </div>
            </div>
          </div>
        </div>

        <div class="related-books" v-if="relatedBooks.length > 0">
          <div class="section-header">
            <span class="decor">◆</span>
            <h3>相关推荐</h3>
            <span class="decor">◆</span>
          </div>
          <div class="related-grid">
            <div 
              v-for="related in relatedBooks" 
              :key="related.id"
              class="related-card"
              @click="goToBook(related.id)"
            >
              <img :src="getCover(related)" :alt="related.name" class="related-cover">
              <div class="related-info">
                <h4 class="related-title">{{ related.name }}</h4>
                <p class="related-author">{{ related.author }}</p>
                <p class="related-price">¥{{ formatPrice(related.price) }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-card v-else class="loading-card">
      <div class="loading-content">
        <span class="loading-icon">📚</span>
        <p>加载中...</p>
      </div>
    </el-card>

    <!-- 图书试读弹窗 -->
    <BookPreview
      :visible="showPreview"
      :book-id="book?.id"
      :book-name="book?.name"
      :book-price="book?.price"
      :summary="book?.summary"
      :catalog="book?.catalog"
      @close="showPreview = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { bookAPI, cartAPI, reviewAPI, favoriteAPI, tagAPI } from '../api'
import { ElMessage } from 'element-plus'
import { useCartCount } from '../utils/cartCount'
import StarRating from '../components/StarRating.vue'
import BookPreview from '../components/BookPreview.vue'

const { setCartCount } = useCartCount()

const router = useRouter()
const route = useRoute()
const book = ref(null)
const relatedBooks = ref([])
const activeTab = ref('summary')
const buyQuantity = ref(1)
const showPreview = ref(false)

// 评论相关
const reviews = ref([])
const reviewStats = ref({ avgRating: 0, reviewCount: 0, ratingDistribution: {} })
const newReview = reactive({ rating: 0, content: '' })
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const currentUserId = computed(() => localStorage.getItem('userId'))

// 收藏相关
const isFavorited = ref(false)

// 标签相关
const bookTags = ref([])

// 放大镜
const magnifierContainer = ref(null)
const magnifierImg = ref(null)
const showMagnifier = ref(false)
const lensPos = reactive({ x: 0, y: 0 })
const zoomLevel = 2.5

const lensStyle = computed(() => ({
  left: (lensPos.x - 60) + 'px',
  top: (lensPos.y - 60) + 'px'
}))

const resultStyle = computed(() => {
  if (!magnifierContainer.value || !magnifierImg.value) return {}
  const img = magnifierImg.value
  const bgX = (lensPos.x / img.offsetWidth) * 100
  const bgY = (lensPos.y / img.offsetHeight) * 100
  return {
    backgroundImage: `url(${getCover(book.value)})`,
    backgroundSize: `${img.offsetWidth * zoomLevel}px ${img.offsetHeight * zoomLevel}px`,
    backgroundPosition: `${bgX}% ${bgY}%`
  }
})

const handleMagnifier = (e) => {
  const rect = magnifierContainer.value.getBoundingClientRect()
  lensPos.x = e.clientX - rect.left
  lensPos.y = e.clientY - rect.top
  showMagnifier.value = true
}

const hideMagnifier = () => {
  showMagnifier.value = false
}

const tabs = [
  { key: 'summary', label: '内容简介', icon: '📖' },
  { key: 'catalog', label: '图书目录', icon: '📋' },
  { key: 'info', label: '详细信息', icon: '📝' },
  { key: 'reviews', label: '评论', icon: '💬' }
]

const catalogItems = computed(() => {
  if (!book.value?.catalog) return []
  const catalog = book.value.catalog
  let items = []
  if (catalog.includes('\n')) {
    items = catalog.split('\n').map(item => item.trim()).filter(item => item)
  } else if (catalog.includes('|')) {
    items = catalog.split('|').map(item => item.trim()).filter(item => item)
  } else if (catalog.includes('\\n')) {
    items = catalog.split('\\n').map(item => item.trim()).filter(item => item)
  } else {
    items = [catalog.trim()]
  }
  return items
})

const loadBook = async () => {
  const id = route.params.id
  try {
    const response = await bookAPI.getById(id)
    book.value = response.data
    loadRelatedBooks()
  } catch (error) {
    console.error('Failed to load book:', error)
  }
}

const loadRelatedBooks = async () => {
  try {
    const categoryId = book.value?.categoryId
    if (categoryId) {
      const response = await bookAPI.getAll({ categoryId, page: 0, size: 4 })
      relatedBooks.value = (response.data.content || []).filter(b => b.id !== book.value?.id).slice(0, 4)
    } else {
      const response = await bookAPI.getAll({ limit: 4 })
      relatedBooks.value = (response.data.content || []).filter(b => b.id !== book.value?.id).slice(0, 4)
    }
  } catch (error) {
    console.error('Failed to load related books:', error)
  }
}

// 加载评论
const loadReviews = async () => {
  try {
    const bookId = route.params.id
    const [reviewsRes, statsRes] = await Promise.all([
      reviewAPI.getByBook(bookId),
      reviewAPI.getStats(bookId)
    ])
    reviews.value = reviewsRes.data
    reviewStats.value = statsRes.data
  } catch (error) {
    console.error('Failed to load reviews:', error)
  }
}

// 发表评论
const submitReview = async () => {
  if (!newReview.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  try {
    await reviewAPI.create({
      bookId: Number(route.params.id),
      rating: newReview.rating,
      content: newReview.content
    })
    ElMessage.success('评论发表成功')
    newReview.rating = 0
    newReview.content = ''
    loadReviews()
    loadBook() // 刷新书籍评分
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '评论失败')
  }
}

// 删除评论
const deleteReview = async (reviewId) => {
  try {
    await reviewAPI.delete(reviewId)
    ElMessage.success('评论已删除')
    loadReviews()
    loadBook()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN')
}

// 收藏相关
const checkFavorite = async () => {
  if (!isLoggedIn.value) return
  try {
    const res = await favoriteAPI.check(route.params.id)
    isFavorited.value = res.data.isFavorited
  } catch (error) {}
}

const toggleFavorite = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    if (isFavorited.value) {
      await favoriteAPI.remove(route.params.id)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteAPI.add(route.params.id)
      isFavorited.value = true
      ElMessage.success('已收藏')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 标签相关
const loadTags = async () => {
  try {
    const res = await tagAPI.getByBook(route.params.id)
    bookTags.value = res.data
  } catch (error) {}
}

const increaseQuantity = () => {
  if (buyQuantity.value < book.value?.stock) {
    buyQuantity.value++
  }
}

const decreaseQuantity = () => {
  if (buyQuantity.value > 1) {
    buyQuantity.value--
  }
}

const addToCart = async () => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    await cartAPI.addToCart({
      bookId: book.value.id,
      quantity: buyQuantity.value
    })
    ElMessage.success('已加入购物车')
    const cartRes = await cartAPI.getCart()
    setCartCount((cartRes.data || []).length)
  } catch (error) {
    const message = error.response?.data?.message || '添加失败'
    ElMessage.error(message)
  }
}

const buyNow = () => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(`/buy-now?bookId=${book.value.id}&quantity=${buyQuantity.value}`)
}

const goToBook = (id) => {
  router.push(`/books/${id}`)
}

const getCover = (book) => `https://picsum.photos/seed/${book.isbn || book.id}/200/280.jpg`

const formatPrice = (price) => typeof price === 'string' ? price : price.toFixed(2)

onMounted(() => {
  loadBook()
  loadReviews()
  checkFavorite()
  loadTags()
})
</script>

<style scoped>
.book-detail {
  min-height: 100vh;
  background: linear-gradient(180deg, #faf7f2 0%, #f0ebe3 100%);
  padding: 40px 20px;
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.hero-section {
  text-align: center;
  margin-bottom: 50px;
}

.section-decoration {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 15px;
}

.decor-line {
  width: 80px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #8b5a2b);
}

.decor-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #c21500;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.9); }
}

.page-title {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 32px;
  color: #6b4423;
  margin: 0;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.book-showcase {
  display: flex;
  gap: 50px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(139, 90, 43, 0.1);
  border: 1px solid #e8e0d0;
}

.book-cover-wrapper {
  position: relative;
  flex-shrink: 0;
}

.cover-frame {
  position: relative;
  width: 220px;
  height: 300px;
  cursor: crosshair;
}

.book-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease;
  pointer-events: none;
}

.cover-frame:hover .book-cover {
  transform: scale(1.02);
}

/* 放大镜镜头 */
.cover-frame .magnifier-lens {
  position: absolute;
  width: 120px;
  height: 120px;
  border: 3px solid rgba(139, 90, 43, 0.5);
  border-radius: 50%;
  pointer-events: none;
  z-index: 10;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.8), 0 4px 20px rgba(0, 0, 0, 0.2);
  transition: opacity 0.15s ease;
}

/* 放大镜结果面板 */
.book-cover-wrapper .magnifier-result {
  position: absolute;
  top: 0;
  left: calc(100% + 20px);
  width: 300px;
  height: 300px;
  border: 2px solid #d4c4a8;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(139, 90, 43, 0.15);
  background-repeat: no-repeat;
  z-index: 20;
  pointer-events: none;
}

.book-shadow {
  position: absolute;
  bottom: -10px;
  left: 10px;
  right: -10px;
  height: 20px;
  background: rgba(139, 90, 43, 0.2);
  border-radius: 50%;
  filter: blur(10px);
}

.book-badge {
  position: absolute;
  top: -10px;
  right: -10px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #228B22, #32CD32);
  border-radius: 20px;
  color: white;
  font-size: 14px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(34, 139, 34, 0.4);
}

.book-badge.out-of-stock {
  background: linear-gradient(135deg, #8B0000, #DC143C);
  box-shadow: 0 4px 12px rgba(139, 0, 0, 0.4);
}

.book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.book-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #d4c4a8;
}

.book-title {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 32px;
  color: #6b4423;
  margin: 0;
}

.seal-icon {
  font-size: 32px;
  color: #c21500;
  animation: rotate 3s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.book-meta {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.meta-label {
  font-size: 13px;
  color: #8b5a2b;
  font-weight: bold;
}

.meta-value {
  font-size: 15px;
  color: #6b4423;
}

.category-tag {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(139, 90, 43, 0.1);
  border-radius: 12px;
  font-size: 13px;
}

.price-section {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(194, 21, 0, 0.05), rgba(194, 21, 0, 0.1));
  border-radius: 12px;
  border: 1px solid rgba(194, 21, 0, 0.2);
}

.current-price {
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: 20px;
  color: #c21500;
  font-weight: bold;
}

.amount {
  font-size: 42px;
  color: #c21500;
  font-weight: bold;
  font-family: 'STKaiti', 'KaiTi', serif;
}

.stock-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stock-label {
  color: #8b5a2b;
}

.stock-value {
  color: #228B22;
  font-weight: bold;
}

.stock-value.low-stock {
  color: #FF6347;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.qty-label {
  color: #8b5a2b;
  font-size: 14px;
}

.qty-control {
  display: flex;
  align-items: center;
  border: 2px solid #d4c4a8;
  border-radius: 8px;
  overflow: hidden;
}

.qty-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #faf7f2, #f0ebe3);
  border: none;
  font-size: 20px;
  color: #8b5a2b;
  cursor: pointer;
  transition: all 0.3s ease;
}

.qty-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  color: white;
}

.qty-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.qty-input {
  width: 60px;
  height: 36px;
  text-align: center;
  border: none;
  outline: none;
  font-size: 16px;
  color: #6b4423;
  font-weight: bold;
  background: white;
}

.qty-input::-webkit-inner-spin-button,
.qty-input::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.actions {
  display: flex;
  gap: 20px;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px 30px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.action-btn::after {
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

.action-btn:hover::after {
  left: 120%;
}

.action-btn.primary {
  background: linear-gradient(135deg, #c21500, #a30000);
  color: white;
  box-shadow: 0 4px 15px rgba(194, 21, 0, 0.4);
}

.action-btn.primary:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(194, 21, 0, 0.5);
}

.action-btn.secondary {
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  color: white;
  box-shadow: 0 4px 15px rgba(139, 90, 43, 0.4);
}

.action-btn.secondary:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(139, 90, 43, 0.5);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 20px;
}

.book-details {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(139, 90, 43, 0.1);
  border: 1px solid #e8e0d0;
}

.detail-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #d4c4a8;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 25px;
  background: transparent;
  border: 2px solid #e8e0d0;
  border-radius: 8px;
  color: #8b5a2b;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  border-color: #8b5a2b;
  background: rgba(139, 90, 43, 0.05);
}

.tab-btn.active {
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  border-color: #8b5a2b;
  color: white;
}

.tab-panel {
  min-height: 200px;
}

.panel-title {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 20px;
  color: #6b4423;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #d4c4a8;
}

.summary-content p {
  color: #555;
  line-height: 1.8;
  font-size: 16px;
  text-indent: 2em;
}

.catalog-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
}

.catalog-content::-webkit-scrollbar {
  width: 6px;
}

.catalog-content::-webkit-scrollbar-track {
  background: rgba(139, 90, 43, 0.1);
  border-radius: 3px;
}

.catalog-content::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  border-radius: 3px;
}

.chapter-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(139, 90, 43, 0.03), rgba(139, 90, 43, 0.08));
  border-radius: 10px;
  border-left: 3px solid transparent;
  transition: all 0.3s ease;
}

.chapter-item:hover {
  background: linear-gradient(135deg, rgba(139, 90, 43, 0.08), rgba(139, 90, 43, 0.15));
  border-left-color: #8b5a2b;
  transform: translateX(3px);
}

.chapter-item.part {
  background: linear-gradient(135deg, rgba(194, 21, 0, 0.05), rgba(194, 21, 0, 0.1));
  border-left-color: #c21500;
  margin: 15px 0;
  padding: 14px 20px;
}

.chapter-item.part:hover {
  background: linear-gradient(135deg, rgba(194, 21, 0, 0.1), rgba(194, 21, 0, 0.18));
}

.chapter-number {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  color: white;
  border-radius: 6px;
  font-size: 13px;
  font-weight: bold;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(139, 90, 43, 0.3);
}

.chapter-item.part .chapter-number {
  background: linear-gradient(135deg, #c21500, #a30000);
  box-shadow: 0 2px 8px rgba(194, 21, 0, 0.4);
}

.chapter-title {
  color: #6b4423;
  font-size: 15px;
  line-height: 1.6;
  flex: 1;
}

.chapter-item.part .chapter-title {
  font-weight: bold;
  font-size: 16px;
  color: #c21500;
}

.empty-catalog {
  color: #888;
  text-align: center;
  padding: 30px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 20px;
  background: rgba(139, 90, 43, 0.05);
  border-radius: 8px;
}

.info-label {
  color: #8b5a2b;
  font-weight: bold;
}

.info-value {
  color: #6b4423;
}

.related-books {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(139, 90, 43, 0.1);
  border: 1px solid #e8e0d0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  margin-bottom: 30px;
}

.section-header h3 {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 22px;
  color: #6b4423;
  margin: 0;
}

.decor {
  color: #c21500;
  font-size: 18px;
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.related-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
  background: white;
  border: 1px solid #e8e0d0;
}

.related-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(139, 90, 43, 0.15);
}

.related-cover {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.related-info {
  padding: 15px;
}

.related-title {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 16px;
  color: #6b4423;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-author {
  font-size: 13px;
  color: #8b5a2b;
  margin: 0 0 10px 0;
}

.related-price {
  font-size: 18px;
  color: #c21500;
  font-weight: bold;
  margin: 0;
}

.loading-card {
  text-align: center;
  padding: 80px;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.loading-icon {
  font-size: 64px;
  animation: bounce 1s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@media (max-width: 768px) {
  .book-showcase {
    flex-direction: column;
    align-items: center;
  }

  .book-cover-wrapper {
    margin-bottom: 20px;
  }

  .book-meta {
    grid-template-columns: 1fr;
  }

  .price-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .related-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 收藏按钮 */
.favorite-btn {
  background: linear-gradient(135deg, #f5f0e8, #ebe3d5) !important;
  color: #8b5a2b !important;
  border: 2px solid #d4c4a8 !important;
  box-shadow: 0 4px 12px rgba(139, 90, 43, 0.15) !important;
}

.favorite-btn:hover {
  border-color: #c21500 !important;
  color: #c21500 !important;
}

.favorite-btn.active {
  background: linear-gradient(135deg, #c21500, #a30000) !important;
  color: white !important;
  border-color: #c21500 !important;
}

/* 评分统计 */
.rating-summary {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.05), rgba(245, 158, 11, 0.1));
  border-radius: 12px;
  border: 1px solid rgba(245, 158, 11, 0.2);
}

.rating-big {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.rating-number {
  font-size: 42px;
  font-weight: bold;
  color: #f59e0b;
  font-family: 'STKaiti', 'KaiTi', serif;
}

.rating-label {
  font-size: 16px;
  color: #8b5a2b;
}

.rating-details {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.review-count {
  font-size: 14px;
  color: #8b7355;
}

/* 标签 */
.book-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  padding: 6px 16px;
  background: rgba(139, 90, 43, 0.08);
  border: 1px solid rgba(139, 90, 43, 0.2);
  border-radius: 20px;
  font-size: 13px;
  color: #8b5a2b;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tag-item:hover {
  background: rgba(139, 90, 43, 0.15);
  border-color: #8b5a2b;
}

/* 评论区 */
.review-form {
  background: #faf7f2;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 30px;
  border: 1px solid #e8e0d0;
}

.form-header {
  font-weight: bold;
  color: #6b4423;
  margin-bottom: 15px;
  font-size: 16px;
}

.form-rating {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.form-label {
  color: #8b5a2b;
  font-size: 14px;
}

.review-textarea {
  width: 100%;
  padding: 15px;
  border: 2px solid #e8e0d0;
  border-radius: 10px;
  font-size: 14px;
  color: #6b4423;
  resize: vertical;
  outline: none;
  transition: border-color 0.3s ease;
  font-family: inherit;
}

.review-textarea:focus {
  border-color: #8b5a2b;
}

.submit-review-btn {
  margin-top: 15px;
  padding: 12px 30px;
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-review-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 90, 43, 0.4);
}

.submit-review-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.login-hint {
  text-align: center;
  padding: 30px;
  background: #faf7f2;
  border-radius: 12px;
  margin-bottom: 30px;
  border: 1px dashed #d4c4a8;
}

.login-hint a {
  color: #8b5a2b;
  font-weight: bold;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e8e0d0;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reviewer-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 16px;
}

.reviewer-detail {
  display: flex;
  flex-direction: column;
}

.reviewer-name {
  font-weight: bold;
  color: #6b4423;
}

.review-time {
  font-size: 12px;
  color: #8b7355;
}

.review-content {
  color: #555;
  line-height: 1.8;
  font-size: 14px;
}

.review-actions {
  margin-top: 10px;
  text-align: right;
}

.review-action-btn {
  padding: 6px 16px;
  background: transparent;
  border: 1px solid #d4c4a8;
  border-radius: 6px;
  color: #8b5a2b;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.review-action-btn:hover {
  background: #c21500;
  border-color: #c21500;
  color: white;
}

.empty-reviews {
  text-align: center;
  padding: 40px;
  color: #8b7355;
  background: #faf7f2;
  border-radius: 12px;
  border: 1px dashed #d4c4a8;
}
</style>
