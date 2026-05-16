import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/defect',
    name: 'Defect',
    component: () => import('./views/index.vue'),
    meta: { title: '缺陷管理', icon: 'bug', permi: 'business:defect:list' }
  }
]

export default routes
