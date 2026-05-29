/**
 * Sprint 模块 E2E 测试 — 覆盖业务硬规则 703 + actual_dates 自动填充
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData, makeSprintData, ERROR_CODES } from './helpers/fixtures'

let token: string
let api: ApiClient
let apiRequest: APIRequestContext
let projectId: number

test.describe('Sprint 模块 E2E', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)

    const proj = await api.createProject(makeProjectData(`spr-suite-${RUN_ID}`))
    expect(proj.code).toBe(200)
    const list = await api.listProjects()
    projectId = list.rows.find((p: any) => p.projectName.includes(`spr-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_sprint', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-Spr-F001 创建 + ADR-0004 编号生成', async () => {
    const data = makeSprintData(projectId, `crud-${RUN_ID}`)
    const resp = await api.createSprint(data)
    expect(resp.code).toBe(200)

    const list = await api.listSprints()
    const s = list.rows.find((x: any) => x.name === data.name)
    expect(s.sprintNo).toMatch(/^SPR-\d{4}-\d{4}$/)
    expect(s.status).toBe('00')
  })

  test('TC-Spr-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const data = makeSprintData(projectId, `del-${RUN_ID}`)
    const createResp = await api.createSprint(data)
    expect(createResp.code, '创建应成功').toBe(200)

    const before = await api.listSprints()
    const created = before.rows.find((x: any) => x.name === data.name)
    expect(created, '新建 sprint 应能在列表里查到').toBeDefined()
    const id: number = created.sprintId
    expect(typeof id, 'sprintId 应是 number').toBe('number')

    const delResp = await api.deleteSprint(id)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.listSprints()
    const stillThere = after.rows.find((x: any) => x.sprintId === id)
    expect(stillThere, `sprintId=${id} 删除后不该出现在 list`).toBeUndefined()
  })

  test('TC-Spr-F003 actual_start_date 自动填充 (00→01)', async () => {
    const data = makeSprintData(projectId, `actual-${RUN_ID}`)
    const create = await api.createSprint(data)
    expect(create.code).toBe(200)
    const list = await api.listSprints()
    const s = list.rows.find((x: any) => x.name === data.name)

    // 推 00 → 01
    const upd = await api.updateSprint({ sprintId: s.sprintId, status: '01' })
    expect(upd.code).toBe(200)

    // 查 DB 确认 actual_start_date 被自动填了
    const reloaded = await api.get(`/business/sprint/${s.sprintId}`)
    expect(reloaded.data.status).toBe('01')
    expect(reloaded.data.actualStartDate).toBeTruthy()
    expect(reloaded.data.actualStartDate).toMatch(/^\d{4}-\d{2}-\d{2}/)

    // 测试后释放 703 锁,避免影响下一个测试
    await api.updateSprint({ sprintId: s.sprintId, status: '02' })
  })

  test('TC-Spr-F004 业务硬规则 703 项目级单一活跃', async () => {
    // 防御: 把任何项目下残留的活跃迭代推到 02 完成,释放 703 锁
    const existingActive = await api.currentSprint(projectId)
    if (existingActive.data) {
      await api.updateSprint({ sprintId: existingActive.data.sprintId, status: '02' })
    }

    // 两个迭代,推一个到 01,另一个再推 01 必须被拒
    const dataA = makeSprintData(projectId, `703a-${RUN_ID}`)
    const dataB = makeSprintData(projectId, `703b-${RUN_ID}`)
    const ca = await api.createSprint(dataA)
    const cb = await api.createSprint(dataB)
    expect(ca.code).toBe(200)
    expect(cb.code).toBe(200)

    const list = await api.listSprints()
    const a = list.rows.find((x: any) => x.name === dataA.name)
    const b = list.rows.find((x: any) => x.name === dataB.name)

    // A 推 01 — OK
    let resp = await api.updateSprint({ sprintId: a.sprintId, status: '01' })
    expect(resp.code).toBe(200)

    // B 推 01 — 应被 703 拒绝
    resp = await api.updateSprint({ sprintId: b.sprintId, status: '01' })
    expect(resp.code).toBe(ERROR_CODES.SPRINT_SINGLE_ACTIVE)
    expect(resp.msg).toContain('进行中的迭代')

    // A 推 02 完成后,B 应能进 01
    resp = await api.updateSprint({ sprintId: a.sprintId, status: '02' })
    expect(resp.code).toBe(200)
    resp = await api.updateSprint({ sprintId: b.sprintId, status: '01' })
    expect(resp.code, '完成 A 后,B 应可激活 (703 释放)').toBe(200)
  })

  test('TC-Spr-F005 当前活跃迭代 current 端点', async () => {
    const cur = await api.currentSprint(projectId)
    expect(cur.code).toBe(200)
    // data 可能是 null (无活跃) 或 Sprint 对象,但不应该报错
    if (cur.data) {
      expect(cur.data.projectId).toBe(projectId)
      expect(cur.data.status).toBe('01')
    }
  })

  test('TC-Spr-F006 健康度统计 stats (通过 ITaskQueryService)', async () => {
    // 找到一个状态=01 的 sprint (前置测试创建的)
    const list = await api.listSprints()
    const activeS = list.rows.find((x: any) => x.projectId === projectId && x.status === '01')
    if (!activeS) {
      console.warn('跳过: 没有活跃 sprint 可测 stats')
      return
    }

    const stats = await api.sprintStats(activeS.sprintId)
    expect(stats.code).toBe(200)
    expect(stats.data).toHaveProperty('plannedTaskCount')
    expect(stats.data).toHaveProperty('completedTaskCount')
    expect(stats.data).toHaveProperty('completeRate')
    expect(stats.data).toHaveProperty('onTime')
    // 数字类型校验
    expect(typeof stats.data.plannedTaskCount).toBe('number')
    expect(typeof stats.data.completeRate).toBe('number')
  })

  test('UI 层: 迭代管理菜单可访问', async ({ page, context }) => {
    await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])
    await page.goto('/business/sprint')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })
  })
})
