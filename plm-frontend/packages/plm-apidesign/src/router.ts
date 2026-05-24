import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/apidesign',
    name: 'Apidesign',
    component: () => import('./views/index.vue'),
    meta: { title: '接口详细设计', icon: 'link', permi: 'business:apidesign:list' }
  }
]

export default routes
