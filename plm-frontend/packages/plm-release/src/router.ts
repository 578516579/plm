import type { RouteRecordRaw } from 'vue-router'
const routes: RouteRecordRaw[] = [{
  path: '/business/release',
  name: 'Release',
  component: () => import('./views/index.vue'),
  meta: { title: '发布管理', permi: 'business:release:list' }
}]
export default routes
