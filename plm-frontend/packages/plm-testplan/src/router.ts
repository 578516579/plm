import type { RouteRecordRaw } from 'vue-router'
const routes: RouteRecordRaw[] = [{
  path: '/business/testplan',
  name: 'TestPlan',
  component: () => import('./views/index.vue'),
  meta: { title: '测试方案', permi: 'business:testplan:list' }
}]
export default routes
