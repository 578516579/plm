// PLM Task 列表压测 — 负载场景(load)
// 50 VU × 5min,P95 < 500ms,错误率 < 0.1%
// 跑法: k6 run --env-file env/local.env scenarios/task-list-get.js
//
// 依赖:压测前需先跑 seed-project.sql + seed-sprint.sql + seed-task.sql(task id 300-499)

import { sleep } from 'k6'
import { login } from '../lib/auth.js'
import { plmGet } from '../lib/http.js'
import { randomInt, pickOne } from '../lib/data.js'

export const options = {
  scenarios: {
    task_load_list: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 50 },
        { duration: '4m',  target: 50 },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    'http_req_duration{name:task-list}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:task-detail}': ['p(95)<300'],
    'http_req_failed': ['rate<0.001'],
    'checks': ['rate>0.999'],
  },
}

const TASK_STATUSES = ['0', '1', '2', '3', '4', '']
const PRIORITIES = ['1', '2', '3', '4', '']
const PROJECT_IDS = [100, 101, 102, 103, 104, '']

export default function () {
  const token = login()

  // Scenario 70%: 翻页 list(无筛选)
  if (Math.random() < 0.7) {
    const page = randomInt(1, 5)
    plmGet(
      token,
      '/business/task/list',
      { pageNum: page, pageSize: 20 },
      'task-list'
    )
  } else {
    // Scenario 30%: 带筛选 list(projectId + status + priority 组合)
    plmGet(
      token,
      '/business/task/list',
      {
        pageNum: 1,
        pageSize: 20,
        projectId: pickOne(PROJECT_IDS),
        status: pickOne(TASK_STATUSES),
        priority: pickOne(PRIORITIES),
      },
      'task-list'
    )
  }

  // 偶尔抽 1 个详情看 — seed 后 task id 300-499
  if (Math.random() < 0.2) {
    plmGet(token, `/business/task/${randomInt(300, 499)}`, {}, 'task-detail')
  }

  sleep(Number(__ENV.THINK_TIME || 1))
}
