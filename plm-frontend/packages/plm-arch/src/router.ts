import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/arch',
    name: 'Arch',
    component: () => import('./views/index.vue'),
    meta: { title: '系统架构', icon: 'setting', permi: 'business:arch:list' }
  }
]

export default routes
