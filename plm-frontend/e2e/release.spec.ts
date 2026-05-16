/** Release 模块 E2E — 原型 release.html 蓝绿/金丝雀/滚动 + DORA */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

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
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_release', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-Rel-F001 创建发布单 (canary 策略)', async () => {
    const r = await api.post('/business/release', {
      projectId,
      version: `v1.0.0-${RUN_ID.slice(-4)}`,
      strategy: 'canary',
      environment: 'prod',
      releaseNotes: '# v1.0.0\n- 新增导出 Excel\n- 修复登录超时',
      releasedByUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
