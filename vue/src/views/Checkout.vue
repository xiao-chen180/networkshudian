<template>
  <div class="checkout-page">
    <div class="wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><router-link to="/cart">购物袋</router-link><i>/</i><span>结算</span></div>
      <div class="pagehead">
        <h1>结算</h1>
        <div class="sub">共 {{ totalQuantity }} 件商品 · 应付金额以右侧明细为准</div>
      </div>

      <div class="steps">
        <div class="step done"><b>✓</b>购物袋</div>
        <div class="stepline"></div>
        <div class="step on"><b>2</b>收货地址</div>
        <div class="stepline"></div>
        <div class="step"><b>3</b>支付</div>
        <div class="stepline"></div>
        <div class="step"><b>4</b>完成</div>
      </div>

      <div class="layout">
        <div class="main">
          <!-- 地址 -->
          <section class="panel">
            <div class="panel-h">
              <h3>收货地址</h3>
              <a class="act" @click="showAddressDialog = true">+ 新增地址</a>
            </div>

            <div
              v-for="addr in addresses"
              :key="addr.id"
              class="addr"
              :class="{ on: selectedAddressId === addr.id }"
              @click="selectAddress(addr)"
            >
              <span class="radio"></span>
              <div class="addr-main">
                <div class="who">{{ addr.name }}<span>{{ maskPhone(addr.phone) }}</span></div>
                <div class="where">{{ addr.province }}{{ addr.city }}{{ addr.district || '' }}{{ addr.detail }}</div>
              </div>
              <span class="def" v-if="addr.isDefault">默认</span>
            </div>

            <div v-if="!addresses.length" class="addr-empty">
              还没有收货地址，<a @click="showAddressDialog = true">现在添加</a>
            </div>
          </section>

          <!-- 商品 -->
          <section class="panel">
            <div class="panel-h">
              <h3>商品清单</h3>
              <router-link to="/cart" class="act">返回购物袋修改</router-link>
            </div>
            <div class="item" v-for="item in cartItems" :key="item.id">
              <BookCover :src="item.imageUrl" :title="item.bookName" class="i-cover" compact ratio="3:4" />
              <div class="i-meta">
                <b>{{ item.bookName }}</b>
                <span>{{ item.author }}</span>
              </div>
              <div class="i-right">
                <span class="unit">¥{{ formatPrice(item.price) }}</span>
                <span class="qty">× {{ item.quantity }}</span>
                <span class="sum">¥{{ formatPrice(item.price * item.quantity) }}</span>
              </div>
            </div>
          </section>

          <!-- 配送 -->
          <section class="panel">
            <div class="panel-h"><h3>配送方式</h3></div>
            <div class="deliv">
              <div class="opt" :class="{ on: delivery === 'standard' }" @click="delivery = 'standard'">
                <div><b>标准配送</b><span>预计 2–3 天送达</span></div>
                <em>免运费</em>
              </div>
              <div class="opt" :class="{ on: delivery === 'express' }" @click="delivery = 'express'">
                <div><b>次日达</b><span>18:00 前下单，明日送达</span></div>
                <em>+ ¥12.00</em>
              </div>
            </div>
          </section>

          <!-- 优惠券 -->
          <section class="panel">
            <div class="panel-h">
              <h3>优惠券</h3>
              <router-link to="/coupons" class="act">去领券中心 →</router-link>
            </div>
            <div class="coupons">
              <div
                v-for="c in usableCoupons"
                :key="c.id"
                class="coupon"
                :class="{ on: selectedCouponId === c.id }"
                @click="toggleCoupon(c)"
              >
                <span class="pick">{{ selectedCouponId === c.id ? '已选' : '可选' }}</span>
                <b v-if="!isPercent(c)"><i>¥</i>{{ Number(c.value).toFixed(0) }}</b>
                <b v-else class="pct">{{ percentText(c) }}<i>折</i></b>
                <div class="c">{{ thresholdText(c) }}</div>
                <div class="d">有效期至 {{ formatDate(c.endTime) }}</div>
              </div>
              <div class="coupon none" :class="{ on: !selectedCouponId }" @click="clearCoupon">不使用优惠券</div>
            </div>
            <div class="coupon-hint" v-if="!usableCoupons.length">暂无可用优惠券，<router-link to="/coupons">去领一张 →</router-link></div>
          </section>

          <!-- 备注 -->
          <section class="panel">
            <div class="panel-h"><h3>订单备注</h3></div>
            <input v-model="memo" class="memo" placeholder="选填：对配送时间、发票或包装的说明" />
          </section>
        </div>

        <!-- 汇总 -->
        <aside class="aside">
          <div class="summary">
            <h3>费用明细</h3>
            <div class="sline"><span>商品合计（{{ totalQuantity }} 件）</span><b>¥{{ fmt(originalTotal) }}</b></div>
            <div class="sline discount" v-if="memberDiscount > 0">
              <span>会员折扣 {{ discountLabel }}</span><b>− ¥{{ fmt(memberDiscount) }}</b>
            </div>
            <div class="sline discount" v-if="couponDiscount > 0">
              <span>优惠券 {{ selectedCoupon?.couponName || '' }}</span><b>− ¥{{ fmt(couponDiscount) }}</b>
            </div>
            <div class="sline"><span>运费</span><b>{{ shippingFee > 0 ? `¥${fmt(shippingFee)}` : '¥0.00' }}</b></div>
            <div class="sline total"><span>应付金额</span><b>¥{{ fmt(finalPrice) }}</b></div>

            <button class="submit" :disabled="!canSubmit || isSubmitting" @click="goToPayment">
              {{ isSubmitting ? '处理中…' : '提交订单' }}
            </button>
            <div class="paytypes"><span>支付宝</span><span>微信支付</span><span>余额</span></div>
            <div class="tip">提交后进入支付页，30 分钟内未支付订单将自动取消并释放库存。</div>
          </div>

          <div class="savebox" v-if="totalSaved > 0">
            <b>这一单省了 ¥{{ fmt(totalSaved) }}</b>
            <span>
              会员折扣 {{ fmt(memberDiscount) }} 元<template v-if="couponDiscount > 0"> + 优惠券 {{ fmt(couponDiscount) }} 元</template>。
            </span>
          </div>

          <div class="panel side-panel">
            <div class="panel-h"><h3>服务保障</h3></div>
            <div class="guarantee">
              <div>· 全部图书由出版社 / 一级经销商直发</div>
              <div>· 7 天无理由退换，退换运费由平台承担</div>
              <div>· 破损、缺页可申请补发或全额退款</div>
            </div>
          </div>
        </aside>
      </div>
    </div>

    <!-- 新增地址 -->
    <el-dialog v-model="showAddressDialog" title="新增收货地址" width="520px" class="addr-dialog">
      <el-form ref="addressFormRef" :model="newAddress" :rules="addressRules" label-position="top">
        <div class="grid2">
          <el-form-item label="收货人" prop="name"><el-input v-model="newAddress.name" /></el-form-item>
          <el-form-item label="手机号" prop="phone"><el-input v-model="newAddress.phone" /></el-form-item>
        </div>
        <div class="grid3">
          <el-form-item label="省份" prop="province">
            <el-select v-model="newAddress.province" placeholder="选择省份" @change="onProvinceChange">
              <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
            </el-select>
          </el-form-item>
          <el-form-item label="城市">
            <el-select v-model="newAddress.city" placeholder="选择城市" @change="onCityChange">
              <el-option v-for="c in cityList" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item label="区县">
            <el-select v-model="newAddress.district" placeholder="选择区县">
              <el-option v-for="d in districtList" :key="d" :label="d" :value="d" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="详细地址" prop="detail"><el-input v-model="newAddress.detail" type="textarea" :rows="2" /></el-form-item>
        <el-form-item><el-checkbox v-model="newAddress.isDefault">设为默认地址</el-checkbox></el-form-item>
      </el-form>
      <template #footer>
        <button class="dlg-btn" @click="showAddressDialog = false">取消</button>
        <button class="dlg-btn gold" :disabled="addingAddress" @click="handleAddAddress">保存地址</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { cartAPI, addressAPI, couponAPI } from '../api'
import { ElMessage } from 'element-plus'
import BookCover from '../components/common/BookCover.vue'

const router = useRouter()
const route = useRoute()

const cartItems = ref([])
const selectedCartIds = ref([])
const addresses = ref([])
const selectedAddress = ref(null)
const selectedAddressId = ref(null)
const showAddressDialog = ref(false)
const isSubmitting = ref(false)
const addingAddress = ref(false)
const addressFormRef = ref()
const delivery = ref('standard')
const memo = ref('')

const coupons = ref([])
const selectedCouponId = ref(null)
const couponDiscount = ref(0)

const provinces = [
  '北京市', '天津市', '河北省', '山西省', '内蒙古自治区', '辽宁省', '吉林省', '黑龙江省',
  '上海市', '江苏省', '浙江省', '安徽省', '福建省', '江西省', '山东省', '河南省', '湖北省',
  '湖南省', '广东省', '广西壮族自治区', '海南省', '重庆市', '四川省', '贵州省', '云南省',
  '西藏自治区', '陕西省', '甘肃省', '青海省', '宁夏回族自治区', '新疆维吾尔自治区'
]
const cityData = {
  北京市: ['东城区', '西城区', '朝阳区', '海淀区', '丰台区', '通州区', '昌平区', '大兴区'],
  上海市: ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区', '杨浦区', '闵行区', '浦东新区'],
  广东省: ['广州市', '深圳市', '佛山市', '东莞市', '中山市', '珠海市'],
  浙江省: ['杭州市', '宁波市', '温州市', '嘉兴市', '湖州市', '绍兴市', '金华市'],
  江苏省: ['南京市', '苏州市', '无锡市', '常州市', '徐州市', '南通市'],
  山东省: ['济南市', '青岛市', '淄博市', '烟台市', '潍坊市'],
  河南省: ['郑州市', '开封市', '洛阳市'],
  湖北省: ['武汉市', '宜昌市', '襄阳市'],
  湖南省: ['长沙市', '株洲市', '湘潭市'],
  四川省: ['成都市', '绵阳市', '德阳市'],
  重庆市: ['渝中区', '江北区', '沙坪坝区', '九龙坡区'],
  天津市: ['和平区', '河东区', '河西区', '南开区'],
  河北省: ['石家庄市', '唐山市', '秦皇岛市'],
  山西省: ['太原市', '大同市', '长治市'],
  福建省: ['福州市', '厦门市', '泉州市'],
  安徽省: ['合肥市', '芜湖市', '蚌埠市'],
  陕西省: ['西安市', '宝鸡市', '咸阳市'],
  辽宁省: ['沈阳市', '大连市', '鞍山市']
}
const cityList = ref([])
const districtList = ref([])
const newAddress = reactive({ name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })
const addressRules = {
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const userDiscount = computed(() => {
  const d = Number(localStorage.getItem('discount') || 0.95)
  return Number.isFinite(d) && d > 0 && d <= 1 ? d : 0.95
})
const discountLabel = computed(() => `${(userDiscount.value * 10).toFixed(1).replace('.0', '')} 折`)

const toNum = (v) => {
  const n = typeof v === 'number' ? v : parseFloat(v)
  return Number.isFinite(n) ? n : 0
}
const fmt = (n) => toNum(n).toFixed(2)
const formatPrice = fmt
const maskPhone = (p) => (p ? String(p).replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '')
const formatDate = (t) => (t ? new Date(t).toLocaleDateString('zh-CN') : '')

const totalQuantity = computed(() => cartItems.value.reduce((s, i) => s + Number(i.quantity || 0), 0))
const originalTotal = computed(() => cartItems.value.reduce((s, i) => s + toNum(i.price) * Number(i.quantity || 0), 0))
const memberDiscount = computed(() => originalTotal.value * (1 - userDiscount.value))
const discountedTotal = computed(() => originalTotal.value - memberDiscount.value)
const shippingFee = computed(() => {
  if (delivery.value === 'express') return 12
  return discountedTotal.value >= 99 ? 0 : 10
})
const finalPrice = computed(() => Math.max(0, discountedTotal.value - toNum(couponDiscount.value) + shippingFee.value))
const totalSaved = computed(() => memberDiscount.value + toNum(couponDiscount.value) + (shippingFee.value === 0 ? 10 : 0))
const canSubmit = computed(() => !!selectedAddress.value && cartItems.value.length > 0)

const isPercent = (c) => c.type === 'PERCENT'
/* 后端 PERCENT 的 value 表示减免百分比：20 → 减 20% → 8 折 */
const percentText = (c) => {
  const n = (100 - Number(c.value)) / 10
  return Number.isFinite(n) ? n.toFixed(1).replace('.0', '') : '—'
}
const thresholdText = (c) => (Number(c.minAmount) > 0 ? `满 ${Number(c.minAmount).toFixed(0)} 元可用` : '无门槛')
const usableCoupons = computed(() =>
  coupons.value.filter(c => toNum(c.minAmount) <= discountedTotal.value)
)

async function loadCart() {
  const idsParam = route.query.ids || route.query.cartIds
  selectedCartIds.value = idsParam ? String(idsParam).split(',').map(Number).filter(Number.isFinite) : []
  try {
    const res = await cartAPI.getCart()
    const all = res.data || []
    cartItems.value = selectedCartIds.value.length
      ? all.filter(i => selectedCartIds.value.includes(i.id))
      : all
    selectedCartIds.value = cartItems.value.map(i => i.id)
  } catch {}
}

async function loadAddresses() {
  try {
    const res = await addressAPI.getAll()
    addresses.value = res.data || []
    const def = addresses.value.find(a => a.isDefault) || addresses.value[0]
    if (def) { selectedAddress.value = def; selectedAddressId.value = def.id }
  } catch {}
}

async function loadCoupons() {
  try {
    const res = await couponAPI.getMyUnused()
    coupons.value = res.data || []
  } catch {}
}

function selectAddress(addr) {
  selectedAddress.value = addr
  selectedAddressId.value = addr.id
}

async function toggleCoupon(c) {
  if (selectedCouponId.value === c.id) { clearCoupon(); return }
  selectedCouponId.value = c.id
  try {
    const res = await couponAPI.calculate({ userCouponId: c.id, totalAmount: discountedTotal.value })
    couponDiscount.value = toNum(res.data?.discount)
  } catch {
    couponDiscount.value = 0
    ElMessage.error('该优惠券不可用')
    selectedCouponId.value = null
  }
}

function clearCoupon() {
  selectedCouponId.value = null
  couponDiscount.value = 0
}

const selectedCoupon = computed(() => coupons.value.find(c => c.id === selectedCouponId.value) || null)

function onProvinceChange() {
  cityList.value = cityData[newAddress.province] || ['市辖区', '城区', '开发区']
  districtList.value = []
  newAddress.city = ''
  newAddress.district = ''
}
function onCityChange() {
  districtList.value = ['东城区', '西城区', '朝阳区', '海淀区', '丰台区', '开发区', '新区']
  newAddress.district = ''
}

async function handleAddAddress() {
  if (!addressFormRef.value) return
  try { await addressFormRef.value.validate() } catch { return }
  addingAddress.value = true
  try {
    const res = await addressAPI.create({ ...newAddress })
    ElMessage.success('地址添加成功')
    showAddressDialog.value = false
    Object.assign(newAddress, { name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })
    await loadAddresses()
    if (res.data) { selectedAddress.value = res.data; selectedAddressId.value = res.data.id }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '添加失败')
  } finally {
    addingAddress.value = false
  }
}

function goToPayment() {
  if (!canSubmit.value || isSubmitting.value) return
  isSubmitting.value = true
  const a = selectedAddress.value
  const shippingAddress = `${a.province}${a.city}${a.district || ''}${a.detail}`
  const query = {
    ids: selectedCartIds.value.join(','),
    addressId: a.id,
    address: shippingAddress,
    phone: a.phone,
    totalQuantity: totalQuantity.value,
    totalPrice: discountedTotal.value.toFixed(2),
    shippingFee: shippingFee.value.toFixed(2),
    finalPrice: finalPrice.value.toFixed(2),
    couponDiscount: couponDiscount.value.toFixed(2),
    couponId: selectedCouponId.value || '',
    memo: memo.value || ''
  }
  router.push({ path: '/payment', query })
}

onMounted(() => {
  loadCart()
  loadAddresses()
  loadCoupons()
})
</script>

<style scoped>
.checkout-page { min-height: 100vh; padding-bottom: 50px; }
.wrap { max-width: var(--container); margin: 0 auto; padding: 0 var(--gutter); }

.crumb { padding: 26px 0 0; font-size: 11px; color: var(--color-ink-faint); letter-spacing: .08em; display: flex; gap: 8px; }
.crumb a { color: var(--color-ink-muted); }
.crumb i { font-style: normal; opacity: .5; }

.pagehead { padding: 18px 0 0; }
h1 { font-family: var(--font-display); font-size: var(--fs-h1); font-weight: 400; letter-spacing: .12em; }
.pagehead .sub { margin-top: 8px; font-size: 11.5px; color: var(--color-ink-muted); letter-spacing: .08em; }

/* 步骤 */
.steps { display: flex; align-items: center; margin: 26px 0 30px; border-top: 1px solid var(--color-line); padding-top: 22px; }
.step { display: flex; align-items: center; gap: 9px; font-size: 11px; letter-spacing: .1em; color: var(--color-ink-faint); }
.step b {
  width: 20px; height: 20px; border-radius: 50%; border: 1px solid var(--color-line-strong);
  display: flex; align-items: center; justify-content: center; font-size: 9px; font-weight: 400; font-family: var(--font-display);
}
.step.done { color: var(--color-ink-muted); }
.step.done b { border-color: var(--color-gold-line); color: var(--color-gold); }
.step.on { color: var(--color-ink); }
.step.on b { background: var(--color-gold); border-color: var(--color-gold); color: #14150f; }
.stepline { flex: 1; height: 1px; background: var(--color-line); margin: 0 18px; }

.layout { display: flex; gap: 36px; align-items: flex-start; }
.main { width: 700px; flex: none; }
.aside { flex: 1; min-width: 0; }

.panel { border: 1px solid var(--color-line); background: var(--color-surface); padding: 22px 24px; margin-bottom: 16px; }
.panel-h { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.panel-h h3 { font-family: var(--font-display); font-size: 14px; font-weight: 400; letter-spacing: .18em; }
.panel-h .act { font-size: 10.5px; color: var(--color-gold); letter-spacing: .1em; cursor: pointer; }

/* 地址 */
.addr { display: flex; gap: 12px; padding: 14px; border: 1px solid var(--color-line); margin-bottom: 10px; cursor: pointer; position: relative; }
.addr:last-child { margin-bottom: 0; }
.addr.on { border-color: var(--color-gold-line); background: rgba(211,174,106,.04); }
.radio { width: 13px; height: 13px; border-radius: 50%; border: 1px solid var(--color-line-strong); flex: none; margin-top: 3px; position: relative; }
.addr.on .radio { border-color: var(--color-gold); }
.addr.on .radio::after { content: ""; position: absolute; inset: 3px; border-radius: 50%; background: var(--color-gold); }
.addr-main { flex: 1; }
.who { font-size: 12.5px; color: var(--color-ink); letter-spacing: .05em; }
.who span { margin-left: 12px; font-size: 10.5px; color: var(--color-ink-muted); }
.where { margin-top: 7px; font-size: 10.5px; color: var(--color-ink-muted); line-height: 1.8; letter-spacing: .03em; }
.def { position: absolute; right: 14px; top: 14px; font-size: 9px; letter-spacing: .14em; color: var(--color-gold); border: 1px solid var(--color-gold-line); padding: 2px 6px; }
.addr-empty { font-size: 11.5px; color: var(--color-ink-muted); }
.addr-empty a { color: var(--color-gold); cursor: pointer; }

/* 商品 */
.item { display: flex; gap: 14px; align-items: center; padding: 12px 0; border-top: 1px solid var(--color-line); }
.item:first-of-type { border-top: 0; padding-top: 0; }
.i-cover { width: 44px; height: 58px; flex: none; }
.i-meta { flex: 1; min-width: 0; }
.i-meta b { display: block; font-family: var(--font-display); font-size: 13px; font-weight: 400; letter-spacing: .05em; }
.i-meta span { display: block; margin-top: 4px; font-size: 9.5px; color: var(--color-ink-faint); }
.i-right { display: flex; align-items: center; gap: 22px; font-size: 11px; color: var(--color-ink-muted); }
.i-right .sum { width: 70px; text-align: right; color: var(--color-gold); }

/* 配送 */
.deliv { display: flex; gap: 10px; }
.opt { flex: 1; border: 1px solid var(--color-line); padding: 13px 15px; display: flex; justify-content: space-between; align-items: center; cursor: pointer; }
.opt.on { border-color: var(--color-gold-line); background: rgba(211,174,106,.04); }
.opt b { font-size: 12px; font-weight: 400; color: var(--color-ink); letter-spacing: .06em; }
.opt span { margin-left: 10px; font-size: 10px; color: var(--color-ink-muted); }
.opt em { font-style: normal; font-size: 10.5px; color: var(--color-gold); }

/* 优惠券 */
.coupons { display: flex; gap: 10px; flex-wrap: wrap; align-items: stretch; }
.coupon {
  width: 196px; height: 104px; border: 1px solid var(--color-line); padding: 14px 15px;
  position: relative; cursor: pointer; display: flex; flex-direction: column; justify-content: center;
}
.coupon.on { border-color: var(--color-gold-line); background: rgba(211,174,106,.06); }
.coupon b { font-family: var(--font-display); font-size: 22px; color: var(--color-gold); font-weight: 400; line-height: 1.1; }
.coupon b i { font-style: normal; font-size: 11px; margin-right: 2px; }
.coupon b.pct { font-size: 20px; }
.coupon b.pct i { font-size: 10px; margin-left: 3px; margin-right: 0; }
.coupon .c { margin-top: 7px; font-size: 10px; color: var(--color-ink-muted); letter-spacing: .04em; }
.coupon .d { margin-top: 6px; font-size: 9px; color: var(--color-ink-faint); }
.coupon .pick { position: absolute; right: 10px; top: 10px; font-size: 9px; color: var(--color-ink-faint); letter-spacing: .1em; }
.coupon.on .pick { color: var(--color-gold); }
.coupon.none {
  display: flex; align-items: center; justify-content: center;
  font-size: 10.5px; color: var(--color-ink-faint); text-align: center;
}
.coupon.none.on { color: var(--color-ink-soft); border-color: var(--color-line-strong); background: transparent; }
.coupon-hint { margin-top: 12px; font-size: 10.5px; color: var(--color-ink-faint); }
.coupon-hint a { color: var(--color-gold); }

.memo {
  width: 100%; height: 38px; background: var(--color-bg); border: 1px solid var(--color-line);
  color: var(--color-ink); font-family: inherit; font-size: 11.5px; padding: 0 12px; letter-spacing: .04em;
}
.memo::placeholder { color: var(--color-ink-faint); }
.memo:focus { outline: 0; border-color: var(--color-gold-line); }

/* 汇总 */
.summary { border: 1px solid var(--color-gold-line); background: var(--color-surface); padding: 22px 22px 20px; position: sticky; top: 92px; }
.summary h3 { font-family: var(--font-display); font-size: 14px; font-weight: 400; letter-spacing: .18em; margin-bottom: 16px; }
.sline { display: flex; justify-content: space-between; font-size: 11.5px; color: var(--color-ink-muted); letter-spacing: .04em; padding: 7px 0; }
.sline b { font-weight: 400; color: var(--color-ink-soft); font-variant-numeric: tabular-nums; }
.sline.discount b { color: var(--color-gold); }
.sline.total { border-top: 1px solid var(--color-line-strong); margin-top: 12px; padding-top: 15px; align-items: baseline; }
.sline.total span { font-size: 12px; color: var(--color-ink); }
.sline.total b { font-family: var(--font-display); font-size: 27px; color: var(--color-gold); }

.submit {
  width: 100%; height: 44px; margin-top: 16px; border: 1px solid var(--color-gold);
  background: var(--color-gold); color: #14150f; font-family: inherit; font-size: 12.5px;
  letter-spacing: .2em; cursor: pointer;
}
.submit:disabled { opacity: .4; cursor: not-allowed; }
.paytypes { display: flex; gap: 8px; justify-content: center; margin-top: 14px; }
.paytypes span { border: 1px solid var(--color-line); padding: 3px 8px; font-size: 9.5px; color: var(--color-ink-faint); letter-spacing: .1em; }
.tip { margin-top: 12px; font-size: 9.5px; color: var(--color-ink-faint); line-height: 1.8; letter-spacing: .03em; }

.savebox { border: 1px solid var(--color-line); padding: 14px 16px; margin-top: 14px; }
.savebox b { display: block; font-size: 11px; color: var(--color-ink-soft); letter-spacing: .08em; margin-bottom: 5px; }
.savebox span { font-size: 10px; color: var(--color-ink-muted); line-height: 1.8; }

.side-panel { margin-top: 14px; margin-bottom: 0; }
.guarantee { display: flex; flex-direction: column; gap: 9px; font-size: 10.5px; color: var(--color-ink-muted); line-height: 1.7; }

/* 地址弹窗 */
.grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 0 14px; }
.grid3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 0 12px; }
.addr-dialog :deep(.el-dialog) { background: var(--color-surface); border: 1px solid var(--color-line); }
.addr-dialog :deep(.el-dialog__title) { font-family: var(--font-display); letter-spacing: .12em; color: var(--color-ink); }
.addr-dialog :deep(.el-form-item__label) { font-size: 10.5px; letter-spacing: .12em; color: var(--color-ink-muted); }
.addr-dialog :deep(.el-input__wrapper) { background: var(--color-bg); box-shadow: 0 0 0 1px var(--color-line) inset; border-radius: 0; }
.addr-dialog :deep(.el-textarea__inner) { background: var(--color-bg); box-shadow: 0 0 0 1px var(--color-line) inset; border-radius: 0; color: var(--color-ink); }
.dlg-btn {
  height: 36px; padding: 0 20px; border: 1px solid var(--color-line-strong); background: transparent;
  color: var(--color-ink-soft); font-family: inherit; font-size: 11px; letter-spacing: .1em; cursor: pointer; margin-left: 8px;
}
.dlg-btn.gold { background: var(--color-gold); border-color: var(--color-gold); color: #14150f; }

@media (max-width: 1024px) {
  .layout { flex-direction: column; }
  .main { width: 100%; }
  .aside { width: 100%; }
  .summary { position: static; }
  .grid3 { grid-template-columns: 1fr; }
}
</style>
