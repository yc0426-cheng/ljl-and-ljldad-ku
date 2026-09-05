# 项目初始化数据

本目录存放项目部署/初始化所需的全部静态数据,包括:

- Nacos 配置中心配置(供 gateway / auth / system 三个微服务拉取)
- 数据库建表 SQL

## 目录结构

```
data/
├── README.md                              # 本文件
├── nacos/
│   └── nacos_config_export_*.zip          # Nacos 控制台导出包,可直接导入
└── sql/
    └── system/
        └── sys_user.sql                   # 数据库建表 + 初始数据
```

---

## 一、Nacos 配置

### 命名空间

| 项 | 值 |
|---|---|
| 命名空间名 | `learn` |
| 命名空间 ID | `d7982b6e-cff5-4b20-b277-fb4c04dc8515` |
| Group | `DEFAULT_GROUP` |

### 1. 公共数据源配置

- **dataId**:`common-datasource`(无 `.yaml` 后缀)
- **Group**:`DEFAULT_GROUP`
- **用途**:三个服务共享的 MySQL + Redis 连接,私有配置可覆盖差异化字段

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/learn?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 666666
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  data:
    redis:
      host: 127.0.0.1
      password: 666666
      port: 6379
      timeout: 5000ms
      database: 1
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

### 2. system-server 私有配置

- **dataId**:`system-server`(无 `.yaml` 后缀)
- **Group**:`DEFAULT_GROUP`
- **对应服务**:`spring.application.name = system-server`(端口 12000)

```yaml
# MyBatis-Plus 配置
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.zz.*.entity
  type-enums-package: com.zz.*.enums
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: assign_id  # 全局配置主键策略为雪花算法

# 日志输出级别
logging:
  level:
    org.springframework.web: DEBUG
    org.springframework.web.servlet: DEBUG
    org.springframework.web.servlet.DispatcherServlet: DEBUG
```

### 3. auth-server 私有配置

- **dataId**:`auth-server`
- **Group**:`DEFAULT_GROUP`
- **对应服务**:`spring.application.name = auth-server`(端口 13000)

```yaml
# MyBatis-Plus 配置
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.zz.*.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: assign_id  # 全局配置主键策略为雪花算法

# 日志输出级别
logging:
  level:
    org.springframework.web: DEBUG
    org.springframework.web.servlet: DEBUG
    org.springframework.web.servlet.DispatcherServlet: DEBUG

# JWT 配置(对应 JwtProperties)
security:
  jwt:
    secret: zhdzXFtQSfyZ1GHYp9gBKlpKhjDH9axabdhc=
    session-expire-time: 1800
```

### 4. gateway-server 私有配置

- **dataId**:`gateway-server`
- **Group**:`DEFAULT_GROUP`
- **对应服务**:`spring.application.name = gateway-server`(端口 11000)

```yaml
spring:
  cloud:
    gateway:
      enabled: true
      # 路由:uri 由 http://localhost:直连 改为 lb://服务名,经 Nacos 注册中心负载均衡
      routes:
        # 认证中心:/auth/login、/auth/check、/auth/logout 等
        - id: server-auth
          uri: lb://auth-server # uri 中的auth-server == nacos中的配置名
          predicates:
            - Path=/auth/**
          filters:
            # 保留请求头中的 Host 信息,后端日志可看到真实来源
            - PreserveHostHeader=true
        # 系统服务:用户管理等接口
        - id: server-system
          uri: lb://system-server
          predicates:
            - Path=/system/**
          filters:
            - PreserveHostHeader=true
      # 跨域:前端直连网关时放行 Vite 开发服务器(走 Vite 代理时同源,不触发跨域)
      # 注意:vite.config.ts 把 dev server 绑定在 127.0.0.1,浏览器页面 Origin 是
      # http://127.0.0.1:10000(或 http://localhost:10000)。若白名单漏掉实际 Origin,
      # 网关会在 CORS 阶段直接返回 403(空 body、无任何后端日志),登录表现为"点击登录弹 403"。
      globalcors:
        cors-configurations:
          '[/**]':
            # 通配端口:以后改 Vite 端口/换 127.0.0.1 与 localhost 访问都不需要再改这里;
            # 部署到局域网/公网域名时,把实际 Origin 追加到本列表(allow-credentials=true 时不能用 "*")
            allowed-origin-patterns:
              - 'http://localhost:*'
              - 'http://127.0.0.1:*'
            allowed-methods:
              - "*"
            allowed-headers:
              - "*"
            allow-credentials: true

# 路由白名单:不走 JWT 校验的路径
auth:
  white-list:
    - "POST /auth/login"
    - "POST /auth/check"
    - "POST /auth/logout"

# 路由转发调试日志,排查网关问题时打开
logging:
  level:
    org.springframework.cloud.gateway: INFO
```

### 关键约定

1. **dataId 命名 = `spring.application.name`**:nacos-config 在 `spring.config.import` 模式下,**显式写 dataId 不会自动加后缀**,所以 Nacos 控制台的 dataId 必须和 yaml 里 `spring.config.import` 写的完全一致(无 `.yaml` 后缀)。
2. **加载顺序**:本地 `application.yaml` 的 `spring.config.import` 列表里**越靠后优先级越高**。约定顺序:先 `common-datasource`,后私有 dataId,这样私有可覆盖公共同名字段。
3. **namespace 必须显式传**:`spring.config.import: optional:nacos:xxx?namespace=<真实ID>&group=DEFAULT_GROUP`,`namespace` 不会从 `spring.cloud.nacos.config.namespace` 自动继承。

---

## 二、SQL 建表语句

数据库名 `learn`,字符集 `utf8mb4`。当前仅 `sys_user` 一张表。

```sql
-- 创建数据库（如果已存在则忽略）
CREATE DATABASE IF NOT EXISTS learn
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换到该数据库
USE learn;

-- 建表（如果表已存在也忽略）
CREATE TABLE IF NOT EXISTS sys_user
(
    user_id        BIGINT PRIMARY KEY COMMENT '用户ID',
    account        VARCHAR(200)      NOT NULL COMMENT '账号',
    name           varchar(200)      not null comment '姓名',
    password       varchar(200)      NOT NULL comment '密码',
    phone          char(11)          not null comment '手机号码',
    id_number       char(18)          not null comment '身份证号',
    email          varchar(200)      not null comment '邮箱',
    pass_error_count int     default 0 not null comment '密码错误次数',
    status         int     default 1 not null comment '状态',
    last_login_time  timestamp(6) comment '最后登录时间',
    del_flag        bool default false comment '删除标记'
) COMMENT ='用户信息表';

insert into learn.sys_user(user_id, account, name, password, phone, id_number, email, pass_error_count, status, last_login_time,
                           del_flag)
    value (
          1,'admin', '管理员', '666666', 11122223333, 123456888888889999, '666666@qq.com', 0, 1, null, 0
    );
```

---

## 部署步骤

### 1. 准备 MySQL

```bash
mysql -u root -p < data/sql/system/sys_user.sql
```

执行后 `learn` 库会出现 `sys_user` 表 + 1 条 admin 初始数据(密码明文 `666666`,仅供本地调试)。

### 2. 准备 Nacos

1. 启动 Nacos Server(本机单机模式,默认 8848)。
2. 控制台 → **命名空间管理** → 新建命名空间,显示名填 `learn`,命名空间 ID 可留空(系统自动生成 UUID,部署时把 README 顶部的 UUID 替换成实际值)。
3. 控制台 → **配置管理** → 切到 `learn` 命名空间,按上面 4 个 dataId 各建一条配置,Group 都填 `DEFAULT_GROUP`,内容从 README 拷贝。
4. (可选)如使用 Nacos 控制台导出包,直接导入 `data/nacos/nacos_config_export_*.zip`。

### 3. 启动微服务

顺序:**Nacos → MySQL/Redis → system/auth → gateway**。

- gateway 是最后起的,需要 system/auth 先注册到 Nacos 才能让 `lb://server-auth`、`lb://system-server` 路由解析成功。
- system 已接入 Nacos,启动后能在 Nacos 控制台 → 服务管理 → 服务列表 看到 `system-server` 实例。
- auth、gateway 待按 README 中"待接入"配置补齐本地 `application.yaml`。
