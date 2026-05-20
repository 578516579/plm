/**
 * Release 模块 E2E — 原型 release.html 蓝绿/金丝雀/滚动 + DORA
 * 覆盖: CRUD + REL-YYYY-NNNN 编号 + UK(project_id+version) + 中文 HEX 守门员
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { assertNoMojibake, execDelete } from './helpers/db'
import { RUN_ID, makeProjectData, ERROR_CODES } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('Release 模块 E2E', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`rel-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`rel-suite-${RUN_ID}`))?.id
    expect(projectId).toBeDefined()
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_release', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-Rel-F001 创建发布单 + REL-YYYY-NNNN 编号 (canary 策略)', async () => {
    const version = `v1.0.0-${RUN_ID.slice(-4)}`
    const r = await api.post('/business/release', {
      projectId,
      version,
      strategy: 'canary',
      environment: 'prod',
      releaseNotes: '# v1.0.0\n- 新增导出 Excel\n- 修复登录超时',
      releasedByUserId: 1
    })
    expect(r.code).toBe(200)

    const list = await api.get('/business/release/list', { pageSize: 100 })
    const rel = list.rows.find((x: any) => x.version === version)
    expect(rel).toBeDefined()
    expect(rel.releaseNo).toMatch(/^REL-\d{4}-\d{4}$/)
    expect(rel.status).toBe('00')
  })

  test('TC-Rel-F002 编码守门员: 中文 releaseNotes + rollbackReason 无乱码', async () => {
    const version = `v-enc-${RUN_ID.slice(-4)}`
    await api.post('/business/release', {
      projectId,
      version,
      strategy: 'rolling',
      environment: 'staging',
      releaseNotes: '【发布说明】病虫害识别 αβγ 🚀 修复 P0 缺陷',
      releasedByUserId: 1
    })
    const list = await api.get('/business/release/list', { pageSize: 100 })
    const rel = list.rows.find((x: any) => x.version === version)
    await api.put('/business/release', {
      releaseId: rel.releaseId,
      rollbackReason: '兼容性问题：上游 SDK 不向后兼容 ★'
    })

    const notesCheck = assertNoMojibake('tb_release', 'release_notes',
      `release_id=${rel.releaseId}`)
    expect.soft(notesCheck.ok, `release_notes HEX=${notesCheck.hex}`).toBe(true)
    const reasonCheck = assertNoMojibake('tb_release', 'rollback_reason',
      `release_id=${rel.releaseId}`)
    expect.soft(reasonCheck.ok, `rollback_reason HEX=${reasonCheck.hex}`).toBe(true)
  })

  test('TC-Rel-F003 同 project 同 version 唯一 (uk_release_project_version)', async () => {
    const version = `v-dup-${RUN_ID.slice(-4)}`
    const r1 = await api.post('/business/release', {
      projectId, version, strategy: 'rolling', environment: 'prod', releasedByUserId: 1
    })
    expect(r1.code).toBe(200)

    const r2 = await api.post('/business/release', {
      projectId, version, strategy: 'blue_green', environment: 'prod', releasedByUserId: 1
    })
    expect.soft(r2.code, '同项目同版本应被 UK 拒').not.toBe(200)
  })

  test('TC-Rel-F004 FK projectId 不存在 → 702', async () => {
    const r = await api.post('/business/release', {
      projectId: 999_999_999,
      version: `v-fk-${RUN_ID.slice(-4)}`,
      strategy: 'rolling',
      environment: 'prod',
      releasedByUserId: 1
    })
    expect.soft(r.code, 'FK 不存在应返 702').toBe(ERROR_CODES.FK_NOT_EXISTS)
  })
})
