/**
 * businessRoute SSoT 单测
 *
 * 锁定 entity → path 映射 (P0 troubleshoot: menu-path-absolute-business-prefix.sql 落地后,
 * sys_menu 子菜单 path 改 `/business/<entity>` 绝对路径,映射统一回 `/business/<entity>`).
 * 此 spec 失败 = 映射表与 sys_menu 漂移, 必须按文件头部 SQL 重新校对.
 */
import { describe, it, expect } from 'vitest'
import { entityToPath, ENTITY_TO_PATH } from '../businessRoute'

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
})
