import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/pipeline',
    name: 'Pipeline',
    component: () => import('./views/index.vue'),
    meta: { title: 'CI/CD 流水线', icon: 'share', stub: true }
  }
]

export default routes
