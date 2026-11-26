<template>
  <div class="assistant">
    <!-- 浮动按钮 -->
    <button class="fab" :class="{ on: open }" @click="toggle" aria-label="AI 找书助手">
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M12 3v3M12 18v3M4.2 7.5l2.6 1.5M17.2 15l2.6 1.5M4.2 16.5l2.6-1.5M17.2 9l2.6-1.5" />
        <circle cx="12" cy="12" r="3.4" />
      </svg>
      <span v-if="!open">找书</span>
    </button>

    <transition name="asst">
      <section v-if="open" class="panel">
        <header class="p-head">
          <div>
            <div class="k">AI BOOK FINDER</div>
            <h3>不知道读什么？跟我说</h3>
          </div>
          <button class="close" @click="open = false" aria-label="关闭">×</button>
        </header>

        <div class="body" ref="bodyRef">
          <!-- 欢迎语 + 推荐问法 -->
          <div class="msg bot" v-if="!history.length">
            <p>描述你想找的感觉就行，比如「想找一本能安静下来的散文」。</p>
          </div>

          <template v-for="(h, i) in history" :key="i">
            <div class="msg user"><p>{{ h.query }}</p></div>
            <div class="msg bot">
              <p>{{ h.reply }}</p>
              <div class="hits" v-if="h.results && h.results.length">
                <button class="hit" v-for="b in h.results" :key="b.id" @click="goBook(b.id)">
                  <BookCover :src="b.imageUrl" :title="b.name" compact ratio="3:4" class="h-cover" />
                  <div class="h-meta">
                    <b>{{ b.name }}</b>
                    <span class="h-author">{{ b.author }}</span>
                    <span class="h-reason">{{ b.reason }}</span>
                  </div>
                  <span class="h-price">¥{{ Number(b.price).toFixed(2) }}</span>
                </button>
              </div>
            </div>
          </template>

          <div class="msg bot thinking" v-if="loading">
            <p>正在翻书架…</p>
          </div>
        </div>

        <div class="chips" v-if="!history.length">
          <button v-for="c in chips" :key="c" @click="ask(c)">{{ c }}</button>
        </div>

        <form class="input-bar" @submit.prevent="ask(input)">
          <input v-model="input" placeholder="例如：想看点烧脑的科幻小说" maxlength="60" />
          <button type="submit" :disabled="loading || !input.trim()">发送</button>
        </form>
        <div class="foot">基于本店书库的本地检索 · 结果仅供参考</div>
      </section>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { aiAPI } from '../api'
import BookCover from './common/BookCover.vue'

const router = useRouter()
const open = ref(false)
const input = ref('')
const loading = ref(false)
const history = ref([])
const bodyRef = ref(null)

const chips = ['想找一本能安静下来的书', '烧脑的科幻', '编程入门', '讲历史的书', '想哭一场']

function toggle() {
  open.value = !open.value
  if (open.value) scrollToEnd()
}

async function scrollToEnd() {
  await nextTick()
  if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
}

async function ask(text) {
  const q = (text || '').trim()
  if (!q || loading.value) return
  input.value = ''
  loading.value = true
  await scrollToEnd()
  try {
    const res = await aiAPI.search({ query: q, limit: 4 })
    const data = res.data || {}
    history.value.push({
      query: q,
      reply: data.reply || '我找到几本，看看是否合胃口。',
      results: data.results || []
    })
  } catch {
    history.value.push({ query: q, reply: '书架暂时翻不动了，稍后再试。', results: [] })
  } finally {
    loading.value = false
    scrollToEnd()
  }
}

function goBook(id) {
  open.value = false
  router.push(`/books/${id}`)
}
</script>

<style scoped>
.assistant { position: fixed; right: 26px; bottom: 26px; z-index: var(--z-dropdown); }

.fab {
  display: flex; align-items: center; gap: 8px; height: 46px; padding: 0 18px;
  border: 1px solid var(--color-gold-line); border-radius: 23px;
  background: var(--color-gold); color: #14150f; cursor: pointer;
  font-family: inherit; font-size: 11.5px; letter-spacing: .14em;
  box-shadow: 0 10px 30px rgba(0,0,0,.45);
  transition: transform var(--dur-fast), opacity var(--dur-fast);
}
.fab:hover { transform: translateY(-2px); }
.fab.on { padding: 0; width: 46px; justify-content: center; border-radius: 50%; }
.fab svg { width: 18px; height: 18px; stroke: currentColor; fill: none; stroke-width: 1.5; }

.panel {
  position: absolute; right: 0; bottom: 60px; width: 384px; max-height: 74vh;
  display: flex; flex-direction: column;
  background: var(--color-surface); border: 1px solid var(--color-line-strong);
  box-shadow: var(--shadow-popover);
}
.p-head {
  display: flex; align-items: flex-start; justify-content: space-between;
  padding: 16px 18px 14px; border-bottom: 1px solid var(--color-line);
  background: linear-gradient(120deg, rgba(211,174,106,.10), transparent 70%);
}
.p-head .k { font-size: 8.5px; letter-spacing: .3em; color: var(--color-gold); }
.p-head h3 { margin-top: 8px; font-family: var(--font-display); font-size: 15px; font-weight: 400; letter-spacing: .08em; }
.close { background: none; border: 0; color: var(--color-ink-faint); font-size: 20px; line-height: 1; cursor: pointer; }
.close:hover { color: var(--color-ink); }

.body { flex: 1; overflow-y: auto; padding: 16px 18px; display: flex; flex-direction: column; gap: 12px; scrollbar-width: thin; }
.msg p { font-size: 11.5px; line-height: 1.9; letter-spacing: .02em; }
.msg.user { align-self: flex-end; max-width: 86%; }
.msg.user p {
  background: var(--color-surface-3); color: var(--color-ink); padding: 8px 12px;
  border: 1px solid var(--color-line);
}
.msg.bot p { color: var(--color-ink-muted); }
.msg.thinking p { color: var(--color-ink-faint); font-style: normal; }

.hits { margin-top: 10px; display: flex; flex-direction: column; gap: 8px; }
.hit {
  display: flex; align-items: center; gap: 10px; padding: 8px 10px; text-align: left;
  border: 1px solid var(--color-line); background: var(--color-bg); cursor: pointer; font-family: inherit;
  transition: border-color var(--dur-fast);
}
.hit:hover { border-color: var(--color-gold-line); }
.h-cover { width: 34px; height: 47px; flex: none; }
.h-meta { flex: 1; min-width: 0; }
.h-meta b { display: block; font-family: var(--font-display); font-size: 12px; font-weight: 400; letter-spacing: .04em; color: var(--color-ink); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.h-author { display: block; margin-top: 3px; font-size: 9px; color: var(--color-ink-faint); }
.h-reason { display: block; margin-top: 4px; font-size: 9px; color: var(--color-gold); letter-spacing: .03em; }
.h-price { font-size: 11px; color: var(--color-gold); flex: none; font-variant-numeric: tabular-nums; }

.chips { display: flex; flex-wrap: wrap; gap: 6px; padding: 0 18px 10px; }
.chips button {
  border: 1px solid var(--color-line); background: transparent; color: var(--color-ink-muted);
  font-family: inherit; font-size: 10px; padding: 5px 9px; cursor: pointer; letter-spacing: .03em;
}
.chips button:hover { border-color: var(--color-gold-line); color: var(--color-gold); }

.input-bar { display: flex; gap: 8px; padding: 12px 18px; border-top: 1px solid var(--color-line); }
.input-bar input {
  flex: 1; height: 34px; padding: 0 10px; background: var(--color-surface-2);
  border: 1px solid var(--color-line); color: var(--color-ink); font-family: inherit; font-size: 11.5px;
}
.input-bar input:focus { outline: none; border-color: var(--color-gold-line); }
.input-bar button {
  height: 34px; padding: 0 16px; border: 1px solid var(--color-gold); background: var(--color-gold);
  color: #14150f; font-family: inherit; font-size: 11px; letter-spacing: .1em; cursor: pointer;
}
.input-bar button:disabled { background: var(--color-surface-3); border-color: var(--color-line-strong); color: var(--color-ink-faint); cursor: not-allowed; }
.foot { padding: 0 18px 12px; font-size: 8.5px; color: var(--color-ink-faint); letter-spacing: .04em; }

.asst-enter-active, .asst-leave-active { transition: opacity .18s ease, transform .18s ease; }
.asst-enter-from, .asst-leave-to { opacity: 0; transform: translateY(10px); }

@media (max-width: 767px) {
  .panel { width: calc(100vw - 32px); right: 0; }
  .assistant { right: 16px; bottom: 16px; }
}
</style>