<template>
  <div class="address-page">
    <div class="page-header">
      <h1>收货地址管理</h1>
    </div>

    <div class="address-container">
      <div class="add-address-btn" @click="openAddModal">
        <span class="add-icon">+</span>
        <span class="add-text">添加新地址</span>
      </div>

      <div v-if="addresses.length === 0" class="empty-state">
        <span class="empty-icon">📍</span>
        <p>暂无收货地址</p>
        <p class="empty-hint">点击上方按钮添加您的收货地址</p>
      </div>

      <div v-else class="address-list">
        <div 
          v-for="address in addresses" 
          :key="address.id" 
          class="address-card"
          :class="{ 'default': address.isDefault }"
        >
          <div class="address-header">
            <span class="address-name">{{ address.name }}</span>
            <span class="address-phone">{{ address.phone }}</span>
            <span v-if="address.isDefault" class="default-badge">默认地址</span>
          </div>
          <div class="address-body">
            <span class="address-icon">📍</span>
            <span class="address-text">
              {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
            </span>
          </div>
          <div class="address-actions">
            <button class="action-btn edit" @click="editAddress(address)">编辑</button>
            <button class="action-btn delete" @click="deleteAddress(address.id)">删除</button>
            <button v-if="!address.isDefault" class="action-btn default" @click="setDefault(address.id)">设为默认</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑地址' : '添加新地址' }}</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        
        <form @submit.prevent="handleSubmit" class="address-form">
          <div class="form-row">
            <div class="form-item">
              <label>收货人</label>
              <input 
                v-model="formData.name" 
                type="text" 
                placeholder="请输入收货人姓名"
                required
              />
            </div>
            <div class="form-item">
              <label>联系电话</label>
              <input 
                v-model="formData.phone" 
                type="tel" 
                placeholder="请输入联系电话"
                required
              />
            </div>
          </div>

          <div class="form-row">
            <div class="form-item">
              <label>省份</label>
              <select v-model="formData.province" @change="onProvinceChange" required>
                <option value="">请选择省份</option>
                <option v-for="province in provinces" :key="province" :value="province">{{ province }}</option>
              </select>
            </div>
            <div class="form-item">
              <label>城市</label>
              <select v-model="formData.city" @change="onCityChange" required>
                <option value="">请选择城市</option>
                <option v-for="city in cities" :key="city" :value="city">{{ city }}</option>
              </select>
            </div>
            <div class="form-item">
              <label>区/县</label>
              <select v-model="formData.district" required>
                <option value="">请选择区/县</option>
                <option v-for="district in districts" :key="district" :value="district">{{ district }}</option>
              </select>
            </div>
          </div>

          <div class="form-item">
            <label>详细地址</label>
            <textarea 
              v-model="formData.detail" 
              placeholder="请输入详细地址"
              required
            ></textarea>
          </div>

          <div class="form-item checkbox-item">
            <label class="checkbox-label">
              <input type="checkbox" v-model="formData.isDefault" />
              <span class="checkbox-text">设为默认地址</span>
            </label>
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
            <button type="submit" class="btn btn-primary">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { addressAPI } from '../api'

const addresses = ref([])
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref(null)

const provinces = ['山西省', '北京市', '上海市', '广东省', '浙江省', '江苏省']
const cityMap = {
  '山西省': ['太原市', '大同市', '阳泉市', '长治市', '晋城市', '朔州市', '晋中市', '运城市', '忻州市', '临汾市', '吕梁市'],
  '北京市': ['东城区', '西城区', '朝阳区', '海淀区', '丰台区', '石景山区', '通州区', '顺义区', '昌平区', '大兴区'],
  '上海市': ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区', '杨浦区', '浦东新区', '闵行区', '宝山区'],
  '广东省': ['广州市', '深圳市', '珠海市', '汕头市', '佛山市', '韶关市', '湛江市', '肇庆市', '江门市', '茂名市'],
  '浙江省': ['杭州市', '宁波市', '温州市', '嘉兴市', '湖州市', '绍兴市', '金华市', '衢州市', '舟山市', '台州市'],
  '江苏省': ['南京市', '无锡市', '徐州市', '常州市', '苏州市', '南通市', '连云港市', '淮安市', '盐城市', '扬州市']
}

const districtMap = {
  '太原市': ['小店区', '迎泽区', '杏花岭区', '尖草坪区', '万柏林区', '晋源区', '清徐县', '阳曲县', '娄烦县', '古交市'],
  '大同市': ['平城区', '云冈区', '新荣区', '云州区', '阳高县', '天镇县', '广灵县', '灵丘县', '浑源县', '左云县'],
  '东城区': ['东华门街道', '景山街道', '交道口街道', '安定门街道', '北新桥街道'],
  '广州市': ['天河区', '越秀区', '荔湾区', '海珠区', '白云区', '黄埔区', '番禺区', '花都区', '南沙区', '增城区'],
  '深圳市': ['福田区', '罗湖区', '南山区', '宝安区', '龙岗区', '盐田区', '龙华区', '坪山区', '光明区', '大鹏新区'],
  '杭州市': ['上城区', '下城区', '西湖区', '滨江区', '拱墅区', '萧山区', '余杭区', '富阳区', '临安区', '建德市'],
  '南京市': ['玄武区', '秦淮区', '鼓楼区', '建邺区', '栖霞区', '雨花台区', '江宁区', '浦口区', '六合区', '溧水区'],
  '徐汇区': ['湖南路街道', '天平路街道', '枫林路街道', '漕河泾街道', '康健新村街道'],
  '浦东新区': ['陆家嘴街道', '潍坊新村街道', '花木街道', '金桥街道', '张江镇'],
  '其他': ['城区', '开发区', '新区', '工业园区']
}

const formData = reactive({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

const cities = ref([])
const districts = ref([])

const loadAddresses = async () => {
  try {
    const response = await addressAPI.getAll()
    addresses.value = response.data
  } catch (error) {
    console.error('加载地址失败:', error)
    ElMessage.error('加载地址失败')
  }
}

const onProvinceChange = () => {
  cities.value = cityMap[formData.province] || []
  formData.city = ''
  districts.value = []
  formData.district = ''
}

const onCityChange = () => {
  districts.value = districtMap[formData.city] || districtMap['其他'] || []
  formData.district = ''
}

const openAddModal = () => {
  isEditing.value = false
  editingId.value = null
  resetForm()
  showModal.value = true
}

const editAddress = (address) => {
  isEditing.value = true
  editingId.value = address.id
  formData.name = address.name
  formData.phone = address.phone
  formData.province = address.province
  formData.city = address.city
  formData.district = address.district
  formData.detail = address.detail
  formData.isDefault = address.isDefault || false
  
  onProvinceChange()
  onCityChange()
  
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  isEditing.value = false
  editingId.value = null
  resetForm()
}

const resetForm = () => {
  formData.name = ''
  formData.phone = ''
  formData.province = ''
  formData.city = ''
  formData.district = ''
  formData.detail = ''
  formData.isDefault = false
  cities.value = []
  districts.value = []
}

const handleSubmit = async () => {
  try {
    const data = {
      name: formData.name.trim(),
      phone: formData.phone.trim(),
      province: formData.province,
      city: formData.city,
      district: formData.district,
      detail: formData.detail.trim(),
      isDefault: formData.isDefault
    }

    if (isEditing.value) {
      await addressAPI.update(editingId.value, data)
    } else {
      await addressAPI.create(data)
    }

    ElMessage.success(isEditing.value ? '地址更新成功' : '地址添加成功')
    closeModal()
    loadAddresses()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败')
  }
}

const deleteAddress = async (id) => {
  if (!confirm('确定要删除这个地址吗？')) return

  try {
    await addressAPI.delete(id)
    ElMessage.success('删除成功')
    loadAddresses()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

const setDefault = async (id) => {
  try {
    await addressAPI.setDefault(id)
    ElMessage.success('设置成功')
    loadAddresses()
  } catch (error) {
    console.error('设置失败:', error)
    ElMessage.error('设置失败')
  }
}

onMounted(() => {
  loadAddresses()
})
</script>

<style scoped>
.address-page {
  padding: 40px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #4a3728;
  margin: 0;
}

.address-container {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.add-address-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px;
  border: 2px dashed #ddd;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 20px;
}

.add-address-btn:hover {
  border-color: #8B4513;
  background: #FAF8F5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.1);
}

.add-icon {
  font-size: 24px;
  color: #8B4513;
  transition: transform 0.3s var(--ease-bounce);
}

.add-address-btn:hover .add-icon {
  transform: scale(1.2) rotate(90deg);
}

.add-text {
  font-size: 16px;
  color: #666;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  margin: 8px 0;
  color: #666;
}

.empty-hint {
  font-size: 14px;
  color: #999;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-card {
  border: 2px solid #eee;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
  animation: addressCardIn 0.4s ease forwards;
}

.address-card:nth-child(1) { animation-delay: 0.1s; }
.address-card:nth-child(2) { animation-delay: 0.15s; }
.address-card:nth-child(3) { animation-delay: 0.2s; }
.address-card:nth-child(4) { animation-delay: 0.25s; }

@keyframes addressCardIn {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.address-card:hover {
  border-color: #8B4513;
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(139, 69, 19, 0.1);
}

.address-card.default {
  border-color: #8B4513;
  background: #FAF8F5;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.address-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.address-phone {
  font-size: 14px;
  color: #666;
}

.default-badge {
  font-size: 12px;
  color: #fff;
  background: #ffc107;
  padding: 2px 8px;
  border-radius: 4px;
  margin-left: auto;
}

.address-body {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 16px;
}

.address-icon {
  font-size: 16px;
}

.address-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

.address-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.action-btn {
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn.edit {
  background: #f0f0f0;
  color: #666;
}

.action-btn.edit:hover {
  background: #e0e0e0;
}

.action-btn.delete {
  background: #fef0f0;
  color: #dc3545;
}

.action-btn.delete:hover {
  background: #fee2e2;
}

.action-btn.default {
  background: #fff3cd;
  color: #856404;
}

.action-btn.default:hover {
  background: #ffeeba;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  font-size: 24px;
  color: #999;
  cursor: pointer;
  background: none;
  border: none;
  padding: 0;
  line-height: 1;
}

.address-form {
  padding: 24px;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.form-item {
  flex: 1;
}

.form-item label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
}

.form-item input,
.form-item select,
.form-item textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #eee;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.form-item input:focus,
.form-item select:focus,
.form-item textarea:focus {
  outline: none;
  border-color: #8B4513;
}

.form-item textarea {
  resize: vertical;
  min-height: 80px;
}

.checkbox-item {
  margin-bottom: 16px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-text {
  font-size: 14px;
  color: #666;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 24px;
}

.btn {
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.btn-secondary {
  background: #f0f0f0;
  color: #666;
}

.btn-secondary:hover {
  background: #e0e0e0;
}

.btn-primary {
  background: linear-gradient(135deg, #8B4513, #A0522D);
  color: #fff;
}

.btn-primary:hover {
  background: linear-gradient(135deg, #A0522D, #8B4513);
}
</style>