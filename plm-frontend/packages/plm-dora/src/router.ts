import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/dora',
    name: 'Dora',
    component: () => import('./views/index.vue'),
    meta: { title: 'DORA 效能', icon: 'trend-charts' }
  }
]

export default routes
