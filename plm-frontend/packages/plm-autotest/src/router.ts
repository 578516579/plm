import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/autotest',
    name: 'Autotest',
    component: () => import('./views/index.vue'),
    meta: { title: '自动化测试', icon: 'cpu' }
  }
]

export default routes
