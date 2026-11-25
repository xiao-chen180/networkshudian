<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><span>搜索</span></div>
      <div class="pg-head">
        <div>
          <h1>搜索</h1>
          <div class="sub">支持书名、作者与 ISBN 模糊匹配</div>
        </div>
      </div>

      <div class="searchbar">
        <svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="6.5"/><path d="m19 19-3.2-3.2"/></svg>
        <input v-model="keyword" placeholder="输入书名、作者或 ISBN" @keyup.enter="doSearch" autofocus />
        <button v-if="keyword" class="clear" @click="clear">×</button>
      </div>

      <template v-if="hasSearched">
        <div class="result-line">
          <span>「{{ lastKeyword }}」找到 {{ results.length }} 种图书</span>
          <router-link to="/books">浏览全部图书 →</router-link>
        </div>

        <div class="grid" v-if="results.length">
          <BookCard v-for="b in results" :key="b.id" :book="b" :show-action="isLoggedIn" @add="addToCart" />
        </div>
        <div class="blank" v-else>没有找到相关图书，换个关键词试试</div>
      </template>
      <div class="hint" v-else>
        <span>热门搜索</span>
        <a v-for="w in hot" :key="w" @click="quick(w)">{{ w }}</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { bookAPI, cartAPI } from '../api'
import { ElMessage } from 'element-plus'
import BookCard from '../components/common/BookCard.vue'

const keyword = ref('')
const lastKeyword = ref('')
const results = ref([])
const hasSearched = ref(false)
const hot = ['三体', '红楼梦', '活着', '百年孤独', '平凡的世界']
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) return
  hasSearched.value = true
  lastKeyword.value = kw
  try {
    const res = await bookAPI.getAll({ keyword: kw, size: 50 })
    results.value = res.data?.content || []
  } catch {
    results.value = []
  }
}

function quick(w) {
  keyword.value = w
  doSearch()
}

function clear() {
  keyword.value = ''
  hasSearched.value = false
  results.value = []
}

async function addToCart(book) {
  try {
    await cartAPI.addToCart({ bookId: book.id, quantity: 1 })
    ElMessage.success('已加入购物袋')
  } catch {
    ElMessage.error('加入失败，请先登录')
  }
}
</script>

<style scoped>
.searchbar {
  height: 48px; border: 1px solid var(--color-line); background: var(--color-surface);
  display: flex; align-items: center; gap: 12px; padding: 0 16px;
}
.searchbar svg { width: 16px; height: 16px; stroke: var(--color-ink-muted); fill: none; stroke-width: 1.6; flex: none; }
.searchbar input {
  flex: 1; border: 0; background: transparent; outline: 0; color: var(--color-ink);
  font-family: inherit; font-size: 13.5px; letter-spacing: .04em;
}
.searchbar input::placeholder { color: var(--color-ink-faint); }
.clear { border: 0; background: none; color: var(--color-ink-faint); font-size: 16px; cursor: pointer; }

.result-line { display: flex; align-items: center; justify-content: space-between; margin: 22px 0 18px; font-size: 11px; color: var(--color-ink-muted); letter-spacing: .06em; }
.result-line a { color: var(--color-gold); }

.grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; }

.hint { margin-top: 26px; display: flex; align-items: center; gap: 14px; flex-wrap: wrap; font-size: 11px; color: var(--color-ink-faint); }
.hint a { color: var(--color-ink-soft); border-bottom: 1px solid var(--color-line-strong); padding-bottom: 2px; cursor: pointer; }
.hint a:hover { color: var(--color-gold); }

@media (max-width: 1024px) { .grid { grid-template-columns: repeat(2, 1fr); } }
</style>