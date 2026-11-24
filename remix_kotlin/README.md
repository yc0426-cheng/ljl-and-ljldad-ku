# Remix Kotlin 学习管理系统

Remix Kotlin 是基于 Kotlin + Spring Boot 3.2 的学习管理系统，后端采用 Gradle 多模块结构。当前已实现 `gateway`(11000)、`auth`(13000)、`system`(12000) 三个可独立启动的服务，均注册到 Nacos（命名空间 `learn`）并从中拉取配置。

## 技术基线

- Kotlin 2.1.20
- Java 17
- Spring Boot 3.2.12
- Spring Cloud 2023.0.4 + Spring Cloud Alibaba 2023.0.3.2（Nacos 注册/配置中心）
- Spring Cloud Gateway（响应式网关）
- Spring Cloud OpenFeign（服务间远程调用）
- Spring Security 6（BCrypt 密码加密）
- MyBatis-Plus 3.5.16（Spring Boot 3 兼容 starter）
- MapStruct 1.6.3（对象转换，kapt 编译期生成）
- Spring Data Redis（Lettuce 客户端）
- Hutool 5.8.32（含 JWT）
- jackson-module-kotlin（Kotlin 数据类 JSON 序列化）
- Gradle 9.8.2（Kotlin DSL）

## 项目结构

```text
remix_kotlin/
├── settings.gradle.kts    # Gradle 多模块声明
├── build.gradle.kts       # 根构建脚本：统一插件版本、BOM 导入、公共依赖
├── gateway/               # 网关服务（路由转发 + JWT 鉴权过滤器 + 白名单）
├── auth/                  # 认证服务（登录、登出、JWT 签发与黑名单）
├── system/                # 系统服务（用户管理）
├── common/                # 公共模块（聚合 common-core + common-redis）
│   ├── common-core/       # 通用核心：异常体系、常量、上下文、POJO、配置属性、注解
│   └── common-redis/      # Redis 工具：RedisService
└── api/                   # Feign 客户端定义（聚合 api-system）
    └── api-system/        # system 服务客户端：SysUserFeignClient
```

## 模块说明

| 模块 | 职责 | 启动类 / 端口 |
|---|---|---|
| `gateway` | 统一网关入口：按 Nacos 路由转发，JWT 鉴权过滤 + 白名单，CORS 跨域配置，用户信息透传到下游请求头 | `GatewayApplication` / 11000 |
| `auth` | 登录、登出、JWT 签发与黑名单管理（校验用户时经 Feign 调 system） | `AuthApplication` / 13000 |
| `system` | 用户管理（CRUD、分页、登录密码错误计数） | `SystemApplication` / 12000 |
| `common-core` | `BaseException` 异常体系、`RedisKeyConstant`、`LoginUserHolder`、`LoginUserInfo`、`JwtProperties`、`@RestResource`/`@PostResource`/`@GetResource` 注解 | 不启动 |
| `common-redis` | `RedisService`（封装 `RedisTemplate`，JSON 序列化） | 不启动 |
| `api-system` | system 服务 Feign 客户端：`SysUserFeignClient` | 不启动 |

## 端口与开发链路

| 进程 | 端口 | 说明 |
|---|---|---|
| Nacos | 8848 | 配置中心 + 注册中心（命名空间 `learn`） |
| gateway | 11000 | Spring Cloud Gateway，CORS → 路由转发 → JWT 鉴权 |
| auth | 13000 | 认证服务，连接 MySQL 与 Redis |
| system | 12000 | 系统服务，连接 MySQL 与 Redis |
| MySQL | 3306 | 库名 `learn`，账号 `root` / 密码 `666666` |
| Redis | 6379 | 密码 `666666`，使用 db1 |

请求链路：`前端 → gateway(11000) → auth(13000) → [Feign] → system(12000)`

## 构建与启动

### 编译

```shell
# 编译全部模块
.\gradlew.bat compileKotlin

# 只编译 system 及其依赖
.\gradlew.bat :system:compileKotlin

# 清理后重新编译
.\gradlew.bat clean compileKotlin
```

### 启动

启动顺序：**Nacos → MySQL/Redis → system → auth → gateway**

```shell
# 启动 system
.\gradlew.bat :system:bootRun

# 启动 auth
.\gradlew.bat :auth:bootRun

# 启动 gateway
.\gradlew.bat :gateway:bootRun
```

或在 IDE 中直接运行各模块的启动类。

### 前置依赖

- Nacos：`127.0.0.1:8848`，命名空间 `learn`（ID `02ff3124-6350-4c04-8cb8-8d9012350754`），控制台 `nacos`/`nacos`
- MySQL：`127.0.0.1:3306`，库名 `learn`，账号 `root` / 密码 `666666`
- Redis：`127.0.0.1:6379`，密码 `666666`，使用 db1

### Nacos 配置

各模块的 `application.yaml` 只声明"从 Nacos 拉取哪个 dataId"，真正的配置内容维护在 Nacos 控制台：

| dataId | 内容 |
|---|---|
| `common-datasource` | MySQL 数据源 + Redis 连接 + 阿里云 OSS |
| `system-server` | MyBatis-Plus 配置 + 日志级别 |
| `auth-server` | JWT 密钥 + 会话过期时间 |
| `gateway-server` | 网关路由 + CORS + 鉴权白名单 |

## 依赖管理规范

### 版本统一管理

根 `build.gradle.kts` 通过 `subprojects` 块统一管理：

- **插件版本**：`plugins` 块中 `apply false`，子模块按需应用
- **BOM 导入**：`spring-boot-dependencies`、`mybatis-plus-bom`、`spring-cloud-dependencies`、`spring-cloud-alibaba-dependencies`
- **公共依赖**：Lombok、MapStruct 等通过 `afterEvaluate` 延迟添加（解决 `subprojects` 执行时机早于子模块 `plugins` 的问题）

### 模块间依赖

```kotlin
// 子模块中引用其他模块
implementation(project(":common:common-core"))
implementation(project(":common:common-redis"))
implementation(project(":api:api-system"))
```

### 约束

- 不在子模块中重复声明公共依赖的版本（由 BOM 管理）
- 公共依赖优先放入根 `build.gradle.kts` 的 `subprojects` 块
- 新增或升级公共依赖时，只修改根脚本中的版本并验证所有受影响模块

## Kotlin 与 Java 差异注意事项

| 场景 | 注意事项 |
|---|---|
| 实体类 | 用 `open class` + `var` 属性（MyBatis-Plus 需要无参构造器 + setter） |
| DTO/VO | 用 `data class` + `var` + 默认值（MapStruct 和 Jackson 都需要 setter） |
| 枚举 | 构造参数写在 `enum class Name(...)` 中，不需要单独构造方法 |
| Lambda 查询 | `lambdaQuery().eq(Entity::field, val)` 在 Kotlin 中可能解析异常，建议用 `QueryWrapper<Entity>().eq("column", val)` |
| 注解 | `@field:NotBlank`（字段级校验）、`@get:AliasFor`（元注解参数转发） |
| 构造器注入 | 直接写在类参数中，不需要 `@Autowired` |
| 可空性 | 数据库可空字段必须声明为 `Type?`，否则运行时 NPE |
