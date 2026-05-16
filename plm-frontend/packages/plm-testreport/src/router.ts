import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/testreport',
    name: 'Testreport',
    component: () => import('./views/index.vue'),
    meta: { title: '测试报告', icon: 'tickets', stub: true }
  }
]

export default routes
