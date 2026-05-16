import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/testplan',
    name: 'Testplan',
    component: () => import('./views/index.vue'),
    meta: { title: '测试方案', icon: 'notebook', stub: true }
  }
]

export default routes
