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

  // === 状态机覆盖 — 5 状态非线性 (Phase 04 Gate B.2) ===
  // 00 计划中 → {01 发布中, 04 已废弃}
  // 01 发布中 → {02 已发布, 03 已回滚}
  // 02 已发布 → {03 已回滚, 04 已废弃}
  // 03 已回滚 / 04 已废弃 (终态)

  async function createRel(suffix: string, strategy = 'canary') {
    const version = `v-sm-${suffix}-${RUN_ID.slice(-4)}`
    await api.post('/business/release', {
      projectId, version, strategy, environment: 'prod', releasedByUserId: 1
    })
    const list = await api.get('/business/release/list', { pageSize: 100 })
    return list.rows.find((x: any) => x.version === version)
  }

  test('TC-Rel-F005 状态机正向 00→01→02 (计划→发布中→已发布)', async () => {
    const rel = await createRel('happy')
    expect(rel.status).toBe('00')

    const r1 = await api.put('/business/release', { releaseId: rel.releaseId, status: '01' })
    expect(r1.code).toBe(200)

    const r2 = await api.put('/business/release', { releaseId: rel.releaseId, status: '02' })
    expect(r2.code).toBe(200)
    const after = await api.get(`/business/release/${rel.releaseId}`)
    expect(after.data.status).toBe('02')
  })

  test('TC-Rel-F006 一键回滚 01→03 (发布中→已回滚)', async () => {
    const rel = await createRel('rollback', 'rolling')
    await api.put('/business/release', { releaseId: rel.releaseId, status: '01' })

    const r = await api.put('/business/release', {
      releaseId: rel.releaseId,
      status: '03',
      rollbackReason: '【P0】数据库迁移脚本撞主键 αβγ'
    })
    expect(r.code).toBe(200)
    const after = await api.get(`/business/release/${rel.releaseId}`)
    expect(after.data.status).toBe('03')
  })

  test('TC-Rel-F007 跳级 00→02 非法 → 601', async () => {
    const rel = await createRel('skip')
    const r = await api.put('/business/release', { releaseId: rel.releaseId, status: '02' })
    expect.soft(r.code, '00→02 必须先经 01').toBe(ERROR_CODES.STATUS_VIOLATION)
  })

  test('TC-Rel-F008 终态保护 03→01 非法 → 601', async () => {
    const rel = await createRel('final')
    await api.put('/business/release', { releaseId: rel.releaseId, status: '01' })
    await api.put('/business/release', { releaseId: rel.releaseId, status: '03' })

    const r = await api.put('/business/release', { releaseId: rel.releaseId, status: '01' })
    expect.soft(r.code, '03 已回滚是终态').toBe(ERROR_CODES.STATUS_VIOLATION)
  })

  test('TC-Rel-F009 策略字典白名单 (blue_green/canary/rolling)', async () => {
    const r = await api.post('/business/release', {
      projectId,
      version: `v-strategy-${RUN_ID.slice(-4)}`,
      strategy: 'big_bang', // 非字典值
      environment: 'prod',
      releasedByUserId: 1
    })
    expect.soft(r.code, '非字典策略应被拒').not.toBe(200)
  })

  test('TC-Rel-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const version = `v-del-${RUN_ID.slice(-4)}`
    const createResp = await api.post('/business/release', {
      projectId,
      version,
      strategy: 'rolling',
      environment: 'prod',
      releasedByUserId: 1
    })
    expect(createResp.code, '创建应成功').toBe(200)

    const before = await api.get('/business/release/list', { pageSize: 100 })
    const created = before.rows.find((x: any) => x.version === version)
    expect(created, '新建 release 应能在列表里查到').toBeDefined()
    const id: number = created.releaseId
    expect(typeof id, 'releaseId 应是 number').toBe('number')

    const delResp = await api.delete(`/business/release/${id}`)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.get('/business/release/list', { pageSize: 100 })
    const stillThere = after.rows.find((x: any) => x.releaseId === id)
    expect(stillThere, `releaseId=${id} 删除后不该出现在 list`).toBeUndefined()
  })
})
