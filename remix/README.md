# Remix 学习管理系统

Remix 是一个基于 Spring Boot 3.2 的学习管理系统，后端采用 Maven 多模块结构，前端使用 Vue 3 + TypeScript + Vite。父 POM 统一聚合后端各模块并管理公共依赖版本，`auth` 模块为当前后端启动入口，`gateway` 为规划中的网关入口（尚未实现）。

## 技术基线

### 后端

- Java 17
- Spring Boot 3.2.12
- Spring Security 6.1.9（通过 `spring-boot-starter-oauth2-resource-server` 传递引入）
- MyBatis-Plus 3.5.16（Spring Boot 3 兼容 starter）
- Spring Data Redis（Lettuce 客户端）
- Hutool 5.8.44
- Lombok 1.18.36
- Maven 3.9+

### 前端

- Vue 3 + TypeScript
- Vite 5+
- Element Plus（按需自动导入）
- Pinia（状态管理）
- Vue Router（路由）
- Axios（HTTP 客户端）
- pnpm（包管理器）

前端工程独立的开发流程、校验命令与写作规范详见 [`front/README.md`](front/README.md)。

## 项目结构

```text
remix/
├── pom.xml              # Maven 聚合父 POM，统一管理版本和构建配置
├── README.md            # 本文件
├── auth/                # 认证模块（登录、登出、JWT 签发），当前后端启动入口
├── system/              # 系统业务模块（用户管理、用户信息 CRUD）
├── common/              # 后端公共模块
│   ├── common-core/     # 通用核心：异常体系、常量、上下文、POJO、配置属性
│   └── common-redis/    # Redis 工具：RedisService、RedisTemplate 配置
├── gateway/             # 网关模块（规划中，目前为空骨架）
├── front/               # 前端工程（Vue 3 + TS + Vite），不属于 Maven 后端模块
└── sql/                 # 数据库脚本
    └── system/          # system 模块相关 SQL（建表、初始化数据）
```

## Maven 父子关系

```text
spring-boot-starter-parent
          ↓
        remix (pom)
       ↙   ↓   ↘   ↘
    auth  gateway  system  common
                       ↙   ↘
              common-core  common-redis
```

`remix` 继承 `spring-boot-starter-parent`，并通过 `<modules>` 聚合 `auth`、`gateway`、`system`、`common` 四个一级模块。`common` 自身也是一个 POM 模块，再聚合 `common-core` 和 `common-redis` 两个子模块。子模块只继承 `remix`，从而间接获得 Spring Boot 的依赖版本与构建配置。

## 模块说明

| 模块 | 职责 | 启动类 / 端口 |
|---|---|---|
| `auth` | 登录、登出、JWT 签发与黑名单管理；当前后端 HTTP 启动入口 | `AuthApplication` / 11000 |
| `system` | 用户信息 CRUD、密码错误次数维护、登录用户信息查询 | （当前未独立启动，作为 `auth` 的依赖） |
| `common-core` | `BizException` / `BaseException` 异常体系、`RedisKeyConstant`、`LoginUserHolder`、`LoginUserInfo`、`JwtProperties` | 不启动 |
| `common-redis` | `RedisService`（封装 `RedisTemplate`）、`RedisConfig`（序列化器配置） | 不启动 |
| `gateway` | 规划中的统一网关入口（鉴权、路由、限流） | 尚未实现 |
| `front` | 前端工程，独立 npm 项目 | `pnpm dev` / 默认 10000 |
| `sql` | 数据库初始化脚本 | 不启动 |

## 端口与开发链路

| 进程 | 端口 | 说明 |
|---|---|---|
| auth 后端 | 11000 | Spring Boot 启动，连接 MySQL（3306 / learn 库）和 Redis（6379 / db6） |
| front 前端 dev server | 10000 | Vite dev server，提供 SPA 入口和 API 代理 |
| 浏览器 | — | 访问 `http://localhost:10000`，前端 API 请求经 vite proxy 转发到 auth |

前端 `vite.config.ts` 配置 `/api` 前缀代理到 `http://localhost:11000`，并 `rewrite` 去掉 `/api` 前缀，因此前端调 `/api/auth/login` 实际打到后端 `http://localhost:11000/auth/login`。

## 构建与启动

### 后端

在 `remix` 目录执行：

```shell
# 编译全部后端模块
mvn compile

# 只编译 auth 及其依赖模块（common、system 等）
mvn -pl auth -am compile
```

`-pl auth` 表示选择 auth 模块，`-am` 表示同时构建该模块依赖的其他模块。

启动 auth 服务：在 IDE 中运行 `auth` 模块的 `AuthApplication` 主类，或：

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

- MySQL：`127.0.0.1:3306`，库名 `learn`，账号 `root` / 密码 `666666`（见 `auth/src/main/resources/application.yaml`）
- Redis：`127.0.0.1:6379`，密码 `666666`，使用 **db6**（不是默认的 db0）
- 建表脚本：`sql/system/` 下

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
| Lombok | 1.18.36 | 父 POM `<dependencyManagement>` |
| Hutool | 5.8.44 | 父 POM `<dependencyManagement>` |
| MyBatis-Plus | 3.5.16 | 父 POM `<dependencyManagement>` 引入 BOM |
| Spring Security | 6.1.9 | 父 POM `<dependencyManagement>`（仅 `spring-security-crypto`） |
| Spring Data Redis | 跟随 Spring Boot | 父 POM `<dependencyManagement>` |

### 约束

- 不在子模块中重复声明公共依赖的版本。
- 不把 `spring-boot-starter-parent` 放入 `<dependencies>`；它只能作为 `<parent>` 使用。
- 公共依赖优先放入父 POM 的 `<dependencyManagement>`，而不是父 POM 的 `<dependencies>`。
- 新增或升级公共依赖时，只修改父 POM 中的版本并验证所有受影响模块。
