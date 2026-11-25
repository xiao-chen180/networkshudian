<template>
  <div class="notes-page">
    <div class="hero-section">
      <h1 class="page-title">📝 读书笔记</h1>
    </div>
    <div class="notes-container">
      <div v-if="notes.length > 0" class="notes-list">
        <div v-for="note in notes" :key="note.id" class="note-card">
          <div class="note-header">
            <span class="note-book">📖 {{ note.bookName || '未知书籍' }}</span>
            <span class="note-time">{{ formatTime(note.createdAt) }}</span>
          </div>
          <h3 class="note-title">{{ note.title }}</h3>
          <p class="note-content">{{ note.content }}</p>
          <div class="note-footer">
            <button class="like-btn" @click="likeNote(note)">❤️ {{ note.likeCount || 0 }}</button>
          </div>
        </div>
      </div>
      <div v-else class="empty">
        <div class="empty-icon">📝</div>
        <p>暂无读书笔记</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { noteAPI } from '../api'
import { ElMessage } from 'element-plus'

const notes = ref([])

const load = async () => {
  try {
    const res = await noteAPI.getMyNotes()
    notes.value = res.data
  } catch (e) {}
}

const likeNote = async (note) => {
  try {
    await noteAPI.like(note.id)
    note.likeCount = (note.likeCount || 0) + 1
    ElMessage.success('已点赞')
  } catch (e) {}
}

const formatTime = (t) => t ? new Date(t).toLocaleDateString('zh-CN') : ''

onMounted(() => load())
</script>

<style scoped>
.notes-page { min-height: 100vh; background: var(--bg-page); }
.hero-section { background: linear-gradient(135deg, var(--primary), var(--primary-light)); padding: 20px; text-align: center; }
.page-title { color: white; font-family: var(--font-serif); font-size: 22px; margin: 0; }
.notes-container { max-width: 800px; margin: 0 auto; padding: 16px; }
.notes-list { display: flex; flex-direction: column; gap: 12px; }
.note-card { background: white; border-radius: 12px; padding: 16px; }
.note-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.note-book { font-size: 13px; color: var(--primary); }
.note-time { font-size: 12px; color: var(--text-secondary); }
.note-title { font-size: 16px; margin: 0 0 8px; }
.note-content { font-size: 14px; color: var(--text-secondary); line-height: 1.6; margin: 0 0 12px; }
.note-footer { border-top: 1px solid #f5f5f5; padding-top: 8px; }
.like-btn { background: none; border: none; font-size: 13px; color: var(--text-secondary); cursor: pointer; }
.empty { text-align: center; padding: 80px 0; color: var(--text-secondary); }
.empty-icon { font-size: 48px; margin-bottom: 16px; }
</style>
