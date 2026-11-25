<template>
  <span class="status" :class="`status-${tone}`">
    <i class="dot"></i>
    {{ label }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, required: true }
})

const map = {
  PENDING: { label: '待付款', tone: 'gold' },
  PAID: { label: '待发货', tone: 'sage' },
  SHIPPED: { label: '待收货', tone: 'info' },
  COMPLETED: { label: '已完成', tone: 'muted' },
  CANCELLED: { label: '已取消', tone: 'danger' }
}

const label = computed(() => map[props.status]?.label || props.status)
const tone = computed(() => map[props.status]?.tone || 'muted')
</script>

<style scoped>
.status {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 10.5px; letter-spacing: .08em; white-space: nowrap;
}
.dot { width: 4px; height: 4px; border-radius: 50%; background: currentColor; }

.status-gold   { color: var(--color-gold); }
.status-sage   { color: var(--color-sage); }
.status-info   { color: var(--color-info); }
.status-muted  { color: var(--color-ink-faint); }
.status-danger { color: var(--color-danger); }
</style>