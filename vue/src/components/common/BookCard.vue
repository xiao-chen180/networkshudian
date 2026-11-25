<template>
  <article v-if="loading" class="book-card is-loading" aria-hidden="true">
    <SkeletonBlock class="cover-skeleton" ratio="3:4" />
    <div class="body">
      <SkeletonBlock width="70%" height="16px" />
      <SkeletonBlock width="45%" height="12px" />
      <SkeletonBlock width="35%" height="14px" />
    </div>
  </article>

  <article v-else class="book-card" :class="[`variant-${variant}`]">
    <router-link class="cover-link" :to="detailTo" :aria-label="cardLabel">
      <BookCover :src="book.imageUrl" :title="book.name" :author="book.author" :eager="eager" />
      <span v-if="rank" class="rank">{{ rank }}</span>
      <span v-if="soldOut" class="sold-out">缺货</span>
    </router-link>

    <div class="body">
      <div class="meta-top">
        <span v-if="eyebrow" class="eyebrow">{{ eyebrow }}</span>
      </div>
      <router-link class="title" :to="detailTo">{{ book.name }}</router-link>
      <p class="author">{{ book.author }}</p>

      <div class="meta">
        <PriceBlock :price="book.price" :member-price="memberPrice" size="sm" />
        <RatingStars
          v-if="book.avgRating != null && Number(book.avgRating) > 0"
          :value="book.avgRating"
          :count="book.reviewCount"
          size="sm"
        />
      </div>

      <button
        v-if="showAction"
        type="button"
        class="add-btn"
        :disabled="soldOut"
        :aria-label="`将《${book.name}》加入购物袋`"
        @click="$emit('add', book)"
      >
        <svg viewBox="0 0 24 24" width="14" height="14"><path d="M12 5v14M5 12h14" stroke="currentColor" fill="none" stroke-width="1.6"/></svg>
        {{ soldOut ? '暂时缺货' : '加入购物袋' }}
      </button>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import BookCover from './BookCover.vue'
import PriceBlock from './PriceBlock.vue'
import RatingStars from './RatingStars.vue'
import SkeletonBlock from './SkeletonBlock.vue'

const props = defineProps({
  book: { type: Object, required: true },
  variant: { type: String, default: 'grid' },
  rank: { type: [Number, String], default: null },
  eyebrow: { type: String, default: '' },
  memberPrice: { type: [Number, String], default: null },
  showAction: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  eager: { type: Boolean, default: false }
})

defineEmits(['add'])

const formatRating = (v) => Number(v).toFixed(1)
const detailTo = computed(() => `/books/${props.book.id}`)
const soldOut = computed(() => Number(props.book.stock) <= 0)
const cardLabel = computed(() => `《${props.book.name}》，${props.book.author || '未知作者'}，¥${props.book.price}`)
</script>

<style scoped>
.book-card {
  display: flex; flex-direction: column; gap: var(--space-3); min-width: 0;
  background: var(--color-surface); border: 1px solid transparent;
  padding: 12px; transition: border-color var(--dur-normal) var(--ease-standard), background var(--dur-normal);
}
.book-card:hover { border-color: var(--color-gold-line); background: var(--color-surface-2); }

.cover-link {
  position: relative; display: block; overflow: hidden;
}

.rank {
  position: absolute; left: 0; top: 0; min-width: 24px;
  padding: 2px 8px; font-family: var(--font-display); font-size: 12px;
  color: #14150f; background: var(--color-gold);
}
.sold-out {
  position: absolute; inset: auto 0 0 0; padding: 4px; text-align: center;
  font-size: 10px; color: var(--color-ink); background: rgba(0,0,0,.7);
}

.body { display: flex; flex-direction: column; gap: 4px; min-width: 0; padding: 14px 0 0; }

.meta-top { display: flex; justify-content: space-between; align-items: center; }
.eyebrow { font-size: 9px; letter-spacing: .2em; color: var(--color-ink-faint); }
.rating-badge { font-size: 9px; letter-spacing: .12em; color: var(--color-ink-muted); }

.title {
  margin-top: 8px; font-family: var(--font-display); font-size: 16px; line-height: 1.55; color: var(--color-ink);
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  font-weight: 400; letter-spacing: .06em;
}
.title:hover { color: var(--color-gold); }
.author { font-size: 11px; color: var(--color-ink-muted); letter-spacing: .06em; }

.meta { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-top: 8px; }

.add-btn {
  margin-top: 12px; padding: 8px 0; font-size: 12px; letter-spacing: .08em;
  color: var(--color-gold); background: transparent;
  border: 1px solid var(--color-gold-line); cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  transition: background var(--dur-fast);
}
.add-btn:hover:not(:disabled) { background: var(--color-gold-soft); }
.add-btn:disabled { color: var(--color-ink-faint); border-color: var(--color-line); cursor: not-allowed; }

.cover-skeleton :deep(.skeleton) { aspect-ratio: 3/4; height: auto; }
.is-loading .body { gap: 8px; padding-top: 8px; }

@media (max-width: 767px) {
  .title { font-size: 14px; }
}
</style>