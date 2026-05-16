/** 接口设计模块 E2E — PRD §F3.3 接口详细设计 + 原型 apidesign.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('接口设计模块 E2E (PRD §F3.3)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`apidesign-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`apidesign-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_apidesign', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-APIDESIGN-F001 创建接口设计 (POST 创建农事记录)', async () => {
    const r = await api.post('/business/apidesign', {
      projectId,
      title: `创建农事记录接口-${RUN_ID}`,
      httpMethod: 'post',
      apiPath: '/business/farming-records',
      description: '创建农事操作记录，包含施肥、灌溉、喷药等操作类型',
      version: 'v1.0',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-APIDESIGN-F002 创建接口设计 (GET 查询土壤墒情)', async () => {
    const r = await api.post('/business/apidesign', {
      projectId,
      title: `查询土壤墒情接口-${RUN_ID}`,
      httpMethod: 'get',
      apiPath: '/business/soil-moisture',
      description: '按地块和时间范围查询土壤墒情传感器数据',
      version: 'v1.0',
      mockEnabled: 'Y',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
