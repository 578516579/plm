import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/testcase',
    name: 'Testcase',
    component: () => import('./views/index.vue'),
    meta: { title: '测试用例', icon: 'check', stub: true }
  }
]

export default routes
