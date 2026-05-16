import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/apidoc',
    name: 'Apidoc',
    component: () => import('./views/index.vue'),
    meta: { title: 'API 文档', icon: 'document', stub: true }
  }
]

export default routes
