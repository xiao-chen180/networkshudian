<template>
  <header class="section-header">
    <div class="text">
      <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
      <h2 class="title">{{ title }}</h2>
      <p v-if="description" class="description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup>
defineProps({
  eyebrow: { type: String, default: '' },
  title: { type: String, required: true },
  description: { type: String, default: '' }
})
</script>

<style scoped>
.section-header {
  display: flex; align-items: center; gap: 20px; margin-bottom: 30px;
}
.text { flex: 1; }
.eyebrow {
  font-size: var(--fs-xs); letter-spacing: var(--tracking-eyebrow);
  text-transform: uppercase; color: var(--color-gold);
}
.title {
  font-family: var(--font-display); font-size: 26px; line-height: var(--lh-h2);
  font-weight: 400; letter-spacing: .14em;
}
.description {
  max-width: 40em; margin: 4px 0 0; font-size: var(--fs-sm); color: var(--color-ink-muted);
}
.actions { flex: 0 0 auto; }

.section-header .line {
  flex: 1; height: 1px; background: var(--color-line); min-width: 40px;
}

@media (max-width: 767px) {
  .section-header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .section-header .line { display: none; }
}
</style>