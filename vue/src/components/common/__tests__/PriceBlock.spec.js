import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PriceBlock from '../PriceBlock.vue'

describe('PriceBlock 价格展示', () => {
  it('格式化金额保留两位小数', () => {
    const wrapper = mount(PriceBlock, { props: { price: 98 } })
    expect(wrapper.find('.price').text()).toBe('98.00')
    expect(wrapper.find('.currency').text()).toBe('¥')
  })

  it('原价高于现价时展示划线原价', () => {
    const wrapper = mount(PriceBlock, { props: { price: 79.2, originalPrice: 99 } })
    expect(wrapper.find('.original').exists()).toBe(true)
    expect(wrapper.find('.original').text()).toContain('99.00')
  })

  it('原价低于现价时不展示划线价', () => {
    const wrapper = mount(PriceBlock, { props: { price: 99, originalPrice: 88 } })
    expect(wrapper.find('.original').exists()).toBe(false)
  })

  it('有会员价时展示会员价标签', () => {
    const wrapper = mount(PriceBlock, { props: { price: 98, memberPrice: 93.1 } })
    expect(wrapper.find('.member').text()).toContain('93.10')
  })

  it('支持字符串入参与非法值兜底', () => {
    const wrapper = mount(PriceBlock, { props: { price: '49.9' } })
    expect(wrapper.find('.price').text()).toBe('49.90')

    const bad = mount(PriceBlock, { props: { price: 'abc' } })
    expect(bad.find('.price').text()).toBe('0.00')
  })
})