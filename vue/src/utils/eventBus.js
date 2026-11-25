import { createApp } from 'vue'

const app = createApp({})
export const eventBus = app.config.globalProperties.$bus = {
  events: {},
  
  on(event, callback) {
    if (!this.events[event]) {
      this.events[event] = []
    }
    this.events[event].push(callback)
  },
  
  emit(event, ...args) {
    if (this.events[event]) {
      this.events[event].forEach(callback => callback(...args))
    }
  },
  
  off(event, callback) {
    if (this.events[event]) {
      this.events[event] = this.events[event].filter(cb => cb !== callback)
    }
  }
}

export default eventBus
