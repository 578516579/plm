import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/analytics',
    name: 'Analytics',
    component: () => import('./views/index.vue'),
    meta: { title: '效能分析', icon: 'data-line', stub: true }
  }
]

export default routes
