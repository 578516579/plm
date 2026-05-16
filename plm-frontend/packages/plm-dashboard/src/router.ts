import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/dashboard',
    name: 'Dashboard',
    component: () => import('./views/index.vue'),
    meta: { title: '工作台', icon: 'dashboard', stub: true }
  }
]

export default routes
