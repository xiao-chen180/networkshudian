<template>
  <span class="status" :class="`status-${tone}`">{{ label }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, required: true }
})

const map = {
  PENDING: { label: '待付款', tone: 'warning' },
  PAID: { label: '待发货', tone: 'info' },
  SHIPPED: { label: '待收货', tone: 'brand' },
  COMPLETED: { label: '已完成', tone: 'success' },
  CANCELLED: { label: '已取消', tone: 'muted' }
}

const label = computed(() => map[props.status]?.label || props.status)
const tone = computed(() => map[props.status]?.tone || 'muted')
</script>

<style scoped>
.status {
  display: inline-flex;
  align-items: center;
  padding: 2px var(--space-2);
  font-size: var(--fs-xs);
  line-height: 1.6;
  border: 1px solid currentColor;
  border-radius: var(--radius-sm);
  white-space: nowrap;
}

.status-warning { color: var(--color-warning); }
.status-info { color: var(--color-info); }
.status-brand { color: var(--color-moss); }
.status-success { color: var(--color-success); }
.status-muted { color: var(--color-ink-faint); }
</style>