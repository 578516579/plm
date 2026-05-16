import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [{
  path: '/business/document',
  name: 'Document',
  component: () => import('./views/index.vue'),
  meta: { title: '文档管理', icon: 'documentation', permi: 'business:document:list' }
}]

export default routes
