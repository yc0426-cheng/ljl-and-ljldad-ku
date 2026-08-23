import js from '@eslint/js' // ESLint 官方 JS 推荐规则
import pluginVue from 'eslint-plugin-vue' // Vue 官方 ESLint 插件（含 flat config）
import tseslint from 'typescript-eslint' // TS 官方 ESLint 配置（推荐规则 + tseslint.config 辅助函数）
import globals from 'globals' // 环境全局变量（browser / node）
import eslintConfigPrettier from 'eslint-config-prettier' // 关闭与 Prettier 冲突的格式类规则

/**
 * ESLint 扁平化配置（Flat Config，ESLint 9+）
 *
 * 约定：本项目不允许 .js / .html 文件，只允许 .ts / .vue 文件，
 * 因此 ESLint 配置文件也使用 eslint.config.ts 编写（ESLint 9.18+ 原生支持 TS 配置文件）。
 *
 * 规则组成（按顺序合并，后面的配置覆盖前面的冲突项）：
 *   1. JS 推荐规则（no-undef 等）
 *   2. TypeScript 推荐规则（自动关闭与 TS 冲突的 JS 规则）
 *   3. Vue3 推荐规则（仅作用于 .vue，内部指定 vue-eslint-parser）
 *   4. .vue 的 <script lang="ts"> 使用 TS 解析器
 *   5. 浏览器全局变量（localStorage / location 等）
 *   6. vite.config.ts 运行在 Node 环境
 *   7. 项目内规则调整
 *   8. 关闭与 Prettier 冲突的规则（必须放最后）
 */
export default tseslint.config(
  // 0. 全局忽略：构建产物、依赖、锁文件不参与检查
  {
    ignores: ['dist/**', 'node_modules/**', '.npm-cache/**', 'package-lock.json', 'pnpm-lock.yaml']
  },

  // 1. JS 基础推荐规则
  js.configs.recommended,

  // 2. TypeScript 推荐规则（eslint-recommended 部分会关掉 no-unused-vars / no-undef 等冲突规则）
  ...tseslint.configs.recommended,

  // 3. Vue3 推荐规则（flat/recommended 内部已把 .vue 的解析器设为 vue-eslint-parser）
  ...pluginVue.configs['flat/recommended'],

  // 4. 让 vue-eslint-parser 解析 <script> 块时使用 TypeScript 解析器，
  //    从而正确检查 <script setup lang="ts"> 的代码
  {
    files: ['**/*.vue'],
    languageOptions: {
      parserOptions: {
        parser: tseslint.parser,
        extraFileExtensions: ['.vue'],
        sourceType: 'module'
      }
    }
  },

  // 5. 浏览器环境全局变量（src 下的 .ts 与 .vue）
  {
    files: ['**/*.ts', '**/*.vue'],
    languageOptions: {
      globals: { ...globals.browser }
    }
  },

  // 6. vite.config.ts 运行在 Node 环境（覆盖上面的 browser，补充 node 全局）
  {
    files: ['vite.config.ts'],
    languageOptions: {
      globals: { ...globals.node }
    }
  },

  // 7. 项目内规则调整
  {
    rules: {
      // 路由页面组件（Login / Home / index）是单文件名，属于路由组件而非通用组件，不强制多单词命名
      'vue/multi-word-component-names': 'off'
    }
  },

  // 8. 关闭与 Prettier 冲突的格式类规则（必须放在最后，否则会被前面的配置覆盖）
  eslintConfigPrettier
)
