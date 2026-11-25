<template>
  <div class="pg">
    <div class="pg-wrap">
      <div class="crumb"><router-link to="/">首页</router-link><i>/</i><router-link to="/profile">个人中心</router-link><i>/</i><span>收货地址</span></div>
      <div class="pg-head">
        <div>
          <h1>收货地址</h1>
          <div class="sub">共 {{ addresses.length }} 个地址 · 结算时默认选中第一个</div>
        </div>
        <button class="btn-gold sm" @click="openAdd">+ 新增地址</button>
      </div>

      <div class="alist" v-if="addresses.length">
        <div class="acard" v-for="a in addresses" :key="a.id" :class="{ on: a.isDefault }">
          <div class="ah">
            <b>{{ a.name }}</b>
            <span>{{ maskPhone(a.phone) }}</span>
            <span class="tag gold" v-if="a.isDefault">默认</span>
          </div>
          <div class="aw">{{ a.province }}{{ a.city }}{{ a.district || '' }}{{ a.detail }}</div>
          <div class="af">
            <button class="op" v-if="!a.isDefault" @click="setDefault(a.id)">设为默认</button>
            <button class="op" @click="edit(a)">编辑</button>
            <button class="op danger" @click="remove(a.id)">删除</button>
          </div>
        </div>
      </div>
      <EmptyState
        v-else
        icon="◌"
        title="还没有收货地址"
        description="添加一个地址，下单时就不用重复填写了"
        action-text="新增地址"
        @action="openAdd"
      />
    </div>

    <el-dialog v-model="showModal" :title="editingId ? '编辑地址' : '新增地址'" width="520px" class="nv-dialog">
      <el-form :model="form" label-position="top">
        <div class="grid2">
          <el-form-item label="收货人"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        </div>
        <div class="grid3">
          <el-form-item label="省份">
            <el-select v-model="form.province" placeholder="省份" @change="onProvinceChange">
              <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
            </el-select>
          </el-form-item>
          <el-form-item label="城市">
            <el-select v-model="form.city" placeholder="城市" @change="onCityChange">
              <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item label="区县">
            <el-select v-model="form.district" placeholder="区县">
              <el-option v-for="d in districts" :key="d" :label="d" :value="d" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="详细地址"><el-input v-model="form.detail" type="textarea" :rows="2" /></el-form-item>
        <el-form-item><el-checkbox v-model="form.isDefault">设为默认地址</el-checkbox></el-form-item>
      </el-form>
      <template #footer>
        <button class="dlg-btn" @click="showModal = false">取消</button>
        <button class="dlg-btn gold" @click="submit">保存</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { addressAPI } from '../api'
import { ElMessage } from 'element-plus'
import EmptyState from '../components/common/EmptyState.vue'

const addresses = ref([])
const showModal = ref(false)
const editingId = ref(null)

const provinces = ['北京市', '上海市', '天津市', '重庆市', '浙江省', '江苏省', '广东省', '山东省', '河南省', '湖北省', '湖南省', '四川省', '福建省', '安徽省', '陕西省', '辽宁省', '山西省', '河北省']
const cityMap = {
  北京市: ['东城区', '西城区', '朝阳区', '海淀区', '丰台区'],
  上海市: ['黄浦区', '徐汇区', '长宁区', '静安区', '浦东新区'],
  天津市: ['和平区', '河东区', '河西区', '南开区'],
  重庆市: ['渝中区', '江北区', '沙坪坝区', '九龙坡区'],
  浙江省: ['杭州市', '宁波市', '温州市', '嘉兴市', '绍兴市'],
  江苏省: ['南京市', '苏州市', '无锡市', '常州市'],
  广东省: ['广州市', '深圳市', '佛山市', '东莞市'],
  山东省: ['济南市', '青岛市', '烟台市', '潍坊市'],
  河南省: ['郑州市', '洛阳市'],
  湖北省: ['武汉市', '宜昌市'],
  湖南省: ['长沙市', '株洲市'],
  四川省: ['成都市', '绵阳市'],
  福建省: ['福州市', '厦门市', '泉州市'],
  安徽省: ['合肥市', '芜湖市'],
  陕西省: ['西安市', '宝鸡市'],
  辽宁省: ['沈阳市', '大连市'],
  山西省: ['太原市', '大同市'],
  河北省: ['石家庄市', '唐山市']
}
const districtMap = {
  杭州市: ['西湖区', '拱墅区', '上城区', '余杭区', '滨江区'],
  深圳市: ['南山区', '福田区', '罗湖区', '宝安区', '龙岗区'],
  广州市: ['天河区', '越秀区', '海珠区', '白云区'],
  成都市: ['武侯区', '锦江区', '青羊区', '金牛区']
}

const form = reactive({ name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })
const cities = ref([])
const districts = ref([])

const maskPhone = (p) => (p ? String(p).replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '')

async function load() {
  try {
    const res = await addressAPI.getAll()
    addresses.value = res.data || []
  } catch {}
}

function onProvinceChange() {
  cities.value = cityMap[form.province] || ['市辖区']
  districts.value = []
  form.city = ''
  form.district = ''
}
function onCityChange() {
  districts.value = districtMap[form.city] || ['城区', '新区', '开发区']
  form.district = ''
}

function openAdd() {
  editingId.value = null
  Object.assign(form, { name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })
  cities.value = []
  districts.value = []
  showModal.value = true
}

function edit(a) {
  editingId.value = a.id
  Object.assign(form, { name: a.name, phone: a.phone, province: a.province, city: a.city, district: a.district || '', detail: a.detail, isDefault: !!a.isDefault })
  cities.value = cityMap[a.province] || []
  districts.value = districtMap[a.city] || []
  showModal.value = true
}

async function submit() {
  if (!form.name || !form.phone || !form.detail) { ElMessage.warning('请填写完整地址信息'); return }
  try {
    if (editingId.value) await addressAPI.update(editingId.value, { ...form })
    else await addressAPI.create({ ...form })
    ElMessage.success('已保存')
    showModal.value = false
    load()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  }
}

async function remove(id) {
  try {
    await addressAPI.delete(id)
    ElMessage.success('已删除')
    load()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function setDefault(id) {
  try {
    await addressAPI.setDefault(id)
    load()
  } catch {
    ElMessage.error('设置失败')
  }
}

onMounted(load)
</script>

<style scoped>
.alist { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; }
.acard { border: 1px solid var(--color-line); background: var(--color-surface); padding: 18px 20px; }
.acard.on { border-color: var(--color-gold-line); }
.ah { display: flex; align-items: center; gap: 12px; }
.ah b { font-family: var(--font-display); font-size: 14px; font-weight: 400; letter-spacing: .06em; }
.ah span { font-size: 10.5px; color: var(--color-ink-muted); }
.aw { margin-top: 10px; font-size: 11px; line-height: 1.9; color: var(--color-ink-muted); letter-spacing: .03em; }
.af { margin-top: 14px; padding-top: 12px; border-top: 1px solid var(--color-line); display: flex; gap: 16px; }
.op { background: none; border: 0; font-family: inherit; font-size: 10px; letter-spacing: .08em; color: var(--color-ink-muted); cursor: pointer; padding: 0; }
.op:hover { color: var(--color-gold); }
.op.danger:hover { color: var(--color-danger); }

.grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 0 14px; }
.grid3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 0 12px; }

.nv-dialog :deep(.el-dialog) { background: var(--color-surface); border: 1px solid var(--color-line); }
.nv-dialog :deep(.el-dialog__title) { font-family: var(--font-display); letter-spacing: .12em; color: var(--color-ink); }
.nv-dialog :deep(.el-form-item__label) { font-size: 10.5px; letter-spacing: .12em; color: var(--color-ink-muted); }
.nv-dialog :deep(.el-input__wrapper) { background: var(--color-bg); box-shadow: 0 0 0 1px var(--color-line) inset; border-radius: 0; }
.nv-dialog :deep(.el-textarea__inner) { background: var(--color-bg); box-shadow: 0 0 0 1px var(--color-line) inset; border-radius: 0; color: var(--color-ink); }
.dlg-btn {
  height: 34px; padding: 0 18px; border: 1px solid var(--color-line-strong); background: transparent;
  color: var(--color-ink-soft); font-family: inherit; font-size: 11px; letter-spacing: .1em; cursor: pointer; margin-left: 8px;
}
.dlg-btn.gold { background: var(--color-gold); border-color: var(--color-gold); color: #14150f; }

@media (max-width: 900px) {
  .alist { grid-template-columns: 1fr; }
  .grid3 { grid-template-columns: 1fr; }
}
</style>