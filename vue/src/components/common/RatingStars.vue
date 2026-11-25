<template>
  <span class="rating" :class="[`size-${size}`]" role="img" :aria-label="label">
    <span class="stars" aria-hidden="true">
      <span class="stars-empty">★★★★★</span>
      <span class="stars-filled" :style="{ width: percent }">★★★★★</span>
    </span>
    <span v-if="showValue" class="value">{{ numeric.toFixed(1) }}</span>
    <span v-if="count != null" class="count">（{{ count }}）</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: { type: [Number, String], default: 0 },
  count: { type: [Number, String], default: null },
  size: { type: String, default: 'md' },
  showValue: { type: Boolean, default: false }
})

const numeric = computed(() => {
  const n = Number(props.value)
  return Number.isFinite(n) ? Math.min(5, Math.max(0, n)) : 0
})
const percent = computed(() => `${(numeric.value / 5) * 100}%`)
const label = computed(() => `评分 ${numeric.value.toFixed(1)} / 5`)
</script>

<style scoped>
.rating { display: inline-flex; align-items: center; gap: 4px; color: var(--color-gold); line-height: 1; white-space: nowrap; }

.stars { position: relative; display: inline-block; font-size: 13px; letter-spacing: 2px; }
.size-sm .stars { font-size: 11px; }
.size-lg .stars { font-size: 17px; }

.stars-empty { color: var(--color-line-strong); }
.stars-filled { position: absolute; inset: 0; overflow: hidden; color: var(--color-gold); }

.value { font-size: 10px; color: var(--color-ink-muted); letter-spacing: .04em; }
.count { font-size: 10px; color: var(--color-ink-faint); }
</style>