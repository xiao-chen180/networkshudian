<template>
  <article v-if="loading" class="book-card is-loading" aria-hidden="true">
    <SkeletonBlock class="cover-skeleton" ratio="3:4" />
    <div class="body">
      <SkeletonBlock width="70%" height="18px" />
      <SkeletonBlock width="45%" height="14px" />
      <SkeletonBlock width="35%" height="16px" />
    </div>
  </article>

  <article v-else class="book-card" :class="[`variant-${variant}`]">
    <router-link class="cover-link" :to="detailTo" :aria-label="cardLabel">
      <BookCover :src="book.imageUrl" :title="book.name" :author="book.author" :eager="eager" />
      <span v-if="rank" class="rank">{{ rank }}</span>
      <span v-if="soldOut" class="sold-out">缺货</span>
    </router-link>

    <div class="body">
      <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
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

const detailTo = computed(() => `/books/${props.book.id}`)
const soldOut = computed(() => Number(props.book.stock) <= 0)
const cardLabel = computed(() => `《${props.book.name}》，${props.book.author || '未知作者'}，¥${props.book.price}`)
</script>

<style scoped>
.book-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  min-width: 0;
}

.cover-link {
  position: relative;
  display: block;
  border-radius: var(--radius-0);
  overflow: hidden;
}

.cover-link :deep(.book-cover) {
  transition: transform var(--dur-normal) var(--ease-standard), box-shadow var(--dur-normal) var(--ease-standard);
}

.book-card:hover .cover-link :deep(.book-cover) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.book-card:hover .cover-link :deep(img) {
  transform: scale(1.02);
}

.rank {
  position: absolute;
  left: 0;
  top: 0;
  min-width: 28px;
  padding: var(--space-1) var(--space-2);
  font-family: var(--font-display);
  font-size: var(--fs-sm);
  color: var(--color-on-brand);
  background: var(--color-moss);
}

.sold-out {
  position: absolute;
  inset: auto 0 0 0;
  padding: var(--space-1);
  text-align: center;
  font-size: var(--fs-xs);
  color: var(--color-on-brand);
  background: rgba(28, 27, 25, 0.72);
}

.body {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
}

.eyebrow {
  margin: 0;
  font-size: var(--fs-xs);
  letter-spacing: var(--tracking-eyebrow);
  color: var(--color-ink-muted);
}

.title {
  font-family: var(--font-display);
  font-size: var(--fs-h3);
  line-height: 1.35;
  color: var(--color-ink);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.title:hover {
  color: var(--color-cinnabar);
}

.author {
  margin: 0;
  font-size: var(--fs-sm);
  color: var(--color-ink-muted);
}

.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
  margin-top: var(--space-1);
  flex-wrap: wrap;
}

.add-btn {
  margin-top: var(--space-2);
  padding: var(--space-2) var(--space-4);
  font-size: var(--fs-sm);
  color: var(--color-moss);
  background: transparent;
  border: 1px solid var(--color-moss);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--dur-fast) var(--ease-standard), color var(--dur-fast) var(--ease-standard);
}

.add-btn:hover:not(:disabled) {
  color: var(--color-on-brand);
  background: var(--color-moss);
}

.add-btn:disabled {
  color: var(--color-ink-faint);
  border-color: var(--color-rule);
  cursor: not-allowed;
}

/* 列表形态 */
.variant-list {
  flex-direction: row;
  align-items: flex-start;
  gap: var(--space-4);
}

.variant-list .cover-link {
  flex: 0 0 96px;
}

.variant-list .body {
  flex: 1;
}

/* 骨架 */
.cover-skeleton {
  width: 100%;
}

.cover-skeleton :deep(.skeleton) {
  aspect-ratio: 3 / 4;
  height: auto;
}

.is-loading .body {
  gap: var(--space-2);
  padding-top: var(--space-2);
}

@media (max-width: 767px) {
  .title {
    font-size: 17px;
  }
}
</style>