// Ambient 全局通配/三方库 shim 声明。
// 本文件不得包含顶层 import/export,否则下面的 `declare module 'X'`
// 会变成 module-scoped augmentation,导致 ts2307 "Cannot find module"。

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 没有官方 d.ts / d.ts 不全的三方库 — ambient shim
declare module 'element-plus'
declare module 'axios'
declare module '@vueup/vue-quill'

declare module 'nprogress' {
  export interface NProgressOptions {
    minimum?: number
    template?: string
    easing?: string
    speed?: number
    trickle?: boolean
    trickleSpeed?: number
    showSpinner?: boolean
    parent?: string
    barSelector?: string
  }
  export interface NProgress {
    start(): NProgress
    set(n: number): NProgress
    inc(amount?: number): NProgress
    done(force?: boolean): NProgress
    remove(): void
    configure(options: NProgressOptions): NProgress
    status: number | null
  }
  const nprogress: NProgress
  export default nprogress
}

declare module 'js-cookie' {
  const Cookies: {
    get(name: string): string | undefined
    get(): { [key: string]: string }
    set(name: string, value: string, options?: any): string | undefined
    remove(name: string, options?: any): void
  }
  export default Cookies
}

declare module 'file-saver' {
  export function saveAs(data: Blob | string, filename?: string, options?: any): void
  export function saveAs(data: Blob | string, filename?: string, disableAutoBOM?: boolean): void
  export default saveAs
}

declare module 'jsencrypt/bin/jsencrypt.min' {
  import JSEncrypt from 'jsencrypt'
  export default JSEncrypt
}

declare module 'sortablejs' {
  export interface SortableEvent {
    oldIndex: number
    newIndex: number
  }
  export interface SortableOptions {
    ghostClass?: string
    onEnd?: (evt: SortableEvent) => void
  }
  export default class Sortable {
    static create(el: HTMLElement, options: SortableOptions): Sortable
  }
}

declare module 'fuse.js' {
  export interface FuseOptions<T> {
    keys: string[]
    threshold?: number
    includeScore?: boolean
    includeMatches?: boolean
    minMatchCharLength?: number
    shouldSort?: boolean
  }
  export default class Fuse<T> {
    constructor(list: T[], options?: FuseOptions<T>)
    search(pattern: string): T[]
  }
}

declare module 'vuedraggable/dist/vuedraggable.common' {
  import { DefineComponent } from 'vue'
  const draggable: DefineComponent
  export default draggable
}

declare module 'vue-cropper' {
  import { DefineComponent } from 'vue'
  const VueCropper: DefineComponent
  export { VueCropper }
}

declare module 'splitpanes' {
  import { DefineComponent } from 'vue'
  export const Splitpanes: DefineComponent
  export const Pane: DefineComponent
}
