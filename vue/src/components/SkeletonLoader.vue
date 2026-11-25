<template>
  <div :class="['skeleton', `skeleton-${type}`]">
    <template v-if="type === 'book-card'">
      <div class="skeleton-image"></div>
      <div class="skeleton-text skeleton-title"></div>
      <div class="skeleton-text skeleton-subtitle"></div>
      <div class="skeleton-text skeleton-price"></div>
    </template>
    <template v-else-if="type === 'list-item'">
      <div class="skeleton-avatar"></div>
      <div class="skeleton-content">
        <div class="skeleton-text skeleton-title"></div>
        <div class="skeleton-text skeleton-subtitle"></div>
      </div>
    </template>
    <template v-else-if="type === 'detail'">
      <div class="skeleton-cover"></div>
      <div class="skeleton-info">
        <div class="skeleton-text skeleton-title"></div>
        <div class="skeleton-text skeleton-subtitle"></div>
        <div class="skeleton-text skeleton-price"></div>
        <div class="skeleton-text skeleton-desc"></div>
      </div>
    </template>
    <template v-else>
      <div class="skeleton-text skeleton-title"></div>
      <div class="skeleton-text skeleton-subtitle"></div>
      <div class="skeleton-text skeleton-desc"></div>
    </template>
  </div>
</template>

<script setup>
defineProps({
  type: { type: String, default: 'text' } // book-card, list-item, detail, text
})
</script>

<style scoped>
.skeleton {
  padding: 16px;
}

.skeleton-book-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.skeleton-image {
  width: 100%;
  aspect-ratio: 3/4;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-cover {
  width: 120px;
  height: 160px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 8px;
}

.skeleton-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 50%;
}

.skeleton-text {
  height: 14px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-top: 10px;
}

.skeleton-title { width: 70%; }
.skeleton-subtitle { width: 50%; }
.skeleton-price { width: 30%; }
.skeleton-desc { width: 90%; }

.skeleton-detail {
  display: flex;
  gap: 20px;
  background: white;
  border-radius: 12px;
}

.skeleton-info {
  flex: 1;
}

.skeleton-list-item {
  display: flex;
  gap: 12px;
  align-items: center;
  background: white;
  border-radius: 8px;
}

.skeleton-content {
  flex: 1;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
