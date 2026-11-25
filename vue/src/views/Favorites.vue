<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><router-link to="/profile">个人中心</router-link><i>/</i><span>我的收藏</span></div>
      <div class="pg-head">
        <div>
          <h1>我的收藏</h1>
          <div class="sub">共 {{ favorites.length }} 本 · 收藏的书有降价时会收到提醒</div>
        </div>
        <router-link to="/books" class="btn-line sm">继续逛书店</router-link>
      </div>

      <div class="grid" v-if="favorites.length">
        <div class="fav" v-for="fav in favorites" :key="fav.id">
          <router-link :to="`/books/${fav.bookId}`" class="cover-link">
            <BookCover :title="fav.bookName" :author="fav.bookAuthor" src="" />
          </router-link>
          <div class="body">
            <router-link :to="`/books/${fav.bookId}`" class="t">{{ fav.bookName }}</router-link>
            <div class="a">{{ fav.bookAuthor }}</div>
            <div class="b">
              <span class="p">¥{{ fmt(fav.bookPrice) }}</span>
              <button class="rm" @click="remove(fav.bookId)">取消收藏</button>
            </div>
          </div>
        </div>
      </div>

      <EmptyState
        v-else
        icon="◌"
        title="收藏夹是空的"
        description="看到喜欢的书，点一下收藏，下次好找"
        action-text="去逛书店"
        :action-to="'/books'"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { favoriteAPI } from '../api'
import { ElMessage } from 'element-plus'
import BookCover from '../components/common/BookCover.vue'
import EmptyState from '../components/common/EmptyState.vue'

const favorites = ref([])
const fmt = (p) => (typeof p === 'string' ? p : Number(p || 0).toFixed(2))

async function load() {
  try {
    const res = await favoriteAPI.getAll()
    favorites.value = res.data || []
  } catch {}
}

async function remove(bookId) {
  try {
    await favoriteAPI.remove(bookId)
    favorites.value = favorites.value.filter(f => f.bookId !== bookId)
    ElMessage.success('已取消收藏')
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(load)
</script>

<style scoped>
.grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; }
.fav { border: 1px solid var(--color-line); background: var(--color-surface); padding: 12px; transition: border-color var(--dur-fast); }
.fav:hover { border-color: var(--color-gold-line); }
.cover-link { display: block; }
.body { padding: 14px 2px 2px; }
.t { display: block; font-family: var(--font-display); font-size: 15px; letter-spacing: .05em; color: var(--color-ink); }
.t:hover { color: var(--color-gold); }
.a { margin-top: 5px; font-size: 10.5px; color: var(--color-ink-muted); letter-spacing: .05em; }
.b { display: flex; align-items: center; justify-content: space-between; margin-top: 12px; padding-top: 11px; border-top: 1px solid var(--color-line); }
.p { font-size: 14px; color: var(--color-gold); }
.rm { background: none; border: 0; font-family: inherit; font-size: 10px; letter-spacing: .08em; color: var(--color-ink-faint); cursor: pointer; }
.rm:hover { color: var(--color-danger); }

@media (max-width: 1024px) { .grid { grid-template-columns: repeat(2, 1fr); } }
</style>