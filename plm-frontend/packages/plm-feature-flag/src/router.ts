import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/feature-flag',
    name: 'FeatureFlag',
    component: () => import('./views/index.vue'),
    meta: { title: 'Feature Flag', icon: 'switch', stub: true }
  }
]

export default routes
