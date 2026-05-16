import type { RouteRecordRaw } from 'vue-router'
const routes: RouteRecordRaw[] = [{
  path: '/business/testcase',
  name: 'TestCase',
  component: () => import('./views/index.vue'),
  meta: { title: '测试用例', icon: 'star', permi: 'business:testcase:list' }
}]
export default routes
