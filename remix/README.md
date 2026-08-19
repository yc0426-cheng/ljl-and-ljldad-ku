# Remix 学习管理系统

Remix 是一个基于 Spring Boot 的学习管理系统，后端采用 Maven 多模块结构。项目目前处于基础架构阶段，由父模块统一聚合和管理后端模块，`gateway` 作为系统入口，`auth` 用于承载认证与授权能力。

## 技术基线

- Java 17
- Spring Boot 3.2.12
- Maven 3.9+
- Lombok 1.18.36

## 项目结构

```text
remix/
├── pom.xml       # Maven 聚合父模块，统一管理版本和构建配置
├── gateway/      # 网关服务及当前后端启动入口
├── auth/         # 认证与授权模块
└── front/        # 前端工程目录，不属于 Maven 后端模块
```

Maven 父子关系如下：

```text
spring-boot-starter-parent
          ↓
        remix
       ↙     ↘
    auth    gateway
```

`ml` 继承 Spring Boot 父项目，并通过 `<modules>` 聚合 `auth` 和 `gateway`。子模块只继承 `remix`，从而间接获得 Spring Boot 的依赖版本与构建配置。

## 构建

在 `remix` 目录执行：

```shell
mvn compile
```

只编译网关及其依赖模块：

```shell
mvn -pl gateway -am compile
```

`-pl gateway` 表示选择网关模块，`-am` 表示同时构建该模块依赖的其他模块。

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

父模块已经继承 `spring-boot-starter-parent`，Spring Boot 官方依赖通常不需要再次指定版本。`spring-boot-starter-parent` 负责版本和构建规则，真正提供代码的 Starter 仍需由使用它的子模块声明。例如 `gateway` 启动类需要：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
```

### 约束

- 不在子模块中重复声明公共依赖的版本。
- 不把 `spring-boot-starter-parent` 放入 `<dependencies>`；它只能作为 `<parent>` 使用。
- 公共依赖优先放入父 POM 的 `<dependencyManagement>`，而不是父 POM 的 `<dependencies>`。
- 新增或升级公共依赖时，只修改父 POM 中的版本并验证所有受影响模块。
