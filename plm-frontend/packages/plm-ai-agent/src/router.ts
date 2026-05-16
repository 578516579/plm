import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/ai-agent',
    name: 'AiAgent',
    component: () => import('./views/index.vue'),
    meta: { title: 'AI Agent 编排', icon: 'cpu', stub: true }
  }
]

export default routes
