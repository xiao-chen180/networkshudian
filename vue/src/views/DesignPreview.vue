<template>
  <main class="preview u-container">
    <PageHeader
      eyebrow="Design System · Nocturne"
      title="深夜书房 · 设计基座"
      description="验证 token、排版与基础组件。此页面仅用于开发阶段对照，不进入正式导航。"
    />

    <section class="block">
      <SectionHeader eyebrow="01 / Color" title="色彩" description="纸色、墨色与克制的朱砂点缀。" />
      <div class="swatches">
        <div v-for="item in swatches" :key="item.name" class="swatch">
          <span class="chip" :style="{ background: `var(${item.token})` }" />
          <span class="name">{{ item.name }}</span>
          <span class="token u-mono">{{ item.token }}</span>
        </div>
      </div>
    </section>

    <section class="block">
      <SectionHeader eyebrow="02 / Type" title="排版" description="宋体标题 + 无衬线正文，中文行长控制在 38 字以内。" />
      <p class="u-eyebrow">Eyebrow · 期号 / 分类 / 标签</p>
      <p class="type-display">重读，是在熟悉的句子里遇见新的自己</p>
      <h1>一级标题 · 页面主题</h1>
      <h2>二级标题 · 区块主题</h2>
      <h3>三级标题 · 卡片书名</h3>
      <div class="u-prose">
        <p>中国古典四大名著之首，被誉为「封建社会的百科全书」。小说以贾、史、王、薛四大家族的兴衰为背景，以贾宝玉、林黛玉、薛宝钗的爱情悲剧为主线，展现了十八世纪中国封建社会的生活百态。</p>
      </div>
    </section>

    <section class="block">
      <SectionHeader eyebrow="03 / Components" title="基础组件" description="列表、详情、购物袋与订单将复用这些组件。" />
      <div class="grid">
        <BookCard :book="sampleBook" show-action eyebrow="文学小说" @add="onAdd" />
        <div class="stack">
          <h3>价格</h3>
          <PriceBlock price="98" original-price="108" member-price="93.10" tag="会员" />
          <PriceBlock price="45" size="lg" />
          <h3>评分</h3>
          <RatingStars :value="4.8" :count="126" show-value />
          <h3>数量</h3>
          <QuantityStepper v-model="quantity" :max="9" />
          <h3>状态</h3>
          <div class="row">
            <OrderStatusBadge status="PENDING" />
            <OrderStatusBadge status="PAID" />
            <OrderStatusBadge status="SHIPPED" />
            <OrderStatusBadge status="COMPLETED" />
            <OrderStatusBadge status="CANCELLED" />
          </div>
          <h3>按钮</h3>
          <div class="row">
            <button class="btn btn-primary">主要操作</button>
            <button class="btn btn-outline">次要操作</button>
            <button class="btn btn-text">文字操作</button>
          </div>
        </div>
      </div>
    </section>

    <section class="block">
      <SectionHeader eyebrow="04 / States" title="状态" description="加载、空数据与缺省封面。" />
      <div class="grid states">
        <BookCard :book="sampleBook" loading />
        <EmptyState title="购物袋还是空的" description="去典籍里挑一本想读的书吧。" action-text="浏览典籍" action-to="/books" />
        <div class="stack">
          <h3>骨架</h3>
          <SkeletonBlock width="80%" height="24px" />
          <SkeletonBlock width="60%" height="16px" />
          <SkeletonBlock width="40%" height="16px" />
          <h3>缺省封面</h3>
          <div class="fallback-cover">
            <BookCover title="没有封面的书" author="佚名" />
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '../components/common/PageHeader.vue'
import SectionHeader from '../components/common/SectionHeader.vue'
import BookCard from '../components/common/BookCard.vue'
import BookCover from '../components/common/BookCover.vue'
import PriceBlock from '../components/common/PriceBlock.vue'
import RatingStars from '../components/common/RatingStars.vue'
import QuantityStepper from '../components/common/QuantityStepper.vue'
import OrderStatusBadge from '../components/common/OrderStatusBadge.vue'
import EmptyState from '../components/common/EmptyState.vue'
import SkeletonBlock from '../components/common/SkeletonBlock.vue'

const quantity = ref(1)

const sampleBook = {
  id: 1,
  name: '红楼梦',
  author: '曹雪芹',
  price: 98,
  stock: 10,
  avgRating: 4.8,
  reviewCount: 126,
  imageUrl: ''
}

const swatches = [
  { name: '纸色', token: '--color-paper' },
  { name: '面板', token: '--color-surface' },
  { name: '次级面板', token: '--color-surface-muted' },
  { name: '墨色', token: '--color-ink' },
  { name: '次级文字', token: '--color-ink-soft' },
  { name: '辅助文字', token: '--color-ink-muted' },
  { name: '细线', token: '--color-rule' },
  { name: '墨绿', token: '--color-moss' },
  { name: '朱砂', token: '--color-cinnabar' },
  { name: '金色', token: '--color-gold' },
  { name: '成功', token: '--color-success' },
  { name: '危险', token: '--color-danger' }
]

const onAdd = () => {
  ElMessage.success('已加入购物袋（预览）')
}
</script>

<style scoped>
.preview {
  padding-bottom: var(--space-12);
}

.block {
  padding: var(--section-gap) 0 0;
}

.swatches {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: var(--space-4);
}

.swatch {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.chip {
  display: block;
  height: 72px;
  border: 1px solid var(--color-rule);
}

.name {
  font-size: var(--fs-sm);
  color: var(--color-ink-soft);
}

.token {
  font-size: var(--fs-xs);
  color: var(--color-ink-faint);
}

.type-display {
  margin: var(--space-3) 0 var(--space-6);
  font-family: var(--font-display);
  font-size: var(--fs-display);
  line-height: var(--lh-display);
  color: var(--color-ink);
}

.block h1,
.block h2,
.block h3 {
  margin: var(--space-4) 0 var(--space-2);
}

.grid {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: var(--space-9);
  align-items: start;
}

.stack {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.stack h3 {
  margin-top: var(--space-4);
  font-size: var(--fs-sm);
  color: var(--color-ink-muted);
  font-family: var(--font-body);
  letter-spacing: var(--tracking-eyebrow);
}

.row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
}

.btn {
  padding: var(--space-3) var(--space-6);
  font-size: var(--fs-sm);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--dur-fast) var(--ease-standard), color var(--dur-fast) var(--ease-standard);
}

.btn-primary {
  color: var(--color-on-brand);
  background: var(--color-cinnabar);
  border: 1px solid var(--color-cinnabar);
}

.btn-primary:hover {
  background: var(--color-cinnabar-soft);
  border-color: var(--color-cinnabar-soft);
}

.btn-outline {
  color: var(--color-moss);
  background: transparent;
  border: 1px solid var(--color-moss);
}

.btn-outline:hover {
  color: var(--color-on-brand);
  background: var(--color-moss);
}

.btn-text {
  color: var(--color-moss);
  background: transparent;
  border: 1px solid transparent;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.states {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.fallback-cover {
  width: 160px;
}

@media (max-width: 1023px) {
  .swatches { grid-template-columns: repeat(4, minmax(0, 1fr)); }
  .states { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 767px) {
  .swatches { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .grid,
  .states { grid-template-columns: 1fr; }
}
</style>