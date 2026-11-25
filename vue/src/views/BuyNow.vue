<template>
  <div class="buy-page">
    <!-- 顶部导航 -->
    <div class="top-bar">
      <div class="top-bar-inner">
        <div class="logo">书香阁</div>
        <div class="step-indicator">
          <div class="step active">
            <span class="step-num">1</span>
            <span class="step-text">确认订单</span>
          </div>
          <div class="step-line active-line"></div>
          <div class="step">
            <span class="step-num">2</span>
            <span class="step-text">支付</span>
          </div>
          <div class="step-line"></div>
          <div class="step">
            <span class="step-num">3</span>
            <span class="step-text">完成</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主体内容 - 上下结构 -->
    <div class="container">
      <!-- 商品信息 -->
      <div class="section-card">
        <div class="section-title">
          <span class="title-icon">📦</span>
          商品信息
        </div>
        <div class="product-row">
          <div class="product-image">
            <img :src="`https://picsum.photos/seed/${book.id}/160/220.jpg`" :alt="book.name" />
            <div class="stock-tag" v-if="book.stock > 0">有货</div>
            <div class="stock-tag out" v-else>缺货</div>
          </div>
          <div class="product-info">
            <h2 class="product-name">{{ book.name }}</h2>
            <p class="product-author">作者: {{ book.author }}</p>
            <p class="product-publisher">出版社: {{ book.publisher }}</p>
            <p class="product-isbn">ISBN: {{ book.isbn }}</p>
            <div class="price-row">
              <div class="price-box">
                <span class="price-label">单价</span>
                <span class="price-main">¥{{ formatPrice(book.price) }}</span>
              </div>
              <div class="price-box discount">
                <span class="price-label">会员价</span>
                <span class="price-sale">¥{{ formatPrice(book.price * 0.95) }}</span>
              </div>
            </div>
          </div>
          <div class="product-qty">
            <span class="qty-label">数量</span>
            <div class="qty-group">
              <button class="qty-btn" @click="decreaseQuantity" :disabled="quantity <= 1 || book.stock <= 0">-</button>
              <input type="number" :value="quantity" class="qty-num" readonly />
              <button class="qty-btn" @click="increaseQuantity" :disabled="quantity >= book.stock">+</button>
            </div>
            <span class="stock-info">库存 {{ book.stock }}本</span>
          </div>
        </div>
      </div>

      <!-- 收货地址 -->
      <div class="section-card">
        <div class="section-title">
          <span class="title-icon">📍</span>
          收货地址
          <button class="link-btn" @click="showAddressDialog = true">+ 新增地址</button>
        </div>

        <div v-if="addresses.length === 0" class="empty-address">
          <p>暂无收货地址</p>
          <button class="btn-add" @click="showAddressDialog = true">添加收货地址</button>
        </div>

        <div v-else class="address-options">
          <label
            v-for="addr in addresses"
            :key="addr.id"
            class="address-option"
            :class="{ active: selectedAddressId === addr.id }"
          >
            <input type="radio" :value="addr.id" v-model="selectedAddressId" class="radio-input" />
            <div class="addr-content">
              <div class="addr-top">
                <span class="addr-name">{{ addr.name }}</span>
                <span class="addr-phone">{{ addr.phone }}</span>
                <span v-if="addr.isDefault" class="badge-default">默认</span>
              </div>
              <div class="addr-text">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</div>
            </div>
            <div class="radio-dot"></div>
          </label>
        </div>
      </div>

      <!-- 结算信息 -->
      <div class="section-card settle-card">
        <div class="settle-content">
          <div class="settle-left">
            <div class="settle-row">
              <span>商品金额</span>
              <span>¥{{ formatPrice(originalTotalAmount) }}</span>
            </div>
            <div class="settle-row discount">
              <span>会员折扣 (95折)</span>
              <span class="discount-amount">-¥{{ formatPrice(originalTotalAmount - discountedTotalAmount) }}</span>
            </div>
            <div class="settle-row">
              <span>运费</span>
              <span :class="{ 'text-green': shippingFee === 0 }">
                {{ shippingFee === 0 ? '免运费' : '¥' + formatPrice(shippingFee) }}
              </span>
            </div>
          </div>
          <div class="settle-right">
            <div class="total-row">
              <span class="total-label">应付总额</span>
              <div class="price-compare">
                <span class="original-price">¥{{ formatPrice(originalTotalAmount + shippingFee) }}</span>
                <span class="total-price">¥{{ formatPrice(finalAmount) }}</span>
              </div>
            </div>
            <div class="action-btns">
              <button class="btn-cancel" @click="goBack">返回</button>
              <button
                class="btn-confirm"
                @click="submitOrder"
                :disabled="!selectedAddressId || book.stock <= 0 || submitting"
              >
                {{ submitting ? '提交中...' : '提交订单' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加地址弹窗 -->
    <el-dialog
      title="新增收货地址"
      v-model="showAddressDialog"
      width="520px"
      class="address-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="newAddress" :rules="addressRules" ref="addressFormRef" label-width="90px">
        <el-form-item label="收货人" prop="name">
          <el-input v-model="newAddress.name" placeholder="请输入收货人姓名" maxlength="20"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="newAddress.phone" placeholder="请输入手机号码" maxlength="11"></el-input>
        </el-form-item>
        <el-form-item label="所在地区" prop="province">
          <div class="region-row">
            <el-select v-model="newAddress.province" placeholder="省份" @change="onProvinceChange" style="flex:1">
              <el-option v-for="p in provinces" :key="p" :label="p" :value="p"></el-option>
            </el-select>
            <el-select v-model="newAddress.city" placeholder="城市" @change="onCityChange" style="flex:1">
              <el-option v-for="c in cityList" :key="c" :label="c" :value="c"></el-option>
            </el-select>
            <el-select v-model="newAddress.district" placeholder="区/县" style="flex:1">
              <el-option v-for="d in districtList" :key="d" :label="d" :value="d"></el-option>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="newAddress.detail" type="textarea" :rows="3" placeholder="请输入详细地址" maxlength="200"></el-input>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="newAddress.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddAddress" :loading="addingAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { bookAPI, addressAPI, orderAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const book = ref({})
const quantity = ref(1)
const selectedAddressId = ref(null)
const addresses = ref([])
const showAddressDialog = ref(false)
const submitting = ref(false)
const addingAddress = ref(false)
const addressFormRef = ref()

const provinces = [
  '北京市', '天津市', '河北省', '山西省', '内蒙古自治区',
  '辽宁省', '吉林省', '黑龙江省', '上海市', '江苏省',
  '浙江省', '安徽省', '福建省', '江西省', '山东省',
  '河南省', '湖北省', '湖南省', '广东省', '广西壮族自治区',
  '海南省', '重庆市', '四川省', '贵州省', '云南省',
  '西藏自治区', '陕西省', '甘肃省', '青海省', '宁夏回族自治区',
  '新疆维吾尔自治区', '香港特别行政区', '澳门特别行政区', '台湾省'
]

const cityData = {
  '北京市': ['东城区', '西城区', '朝阳区', '海淀区', '丰台区', '石景山区', '通州区', '顺义区', '昌平区', '大兴区'],
  '上海市': ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区', '杨浦区', '闵行区', '宝山区', '浦东新区'],
  '广东省': ['广州市', '深圳市', '佛山市', '东莞市', '中山市', '珠海市', '惠州市', '江门市'],
  '浙江省': ['杭州市', '宁波市', '温州市', '嘉兴市', '湖州市', '绍兴市', '金华市'],
  '江苏省': ['南京市', '苏州市', '无锡市', '常州市', '徐州市', '南通市'],
  '山东省': ['济南市', '青岛市', '淄博市', '烟台市', '潍坊市', '济宁市'],
  '河南省': ['郑州市', '开封市', '洛阳市', '平顶山市', '安阳市'],
  '湖北省': ['武汉市', '黄石市', '十堰市', '宜昌市', '襄阳市'],
  '湖南省': ['长沙市', '株洲市', '湘潭市', '衡阳市', '岳阳市'],
  '四川省': ['成都市', '自贡市', '攀枝花市', '泸州市', '德阳市'],
  '重庆市': ['渝中区', '大渡口区', '江北区', '沙坪坝区', '九龙坡区'],
  '天津市': ['和平区', '河东区', '河西区', '南开区', '河北区'],
  '河北省': ['石家庄市', '唐山市', '秦皇岛市', '邯郸市', '邢台市'],
  '山西省': ['太原市', '大同市', '阳泉市', '长治市', '晋城市'],
}

const cityList = ref([])
const districtList = ref([])

const newAddress = reactive({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

const addressRules = {
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const originalTotalAmount = computed(() => book.value.price * quantity.value)
const discountedTotalAmount = computed(() => book.value.price * 0.95 * quantity.value)
const totalAmount = computed(() => discountedTotalAmount.value)
const shippingFee = computed(() => discountedTotalAmount.value >= 99 ? 0 : 10)
const finalAmount = computed(() => discountedTotalAmount.value + shippingFee.value)

const onProvinceChange = () => {
  cityList.value = cityData[newAddress.province] || ['市辖区', '城区', '开发区']
  districtList.value = []
  newAddress.city = ''
  newAddress.district = ''
}

const onCityChange = () => {
  districtList.value = ['东城区', '西城区', '朝阳区', '海淀区', '丰台区', '开发区', '新区']
  newAddress.district = ''
}

const loadBook = async () => {
  const bookId = route.query.bookId
  if (!bookId) {
    ElMessage.error('缺少书籍ID')
    router.push('/books')
    return
  }
  try {
    const response = await bookAPI.getById(bookId)
    book.value = response.data
  } catch (error) {
    ElMessage.error('获取书籍信息失败')
    router.push('/books')
  }
}

const loadAddresses = async () => {
  try {
    const response = await addressAPI.getAll()
    addresses.value = response.data || []
    if (addresses.value.length > 0) {
      const defaultAddr = addresses.value.find(a => a.isDefault)
      selectedAddressId.value = defaultAddr ? defaultAddr.id : addresses.value[0].id
    }
  } catch (error) {
    console.error('加载地址失败:', error)
  }
}

const increaseQuantity = () => {
  if (quantity.value < book.value.stock) quantity.value++
}

const decreaseQuantity = () => {
  if (quantity.value > 1) quantity.value--
}

const handleAddAddress = async () => {
  if (!addressFormRef.value) return
  try {
    await addressFormRef.value.validate()
  } catch {
    return
  }

  addingAddress.value = true
  try {
    const data = {
      name: newAddress.name,
      phone: newAddress.phone,
      province: newAddress.province,
      city: newAddress.city,
      district: newAddress.district,
      detail: newAddress.detail,
      isDefault: newAddress.isDefault
    }
    const response = await addressAPI.create(data)
    ElMessage.success('地址添加成功')
    showAddressDialog.value = false
    resetNewAddress()
    await loadAddresses()
    selectedAddressId.value = response.data.id
  } catch (error) {
    const msg = error.response?.data?.message || '添加失败'
    ElMessage.error(msg)
  } finally {
    addingAddress.value = false
  }
}

const resetNewAddress = () => {
  newAddress.name = ''
  newAddress.phone = ''
  newAddress.province = ''
  newAddress.city = ''
  newAddress.district = ''
  newAddress.detail = ''
  newAddress.isDefault = false
  cityList.value = []
  districtList.value = []
}

const submitOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (book.value.stock <= 0) {
    ElMessage.warning('该商品已售罄')
    return
  }

  submitting.value = true
  try {
    const selectedAddr = addresses.value.find(a => a.id === selectedAddressId.value)
    const shippingAddress = selectedAddr.province + selectedAddr.city +
      (selectedAddr.district || '') + selectedAddr.detail

    // 只传递订单信息到支付页面，不在此创建订单
    const query = {
      bookId: book.value.id,
      quantity: quantity.value,
      addressId: selectedAddressId.value,
      address: shippingAddress,
      phone: selectedAddr.phone,
      totalQuantity: quantity.value,
      totalPrice: totalAmount.value,
      shippingFee: shippingFee.value,
      finalPrice: finalAmount.value
    }
    router.push({ path: '/payment', query })
  } catch (error) {
    ElMessage.error('跳转支付页面失败')
  } finally {
    submitting.value = false
  }
}

const goBack = () => router.back()

const formatPrice = (price) => {
  if (typeof price === 'number') return price.toFixed(2)
  return parseFloat(price).toFixed(2)
}

onMounted(() => {
  loadBook()
  loadAddresses()
})
</script>

<style scoped>
.buy-page {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 顶部导航 */
.top-bar {
  background: white;
  border-bottom: 2px solid #eee;
  padding: 0 40px;
}

.top-bar-inner {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.logo {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 24px;
  font-weight: bold;
  color: #c21500;
}

/* 步骤指示器 - 加大版 */
.step-indicator {
  display: flex;
  align-items: center;
  gap: 0;
}

.step {
  display: flex;
  align-items: center;
  gap: 8px;
}

.step-num {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  transition: all 0.3s;
}

.step.active .step-num {
  background: #c21500;
  color: white;
  box-shadow: 0 3px 12px rgba(194, 21, 0, 0.35);
}

.step-text {
  font-size: 16px;
  color: #999;
  font-weight: 500;
}

.step.active .step-text {
  color: #c21500;
  font-weight: 700;
}

.step-line {
  width: 80px;
  height: 3px;
  background: #e0e0e0;
  margin: 0 16px;
  border-radius: 2px;
}

.step-line.active-line {
  background: linear-gradient(90deg, #c21500, #e0e0e0);
}

/* 主体容器 */
.container {
  max-width: 900px;
  margin: 24px auto;
  padding: 0 40px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 卡片通用 */
.section-card {
  background: white;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.title-icon {
  font-size: 18px;
}

.link-btn {
  margin-left: auto;
  padding: 5px 14px;
  background: transparent;
  border: 1px solid #c21500;
  border-radius: 4px;
  color: #c21500;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.link-btn:hover {
  background: #c21500;
  color: white;
}

/* 商品行 */
.product-row {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.product-image {
  position: relative;
  flex-shrink: 0;
}

.product-image img {
  width: 140px;
  height: 190px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stock-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 3px 10px;
  background: rgba(34, 139, 34, 0.9);
  color: white;
  border-radius: 4px;
  font-size: 11px;
}

.stock-tag.out {
  background: rgba(200, 0, 0, 0.9);
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 20px;
  font-weight: 600;
  color: #222;
  margin: 0 0 8px 0;
}

.product-author,
.product-publisher,
.product-isbn {
  font-size: 13px;
  color: #888;
  margin: 0 0 4px 0;
}

.price-row {
  display: flex;
  gap: 24px;
  margin-top: 16px;
}

.price-box {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.price-label {
  font-size: 12px;
  color: #999;
}

.price-main {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.price-sale {
  font-size: 20px;
  font-weight: 600;
  color: #c21500;
}

/* 数量 */
.product-qty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.qty-label {
  font-size: 13px;
  color: #999;
}

.qty-group {
  display: flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 6px;
  overflow: hidden;
}

.qty-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  border: none;
  font-size: 18px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.qty-btn:hover:not(:disabled) {
  background: #f0f0f0;
  color: #c21500;
}

.qty-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.qty-num {
  width: 44px;
  height: 34px;
  border: none;
  border-left: 1px solid #ddd;
  border-right: 1px solid #ddd;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  background: white;
  outline: none;
}

.stock-info {
  font-size: 11px;
  color: #999;
}

/* 地址 */
.empty-address {
  text-align: center;
  padding: 32px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px dashed #ddd;
}

.empty-address p {
  color: #999;
  margin: 0 0 12px 0;
  font-size: 14px;
}

.btn-add {
  padding: 8px 20px;
  background: #c21500;
  border: none;
  border-radius: 6px;
  color: white;
  font-size: 13px;
  cursor: pointer;
}

.btn-add:hover {
  background: #a30000;
}

.address-options {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.address-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  border: 2px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 240px;
  flex-shrink: 0;
}

.address-option:hover {
  border-color: #c21500;
}

.address-option.active {
  border-color: #c21500;
  background: #fff5f5;
}

.radio-input {
  display: none;
}

.addr-content {
  flex: 1;
}

.addr-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.addr-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.addr-phone {
  font-size: 12px;
  color: #999;
}

.badge-default {
  padding: 1px 6px;
  background: #ff9800;
  color: white;
  border-radius: 3px;
  font-size: 10px;
}

.addr-text {
  font-size: 12px;
  color: #888;
  line-height: 1.4;
}

.radio-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #ddd;
  flex-shrink: 0;
  transition: all 0.2s;
  position: relative;
}

.address-option.active .radio-dot {
  border-color: #c21500;
  background: #c21500;
}

.address-option.active .radio-dot::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 6px;
  height: 6px;
  background: white;
  border-radius: 50%;
}

/* 结算 */
.settle-card {
  padding: 20px 24px;
}

.settle-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.settle-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.settle-row {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #888;
}

.settle-row.discount {
  color: #c21500;
}

.discount-amount {
  font-weight: 600;
}

.text-green {
  color: #228B22;
}

.settle-right {
  display: flex;
  align-items: center;
  gap: 32px;
}

.total-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.total-label {
  font-size: 14px;
  color: #666;
}

.price-compare {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.original-price {
  font-size: 13px;
  color: #bbb;
  text-decoration: line-through;
}

.total-price {
  font-size: 26px;
  font-weight: 700;
  color: #c21500;
  line-height: 1;
}

.action-btns {
  display: flex;
  gap: 10px;
}

.btn-cancel {
  padding: 12px 28px;
  background: #fafafa;
  border: 1px solid #ddd;
  border-radius: 6px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #f0f0f0;
}

.btn-confirm {
  padding: 12px 36px;
  background: #c21500;
  border: none;
  border-radius: 6px;
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-confirm:hover:not(:disabled) {
  background: #a30000;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(194, 21, 0, 0.3);
}

.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.region-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
</style>
