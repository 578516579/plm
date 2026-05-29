/**
 * 跨模块流程 E2E — 0028 §8 信号 6(目标 ≥ 5 跨模块 E2E case)
 *
 * 覆盖 0028 epic 5 主线贯通的核心跨模块端点:
 *   TC-XM-001: inception promote-to-project 幂等晋升(P0-2B + ADR-0010)
 *   TC-XM-002: submission attach-testplan 跨模块拉起(P0-2B)
 *   TC-XM-003: defect.testcaseId FK 写入 + 同 projectId 校验(P0-1b)
 *   TC-XM-004: release.pipelineId FK + SPI 同 projectId 校验(P0-1c + P0-2A SPI)
 *   TC-XM-005: pipeline.releaseId 反向 FK + SPI 校验(P0-1d known limit 关闭 ADR-0012)
 *
 * 关联:
 *   - proposal 0028 §3 P0-2 + P0-1
 *   - ADR-0010 inception promote-to-project
 *   - ADR-0012 SPI 跨模块模式
 *   - reflect 2026-W22-mainline-uplift §2 模式 3
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext
let projectIdA: number, projectIdB: number  // A = 主项目 / B = 跨项目校验用

test.describe('跨模块流程 E2E (0028 §8 信号 6,5 case)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)

    // 创建 2 个项目(A 主 / B 跨项目校验用,验证同 projectId 强校验)
    const pa = await api.post('/business/project', {
      projectName: `E2E-XM-A-${RUN_ID}`,
      projectStatus: '1',  // 进行中
      ownerUserId: 1
    })
    expect(pa.code).toBe(200)
    projectIdA = pa.data?.projectId || pa.data?.id || pa.data

    const pb = await api.post('/business/project', {
      projectName: `E2E-XM-B-${RUN_ID}`,
      projectStatus: '1',
      ownerUserId: 1
    })
    expect(pb.code).toBe(200)
    projectIdB = pb.data?.projectId || pb.data?.id || pb.data
  })

  test.afterAll(async () => {
    // 反向 FK 顺序清理:defect → testcase → release → pipeline → submission → testplan → inception → project
    execDelete('tb_defect', `title like 'E2E-XM-${RUN_ID}%'`)
    execDelete('tb_testcase', `title like 'E2E-XM-${RUN_ID}%'`)
    execDelete('tb_release', `release_no like 'XM-${RUN_ID}%' or version like '%${RUN_ID}%'`)
    execDelete('tb_pipeline', `pipeline_name like 'E2E-XM-${RUN_ID}%'`)
    execDelete('tb_submission', `title like 'E2E-XM-${RUN_ID}%'`)
    execDelete('tb_testplan', `title like 'E2E-XM-${RUN_ID}%'`)
    execDelete('tb_inception', `project_name like 'E2E-XM-${RUN_ID}%'`)
    execDelete('tb_project', `project_name like 'E2E-XM-%${RUN_ID}%'`)
    await apiRequest?.dispose()
  })

  /**
   * TC-XM-001: inception → project 幂等晋升
   * 路径:创建 inception(status=02 审批通过)→ promoteToProject →
   *      验证 inception.projectId 写入 + project 已建 + 重复调用幂等返回同 id
   * ADR-0010 决策:首屏 projectId 非空检查 + 同事务回写
   */
  test('TC-XM-001 inception promote-to-project 幂等晋升', async () => {
    // Setup: 创建 inception 并推到 status=02 审批通过
    const inc = await api.post('/business/inception', {
      projectName: `E2E-XM-${RUN_ID}-病虫害识别`,
      businessLine: 'plant_protection',
      inceptionType: 'new_product',
      background: 'E2E 跨模块 TC-XM-001 测试样例',
      submitterUserId: 1,
      status: '02'  // 直接置已批,跳过 00→01→02 状态机
    })
    expect(inc.code).toBe(200)
    const inceptionId = inc.data?.inceptionId || inc.data?.id || inc.data

    // 第一次 promote → 应建 project + 写回 projectId
    const promote1 = await api.post(`/business/inception/${inceptionId}/promote-to-project`, {})
    expect(promote1.code).toBe(200)
    const projectId1 = promote1.data?.projectId || promote1.data?.id || promote1.data
    expect(projectId1).toBeTruthy()

    // 第二次 promote → 幂等,返回同 projectId(不重建)
    const promote2 = await api.post(`/business/inception/${inceptionId}/promote-to-project`, {})
    expect(promote2.code).toBe(200)
    const projectId2 = promote2.data?.projectId || promote2.data?.id || promote2.data
    expect(projectId2).toBe(projectId1)  // ADR-0010 幂等防线

    // 验证 inception.projectId 已锁定
    const incVerify = await api.get(`/business/inception/${inceptionId}`)
    expect(incVerify.data?.projectId).toBe(projectId1)
  })

  /**
   * TC-XM-002: submission attach-testplan 跨模块拉起
   * 路径:创建 submission + 同 projectId 下创建 testplan → attach → 验证 testplanId 写入
   * + 跨项目 testplan attach → 应 702
   * P0-1a + P0-2B endpoint
   */
  test('TC-XM-002 submission attach-testplan 跨模块拉起 + 跨项目拒绝', async () => {
    // 同 projectId 下 submission + testplan
    const sub = await api.post('/business/submission', {
      title: `E2E-XM-${RUN_ID}-提测`,
      projectId: projectIdA,
      scope: 'API + UI 全量',
      environment: 'sit',
      submitterUserId: 1
    })
    expect(sub.code).toBe(200)
    const submissionId = sub.data?.submissionId || sub.data?.id || sub.data

    const tp = await api.post('/business/testplan', {
      title: `E2E-XM-${RUN_ID}-方案`,
      projectId: projectIdA,
      testType: '01,02',  // 功能+性能
      authorUserId: 1
    })
    expect(tp.code).toBe(200)
    const testplanId = tp.data?.testplanId || tp.data?.id || tp.data

    // attach 正常路径
    const attach = await api.post(`/business/submission/${submissionId}/attach-testplan`, {
      testplanId
    })
    expect(attach.code).toBe(200)

    // 验证 submission.testplanId 写回
    const subVerify = await api.get(`/business/submission/${submissionId}`)
    expect(subVerify.data?.testplanId).toBe(testplanId)

    // 跨项目 attach → 702(同 projectId 强校验)
    const tpB = await api.post('/business/testplan', {
      title: `E2E-XM-${RUN_ID}-跨项目方案`,
      projectId: projectIdB,  // 跨项目!
      testType: '01',
      authorUserId: 1
    })
    expect(tpB.code).toBe(200)
    const testplanIdB = tpB.data?.testplanId || tpB.data?.id || tpB.data

    const crossAttach = await api.post(`/business/submission/${submissionId}/attach-testplan`, {
      testplanId: testplanIdB
    })
    expect(crossAttach.code).not.toBe(200)  // 应 702 同 projectId 校验
  })

  /**
   * TC-XM-003: defect.testcaseId FK 写入 + 同 projectId 校验
   * 路径:创建 testcase + 跨模块 defect with testcaseId → 验证 FK + 同 projectId 强校验
   * P0-1b
   */
  test('TC-XM-003 defect.testcaseId FK + 同 projectId 强校验', async () => {
    const tc = await api.post('/business/testcase', {
      title: `E2E-XM-${RUN_ID}-用例`,
      projectId: projectIdA,
      category: '01',  // 功能
      priority: '00',  // P0
      authorUserId: 1
    })
    expect(tc.code).toBe(200)
    const testcaseId = tc.data?.testcaseId || tc.data?.id || tc.data

    // 同 projectId defect with testcaseId
    const defect = await api.post('/business/defect', {
      title: `E2E-XM-${RUN_ID}-缺陷`,
      projectId: projectIdA,
      testcaseId,  // P0-1b 新外键
      severity: '00',  // P0
      reporterUserId: 1
    })
    expect(defect.code).toBe(200)
    const defectId = defect.data?.defectId || defect.data?.id || defect.data

    // 验证 defect.testcaseId 持久化
    const defectVerify = await api.get(`/business/defect/${defectId}`)
    expect(defectVerify.data?.testcaseId).toBe(testcaseId)

    // 跨项目 defect with 项目 A 的 testcaseId → 应 702
    const crossDefect = await api.post('/business/defect', {
      title: `E2E-XM-${RUN_ID}-跨项目缺陷`,
      projectId: projectIdB,
      testcaseId,  // 项目 A 的 testcaseId,但 defect.projectId=B → 不一致
      severity: '01',
      reporterUserId: 1
    })
    // 应被 ServiceImpl 同 projectId 校验拒绝(702)或者允许 NULL 化
    // (根据 P0-1b 设计:testcaseId 可空,但若非空必须同 projectId)
    if (crossDefect.code === 200) {
      // 若后端允许,起码 testcaseId 应被置 null(P1 TODO 可改)
      const verify = await api.get(`/business/defect/${crossDefect.data?.defectId || crossDefect.data}`)
      expect(verify.data?.testcaseId).not.toBe(testcaseId)
    } else {
      expect(crossDefect.code).not.toBe(200)
    }
  })

  /**
   * TC-XM-004: release.pipelineId FK + SPI 同 projectId 校验
   * 路径:同项目 pipeline + release with pipelineId → 验证 SPI ProjectScopedLookup 工作
   * P0-1c + P0-2A SPI(ADR-0012)
   */
  test('TC-XM-004 release.pipelineId FK + SPI ProjectScopedLookup 同 projectId 校验', async () => {
    const pipe = await api.post('/business/pipeline', {
      pipelineName: `E2E-XM-${RUN_ID}-流水线`,
      projectId: projectIdA,
      repoName: `e2e-xm-${RUN_ID}`,
      repoBranch: 'main',
      cicdTool: 'jenkins',
      triggerType: 'manual',
      authorUserId: 1
    })
    expect(pipe.code).toBe(200)
    const pipelineId = pipe.data?.pipelineId || pipe.data?.id || pipe.data

    // 同 projectId release with pipelineId
    const rel = await api.post('/business/release', {
      version: `v0.1-${RUN_ID}`,
      projectId: projectIdA,
      pipelineId,  // P0-1c 新外键 + P0-2A SPI 校验
      strategy: 'blue_green',
      environment: 'sit',
      releasedByUserId: 1
    })
    expect(rel.code).toBe(200)
    const releaseId = rel.data?.releaseId || rel.data?.id || rel.data

    // 验证 release.pipelineId 持久化
    const relVerify = await api.get(`/business/release/${releaseId}`)
    expect(relVerify.data?.pipelineId).toBe(pipelineId)

    // 跨项目 release(B)with 项目 A 的 pipelineId → SPI 应识别不同 projectId
    const crossRel = await api.post('/business/release', {
      version: `v0.2-XM-${RUN_ID}`,
      projectId: projectIdB,
      pipelineId,  // 项目 A 的!
      strategy: 'rolling',
      environment: 'sit',
      releasedByUserId: 1
    })
    if (crossRel.code === 200) {
      // 若后端容忍,起码 SPI 应该 nullify 或返回非 200
      const verify = await api.get(`/business/release/${crossRel.data?.releaseId || crossRel.data}`)
      expect(verify.data?.pipelineId).not.toBe(pipelineId)
    } else {
      expect(crossRel.code).not.toBe(200)  // SPI 拒绝
    }
  })

  /**
   * TC-XM-005: pipeline.releaseId 反向 FK + SPI 校验(known limit 关闭)
   * 路径:同项目 pipeline 更新 with releaseId → 验证 DoraAggregationSource SPI 同范式
   * P0-1d known limitation 关闭 commit `21b7166`(ADR-0012)
   */
  test('TC-XM-005 pipeline.releaseId 反向 FK + SPI 校验(P0-1d known limit 关闭)', async () => {
    const rel = await api.post('/business/release', {
      version: `v0.3-XM-${RUN_ID}`,
      projectId: projectIdA,
      strategy: 'canary',
      environment: 'sit',
      releasedByUserId: 1
    })
    expect(rel.code).toBe(200)
    const releaseId = rel.data?.releaseId || rel.data?.id || rel.data

    const pipe = await api.post('/business/pipeline', {
      pipelineName: `E2E-XM-${RUN_ID}-反向流水线`,
      projectId: projectIdA,
      releaseId,  // P0-1d 反向外键 + SPI 校验
      repoName: `e2e-xm-rev-${RUN_ID}`,
      repoBranch: 'main',
      cicdTool: 'gitlab',
      triggerType: 'push',
      authorUserId: 1
    })
    expect(pipe.code).toBe(200)
    const pipelineId = pipe.data?.pipelineId || pipe.data?.id || pipe.data

    // 验证 pipeline.releaseId 写入(known limit 关闭后应正常)
    const pipeVerify = await api.get(`/business/pipeline/${pipelineId}`)
    expect(pipeVerify.data?.releaseId).toBe(releaseId)

    // 跨项目 pipeline B with 项目 A 的 releaseId → SPI 同款拒绝
    const crossPipe = await api.post('/business/pipeline', {
      pipelineName: `E2E-XM-${RUN_ID}-跨项目流水线`,
      projectId: projectIdB,
      releaseId,  // 项目 A 的!
      repoName: `e2e-xm-cross-${RUN_ID}`,
      repoBranch: 'main',
      cicdTool: 'jenkins',
      triggerType: 'manual',
      authorUserId: 1
    })
    if (crossPipe.code === 200) {
      const verify = await api.get(`/business/pipeline/${crossPipe.data?.pipelineId || crossPipe.data}`)
      expect(verify.data?.releaseId).not.toBe(releaseId)
    } else {
      expect(crossPipe.code).not.toBe(200)  // SPI 同款拒绝
    }
  })
})
