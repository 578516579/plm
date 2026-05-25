/**
 * businessRoute SSoT 单测
 *
 * 锁定 entity → path 映射 (sys_menu 重分组导致的"按钮 404"修复证据).
 * 此 spec 失败 = 映射表与 sys_menu 漂移, 必须按文件头部 SQL 重新校对.
 */
import { describe, it, expect } from 'vitest'
import { entityToPath, ENTITY_TO_PATH } from '../businessRoute'

describe('businessRoute SSoT (entity → /<phase>/<entity>)', () => {
  describe('8 阶段映射齐全', () => {
    it('workbench: dashboard', () => {
      expect(entityToPath('dashboard')).toBe('/workbench/dashboard')
    })

    it('phase-plan: project / inception / competitive', () => {
      expect(entityToPath('project')).toBe('/phase-plan/project')
      expect(entityToPath('inception')).toBe('/phase-plan/inception')
      expect(entityToPath('competitive')).toBe('/phase-plan/competitive')
    })

    it('phase-design: 7 个 entity', () => {
      ;['requirement', 'prd', 'ued', 'arch', 'dbdesign', 'apidesign', 'document'].forEach(e => {
        expect(entityToPath(e)).toBe(`/phase-design/${e}`)
      })
    })

    it('phase-dev: task / mytask / sprint', () => {
      expect(entityToPath('task')).toBe('/phase-dev/task')
      expect(entityToPath('mytask')).toBe('/phase-dev/mytask')
      expect(entityToPath('sprint')).toBe('/phase-dev/sprint')
    })

    it('phase-test: 7 个 entity', () => {
      ;['defect', 'testcase', 'submission', 'autotest', 'testplan', 'testreport', 'testdata'].forEach(e => {
        expect(entityToPath(e)).toBe(`/phase-test/${e}`)
      })
    })

    it('phase-deploy: 8 个 entity (含中划线 manual-* / feature-flag)', () => {
      ;['release', 'apidoc', 'manual-product', 'manual-impl', 'manual-ops',
        'pipeline', 'feature-flag', 'dora'].forEach(e => {
        expect(entityToPath(e)).toBe(`/phase-deploy/${e}`)
      })
    })

    it('phase-ai: ai-agent / openspec / ai-invocation-log', () => {
      expect(entityToPath('ai-agent')).toBe('/phase-ai/ai-agent')
      expect(entityToPath('openspec')).toBe('/phase-ai/openspec')
      expect(entityToPath('ai-invocation-log')).toBe('/phase-ai/ai-invocation-log')
    })

    it('phase-report: analytics', () => {
      expect(entityToPath('analytics')).toBe('/phase-report/analytics')
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

    it('所有映射 value 都以 / 开头且无尾 /', () => {
      Object.values(ENTITY_TO_PATH).forEach(path => {
        expect(path).toMatch(/^\/[a-z][a-z-]*\/[a-z][a-z-]*$/)
      })
    })

    it('所有 key 都是有效 entity 标识符 (小写字母 / 中划线)', () => {
      Object.keys(ENTITY_TO_PATH).forEach(entity => {
        expect(entity).toMatch(/^[a-z][a-z-]*$/)
      })
    })
  })
})
