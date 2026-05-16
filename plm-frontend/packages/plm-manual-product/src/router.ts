import type { RouteRecordRaw } from 'vue-router'
const routes: RouteRecordRaw[] = [{
  path: '/business/manual-product',
  name: 'ManualProduct',
  component: () => import('./views/index.vue'),
  meta: { title: '产品手册', permi: 'business:manual-product:list' }
}]
export default routes
