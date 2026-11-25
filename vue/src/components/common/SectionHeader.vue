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
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-6);
  padding-bottom: var(--space-4);
  margin-bottom: var(--space-7);
  border-bottom: 1px solid var(--color-rule);
}

.eyebrow {
  margin: 0 0 var(--space-1);
  font-size: var(--fs-xs);
  letter-spacing: var(--tracking-eyebrow);
  text-transform: uppercase;
  color: var(--color-ink-muted);
}

.title {
  font-size: var(--fs-h2);
  line-height: var(--lh-h2);
}

.description {
  max-width: 40em;
  margin: var(--space-2) 0 0;
  font-size: var(--fs-sm);
  color: var(--color-ink-muted);
}

.actions {
  flex: 0 0 auto;
}

@media (max-width: 767px) {
  .section-header {
    align-items: flex-start;
    flex-direction: column;
    gap: var(--space-3);
  }
}
</style>