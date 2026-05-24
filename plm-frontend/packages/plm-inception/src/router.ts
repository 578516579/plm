import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/inception',
    name: 'Inception',
    component: () => import('./views/index.vue'),
    meta: { title: '项目立项', icon: 'document-add', permi: 'business:inception:list' }
  }
]

export default routes
