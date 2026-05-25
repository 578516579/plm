/**
 * 业务模块前端路由 — entity → 完整 path 单一事实来源 (SSoT)
 *
 * **为什么需要这个文件**:
 * menu-regroup-by-phase.sql 把业务菜单从平铺 parent_id=2000「业务管理」
 * 重分组到 8 阶段 parent_id=2900-2970, sys_menu 拼出的前端 URL 从
 * /business/<entity> 变成 /<phase>/<entity>. 任何 view 里硬编码
 * `router.push('/business/${entity}')` 都会被 vue-router catch-all
 * (`/:pathMatch(.*)*` → 404.vue) 接住, 表现为按钮点击 404.
 *
 * 修复策略 = 抽单一映射表 + 各 view 改用 entityToPath(entity).
 *
 * **映射数据源**: 跑下面 SQL 重新生成
 * ```sql
 *   SELECT m.path AS entity, p.path AS phase_path
 *   FROM sys_menu m JOIN sys_menu p ON p.menu_id = m.parent_id
 *   WHERE m.perms LIKE 'business:%:list'
 *   ORDER BY p.menu_id, m.menu_id;
 * ```
 *
 * **后续变动**: 若 sys_menu 分组再调整, 仅需更新本文件 (+ 跑 SQL 重新校对).
 */
export const ENTITY_TO_PATH: Record<string, string> = {
  // workbench (2900)
  dashboard:        '/workbench/dashboard',

  // phase-plan (2910) 规划阶段
  project:          '/phase-plan/project',
  inception:        '/phase-plan/inception',
  competitive:      '/phase-plan/competitive',

  // phase-design (2920) 需求与设计
  requirement:      '/phase-design/requirement',
  prd:              '/phase-design/prd',
  ued:              '/phase-design/ued',
  arch:             '/phase-design/arch',
  dbdesign:         '/phase-design/dbdesign',
  apidesign:        '/phase-design/apidesign',
  document:         '/phase-design/document',

  // phase-dev (2930) 研发阶段
  task:             '/phase-dev/task',
  mytask:           '/phase-dev/mytask',
  sprint:           '/phase-dev/sprint',

  // phase-test (2940) 测试阶段
  defect:           '/phase-test/defect',
  testcase:         '/phase-test/testcase',
  submission:       '/phase-test/submission',
  autotest:         '/phase-test/autotest',
  testplan:         '/phase-test/testplan',
  testreport:       '/phase-test/testreport',
  testdata:         '/phase-test/testdata',

  // phase-deploy (2950) 交付与运维
  release:          '/phase-deploy/release',
  apidoc:           '/phase-deploy/apidoc',
  'manual-product': '/phase-deploy/manual-product',
  'manual-impl':    '/phase-deploy/manual-impl',
  'manual-ops':     '/phase-deploy/manual-ops',
  pipeline:         '/phase-deploy/pipeline',
  'feature-flag':   '/phase-deploy/feature-flag',
  dora:             '/phase-deploy/dora',

  // phase-ai (2960) AI 能力
  'ai-agent':            '/phase-ai/ai-agent',
  openspec:              '/phase-ai/openspec',
  'ai-invocation-log':   '/phase-ai/ai-invocation-log',

  // phase-report (2970) 分析报表
  analytics:        '/phase-report/analytics'
}

/**
 * 把业务 entity (如 'project' / 'inception') 转成完整 vue-router path.
 *
 * @param entity 业务模块 key, 见上方 ENTITY_TO_PATH 表
 * @returns 完整 path 如 '/phase-plan/inception'; 未匹配时回落到 `/business/${entity}` (兼容旧路径,
 *          实际仍会触发 404, 但留给调用方 catch 决定如何提示用户)
 */
export function entityToPath(entity: string): string {
  return ENTITY_TO_PATH[entity] || `/business/${entity}`
}
