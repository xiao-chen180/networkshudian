<template>
  <div class="cart-page">
    <div class="wrap">
      <div class="pagehead">
        <div class="crumb"><router-link to="/">首页</router-link><i>/</i><span>购物袋</span></div>
        <h1>购物袋</h1>
        <div class="sub" v-if="cartItems.length">{{ cartItems.length }} 件商品 · 已选 {{ selectedItems.length }} 件</div>
      </div>

      <template v-if="cartItems.length">
        <div class="sel-bar">
          <label class="check">
            <input type="checkbox" v-model="selectAll" @change="toggleSelectAll"/>
            <span class="box"></span>
            <span>全选</span>
          </label>
        </div>

        <div class="items">
          <div class="item" v-for="item in cartItems" :key="item.id">
            <label class="check">
              <input type="checkbox" v-model="item.selected" @change="updateTotal"/>
              <span class="box"></span>
            </label>
            <router-link :to="`/books/${item.bookId}`" class="cover-link">
              <BookCover :src="item.imageUrl" compact :title="item.bookName" ratio="3:4" class="item-cover"/>
            </router-link>
            <div class="meta">
              <router-link :to="`/books/${item.bookId}`" class="name">{{ item.bookName }}</router-link>
              <span class="author">{{ item.author }}</span>
            </div>
            <div class="unit">¥{{ formatPrice(item.price) }}</div>
            <div class="qty">
              <button @click="changeQty(item, -1)" :disabled="item.quantity <= 1" aria-label="减少">−</button>
              <span>{{ item.quantity }}</span>
              <button @click="changeQty(item, 1)" aria-label="增加">+</button>
            </div>
            <div class="sum">¥{{ formatPrice(item.price * item.quantity) }}</div>
            <button class="rm" @click="removeItem(item.id)" aria-label="删除">×</button>
          </div>
        </div>

        <div class="bar">
          <div class="total">
            <span>合计</span>
            <b>¥{{ totalPrice.toFixed(2) }}</b>
            <span class="hint" v-if="selectedItems.length && totalPrice < 99">还差 ¥{{ (99 - totalPrice).toFixed(2) }} 免运费</span>
            <span class="hint free" v-else-if="selectedItems.length">已满 99 元，免运费</span>
          </div>
          <button class="checkout-btn" :disabled="!selectedItems.length" @click="goCheckout">
            结算（{{ selectedItems.length }}）
          </button>
        </div>
      </template>

      <EmptyState v-else icon="◌" title="购物袋是空的" description="去逛逛，挑几本喜欢的书" action-text="去逛书店" :action-to="'/books'" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { cartAPI } from '../api'
import { ElMessage } from 'element-plus'
import { useCartCount } from '../utils/cartCount'
import BookCover from '../components/common/BookCover.vue'
import EmptyState from '../components/common/EmptyState.vue'

const { setCartCount } = useCartCount()
const router = useRouter()
const cartItems = ref([])
const selectAll = ref(false)

const selectedItems = computed(() => cartItems.value.filter(i => i.selected))
const totalPrice = computed(() => selectedItems.value.reduce((s, i) => s + Number(i.price) * i.quantity, 0))

async function loadCart() {
  try {
    const res = await cartAPI.getCart()
    cartItems.value = (res.data || []).map(item => ({ ...item, selected: true }))
    selectAll.value = cartItems.value.length > 0
  } catch {}
}
const toggleSelectAll = () => cartItems.value.forEach(item => { item.selected = selectAll.value })
const updateTotal = () => { selectAll.value = cartItems.value.length > 0 && cartItems.value.every(i => i.selected) }

async function changeQty(item, delta) {
  const n = item.quantity + delta
  if (n < 1) return
  try {
    await cartAPI.updateCartItem(item.id, { quantity: n })
    item.quantity = n
  } catch {
    ElMessage.error('更新失败')
  }
}

async function removeItem(id) {
  try {
    await cartAPI.removeFromCart(id)
    cartItems.value = cartItems.value.filter(i => i.id !== id)
    setCartCount(cartItems.value.length)
    ElMessage.success('已删除')
  } catch {
    ElMessage.error('删除失败')
  }
}

function goCheckout() {
  if (!selectedItems.value.length) return
  router.push(`/checkout?ids=${selectedItems.value.map(i => i.id).join(',')}`)
}

const formatPrice = (p) => (typeof p === 'string' ? p : Number(p || 0).toFixed(2))

onMounted(loadCart)
</script>

<style scoped>
.cart-page { min-height: 100vh; padding-bottom: 60px; }
.wrap { max-width: var(--container); margin: 0 auto; padding: 0 var(--gutter); }

.pagehead { padding: 40px 0 22px; }
.crumb { font-size: 11px; color: var(--color-ink-faint); letter-spacing: .08em; display: flex; gap: 8px; }
.crumb a { color: var(--color-ink-muted); }
.crumb i { font-style: normal; opacity: .5; }
.pagehead h1 { margin-top: 12px; font-family: var(--font-display); font-size: var(--fs-h1); font-weight: 400; letter-spacing: .12em; }
.pagehead .sub { margin-top: 8px; font-size: 11.5px; color: var(--color-ink-muted); letter-spacing: .08em; }

/* 自定义勾选框 */
.check { display: inline-flex; align-items: center; gap: 8px; cursor: pointer; font-size: 11.5px; color: var(--color-ink-soft); user-select: none; }
.check input { position: absolute; opacity: 0; width: 0; height: 0; }
.check .box {
  width: 14px; height: 14px; border: 1px solid var(--color-line-strong); position: relative; flex: none;
  transition: background var(--dur-fast), border-color var(--dur-fast);
}
.check input:checked + .box { background: var(--color-gold); border-color: var(--color-gold); }
.check input:checked + .box::after {
  content: ""; position: absolute; left: 4px; top: 1px; width: 3px; height: 7px;
  border: 1px solid #14150f; border-top: 0; border-left: 0; transform: rotate(42deg);
}
.check input:focus-visible + .box { outline: 1px solid var(--color-gold); outline-offset: 2px; }

.sel-bar { padding: 12px 0; border-bottom: 1px solid var(--color-line); }

.item {
  display: grid; grid-template-columns: 20px 52px 1fr 100px 110px 90px 24px;
  gap: 16px; align-items: center; padding: 16px 0; border-bottom: 1px solid var(--color-line);
}
.cover-link { display: block; }
.item-cover { width: 52px; height: 72px; }
.meta { min-width: 0; }
.meta .name { display: block; font-family: var(--font-display); font-size: 14px; letter-spacing: .06em; color: var(--color-ink); }
.meta .name:hover { color: var(--color-gold); }
.meta .author { display: block; margin-top: 5px; font-size: 10px; color: var(--color-ink-faint); letter-spacing: .06em; }

.unit { font-size: 12px; color: var(--color-ink-muted); text-align: right; }
.sum { font-size: 13px; color: var(--color-gold); text-align: right; }

.qty { display: flex; align-items: center; justify-content: center; border: 1px solid var(--color-line); height: 30px; }
.qty button { width: 28px; height: 28px; border: 0; background: transparent; color: var(--color-ink-soft); cursor: pointer; font-size: 13px; }
.qty button:hover:not(:disabled) { color: var(--color-gold); }
.qty button:disabled { color: var(--color-ink-faint); cursor: not-allowed; }
.qty span { width: 34px; text-align: center; font-size: 12px; }

.rm { background: none; border: 0; font-size: 16px; color: var(--color-ink-faint); cursor: pointer; padding: 2px; }
.rm:hover { color: var(--color-danger); }

.bar { display: flex; align-items: center; justify-content: space-between; margin-top: 22px; padding: 18px 0; border-top: 1px solid var(--color-line-strong); }
.total { display: flex; align-items: baseline; gap: 12px; flex-wrap: wrap; }
.total > span:first-child { font-size: 11.5px; color: var(--color-ink-muted); letter-spacing: .08em; }
.total b { font-family: var(--font-display); font-size: 26px; color: var(--color-gold); font-weight: 400; }
.hint { font-size: 10px; color: var(--color-ink-faint); letter-spacing: .05em; }
.hint.free { color: var(--color-sage); }

.checkout-btn {
  height: 44px; padding: 0 32px; border: 1px solid var(--color-gold); background: var(--color-gold);
  color: #14150f; font-family: inherit; font-size: 12.5px; letter-spacing: .16em; cursor: pointer;
}
.checkout-btn:hover:not(:disabled) { opacity: .88; }
.checkout-btn:disabled { background: var(--color-surface-3); border-color: var(--color-line-strong); color: var(--color-ink-faint); cursor: not-allowed; }

@media (max-width: 900px) {
  .item { grid-template-columns: 20px 44px 1fr 24px; grid-template-areas: "c cov m rm" ". . q s"; row-gap: 10px; }
}
</style>