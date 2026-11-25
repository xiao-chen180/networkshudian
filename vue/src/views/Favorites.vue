<template>
  <div class="favorites-page">
    <div class="hero-section">
      <div class="section-decoration">
        <span class="decor-line"></span>
        <span class="decor-dot"></span>
        <span class="decor-line"></span>
      </div>
      <h1 class="page-title">我的收藏</h1>
      <div class="section-decoration">
        <span class="decor-line"></span>
        <span class="decor-dot"></span>
        <span class="decor-line"></span>
      </div>
    </div>

    <div class="favorites-container">
      <div v-if="favorites.length > 0" class="favorites-grid">
        <div
          v-for="fav in favorites"
          :key="fav.id"
          class="favorite-card"
        >
          <div class="cover-wrapper" @click="goToBook(fav.bookId)">
            <img :src="getCover(fav)" :alt="fav.bookName" class="book-cover" />
            <div class="cover-overlay">
              <span class="view-btn">查看详情</span>
            </div>
          </div>
          <div class="book-info">
            <h3 class="book-name" @click="goToBook(fav.bookId)">{{ fav.bookName }}</h3>
            <p class="book-author">{{ fav.bookAuthor }}</p>
            <p class="book-category" v-if="fav.categoryName">{{ fav.categoryName }}</p>
            <div class="book-bottom">
              <span class="book-price">¥{{ formatPrice(fav.bookPrice) }}</span>
              <button class="remove-btn" @click="removeFavorite(fav.bookId)">
                取消收藏
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">💝</div>
        <h3>收藏夹是空的</h3>
        <p>快去挑选你喜欢的书吧！</p>
        <button class="go-shop-btn" @click="router.push('/books')">去逛逛</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { favoriteAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const favorites = ref([])

const loadFavorites = async () => {
  try {
    const res = await favoriteAPI.getAll()
    favorites.value = res.data
  } catch (error) {
    console.error('Failed to load favorites:', error)
  }
}

const removeFavorite = async (bookId) => {
  try {
    await favoriteAPI.remove(bookId)
    favorites.value = favorites.value.filter(f => f.bookId !== bookId)
    ElMessage.success('已取消收藏')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goToBook = (bookId) => {
  router.push(`/books/${bookId}`)
}

const getCover = (fav) => `https://picsum.photos/seed/${fav.bookId}/200/280.jpg`

const formatPrice = (price) => {
  if (!price) return '0.00'
  return typeof price === 'string' ? price : price.toFixed(2)
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #faf7f2 0%, #f0ebe3 100%);
  padding: 40px 20px;
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
}

.page-title {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 32px;
  color: #6b4423;
  margin: 0;
}

.favorites-container {
  max-width: 1200px;
  margin: 0 auto;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 30px;
}

.favorite-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(139, 90, 43, 0.1);
  border: 1px solid #e8e0d0;
  transition: all 0.3s ease;
}

.favorite-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(139, 90, 43, 0.2);
}

.cover-wrapper {
  position: relative;
  height: 280px;
  overflow: hidden;
  cursor: pointer;
}

.book-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.favorite-card:hover .book-cover {
  transform: scale(1.08);
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(139, 90, 43, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.favorite-card:hover .cover-overlay {
  opacity: 1;
}

.view-btn {
  padding: 10px 24px;
  background: white;
  color: #6b4423;
  border-radius: 25px;
  font-weight: 600;
  transform: translateY(10px);
  transition: transform 0.3s ease;
}

.favorite-card:hover .view-btn {
  transform: translateY(0);
}

.book-info {
  padding: 20px;
}

.book-name {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 18px;
  color: #6b4423;
  margin: 0 0 8px 0;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-name:hover {
  color: #c21500;
}

.book-author {
  font-size: 13px;
  color: #8b7355;
  margin: 0 0 8px 0;
}

.book-category {
  font-size: 12px;
  padding: 3px 10px;
  background: rgba(139, 90, 43, 0.08);
  border-radius: 12px;
  color: #8b5a2b;
  display: inline-block;
  margin-bottom: 12px;
}

.book-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.book-price {
  font-size: 22px;
  color: #c21500;
  font-weight: bold;
}

.remove-btn {
  padding: 6px 14px;
  background: transparent;
  border: 1px solid #d4c4a8;
  border-radius: 6px;
  color: #8b5a2b;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.remove-btn:hover {
  background: #c21500;
  border-color: #c21500;
  color: white;
}

.empty-state {
  text-align: center;
  padding: 80px 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(139, 90, 43, 0.1);
  border: 1px solid #e8e0d0;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 24px;
  color: #6b4423;
  margin-bottom: 10px;
}

.empty-state p {
  color: #8b7355;
  margin-bottom: 30px;
}

.go-shop-btn {
  padding: 14px 40px;
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.go-shop-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(139, 90, 43, 0.4);
}
</style>
