# Remix 学习管理系统

Remix 是一个基于 Spring Boot 3.2 的学习管理系统，后端采用 Maven 多模块结构，前端使用 Vue 3 + TypeScript + Vite。父 POM 统一聚合后端各模块并管理公共依赖版本。五个可独立启动的服务均注册到 Nacos（命名空间 `learn`）并从中拉取配置：`gateway`(11000) 为统一网关入口，`auth`(13000) 为认证服务，`system`(12000) 为系统服务（用户/角色/菜单），`file`(14000) 为文件服务（OSS 上传下载），`book`(15000) 为书籍服务（书籍管理与在线阅读）；前端经 `gateway` 访问后端。

## 技术基线

### 后端

- Java 17
- Spring Boot 3.2.12
- Spring Cloud 2023.0.4 + Spring Cloud Alibaba 2023.0.3.2（Nacos 注册/配置中心）
- Spring Cloud OpenFeign 4.1.5（服务间远程调用）
- Spring Security 6.1.9（BCrypt 密码加密）
- MyBatis-Plus 3.5.16（Spring Boot 3 兼容 starter）
- MapStruct 1.6.3（对象转换）
- Spring Data Redis（Lettuce 客户端）
- 阿里云 OSS（alibabacloud-oss-v2 0.5.1，头像/书籍文件存储）
- Fesod 2.0.2（Excel 导入导出）
- Hutool 5.8.44（含 JWT）
- Lombok 1.18.36
- Maven 3.9+

### 前端

- Vue 3 + TypeScript
- Vite 5+
- Element Plus（按需自动导入）+ @element-plus/icons-vue
- ECharts 6（热力图、柱图等统计图表）
- Pinia（状态管理）
- Vue Router（路由，支持菜单数据驱动的动态路由）
- Axios（HTTP 客户端）
- pnpm（包管理器）

前端工程独立的开发流程、校验命令与写作规范详见 [`front/README.md`](front/README.md)。

## 项目结构

```text
remix/
├── pom.xml              # Maven 聚合父 POM，统一管理版本和构建配置
├── README.md            # 本文件
├── api/                 # Feign 客户端定义（POM 聚合模块）
│   ├── api-system/      # system 服务客户端：SysUserFeignClient、SysUserMiscClient、OperationLogFeignClient
│   ├── api-file/        # file 服务客户端：FileFeignClient、AvatarFeignClient
│   └── api-book/        # book 服务客户端：BookFeignClient
├── auth/                # 认证服务（登录、登出、JWT 签发与黑名单；登录操作日志埋点）
├── system/              # 系统服务（用户、角色、菜单、用户杂项、操作日志落库）
├── file/                # 文件服务（头像/书籍上传 OSS、历史头像、OSS 文件下载）
├── book/                # 书籍服务（书籍 CRUD、分页阅读、书签、导入导出）
├── common/              # 后端公共模块（POM 聚合 common-core + common-redis）
│   ├── common-core/     # 通用核心：异常体系、常量、上下文、POJO、配置属性、请求日志过滤器、操作追踪注解/TraceContext
│   ├── common-redis/    # Redis 工具：RedisService、RedisTemplate 配置
│   └── common-log/      # 操作日志：@OperationLog 注解 + LogAspect（AOP 打印，独立被父 POM 聚合）
├── gateway/             # 网关（路由转发 + JWT 鉴权过滤器；路由、CORS 与白名单配置在 Nacos）
├── data/                # 项目部署/初始化数据
│   ├── nacos/           # Nacos 配置导出包（dataId: gateway-server / auth-server / system-server / common-datasource）
│   └── sql/             # 建表 SQL（system/ 7 张 + book/ 3 张，共 10 张表，见 data/README.md）
└── front/               # 前端工程（Vue 3 + TS + Vite），不属于 Maven 后端模块
```

## Maven 父子关系

```text
spring-boot-starter-parent
          ↓
        remix (pom)
    ↙    ↓    ↓    ↘        ↘        ↘            ↘
 auth  gateway system  common     api        file  book  common/common-log
                          ↙ ↘       ↙  ↓  ↘
              common-core  common- api- api- api-
                   redis   system file book
```

`remix` 继承 `spring-boot-starter-parent`，并通过 `<modules>` 聚合 `auth`、`gateway`、`system`、`common`、`api`、`file`、`book` 七个一级模块，以及路径位于 `common/` 下、但单独聚合的 `common-log`。`common` 自身是 POM 模块，聚合 `common-core` 和 `common-redis`；`api` 自身也是 POM 模块，聚合 `api-system`、`api-file`、`api-book`。子模块只继承 `remix`，从而间接获得 Spring Boot 的依赖版本与构建配置。

## 模块说明

| 模块 | 职责 | 启动类 / 端口 |
|---|---|---|
| `gateway` | 统一网关入口：按 Nacos 路由转发，JWT 鉴权过滤 + 白名单，CORS 跨域配置，并把用户信息透传到下游请求头 | `GatewayApplication` / 11000 |
| `auth` | 登录、登出、JWT 签发与黑名单管理（校验用户时经 Feign 调 system）；登录调用链埋点（`@TraceRequest`/`@TraceStep` + AOP，经 Feign 通知 system 落库） | `AuthApplication` / 13000 |
| `system` | 用户管理、角色管理、菜单管理、用户杂项（头像/昵称/签名）、用户角色分配；操作日志统一落库（`sys_user_operation_log` / `sys_user_operation_step_log`） | `SystemApplication` / 12000 |
| `file` | 头像/书籍文件上传至 OSS、头像与历史头像查询、OSS 文件流下载 | `FileApplication` / 14000 |
| `book` | 书籍 CRUD、上传与导出、翻页/定位阅读、书签管理 | `BookApplication` / 15000 |
| `common-core` | `BizException` / `BaseException` 异常体系、`RedisKeyConstant`、`LoginUserHolder`、`LoginUserInfo`、`JwtProperties`、`RequestLogFilter`；操作追踪公共件：`@TraceRequest` / `@TraceStep` 注解、`TraceContext`（ThreadLocal）、`TraceHeaders` 常量 | 不启动 |
| `common-redis` | `RedisService`（封装 `RedisTemplate`）、`RedisConfig`（序列化器配置） | 不启动 |
| `common-log` | `@OperationLog` 注解（操作描述 + 模块归属）、`LogAspect`（环绕切面，把操作信息打到应用日志） | 不启动 |
| `api-system` | system 服务 Feign 客户端：业务接口 `SysUserFeignClient`、`SysUserMiscClient` + 日志落库通道 `OperationLogFeignClient` | 不启动 |
| `api-file` | file 服务 Feign 客户端：`FileFeignClient`、`AvatarFeignClient` | 不启动 |
| `api-book` | book 服务 Feign 客户端：`BookFeignClient` | 不启动 |
| `front` | 前端工程，独立 npm 项目 | `pnpm dev` / 默认 10000 |

## 端口与开发链路

| 进程 | 端口 | 说明 |
|---|---|---|
| Nacos | 8848 | 配置中心 + 注册中心（命名空间 `learn`），各服务的配置与路由都从这里拉取 |
| gateway | 11000 | Spring Cloud Gateway，CORS 校验 → 路由转发 → 鉴权过滤器（白名单外的请求校验 JWT） |
| auth | 13000 | 认证服务，连接 MySQL（3306 / learn 库）与 Redis（6379 / db1） |
| system | 12000 | 系统服务，连接 MySQL（3306 / learn 库）与 Redis（6379 / db1） |
| file | 14000 | 文件服务，连接 MySQL 与 Redis，文件本体存阿里云 OSS |
| book | 15000 | 书籍服务，连接 MySQL 与 Redis，书籍文件经 file 服务/OSS 存取 |
| front 前端 dev server | 10000 | Vite dev server（绑定 127.0.0.1），提供 SPA 入口和 API 代理 |
| 浏览器 | — | 访问 `http://localhost:10000` 或 `http://127.0.0.1:10000`，前端 API 请求经 vite proxy 转发到 gateway |

前端 `vite.config.ts` 把 `/api` 前缀代理到 `http://localhost:11000`（gateway），并 `rewrite` 去掉 `/api` 前缀，因此前端调 `/api/auth/login` 实际打到网关的 `http://localhost:11000/auth/login`；网关再按 Nacos 路由把 `/auth/**` 转发到 auth-server(13000)。gateway 的 CORS 白名单放行 `http://localhost:*` 与 `http://127.0.0.1:*`（Nacos dataId `gateway-server`），白名单之外的 Origin 会在网关层被直接拒绝（403、无后端日志）。

## 两套日志体系

项目中并存两套用途不同的日志，注意区分：

| 体系 | 实现位置 | 产物 | 用途 |
|---|---|---|---|
| **操作日志** | `@OperationLog` 注解（common-log）+ `LogAspect` 环绕切面 | 应用日志（logback 输出到控制台/日志文件），**不落库** | 记录"谁在什么时间调了哪个接口、入参/返回/耗时"，便于本地排查 |
| **调用链追踪** | `@TraceRequest` / `@TraceStep` 注解（common-core）+ 各模块切面/记录器 | 落库到 `sys_user_operation_log`（主表）+ `sys_user_operation_step_log`（子表） | 跨服务调用链审计：一次请求产生一棵方法步骤树，能定位失败步骤与耗时 |

### 调用链追踪的数据形态

- **主表 `sys_user_operation_log`**：一行 = 一次完整请求；字段含 `user_id`（匿名入口登录成功后回填）、`status`（0 失败 / 1 成功）、`error_message`、`description`——由落库端在请求结束时按 `step_no` 先序把步骤拼成摘要，如 `login(auth)->getUserInfoByAccount(feign)->getUserInfoByAccount(system)`。
- **子表 `sys_user_operation_step_log`**：一行 = 调用链上一个方法调用；`log_id` 关联主表，`parent_step_id` 表达嵌套层级（根步骤为 NULL），`step_no` 为请求内先序序号；写操作步骤可带 `target_db` / `target_table`（改了哪个库/表）；`status`：0 失败 / 1 成功 / 2 执行中。

### 跨服务续链（为什么一棵树能横跨 auth 与 system）

1. 入口模块发起 Feign 前，`auth` 的 `FeignClientTraceConfig` 提供的 `RequestInterceptor` 自动从本线程 `TraceContext` 取 `log_id` 与当前父步骤，写入请求头 `X-Trace-Log-Id` / `X-Trace-Parent-Step-Id`；
2. 下游收到**带头请求** = 上游 feign 续链：不重复建主表，只把自己执行的步骤挂到上游父步骤下（system 的 `TraceHeaderSeedFilter` 负责）；收到**无头请求** = 网关直达的新请求：入口模块建主表 + 根步骤；
3. 网关鉴权通过后，会把 `X-User-Id` / `X-Account` / `X-User-Name` 透传到下游（见 `JwtAuthGlobalFilter`），供各模块获取当前操作人；
4. 记日志失败一律 try/catch 降级，**绝不影响登录等业务**。

### 扩展方式

- 打印接口操作日志：方法上标 `@OperationLog(value = "操作描述", model = LogModel.SYSTEM)`（模块归属取 `API/AUTH/BOOK/FILE/GATEWAY/SYSTEM`）；
- 给调用链记一步：标 `@TraceStep(module = "xxx", callType = "service", db = "learn", table = "sys_user")`（仅写方法带 db/table）；
- 新增"网关直达入口"的模块：入口方法标 `@TraceRequest(module = "xxx")`，并参照 auth 提供本模块的记录器与切面。

### 已知边界

- 入口模块在 `finishRequest` 前崩溃，主表会停在 `status=2`「执行中」，可另加定时任务把超时记录置为失败；
- `step_no` 用 MAX+1 分配（同请求步骤基本由单线程串行产生，冲突概率低；生产可给 `(log_id, step_no)` 加唯一索引兜底）；
- `@TraceStep` 直接标在 Feign 接口方法上，个别版本/代理顺序下 AOP 可能不生效，实测无效时改用「门面包装」方式（详见 `TraceStep` 注解注释）。

## 当前进度与后续计划

已完成：

- 五服务骨架（gateway/auth/system/file/book）全部接入 Nacos，服务间 Feign 调用打通；
- 登录认证全链路：JWT 签发、Redis 会话、登出黑名单、前端路由守卫与动态路由；
- 系统管理：用户管理、菜单管理、角色管理（`sys_role` 含超管/普通/图书/题目四个初始角色）、用户角色分配、用户杂项（头像等）；
- 文件能力：头像/书籍上传 OSS、历史头像、OSS 下载；
- 书籍能力：书籍 CRUD、分页阅读（翻页/定位）、书签、Excel 导入导出；
- 两套日志体系（应用操作日志 + 落库调用链）。

待完成：

- **网关路由补齐**：Nacos 中 `gateway-server` 当前只配了 `/auth/**`、`/system/**`（含 `/sys/**`）路由，`file`(14000)、`book`(15000) 的转发路由待增加；
- **角色级鉴权（RBAC）**：角色数据目前未写入登录态（`LoginUserInfo` 无 roles），网关只能校验"是否登录"，尚不能按角色拦截；菜单树为全量返回，未按角色过滤（规划：登录时查 `sys_user_role` 写入 Redis → 网关按"路径→角色"规则拦截 → 菜单按角色过滤）；
- 修改密码接口（`LoginController` 已留位置）。

## 构建与启动

### 后端

在 `remix` 目录执行：

```shell
# 编译全部后端模块
mvn compile

# 只编译 auth 及其依赖模块（common、api 等）
mvn -pl auth -am compile
```

`-pl auth` 表示选择 auth 模块，`-am` 表示同时构建该模块依赖的其他模块。

五个服务分别在 IDE 中运行各自的启动类：`gateway` 的 `GatewayApplication`、`auth` 的 `AuthApplication`、`system` 的 `SystemApplication`、`file` 的 `FileApplication`、`book` 的 `BookApplication`，或对单模块执行 `mvn -pl <模块> -am spring-boot:run`。启动顺序：**Nacos → MySQL/Redis → system/auth/file/book → gateway**（gateway 需等下游服务注册到 Nacos 后，`lb://` 路由才能解析；各服务启动时会从 Nacos 拉取配置，故 Nacos 必须最先就绪）。示例（auth）：

```shell
mvn -pl auth -am spring-boot:run
```

### 前端

在 `front` 目录执行：

```shell
pnpm install      # 安装依赖
pnpm dev          # 启动 dev server，默认 10000 端口
pnpm build        # 生产构建
```

### 数据库与 Redis 前置依赖

- MySQL：`127.0.0.1:3306`，库名 `learn`，账号 `root` / 密码 `666666`（数据源配置见 Nacos dataId `common-datasource`）
- Redis：`127.0.0.1:6379`，密码 `666666`，使用 **db1**（配置见 Nacos dataId `common-datasource`，各服务共用）
- 阿里云 OSS：AK/SK 与 bucket 配置在使用 OSS 的服务中（file 等）
- Nacos：`127.0.0.1:8848`，命名空间 `learn`（ID `d7982b6e-cff5-4b20-b277-fb4c04dc8515`），控制台账号 `nacos`/`nacos`
- 建表脚本：`data/sql/system/` 与 `data/sql/book/` 下

### Nacos 配置存放位置（重要）

gateway/auth/system 等服务的运行配置（数据源、Redis、JWT、MyBatis、网关路由 / CORS 白名单 / 鉴权白名单、日志级别等）**不放在各模块的 `application.yaml` 里**——各模块的 yaml 只声明“从 Nacos 拉取哪个 dataId”。真正的配置内容统一维护在仓库 **`data/`** 目录下：

- `data/README.md`：列出全部 4 个 dataId（`gateway-server`、`auth-server`、`system-server`、`common-datasource`）的**完整配置内容**与部署步骤，是配置的可读文本版。
- `data/nacos/nacos_config_export_*.zip`：Nacos 控制台导出包，可在控制台「配置管理 → 导入」一键恢复同一套配置。

实际运行时以 Nacos 控制台中的配置为准；**在控制台修改配置后，请同步重新导出覆盖 `data/nacos/` 下的 zip，并同步更新 `data/README.md`**，保证仓库与线上配置一致、他人拉代码即可复现。

## 依赖管理规范

### 多模块共用依赖

当两个或更多模块需要同一个依赖时，由父 POM 的 `<dependencyManagement>` 统一管理版本。子模块仍需在自己的 `<dependencies>` 中按需声明，但不再填写版本号。

父 POM：

```xml
<properties>
    <example.version>1.0.0</example.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>example-library</artifactId>
            <version>${example.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

需要该依赖的子模块：

```xml
<dependencies>
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>example-library</artifactId>
    </dependency>
</dependencies>
```

这种方式可以统一版本，同时避免未使用该依赖的模块被动引入它。

### 单模块专用依赖

只有一个模块使用的依赖直接声明在该模块的 `<dependencies>` 中。如果版本已经由 Spring Boot 或其他父级 BOM 管理，则不填写版本号；否则在该模块中明确填写版本。

### Spring Boot 依赖

父模块已经继承 `spring-boot-starter-parent`，Spring Boot 官方依赖通常不需要再次指定版本。`spring-boot-starter-parent` 负责版本和构建规则，真正提供代码的 Starter 仍需由使用它的子模块声明。例如 `auth` 启动类需要：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### 当前父 POM 已统一管理的版本

| 依赖 | 版本 | 管理方式 |
|---|---|---|
| Spring Boot | 3.2.12 | `spring-boot-starter-parent` 父项目 |
| Spring Cloud | 2023.0.4 | 父 POM `<dependencyManagement>` 引入 BOM |
| Spring Cloud Alibaba | 2023.0.3.2 | 父 POM `<dependencyManagement>` 引入 BOM |
| OpenFeign | 4.1.5 | 父 POM `<dependencyManagement>` |
| MyBatis-Plus | 3.5.16 | 父 POM `<dependencyManagement>` 引入 BOM |
| MapStruct | 1.6.3 | 父 POM `<dependencyManagement>` |
| 阿里云 OSS | 0.5.1 | 父 POM `<dependencyManagement>` |
| Fesod | 2.0.2 | 父 POM `<dependencyManagement>` |
| Lombok | 1.18.36 | 父 POM `<dependencyManagement>` |
| Hutool | 5.8.44 | 父 POM `<dependencyManagement>` |
| Spring Security | 6.1.9 | 父 POM `<dependencyManagement>`（仅 `spring-security-crypto`） |
| Spring Data Redis | 跟随 Spring Boot | 父 POM `<dependencyManagement>` |

### 约束

- 不在子模块中重复声明公共依赖的版本。
- 不把 `spring-boot-starter-parent` 放入 `<dependencies>`；它只能作为 `<parent>` 使用。
- 公共依赖优先放入父 POM 的 `<dependencyManagement>`，而不是父 POM 的 `<dependencies>`。
- 新增或升级公共依赖时，只修改父 POM 中的版本并验证所有受影响模块。
