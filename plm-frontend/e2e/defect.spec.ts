/**
 * Defect 模块 E2E 测试 — 5×5 状态机含反向边 03→01 (回归打回) + 进入 03 必填 resolution + 3 FK
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import {
  RUN_ID,
  makeProjectData,
  makeSprintData,
  makeRequirementData,
  makeTaskData,
  ERROR_CODES
} from './helpers/fixtures'
import { makeDefectData, DEFECT_STATUS_TRANSITIONS } from './helpers/fixtures-defect'

let token: string
let api: ApiClient
let apiRequest: APIRequestContext
let projectId: number
let sprintId: number
let taskId: number

test.describe('Defect 模块 E2E', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)

    // Project FK
    await api.createProject(makeProjectData(`defect-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`defect-suite-${RUN_ID}`))?.id
    expect(projectId).toBeDefined()

    // Sprint FK (可选)
    await api.createSprint(makeSprintData(projectId, `defect-suite-${RUN_ID}`))
    const sl = await api.listSprints()
    sprintId = sl.rows.find((s: any) => s.name.includes(`defect-suite-${RUN_ID}`))?.sprintId

    // Task FK (可选)
    await api.createRequirement(makeRequirementData(projectId, `defect-suite-${RUN_ID}`))
    const rl = await api.listRequirements()
    const requirementId = rl.rows.find((r: any) => r.title.includes(`defect-suite-${RUN_ID}`))?.requirementId
    await api.createTask(makeTaskData(projectId, sprintId, requirementId, `defect-suite-${RUN_ID}`))
    const tl = await api.listTasks()
    taskId = tl.rows.find((t: any) => t.title.includes(`defect-suite-${RUN_ID}`))?.taskId
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_defect', `project_id=${projectId}`)
      if (taskId) execDelete('tb_task', `task_id=${taskId}`)
      if (sprintId) execDelete('tb_sprint', `sprint_id=${sprintId}`)
      execDelete('tb_requirement', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-Defect-F001 CRUD + ADR-0005 编号', async () => {
    const data = makeDefectData(projectId, `crud-${RUN_ID}`)
    const resp = await api.post('/business/defect', data)
    expect(resp.code).toBe(200)
    const list = await api.get('/business/defect/list', { pageSize: 100 })
    const d = list.rows.find((x: any) => x.title === data.title)
    expect(d).toBeDefined()
    expect(d.defectNo).toMatch(/^DEFECT-\d{4}-\d{4}$/)
    expect(d.status).toBe('00')
    expect(d.severity).toBe('01')
    expect(d.category).toBe('01')
  })

  test('TC-Defect-F003 反向边 03→00 重开 (ADR-D)', async () => {
    const data = makeDefectData(projectId, `reverse-${RUN_ID}`)
    const c = await api.post('/business/defect', data)
    expect(c.code).toBe(200)
    const list = await api.get('/business/defect/list', { pageSize: 100 })
    const d = list.rows.find((x: any) => x.title === data.title)
    const id = d.defectId

    // 推 00 待确认 → 01 修复中
    let r = await api.put('/business/defect', { defectId: id, status: '01' })
    expect(r.code).toBe(200)
    // 01 修复中 → 02 待验证 必填 resolution
    r = await api.put('/business/defect', { defectId: id, status: '02' })
    expect(r.code, '进入 02 待验证 无 resolution → 705').toBe(705)
    r = await api.put('/business/defect', { defectId: id, status: '02', resolution: '已修复,通过 commit abc123' })
    expect(r.code).toBe(200)
    // 02 待验证 → 03 已关闭
    r = await api.put('/business/defect', { defectId: id, status: '03' })
    expect(r.code).toBe(200)

    // 反向边 03 已关闭 → 00 待确认 (重开)
    r = await api.put('/business/defect', { defectId: id, status: '00' })
    expect(r.code, '反向边 03→00 (重开) 应允许').toBe(200)
  })

  test('TC-Defect-F004 进入 02 待验证 必填 resolution (ADR-D D3)', async () => {
    const data = makeDefectData(projectId, `res-${RUN_ID}`)
    const c = await api.post('/business/defect', data)
    expect(c.code).toBe(200)
    const list = await api.get('/business/defect/list', { pageSize: 100 })
    const d = list.rows.find((x: any) => x.title === data.title)
    const id = d.defectId

    // 推 00 → 01
    await api.put('/business/defect', { defectId: id, status: '01' })

    // 不填 resolution 推 02 应失败 705
    let r = await api.put('/business/defect', { defectId: id, status: '02' })
    expect(r.code).toBe(705)
    expect(r.msg).toContain('解决说明')

    // 带 resolution 推 02 应成功
    r = await api.put('/business/defect', { defectId: id, status: '02', resolution: '修复说明' })
    expect(r.code).toBe(200)
  })

  test('TC-Defect-F005 状态机非法转换全覆盖', async () => {
    for (const tc of DEFECT_STATUS_TRANSITIONS.illegal) {
      const data = makeDefectData(projectId, `illegal-${tc.from}${tc.to}-${RUN_ID}`)
      const c = await api.post('/business/defect', data)
      expect(c.code).toBe(200)
      const list = await api.get('/business/defect/list', { pageSize: 100 })
      const d = list.rows.find((x: any) => x.title === data.title)
      const id = d.defectId

      // 推到 from 状态 (00 已是,其他需中间步骤)
      // ADR-D 新 4 态:00 待确认 / 01 修复中 / 02 待验证 / 03 已关闭
      if (tc.from === '01') {
        await api.put('/business/defect', { defectId: id, status: '01' })
      } else if (tc.from === '02') {
        await api.put('/business/defect', { defectId: id, status: '01' })
        await api.put('/business/defect', { defectId: id, status: '02', resolution: 'auto' })
      } else if (tc.from === '03') {
        // 00 → 03 直接关闭 (重复/无效快关,合法转换)
        await api.put('/business/defect', { defectId: id, status: '03' })
      }

      // 非法转换
      const r = await api.put('/business/defect', { defectId: id, status: tc.to })
      expect.soft(r.code, `${tc.name} 应被拒 (601)`).toBe(ERROR_CODES.STATUS_VIOLATION)
    }
  })

  test('TC-Defect-F006 3 FK 校验', async () => {
    // 不存在的 projectId
    let r = await api.post('/business/defect', { ...makeDefectData(projectId), projectId: 99999 })
    expect(r.code).toBe(ERROR_CODES.FK_NOT_EXISTS)

    // 不存在的 sprintId
    r = await api.post('/business/defect', { ...makeDefectData(projectId), sprintId: 99999 })
    expect(r.code).toBe(ERROR_CODES.FK_NOT_EXISTS)
    expect(r.msg).toContain('迭代')

    // 不存在的 taskId
    r = await api.post('/business/defect', { ...makeDefectData(projectId), taskId: 99999 })
    expect(r.code).toBe(ERROR_CODES.FK_NOT_EXISTS)
    expect(r.msg).toContain('任务')
  })

  test('TC-Defect-F007 新建状态必须为 00', async () => {
    const r = await api.post('/business/defect', {
      ...makeDefectData(projectId, `status-${RUN_ID}`),
      status: '02'
    })
    expect(r.code).toBe(ERROR_CODES.STATUS_VIOLATION)
  })

  test('TC-Defect-F008 关联 Sprint + Task FK 联调', async () => {
    const r = await api.post('/business/defect', {
      ...makeDefectData(projectId, `fk-ok-${RUN_ID}`),
      sprintId,
      taskId
    })
    expect(r.code).toBe(200)
  })

  test('UI 层: 缺陷管理菜单可访问', async ({ page, context }) => {
    await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])
    await page.goto('/business/defect')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })
    // 验证有"严重级别""分类"两个搜索字段(Defect 独有,区别于其他模块)
    await expect(page.getByText(/严重级别/).first()).toBeVisible()
    await expect(page.getByText(/分类/).first()).toBeVisible()
  })
})
