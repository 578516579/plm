/** DbDesign 模块 E2E — PRD §F3.2 数据库设计 + 原型 dbdesign.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('DbDesign 模块 E2E (PRD §F3.2)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`db-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`db-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_dbdesign', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-DB-F001 创建数据库设计 (引擎选型 + AI 生成入口)', async () => {
    const r = await api.post('/business/dbdesign', {
      projectId,
      title: `PLM 业务库设计-${RUN_ID}`,
      dbEngine: 'mysql',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
