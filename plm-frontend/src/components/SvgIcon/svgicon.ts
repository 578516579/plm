import * as components from '@element-plus/icons-vue'

export default {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  install: (app: any) => {
    for (const key in components) {
      const componentConfig = (components as any)[key]
      app.component(componentConfig.name, componentConfig)
    }
  }
}
