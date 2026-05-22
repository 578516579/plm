/**
 * @file SvgIcon.spec.ts
 * @description SvgIcon 组件挂载 smoke test
 *
 * 验证：
 *   - 组件能在 happy-dom 中正常挂载，不抛错
 *   - iconClass prop 被映射为 #icon-xxx
 *   - className prop 拼入 svgClass
 */

import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SvgIcon from '../index.vue'

describe('SvgIcon', () => {
  it('挂载不抛错', () => {
    // 最低要求：iconClass 是 required prop
    expect(() => mount(SvgIcon, { props: { iconClass: 'home' } })).not.toThrow()
  })

  it('iconClass 映射为 #icon-xxx', () => {
    const wrapper = mount(SvgIcon, { props: { iconClass: 'home' } })
    const use = wrapper.find('use')
    // happy-dom 对 xlink:href 的序列化可能是 'xlink:href' 或 'href'；
    // 只需验证值中含有 '#icon-home'
    const attrs = use.attributes()
    const hrefVal = attrs['xlink:href'] ?? attrs['href'] ?? ''
    expect(hrefVal).toBe('#icon-home')
  })

  it('默认 svgClass 为 svg-icon', () => {
    const wrapper = mount(SvgIcon, { props: { iconClass: 'star' } })
    const svg = wrapper.find('svg')
    expect(svg.classes()).toContain('svg-icon')
  })

  it('传入 className 附加到 svgClass', () => {
    const wrapper = mount(SvgIcon, { props: { iconClass: 'star', className: 'my-icon' } })
    const svg = wrapper.find('svg')
    expect(svg.classes()).toContain('svg-icon')
    expect(svg.classes()).toContain('my-icon')
  })
})
