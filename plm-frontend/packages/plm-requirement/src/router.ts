import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/requirement',
    name: 'Requirement',
    component: () => import('./views/index.vue'),
    meta: { title: '需求管理', icon: 'edit', permi: 'business:requirement:list' }
  }
]

export default routes
