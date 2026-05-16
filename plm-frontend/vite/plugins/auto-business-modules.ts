import type { Plugin } from 'vite'

// =============================================================================
// auto-business-modules - Legacy 镜像迁移 Stage 2 vite 插件 (占位 / WIP)
//
// 当前状态: DISABLED (仅占位, Stage 1 极薄壳是当前生产模式)
//
// Stage 2 目标: 让 src views business 模块的 import 自动指向 packages 目录,
//              从而彻底删除 legacy 镜像。
//
// 当前阻塞: 若依动态路由用 import.meta.glob 在构建期扫描文件系统,
//          resolveId hook 无法影响 glob 结果。需要更深的改造。
//
// 候选实现方案:
//   A. 改写 glob 调用, 加入 packages 模块的 views 文件
//   B. 在 vite generateBundle hook 把 packages 路径软链到 src views business
//   C. 改成静态聚合路由 (src router index.ts 显式 import @plm 路由) = Stage 3
//
// 选定实现路径前, 坚持用 Legacy 镜像迁移 v1.0 Stage 1 极薄壳模式 (1-3 行 re-export)。
//
// 关联文档: 03-开发/Legacy-镜像迁移-playbook.md
// =============================================================================
export default function autoBusinessModules(): Plugin {
  return {
    name: 'plm:auto-business-modules',
    enforce: 'pre'
    // 当前禁用,等 Stage 2 实现方案选定再启用
  }
}
