import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/manual-ops',
    name: 'ManualOps',
    component: () => import('./views/index.vue'),
    meta: { title: '运维手册', icon: 'monitor' }
  }
]

export default routes
