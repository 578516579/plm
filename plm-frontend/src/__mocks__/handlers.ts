import { http, HttpResponse } from 'msw'

export const handlers = [
  // 登录 — 返回固定 JWT
  http.post('*/login', () => HttpResponse.json({
    code: 200, msg: '操作成功', token: 'test-jwt-token'
  })),

  // 用户信息
  http.get('*/getInfo', () => HttpResponse.json({
    code: 200, msg: '操作成功',
    user: { userId: 1, userName: 'admin', nickName: '管理员' },
    roles: ['admin'], permissions: ['*:*:*']
  })),

  // 动态路由 — 返回 PRD-aligned 13 模块的简化菜单
  http.get('*/getRouters', () => HttpResponse.json({
    code: 200, msg: '操作成功',
    data: [
      { name: 'project', path: '/business/project', component: 'business/project/index',
        meta: { title: '项目管理', icon: 'tree-table' } }
    ]
  })),

  // 登出
  http.post('*/logout', () => HttpResponse.json({
    code: 200, msg: '退出成功'
  })),

  // 字典(给 selectDictLabel 测试用)
  http.get('*/system/dict/data/type/:type', () => HttpResponse.json({
    code: 200, msg: '操作成功',
    data: [
      { dictLabel: '启用', dictValue: '0' },
      { dictLabel: '停用', dictValue: '1' }
    ]
  }))
]
