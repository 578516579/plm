import vue from '@vitejs/plugin-vue'

import createAutoImport from './auto-import'
import createSvgIcon from './svg-icon'
import createCompression from './compression'
import createSetupExtend from './setup-extend'
import autoBusinessModules from './auto-business-modules'
import { PluginOption } from 'vite'

export default function createVitePlugins(viteEnv: Record<string, string>, isBuild = false) {
  const vitePlugins: PluginOption[] = [vue()]
  vitePlugins.push(createAutoImport())
  vitePlugins.push(createSetupExtend())
  vitePlugins.push(createSvgIcon(isBuild))
  // Legacy 镜像迁移 Stage 2: 自动把 src/{api,types/api,views}/business/<m>/ 重定向到 packages/plm-<m>/src/
  vitePlugins.push(autoBusinessModules())
  isBuild && vitePlugins.push(...createCompression(viteEnv))
  return vitePlugins
}
