// 让本文件被 TS 解析为 module(而不是 ambient global script)。
// 否则下方 `declare module 'vue' { ... }` 会被当作"重新声明" vue 模块,
// 而不是 module augmentation,导致 vue 的 ref/reactive/createApp/App 等
// named exports 在 program 中全部 TS2305 "no exported member"。
// `declare module '*.vue'` 通配声明 + 三方库 shim 已移至 src/types/shims-vue.d.ts(ambient script)。
export {}

/** Vite 环境变量类型 */
interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_BASE_API: string
  readonly VITE_APP_ENV: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

// Vue 模块 augmentation — 给 ComponentInternalInstance 加 proxy 字段
declare module 'vue' {
  interface ComponentInternalInstance { proxy: any }
}
