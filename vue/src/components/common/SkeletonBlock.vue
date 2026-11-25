<template>
  <div class="skeleton" :class="{ 'is-circle': circle }" :style="styles" aria-hidden="true"/>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  width: { type: [String, Number], default: '100%' },
  height: { type: [String, Number], default: '14px' },
  circle: { type: Boolean, default: false },
  radius: { type: String, default: '' }
})

const px = (value) => (typeof value === 'number' ? `${value}px` : value)
const styles = computed(() => ({
  width: px(props.width), height: px(props.height),
  borderRadius: props.circle ? '50%' : props.radius || 'var(--radius-sm)'
}))
</script>

<style scoped>
.skeleton {
  display: block;
  background: linear-gradient(90deg,
    var(--color-surface-2) 25%,
    var(--color-surface-3) 37%,
    var(--color-surface-2) 63%);
  background-size: 400% 100%;
  animation: shimmer 1.4s ease infinite;
}

@keyframes shimmer {
  0% { background-position: 100% 50%; }
  100% { background-position: 0 50%; }
}

@media (prefers-reduced-motion: reduce) { .skeleton { animation: none; } }
</style>