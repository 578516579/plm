import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/testdata',
    name: 'Testdata',
    component: () => import('./views/index.vue'),
    meta: { title: '测试数据工厂', icon: 'database', stub: true }
  }
]

export default routes
