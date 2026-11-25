<template>
  <div class="cart-page">
    <div class="hero-section">
      <h1 class="page-title">🛒 购物车</h1>
    </div>

    <div class="cart-container">
      <div v-if="cartItems.length > 0">
        <!-- 全选栏 -->
        <div class="select-all-bar">
          <label class="checkbox-label">
            <input type="checkbox" v-model="selectAll" @change="toggleSelectAll" />
            <span>全选</span>
          </label>
        </div>

        <!-- 购物车列表 -->
        <div class="cart-list">
          <div v-for="item in cartItems" :key="item.id" class="cart-item">
            <label class="checkbox-label">
              <input type="checkbox" v-model="item.selected" @change="updateTotal" />
            </label>
            <img :src="getCover(item)" class="item-cover" />
            <div class="item-info">
              <h3 class="item-name">{{ item.bookName }}</h3>
              <p class="item-author">{{ item.bookAuthor }}</p>
              <div class="item-bottom">
                <span class="item-price">¥{{ formatPrice(item.price) }}</span>
                <div class="qty-control">
                  <button @click="changeQty(item, -1)" :disabled="item.quantity <= 1">-</button>
                  <span>{{ item.quantity }}</span>
                  <button @click="changeQty(item, 1)">+</button>
                </div>
              </div>
            </div>
            <button class="delete-btn" @click="removeItem(item.id)">×</button>
          </div>
        </div>

        <!-- 结算栏 -->
        <div class="checkout-bar">
          <div class="total-info">
            <span>合计：</span>
            <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <button class="checkout-btn" @click="goCheckout" :disabled="selectedItems.length === 0">
            结算({{ selectedItems.length }})
          </button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">🛒</div>
        <p>购物车是空的</p>
        <router-link to="/books" class="go-shop-btn">去逛逛</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { cartAPI } from '../api'
import { ElMessage } from 'element-plus'
import { useCartCount } from '../utils/cartCount'

const { setCartCount } = useCartCount()
const router = useRouter()
const cartItems = ref([])
const selectAll = ref(false)

const selectedItems = computed(() => cartItems.value.filter(i => i.selected))

const totalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const loadCart = async () => {
  try {
    const res = await cartAPI.getCart()
    cartItems.value = (res.data || []).map(item => ({ ...item, selected: false }))
  } catch (e) {
    console.error(e)
  }
}

const toggleSelectAll = () => {
  cartItems.value.forEach(item => item.selected = selectAll.value)
}

const updateTotal = () => {
  selectAll.value = cartItems.value.length > 0 && cartItems.value.every(i => i.selected)
}

const changeQty = async (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty < 1) return
  try {
    await cartAPI.updateCartItem(item.id, { quantity: newQty })
    item.quantity = newQty
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

const removeItem = async (id) => {
  try {
    await cartAPI.removeFromCart(id)
    cartItems.value = cartItems.value.filter(i => i.id !== id)
    setCartCount(cartItems.value.length)
    ElMessage.success('已删除')
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const goCheckout = () => {
  if (selectedItems.value.length === 0) return
  const ids = selectedItems.value.map(i => i.id).join(',')
  router.push(`/checkout?cartIds=${ids}`)
}

const getCover = (item) => `https://picsum.photos/seed/${item.bookId || item.id}/200/280.jpg`
const formatPrice = (p) => typeof p === 'string' ? p : Number(p).toFixed(2)

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  background: var(--bg-page);
  padding-bottom: 80px;
}

.hero-section {
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  padding: 20px;
  text-align: center;
}

.page-title {
  color: white;
  font-family: var(--font-serif);
  font-size: 22px;
  margin: 0;
}

.cart-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;
}

.select-all-bar {
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 12px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  accent-color: var(--primary);
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 80px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  padding: 16px;
  border-radius: 12px;
}

.item-cover {
  width: 80px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 15px;
  color: var(--text-primary);
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-author {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0 0 8px;
}

.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price {
  font-size: 16px;
  color: var(--accent);
  font-weight: bold;
}

.qty-control {
  display: flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 6px;
}

.qty-control button {
  width: 28px;
  height: 28px;
  border: none;
  background: #f5f5f5;
  cursor: pointer;
  font-size: 16px;
}

.qty-control span {
  width: 36px;
  text-align: center;
  font-size: 14px;
}

.delete-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #ccc;
  cursor: pointer;
  padding: 4px;
}

.checkout-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: white;
  border-top: 1px solid #eee;
}

.total-price {
  font-size: 20px;
  color: var(--accent);
  font-weight: bold;
}

.checkout-btn {
  padding: 10px 32px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 15px;
  cursor: pointer;
}

.checkout-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.go-shop-btn {
  display: inline-block;
  padding: 10px 32px;
  background: var(--primary);
  color: white;
  border-radius: 20px;
  text-decoration: none;
}
</style>
