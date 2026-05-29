// PLM Sprint 列表压测 — 负载场景(load)
// 50 VU × 5min,P95 < 500ms,错误率 < 0.1%
// 跑法: k6 run --env-file env/local.env scenarios/sprint-list-get.js
//
// 依赖:压测前需先跑 seed-project.sql(项目 id 100-149)+ seed-sprint.sql(sprint id 200-249)

import { sleep } from 'k6'
import { login } from '../lib/auth.js'
import { plmGet } from '../lib/http.js'
import { randomInt, pickOne } from '../lib/data.js'

export const options = {
  scenarios: {
    sprint_load_list: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 50 },  // 爬坡到 50 VU
        { duration: '4m',  target: 50 },  // 稳态 4min
        { duration: '30s', target: 0 },   // 降回
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    // 业务基线:GET 列表 P95 < 500ms,P99 < 1000ms
    'http_req_duration{name:sprint-list}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:sprint-detail}': ['p(95)<300'],
    'http_req_failed': ['rate<0.001'],   // < 0.1%
    'checks': ['rate>0.999'],
  },
}

const SPRINT_STATUSES = ['0', '1', '2', '3', '']
const PROJECT_IDS = [100, 101, 102, 103, 104, 105, '']

export default function () {
  const token = login()

  // Scenario 70%: 翻页 list(无筛选)
  if (Math.random() < 0.7) {
    const page = randomInt(1, 5)
    plmGet(
      token,
      '/business/sprint/list',
      { pageNum: page, pageSize: 20 },
      'sprint-list'
    )
  } else {
    // Scenario 30%: 带筛选 list(projectId + status 组合)
    plmGet(
      token,
      '/business/sprint/list',
      {
        pageNum: 1,
        pageSize: 20,
        projectId: pickOne(PROJECT_IDS),
        status: pickOne(SPRINT_STATUSES),
      },
      'sprint-list'
    )
  }

  // 偶尔抽 1 个详情看(模拟点击)— seed 后 sprint id 200-249
  if (Math.random() < 0.2) {
    plmGet(token, `/business/sprint/${randomInt(200, 249)}`, {}, 'sprint-detail')
  }

  sleep(Number(__ENV.THINK_TIME || 1))
}
