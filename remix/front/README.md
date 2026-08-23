# Remix 前端工程（Vue 3 + TypeScript + Vite）

基于现有后端（`auth` 模块，端口 11000）构建的基础前端，目前只包含**登录逻辑**，未设计页面样式。

**约定：项目内不允许出现 `.js` 和 `.html` 文件，只允许 `.ts` 与 `.vue` 文件。**

## 启动方式

```shell
cd front
pnpm install      # 安装依赖
pnpm dev          # 启动开发服务器（默认 http://localhost:5173）
pnpm build        # 类型检查(vue-tsc) + 生产构建
```

## 常用脚本

| 命令                | 说明                                             |
| ------------------- | ------------------------------------------------ |
| `pnpm dev`          | 启动开发服务器                                   |
| `pnpm type-check`   | TypeScript 类型检查（vue-tsc --noEmit）          |
| `pnpm lint`         | 代码检查（oxlint + eslint，均带 --fix 自动修复） |
| `pnpm format`       | 格式化全部文件（prettier --write）               |
| `pnpm format:check` | 仅检查格式是否合规（CI 可用）                    |
| `pnpm build`        | 类型检查 + 生产构建                              |

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
    ├── main.ts                  # 应用入口：注册 pinia、router
    ├── App.vue                  # 根组件（仅 <router-view />）
    ├── api/
    │   └── auth.ts              # 登录/登出接口定义（对应后端 LoginController）
    ├── utils/
    │   └── request.ts           # axios 封装：请求/响应拦截器、错误统一处理
    ├── store/
    │   └── user.ts              # Pinia 用户仓库：token 持久化、login/logout 动作
    ├── router/
    │   └── index.ts             # 路由表 + 登录守卫（未登录强制跳 /login）
    └── views/
        ├── login/index.vue      # 登录页（仅逻辑，无样式）
        └── home/index.vue       # 首页占位（验证登录链路 + 登出演示）
```

## 与后端的接口契约（重要）

| 项         | 说明                                                                                                                                     |
| ---------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| 登录接口   | `POST /auth/login`（开发环境经 vite 代理转发到 auth 服务 11000）                                                                         |
| 请求格式   | **表单格式**（`application/x-www-form-urlencoded`），字段 `account`、`password`。后端 `LoginDTO` 没有 `@RequestBody`，传 JSON 收不到参数 |
| 成功返回   | **纯字符串 token**（无 `{code,data}` 包装），前端直接保存                                                                                |
| 失败返回   | 后端目前无全局异常处理器，业务异常会返回 Spring 默认 5xx 错误页；前端按非 2xx 处理，等后端补全异常处理后错误信息会更好看                 |
| 登出接口   | 后端 `logout()` 暂无注解未暴露，前端已留好调用位置（`api/auth.ts`）                                                                      |
| Token 传递 | 登录后写入 `localStorage`，后续请求由 `request.ts` 拦截器自动加 `Authorization: Bearer <token>` 头                                       |
