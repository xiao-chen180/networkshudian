<template>
  <teleport to="body">
    <transition name="fly">
      <div v-if="visible" class="fly-item" :style="style">
        <img :src="image" alt="" />
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  visible: Boolean,
  image: String,
  startX: Number,
  startY: Number,
  endX: Number,
  endY: Number
})

const style = ref({})

watch(() => props.visible, (val) => {
  if (val) {
    style.value = {
      left: props.startX + 'px',
      top: props.startY + 'px',
      opacity: 1,
      transform: 'scale(1)'
    }
    setTimeout(() => {
      style.value = {
        left: props.endX + 'px',
        top: props.endY + 'px',
        opacity: 0,
        transform: 'scale(0.2)'
      }
    }, 50)
  }
})
</script>

<style scoped>
.fly-item {
  position: fixed;
  width: 60px;
  height: 80px;
  z-index: 10000;
  pointer-events: none;
  transition: all 0.6s cubic-bezier(0.2, 0.8, 0.2, 1);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.fly-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
