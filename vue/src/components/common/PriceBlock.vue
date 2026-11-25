<template>
  <p class="price-block" :class="[`size-${size}`]">
    <span class="currency">¥</span>
    <span class="price">{{ formattedPrice }}</span>
    <span v-if="hasOriginal" class="original">¥{{ format(originalPrice) }}</span>
    <span v-if="memberPrice != null" class="member">会员价 ¥{{ format(memberPrice) }}</span>
    <span v-if="tag" class="tag">{{ tag }}</span>
  </p>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  price: { type: [Number, String], required: true },
  originalPrice: { type: [Number, String], default: null },
  memberPrice: { type: [Number, String], default: null },
  tag: { type: String, default: '' },
  size: { type: String, default: 'md' }
})

const format = (value) => {
  const num = Number(value)
  return Number.isFinite(num) ? num.toFixed(2) : '0.00'
}

const formattedPrice = computed(() => format(props.price))
const hasOriginal = computed(() => props.originalPrice != null && Number(props.originalPrice) > Number(props.price))
</script>

<style scoped>
.price-block {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin: 0;
  font-family: var(--font-display);
  color: var(--color-cinnabar);
}

.currency {
  font-size: 0.65em;
  transform: translateY(-0.1em);
}

.price {
  font-size: 1.4em;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.size-sm .price { font-size: 1.1em; }
.size-lg .price { font-size: 2em; }

.original {
  font-family: var(--font-body);
  font-size: var(--fs-xs);
  color: var(--color-ink-faint);
  text-decoration: line-through;
}

.member {
  font-family: var(--font-body);
  font-size: var(--fs-xs);
  color: var(--color-moss);
}

.tag {
  padding: 1px var(--space-2);
  font-family: var(--font-body);
  font-size: var(--fs-xs);
  color: var(--color-cinnabar);
  border: 1px solid var(--color-cinnabar);
  border-radius: var(--radius-sm);
}
</style>