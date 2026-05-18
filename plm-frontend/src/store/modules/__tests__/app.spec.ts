/**
 * @file app.spec.ts
 * @description Pinia useAppStore 单元测试
 *
 * 覆盖纯同步 actions：
 *   toggleSideBar / closeSideBar / toggleDevice / setSize / toggleSideBarHide
 *
 * 注意：js-cookie 在 happy-dom 中可用，但 Cookies.get() 初始值为 undefined，
 * 因此 sidebar.opened 默认为 true（逻辑 !!+undefined → false → !!false → false，
 * 但 undefined 情况下走 ternary 的 else 分支 true）。
 */

import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import useAppStore from '../app'

describe('useAppStore', () => {
  beforeEach(() => {
    // 每条测试隔离 Pinia 实例
    setActivePinia(createPinia())
  })

  it('初始 device 为 desktop', () => {
    const store = useAppStore()
    expect(store.device).toBe('desktop')
  })

  it('toggleDevice 切换设备类型', () => {
    const store = useAppStore()
    store.toggleDevice('mobile')
    expect(store.device).toBe('mobile')
    store.toggleDevice('desktop')
    expect(store.device).toBe('desktop')
  })

  it('setSize 更新尺寸', () => {
    const store = useAppStore()
    store.setSize('large')
    expect(store.size).toBe('large')
  })

  it('toggleSideBar 反转 opened 状态', () => {
    const store = useAppStore()
    const initial = store.sidebar.opened
    store.toggleSideBar()
    expect(store.sidebar.opened).toBe(!initial)
    // 再切换回来
    store.toggleSideBar()
    expect(store.sidebar.opened).toBe(initial)
  })

  it('hide=true 时 toggleSideBar 不生效', () => {
    const store = useAppStore()
    store.toggleSideBarHide(true)
    const before = store.sidebar.opened
    store.toggleSideBar()
    expect(store.sidebar.opened).toBe(before)
  })

  it('closeSideBar 关闭侧边栏', () => {
    const store = useAppStore()
    // 先确保已打开
    if (!store.sidebar.opened) store.toggleSideBar()
    store.closeSideBar({ withoutAnimation: true })
    expect(store.sidebar.opened).toBe(false)
    expect(store.sidebar.withoutAnimation).toBe(true)
  })

  it('toggleSideBarHide 设置 hide 状态', () => {
    const store = useAppStore()
    store.toggleSideBarHide(true)
    expect(store.sidebar.hide).toBe(true)
    store.toggleSideBarHide(false)
    expect(store.sidebar.hide).toBe(false)
  })
})
