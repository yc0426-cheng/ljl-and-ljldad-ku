# Remix 前端工程（Vue 3 + TypeScript + Vite）

基于现有后端（`auth` 模块，端口 11000）构建的基础前端，目前包含**登录逻辑**与 **axios 请求封装**（Element Plus 仅用于消息提示），未设计页面样式。

**约定：项目内不允许出现 `.js` 和 `.html` 文件，只允许 `.ts` 与 `.vue` 文件。**

## 快速开始

```shell
cd front
pnpm install   # 安装依赖
pnpm dev       # 启动开发服务器（默认 http://localhost:10000）
```

## 开发操作流程

按以下流程新增/修改功能，完成后必须执行[校验命令](#校验命令提交前必须全部通过)。

### 1. 新增页面

1. 在 `src/views/<模块>/` 下新建 `<页面>.vue`，用 `<script setup lang="ts">` 编写逻辑（样式后补）
2. 在 `src/router/index.ts` 的 `routes` 中注册路由，组件使用懒加载：
   `component: () => import('@/views/<模块>/index.vue')`
3. 页面间共享数据放入 Pinia store（见第 3 步），不要跨级传递 prop

### 2. 新增后端接口

1. 在 `src/api/` 下新建对应模块 `.ts`，统一使用 `@/utils/request` 封装
2. 函数签名必须标注完整 TS 类型（入参与返回值），例如：
   `export function login(account: string, password: string): Promise<string>`
3. 请求格式以后端为准：后端没有 `@RequestBody` 时用 `URLSearchParams` 提交表单（参照 `api/auth.ts` 注释）

### 3. 新增全局状态

1. 在 `src/store/` 下用 `defineStore` 定义仓库（参照 `store/user.ts`）
2. 需要持久化的数据（如 token）同步写入 localStorage，并注释说明
3. 组件内通过 `useXxxStore()` 获取仓库实例，禁止直接读写 localStorage

### 4. 完成开发后执行校验

按顺序执行，全部通过才算完成：

```shell
pnpm type-check   # 1. 严格 TS 类型检查（vue-tsc，strict 模式）
pnpm lint         # 2. 代码规范检查（oxlint + eslint，自动修复）
pnpm format       # 3. 格式化全部文件（prettier）
pnpm build        # 4. 最终构建（再次类型检查 + 产物构建）
```

### 5. 确认文件类型合规

仓库中不得出现 `.js` / `.html` 文件，提交前检查：

```powershell
Get-ChildItem -Recurse -File | Where-Object { $_.Extension -in '.js', '.html' }
```

## 校验命令（提交前必须全部通过）

| 命令                | 作用                                         | 不过怎么办                             |
| ------------------- | -------------------------------------------- | -------------------------------------- |
| `pnpm type-check`   | vue-tsc 严格类型检查（tsconfig 已开 strict） | 按报错补全/修正类型，禁止用 `any` 绕过 |
| `pnpm lint`         | oxlint + eslint（自带 --fix 自动修复）       | 自动修复后仍有报错，按提示手工修改     |
| `pnpm format`       | prettier 格式化全部文件                      | 格式化后执行 `pnpm format:check` 确认  |
| `pnpm format:check` | 检查格式是否合规（CI 可用）                  | 不通过则执行 `pnpm format` 重新格式化  |
| `pnpm build`        | 类型检查 + 生产构建                          | 以 build 通过为最终验收标准            |

## 写作规范（严格 TS + Vue3）

### 文件与目录

- 只允许 `.ts` / `.vue`，禁止 `.js` / `.html`（`index.html` 由 `vite.config.ts` 虚拟插件动态生成）
- 目录按职责划分：`views/`（页面）、`api/`（接口）、`store/`（全局状态）、`utils/`（工具）、`router/`（路由）
- 文件命名 kebab-case：如 `user-info.ts`；页面统一放 `src/views/<模块>/index.vue`

### TypeScript 类型规范

- `tsconfig.json` 已开启 `strict: true`，所有代码受 `vue-tsc` 严格校验
- 函数入参、返回值必须显式标注类型；禁止 `any` 与隐式 any
- 与后端交互的数据结构用 `interface` 定义并导出（参照 `store/user.ts` 的 `UserInfo`）
- 类型断言必须注释说明依据（如登录接口返回纯字符串 token 的断言）

### Vue3 规范

- 一律使用 `<script setup lang="ts">` + 组合式 API
- 组件内代码按区块组织并加注释：import 区 → 实例化 → 响应式状态 → 逻辑函数
- 模板只做展示与事件绑定，复杂逻辑一律写在 `<script>` 里
- 全局状态走 Pinia；请求一律走 `@/utils/request`（拦截器统一加 token、统一处理错误）
- 组件 props 用 `defineProps<类型>()` 声明

### 注释规范

- 使用中文注释
- 公共函数与接口定义使用 JSDoc 注释（`/** ... */`）
- 注释说明"为什么"（后端契约、格式要求等），不重复描述代码本身

## 登录页应该写的位置

登录页位于 **`src/views/login/index.vue`**，由 `src/router/index.ts` 中 `/login` 路由懒加载。
开发时直接修改该文件即可；新增页面在 `src/router/index.ts` 的 `routes` 里追加路由。

## 没有 index.html 怎么办？

Vite 默认需要 `index.html` 作为入口，但本项目约定不使用 `.html` 文件，
因此由 `vite.config.ts` 中的 **虚拟 HTML 插件** 在运行时动态生成：

- 开发模式：拦截根路径请求，返回生成的 HTML（入口指向 `src/main.ts`）
- 生产构建：把生成的 HTML 作为产物输出到 `dist/index.html`

## 目录结构与职责

```text
front/
├── vite.config.ts               # vite 配置：虚拟 HTML 插件 + @ 别名 + 开发代理（/auth → localhost:11000）
├── tsconfig.json                # TypeScript 编译配置（strict 模式、@ 路径映射）
├── package.json                 # 依赖与脚本（json 文件，非 js/html，允许保留）
└── src/
    ├── main.ts                  # 应用入口：注册 pinia、router，引入 element-plus 样式
    ├── App.vue                  # 根组件（仅 <router-view />）
    ├── api/
    │   └── auth.ts              # 登录/登出接口定义（对应后端 LoginController）
    ├── types/
    │   ├── axios.d.ts           # 扩展 axios 配置（skipGlobalError 跳过全局提示）
    │   └── common/
    │       ├── api.ts           # 统一响应格式 ApiResponse（后端补全包装后使用）
    │       └── page.ts          # 统一分页结构 PageResult（对应 MyBatis-Plus IPage）
    ├── utils/
    │   ├── request.ts           # axios 封装：拦截器 + 类型化 request 门面（get/post/download/preview）
    │   ├── crypto.ts            # 国密加密（占位透传，等后端实现后填真实逻辑）
    │   └── auth.ts              # 被动登出工具（会话失效/越权时清除登录态并跳转）
    ├── store/
    │   └── user.ts              # Pinia 用户仓库：token 持久化、login/logout 动作
    ├── router/
    │   └── index.ts             # 路由表 + 登录守卫（未登录强制跳 /login）
    └── views/
        ├── login/index.vue      # 登录页（仅逻辑，无样式）
        └── home/index.vue       # 首页占位（验证登录链路 + 登出演示）
```

## 请求封装说明（utils/request.ts）

结构参照成熟后台模板（类型化 request 门面 + 请求/响应拦截器），已针对当前后端适配：

- **不做 code 强校验**：模板要求响应 `{code:'200', data, message}`，但当前后端无统一包装（登录直接返回纯字符串 token），拦截器直接透传 response、门面取 `.data`；等后端补全统一响应格式后可恢复 `code === '200'` 校验（`types/common/api.ts` 已备好类型）
- **国密加密占位**：POST 请求体会经过 `utils/crypto.ts` 的加密函数，当前为原样透传（后端未实现国密解密），等后端支持后填入真实 SM2/SM3/SM4 逻辑即可，签名不变
- **状态码统一处理**：400 提示、401/403 弹框踢出登录、500（`code` 以 `D` 开头视为危险级）踢出登录、其余提示；调用方传 `skipGlobalError: true` 可跳过全局提示（如登录页自行展示错误）
- **提示组件**：使用 element-plus 的 `ElMessage` / `ElMessageBox`，样式在 `main.ts` 全局引入

## 与后端的接口契约（重要）

| 项         | 说明                                                                                                                                     |
| ---------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| 登录接口   | `POST /auth/login`（开发环境经 vite 代理转发到 auth 服务 11000）                                                                         |
| 请求格式   | **表单格式**（`application/x-www-form-urlencoded`），字段 `account`、`password`。后端 `LoginDTO` 没有 `@RequestBody`，传 JSON 收不到参数 |
| 成功返回   | **纯字符串 token**（无 `{code,data}` 包装），前端直接保存                                                                                |
| 失败返回   | 后端目前无全局异常处理器，业务异常会返回 Spring 默认 5xx 错误页；前端按非 2xx 处理，等后端补全异常处理后错误信息会更好看                 |
| 登出接口   | 后端 `logout()` 暂无注解未暴露，前端已留好调用位置（`api/auth.ts`）                                                                      |
| Token 传递 | 登录后写入 `localStorage`，后续请求由 `request.ts` 拦截器自动加 `Authorization: Bearer <token>` 头                                       |
| 统一包装   | 后端暂无 `{code,data,message}` 包装，request 已适配为直接返回数据；后端补全后可恢复 code 校验（见"请求封装说明"）                        |
| 国密加密   | 后端暂无国密解密，`utils/crypto.ts` 为透传占位，待后端支持后启用                                                                         |
