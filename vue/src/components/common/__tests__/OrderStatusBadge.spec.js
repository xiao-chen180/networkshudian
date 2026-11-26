import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import OrderStatusBadge from '../OrderStatusBadge.vue'

describe('OrderStatusBadge 订单状态', () => {
  const cases = [
    ['PENDING', '待付款', 'gold'],
    ['PAID', '待发货', 'sage'],
    ['SHIPPED', '待收货', 'info'],
    ['COMPLETED', '已完成', 'muted'],
    ['CANCELLED', '已取消', 'danger']
  ]

  it.each(cases)('%s 显示文案与色调正确', (status, label, tone) => {
    const wrapper = mount(OrderStatusBadge, { props: { status } })
    expect(wrapper.text()).toContain(label)
    expect(wrapper.find('.status').classes()).toContain(`status-${tone}`)
  })

  it('未知状态回退展示原始值，不抛错', () => {
    const wrapper = mount(OrderStatusBadge, { props: { status: 'UNKNOWN_X' } })
    expect(wrapper.text()).toContain('UNKNOWN_X')
    expect(wrapper.find('.status').classes()).toContain('status-muted')
  })
})