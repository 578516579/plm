import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/defect',
    name: 'Defect',
    component: () => import('./views/index.vue'),
    meta: { title: '缺陷管理', icon: 'warning', stub: true }
  }
]

export default routes
