import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/competitive',
    name: 'Competitive',
    component: () => import('./views/index.vue'),
    meta: { title: '竞品情报', icon: 'chart', permi: 'business:competitive:list' }
  }
]

export default routes
