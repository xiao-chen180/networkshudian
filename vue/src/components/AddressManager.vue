<template>
  <div v-if="show" class="address-manager">
    <div class="am-header">
      <h2>收货地址管理</h2>
      <button class="add-btn" @click="openAddModal">
        <span class="add-icon">+</span>
        <span>添加新地址</span>
      </button>
    </div>

    <div class="address-grid">
      <div 
        v-for="addr in addresses" 
        :key="addr.id"
        class="address-card"
      >
        <div class="card-header">
          <div class="contact">
            <span class="name">{{ addr.name }}</span>
            <span class="phone">{{ addr.phone }}</span>
          </div>
          <div v-if="addr.isDefault" class="default-badge">默认地址</div>
        </div>
        <div class="card-body">
          <p class="full-address">
            <span class="location-icon">📍</span>
            {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}
          </p>
        </div>
        <div class="card-footer">
          <button class="btn-edit" @click="openEditModal(addr)">编辑</button>
          <button class="btn-delete" @click="handleDelete(addr.id)">删除</button>
        </div>
      </div>

      <div v-if="addresses.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <p class="empty-text">暂无收货地址</p>
        <button class="empty-btn" @click="openAddModal">立即添加</button>
      </div>
    </div>

    <el-dialog 
      :title="isEditing ? '编辑地址' : '添加地址'" 
      v-model="showModal" 
      width="520px"
      :close-on-click-modal="false"
    >
      <div class="form-container">
        <div class="form-row">
          <div class="form-item">
            <label class="form-label">收货人 <span class="required">*</span></label>
            <input 
              v-model="formData.name" 
              type="text" 
              class="form-input" 
              placeholder="请输入收货人姓名"
              maxlength="20"
            />
          </div>
          <div class="form-item">
            <label class="form-label">手机号 <span class="required">*</span></label>
            <input 
              v-model="formData.phone" 
              type="tel" 
              class="form-input" 
              placeholder="请输入手机号码"
              maxlength="11"
            />
          </div>
        </div>

        <div class="form-row">
          <label class="form-label full-width">所在地区 <span class="required">*</span></label>
          <div class="region-selectors">
            <select 
              v-model="formData.province" 
              class="region-select"
              @change="onProvinceChange"
            >
              <option value="">请选择省份</option>
              <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
            </select>
            <select 
              v-model="formData.city" 
              class="region-select"
              @change="onCityChange"
            >
              <option value="">请选择城市</option>
              <option v-for="c in cityList" :key="c" :value="c">{{ c }}</option>
            </select>
            <select 
              v-model="formData.district" 
              class="region-select"
            >
              <option value="">请选择区/县</option>
              <option v-for="d in districtList" :key="d" :value="d">{{ d }}</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-item full-width">
            <label class="form-label">详细地址 <span class="required">*</span></label>
            <textarea 
              v-model="formData.detail" 
              class="form-textarea" 
              placeholder="请输入详细地址"
              rows="3"
              maxlength="200"
            ></textarea>
          </div>
        </div>

        <div class="form-row">
          <label class="checkbox-label">
            <input 
              v-model="formData.isDefault" 
              type="checkbox" 
              class="checkbox-input"
            />
            <span class="checkbox-text">设为默认地址</span>
          </label>
        </div>
      </div>

      <template #footer>
        <el-button @click="showModal = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { addressAPI } from '../api';
import { ElMessage } from 'element-plus';

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
});

const addresses = ref([]);
const showModal = ref(false);
const isEditing = ref(false);
const editingId = ref(null);

const provinces = [
  '北京市', '天津市', '河北省', '山西省', '内蒙古自治区', 
  '辽宁省', '吉林省', '黑龙江省', '上海市', '江苏省', 
  '浙江省', '安徽省', '福建省', '江西省', '山东省', 
  '河南省', '湖北省', '湖南省', '广东省', '广西壮族自治区', 
  '海南省', '重庆市', '四川省', '贵州省', '云南省', 
  '西藏自治区', '陕西省', '甘肃省', '青海省', '宁夏回族自治区', 
  '新疆维吾尔自治区', '香港特别行政区', '澳门特别行政区', '台湾省'
];

const cityData = {
  '北京市': ['东城区', '西城区', '朝阳区', '海淀区', '丰台区', '石景山区', '通州区', '顺义区', '昌平区', '大兴区', '房山区', '门头沟区', '怀柔区', '平谷区', '密云区', '延庆区'],
  '上海市': ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区', '杨浦区', '闵行区', '宝山区', '嘉定区', '浦东新区', '金山区', '松江区', '青浦区', '奉贤区', '崇明区'],
  '广东省': ['广州市', '深圳市', '佛山市', '东莞市', '中山市', '珠海市', '惠州市', '江门市', '湛江市', '汕头市', '揭阳市', '潮州市', '肇庆市', '清远市', '韶关市'],
  '浙江省': ['杭州市', '宁波市', '温州市', '嘉兴市', '湖州市', '绍兴市', '金华市', '衢州市', '舟山市', '台州市', '丽水市'],
  '江苏省': ['南京市', '苏州市', '无锡市', '常州市', '徐州市', '南通市', '连云港市', '淮安市', '盐城市', '扬州市', '镇江市', '泰州市', '宿迁市'],
  '山东省': ['济南市', '青岛市', '淄博市', '枣庄市', '东营市', '烟台市', '潍坊市', '济宁市', '泰安市', '威海市', '日照市', '临沂市'],
  '河南省': ['郑州市', '开封市', '洛阳市', '平顶山市', '安阳市', '鹤壁市', '新乡市', '焦作市', '濮阳市', '许昌市', '漯河市', '三门峡市'],
  '湖北省': ['武汉市', '黄石市', '十堰市', '宜昌市', '襄阳市', '鄂州市', '荆门市', '孝感市', '荆州市', '黄冈市', '咸宁市'],
  '湖南省': ['长沙市', '株洲市', '湘潭市', '衡阳市', '邵阳市', '岳阳市', '常德市', '张家界市', '益阳市', '郴州市', '永州市'],
  '四川省': ['成都市', '自贡市', '攀枝花市', '泸州市', '德阳市', '绵阳市', '广元市', '遂宁市', '内江市', '乐山市', '南充市'],
  '重庆市': ['渝中区', '大渡口区', '江北区', '沙坪坝区', '九龙坡区', '南岸区', '北碚区', '渝北区', '巴南区', '长寿区', '江津区'],
  '天津市': ['和平区', '河东区', '河西区', '南开区', '河北区', '红桥区', '东丽区', '西青区', '津南区', '北辰区', '武清区'],
};

const formData = reactive({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
});

const cityList = ref(['请选择城市']);
const districtList = ref(['请选择区/县']);

const loadAddresses = async () => {
  if (!localStorage.getItem('token')) return;
  try {
    const response = await addressAPI.getAll();
    addresses.value = response.data;
  } catch (error) {
    console.error('加载地址失败:', error);
  }
};

const openAddModal = () => {
  isEditing.value = false;
  editingId.value = null;
  resetForm();
  cityList.value = ['请选择城市'];
  districtList.value = ['请选择区/县'];
  showModal.value = true;
};

const openEditModal = (addr) => {
  isEditing.value = true;
  editingId.value = addr.id;
  formData.name = addr.name;
  formData.phone = addr.phone;
  formData.province = addr.province;
  formData.city = addr.city;
  formData.district = addr.district;
  formData.detail = addr.detail;
  formData.isDefault = addr.isDefault;
  
  if (addr.province) {
    cityList.value = cityData[addr.province] || ['市辖区', '城区', '开发区', '新区'];
  }
  if (addr.city) {
    districtList.value = ['东城区', '西城区', '朝阳区', '海淀区', '丰台区', '石景山区', '门头沟区', '房山区', '通州区', '顺义区', '昌平区', '大兴区', '怀柔区', '平谷区', '密云区', '延庆区'];
  }
  
  showModal.value = true;
};

const resetForm = () => {
  formData.name = '';
  formData.phone = '';
  formData.province = '';
  formData.city = '';
  formData.district = '';
  formData.detail = '';
  formData.isDefault = false;
};

const onProvinceChange = () => {
  const province = formData.province;
  if (province) {
    cityList.value = cityData[province] || ['市辖区', '城区', '开发区', '新区'];
  } else {
    cityList.value = ['请选择城市'];
  }
  formData.city = '';
  formData.district = '';
  districtList.value = ['请选择区/县'];
};

const onCityChange = () => {
  const city = formData.city;
  if (city) {
    districtList.value = ['东城区', '西城区', '朝阳区', '海淀区', '丰台区', '石景山区', '门头沟区', '房山区', '通州区', '顺义区', '昌平区', '大兴区', '怀柔区', '平谷区', '密云区', '延庆区'];
  } else {
    districtList.value = ['请选择区/县'];
  }
  formData.district = '';
};

const validateForm = () => {
  if (!formData.name.trim()) {
    ElMessage.warning('请输入收货人姓名');
    return false;
  }
  if (!formData.phone.trim()) {
    ElMessage.warning('请输入手机号码');
    return false;
  }
  if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
    ElMessage.warning('请输入正确的手机号码');
    return false;
  }
  if (!formData.province) {
    ElMessage.warning('请选择省份');
    return false;
  }
  if (!formData.city) {
    ElMessage.warning('请选择城市');
    return false;
  }
  if (!formData.district) {
    ElMessage.warning('请选择区/县');
    return false;
  }
  if (!formData.detail.trim()) {
    ElMessage.warning('请输入详细地址');
    return false;
  }
  return true;
};

const handleSubmit = async () => {
  if (!validateForm()) return;

  try {
    const data = {
      name: formData.name.trim(),
      phone: formData.phone.trim(),
      province: formData.province,
      city: formData.city,
      district: formData.district,
      detail: formData.detail.trim(),
      isDefault: formData.isDefault
    };
    
    console.log('准备发送的数据:', data);
    console.log('Token:', localStorage.getItem('token'));

    if (isEditing.value) {
      const response = await addressAPI.update(editingId.value, data);
      console.log('更新成功:', response);
      ElMessage.success('地址更新成功');
    } else {
      const response = await addressAPI.create(data);
      console.log('创建成功:', response);
      ElMessage.success('地址添加成功');
    }

    showModal.value = false;
    loadAddresses();
  } catch (error) {
    console.error('保存失败:', error);
    console.error('错误响应:', error.response);
    console.error('错误状态:', error.response?.status);
    console.error('错误数据:', error.response?.data);
    const msg = error.response?.data?.message || error.message || '保存失败';
    ElMessage.error(msg);
  }
};

const handleDelete = async (id) => {
  if (!confirm('确定要删除这个地址吗？')) return;
  
  try {
    await addressAPI.delete(id);
    ElMessage.success('地址删除成功');
    loadAddresses();
  } catch (error) {
    ElMessage.error('删除失败');
  }
};

loadAddresses();
</script>

<style scoped>
.address-manager {
  padding: 30px;
  max-width: 1000px;
  margin: 0 auto;
}

.am-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #d4c4a8;
}

.am-header h2 {
  font-family: 'STKaiti', 'KaiTi', serif;
  font-size: 26px;
  color: #6b4423;
  margin: 0;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(139, 90, 43, 0.4);
}

.add-icon {
  font-size: 20px;
  font-weight: bold;
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.address-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #eee;
}

.address-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #eee;
}

.contact {
  display: flex;
  gap: 15px;
  align-items: center;
}

.name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.phone {
  font-size: 14px;
  color: #888;
}

.default-badge {
  padding: 4px 12px;
  background: linear-gradient(135deg, #ffd700, #ffb700);
  border-radius: 20px;
  font-size: 12px;
  color: #8b4513;
  font-weight: 500;
}

.card-body {
  margin-bottom: 16px;
}

.full-address {
  margin: 0;
  font-size: 14px;
  color: #555;
  line-height: 1.6;
}

.location-icon {
  margin-right: 8px;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-edit, .btn-delete {
  padding: 8px 18px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-edit {
  background: rgba(139, 90, 43, 0.1);
  color: #8b5a2b;
}

.btn-edit:hover {
  background: #8b5a2b;
  color: white;
}

.btn-delete {
  background: rgba(194, 21, 0, 0.1);
  color: #c21500;
}

.btn-delete:hover {
  background: #c21500;
  color: white;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  background: rgba(255, 253, 245, 0.6);
  border: 2px dashed #d4c4a8;
  border-radius: 12px;
}

.empty-icon {
  font-size: 56px;
  display: block;
  margin-bottom: 16px;
}

.empty-text {
  color: #888;
  font-size: 16px;
  margin-bottom: 20px;
}

.empty-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, #8b5a2b, #a0522d);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.empty-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 90, 43, 0.4);
}

.form-container {
  padding: 10px 0;
}

.form-row {
  margin-bottom: 20px;
}

.form-item {
  flex: 1;
}

.form-item.full-width {
  width: 100%;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.form-label.full-width {
  margin-bottom: 12px;
}

.required {
  color: #c21500;
  margin-left: 4px;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #8b5a2b;
  box-shadow: 0 0 0 3px rgba(139, 90, 43, 0.1);
}

.form-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  resize: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-textarea:focus {
  outline: none;
  border-color: #8b5a2b;
  box-shadow: 0 0 0 3px rgba(139, 90, 43, 0.1);
}

.region-selectors {
  display: flex;
  gap: 12px;
}

.region-select {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.region-select:focus {
  outline: none;
  border-color: #8b5a2b;
  box-shadow: 0 0 0 3px rgba(139, 90, 43, 0.1);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-input {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.checkbox-text {
  font-size: 14px;
  color: #555;
}
</style>