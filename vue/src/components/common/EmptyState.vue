<template>
  <div class="empty-state" :class="{ compact }" role="status">
    <span v-if="icon" class="icon" aria-hidden="true">{{ icon }}</span>
    <h3 class="title">{{ title }}</h3>
    <p v-if="description" class="description">{{ description }}</p>
    <slot>
      <router-link v-if="actionText && actionTo" :to="actionTo" class="action">
        {{ actionText }}
      </router-link>
      <button v-else-if="actionText" type="button" class="action" @click="$emit('action')">
        {{ actionText }}
      </button>
    </slot>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '◌' },
  title: { type: String, default: '这里还什么都没有' },
  description: { type: String, default: '' },
  actionText: { type: String, default: '' },
  actionTo: { type: [String, Object], default: '' },
  compact: { type: Boolean, default: false }
})

defineEmits(['action'])
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-11) var(--space-6);
  text-align: center;
  border: 1px dashed var(--color-rule);
  background: var(--color-surface);
}

.empty-state.compact {
  padding: var(--space-7) var(--space-4);
  border: 0;
  background: transparent;
}

.icon {
  font-size: 32px;
  line-height: 1;
  color: var(--color-rule-strong);
}

.title {
  font-size: var(--fs-h3);
  color: var(--color-ink-soft);
}

.description {
  max-width: 32em;
  margin: 0;
  font-size: var(--fs-sm);
  color: var(--color-ink-muted);
}

.action {
  margin-top: var(--space-2);
  padding: var(--space-2) var(--space-5);
  font-size: var(--fs-sm);
  color: var(--color-on-brand);
  background: var(--color-moss);
  border: 1px solid var(--color-moss);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.action:hover {
  background: var(--color-moss-soft);
  border-color: var(--color-moss-soft);
  color: var(--color-on-brand);
}
</style>