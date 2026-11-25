<template>
  <div class="book-cover" :class="[`ratio-${ratio}`, { compact }]">
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
      <span class="imprint">书香阁</span>
      <span class="ghost">{{ mark }}</span>
      <span class="hair"></span>
      <span class="fb-title">{{ title }}</span>
      <span v-if="author" class="fb-author">{{ author }}</span>
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
  eager: { type: Boolean, default: false },
  /* 小尺寸场景（列表行、订单行）只显示首字，避免文字溢出 */
  compact: { type: Boolean, default: false }
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
  background: var(--color-surface-2);
  border: 1px solid var(--color-line);
  container-type: inline-size;
}
.ratio-3\:4 { aspect-ratio: 3 / 4; }
.ratio-4\:3 { aspect-ratio: 4 / 3; }
.ratio-1\:1 { aspect-ratio: 1 / 1; }

.cover-image { width: 100%; height: 100%; object-fit: cover; transition: transform var(--dur-normal) var(--ease-standard); }

.cover-fallback {
  position: relative; width: 100%; height: 100%;
  display: flex; flex-direction: column; justify-content: flex-end;
  padding: 9% 9% 8%;
  background:
    radial-gradient(120% 80% at 85% 0%, rgba(255,255,255,.07), transparent 58%),
    linear-gradient(180deg, rgba(0,0,0,0) 42%, rgba(0,0,0,.42));
  isolation: isolate;
}
.cover-fallback::after {
  content: ""; position: absolute; inset: 6px; border: 1px solid rgba(211,174,106,.26); pointer-events: none;
}

.imprint {
  position: absolute; left: 9%; top: 6.5%;
  font-size: clamp(6.5px, 2.4cqw, 10px); letter-spacing: .14em; color: rgba(211,174,106,.55);
  white-space: nowrap;
}
.ghost {
  position: absolute; left: 50%; top: 40%; transform: translate(-50%, -50%);
  font-family: var(--font-display); font-size: 34cqw; line-height: 1;
  color: rgba(211,174,106,.13); pointer-events: none; z-index: -1;
}
.hair { width: 22%; height: 1px; background: rgba(211,174,106,.55); margin-bottom: 7%; }

.fb-title {
  font-family: var(--font-display); font-weight: 400; color: var(--color-ink);
  font-size: clamp(10px, 7.2cqw, 26px); line-height: 1.4; letter-spacing: .06em;
  display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;
}
.fb-author {
  margin-top: 6%;
  font-size: clamp(6px, 3cqw, 11px); letter-spacing: .16em; color: rgba(243,239,231,.62);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

/* 小尺寸：只留首字 */
.compact .cover-fallback { padding: 0; align-items: center; justify-content: center; }
.compact .cover-fallback::after { inset: 3px; }
.compact .imprint, .compact .hair, .compact .fb-title, .compact .fb-author { display: none; }
.compact .ghost {
  position: static; transform: none; z-index: 0;
  font-size: 42cqw; color: rgba(211,174,106,.5);
}
</style>