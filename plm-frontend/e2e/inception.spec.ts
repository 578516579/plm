/** Inception 模块 E2E — PRD §F1.1 立项 AI 助手 + 原型 inception.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext

test.describe('Inception 模块 E2E (PRD §F1.1)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
  })

  test.afterAll(async () => {
    execDelete('tb_inception', `project_name like 'E2E 立项-${RUN_ID}%'`)
    await apiRequest?.dispose()
  })

  test('TC-Inc-F001 创建立项 (含业务线 + 类型 + 背景诉求)', async () => {
    const r = await api.post('/business/inception', {
      projectName: `E2E 立项-${RUN_ID}-病虫害识别`,
      businessLine: 'plant_protection',
      inceptionType: 'new_product',
      background: '基于 AI 视觉识别技术,实现拍照识别病虫害种类。原型 inception.html L147 同款诉求。',
      estimatedDurationMonths: 6,
      estimatedTeam: '产品×1 前端×2 后端×3 测试×2 AI×2',
      submitterUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
