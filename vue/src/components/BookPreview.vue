<template>
  <div class="preview-modal" v-if="visible" @click.self="close">
    <div class="preview-content">
      <!-- 头部 -->
      <div class="preview-header">
        <button class="close-btn" @click="close">×</button>
        <span class="header-title">{{ bookName }}</span>
        <span class="header-badge">试读</span>
      </div>

      <!-- 正文内容 -->
      <div class="preview-body">
        <div class="book-page">
          <p v-for="(para, index) in currentPageContent" :key="index" class="paragraph">
            {{ para }}
          </p>
        </div>
      </div>

      <!-- 底部 -->
      <div class="preview-footer">
        <!-- 试读结束提示 - 只在最后一页显示 -->
        <div class="end-hint" v-if="currentPage >= totalPages">
          <p class="end-text">— 试读结束 —</p>
          <p class="end-sub">购买后可阅读完整内容</p>
          <button class="buy-btn" @click="goBuy">立即购买 ¥{{ formatPrice(bookPrice) }}</button>
        </div>

        <!-- 页码导航 -->
        <div class="page-nav">
          <button class="nav-btn" @click="prevPage" :disabled="currentPage <= 1">
            <span>‹</span>
          </button>
          <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
          <button class="nav-btn" @click="nextPage" :disabled="currentPage >= totalPages">
            <span>›</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  visible: Boolean,
  bookId: Number,
  bookName: String,
  bookPrice: { type: Number, default: 0 },
  summary: String,
  catalog: String
})

const emit = defineEmits(['close'])
const router = useRouter()
const currentPage = ref(1)
const itemsPerPage = 4

// 将摘要内容分割成段落
const contentParagraphs = computed(() => {
  const paragraphs = []

  if (props.summary) {
    // 按句号、换行分割
    const sentences = props.summary.split(/[。\n]/).filter(s => s.trim())
    sentences.forEach(sentence => {
      const trimmed = sentence.trim()
      if (trimmed && trimmed.length > 3) {
        paragraphs.push(trimmed + '。')
      }
    })
  }

  // 如果内容不足，添加补充内容确保至少3页
  const minParagraphs = itemsPerPage * 3
  const extraSentences = [
    '这是一本值得细细品味的好书',
    '作者用细腻的笔触描绘了一个丰富多彩的世界',
    '书中的每一个人物都栩栩如生',
    '每一段故事都引人入胜，让人回味无穷',
    '阅读这本书，你会感受到文字的力量',
    '好的书籍就像好的朋友，值得用心感受',
    '每一个章节都充满了智慧和思考',
    '让我们一起走进这本书的世界',
    '感受其中的温情与力量',
    '相信这本书会给你带来不一样的阅读体验'
  ]

  let extraIndex = 0
  while (paragraphs.length < minParagraphs) {
    paragraphs.push(extraSentences[extraIndex % extraSentences.length] + '。')
    extraIndex++
  }

  return paragraphs
})

const totalPages = computed(() => {
  return Math.ceil(contentParagraphs.value.length / itemsPerPage)
})

const currentPageContent = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return contentParagraphs.value.slice(start, start + itemsPerPage)
})

const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++
}

const close = () => {
  currentPage.value = 1
  emit('close')
}

const goBuy = () => {
  close()
  router.push(`/books/${props.bookId}`)
}

const formatPrice = (price) => price ? Number(price).toFixed(2) : '0.00'

watch(() => props.visible, (val) => {
  if (val) currentPage.value = 1
})
</script>

<style scoped>
.preview-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 16px;
  animation: fadeIn 0.25s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.preview-content {
  background: #f8f4ee;
  border-radius: 12px;
  width: 100%;
  max-width: 460px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.35);
}

/* 头部 */
.preview-header {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: white;
  gap: 12px;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  color: white;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.header-title {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-badge {
  background: rgba(255, 255, 255, 0.25);
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
  flex-shrink: 0;
}

/* 正文内容 */
.preview-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px 20px;
}

.book-page {
  background: white;
  border-radius: 8px;
  padding: 28px 24px;
  min-height: 280px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.paragraph {
  font-size: 16px;
  line-height: 2;
  color: #333;
  margin-bottom: 14px;
  text-indent: 2em;
  font-family: var(--font-serif);
}

.paragraph:last-child {
  margin-bottom: 0;
}

/* 底部 */
.preview-footer {
  background: white;
  border-top: 1px solid #e8e0d5;
}

.end-hint {
  text-align: center;
  padding: 20px 20px 16px;
  border-bottom: 1px solid #f0ebe3;
}

.end-text {
  font-size: 15px;
  color: #999;
  margin: 0 0 6px;
}

.end-sub {
  font-size: 13px;
  color: #bbb;
  margin: 0 0 14px;
}

.buy-btn {
  display: inline-block;
  padding: 10px 36px;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: white;
  border: none;
  border-radius: 22px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(139, 90, 43, 0.3);
}

.buy-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(139, 90, 43, 0.4);
}

.page-nav {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 32px;
  padding: 14px 20px;
}

.nav-btn {
  width: 40px;
  height: 40px;
  background: #f5f0e8;
  border: none;
  border-radius: 50%;
  font-size: 22px;
  color: var(--primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.nav-btn:hover:not(:disabled) {
  background: var(--primary);
  color: white;
}

.nav-btn:disabled {
  opacity: 0.25;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #888;
}

/* 滚动条 */
.preview-body::-webkit-scrollbar {
  width: 4px;
}

.preview-body::-webkit-scrollbar-thumb {
  background: #d4c4a8;
  border-radius: 2px;
}
</style>
