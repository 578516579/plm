import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/prd',
    name: 'Prd',
    component: () => import('./views/index.vue'),
    meta: { title: 'PRD 文档', icon: 'edit-pen', permi: 'business:prd:list' }
  }
]

export default routes
