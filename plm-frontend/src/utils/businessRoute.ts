/**
 * 业务模块前端路由 — entity → 完整 path 单一事实来源 (SSoT)
 *
 * **历史与契约**:
 * 1) v0.5 期 menu-regroup-by-phase.sql 把业务菜单从平铺 parent_id=2000「业务管理」
 *    重分组到 8 阶段 parent_id=2900-2970, 一度导致 sys_menu 拼出的前端 URL 从
 *    /business/<entity> 变成 /<phase>/<entity>, 引入 dashboard 等跳转 404.
 * 2) v0.6 期 menu-path-absolute-business-prefix.sql 修复 P0(E2E /business/* 全 404):
 *    把 35 个业务子菜单 path 改为绝对路径 `/business/<entity>` — RuoYi `getRouterPath()`
 *    遇到 `/` 开头的子菜单 path 时不拼父路径, Vue Router 直接渲染 `/business/<entity>`.
 *    URL 契约恢复为 `/business/<entity>`, 保持 PRD-MAPPING `/business/<module>` SSoT.
 *
 * **本文件状态(2026-05-25 后)**: 仍保留, 用作 view 跳转的统一映射点. 映射统一指向
 * `/business/<entity>` 跟实际菜单 path 对齐. 后续若再次重组, 仅需改本文件 + 配套 SQL.
 *
 * **映射数据源**: 跑下面 SQL 校对
 * ```sql
 *   SELECT m.menu_id, m.menu_name, m.path FROM sys_menu m
 *   WHERE m.perms LIKE 'business:%:list' ORDER BY m.menu_id;
 * ```
 *
 * **后续变动**: 若 sys_menu path 再调整, 仅需更新本文件 (+ 跑 SQL 重新校对).
 */
export const ENTITY_TO_PATH: Record<string, string> = {
  // workbench (2900)
  dashboard:        '/business/dashboard',

  // phase-plan (2910) 规划阶段
  project:          '/business/project',
  inception:        '/business/inception',
  competitive:      '/business/competitive',

  // phase-design (2920) 需求与设计
  requirement:      '/business/requirement',
  prd:              '/business/prd',
  ued:              '/business/ued',
  arch:             '/business/arch',
  dbdesign:         '/business/dbdesign',
  apidesign:        '/business/apidesign',
  document:         '/business/document',

  // phase-dev (2930) 研发阶段
  task:             '/business/task',
  mytask:           '/business/mytask',
  sprint:           '/business/sprint',

  // phase-test (2940) 测试阶段
  defect:           '/business/defect',
  testcase:         '/business/testcase',
  submission:       '/business/submission',
  autotest:         '/business/autotest',
  testplan:         '/business/testplan',
  testreport:       '/business/testreport',
  testdata:         '/business/testdata',

  // phase-deploy (2950) 交付与运维
  release:          '/business/release',
  apidoc:           '/business/apidoc',
  'manual-product': '/business/manual-product',
  'manual-impl':    '/business/manual-impl',
  'manual-ops':     '/business/manual-ops',
  pipeline:         '/business/pipeline',
  'feature-flag':   '/business/feature-flag',
  dora:             '/business/dora',

  // phase-ai (2960) AI 能力
  'ai-agent':            '/business/ai-agent',
  openspec:              '/business/openspec',
  'ai-invocation-log':   '/business/ai-invocation-log',

  // phase-report (2970) 分析报表
  analytics:        '/business/analytics'
}

/**
 * 把业务 entity (如 'project' / 'inception') 转成完整 vue-router path.
 *
 * @param entity 业务模块 key, 见上方 ENTITY_TO_PATH 表
 * @returns 完整 path 如 '/business/inception'; 未匹配时回落到 `/business/${entity}` (兼容默认约定)
 */
export function entityToPath(entity: string): string {
  return ENTITY_TO_PATH[entity] || `/business/${entity}`
}
