/**
 * @file permission.spec.ts
 * @description Pinia usePermissionStore 单元/集成测试
 *
 * 覆盖：
 *   - filterDynamicRoutes 纯函数：permissions 校验 / roles 校验 / 无权限过滤
 *   - setRoutes / setDefaultRoutes / setTopbarRoutes / setSidebarRouters 同步 action
 *
 * 跳过：generateRoutes — 依赖完整 vue-router + MSW 后端响应 + filterAsyncRouter 组件解析，
 *       复杂度过高，留待后续 PR 覆盖。
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// --- mock 外部依赖 ---

// vue-router：permission.ts 在顶层 import router + constantRoutes + dynamicRoutes
vi.mock('@/router', () => ({
  default: { addRoute: vi.fn(), push: vi.fn() },
  // constantRoutes：permission store 用它拼接 routes / defaultRoutes
  constantRoutes: [
    { path: '/login', name: 'Login', hidden: true }
  ],
  // dynamicRoutes：filterDynamicRoutes 的输入
  dynamicRoutes: [
    {
      path: '/system/user-auth',
      permissions: ['system:user:edit'],
      children: [{ path: 'role/:userId', name: 'AuthRole' }]
    },
    {
      path: '/system/role-auth',
      roles: ['admin'],
      children: [{ path: 'user/:roleId', name: 'AuthUser' }]
    },
    {
      path: '/system/dict-detail',
      permissions: ['system:dict:list'],
      children: [{ path: 'index/:dictId', name: 'DictData' }]
    }
  ]
}))

// @/plugins/auth：hasPermiOr / hasRoleOr 依赖 useUserStore，直接 stub
vi.mock('@/plugins/auth', () => ({
  default: {
    hasPermi: vi.fn(),
    hasPermiOr: vi.fn((perms: string[]) => {
      // 模拟拥有 system:user:edit 权限
      return perms.includes('system:user:edit')
    }),
    hasPermiAnd: vi.fn().mockReturnValue(false),
    hasRoleOr: vi.fn((roles: string[]) => {
      // 模拟角色 admin
      return roles.includes('admin')
    }),
    hasRoleAnd: vi.fn().mockReturnValue(false)
  }
}))

// Layout / ParentView / InnerLink 组件：filterAsyncRouter 里用到，测试不需要真实组件
vi.mock('@/layout/index.vue', () => ({ default: { name: 'Layout' } }))
vi.mock('@/components/ParentView/index.vue', () => ({ default: { name: 'ParentView' } }))
vi.mock('@/layout/components/InnerLink/index.vue', () => ({ default: { name: 'InnerLink' } }))

// api/menu：generateRoutes 用，但本测试不调 generateRoutes，留空即可
vi.mock('@/api/menu', () => ({ getRouters: vi.fn() }))

import usePermissionStore, { filterDynamicRoutes } from '../permission'

describe('filterDynamicRoutes (纯函数)', () => {
  // auth mock 已在模块顶部定义：hasPermiOr 匹配 system:user:edit，hasRoleOr 匹配 admin

  it('有 permissions 且匹配 → 路由被保留', async () => {
    // dynamicRoutes[0] 需要 system:user:edit，mock 返回 true
    const { dynamicRoutes } = await import('@/router')
    const result = filterDynamicRoutes([dynamicRoutes[0]])
    expect(result).toHaveLength(1)
    expect(result[0].path).toBe('/system/user-auth')
  })

  it('有 permissions 但不匹配 → 路由被过滤', async () => {
    const { dynamicRoutes } = await import('@/router')
    // dynamicRoutes[2] 需要 system:dict:list，mock hasPermiOr 只匹配 system:user:edit
    const result = filterDynamicRoutes([dynamicRoutes[2]])
    expect(result).toHaveLength(0)
  })

  it('有 roles 且匹配 → 路由被保留', async () => {
    const { dynamicRoutes } = await import('@/router')
    // dynamicRoutes[1] 需要 admin role，mock 返回 true
    const result = filterDynamicRoutes([dynamicRoutes[1]])
    expect(result).toHaveLength(1)
    expect(result[0].path).toBe('/system/role-auth')
  })

  it('空数组 → 空数组', () => {
    expect(filterDynamicRoutes([])).toEqual([])
  })
})

describe('usePermissionStore — 同步 actions', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('初始 state 全部为空数组', () => {
    const store = usePermissionStore()
    expect(store.routes).toEqual([])
    expect(store.addRoutes).toEqual([])
    expect(store.defaultRoutes).toEqual([])
    expect(store.topbarRouters).toEqual([])
    expect(store.sidebarRouters).toEqual([])
  })

  it('setRoutes 合并 constantRoutes + 新路由', () => {
    const store = usePermissionStore()
    const newRoutes = [{ path: '/business', name: 'Business' }]
    store.setRoutes(newRoutes)
    // addRoutes 仅包含传入的路由
    expect(store.addRoutes).toEqual(newRoutes)
    // routes = constantRoutes (1条) + newRoutes (1条)
    expect(store.routes).toHaveLength(2)
    expect(store.routes[0].path).toBe('/login')  // constantRoutes[0]
    expect(store.routes[1].path).toBe('/business')
  })

  it('setDefaultRoutes 合并 constantRoutes + 路由', () => {
    const store = usePermissionStore()
    const r = [{ path: '/home', name: 'Home' }]
    store.setDefaultRoutes(r)
    expect(store.defaultRoutes).toHaveLength(2)
  })

  it('setTopbarRoutes 直接赋值', () => {
    const store = usePermissionStore()
    const r = [{ path: '/top', name: 'Top' }]
    store.setTopbarRoutes(r)
    expect(store.topbarRouters).toEqual(r)
  })

  it('setSidebarRouters 直接赋值', () => {
    const store = usePermissionStore()
    const r = [{ path: '/side', name: 'Side' }]
    store.setSidebarRouters(r)
    expect(store.sidebarRouters).toEqual(r)
  })
})
