<template>
  <div class="home">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-copy">
          <div class="kicker">阅读与生活 · 秋季选书</div>
          <h1>灯还亮着，<br>书就还<em>读得下去</em></h1>
          <p>为深夜读书的人挑书。我们只上架读过、愿意再读一遍的书，附编辑短评、版本信息与真实书评。</p>
          <div class="cta">
            <button class="btn btn-fill" @click="goBooks">进入书店
              <svg viewBox="0 0 24 24"><path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" fill="none" stroke-width="1.4"/></svg>
            </button>
            <router-link to="/rankings" class="link-line">查看本周榜单 →</router-link>
          </div>
        </div>
        <div class="hero-visual">
          <div class="hero-cover">
            <BookCover title="夜航西飞" author="柏瑞尔·马卡姆" />
          </div>
        </div>
      </div>
    </section>

    <!-- 运营 Banner（有数据才渲染） -->
    <section class="banner-sec" v-if="banners.length">
      <div class="wrap">
        <div class="banner" v-for="b in banners" :key="b.id" @click="b.bookId && goBookDetail(b.bookId)">
          <div class="b-left">
            <span class="b-k">EDITOR'S NOTE</span>
            <b>{{ b.title }}</b>
            <p v-if="b.subtitle">{{ b.subtitle }}</p>
            <span class="b-cta" v-if="b.bookId">查看这本书 →</span>
          </div>
          <div class="b-right" v-if="b.imageUrl">
            <img :src="b.imageUrl" :alt="b.title" />
          </div>
        </div>
      </div>
    </section>

    <!-- 分类 -->
    <section class="cats">
      <div class="wrap">
        <div class="cat" v-for="c in categories" :key="c.id" @click="goCategory(c.id)">
          <b>{{ c.name }}</b>
          <span>{{ c.description || '精选书目' }}</span>
        </div>
      </div>
    </section>

    <!-- 本周新书 -->
    <section class="sec" v-if="hotBooks.length">
      <div class="wrap">
        <div class="sec-head">
          <h2>本周新书</h2>
          <span class="en">NEW ARRIVALS</span>
          <span class="line"></span>
          <router-link to="/books" class="more">全部 →</router-link>
        </div>
        <div class="grid4">
          <BookCard v-for="book in hotBooks" :key="book.id" :book="book"
            :show-action="isLoggedIn" @add="addToCart" />
        </div>
      </div>
    </section>

    <!-- 销量排行 -->
    <section class="sec" v-if="salesRanking.length">
      <div class="wrap">
        <div class="sec-head">
          <h2>深夜榜单</h2>
          <span class="en">TOP 5</span>
          <span class="line"></span>
          <router-link to="/rankings" class="more">完整榜单 →</router-link>
        </div>
        <div class="rows">
          <div class="row" v-for="(book, i) in salesRanking" :key="book.id"
            :class="{ 'is-top': i < 3 }" @click="goBookDetail(book.id)">
            <span class="no">{{ pad(i + 1) }}</span>
            <BookCover :src="book.imageUrl" :title="book.name" :author="book.author" ratio="3:4" class="row-cover" compact />
            <div class="info">
              <b>{{ book.name }}</b>
              <span>{{ book.author }} · {{ book.categoryName || '综合' }}</span>
            </div>
            <div class="p">
              <b>¥{{ formatPrice(book.price) }}</b>
              <span>库存 {{ book.stock }} 件</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 猜你喜欢 -->
    <section class="sec" v-if="recommendBooks.length">
      <div class="wrap">
        <div class="sec-head">
          <h2>猜你喜欢</h2>
          <span class="en">FOR YOU</span>
          <span class="line"></span>
        </div>
        <div class="grid4">
          <BookCard v-for="book in recommendBooks" :key="book.id" :book="book"
            :show-action="isLoggedIn" @add="addToCart" />
        </div>
      </div>
    </section>

    <!-- 名言 -->
    <section class="quote-sec">
      <div class="wrap">
        <div class="quote-block" v-if="currentQuote">
          <span class="q">「</span>
          <p>{{ currentQuote.content }}</p>
          <cite v-if="currentQuote.authorName">—— {{ currentQuote.authorName }}</cite>
        </div>
        <div class="quote-block" v-else>
          <span class="q">「</span>
          <p>书中自有黄金屋，书中自有颜如玉</p>
          <cite>—— 《劝学诗》</cite>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { categoryAPI, bookAPI, quoteAPI, rankingAPI, recommendAPI, cartAPI, bannerAPI } from '../api'
import { ElMessage } from 'element-plus'
import BookCover from '../components/common/BookCover.vue'
import BookCard from '../components/common/BookCard.vue'

const router = useRouter()
const categories = ref([])
const hotBooks = ref([])
const banners = ref([])
const currentQuote = ref(null)
const salesRanking = ref([])
const recommendBooks = ref([])

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const pad = (n) => String(n).padStart(2, '0')
const formatPrice = (p) => (typeof p === 'string' ? p : Number(p).toFixed(2))

const goBooks = () => router.push('/books')
const goCategory = (id) => router.push(`/books?category=${id}`)
const goBookDetail = (id) => router.push(`/books/${id}`)

const addToCart = async (book) => {
  try {
    await cartAPI.addToCart({ bookId: book.id, quantity: 1 })
    ElMessage.success('已加入购物袋')
  } catch (e) {
    ElMessage.error('加入失败')
  }
}

onMounted(async () => {
  try {
    const [catRes, bookRes] = await Promise.all([
      categoryAPI.getAll(),
      bookAPI.getAll({ page: 0, size: 8 })
    ])
    categories.value = catRes.data.slice(0, 6)
    hotBooks.value = bookRes.data?.content?.slice(0, 4) || []
  } catch (e) { /* silent */ }
  quoteAPI.getRandom().then(r => { if (r.data) currentQuote.value = r.data }).catch(() => {})
  bannerAPI.getActive().then(r => { banners.value = (r.data || []).slice(0, 1) }).catch(() => {})
  rankingAPI.getSales().then(r => { salesRanking.value = (r.data || []).slice(0, 5) }).catch(() => {})
  recommendAPI.guessYouLike().then(r => { recommendBooks.value = (r.data || []).slice(0, 4) }).catch(() => {})
})
</script>

<style scoped>
.home { min-height: 100vh; }

/* ---- Hero ---- */
.hero { padding: 80px 0; position: relative; overflow: hidden; }
.hero::before {
  content: ""; position: absolute; left: 50%; top: -280px; width: 1000px; height: 700px;
  transform: translateX(-35%);
  background: radial-gradient(ellipse, rgba(211,174,106,.09), transparent 60%);
}
.hero-inner { max-width: var(--container); margin: 0 auto; padding: 0 var(--gutter); display: flex; gap: 60px; align-items: center; position: relative; }

.hero-copy { width: 540px; }
.kicker { display: flex; align-items: center; gap: 12px; font-size: 10px; letter-spacing: .28em; color: var(--color-gold); }
.kicker::after { content: ""; flex: 1; height: 1px; background: linear-gradient(90deg, rgba(211,174,106,.5), transparent); }
h1 { font-family: var(--font-display); font-size: var(--fs-display); font-weight: 400; line-height: 1.35; margin-top: 24px; letter-spacing: .04em; }
h1 em { font-style: normal; color: var(--color-gold); }
.hero-copy p { margin-top: 22px; font-size: 14px; line-height: 2; color: var(--color-ink-soft); letter-spacing: .04em; width: 440px; }

.cta { display: flex; align-items: center; gap: 28px; margin-top: 32px; }
.btn {
  height: 46px; padding: 0 24px; font-family: inherit; font-size: 13px; letter-spacing: .12em;
  display: inline-flex; align-items: center; gap: 10px; cursor: pointer;
  border: 1px solid var(--color-gold); background: transparent; color: var(--color-gold);
}
.btn svg { width: 14px; height: 14px; }
.btn-fill { background: var(--color-gold); color: #14150f; font-weight: 500; }
.link-line { font-size: 12px; letter-spacing: .1em; color: var(--color-ink-soft); border-bottom: 1px solid var(--color-line-strong); padding-bottom: 3px; }

.hero-visual { width: 480px; flex: none; position: relative; }
.hero-cover { width: 280px; margin-left: 100px; position: relative; }
.hero-cover::before {
  content: ""; position: absolute; left: 16px; top: 14px; width: 100%; height: 100%;
  border: 1px solid rgba(211,174,106,.42); transform: rotate(-1.5deg); pointer-events: none;
}

/* ---- 运营 Banner ---- */
.banner-sec { padding: 36px 0 0; }
.banner {
  display: flex; align-items: center; gap: 30px; border: 1px solid var(--color-gold-line);
  background:
    radial-gradient(120% 140% at 0% 0%, rgba(211,174,106,.12), transparent 62%),
    var(--color-surface);
  padding: 26px 30px; cursor: pointer;
}
.banner:hover { background: rgba(211,174,106,.06); }
.b-left { flex: 1; }
.b-k { font-size: 8.5px; letter-spacing: .3em; color: var(--color-gold); }
.b-left b { display: block; margin-top: 12px; font-family: var(--font-display); font-size: 24px; font-weight: 400; letter-spacing: .1em; }
.b-left p { margin-top: 10px; font-size: 11.5px; color: var(--color-ink-muted); line-height: 1.9; letter-spacing: .04em; }
.b-cta { display: inline-block; margin-top: 16px; font-size: 10.5px; letter-spacing: .12em; color: var(--color-gold); border-bottom: 1px solid var(--color-gold-line); padding-bottom: 3px; }
.b-right { width: 200px; flex: none; }
.b-right img { width: 100%; height: 120px; object-fit: cover; border: 1px solid var(--color-line); }

/* ---- 分类 ---- */
.cats { border-top: 1px solid var(--color-line); border-bottom: 1px solid var(--color-line); background: var(--color-surface); margin-top: 36px; }
.cats .wrap { display: grid; grid-template-columns: repeat(6, 1fr); max-width: var(--container); margin: 0 auto; padding: 0 var(--gutter); }
.cat { padding: 24px 0 24px 22px; border-left: 1px solid var(--color-line); cursor: pointer; transition: color var(--dur-fast); }
.cat:first-child { border-left: 0; padding-left: 0; }
.cat:hover b { color: var(--color-gold); }
.cat b { display: block; font-family: var(--font-display); font-size: 16px; font-weight: 400; letter-spacing: .12em; }
.cat span { display: block; margin-top: 8px; font-size: 10px; letter-spacing: .2em; color: var(--color-ink-faint); }

/* ---- 区块 ---- */
.sec { padding: 72px 0; }
.sec-head { display: flex; align-items: center; gap: 18px; margin-bottom: 30px; }
.sec-head h2 { font-family: var(--font-display); font-size: 26px; font-weight: 400; letter-spacing: .14em; flex: none; }
.sec-head .en { font-size: 9.5px; letter-spacing: .28em; color: var(--color-ink-faint); flex: none; padding-top: 6px; }
.sec-head .line { flex: 1; height: 1px; background: var(--color-line); min-width: 30px; }
.sec-head .more { font-size: 11px; letter-spacing: .14em; color: var(--color-ink-soft); flex: none; }

.wrap { max-width: var(--container); margin: 0 auto; padding: 0 var(--gutter); }
.grid4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 22px; }

/* ---- 榜单 ---- */
.rows { border-top: 1px solid var(--color-line-strong); }
.row {
  display: grid; grid-template-columns: 46px 44px 1fr auto; gap: 18px; align-items: center;
  padding: 14px 0; border-bottom: 1px solid var(--color-line); cursor: pointer;
  transition: background var(--dur-fast);
}
.row:hover { background: var(--color-surface); }
.row .no { font-family: var(--font-display); font-size: 22px; color: var(--color-ink-faint); letter-spacing: -.02em; }
.row.is-top .no { color: var(--color-gold); }
.row-cover { width: 44px; height: 60px; }
.row .info b { display: block; font-family: var(--font-display); font-size: 15px; font-weight: 400; letter-spacing: .08em; }
.row .info span { display: block; margin-top: 5px; font-size: 10px; color: var(--color-ink-muted); letter-spacing: .06em; }
.row .p { text-align: right; }
.row .p b { font-size: 14px; color: var(--color-gold); font-weight: 400; }
.row .p span { display: block; margin-top: 3px; font-size: 9px; color: var(--color-ink-faint); letter-spacing: .06em; }

/* ---- 名言 ---- */
.quote-sec { padding: 70px 0; border-top: 1px solid var(--color-line); }
.quote-block { text-align: center; max-width: 640px; margin: 0 auto; }
.q {
  font-family: var(--font-display); font-size: 48px; color: var(--color-gold); opacity: .55;
  line-height: 1; display: block;
}
.quote-block p {
  margin-top: 10px; font-family: var(--font-display); font-size: 17px; line-height: 2;
  color: var(--color-ink-soft); letter-spacing: .06em;
}
cite { display: block; margin-top: 18px; font-size: 11px; color: var(--color-ink-muted); letter-spacing: .14em; font-style: normal; }

@media (max-width: 767px) {
  .hero-inner { flex-direction: column; }
  .hero-copy { width: 100%; }
  .hero-visual { width: 100%; }
  .cats .wrap { grid-template-columns: repeat(2, 1fr); }
  .grid4 { grid-template-columns: repeat(2, 1fr); }
  .row { grid-template-columns: 36px 36px 1fr auto; gap: 10px; }
}
</style>
