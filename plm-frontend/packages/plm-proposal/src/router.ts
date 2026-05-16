import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/proposal',
    name: 'Proposal',
    component: () => import('./views/index.vue'),
    meta: { title: '立项建议书', icon: 'document', stub: true }
  }
]

export default routes
