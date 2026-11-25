<template>
  <header class="page-header">
    <nav v-if="$slots.breadcrumb" class="breadcrumb" aria-label="面包屑">
      <slot name="breadcrumb" />
    </nav>
    <div class="row">
      <div class="text">
        <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
        <h1 class="title">{{ title }}</h1>
        <p v-if="description" class="description">{{ description }}</p>
      </div>
      <div v-if="$slots.actions" class="actions">
        <slot name="actions" />
      </div>
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
.page-header {
  padding: var(--space-8) 0 var(--space-6);
}
.breadcrumb {
  margin-bottom: var(--space-3); font-size: var(--fs-sm); color: var(--color-ink-faint); letter-spacing: .08em;
}
.breadcrumb :deep(a) { color: var(--color-ink-muted); }
.breadcrumb :deep(i) { font-style: normal; margin: 0 8px; opacity: .5; }
.row {
  display: flex; align-items: flex-end; justify-content: space-between; gap: var(--space-6);
}
.eyebrow {
  font-size: var(--fs-xs); letter-spacing: var(--tracking-eyebrow);
  text-transform: uppercase; color: var(--color-ink-muted);
}
.title {
  font-family: var(--font-display); font-size: var(--fs-h1); line-height: var(--lh-h1);
  font-weight: 400; letter-spacing: .12em;
}
.description {
  max-width: 40em; margin: var(--space-2) 0 0; font-size: var(--fs-sm);
  color: var(--color-ink-muted); letter-spacing: .06em;
}
.actions { display: flex; gap: var(--space-3); flex: 0 0 auto; }

@media (max-width: 767px) {
  .page-header { padding: var(--space-6) 0 var(--space-4); }
  .row { align-items: flex-start; flex-direction: column; }
}
</style>