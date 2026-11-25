<template>
  <div class="detail-page">
    <div class="wrap" v-if="book">
      <div class="crumb">
        <router-link to="/">首页</router-link><i>/</i>
        <router-link to="/books">全部图书</router-link><i>/</i>
        <span>{{ book.name }}</span>
      </div>

      <!-- 主信息 -->
      <div class="top">
        <div class="visual">
          <div class="bigcover">
            <BookCover :src="book.imageUrl" :title="book.name" :author="book.author" ratio="3:4" />
          </div>
          <div class="visual-links">
            <a @click="activeTab = 'summary'">内容简介</a>
            <a @click="toggleFavorite">{{ isFavorited ? '已收藏 ✓' : '加入收藏' }}</a>
          </div>
        </div>

        <div class="info">
          <div class="flags">
            <span class="tagline" v-if="book.avgRating >= 4.5">编辑推荐</span>
            <span class="tagline muted">{{ book.categoryName || '未分类' }}</span>
            <span class="tagline muted" :class="{ warn: Number(book.stock) <= 0 }">
              {{ Number(book.stock) > 0 ? `现货 ${book.stock} 件` : '暂时缺货' }}
            </span>
          </div>

          <h1>{{ book.name }}</h1>
          <div class="byline">
            <b>{{ book.author }}</b>
            <span v-if="book.publisher"> 著 ／ {{ book.publisher }}</span>
          </div>
          <div class="metarow">
            <span v-if="book.publisher">{{ book.publisher }}</span>
            <span v-if="book.pages">{{ book.pages }} 页</span>
            <span v-if="book.isbn">ISBN {{ book.isbn }}</span>
          </div>

          <!-- 评分 -->
          <div class="rating">
            <div class="score">
              <b>{{ Number(book.avgRating || 0).toFixed(1) }}</b>
              <span>READER SCORE</span>
            </div>
            <div class="rright">
              <div class="starsline">
                <RatingStars :value="book.avgRating" size="lg" />
                <span class="cnt">{{ book.reviewCount || 0 }} 条书评</span>
              </div>
              <div class="ratebars">
                <div class="rb" v-for="r in 5" :key="r">
                  <span>{{ 6 - r }}</span>
                  <span class="bar"><i :style="{ width: pct(6 - r) }"></i></span>
                  <span class="pct">{{ pct(6 - r) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 价格与购买 -->
          <div class="buybox">
            <div class="priceline">
              <b>¥{{ formatPrice(book.price) }}</b>
              <span class="off" v-if="memberPrice">会员价 ¥{{ memberPrice }}</span>
            </div>
            <div class="stockline">
              <span>{{ Number(book.stock) > 0 ? `现货 ${book.stock} 件` : '补货中' }}</span>
              <span>48 小时内发货</span>
              <span>7 天无理由退换</span>
            </div>

            <div class="buyrow">
              <QuantityStepper v-model="buyQuantity" :max="Math.max(1, Number(book.stock) || 1)" />
              <button class="btn btn-fill big" :disabled="Number(book.stock) <= 0" @click="addToCart">加入购物袋</button>
              <button class="btn" :disabled="Number(book.stock) <= 0" @click="buyNow">立即购买</button>
              <button class="fav" :class="{ on: isFavorited }" @click="toggleFavorite" aria-label="收藏">
                <svg viewBox="0 0 24 24"><path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1-1.1a5.5 5.5 0 0 0-7.8 7.8l8.8 8.8 8.8-8.8a5.5 5.5 0 0 0 0-7.8Z" stroke="currentColor" fill="none" stroke-width="1.5"/></svg>
              </button>
            </div>

            <div class="services">
              <span><i>✓</i>正版保障</span>
              <span><i>✓</i>满 99 免运费</span>
              <span><i>✓</i>可开发票</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 详情主体 -->
      <div class="body">
        <div class="article">
          <div class="tabs">
            <a :class="{ on: activeTab === 'summary' }" @click="activeTab = 'summary'">内容简介</a>
            <a :class="{ on: activeTab === 'catalog' }" @click="activeTab = 'catalog'">目录</a>
            <a :class="{ on: activeTab === 'info' }" @click="activeTab = 'info'">详细信息</a>
            <a :class="{ on: activeTab === 'reviews' }" @click="activeTab = 'reviews'">书评 <em>{{ book.reviewCount || 0 }}</em></a>
          </div>

          <div class="tab-body">
            <template v-if="activeTab === 'summary'">
              <p v-for="(para, i) in summaryParas" :key="i">{{ para }}</p>
              <p v-if="!summaryParas.length" class="muted">这本书还没有简介。</p>
              <div class="tags" v-if="bookTags.length">
                <span v-for="t in bookTags" :key="t.id || t.name">#{{ t.name }}</span>
              </div>
            </template>

            <template v-else-if="activeTab === 'catalog'">
              <div class="catalog" v-if="catalogItems.length">
                <div v-for="(item, i) in catalogItems" :key="i" class="cat-item">
                  <span class="idx">{{ String(i + 1).padStart(2, '0') }}</span>{{ item }}
                </div>
              </div>
              <p v-else class="muted">暂无目录信息。</p>
            </template>

            <template v-else-if="activeTab === 'info'">
              <div class="keyinfo">
                <div><dt>出版社</dt><dd>{{ book.publisher || '—' }}</dd></div>
                <div><dt>页数</dt><dd>{{ book.pages || '—' }} 页</dd></div>
                <div><dt>ISBN</dt><dd>{{ book.isbn || '—' }}</dd></div>
                <div><dt>所属分类</dt><dd>{{ book.categoryName || '未分类' }}</dd></div>
                <div><dt>库存</dt><dd>{{ book.stock }} 件</dd></div>
                <div><dt>累计销量</dt><dd>{{ book.salesCount || 0 }} 本</dd></div>
              </div>
            </template>

            <template v-else>
              <!-- 写书评 -->
              <div class="review-form" v-if="isLoggedIn">
                <div class="rf-top">
                  <span>给这本书打分</span>
                  <div class="star-pick">
                    <span v-for="s in 5" :key="s" :class="{ on: s <= newReview.rating }" @click="newReview.rating = s">★</span>
                  </div>
                </div>
                <textarea v-model="newReview.content" placeholder="写下你的读后感…" rows="3"></textarea>
                <button class="btn btn-fill sm" @click="submitReview">发表书评</button>
              </div>
              <div class="review-form" v-else>
                <p class="muted"><router-link to="/login">登录</router-link> 后可以发表书评。</p>
              </div>

              <div class="reviews" v-if="reviews.length">
                <div class="review" v-for="r in reviews" :key="r.id">
                  <div class="avatar">{{ (r.username || r.account || '读').charAt(0) }}</div>
                  <div class="rv-main">
                    <div class="rv-head">
                      <b>{{ r.username || r.account || '匿名读者' }}</b>
                      <span class="rv-stars">{{ '★'.repeat(r.rating) }}<i>{{ '★'.repeat(5 - r.rating) }}</i></span>
                      <span class="rv-time">{{ formatTime(r.createdAt) }}</span>
                      <span class="rv-del" v-if="String(r.userId) === String(currentUserId)" @click="deleteReview(r.id)">删除</span>
                    </div>
                    <p>{{ r.content }}</p>
                  </div>
                </div>
              </div>
              <p v-else class="muted">还没有书评，来写第一条吧。</p>
            </template>
          </div>
        </div>

        <!-- 相关推荐 -->
        <aside class="side">
          <h4>同分类推荐</h4>
          <div class="srow" v-for="b in relatedBooks" :key="b.id" @click="goBook(b.id)">
            <BookCover :src="b.imageUrl" :title="b.name" class="s-cover" compact ratio="3:4" />
            <div class="sc">
              <div class="t">{{ b.name }}</div>
              <div class="a">{{ b.author }}</div>
              <div class="p">¥{{ formatPrice(b.price) }}</div>
            </div>
          </div>
          <div class="note" v-if="Number(book.stock) <= 10 && Number(book.stock) > 0">
            <b>库存提醒</b>
            本书仅剩 {{ book.stock }} 件，售罄后需等待加印。
          </div>
        </aside>
      </div>
    </div>

    <div class="wrap" v-else-if="loading">
      <div class="skeleton-wrap">
        <SkeletonBlock width="280px" height="380px" />
        <div style="flex:1;display:flex;flex-direction:column;gap:14px">
          <SkeletonBlock width="60%" height="36px" />
          <SkeletonBlock width="40%" height="16px" />
          <SkeletonBlock width="100%" height="120px" />
        </div>
      </div>
    </div>

    <div class="wrap" v-else>
      <EmptyState title="没有找到这本书" description="它可能已下架" action-text="返回全部图书" :action-to="'/books'" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { bookAPI, cartAPI, reviewAPI, favoriteAPI, tagAPI } from '../api'
import { ElMessage } from 'element-plus'
import { useCartCount } from '../utils/cartCount'
import BookCover from '../components/common/BookCover.vue'
import RatingStars from '../components/common/RatingStars.vue'
import QuantityStepper from '../components/common/QuantityStepper.vue'
import SkeletonBlock from '../components/common/SkeletonBlock.vue'
import EmptyState from '../components/common/EmptyState.vue'

const { setCartCount } = useCartCount()
const router = useRouter()
const route = useRoute()

const book = ref(null)
const relatedBooks = ref([])
const reviews = ref([])
const reviewStats = ref({ ratingDistribution: {} })
const bookTags = ref([])
const activeTab = ref('summary')
const buyQuantity = ref(1)
const isFavorited = ref(false)
const loading = ref(true)
const newReview = reactive({ rating: 0, content: '' })

const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const currentUserId = computed(() => localStorage.getItem('userId'))
const discount = computed(() => Number(localStorage.getItem('discount') || 0.95))
const memberPrice = computed(() => {
  if (!book.value) return ''
  return (Number(book.value.price) * discount.value).toFixed(2)
})

const summaryParas = computed(() => {
  const s = book.value?.summary || ''
  return s.split(/\n+/).map(t => t.trim()).filter(Boolean)
})

const catalogItems = computed(() => {
  const c = book.value?.catalog || ''
  if (!c) return []
  return c.split(/\n|\||\\n/).map(t => t.trim()).filter(Boolean)
})

const pct = (star) => {
  const dist = reviewStats.value?.ratingDistribution || {}
  const v = Number(dist[star] ?? dist[String(star)] ?? 0)
  const total = Object.values(dist).reduce((s, n) => s + Number(n || 0), 0)
  if (!total) return '0%'
  return `${Math.round((v / total) * 100)}%`
}

const formatPrice = (p) => (typeof p === 'string' ? p : Number(p || 0).toFixed(2))
const formatTime = (t) => (t ? new Date(t).toLocaleDateString('zh-CN') : '')

async function loadBook() {
  loading.value = true
  try {
    const res = await bookAPI.getById(route.params.id)
    book.value = res.data
    loadRelated()
    loadReviews()
    loadTags()
    checkFavorite()
  } catch {
    book.value = null
  } finally {
    loading.value = false
  }
}

async function loadRelated() {
  try {
    const cid = book.value?.categoryId
    const res = await bookAPI.getAll({ categoryId: cid, page: 0, size: 5 })
    relatedBooks.value = (res.data.content || []).filter(b => b.id !== book.value?.id).slice(0, 4)
  } catch {}
}

async function loadReviews() {
  try {
    const [r, s] = await Promise.all([reviewAPI.getByBook(route.params.id), reviewAPI.getStats(route.params.id)])
    reviews.value = r.data || []
    reviewStats.value = s.data || {}
  } catch {}
}

async function loadTags() {
  try {
    const res = await tagAPI.getByBook(route.params.id)
    bookTags.value = res.data || []
  } catch {}
}

async function checkFavorite() {
  if (!isLoggedIn.value) return
  try {
    const res = await favoriteAPI.check(route.params.id)
    isFavorited.value = !!res.data?.isFavorited
  } catch {}
}

async function toggleFavorite() {
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
  } catch {
    ElMessage.error('操作失败')
  }
}

async function addToCart() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await cartAPI.addToCart({ bookId: book.value.id, quantity: buyQuantity.value })
    ElMessage.success('已加入购物袋')
    const res = await cartAPI.getCart()
    setCartCount((res.data || []).length)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '添加失败')
  }
}

function buyNow() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push({ path: '/buy-now', query: { bookId: book.value.id, quantity: buyQuantity.value } })
}

async function submitReview() {
  if (!newReview.rating) { ElMessage.warning('请选择评分'); return }
  if (!newReview.content.trim()) { ElMessage.warning('写点什么吧'); return }
  try {
    await reviewAPI.create({ bookId: Number(route.params.id), rating: newReview.rating, content: newReview.content })
    ElMessage.success('书评已发表')
    newReview.rating = 0
    newReview.content = ''
    loadReviews()
    loadBook()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发表失败')
  }
}

async function deleteReview(id) {
  try {
    await reviewAPI.delete(id)
    ElMessage.success('已删除')
    loadReviews()
    loadBook()
  } catch {
    ElMessage.error('删除失败')
  }
}

const goBook = (id) => router.push(`/books/${id}`)

onMounted(loadBook)
watch(() => route.params.id, (id) => { if (id) { buyQuantity.value = 1; activeTab.value = 'summary'; loadBook() } })
</script>

<style scoped>
.detail-page { min-height: 100vh; padding-bottom: 40px; }
.wrap { max-width: var(--container); margin: 0 auto; padding: 0 var(--gutter); }

.crumb { padding: 26px 0 0; font-size: 11px; color: var(--color-ink-faint); letter-spacing: .08em; display: flex; gap: 8px; align-items: center; }
.crumb a { color: var(--color-ink-muted); }
.crumb i { font-style: normal; opacity: .5; }

.top { display: flex; gap: 50px; margin-top: 30px; }
.visual { width: 320px; flex: none; }
.bigcover { width: 320px; }
.visual-links { display: flex; gap: 20px; margin-top: 18px; font-size: 11px; color: var(--color-ink-muted); letter-spacing: .1em; }
.visual-links a { border-bottom: 1px solid var(--color-line-strong); padding-bottom: 3px; cursor: pointer; }

.info { flex: 1; min-width: 0; }
.flags { display: flex; gap: 8px; margin-bottom: 18px; }
.tagline {
  font-size: 9.5px; letter-spacing: .14em; color: var(--color-gold);
  border: 1px solid var(--color-gold-line); padding: 3px 8px;
}
.tagline.muted { color: var(--color-ink-muted); border-color: var(--color-line-strong); }
.tagline.warn { color: var(--color-danger); border-color: var(--color-danger); }

h1 { font-family: var(--font-display); font-size: 40px; font-weight: 400; letter-spacing: .1em; line-height: 1.35; }
.byline { margin-top: 12px; font-size: 12.5px; color: var(--color-ink-soft); letter-spacing: .06em; }
.byline b { font-family: var(--font-display); font-size: 15px; font-weight: 400; color: var(--color-ink); }
.metarow { display: flex; gap: 18px; margin-top: 10px; font-size: 11px; color: var(--color-ink-muted); letter-spacing: .04em; flex-wrap: wrap; }
.metarow span { position: relative; padding-left: 18px; }
.metarow span:first-child { padding-left: 0; }
.metarow span + span::before { content: "·"; position: absolute; left: 6px; opacity: .5; }

.rating {
  display: flex; gap: 30px; align-items: center; margin-top: 24px;
  padding: 18px 0; border-top: 1px solid var(--color-line); border-bottom: 1px solid var(--color-line);
}
.score b { display: block; font-family: var(--font-display); font-size: 38px; color: var(--color-gold); line-height: 1; font-weight: 400; letter-spacing: -.02em; }
.score span { display: block; margin-top: 8px; font-size: 8.5px; letter-spacing: .22em; color: var(--color-ink-faint); }
.rright { flex: 1; max-width: 380px; }
.starsline { display: flex; align-items: center; gap: 12px; }
.cnt { font-size: 10px; color: var(--color-ink-muted); letter-spacing: .06em; }
.ratebars { display: flex; flex-direction: column; gap: 4px; margin-top: 12px; }
.rb { display: flex; align-items: center; gap: 9px; font-size: 9px; color: var(--color-ink-faint); }
.rb > span:first-child { width: 6px; }
.bar { flex: 1; height: 2px; background: var(--color-surface-3); position: relative; }
.bar i { position: absolute; left: 0; top: 0; bottom: 0; background: var(--color-gold); opacity: .75; }
.pct { width: 30px; text-align: right; }

.buybox { margin-top: 24px; background: var(--color-surface); border: 1px solid var(--color-line); padding: 22px 24px; }
.priceline { display: flex; align-items: baseline; gap: 14px; }
.priceline b { font-family: var(--font-display); font-size: 34px; color: var(--color-gold); font-weight: 400; letter-spacing: -.01em; }
.off { font-size: 10px; letter-spacing: .1em; color: var(--color-gold); border: 1px solid var(--color-gold-line); padding: 2px 7px; }
.stockline { display: flex; gap: 22px; margin-top: 14px; font-size: 10.5px; color: var(--color-ink-muted); letter-spacing: .06em; }
.buyrow { display: flex; gap: 10px; margin-top: 20px; align-items: center; }

.btn {
  height: 42px; padding: 0 20px; font-family: inherit; font-size: 12.5px; letter-spacing: .12em;
  display: inline-flex; align-items: center; justify-content: center; cursor: pointer;
  border: 1px solid var(--color-gold); background: transparent; color: var(--color-gold);
}
.btn-fill { background: var(--color-gold); color: #14150f; font-weight: 500; }
.btn.big { flex: 1; }
.btn.sm { height: 34px; padding: 0 16px; margin-top: 10px; }
.btn:disabled { opacity: .35; cursor: not-allowed; }

.fav { width: 42px; height: 42px; border: 1px solid var(--color-line-strong); background: transparent; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.fav svg { width: 15px; height: 15px; color: var(--color-ink-muted); }
.fav.on svg { color: var(--color-gold); fill: var(--color-gold); }

.services { display: flex; gap: 22px; margin-top: 18px; font-size: 10px; color: var(--color-ink-muted); letter-spacing: .06em; }
.services i { font-style: normal; color: var(--color-gold); margin-right: 5px; }

.body { display: flex; gap: 50px; margin-top: 52px; align-items: flex-start; }
.article { flex: 1; min-width: 0; }
.tabs { display: flex; gap: 30px; border-bottom: 1px solid var(--color-line-strong); }
.tabs a { padding-bottom: 13px; font-size: 12.5px; letter-spacing: .12em; color: var(--color-ink-muted); cursor: pointer; position: relative; }
.tabs a.on { color: var(--color-ink); }
.tabs a.on::after { content: ""; position: absolute; left: 0; right: 0; bottom: -1px; height: 1px; background: var(--color-gold); }
.tabs em { font-style: normal; font-size: 9px; color: var(--color-ink-faint); margin-left: 5px; }

.tab-body { padding-top: 26px; }
.tab-body p { font-size: 13px; line-height: 2.15; color: var(--color-ink-soft); letter-spacing: .03em; margin-bottom: 16px; text-align: justify; }
.tab-body .muted { color: var(--color-ink-faint); font-size: 12px; }

.tags { display: flex; gap: 8px; margin-top: 20px; flex-wrap: wrap; }
.tags span { font-size: 10px; color: var(--color-ink-muted); border: 1px solid var(--color-line-strong); padding: 3px 8px; }

.catalog .cat-item { font-size: 12.5px; line-height: 2.4; color: var(--color-ink-soft); border-bottom: 1px dashed var(--color-line); }
.cat-item .idx { font-family: var(--font-mono); font-size: 10px; color: var(--color-ink-faint); margin-right: 14px; }

.keyinfo { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0 40px; }
.keyinfo div { display: flex; gap: 14px; padding: 11px 0; border-bottom: 1px solid var(--color-line); font-size: 12px; }
.keyinfo dt { width: 70px; color: var(--color-ink-faint); flex: none; }
.keyinfo dd { color: var(--color-ink-soft); margin: 0; }

.review-form { background: var(--color-surface); border: 1px solid var(--color-line); padding: 18px 20px; }
.rf-top { display: flex; align-items: center; gap: 16px; font-size: 11.5px; color: var(--color-ink-muted); }
.star-pick { display: flex; gap: 4px; }
.star-pick span { font-size: 17px; color: var(--color-line-strong); cursor: pointer; transition: color var(--dur-fast); }
.star-pick span.on { color: var(--color-gold); }
.review-form textarea {
  width: 100%; margin-top: 12px; background: var(--color-bg); border: 1px solid var(--color-line);
  color: var(--color-ink); font-family: inherit; font-size: 12.5px; line-height: 1.9; padding: 10px 12px; resize: vertical;
}
.review-form textarea:focus { outline: 0; border-color: var(--color-gold-line); }

.reviews { margin-top: 26px; }
.review { display: flex; gap: 14px; padding: 18px 0; border-bottom: 1px solid var(--color-line); }
.avatar {
  width: 34px; height: 34px; border-radius: 50%; border: 1px solid var(--color-line-strong);
  display: flex; align-items: center; justify-content: center; font-family: var(--font-display);
  font-size: 12px; color: var(--color-ink-muted); flex: none;
}
.rv-main { flex: 1; }
.rv-head { display: flex; align-items: center; gap: 12px; font-size: 11.5px; color: var(--color-ink); }
.rv-stars { color: var(--color-gold); font-size: 10px; letter-spacing: 1px; }
.rv-stars i { font-style: normal; color: var(--color-line-strong); }
.rv-time { font-size: 9.5px; color: var(--color-ink-faint); }
.rv-del { margin-left: auto; font-size: 9.5px; color: var(--color-ink-faint); cursor: pointer; }
.rv-del:hover { color: var(--color-danger); }
.rv-main p { margin-top: 8px; font-size: 12px; line-height: 2; color: var(--color-ink-soft); }

.side { width: 300px; flex: none; }
.side h4 { font-family: var(--font-display); font-size: 13px; font-weight: 400; letter-spacing: .18em; margin-bottom: 16px; }
.srow { display: flex; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--color-line); cursor: pointer; align-items: center; }
.srow:hover .t { color: var(--color-gold); }
.s-cover { width: 46px; height: 62px; flex: none; }
.sc .t { font-family: var(--font-display); font-size: 12.5px; letter-spacing: .05em; }
.sc .a { margin-top: 4px; font-size: 10px; color: var(--color-ink-muted); }
.sc .p { margin-top: 6px; font-size: 11.5px; color: var(--color-gold); }
.note { margin-top: 22px; padding: 16px; background: var(--color-surface); border: 1px solid var(--color-gold-line); font-size: 11px; line-height: 1.9; color: var(--color-ink-muted); }
.note b { display: block; color: var(--color-gold); font-weight: 400; letter-spacing: .14em; margin-bottom: 6px; }

.skeleton-wrap { display: flex; gap: 40px; padding: 60px 0; }
</style>
