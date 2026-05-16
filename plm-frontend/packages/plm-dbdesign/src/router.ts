import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/dbdesign',
    name: 'Dbdesign',
    component: () => import('./views/index.vue'),
    meta: { title: '数据库设计', icon: 'coin', stub: true }
  }
]

export default routes
