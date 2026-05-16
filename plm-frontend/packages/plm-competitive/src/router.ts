import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/competitive',
    name: 'Competitive',
    component: () => import('./views/index.vue'),
    meta: { title: '竞品情报', icon: 'chart', stub: true }
  }
]

export default routes
