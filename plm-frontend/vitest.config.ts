import { defineConfig, mergeConfig } from 'vitest/config'
import viteConfig from './vite.config'

// vite.config.ts 是函数形态 ({ mode, command }) => {...}
// 传入 ConfigEnv 后得到完整的 vite ResolvedConfig，再与 vitest 专属配置合并，
// 这样 alias（@、@plm）、auto-import 等 plugin 都会在测试环境中生效。
export default defineConfig(env =>
  mergeConfig(
    viteConfig({ mode: env.mode ?? 'test', command: 'serve' } as any),
    defineConfig({
      test: {
        environment: 'happy-dom',
        globals: true,
        setupFiles: ['src/__mocks__/setup.ts'],
        include: [
          'src/**/*.spec.ts',
          'src/**/*.test.ts',
          'packages/plm-*/src/**/*.spec.ts',
          'packages/plm-*/src/**/*.test.ts'
        ],
        exclude: ['e2e/**', 'node_modules/**', 'dist/**'],
        coverage: {
          provider: 'v8',
          reporter: ['text', 'html'],
          reportsDirectory: 'coverage',
          exclude: ['e2e/**', '**/*.spec.ts', '**/*.test.ts', 'vite/**', '*.config.*']
        }
      }
    })
  )
)
