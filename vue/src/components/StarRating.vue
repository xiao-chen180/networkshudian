<template>
  <div class="star-rating" :class="{ readonly: readonly }">
    <span
      v-for="star in 5"
      :key="star"
      class="star"
      :class="{ active: star <= currentValue, hover: star <= hoverValue }"
      @click="!readonly && setValue(star)"
      @mouseenter="!readonly && (hoverValue = star)"
      @mouseleave="hoverValue = 0"
    >★</span>
    <span v-if="showValue && currentValue > 0" class="rating-value">{{ currentValue.toFixed(1) }}</span>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Number, default: 0 },
  readonly: { type: Boolean, default: false },
  showValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

const currentValue = ref(props.modelValue)
const hoverValue = ref(0)

watch(() => props.modelValue, (val) => {
  currentValue.value = val
})

const setValue = (star) => {
  currentValue.value = star
  emit('update:modelValue', star)
}
</script>

<style scoped>
.star-rating {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.star {
  font-size: 20px;
  color: #d4c4a8;
  cursor: pointer;
  transition: all 0.2s ease;
}

.star.active, .star.hover {
  color: #f59e0b;
  transform: scale(1.1);
}

.star-rating.readonly .star {
  cursor: default;
}

.rating-value {
  margin-left: 8px;
  font-size: 14px;
  color: #8b5a2b;
  font-weight: 600;
}
</style>
