import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/manual-impl',
    name: 'ManualImpl',
    component: () => import('./views/index.vue'),
    meta: { title: '实施手册', icon: 'guide', stub: true }
  }
]

export default routes
