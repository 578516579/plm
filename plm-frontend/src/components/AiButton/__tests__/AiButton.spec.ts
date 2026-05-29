/**
 * @file AiButton.spec.ts
 * @description AiButton 组件单测（P0028-P0-4）
 *
 * 验证：
 *   - 默认渲染：含 ✨ 图标 + slot 文本
 *   - loading / disabled prop 透传给 el-button
 *   - plain 切换 ai-button--plain 样式类
 *   - click 事件向外发射
 */

import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AiButton from '../index.vue'

describe('AiButton', () => {
  it('默认渲染含 ✨ 图标 + slot 文本', () => {
    const wrapper = mount(AiButton, {
      slots: { default: 'AI 分析优先级' }
    })
    expect(wrapper.text()).toContain('✨')
    expect(wrapper.text()).toContain('AI 分析优先级')
    // 含 ai-icon class
    expect(wrapper.find('.ai-icon').exists()).toBe(true)
  })

  it('默认形态不带 ai-button--plain', () => {
    const wrapper = mount(AiButton, { slots: { default: 'X' } })
    expect(wrapper.classes()).toContain('ai-button')
    expect(wrapper.classes()).not.toContain('ai-button--plain')
  })

  it('plain=true 时附加 ai-button--plain', () => {
    const wrapper = mount(AiButton, {
      props: { plain: true },
      slots: { default: 'X' }
    })
    expect(wrapper.classes()).toContain('ai-button--plain')
  })

  it('loading=true 时 button 渲染 loading 状态', () => {
    const wrapper = mount(AiButton, {
      props: { loading: true },
      slots: { default: 'X' }
    })
    // 不同 Element Plus 版本输出的 loading class 名可能差异,这里宽松匹配 class 或 attribute
    const html = wrapper.html()
    expect(html).toMatch(/is-loading|loading/i)
  })

  it('disabled=true 时 button 渲染 disabled 状态', () => {
    const wrapper = mount(AiButton, {
      props: { disabled: true },
      slots: { default: 'X' }
    })
    const html = wrapper.html()
    expect(html).toMatch(/is-disabled|disabled/i)
  })

  it('click 事件向外发射', async () => {
    const wrapper = mount(AiButton, { slots: { default: 'X' } })
    await wrapper.trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
    expect(wrapper.emitted('click')!.length).toBe(1)
  })
})
