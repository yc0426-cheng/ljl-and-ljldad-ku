# Remix 前端工程（Vue 3 + TypeScript + Vite）

Remix 前端基于后端五服务（gateway 11000 / auth 13000 / system 12000 / file 14000 / book 15000）构建，目前已包含：登录页、常驻布局（左侧菜单树 + 底部用户区）、学习主页（热力图/统计/柱图）、用户管理、菜单管理、个人设置（含头像上传）等完整页面，以及 axios 请求封装、动态路由与登录守卫。

**约定：项目内不允许出现 `.js` 和 `.html` 文件，只允许 `.ts` 与 `.vue` 文件。**

## 快速开始

```shell
cd front
pnpm install   # 安装依赖
pnpm dev       # 启动开发服务器（默认 http://localhost:10000）
```

## 现有页面

| 页面 | 路径 / 路由 | 文件 |
|---|---|---|
| 登录页 | `/login`（`meta.public`，唯一可匿名访问） | `views/login/index.vue` |
| 学习主页 | `/home/index`（`/` 重定向到此；支持 `?view=` 切换内置视图） | `views/home/index.vue` |
| 用户列表 | `/system/user`（由菜单数据动态注册） | `views/system/user/index.vue` |
| 菜单管理 | `/system/menu`（静态路由） | `views/system/menu/index.vue` |
| 个人设置 | `/user/setting`（布局底部用户区也可弹出） | `views/system/user/setting/UserSetting.vue` |
| 建设中占位 | `/system/coming-soon`（菜单组件未实现时跳转） | `views/system/coming-soon/index.vue` |

## 开发操作流程

按以下流程新增/修改功能，完成后必须执行[校验命令](#校验命令提交前必须全部通过)。

### 1. 新增页面

1. 在 `src/views/<模块>/` 下新建 `<页面>.vue`，用 `<script setup lang="ts">` 编写逻辑
2. 在 `src/router/index.ts` 的 `routes` 中注册**静态路由**，组件使用懒加载：
   `component: () => import('@/views/<模块>/index.vue')`
3. 若页面由 `sys_menu` 菜单数据驱动（动态路由），只需在后端菜单表配置 `routePath` + `component`（如 `system/user/index`），前端 `registerMenuRoutes` 会自动解析 `src/views/` 下组件并注册，无需手写路由
4. 页面间共享数据放入 Pinia store，不要跨级传递 prop

### 2. 新增后端接口

1. 在 `src/api/` 下新建对应模块 `.ts`，统一使用 `@/utils/request` 封装
2. 函数签名必须标注完整 TS 类型（入参与返回值），例如：
   `export function login(account: string, password: string): Promise<string>`
3. 请求格式以后端注解为准：后端方法标 `@RequestBody` 时传 JSON 对象；无 `@RequestBody` 的表单接口用 `URLSearchParams`（参照 `api/auth.ts` 注释）

### 3. 新增全局状态

1. 在 `src/store/` 下用 `defineStore` 定义仓库（参照 `store/user.ts`）
2. 需要持久化的数据（如 token）同步写入 localStorage，并注释说明
3. 组件内通过 `useXxxStore()` 获取仓库实例，禁止组件直接读写 localStorage

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
- 目录按职责划分：`views/`（页面）、`api/`（接口）、`store/`（全局状态）、`utils/`（工具）、`router/`（路由）、`types/`（类型）、`layout/`（布局）、`style/`（全局样式）
- 文件命名 kebab-case：如 `user-info.ts`；页面统一放 `src/views/<模块>/index.vue`

### TypeScript 类型规范

- `tsconfig.json` 已开启 `strict: true`，所有代码受 `vue-tsc` 严格校验
- 函数入参、返回值必须显式标注类型；禁止 `any` 与隐式 any
- 与后端交互的数据结构用 `interface` 定义并导出（参照 `types/system/menu.ts`、`api/auth.ts` 的 `AuthUserInfo`）
- 类型断言必须注释说明依据

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

## 登录页与路由位置

- 登录页位于 **`src/views/login/index.vue`**，由 `src/router/index.ts` 中 `/login` 路由懒加载；
- 静态路由（主页/菜单管理/个人设置等）直接写在 `src/router/index.ts` 的 `routes` 里；
- 动态路由不手写：布局 `onMounted` 时调菜单树接口，`registerMenuRoutes` 根据 `sys_menu` 的 `component` 字段从 `src/views/**/*.vue` 解析组件并注册到 Layout 下（幂等，静态路由同名路径不重复注册）；
- 全局守卫 `router.beforeEach`：无 token 一律跳 `/login` 并带 `redirect`；本地 token 首次进入时调 `/auth/check` 向后端验真，通过后回写用户信息，失败清登录态。

## 没有 index.html 怎么办？

Vite 默认需要 `index.html` 作为入口，但本项目约定不使用 `.html` 文件，
因此由 `vite.config.ts` 中的 **虚拟 HTML 插件** 在运行时动态生成：

- 开发模式：拦截根路径请求，返回生成的 HTML（入口指向 `src/main.ts`）
- 生产构建：把生成的 HTML 作为产物输出到 `dist/index.html`

## 目录结构与职责

```text
front/
├── vite.config.ts               # vite 配置：虚拟 HTML 插件 + @ 别名 + /api 代理到 localhost:11000
├── tsconfig.json                # TypeScript 编译配置（strict 模式、@ 路径映射）
├── package.json                 # 依赖与脚本（json 文件，非 js/html，允许保留）
└── src/
    ├── main.ts                  # 应用入口：注册 pinia、router，引入 element-plus 样式
    ├── App.vue                  # 根组件（仅 <router-view />）
    ├── layout/
    │   └── index.vue            # 常驻布局：左侧菜单树（sys_menu 驱动）+ 底部用户区 + 右侧 router-view
    ├── api/
    │   ├── auth.ts              # 登录/登出/token 校验接口（对应 LoginController）
    │   ├── home/index.ts        # 主页接口（热力图、每日记录、答题统计）
    │   └── system/
    │       ├── menu.ts          # 菜单树查询与增删改（SysMenuController）
    │       ├── user.ts          # 用户管理接口
    │       └── user/user.ts     # 用户相关细分接口
    ├── types/
    │   ├── axios.d.ts           # 扩展 axios 配置（skipGlobalError 跳过全局提示）
    │   ├── common/
    │   │   ├── api.d.ts         # 统一响应格式 ApiResponse（后端补全包装后使用）
    │   │   ├── base.d.ts        # 基础类型
    │   │   └── page.d.ts        # 统一分页结构 PageResult（对应 MyBatis-Plus IPage）
    │   ├── home/index.d.ts      # 主页数据类型
    │   └── system/
    │       ├── menu.ts          # SysMenu 菜单类型
    │       └── user/            # 用户类型与常量
    ├── utils/
    │   ├── request.ts           # axios 封装：拦截器 + 类型化 request 门面（get/post/download/preview）
    │   ├── crypto.ts            # 国密加密（占位透传，等后端实现后填真实逻辑）
    │   └── auth.ts              # 被动登出工具（会话失效/越权时清除登录态并跳转）
    ├── store/
    │   └── user.ts              # Pinia 用户仓库：token 持久化、login/logout、用户信息
    ├── style/
    │   ├── common.css           # 布局/菜单/用户区公共样式
    │   └── global.css           # 全局样式
    ├── router/
    │   └── index.ts             # 静态路由 + 动态路由注册 + 登录守卫
    └── views/
        ├── login/index.vue      # 登录页
        ├── home/index.vue       # 学习主页：热力图、统计横条、答题柱图（ECharts）
        └── system/
            ├── user/index.vue           # 用户列表/用户管理
            ├── user/setting/            # 个人设置：UserSetting + 资料表单 + 历史头像
            ├── menu/index.vue           # 菜单管理
            └── coming-soon/index.vue    # 建设中占位页
```

## 请求封装说明（utils/request.ts）

结构参照成熟后台模板（类型化 request 门面 + 请求/响应拦截器），已针对当前后端适配：

- **不做 code 强校验**：模板要求响应 `{code:'200', data, message}`，但当前后端无统一包装（登录直接返回纯字符串 token），拦截器直接透传 response、门面取 `.data`；等后端补全统一响应格式后可恢复 `code === '200'` 校验（`types/common/api.d.ts` 已备好类型）
- **国密加密占位**：POST 请求体会经过 `utils/crypto.ts` 的加密函数，当前为原样透传（后端未实现国密解密），等后端支持后填入真实 SM2/SM3/SM4 逻辑即可，签名不变
- **状态码统一处理**：400 提示、401/403 弹框踢出登录、500（`code` 以 `D` 开头视为危险级）踢出登录、其余提示；调用方传 `skipGlobalError: true` 可跳过全局提示（如登录页/守卫自行处理错误）
- **提示组件**：使用 element-plus 的 `ElMessage` / `ElMessageBox`，样式在 `main.ts` 全局引入

## 与后端的接口契约（重要）

| 项         | 说明                                                                                                                                     |
| ---------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| 登录接口   | `POST /auth/login`（开发环境经 vite 代理 `/api` → 网关 11000 → auth 13000）                                                              |
| 请求格式   | **JSON 格式**（`application/json`），字段 `account`、`password`。后端 `LoginController.login` 参数为 `@RequestBody LoginDTO`，传表单反而收不到 |
| 成功返回   | **纯字符串 token**（无 `{code,data}` 包装），前端直接保存                                                                                |
| token 校验 | `POST /auth/check`，请求头自动带 `Authorization: Bearer <token>`，返回 `LoginUserInfo`（userId/account/name/avatar 等）；无效时后端抛业务异常，守卫清登录态 |
| 登出接口   | `POST /auth/logout`，后端删 Redis 会话并加入黑名单；接口幂等，失败不影响前端本地清理                                                      |
| Token 传递 | 登录后写入 `localStorage`，后续请求由 `request.ts` 拦截器自动加 `Authorization: Bearer <token>` 头                                       |
| 菜单接口   | `GET /system/menu/list/tree` 返回菜单树，用于渲染左侧菜单 + 动态注册路由；当前为全量返回，尚未按角色过滤                                 |
| 统一包装   | 后端暂无 `{code,data,message}` 包装，request 已适配为直接返回数据；后端补全后可恢复 code 校验                                            |
| 国密加密   | 后端暂无国密解密，`utils/crypto.ts` 为透传占位，待后端支持后启用                                                                         |
