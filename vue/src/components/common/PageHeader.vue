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
  padding: var(--space-9) 0 var(--space-7);
}

.breadcrumb {
  margin-bottom: var(--space-4);
  font-size: var(--fs-sm);
  color: var(--color-ink-muted);
}

.row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-6);
}

.eyebrow {
  margin: 0 0 var(--space-2);
  font-size: var(--fs-xs);
  letter-spacing: var(--tracking-eyebrow);
  text-transform: uppercase;
  color: var(--color-ink-muted);
}

.title {
  font-size: var(--fs-h1);
  line-height: var(--lh-h1);
}

.description {
  max-width: 40em;
  margin: var(--space-3) 0 0;
  font-size: var(--fs-sm);
  color: var(--color-ink-muted);
}

.actions {
  display: flex;
  gap: var(--space-3);
  flex: 0 0 auto;
}

@media (max-width: 767px) {
  .page-header {
    padding: var(--space-7) 0 var(--space-5);
  }

  .row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>