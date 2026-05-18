import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/ued',
    name: 'Ued',
    component: () => import('./views/index.vue'),
    meta: { title: 'UED 设计', icon: 'picture' }
  }
]

export default routes
