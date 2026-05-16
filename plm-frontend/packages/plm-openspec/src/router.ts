import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/openspec',
    name: 'Openspec',
    component: () => import('./views/index.vue'),
    meta: { title: 'AI OpenSpec', icon: 'magic-stick', stub: true }
  }
]

export default routes
