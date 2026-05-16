/** 实施手册模块 E2E — PRD §F5.2 + 原型 implmanual.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
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

  test('TC-MANUAL-IMPL-F001 创建实施手册 (Docker Compose + CentOS + PostgreSQL)', async () => {
    const r = await api.post('/business/manual-impl', {
      projectId,
      title: `AgriPLM Docker 实施手册-${RUN_ID}`,
      deployMode: 'docker_compose',
      osType: 'centos7',
      dbType: 'postgresql14',
      outputFormats: 'pdf,markdown',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-MANUAL-IMPL-F002 创建实施手册 (K8s + Kylin + KingbaseES 国产化)', async () => {
    const r = await api.post('/business/manual-impl', {
      projectId,
      title: `信创栈实施手册-${RUN_ID}`,
      deployMode: 'kubernetes',
      osType: 'kylin',
      dbType: 'kdb',
      envConfig: '{"DB_HOST":"kdb.internal","REDIS_HOST":"redis.internal"}',
      outputFormats: 'pdf,word',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-MANUAL-IMPL-F003 AI 生成实施手册', async () => {
    const createRes = await api.post('/business/manual-impl', {
      projectId,
      title: `AI 实施手册-${RUN_ID}`,
      deployMode: 'docker_compose',
      osType: 'ubuntu20',
      dbType: 'mysql8',
      authorUserId: 1
    })
    expect(createRes.code).toBe(200)

    const list = await api.get('/business/manual-impl/list', { projectId })
    const mi = list.rows.find((r: any) => r.title.includes(`AI 实施手册-${RUN_ID}`))
    expect(mi).toBeDefined()

    const aiRes = await api.post(`/business/manual-impl/ai/generate/${mi.manualimplId}`, {})
    expect(aiRes.code).toBe(200)
    expect(aiRes.data.aiGenerated).toBe('Y')
    expect(aiRes.data.status).toBe('02')
    expect(aiRes.data.content).toContain('部署环境')
  })
})
