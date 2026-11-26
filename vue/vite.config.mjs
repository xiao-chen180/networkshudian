import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    // Element Plus 全量引入，单 chunk 体积较大但可长期缓存；调高告警阈值避免噪音
    chunkSizeWarningLimit: 1200,
    rolldownOptions: {
      output: {
        advancedChunks: {
          groups: [
            { name: 'element-plus', test: /node_modules[\\/]element-plus/ },
            { name: 'vue-vendor', test: /node_modules[\\/](vue|@vue|vue-router)[\\/]/ },
            { name: 'vendor', test: /node_modules[\\/]/ }
          ]
        }
      }
    }
  }
})