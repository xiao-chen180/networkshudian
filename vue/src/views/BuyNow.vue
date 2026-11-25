<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><router-link :to="`/books/${book.id}`">图书详情</router-link><i>/</i><span>立即购买</span></div>
      <div class="pg-head">
        <div>
          <h1>立即购买</h1>
          <div class="sub">确认商品与收货地址后进入支付</div>
        </div>
      </div>

      <div class="layout">
        <div class="main">
          <section class="panel">
            <div class="panel-h"><h3>商品确认</h3><router-link :to="`/books/${book.id}`" class="act">回详情页</router-link></div>
            <div class="goods" v-if="book.id">
              <BookCover :src="book.imageUrl" :title="book.name" :author="book.author" class="g-cover" ratio="3:4" />
              <div class="g-meta">
                <b>{{ book.name }}</b>
                <span>{{ book.author }} · {{ book.publisher }}</span>
                <div class="g-row">
                  <span class="price">¥{{ fmt(book.price) }}</span>
                  <QuantityStepper v-model="quantity" :max="Math.max(1, Number(book.stock) || 1)" />
                </div>
              </div>
            </div>
          </section>

          <section class="panel">
            <div class="panel-h"><h3>收货地址</h3><router-link to="/addresses" class="act">管理地址 →</router-link></div>
            <div v-if="addresses.length" class="addrs">
              <div class="addr" v-for="a in addresses" :key="a.id" :class="{ on: selectedAddressId === a.id }" @click="select(a)">
                <span class="radio"></span>
                <div class="am">
                  <div class="who">{{ a.name }}<span>{{ maskPhone(a.phone) }}</span><i class="tag gold" v-if="a.isDefault">默认</i></div>
                  <div class="where">{{ a.province }}{{ a.city }}{{ a.district || '' }}{{ a.detail }}</div>
                </div>
              </div>
            </div>
            <div class="blank" v-else>还没有收货地址，请先<router-link to="/addresses">去添加</router-link></div>
          </section>
        </div>

        <aside class="aside">
          <div class="summary">
            <h3>费用明细</h3>
            <div class="sline"><span>商品金额（{{ quantity }} 件）</span><b>¥{{ fmt(originalAmount) }}</b></div>
            <div class="sline discount"><span>会员折扣</span><b>− ¥{{ fmt(originalAmount - totalAmount) }}</b></div>
            <div class="sline"><span>运费</span><b>{{ shippingFee > 0 ? `¥${fmt(shippingFee)}` : '¥0.00' }}</b></div>
            <div class="sline total"><span>应付金额</span><b>¥{{ fmt(finalAmount) }}</b></div>
            <button class="submit" :disabled="!selectedAddressId || !book.id" @click="goPay">去支付</button>
            <div class="tip">立即购买不会修改购物袋内容</div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { bookAPI, addressAPI } from '../api'
import { ElMessage } from 'element-plus'
import BookCover from '../components/common/BookCover.vue'
import QuantityStepper from '../components/common/QuantityStepper.vue'

const router = useRouter()
const route = useRoute()

const book = ref({})
const quantity = ref(1)
const addresses = ref([])
const selectedAddressId = ref(null)

const fmt = (n) => Number(n || 0).toFixed(2)
const maskPhone = (p) => (p ? String(p).replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '')
const discount = (() => {
  const d = Number(localStorage.getItem('discount') || 0.95)
  return Number.isFinite(d) && d > 0 && d <= 1 ? d : 0.95
})()

const originalAmount = computed(() => Number(book.value.price || 0) * quantity.value)
const totalAmount = computed(() => originalAmount.value * discount)
const shippingFee = computed(() => (totalAmount.value >= 99 ? 0 : 10))
const finalAmount = computed(() => totalAmount.value + shippingFee.value)

const selectedAddress = computed(() => addresses.value.find(a => a.id === selectedAddressId.value) || null)

function select(a) { selectedAddressId.value = a.id }

async function load() {
  const bookId = route.query.bookId
  if (!bookId) { router.push('/books'); return }
  const q = parseInt(route.query.quantity, 10)
  if (Number.isFinite(q) && q > 0) quantity.value = q
  try {
    const [b, addr] = await Promise.all([
      bookAPI.getById(bookId),
      addressAPI.getAll().catch(() => ({ data: [] }))
    ])
    book.value = b.data || {}
    addresses.value = addr.data || []
    const def = addresses.value.find(a => a.isDefault) || addresses.value[0]
    if (def) selectedAddressId.value = def.id
  } catch {
    ElMessage.error('加载图书失败')
    router.push('/books')
  }
}

function goPay() {
  const a = selectedAddress.value
  if (!a) { ElMessage.warning('请选择收货地址'); return }
  router.push({
    path: '/payment',
    query: {
      bookId: book.value.id,
      quantity: quantity.value,
      addressId: a.id,
      address: `${a.province}${a.city}${a.district || ''}${a.detail}`,
      phone: a.phone,
      totalQuantity: quantity.value,
      totalPrice: totalAmount.value.toFixed(2),
      shippingFee: shippingFee.value.toFixed(2),
      finalPrice: finalAmount.value.toFixed(2)
    }
  })
}

onMounted(load)
</script>

<style scoped>
.layout { display: flex; gap: 36px; align-items: flex-start; }
.main { width: 700px; flex: none; }
.aside { flex: 1; }

.goods { display: flex; gap: 18px; }
.g-cover { width: 92px; height: 124px; flex: none; }
.g-meta { flex: 1; }
.g-meta b { display: block; font-family: var(--font-display); font-size: 17px; font-weight: 400; letter-spacing: .06em; }
.g-meta > span { display: block; margin-top: 6px; font-size: 10.5px; color: var(--color-ink-muted); }
.g-row { display: flex; align-items: center; justify-content: space-between; margin-top: 18px; }
.g-row .price { font-size: 18px; color: var(--color-gold); }

.addrs { display: flex; flex-direction: column; gap: 10px; }
.addr { display: flex; gap: 12px; border: 1px solid var(--color-line); padding: 13px 14px; cursor: pointer; }
.addr.on { border-color: var(--color-gold-line); background: rgba(211,174,106,.04); }
.radio { width: 13px; height: 13px; border-radius: 50%; border: 1px solid var(--color-line-strong); flex: none; margin-top: 3px; position: relative; }
.addr.on .radio { border-color: var(--color-gold); }
.addr.on .radio::after { content: ""; position: absolute; inset: 3px; border-radius: 50%; background: var(--color-gold); }
.am { flex: 1; }
.who { font-size: 12px; color: var(--color-ink); display: flex; align-items: center; gap: 10px; }
.who span { font-size: 10.5px; color: var(--color-ink-muted); }
.where { margin-top: 6px; font-size: 10.5px; color: var(--color-ink-muted); line-height: 1.8; }

.summary { border: 1px solid var(--color-gold-line); background: var(--color-surface); padding: 24px 22px; position: sticky; top: 92px; }
.summary h3 { font-family: var(--font-display); font-size: 14px; font-weight: 400; letter-spacing: .18em; margin-bottom: 16px; }
.sline { display: flex; justify-content: space-between; padding: 7px 0; font-size: 11.5px; color: var(--color-ink-muted); }
.sline b { font-weight: 400; color: var(--color-ink-soft); }
.sline.discount b { color: var(--color-gold); }
.sline.total { border-top: 1px solid var(--color-line-strong); margin-top: 12px; padding-top: 15px; align-items: baseline; }
.sline.total span { font-size: 12px; color: var(--color-ink); }
.sline.total b { font-family: var(--font-display); font-size: 27px; color: var(--color-gold); }
.submit {
  width: 100%; height: 44px; margin-top: 16px; border: 1px solid var(--color-gold);
  background: var(--color-gold); color: #14150f; font-family: inherit; font-size: 12.5px; letter-spacing: .2em; cursor: pointer;
}
.submit:disabled { opacity: .4; cursor: not-allowed; }
.tip { margin-top: 12px; font-size: 9.5px; color: var(--color-ink-faint); text-align: center; }

@media (max-width: 1024px) {
  .layout { flex-direction: column; }
  .main, .aside { width: 100%; }
  .summary { position: static; }
}
</style>