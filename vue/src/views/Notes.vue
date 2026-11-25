<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><router-link to="/profile">个人中心</router-link><i>/</i><span>读书笔记</span></div>
      <div class="pg-head">
        <div>
          <h1>读书笔记</h1>
          <div class="sub">共 {{ notes.length }} 篇 · 笔记获赞可累积积分</div>
        </div>
      </div>

      <div class="nlist" v-if="notes.length">
        <article class="note" v-for="note in notes" :key="note.id">
          <div class="nh">
            <span class="book">《{{ note.bookName || '未知书籍' }}》</span>
            <span class="time">{{ fmtTime(note.createdAt) }}</span>
          </div>
          <h3 v-if="note.title">{{ note.title }}</h3>
          <p>{{ note.content }}</p>
          <div class="nf">
            <button class="like" @click="likeNote(note)">
              <svg viewBox="0 0 24 24"><path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1-1.1a5.5 5.5 0 0 0-7.8 7.8l8.8 8.8 8.8-8.8a5.5 5.5 0 0 0 0-7.8Z" stroke="currentColor" fill="none" stroke-width="1.5"/></svg>
              {{ note.likeCount || 0 }}
            </button>
          </div>
        </article>
      </div>
      <div class="blank" v-else>还没有读书笔记，去图书详情页写一条吧</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { noteAPI } from '../api'
import { ElMessage } from 'element-plus'

const notes = ref([])
const fmtTime = (t) => (t ? new Date(t).toLocaleDateString('zh-CN') : '')

async function load() {
  try {
    const res = await noteAPI.getMyNotes()
    notes.value = res.data || []
  } catch {}
}

async function likeNote(note) {
  try {
    await noteAPI.like(note.id)
    note.likeCount = (note.likeCount || 0) + 1
    ElMessage.success('已点赞')
  } catch {}
}

onMounted(load)
</script>

<style scoped>
.nlist { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.note { border: 1px solid var(--color-line); background: var(--color-surface); padding: 20px 22px; }
.nh { display: flex; align-items: center; justify-content: space-between; }
.nh .book { font-family: var(--font-display); font-size: 11.5px; color: var(--color-gold); letter-spacing: .06em; }
.nh .time { font-size: 9.5px; color: var(--color-ink-faint); }
.note h3 { margin-top: 12px; font-family: var(--font-display); font-size: 16px; font-weight: 400; letter-spacing: .08em; }
.note p { margin-top: 10px; font-size: 12px; line-height: 2; color: var(--color-ink-soft); letter-spacing: .03em; }
.nf { margin-top: 14px; padding-top: 12px; border-top: 1px solid var(--color-line); display: flex; justify-content: flex-end; }
.like { display: inline-flex; align-items: center; gap: 6px; background: none; border: 0; cursor: pointer; color: var(--color-ink-muted); font-size: 10.5px; }
.like svg { width: 13px; height: 13px; }
.like:hover { color: var(--color-gold); }

@media (max-width: 900px) { .nlist { grid-template-columns: 1fr; } }
</style>