import { RUN_ID } from './fixtures'

export function makeTestCaseData(projectId: number, requirementId?: number, suffix?: string) {
  const tag = suffix || RUN_ID
  return {
    projectId,
    requirementId,
    title: `E2E 用例-${tag}`,
    description: `自动化测试 ${tag}`,
    category: 'agri',  // 农业专项 (ADR-B Option B / proposal 0300);旧 '06' E2E 已转入 tags 字段
    priority: '01',  // P1 主要
    preconditions: '后端 + 前端启动 + admin 登录',
    steps: '1. POST /business/testcase\n2. 验证返回 200',
    expectedResult: 'code=200 且 testcaseNo 符合 TC-YYYY-NNNN',
    isAutomated: 'Y',
    automationScriptPath: 'plm-frontend/e2e/testcase.spec.ts',
    tags: 'e2e,smoke'
  }
}

export const TESTCASE_STATUS_TRANSITIONS = {
  legal: [
    { from: '00', to: '01', name: '草稿→待执行' },
    { from: '01', to: '02', name: '待执行→执行中' },
    { from: '02', to: '03', name: '执行中→已通过' },
    { from: '02', to: '04', name: '执行中→已失败' },
    { from: '03', to: '01', name: '已通过→待执行 (反向边·重测)' },
    { from: '04', to: '01', name: '已失败→待执行 (反向边·重测)' }
  ],
  illegal: [
    { from: '00', to: '02', name: '草稿→执行中 (跨级)' },
    { from: '00', to: '03', name: '草稿→已通过 (跨级)' },
    { from: '01', to: '03', name: '待执行→已通过 (跨级)' }
  ]
}
