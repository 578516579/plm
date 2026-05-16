import { RUN_ID } from './fixtures'

export function makeDocumentData(projectId: number, docType: string = 'prd', suffix?: string) {
  const tag = suffix || RUN_ID
  return {
    projectId,
    docType,
    title: `E2E ${docType} 文档-${tag}`,
    content: `# ${docType.toUpperCase()} 测试文档\n\n## 章节 1\n\n内容 αβγ ${tag}`,
    version: 'v1.0',
    authorUserId: 1,
    tags: 'e2e,test'
  }
}

export const DOC_STATUS_TRANSITIONS = {
  legal: [
    { from: '00', to: '01', name: '草稿→待评审' },
    { from: '01', to: '00', name: '待评审→草稿 (反向打回)' },
    { from: '01', to: '02', name: '待评审→已发布 (需要 reviewer)' },
    { from: '02', to: '01', name: '已发布→待评审 (反向重审)' },
    { from: '02', to: '03', name: '已发布→已归档' }
  ],
  illegal: [
    { from: '00', to: '02', name: '草稿→已发布 (跨级)' },
    { from: '00', to: '03', name: '草稿→已归档 (跨级)' },
    { from: '03', to: '00', name: '已归档→草稿 (终态)' }
  ]
}
