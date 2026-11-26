<template>
  <div class="admin">
    <!-- 侧栏 -->
    <aside class="sb">
      <div class="logo"><b>书香阁</b><span>ADMIN</span></div>

      <div class="sbg">
        <h6>概览</h6>
        <a class="sbi" :class="{ on: activeMenu === 'overview' }" @click="activeMenu = 'overview'">
          <svg viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>
          数据概览
        </a>
      </div>

      <div class="sbg">
        <h6>内容</h6>
        <a class="sbi" :class="{ on: activeMenu === 'books' }" @click="activeMenu = 'books'">
          <svg viewBox="0 0 24 24"><path d="M4 4h11l5 5v11H4z"/><path d="M15 4v5h5"/></svg>
          图书管理 <em>{{ books.length }}</em>
        </a>
        <a class="sbi" :class="{ on: activeMenu === 'banners' }" @click="activeMenu = 'banners'">
      <svg viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="16"/><path d="M3 9h18M8 14h8"/></svg>
      运营 Banner
      </a>
      </div>

      <div class="sbg">
        <h6>交易</h6>
        <a class="sbi" :class="{ on: activeMenu === 'orders' }" @click="activeMenu = 'orders'">
          <svg viewBox="0 0 24 24"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/><path d="M3 6h18"/></svg>
          订单管理 <em v-if="pendingShip">{{ pendingShip }}</em>
        </a>
        <a class="sbi" :class="{ on: activeMenu === 'after-sales' }" @click="activeMenu = 'after-sales'">
          <svg viewBox="0 0 24 24"><path d="M3 12a9 9 0 1 0 3-6.7"/><path d="M3 4v5h5"/><path d="M12 8v4l3 2"/></svg>
          售后管理 <em v-if="pendingAfterSales">{{ pendingAfterSales }}</em>
        </a>
      </div>

      <div class="sbg">
        <h6>用户</h6>
        <a class="sbi" :class="{ on: activeMenu === 'users' }" @click="activeMenu = 'users'">
          <svg viewBox="0 0 24 24"><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></svg>
          用户管理
        </a>
      </div>

      <div class="bottom">
        <b>{{ adminName }}</b>
        <span>{{ adminAccount }}</span>
        <a @click="goShop">返回商城 →</a>
        <a @click="logout" class="out">退出登录</a>
      </div>
    </aside>

    <!-- 主区 -->
    <main class="mn">
      <div class="topbar">
        <div class="crumb">管理后台 <i>/</i> <span>{{ menuTitle }}</span></div>
        <div class="tb-right">
          <span class="sync" @click="loadData">刷新数据</span>
          <div class="who"><span class="av">{{ adminName.charAt(0) }}</span>{{ adminName }}</div>
        </div>
      </div>

      <div class="content">
        <!-- ============ 概览 ============ -->
        <template v-if="activeMenu === 'overview'">
          <div class="head">
            <div>
              <h1>数据概览</h1>
              <div class="sub">{{ today }} · 数据来自实时接口</div>
            </div>
          </div>

          <div class="kpis">
            <div class="kpi">
              <div class="l">TOTAL REVENUE</div>
              <div class="v">¥{{ fmt(stats.totalRevenue) }}</div>
              <div class="d">已支付与已完成订单合计</div>
            </div>
            <div class="kpi">
              <div class="l">ORDERS</div>
              <div class="v">{{ stats.totalOrders || 0 }}</div>
              <div class="d">待发货 {{ stats.paidOrders || 0 }} 笔</div>
            </div>
            <div class="kpi">
              <div class="l">USERS</div>
              <div class="v">{{ stats.totalUsers || 0 }}</div>
              <div class="d">注册用户总数</div>
            </div>
            <div class="kpi" :class="{ warn: lowStockBooks.length }">
              <div class="l">LOW STOCK</div>
              <div class="v">{{ lowStockBooks.length }}<em>种</em></div>
              <div class="d">{{ lowStockBooks.length ? '需要补货' : '库存充足' }}</div>
            </div>
          </div>

          <div class="charts">
            <div class="panel chart-main">
              <div class="panel-h"><h3>订单状态分布</h3><span class="plain">共 {{ stats.totalOrders || 0 }} 笔</span></div>
              <div class="status-bars">
                <div class="sbar" v-for="s in statusBars" :key="s.key">
                  <span class="sn">{{ s.label }}</span>
                  <span class="track"><i :style="{ width: s.percent + '%', background: s.color }"></i></span>
                  <span class="sv mono">{{ s.value }}</span>
                </div>
              </div>
            </div>

            <div class="panel chart-side">
              <div class="panel-h"><h3>销量 Top 5</h3><span class="plain">累计销量</span></div>
              <div class="tops">
                <div class="top" v-for="(b, i) in topBooks" :key="b.id">
                  <span class="no">{{ String(i + 1).padStart(2, '0') }}</span>
                  <span class="tn">{{ b.name }}</span>
                  <span class="tv mono">{{ b.salesCount || 0 }}</span>
                </div>
                <div class="blank" v-if="!topBooks.length">暂无销量数据</div>
              </div>
            </div>
          </div>

          <div class="bottom-grid">
            <div class="panel">
              <div class="panel-h"><h3>最新订单</h3><span class="act" @click="activeMenu = 'orders'">全部订单 →</span></div>
              <table class="tbl">
                <thead><tr><th style="width:130px">订单号</th><th>商品</th><th style="width:90px">金额</th><th style="width:80px">状态</th><th style="width:70px">操作</th></tr></thead>
                <tbody>
                  <tr v-for="o in recentOrders" :key="o.id">
                    <td class="mono">SG{{ String(o.id).padStart(8, '0') }}</td>
                    <td class="ellipsis">{{ (o.items || []).map(i => i.bookName).join(' / ') }}</td>
                    <td class="mono">¥{{ fmt(o.totalPrice) }}</td>
                    <td><OrderStatusBadge :status="o.status" /></td>
                    <td>
                      <button class="tbtn" v-if="o.status === 'PAID'" @click="ship(o.id)">发货</button>
                      <button class="tbtn plain" v-else @click="activeMenu = 'orders'">详情</button>
                    </td>
                  </tr>
                  <tr v-if="!recentOrders.length"><td colspan="5" class="blank">暂无订单</td></tr>
                </tbody>
              </table>
            </div>

            <div class="panel">
              <div class="panel-h"><h3>低库存预警</h3><span class="plain">库存 &lt; 10</span></div>
              <div class="lows" v-if="lowStockBooks.length">
                <div class="ls" v-for="b in lowStockBooks" :key="b.id">
                  <BookCover :src="b.imageUrl" :title="b.name" compact class="ls-cover" ratio="3:4" />
                  <div class="lsm">
                    <b>{{ b.name }}</b>
                    <span>¥{{ fmt(b.price) }}</span>
                  </div>
                  <div class="lsn"><b class="mono">{{ b.stock }}</b><span>件</span></div>
                </div>
              </div>
              <div class="blank" v-else>库存充足</div>
            </div>
          </div>
        </template>

        <!-- ============ 图书管理 ============ -->
        <template v-else-if="activeMenu === 'books'">
          <div class="head">
            <div>
              <h1>图书管理</h1>
              <div class="sub">共 {{ books.length }} 种在售 · 可搜索、筛选、调价</div>
            </div>
            <div class="head-acts">
              <button class="tbtn" @click="showPriceDialog = true">批量调价</button>
              <button class="tbtn gold" @click="openBook()">+ 上架新书</button>
            </div>
          </div>

          <div class="toolbar">
            <input v-model="bookSearch" class="inp" placeholder="搜索书名 / 作者 / ISBN" />
            <select v-model="bookCategoryFilter" class="sel">
              <option value="">全部分类</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
            <span class="count">筛选结果 {{ filteredBooks.length }} 种</span>
          </div>

          <div class="panel">
            <table class="tbl">
              <thead><tr><th style="width:52px">封面</th><th>书名 / 作者</th><th style="width:110px">分类</th><th style="width:90px">价格</th><th style="width:70px">库存</th><th style="width:70px">评分</th><th style="width:120px">操作</th></tr></thead>
              <tbody>
                <tr v-for="b in filteredBooks" :key="b.id">
                  <td><BookCover :src="b.imageUrl" :title="b.name" compact class="tc" ratio="3:4" /></td>
                  <td><span class="tt">{{ b.name }}</span><span class="ts">{{ b.author }} · {{ b.publisher }}</span></td>
                  <td class="muted">{{ b.categoryName || '—' }}</td>
                  <td class="mono gold">¥{{ fmt(b.price) }}</td>
                  <td class="mono" :class="{ warnText: b.stock < 10 }">{{ b.stock }}</td>
                  <td class="mono">{{ Number(b.avgRating || 0).toFixed(1) }}</td>
                  <td>
                    <button class="tbtn plain" @click="openBook(b)">编辑</button>
                    <button class="tbtn danger" @click="removeBook(b)">删除</button>
                  </td>
                </tr>
                <tr v-if="!filteredBooks.length"><td colspan="7" class="blank">没有匹配的图书</td></tr>
              </tbody>
            </table>
          </div>
        </template>

        <!-- ============ 订单管理 ============ -->
        <template v-else-if="activeMenu === 'orders'">
          <div class="head">
            <div>
              <h1>订单管理</h1>
              <div class="sub">共 {{ orders.length }} 笔 · 按状态筛选处理</div>
            </div>
          </div>

          <div class="tabs">
            <button v-for="f in orderFilters" :key="f.key" :class="{ on: orderFilter === f.key }" @click="orderFilter = f.key">
              {{ f.label }}<em>{{ f.count }}</em>
            </button>
          </div>

          <div class="panel">
            <table class="tbl">
              <thead><tr><th style="width:130px">订单号</th><th>商品</th><th style="width:80px">件数</th><th style="width:90px">金额</th><th style="width:80px">状态</th><th style="width:130px">操作</th></tr></thead>
              <tbody>
                <tr v-for="o in filteredOrders" :key="o.id">
                  <td class="mono">SG{{ String(o.id).padStart(8, '0') }}</td>
                  <td class="ellipsis">{{ (o.items || []).map(i => `${i.bookName} ×${i.quantity}`).join('，') }}</td>
                  <td class="mono">{{ totalQty(o) }}</td>
                  <td class="mono gold">¥{{ fmt(o.totalPrice) }}</td>
                  <td><OrderStatusBadge :status="o.status" /></td>
                  <td>
                    <button class="tbtn" v-if="o.status === 'PAID'" @click="ship(o.id)">发货</button>
                    <button class="tbtn" v-if="o.status === 'SHIPPED'" @click="complete(o.id)">完成</button>
                    <button class="tbtn danger" v-if="o.status === 'PENDING' || o.status === 'PAID'" @click="cancel(o.id)">取消</button>
                    <span class="muted" v-if="o.status === 'COMPLETED' || o.status === 'CANCELLED'">—</span>
                  </td>
                </tr>
                <tr v-if="!filteredOrders.length"><td colspan="6" class="blank">该状态下暂无订单</td></tr>
              </tbody>
            </table>
          </div>
        </template>

        <!-- ============ 运营 Banner ============ -->
        <template v-else-if="activeMenu === 'banners'">
          <div class="head">
            <div>
              <h1>运营 Banner</h1>
              <div class="sub">首页横幅配置 · 当前 {{ banners.filter(b => b.status === 'ACTIVE').length }} 条生效</div>
            </div>
            <button class="tbtn" @click="openBanner(null)">+ 新建 Banner</button>
          </div>

          <div class="panel">
            <table class="tbl">
              <thead><tr>
                <th style="width:60px">排序</th>
                <th>标题 / 副标题</th>
                <th style="width:200px">配图</th>
                <th style="width:90px">关联图书</th>
                <th style="width:90px">状态</th>
                <th style="width:200px">操作</th>
              </tr></thead>
              <tbody>
                <tr v-for="b in banners" :key="b.id">
                  <td class="mono">{{ b.sortOrder }}</td>
                  <td>
                    <div class="tt">{{ b.title }}</div>
                    <div class="ts">{{ b.subtitle || '—' }}</div>
                  </td>
                  <td>
                    <img v-if="b.imageUrl" :src="b.imageUrl" class="bn-thumb" alt="" />
                    <span v-else class="muted">无配图</span>
                  </td>
                  <td class="mono">{{ b.bookId ? '#' + b.bookId : '—' }}</td>
                  <td><span class="tag" :class="{ gold: b.status === 'ACTIVE' }">{{ b.status === 'ACTIVE' ? '生效中' : '已下线' }}</span></td>
                  <td>
                    <button class="tbtn" @click="openBanner(b)">编辑</button>
                    <button class="tbtn plain" @click="toggleBanner(b)">{{ b.status === 'ACTIVE' ? '下线' : '上线' }}</button>
                    <button class="tbtn danger" @click="removeBanner(b)">删除</button>
                  </td>
                </tr>
                <tr v-if="!banners.length"><td colspan="6" class="blank">暂无 Banner，点击右上角新建</td></tr>
              </tbody>
            </table>
          </div>
        </template>
        <!-- ============ 售后管理 ============ -->
        <template v-else-if="activeMenu === 'after-sales'">
          <div class="head">
            <div>
              <h1>售后管理</h1>
              <div class="sub">共 {{ afterSales.length }} 条申请 · 待处理 {{ pendingAfterSales }} 条</div>
            </div>
          </div>

          <div class="panel">
            <table class="tbl">
              <thead><tr>
                <th style="width:60px">编号</th>
                <th style="width:130px">订单号</th>
                <th style="width:100px">用户</th>
                <th style="width:90px">类型</th>
                <th>问题描述</th>
                <th style="width:90px">金额</th>
                <th style="width:90px">状态</th>
                <th style="width:160px">操作</th>
              </tr></thead>
              <tbody>
                <tr v-for="a in afterSales" :key="a.id">
                  <td class="mono">#{{ a.id }}</td>
                  <td class="mono">{{ a.orderNo }}</td>
                  <td>{{ a.username || '—' }}</td>
                  <td><span class="tag">{{ a.typeText }}</span></td>
                  <td>
                    <div class="tt">{{ a.reason || '—' }}</div>
                    <div class="ts" v-if="a.description">{{ a.description }}</div>
                    <div class="ts reply" v-if="a.adminRemark">回复：{{ a.adminRemark }}</div>
                  </td>
                  <td class="mono">¥{{ fmt(a.refundAmount) }}</td>
                  <td><span class="tag" :class="afterSaleTag(a.status)">{{ a.statusText }}</span></td>
                  <td>
                    <template v-if="a.status === 'PENDING'">
                      <button class="tbtn" @click="openHandle(a, 'APPROVED')">通过</button>
                      <button class="tbtn danger" @click="openHandle(a, 'REJECTED')">驳回</button>
                    </template>
                    <button class="tbtn plain" v-else-if="a.status === 'APPROVED'" @click="openHandle(a, 'COMPLETED')">标记完成</button>
                    <span class="muted" v-else>—</span>
                  </td>
                </tr>
                <tr v-if="!afterSales.length"><td colspan="8" class="blank">暂无售后申请</td></tr>
              </tbody>
            </table>
          </div>
        </template>

        <!-- ============ 用户管理 ============ -->
        <template v-else>
          <div class="head">
            <div>
              <h1>用户管理</h1>
              <div class="sub">共 {{ users.length }} 位 · 可调整角色与折扣</div>
            </div>
          </div>

          <div class="panel">
            <table class="tbl">
              <thead><tr><th style="width:60px">头像</th><th>昵称 / 账号</th><th style="width:80px">角色</th><th style="width:80px">折扣</th><th style="width:70px">订单</th><th style="width:120px">操作</th></tr></thead>
              <tbody>
                <tr v-for="u in users" :key="u.id">
                  <td><span class="uav">{{ (u.username || u.account || '读').charAt(0) }}</span></td>
                  <td><span class="tt">{{ u.username || '未命名' }}</span><span class="ts">{{ u.account }}</span></td>
                  <td><span class="tag" :class="{ gold: u.role === 'ADMIN' }">{{ u.role }}</span></td>
                  <td class="mono">{{ u.discount ? `${(Number(u.discount) * 10).toFixed(1).replace('.0', '')} 折` : '—' }}</td>
                  <td class="mono">{{ u.orderCount ?? 0 }}</td>
                  <td>
                    <button class="tbtn" v-if="u.role !== 'ADMIN'" @click="setRole(u, 'ADMIN')">设为管理员</button>
                    <button class="tbtn plain" v-else @click="setRole(u, 'USER')">取消管理员</button>
                  </td>
                </tr>
                <tr v-if="!users.length"><td colspan="6" class="blank">暂无用户</td></tr>
              </tbody>
            </table>
          </div>
        </template>
      </div>
    </main>

    <!-- 售后处理 -->
    <el-dialog v-model="showAfterSaleDialog" :title="afterSaleAction === 'APPROVED' ? '通过售后申请' : afterSaleAction === 'REJECTED' ? '驳回售后申请' : '标记售后完成'" width="440px" class="nv-dialog">
      <div class="as-dlg">
        <p class="as-dlg-tip">
          {{ afterSaleCurrent?.orderNo }} · {{ afterSaleCurrent?.typeText }} · ¥{{ fmt(afterSaleCurrent?.refundAmount) }}
        </p>
        <el-input v-model="afterSaleRemark" type="textarea" :rows="3" maxlength="200" show-word-limit
                  :placeholder="afterSaleAction === 'REJECTED' ? '请说明驳回原因（会展示给用户）' : '可填写处理说明（会展示给用户）'" />
      </div>
      <template #footer>
        <button class="tbtn plain" @click="showAfterSaleDialog = false">取消</button>
        <button class="tbtn" :disabled="afterSaleSubmitting" @click="submitAfterSaleHandle">
          {{ afterSaleSubmitting ? '提交中…' : '确认' }}
        </button>
      </template>
    </el-dialog>

    <!-- Banner 编辑 -->
    <el-dialog v-model="showBannerDialog" :title="editingBanner.id ? '编辑 Banner' : '新建 Banner'" width="520px" class="nv-dialog">
      <el-form :model="editingBanner" label-position="top">
        <el-form-item label="标题">
          <el-input v-model="editingBanner.title" maxlength="40" show-word-limit placeholder="例如：本周主推 · 深夜书房" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="editingBanner.subtitle" maxlength="80" show-word-limit placeholder="一句话说明，可留空" />
        </el-form-item>
        <div class="g2">
          <el-form-item label="配图地址">
            <el-input v-model="editingBanner.imageUrl" placeholder="/covers/xxx.jpg 或外链" />
          </el-form-item>
          <el-form-item label="关联图书 ID">
            <el-input v-model="editingBanner.bookId" placeholder="留空则不可点击" />
          </el-form-item>
        </div>
        <div class="g2">
          <el-form-item label="排序（越小越靠前）">
            <el-input v-model="editingBanner.sortOrder" type="number" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="editingBanner.status" size="small">
              <el-radio-button label="ACTIVE">生效</el-radio-button>
              <el-radio-button label="INACTIVE">下线</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <button class="tbtn plain" @click="showBannerDialog = false">取消</button>
        <button class="tbtn" :disabled="savingBanner" @click="saveBanner">{{ savingBanner ? '保存中…' : '保存' }}</button>
      </template>
    </el-dialog>

    <!-- 图书编辑 -->
    <el-dialog v-model="showBookDialog" :title="editingBook.id ? '编辑图书' : '上架新书'" width="640px" class="nv-dialog">
      <el-form :model="editingBook" label-position="top">
        <div class="g2">
          <el-form-item label="书名"><el-input v-model="editingBook.name" /></el-form-item>
          <el-form-item label="作者"><el-input v-model="editingBook.author" /></el-form-item>
        </div>
        <div class="g2">
          <el-form-item label="出版社"><el-input v-model="editingBook.publisher" /></el-form-item>
          <el-form-item label="ISBN"><el-input v-model="editingBook.isbn" /></el-form-item>
        </div>
        <div class="g4">
          <el-form-item label="价格"><el-input v-model.number="editingBook.price" /></el-form-item>
          <el-form-item label="库存"><el-input v-model.number="editingBook.stock" /></el-form-item>
          <el-form-item label="页数"><el-input v-model.number="editingBook.pages" /></el-form-item>
          <el-form-item label="分类">
            <el-select v-model="editingBook.categoryId" placeholder="分类">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="简介"><el-input v-model="editingBook.summary" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="目录"><el-input v-model="editingBook.catalog" type="textarea" :rows="2" placeholder="每行一个章节" /></el-form-item>
      </el-form>
      <template #footer>
        <button class="dlg-btn" @click="showBookDialog = false">取消</button>
        <button class="dlg-btn gold" :disabled="savingBook" @click="saveBook">保存</button>
      </template>
    </el-dialog>

    <!-- 批量调价 -->
    <el-dialog v-model="showPriceDialog" title="批量调价" width="460px" class="nv-dialog">
      <el-form label-position="top">
        <el-form-item label="范围">
          <el-select v-model="priceForm.categoryId" placeholder="全部分类" clearable>
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="方式">
          <el-radio-group v-model="priceForm.type">
            <el-radio label="fixed">固定金额</el-radio>
            <el-radio label="percent">百分比</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="priceForm.type === 'fixed' ? '调整金额（负数降价）' : '调整百分比（负数降价）'">
          <el-input v-model.number="priceForm.adjustment" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="dlg-btn" @click="showPriceDialog = false">取消</button>
        <button class="dlg-btn gold" @click="batchPrice">确认调整</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminAPI, categoryAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import BookCover from '../components/common/BookCover.vue'
import OrderStatusBadge from '../components/common/OrderStatusBadge.vue'

const router = useRouter()

const activeMenu = ref('overview')
const adminName = ref(localStorage.getItem('username') || '管理员')
const adminAccount = ref(localStorage.getItem('account') || '')

const stats = ref({})
const books = ref([])
const orders = ref([])
const users = ref([])
const categories = ref([])
const afterSales = ref([])
const banners = ref([])

/* Banner 编辑弹窗 */
const showBannerDialog = ref(false)
const savingBanner = ref(false)
const editingBanner = reactive({ id: null, title: '', subtitle: '', imageUrl: '', bookId: null, sortOrder: 0, status: 'ACTIVE' })

/* 售后处理弹窗 */
const showAfterSaleDialog = ref(false)
const afterSaleCurrent = ref(null)
const afterSaleAction = ref('APPROVED')
const afterSaleRemark = ref('')
const afterSaleSubmitting = ref(false)
const pendingAfterSales = computed(() => afterSales.value.filter(a => a.status === 'PENDING').length)
const afterSaleTag = (s) => ({ PENDING: 'gold', APPROVED: 'sage', COMPLETED: 'sage', REJECTED: 'danger' }[s] || '')

function openHandle(item, action) {
  afterSaleCurrent.value = item
  afterSaleAction.value = action
  afterSaleRemark.value = ''
  showAfterSaleDialog.value = true
}


/* ---- Banner 管理 ---- */
function openBanner(item) {
  if (item) {
    Object.assign(editingBanner, item)
  } else {
    Object.assign(editingBanner, {
      id: null, title: '', subtitle: '', imageUrl: '', bookId: null, sortOrder: 0, status: 'ACTIVE'
    })
  }
  showBannerDialog.value = true
}

async function saveBanner() {
  if (!editingBanner.title.trim()) {
    ElMessage.warning('请填写标题')
    return
  }
  savingBanner.value = true
  try {
    const payload = {
      title: editingBanner.title,
      subtitle: editingBanner.subtitle,
      imageUrl: editingBanner.imageUrl,
      bookId: editingBanner.bookId || null,
      sortOrder: Number(editingBanner.sortOrder) || 0,
      status: editingBanner.status
    }
    if (editingBanner.id) await adminAPI.updateBanner(editingBanner.id, payload)
    else await adminAPI.createBanner(payload)
    ElMessage.success('已保存')
    showBannerDialog.value = false
    await loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally {
    savingBanner.value = false
  }
}

async function toggleBanner(banner) {
  try {
    await adminAPI.toggleBanner(banner.id)
    await loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function removeBanner(banner) {
  try {
    await ElMessageBox.confirm(`确认删除「${banner.title}」？`, '删除 Banner', { type: 'warning' })
  } catch { return }
  try {
    await adminAPI.deleteBanner(banner.id)
    ElMessage.success('已删除')
    await loadData()
  } catch {
    ElMessage.error('删除失败')
  }
}
async function submitAfterSaleHandle() {
  if (!afterSaleCurrent.value) return
  afterSaleSubmitting.value = true
  try {
    await adminAPI.handleAfterSale(afterSaleCurrent.value.id, {
      status: afterSaleAction.value,
      adminRemark: afterSaleRemark.value
    })
    ElMessage.success('已处理')
    showAfterSaleDialog.value = false
    await loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '处理失败')
  } finally {
    afterSaleSubmitting.value = false
  }
}

const bookSearch = ref('')
const bookCategoryFilter = ref('')
const orderFilter = ref('ALL')

const showBookDialog = ref(false)
const showPriceDialog = ref(false)
const savingBook = ref(false)
const editingBook = ref({})
const priceForm = reactive({ categoryId: null, type: 'fixed', adjustment: 0 })

const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
const fmt = (n) => Number(n || 0).toFixed(2)
const menuTitle = computed(() => ({ overview: '数据概览', books: '图书管理', banners: '运营 Banner', orders: '订单管理', 'after-sales': '售后管理', users: '用户管理' }[activeMenu.value]))

const pendingShip = computed(() => orders.value.filter(o => o.status === 'PAID').length)
const recentOrders = computed(() => orders.value.slice(0, 5))
const lowStockBooks = computed(() => books.value.filter(b => Number(b.stock) < 10).sort((a, b) => a.stock - b.stock).slice(0, 5))
const topBooks = computed(() => [...books.value].sort((a, b) => Number(b.salesCount || 0) - Number(a.salesCount || 0)).slice(0, 5))

const totalQty = (o) => (o.items || []).reduce((s, i) => s + Number(i.quantity || 0), 0)

const statusBars = computed(() => {
  const total = Number(stats.value.totalOrders || 0) || 1
  return [
    { key: 'paid', label: '待发货', value: stats.value.paidOrders || 0, color: 'var(--color-gold)' },
    { key: 'shipped', label: '已发货', value: stats.value.shippedOrders || 0, color: 'var(--color-sage)' },
    { key: 'completed', label: '已完成', value: stats.value.completedOrders || 0, color: '#6b6559' },
    { key: 'pending', label: '待付款', value: stats.value.pendingOrders || 0, color: 'var(--color-info)' },
    { key: 'cancelled', label: '已取消', value: stats.value.cancelledOrders || 0, color: 'var(--color-danger)' }
  ].map(s => ({ ...s, percent: Math.round((s.value / total) * 100) }))
})

const filteredBooks = computed(() => {
  let list = books.value
  if (bookCategoryFilter.value) list = list.filter(b => String(b.categoryId) === String(bookCategoryFilter.value))
  if (bookSearch.value.trim()) {
    const kw = bookSearch.value.trim().toLowerCase()
    list = list.filter(b =>
      String(b.name || '').toLowerCase().includes(kw) ||
      String(b.author || '').toLowerCase().includes(kw) ||
      String(b.isbn || '').includes(kw)
    )
  }
  return list
})

const filteredOrders = computed(() =>
  orderFilter.value === 'ALL' ? orders.value : orders.value.filter(o => o.status === orderFilter.value)
)

const orderFilters = computed(() => [
  { key: 'ALL', label: '全部', count: orders.value.length },
  { key: 'PENDING', label: '待付款', count: orders.value.filter(o => o.status === 'PENDING').length },
  { key: 'PAID', label: '待发货', count: orders.value.filter(o => o.status === 'PAID').length },
  { key: 'SHIPPED', label: '待收货', count: orders.value.filter(o => o.status === 'SHIPPED').length },
  { key: 'COMPLETED', label: '已完成', count: orders.value.filter(o => o.status === 'COMPLETED').length },
  { key: 'CANCELLED', label: '已取消', count: orders.value.filter(o => o.status === 'CANCELLED').length }
])

async function loadData() {
  const [d, b, o, u, c, a, bn] = await Promise.all([
    adminAPI.getDashboard().catch(() => ({ data: {} })),
    adminAPI.getAllBooks().catch(() => ({ data: [] })),
    adminAPI.getAllOrders().catch(() => ({ data: [] })),
    adminAPI.getAllUsers().catch(() => ({ data: [] })),
    categoryAPI.getAll().catch(() => ({ data: [] })),
    adminAPI.getAfterSales().catch(() => ({ data: [] })),
    adminAPI.getBanners().catch(() => ({ data: [] }))
  ])
  stats.value = d.data || {}
  books.value = Array.isArray(b.data) ? b.data : (b.data?.content || [])
  orders.value = o.data || []
  users.value = u.data || []
  categories.value = c.data || []
  afterSales.value = a.data || []
  banners.value = bn.data || []
}

function openBook(book) {
  editingBook.value = book
    ? { ...book }
    : { name: '', author: '', publisher: '', isbn: '', price: 0, stock: 0, pages: 0, summary: '', catalog: '', categoryId: null }
  showBookDialog.value = true
}

async function saveBook() {
  if (!editingBook.value.name || !editingBook.value.author || !editingBook.value.isbn) {
    ElMessage.warning('请填写书名、作者与 ISBN')
    return
  }
  savingBook.value = true
  try {
    if (editingBook.value.id) await adminAPI.updateBook(editingBook.value.id, editingBook.value)
    else await adminAPI.createBook(editingBook.value)
    ElMessage.success('已保存')
    showBookDialog.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally {
    savingBook.value = false
  }
}

async function removeBook(book) {
  try {
    await ElMessageBox.confirm(`确定删除《${book.name}》吗？`, '确认删除', { confirmButtonText: '确定', cancelButtonText: '取消' })
    await adminAPI.deleteBook(book.id)
    ElMessage.success('已删除')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

async function batchPrice() {
  if (!priceForm.adjustment) { ElMessage.warning('请输入调整数值'); return }
  try {
    const res = await adminAPI.batchUpdatePrice(priceForm)
    ElMessage.success(res.data?.message || '调价完成')
    showPriceDialog.value = false
    loadData()
  } catch {
    ElMessage.error('调价失败')
  }
}

async function ship(id) {
  try { await adminAPI.shipOrder(id); ElMessage.success('已发货'); loadData() } catch { ElMessage.error('操作失败') }
}
async function complete(id) {
  try { await adminAPI.completeOrder(id); ElMessage.success('已完成'); loadData() } catch { ElMessage.error('操作失败') }
}
async function cancel(id) {
  try { await adminAPI.cancelOrder(id); ElMessage.success('已取消并恢复库存'); loadData() } catch { ElMessage.error('操作失败') }
}

async function setRole(u, role) {
  try {
    await adminAPI.updateUserRole(u.id, role)
    ElMessage.success('角色已更新')
    loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

function goShop() { router.push('/') }
function logout() {
  localStorage.clear()
  router.push('/admin-login')
  window.location.reload()
}

onMounted(loadData)
</script>

<style scoped>
.admin { display: flex; min-height: 100vh; background: var(--color-bg); }

/* 侧栏 */
.sb {
  width: 208px; flex: none; background: #090a0c; border-right: 1px solid var(--color-line);
  display: flex; flex-direction: column; padding: 22px 0 18px;
  position: sticky; top: 0; height: 100vh;
}
.logo { padding: 0 20px 20px; display: flex; align-items: baseline; gap: 10px; border-bottom: 1px solid var(--color-line); margin-bottom: 14px; }
.logo b { font-family: var(--font-display); font-size: 19px; letter-spacing: .28em; font-weight: 500; }
.logo span { font-size: 8px; letter-spacing: .3em; color: var(--color-gold); }
.sbg { padding: 10px 0; }
.sbg h6 { font-size: 8.5px; letter-spacing: .26em; color: var(--color-ink-faint); font-weight: 400; padding: 0 20px 8px; }
.sbi {
  display: flex; align-items: center; gap: 10px; height: 36px; padding: 0 20px;
  font-size: 12px; color: var(--color-ink-muted); letter-spacing: .08em; cursor: pointer; position: relative;
}
.sbi svg { width: 13px; height: 13px; stroke: currentColor; fill: none; stroke-width: 1.4; opacity: .85; }
.sbi:hover { color: var(--color-ink-soft); }
.sbi.on { background: var(--color-surface); color: var(--color-ink); }
.sbi.on::before { content: ""; position: absolute; left: 0; top: 0; bottom: 0; width: 2px; background: var(--color-gold); }
.sbi em {
  margin-left: auto; font-style: normal; font-size: 8.5px; color: #14150f;
  background: var(--color-gold); padding: 1px 5px;
}
.sb .bottom {
  margin-top: auto; padding: 16px 20px 0; border-top: 1px solid var(--color-line);
  display: flex; flex-direction: column; gap: 4px; font-size: 10.5px; color: var(--color-ink-faint);
}
.sb .bottom b { font-weight: 400; color: var(--color-ink-soft); letter-spacing: .08em; }
.sb .bottom a { color: var(--color-gold); margin-top: 8px; cursor: pointer; }
.sb .bottom a.out { color: var(--color-ink-faint); margin-top: 2px; }
.sb .bottom a.out:hover { color: var(--color-danger); }

/* 主区 */
.mn { flex: 1; min-width: 0; }
.topbar { height: 60px; border-bottom: 1px solid var(--color-line); display: flex; align-items: center; padding: 0 32px; gap: 20px; }
.crumb { font-size: 11px; color: var(--color-ink-faint); letter-spacing: .08em; display: flex; gap: 8px; align-items: center; }
.crumb i { font-style: normal; opacity: .5; }
.crumb span { color: var(--color-ink-soft); }
.tb-right { margin-left: auto; display: flex; align-items: center; gap: 20px; }
.sync { font-size: 10.5px; color: var(--color-ink-muted); letter-spacing: .1em; cursor: pointer; }
.sync:hover { color: var(--color-gold); }
.who { display: flex; align-items: center; gap: 8px; font-size: 11px; color: var(--color-ink-soft); }
.av {
  width: 24px; height: 24px; border-radius: 50%; background: var(--color-surface-2); border: 1px solid var(--color-line-strong);
  display: flex; align-items: center; justify-content: center; font-family: var(--font-display); font-size: 10px; color: var(--color-gold);
}

.content { padding: 26px 32px 40px; }
.head { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 22px; gap: 20px; }
.head h1 { font-family: var(--font-display); font-size: 27px; font-weight: 400; letter-spacing: .12em; }
.head .sub { margin-top: 8px; font-size: 10.5px; color: var(--color-ink-muted); letter-spacing: .06em; }
.head-acts { display: flex; gap: 10px; }

/* KPI */
.kpis { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.kpi { border: 1px solid var(--color-line); background: var(--color-surface); padding: 18px 20px; }
.kpi .l { font-size: 8.5px; letter-spacing: .24em; color: var(--color-ink-faint); }
.kpi .v { margin-top: 12px; font-family: var(--font-display); font-size: 27px; letter-spacing: -.01em; }
.kpi .v em { font-style: normal; font-size: 12px; color: var(--color-ink-muted); margin-left: 3px; }
.kpi .d { margin-top: 9px; font-size: 9.5px; letter-spacing: .05em; color: var(--color-ink-muted); }
.kpi.warn { border-color: var(--color-gold-line); background: rgba(211,174,106,.05); }
.kpi.warn .v { color: var(--color-gold); }

/* 图表 */
.charts { display: flex; gap: 14px; margin-top: 14px; }
.chart-main { width: 62%; flex: none; }
.chart-side { flex: 1; }
.status-bars { display: flex; flex-direction: column; gap: 12px; }
.sbar { display: flex; align-items: center; gap: 12px; font-size: 10.5px; color: var(--color-ink-muted); }
.sbar .sn { width: 52px; flex: none; }
.sbar .track { flex: 1; height: 4px; background: var(--color-surface-3); position: relative; }
.sbar .track i { position: absolute; left: 0; top: 0; bottom: 0; }
.sbar .sv { width: 40px; text-align: right; color: var(--color-ink-soft); }

.tops { display: flex; flex-direction: column; }
.top { display: flex; align-items: center; gap: 12px; padding: 11px 0; border-bottom: 1px solid var(--color-line); font-size: 11px; }
.top:last-child { border-bottom: 0; }
.top .no { font-family: var(--font-display); color: var(--color-ink-faint); }
.top .tn { flex: 1; color: var(--color-ink-soft); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.top .tv { color: var(--color-gold); }

.bottom-grid { display: flex; gap: 14px; margin-top: 14px; align-items: flex-start; }
.bottom-grid .panel:first-child { flex: 1; min-width: 0; }
.bottom-grid .panel:last-child { width: 300px; flex: none; }

.panel { border: 1px solid var(--color-line); background: var(--color-surface); padding: 18px 20px; }
.panel-h { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.panel-h h3 { font-family: var(--font-display); font-size: 13px; font-weight: 400; letter-spacing: .18em; }
.panel-h .plain { font-size: 9.5px; color: var(--color-ink-faint); letter-spacing: .06em; }
.panel-h .act { font-size: 10px; color: var(--color-gold); cursor: pointer; letter-spacing: .08em; }

.lows { display: flex; flex-direction: column; }
.ls { display: flex; align-items: center; gap: 11px; padding: 10px 0; border-bottom: 1px solid var(--color-line); }
.ls:last-child { border-bottom: 0; }
.ls-cover { width: 30px; height: 40px; flex: none; }
.lsm { flex: 1; min-width: 0; }
.lsm b { display: block; font-size: 11.5px; font-weight: 400; color: var(--color-ink-soft); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.lsm span { display: block; margin-top: 3px; font-size: 9.5px; color: var(--color-ink-faint); }
.lsn { text-align: right; }
.lsn b { font-family: var(--font-display); font-size: 14px; color: var(--color-gold); font-weight: 400; }
.lsn span { font-size: 8.5px; color: var(--color-ink-faint); margin-left: 2px; }

/* 表格 */
.tbl { width: 100%; border-collapse: collapse; }
.tbl th {
  text-align: left; font-size: 8.5px; letter-spacing: .22em; color: var(--color-ink-faint);
  font-weight: 400; padding: 0 12px 11px 0; border-bottom: 1px solid var(--color-line-strong);
}
.tbl td { padding: 11px 12px 11px 0; border-bottom: 1px solid var(--color-line); font-size: 11px; color: var(--color-ink-muted); vertical-align: middle; }
.tbl tbody tr:last-child td { border-bottom: 0; }
.tbl .mono { font-variant-numeric: tabular-nums; }
.tbl .gold { color: var(--color-gold); }
.tbl .muted { color: var(--color-ink-faint); }
.tbl .warnText { color: var(--color-danger); }
.tbl .tt { display: block; font-size: 11.5px; color: var(--color-ink-soft); }
.tbl .ts { display: block; margin-top: 3px; font-size: 9.5px; color: var(--color-ink-faint); }
.tbl .ellipsis { max-width: 320px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tbl .tc { width: 34px; height: 46px; }
.tbl .blank { text-align: center; padding: 30px 0; color: var(--color-ink-faint); font-size: 10.5px; }

.uav {
  width: 26px; height: 26px; border-radius: 50%; border: 1px solid var(--color-line-strong);
  display: inline-flex; align-items: center; justify-content: center;
  font-family: var(--font-display); font-size: 10px; color: var(--color-gold);
}

/* 工具栏 */
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.inp, .sel {
  height: 34px; border: 1px solid var(--color-line); background: var(--color-surface);
  color: var(--color-ink); font-family: inherit; font-size: 11.5px; padding: 0 12px; letter-spacing: .04em;
}
.inp { width: 260px; }
.inp::placeholder { color: var(--color-ink-faint); }
.inp:focus, .sel:focus { outline: 0; border-color: var(--color-gold-line); }
.count { margin-left: auto; font-size: 10px; color: var(--color-ink-faint); letter-spacing: .06em; }

.tabs { display: flex; gap: 24px; border-bottom: 1px solid var(--color-line-strong); margin-bottom: 14px; }
.tabs button {
  background: none; border: 0; font-family: inherit; padding: 0 0 12px; font-size: 11.5px;
  letter-spacing: .1em; color: var(--color-ink-muted); cursor: pointer; position: relative;
}
.tabs button.on { color: var(--color-ink); }
.tabs button.on::after { content: ""; position: absolute; left: 0; right: 0; bottom: -1px; height: 1px; background: var(--color-gold); }
.tabs em { font-style: normal; font-size: 8.5px; color: var(--color-ink-faint); margin-left: 4px; }

/* 小按钮 */
.tbtn {
  height: 24px; padding: 0 10px; border: 1px solid var(--color-gold-line); background: transparent;
  color: var(--color-gold); font-family: inherit; font-size: 9.5px; letter-spacing: .08em; cursor: pointer;
}
.tbtn:hover { background: var(--color-gold-soft); }
.tbtn.plain { border-color: var(--color-line-strong); color: var(--color-ink-muted); }
.tbtn.plain:hover { color: var(--color-ink); }
.tbtn.danger { border-color: rgba(201,116,92,.5); color: var(--color-danger); margin-left: 4px; }
.tbtn.gold { background: var(--color-gold); color: #14150f; }

/* 弹窗 */
.nv-dialog :deep(.el-dialog) { background: var(--color-surface); border: 1px solid var(--color-line); }
.nv-dialog :deep(.el-dialog__title) { font-family: var(--font-display); letter-spacing: .12em; color: var(--color-ink); }
.nv-dialog :deep(.el-form-item__label) { font-size: 10px; letter-spacing: .12em; color: var(--color-ink-muted); padding-bottom: 3px; }
.nv-dialog :deep(.el-input__wrapper) { background: var(--color-bg); box-shadow: 0 0 0 1px var(--color-line) inset; border-radius: 0; }
.nv-dialog :deep(.el-textarea__inner) { background: var(--color-bg); box-shadow: 0 0 0 1px var(--color-line) inset; border-radius: 0; color: var(--color-ink); }
.nv-dialog :deep(.el-radio__label) { color: var(--color-ink-soft); font-size: 11.5px; }
.dlg-btn {
  height: 34px; padding: 0 18px; border: 1px solid var(--color-line-strong); background: transparent;
  color: var(--color-ink-soft); font-family: inherit; font-size: 11px; letter-spacing: .1em; cursor: pointer; margin-left: 8px;
}
.dlg-btn.gold { background: var(--color-gold); border-color: var(--color-gold); color: #14150f; }
.g2 { display: grid; grid-template-columns: 1fr 1fr; gap: 0 14px; }
.g4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 0 12px; }

.bn-thumb { width: 132px; height: 52px; object-fit: cover; border: 1px solid var(--color-line); }
.as-dlg-tip { font-size: 11px; color: var(--color-ink-muted); letter-spacing: .05em; margin-bottom: 12px; }
.ts.reply { color: var(--color-gold); }

@media (max-width: 1200px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
  .charts { flex-direction: column; }
  .chart-main { width: 100%; }
  .bottom-grid { flex-direction: column; }
  .bottom-grid .panel:last-child { width: 100%; }
}
.bn-thumb { width: 132px; height: 52px; object-fit: cover; border: 1px solid var(--color-line); }
.as-dlg-tip { font-size: 11px; color: var(--color-ink-muted); letter-spacing: .05em; margin-bottom: 12px; }
.ts.reply { color: var(--color-gold); }

</style>