<template>
  <button class="dark-toggle" @click="toggleDark" :title="isDark ? '切换到亮色模式' : '切换到暗色模式'">
    <span v-if="isDark" class="icon">☀️</span>
    <span v-else class="icon">🌙</span>
  </button>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'

const isDark = ref(false)

const toggleDark = () => {
  isDark.value = !isDark.value
  document.documentElement.classList.toggle('dark', isDark.value)
  localStorage.setItem('darkMode', isDark.value)
}

onMounted(() => {
  const saved = localStorage.getItem('darkMode')
  if (saved === 'true') {
    isDark.value = true
    document.documentElement.classList.add('dark')
  }
})
</script>

<style scoped>
.dark-toggle {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.dark-toggle:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(20deg);
}

.icon {
  font-size: 18px;
}
</style>
