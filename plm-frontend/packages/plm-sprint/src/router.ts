import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/sprint',
    name: 'Sprint',
    component: () => import('./views/index.vue'),
    meta: { title: '迭代管理', icon: 'time', permi: 'business:sprint:list' }
  }
]

export default routes
