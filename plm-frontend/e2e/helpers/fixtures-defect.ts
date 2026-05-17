/**
 * Defect 模块测试数据 + 状态机 (附加到 fixtures.ts 主集)
 */
import { RUN_ID } from './fixtures'

export function makeDefectData(projectId: number, suffix?: string) {
  const tag = suffix || RUN_ID
  return {
    projectId,
    title: `E2E 缺陷-${tag}`,
    description: `自动测试发现的 bug ${tag}`,
    severity: '01',          // P1 严重
    category: '01',          // 功能
    reporterUserId: 1,
    reproduceSteps: '1. 登录\n2. 进入项目管理\n3. 点击新增',
    expectedResult: '能正常新增项目',
    actualResult: '提交后报 500 错误',
    tags: 'regression,automation'
  }
}

/**
 * Defect 状态机测试矩阵 (v2 ADR-D Option A,4 主态 + 反向边 03→00)
 * 引用 PRD-MAPPING.md §2 Requirement / Defect (proposal 0302)
 */
export const DEFECT_STATUS_TRANSITIONS = {
  legal: [
    { from: '00', to: '01', name: '待确认→修复中' },
    { from: '00', to: '03', name: '待确认→已关闭 (重复/无效快关)' },
    { from: '01', to: '00', name: '修复中→待确认 (回退)' },
    { from: '01', to: '02', name: '修复中→待验证 (需带 resolution)' },
    { from: '02', to: '01', name: '待验证→修复中 (验证失败打回)' },
    { from: '02', to: '03', name: '待验证→已关闭' },
    { from: '03', to: '00', name: '已关闭→待确认 (反向边·重开)' }
  ],
  illegal: [
    { from: '00', to: '02', name: '待确认→待验证 (跨级)' },
    { from: '01', to: '03', name: '修复中→已关闭 (跨级,必须经待验证)' },
    { from: '03', to: '01', name: '已关闭→修复中 (跨级反向,只允许 03→00)' },
    { from: '03', to: '02', name: '已关闭→待验证 (跨级反向)' }
  ]
}
