<template>
  <div class="cart-page">
    <div class="wrap">
      <div class="pagehead">
        <div class="crumb"><router-link to="/">首页</router-link><i>/</i><span>购物袋</span></div>
        <h1>购物袋</h1>
        <div class="sub" v-if="cartItems.length">{{ cartItems.length }} 件商品</div>
      </div>

      <template v-if="cartItems.length">
        <!-- 全选 -->
        <div class="sel-bar">
          <label class="check"><input type="checkbox" v-model="selectAll" @change="toggleSelectAll"/><span>全选</span></label>
        </div>

        <!-- 列表 -->
        <div class="items">
          <div class="item" v-for="item in cartItems" :key="item.id">
            <label class="check"><input type="checkbox" v-model="item.selected" @change="updateTotal"/></label>
            <BookCover :src="item.imageUrl" compact :title="item.bookName" class="item-cover" ratio="3:4"/>
            <div class="meta">
              <b>{{ item.bookName }}</b>
              <span>{{ item.author }}</span>
              <div class="row-bot">
                <span class="price">¥{{ formatPrice(item.price) }}</span>
                <div class="qty">
                  <button @click="changeQty(item, -1)" :disabled="item.quantity <= 1">−</button>
                  <span>{{ item.quantity }}</span>
                  <button @click="changeQty(item, 1)">+</button>
                </div>
              </div>
            </div>
            <button class="rm" @click="removeItem(item.id)" aria-label="删除">×</button>
          </div>
        </div>

        <!-- 底部结算 -->
        <div class="bar">
          <div class="total">
            <span>合计</span>
            <b>¥{{ totalPrice.toFixed(2) }}</b>
            <span class="hint" v-if="totalPrice < 99">还差 ¥{{ (99 - totalPrice).toFixed(2) }} 免运费</span>
            <span class="hint free" v-else>已满 99 元，免运费</span>
          </div>
          <button class="btn btn-fill" :disabled="!selectedItems.length" @click="goCheckout">
            结算 ({{ selectedItems.length }})
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
const totalPrice = computed(() => selectedItems.value.reduce((s, i) => s + i.price * i.quantity, 0))

const loadCart = async () => {
  try {
    const res = await cartAPI.getCart()
    cartItems.value = (res.data || []).map(item => ({ ...item, selected: false }))
  } catch {}
}
const toggleSelectAll = () => cartItems.value.forEach(item => item.selected = selectAll.value)
const updateTotal = () => { selectAll.value = cartItems.value.length > 0 && cartItems.value.every(i => i.selected) }

const changeQty = async (item, delta) => {
  const n = item.quantity + delta
  if (n < 1) return
  try { await cartAPI.updateCartItem(item.id, { quantity: n }); item.quantity = n } catch { ElMessage.error('更新失败') }
}
const removeItem = async (id) => {
  try { await cartAPI.removeFromCart(id); cartItems.value = cartItems.value.filter(i => i.id !== id); setCartCount(cartItems.value.length); ElMessage.success('已删除') } catch { ElMessage.error('删除失败') }
}
const goCheckout = () => {
  if (!selectedItems.value.length) return
  router.push(`/checkout?ids=${selectedItems.value.map(i => i.id).join(',')}`)
}
const formatPrice = (p) => (typeof p === 'string' ? p : Number(p).toFixed(2))

onMounted(loadCart)
</script>

<style scoped>
.cart-page { min-height: 100vh; padding-bottom: 80px; }
.wrap { max-width: var(--container); margin: 0 auto; padding: 0 var(--gutter); }

.pagehead { padding: 40px 0 24px; }
.crumb { font-size: 11px; color: var(--color-ink-faint); letter-spacing: .08em; display: flex; gap: 8px; }
.crumb a { color: var(--color-ink-muted); }
.crumb i { font-style: normal; opacity: .5; }
.pagehead h1 { font-family: var(--font-display); font-size: var(--fs-h1); font-weight: 400; letter-spacing: .12em; }
.pagehead .sub { margin-top: 8px; font-size: 12px; color: var(--color-ink-muted); letter-spacing: .08em; }

.sel-bar { padding: 10px 0; border-bottom: 1px solid var(--color-line); }
.check { display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 12px; color: var(--color-ink-soft); letter-spacing: .06em; }
.check input { accent-color: var(--color-gold); width: 14px; height: 14px; }

.item {
  display: flex; align-items: center; gap: 16px; padding: 16px 0; border-bottom: 1px solid var(--color-line);
}
.item-cover { width: 52px; height: 72px; flex: none; }
.meta { flex: 1; min-width: 0; }
.meta b { display: block; font-family: var(--font-display); font-size: 14px; font-weight: 400; letter-spacing: .06em; color: var(--color-ink); }
.meta > span { display: block; margin-top: 4px; font-size: 10px; color: var(--color-ink-muted); letter-spacing: .06em; }

.row-bot { display: flex; align-items: center; justify-content: space-between; margin-top: 10px; }
.price { font-size: 14px; color: var(--color-gold); }

.qty { display: flex; align-items: center; border: 1px solid var(--color-line); height: 28px; }
.qty button { width: 26px; border: 0; background: transparent; color: var(--color-ink-soft); cursor: pointer; font-size: 14px; }
.qty button:hover:not(:disabled) { color: var(--color-gold); }
.qty button:disabled { color: var(--color-ink-faint); }
.qty span { width: 32px; text-align: center; font-size: 12px; }

.rm { background: none; border: 0; font-size: 18px; color: var(--color-ink-faint); cursor: pointer; padding: 4px; }
.rm:hover { color: var(--color-danger); }

.bar { display: flex; align-items: center; justify-content: space-between; margin-top: 24px; padding: 18px 0; border-top: 1px solid var(--color-line); }
.total { display: flex; align-items: baseline; gap: 12px; }
.total span { font-size: 12px; color: var(--color-ink-muted); }
.total b { font-family: var(--font-display); font-size: 24px; color: var(--color-gold); font-weight: 400; }
.hint { font-size: 10px; color: var(--color-ink-faint); letter-spacing: .06em; }
.hint.free { color: var(--color-success); }

.btn {
  height: 46px; padding: 0 28px; font-family: inherit; font-size: 13px; letter-spacing: .12em;
  display: inline-flex; align-items: center; cursor: pointer;
  border: 1px solid var(--color-gold); background: transparent; color: var(--color-gold);
}
.btn-fill { background: var(--color-gold); color: #14150f; font-weight: 500; }
.btn:disabled { opacity: .35; cursor: not-allowed; }
</style>
