import type { RouteRecordRaw } from 'vue-router'
const routes: RouteRecordRaw[] = [{
  path: '/business/submission',
  name: 'Submission',
  component: () => import('./views/index.vue'),
  meta: { title: '提测管理', permi: 'business:submission:list' }
}]
export default routes
