// PLM Task 新增并发 — 压力场景(stress)
// 100 VU × 2min,P95 < 800ms,错误率 < 1%
// 跑法: k6 run --env-file env/local.env scenarios/task-create-post.js
//
// 依赖:压测前需先跑 seed-project.sql(项目 id 100-149)+ seed-sprint.sql(sprint id 200-249)

import { sleep } from 'k6'
import { Counter } from 'k6/metrics'
import { login } from '../lib/auth.js'
import { plmPost } from '../lib/http.js'
import { fakeTask, randomInt } from '../lib/data.js'

const createSuccessCounter = new Counter('task_create_success')

export const options = {
  scenarios: {
    task_stress_create: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 100 },
        { duration: '90s', target: 100 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    'http_req_duration{name:task-create}': ['p(95)<800', 'p(99)<2000'],
    'http_req_failed': ['rate<0.01'],
    'checks': ['rate>0.99'],
  },
}

export default function () {
  const token = login()

  const projectId = randomInt(100, 149)
  const sprintId = randomInt(200, 249)
  const payload = fakeTask(projectId, sprintId)
  const res = plmPost(token, '/business/task', payload, 'task-create')

  const code = res.json('code')
  if (code === 200) {
    createSuccessCounter.add(1)
  }

  sleep(Number(__ENV.THINK_TIME || 0.5))
}

// 清理:压测会插入大量数据,跑完后清
// DELETE FROM tb_task WHERE create_by='admin' AND title LIKE 'Task-压测-%';
