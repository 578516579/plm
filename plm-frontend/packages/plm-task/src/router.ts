import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/business/task',
    name: 'Task',
    component: () => import('./views/index.vue'),
    meta: { title: '任务管理', icon: 'list', permi: 'business:task:list' }
  },
  {
    path: '/business/taskkanban',
    name: 'TaskKanban',
    component: () => import('./views/kanban.vue'),
    meta: { title: '任务看板', icon: 'tree', permi: 'business:task:kanban' }
  },
  {
    path: '/business/mytask',
    name: 'MyTasks',
    component: () => import('./views/my.vue'),
    meta: { title: '我的任务', icon: 'people', permi: 'business:task:list' }
  }
]

export default routes
