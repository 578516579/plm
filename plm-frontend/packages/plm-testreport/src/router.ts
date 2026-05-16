import type { RouteRecordRaw } from 'vue-router'
const routes: RouteRecordRaw[] = [{
  path: '/business/testreport',
  name: 'TestReport',
  component: () => import('./views/index.vue'),
  meta: { title: '测试报告', permi: 'business:testreport:list' }
}]
export default routes
