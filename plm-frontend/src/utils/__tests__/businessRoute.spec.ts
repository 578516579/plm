/**
 * businessRoute SSoT 单测
 *
 * 锁定 entity → path 映射 (P0 troubleshoot: menu-path-absolute-business-prefix.sql 落地后,
 * sys_menu 子菜单 path 改 `/business/<entity>` 绝对路径,映射统一回 `/business/<entity>`).
 * 此 spec 失败 = 映射表与 sys_menu 漂移, 必须按文件头部 SQL 重新校对.
 *
 * 2026-05-28 0028 P0-2C 扩展: 加 useBusinessRoute composable 单测,
 * 用 vi.mock('vue-router') 拦截 router.push, 验证 4 个跨模块导航语义。
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { entityToPath, ENTITY_TO_PATH, useBusinessRoute } from '../businessRoute'

// Mock vue-router 的 useRouter — 注入一个可观察的 router.push spy
const pushMock = vi.fn().mockResolvedValue(undefined)
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
}))

describe('businessRoute SSoT (entity → /business/<entity>)', () => {
  describe('8 阶段映射齐全', () => {
    it('workbench: dashboard', () => {
      expect(entityToPath('dashboard')).toBe('/business/dashboard')
    })

    it('phase-plan: project / inception / competitive', () => {
      expect(entityToPath('project')).toBe('/business/project')
      expect(entityToPath('inception')).toBe('/business/inception')
      expect(entityToPath('competitive')).toBe('/business/competitive')
    })

    it('phase-design: 7 个 entity', () => {
      ;['requirement', 'prd', 'ued', 'arch', 'dbdesign', 'apidesign', 'document'].forEach(e => {
        expect(entityToPath(e)).toBe(`/business/${e}`)
      })
    })

    it('phase-dev: task / mytask / sprint', () => {
      expect(entityToPath('task')).toBe('/business/task')
      expect(entityToPath('mytask')).toBe('/business/mytask')
      expect(entityToPath('sprint')).toBe('/business/sprint')
    })

    it('phase-test: 7 个 entity', () => {
      ;['defect', 'testcase', 'submission', 'autotest', 'testplan', 'testreport', 'testdata'].forEach(e => {
        expect(entityToPath(e)).toBe(`/business/${e}`)
      })
    })

    it('phase-deploy: 8 个 entity (含中划线 manual-* / feature-flag)', () => {
      ;['release', 'apidoc', 'manual-product', 'manual-impl', 'manual-ops',
        'pipeline', 'feature-flag', 'dora'].forEach(e => {
        expect(entityToPath(e)).toBe(`/business/${e}`)
      })
    })

    it('phase-ai: ai-agent / openspec / ai-invocation-log', () => {
      expect(entityToPath('ai-agent')).toBe('/business/ai-agent')
      expect(entityToPath('openspec')).toBe('/business/openspec')
      expect(entityToPath('ai-invocation-log')).toBe('/business/ai-invocation-log')
    })

    it('phase-report: analytics', () => {
      expect(entityToPath('analytics')).toBe('/business/analytics')
    })
  })

  describe('未知 entity fallback', () => {
    it('未匹配时回落到 /business/${entity}', () => {
      expect(entityToPath('not-a-real-module')).toBe('/business/not-a-real-module')
    })
  })

  describe('结构性', () => {
    it('共 33 个 entity 映射 (sys_menu 重分组基线)', () => {
      expect(Object.keys(ENTITY_TO_PATH).length).toBe(33)
    })

    it('所有映射 value 都以 /business/ 开头', () => {
      Object.values(ENTITY_TO_PATH).forEach(path => {
        expect(path).toMatch(/^\/business\/[a-z][a-z-]*$/)
      })
    })

    it('所有 key 都是有效 entity 标识符 (小写字母 / 中划线)', () => {
      Object.keys(ENTITY_TO_PATH).forEach(entity => {
        expect(entity).toMatch(/^[a-z][a-z-]*$/)
      })
    })
  })

  // ────── 0028 P0-2C: useBusinessRoute composable ──────
  describe('useBusinessRoute (跨模块导航 composable)', () => {
    beforeEach(() => pushMock.mockClear())

    it('goEntityList 不带 query → 仅 path', async () => {
      const nav = useBusinessRoute()
      await nav.goEntityList('project')
      expect(pushMock).toHaveBeenCalledWith({ path: '/business/project', query: undefined })
    })

    it('goEntityList 带 query → 路径 + 过滤参数', async () => {
      const nav = useBusinessRoute()
      await nav.goEntityList('defect', { testcaseId: '42', projectId: '1' })
      expect(pushMock).toHaveBeenCalledWith({
        path: '/business/defect',
        query: { testcaseId: '42', projectId: '1' }
      })
    })

    it('goEntityDetail → 携 id + openDetail=1 query', async () => {
      const nav = useBusinessRoute()
      await nav.goEntityDetail('project', 42)
      expect(pushMock).toHaveBeenCalledWith({
        path: '/business/project',
        query: { id: '42', openDetail: '1' }
      })
    })

    it('goEntityDetail 接受 string id', async () => {
      const nav = useBusinessRoute()
      await nav.goEntityDetail('testplan', '7')
      expect(pushMock).toHaveBeenCalledWith({
        path: '/business/testplan',
        query: { id: '7', openDetail: '1' }
      })
    })

    it('backToParent 等价于 goEntityDetail', async () => {
      const nav = useBusinessRoute()
      await nav.backToParent('requirement', 99)
      expect(pushMock).toHaveBeenCalledWith({
        path: '/business/requirement',
        query: { id: '99', openDetail: '1' }
      })
    })

    it('goEntity 兜底 — 仅 path 无 query', async () => {
      const nav = useBusinessRoute()
      await nav.goEntity('dashboard')
      expect(pushMock).toHaveBeenCalledWith({ path: '/business/dashboard' })
    })

    it('未注册 entity 走 entityToPath fallback /business/<entity>', async () => {
      const nav = useBusinessRoute()
      await nav.goEntityList('not-a-module')
      expect(pushMock).toHaveBeenCalledWith({
        path: '/business/not-a-module',
        query: undefined
      })
    })

    it('返回对象暴露 4 个方法', () => {
      const nav = useBusinessRoute()
      expect(typeof nav.goEntityList).toBe('function')
      expect(typeof nav.goEntityDetail).toBe('function')
      expect(typeof nav.backToParent).toBe('function')
      expect(typeof nav.goEntity).toBe('function')
    })
  })
})
