import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/project',
    name: 'Project',
    component: () => import('./views/index.vue'),
    meta: { title: '项目管理', icon: 'tree-table', permi: 'business:project:list' }
  }
]

export default routes
