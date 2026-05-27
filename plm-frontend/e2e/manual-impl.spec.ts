/** 实施手册模块 E2E — PRD §F5.2 + 原型 implmanual.html
 *
 * 覆盖 (对齐 competitive.spec.ts 套路, 11 case):
 *   F001 创建 (Docker + CentOS + PostgreSQL)
 *   F002 创建 (K8s + Kylin + KingbaseES 国产化)
 *   F003 title 必填 → 602
 *   F004 关联项目不存在 → 702
 *   F005 deployMode 白名单非法 → 604
 *   F006 状态机正向 00→01→02 (进 02 自动填 generatedAt)
 *   F007 状态机跳级 00→02 非法 → 601
 *   F008 终态保护 03→00 非法 → 601
 *   F009 AI 生成 → content + status=02 + aiGenerated=Y
 *   F010 编号格式 IM-YYYY-NNNN
 *   ENC001 编码 HEX — 中文 title 不含 EFBFBD 替换符
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, getFieldHex } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('实施手册模块 E2E (PRD §F5.2)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`manual-impl-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`manual-impl-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_manual_impl', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  async function createImpl(suffix: string, extra: any = {}) {
    const r = await api.post('/business/manual-impl', {
      projectId,
      title: `${suffix}-${RUN_ID}`,
      authorUserId: 1,
      ...extra
    })
    return r
  }
  async function idByTitle(suffix: string) {
    const list = await api.get('/business/manual-impl/list', { projectId })
    return list.rows.find((r: any) => r.title.includes(`${suffix}-${RUN_ID}`))?.manualimplId
  }

  test('TC-MANUAL-IMPL-F001 创建实施手册 (Docker Compose + CentOS + PostgreSQL)', async () => {
    const r = await createImpl('AgriPLM Docker 实施手册', {
      deployMode: 'docker_compose', osType: 'centos7', dbType: 'postgresql14',
      outputFormats: 'pdf,markdown'
    })
    expect(r.code).toBe(200)
  })

  test('TC-MANUAL-IMPL-F002 创建实施手册 (K8s + Kylin + KingbaseES 国产化)', async () => {
    const r = await createImpl('信创栈实施手册', {
      deployMode: 'kubernetes', osType: 'kylin', dbType: 'kdb',
      envConfig: '{"DB_HOST":"kdb.internal","REDIS_HOST":"redis.internal"}', outputFormats: 'pdf,word'
    })
    expect(r.code).toBe(200)
  })

  test('TC-MANUAL-IMPL-F003 title 必填 → 602', async () => {
    const r = await api.post('/business/manual-impl', { projectId, authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/标题/)
  })

  test('TC-MANUAL-IMPL-F004 关联项目不存在 → 702', async () => {
    const r = await api.post('/business/manual-impl', {
      projectId: 99999999, title: `非法项目-${RUN_ID}`, authorUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/关联项目不存在/)
  })

  test('TC-MANUAL-IMPL-F005 deployMode 白名单非法 → 604', async () => {
    const r = await createImpl('非法部署模式', { deployMode: 'nomad_swarm' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/部署模式/)
  })

  test('TC-MANUAL-IMPL-F006 状态机正向 00→01→02 自动填 generatedAt', async () => {
    expect((await createImpl('状态机正向')).code).toBe(200)
    const id = await idByTitle('状态机正向')
    expect((await api.put('/business/manual-impl', { manualimplId: id, status: '01' })).code).toBe(200)
    expect((await api.put('/business/manual-impl', { manualimplId: id, status: '02' })).code).toBe(200)
    const got = await api.get(`/business/manual-impl/${id}`)
    expect(got.data.status).toBe('02')
    expect(got.data.generatedAt).toBeTruthy()
  })

  test('TC-MANUAL-IMPL-F007 状态机跳级 00→02 非法 → 601', async () => {
    expect((await createImpl('跳级非法')).code).toBe(200)
    const id = await idByTitle('跳级非法')
    const r = await api.put('/business/manual-impl', { manualimplId: id, status: '02' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-MANUAL-IMPL-F008 终态保护 03→00 非法 → 601', async () => {
    expect((await createImpl('终态保护')).code).toBe(200)
    const id = await idByTitle('终态保护')
    await api.put('/business/manual-impl', { manualimplId: id, status: '01' })
    await api.put('/business/manual-impl', { manualimplId: id, status: '02' })
    await api.put('/business/manual-impl', { manualimplId: id, status: '03' })
    const r = await api.put('/business/manual-impl', { manualimplId: id, status: '00' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-MANUAL-IMPL-F009 AI 生成 → content + status=02 + aiGenerated=Y', async () => {
    expect((await createImpl('AI 实施手册', { deployMode: 'docker_compose', osType: 'ubuntu20', dbType: 'mysql8' })).code).toBe(200)
    const id = await idByTitle('AI 实施手册')
    const aiRes = await api.post(`/business/manual-impl/ai/generate/${id}`, {})
    expect(aiRes.code).toBe(200)
    expect(aiRes.data.aiGenerated).toBe('Y')
    expect(aiRes.data.status).toBe('02')
    expect(aiRes.data.content).toContain('部署环境')
  })

  test('TC-MANUAL-IMPL-F010 编号格式 IM-YYYY-NNNN', async () => {
    expect((await createImpl('编号格式')).code).toBe(200)
    const list = await api.get('/business/manual-impl/list', { projectId })
    const row = list.rows.find((r: any) => r.title.includes(`编号格式-${RUN_ID}`))
    const year = new Date().getFullYear()
    expect(row.manualimplNo).toMatch(new RegExp(`^IM-${year}-\\d{4}$`))
  })

  test('TC-MANUAL-IMPL-ENC001 编码 HEX — 中文 title 不含 EFBFBD', async () => {
    const cn = '农情实施手册-中文检测'
    expect((await createImpl(cn)).code).toBe(200)
    const hex = getFieldHex('tb_manual_impl', 'title', `title like '${cn}-${RUN_ID}%'`)
    expect(hex.toUpperCase()).not.toContain('EFBFBD')
  })
})
