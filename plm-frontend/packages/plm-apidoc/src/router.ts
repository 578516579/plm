import type { RouteRecordRaw } from 'vue-router'
const routes: RouteRecordRaw[] = [{
  path: '/business/apidoc',
  name: 'ApiDoc',
  component: () => import('./views/index.vue'),
  meta: { title: 'API 文档', permi: 'business:apidoc:list' }
}]
export default routes
