<template>
  <div class="book-cover" :class="[`ratio-${ratio}`]">
    <img
      v-if="showImage"
      class="cover-image"
      :src="src"
      :alt="alt || `《${title}》封面`"
      :loading="eager ? 'eager' : 'lazy'"
      decoding="async"
      @error="failed = true"
    />
    <div v-else class="cover-fallback" aria-hidden="true">
      <span class="fallback-mark">{{ mark }}</span>
      <span class="fallback-title">{{ title }}</span>
      <span v-if="author" class="fallback-author">{{ author }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  src: { type: String, default: '' },
  title: { type: String, default: '' },
  author: { type: String, default: '' },
  alt: { type: String, default: '' },
  ratio: { type: String, default: '3:4' },
  eager: { type: Boolean, default: false }
})

const failed = ref(false)
watch(() => props.src, () => { failed.value = false })

const showImage = computed(() => Boolean(props.src) && !failed.value)
const mark = computed(() => (props.title || '书').trim().slice(0, 1))
</script>

<style scoped>
.book-cover {
  position: relative;
  width: 100%;
  overflow: hidden;
  background: var(--color-surface-muted);
  border: 1px solid var(--color-rule);
  border-radius: var(--radius-0);
}

.ratio-3\:4 { aspect-ratio: 3 / 4; }
.ratio-4\:3 { aspect-ratio: 4 / 3; }
.ratio-1\:1 { aspect-ratio: 1 / 1; }
.ratio-16\:9 { aspect-ratio: 16 / 9; }

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--dur-normal) var(--ease-standard);
}

.cover-fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  width: 100%;
  height: 100%;
  padding: var(--space-4);
  text-align: center;
  background:
    linear-gradient(135deg, rgba(176, 138, 74, 0.10), transparent 55%),
    repeating-linear-gradient(0deg, transparent 0 23px, rgba(28, 27, 25, 0.04) 23px 24px),
    var(--color-surface-muted);
}

.fallback-mark {
  font-family: var(--font-display);
  font-size: 44px;
  line-height: 1;
  color: var(--color-moss);
  opacity: 0.55;
}

.fallback-title {
  font-family: var(--font-display);
  font-size: var(--fs-sm);
  color: var(--color-ink-soft);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.fallback-author {
  font-size: var(--fs-xs);
  color: var(--color-ink-muted);
}
</style>