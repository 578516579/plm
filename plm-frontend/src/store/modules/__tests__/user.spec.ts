/**
 * @file user.spec.ts
 * @description Pinia useUserStore 集成测试 (MSW mock 后端)
 *
 * 覆盖：
 *   - 初始 state 默认值
 *   - login action 成功路径 (MSW mock /dev-api/login)
 *   - logOut action 清理 state
 *   - getInfo action 读用户信息 (MSW mock /dev-api/getInfo)
 *
 * 注意：
 *   - user.ts 依赖 vue-router / ElMessageBox / useLockStore，均在此 vi.mock()
 *   - MSW server 在 src/__mocks__/setup.ts 中全局启停
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// --- mock 有副作用的外部依赖 ---

// vue-router: 仅需 push，无需真实路由实例
vi.mock('@/router', () => ({
  default: { push: vi.fn(), addRoute: vi.fn() },
  constantRoutes: [],
  dynamicRoutes: []
}))

// element-plus: ElMessageBox.confirm 在 happy-dom 里会报错，直接 stub
vi.mock('element-plus', () => ({
  ElMessageBox: { confirm: vi.fn().mockResolvedValue(undefined) },
  ElNotification: vi.fn(),
  ElMessage: vi.fn(),
  ElLoading: { service: vi.fn().mockReturnValue({ close: vi.fn() }) }
}))

// lock store: unlockScreen 不需要真正实现
vi.mock('@/store/modules/lock', () => ({
  default: vi.fn().mockReturnValue({ unlockScreen: vi.fn() })
}))

// 加载 store（放在 mock 之后，确保 hoisting 正确）
import useUserStore from '../user'

describe('useUserStore', () => {
  beforeEach(() => {
    // 每条测试隔离 Pinia 实例，避免 token/state 污染
    setActivePinia(createPinia())
    // 清理 Cookie 中的 Admin-Token（happy-dom document.cookie）
    document.cookie = 'Admin-Token=; Max-Age=0; path=/'
  })

  // ----------------------------------------------------------------
  // 1. 初始 state
  // ----------------------------------------------------------------
  it('初始 state 默认值正确', () => {
    const store = useUserStore()
    // token 来自 Cookies.get('Admin-Token')，初始无 cookie → undefined
    expect(store.token).toBeUndefined()
    expect(store.name).toBe('')
    expect(store.nickName).toBe('')
    expect(store.avatar).toBe('')
    expect(store.roles).toEqual([])
    expect(store.permissions).toEqual([])
  })

  // ----------------------------------------------------------------
  // 2. login 成功路径
  // ----------------------------------------------------------------
  it('login 成功：token 写入 store', async () => {
    const store = useUserStore()
    await store.login({ username: 'admin', password: 'admin123', code: '1234', uuid: 'uuid-1' })
    expect(store.token).toBe('test-jwt-token')
  })

  it('login 成功：token 写入 Cookie', async () => {
    const store = useUserStore()
    await store.login({ username: 'admin', password: 'admin123', code: '1234', uuid: 'uuid-1' })
    expect(document.cookie).toContain('Admin-Token')
  })

  // ----------------------------------------------------------------
  // 3. logOut 清理 state
  // ----------------------------------------------------------------
  it('logOut 清空 token / roles / permissions', async () => {
    const store = useUserStore()
    // 先模拟登录状态
    await store.login({ username: 'admin', password: 'admin123', code: '1234', uuid: 'uuid-1' })
    expect(store.token).toBe('test-jwt-token')

    await store.logOut()
    expect(store.token).toBe('')
    expect(store.roles).toEqual([])
    expect(store.permissions).toEqual([])
  })

  // ----------------------------------------------------------------
  // 4. getInfo 读用户信息
  // ----------------------------------------------------------------
  it('getInfo 填充 name / nickName / roles / permissions', async () => {
    const store = useUserStore()
    // MSW handler: GET */getInfo → { user: {userId:1, userName:'admin', nickName:'管理员'}, roles:['admin'], permissions:['*:*:*'] }
    await store.getInfo()
    expect(store.name).toBe('admin')
    expect(store.nickName).toBe('管理员')
    expect(store.roles).toEqual(['admin'])
    expect(store.permissions).toEqual(['*:*:*'])
  })

  it('getInfo 无 roles 时设置默认角色 ROLE_DEFAULT', async () => {
    // 覆写 handler，返回空 roles
    const { server } = await import('@/__mocks__/server')
    const { http, HttpResponse } = await import('msw')
    server.use(
      http.get('*/getInfo', () => HttpResponse.json({
        code: 200, msg: '操作成功',
        user: { userId: 2, userName: 'guest', nickName: '访客' },
        roles: [], permissions: []
      }))
    )
    const store = useUserStore()
    await store.getInfo()
    expect(store.roles).toEqual(['ROLE_DEFAULT'])
  })
})
