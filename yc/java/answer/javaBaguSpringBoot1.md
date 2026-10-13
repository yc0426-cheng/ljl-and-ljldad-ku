# SpringBoot 八股答案

### Q1

> SpringBoot 的核心注解是什么，由哪些注解组成

核心注解是 **@SpringBootApplication**，它是组合注解：
- **@SpringBootConfiguration**：本质是 @Configuration，声明配置类
- **@EnableAutoConfiguration**：开启自动装配（核心）
- **@ComponentScan**：扫描主类所在包及其子包下的组件

### Q2

> SpringBoot 自动装配的原理

1. @SpringBootApplication 中的 @EnableAutoConfiguration 通过 @Import(AutoConfigurationImportSelector) 导入选择器
2. 选择器读取 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 文件（2.7 之前是 spring.factories），获取所有候选自动配置类（全量 140+ 个）
3. 每个自动配置类上有 @Conditional 条件注解（@ConditionalOnClass 类路径存在、@ConditionalOnMissingBean 用户没自定义、@ConditionalOnProperty 配置开启等），条件满足的配置类才生效
4. 生效的配置类通过 @Bean 向容器注册组件

一句话：**约定大于配置，按条件把 starter 里的配置类和 Bean 自动注册进容器**。

### Q3

> @EnableAutoConfiguration 的工作原理

自动装配入口。内部 @Import(AutoConfigurationImportSelector)，selectImports() 中通过 SpringFactoriesLoader / ImportCandidates 读取配置文件中的自动配置类全类名列表，再经 getAutoConfigurationEntry 过滤（exclude 排除、去重、@Conditional 条件过滤）后交给容器导入。

### Q4

> @Conditional 系列注解的作用

条件装配：满足指定条件才把 Bean 注册进容器。常用衍生注解：
- @ConditionalOnClass：类路径存在指定类（starter 核心机制）
- @ConditionalOnMissingClass / @ConditionalOnMissingBean / @ConditionalOnBean
- @ConditionalOnProperty：配置属性满足条件（如 spring.datasource 存在才配数据源）
- @ConditionalOnWebApplication / @ConditionalOnJava 等
这也是"用户配置优先于默认配置"的实现基础（用户定义了 Bean，默认的就不加载）。

### Q5

> SpringBoot 的启动流程

1. main() 调用 SpringApplication.run()
2. 创建 SpringApplication：推断应用类型（Servlet/Reactive）、加载 Initializer 和 Listener
3. 运行 SpringApplicationRunListeners 启动监听，准备 **Environment**（加载配置文件、环境变量）
4. 创建 **ApplicationContext**（web 环境为 AnnotationConfigServletWebServerApplicationContext）
5. prepareContext：加载主类为 BeanDefinition
6. **refresh()**：IOC 容器初始化核心（自动装配、注册 Bean、onRefresh 中创建并启动**内嵌 Tomcat**）
7. 回调 ApplicationRunner / CommandLineRunner，发布 ApplicationReadyEvent，启动完成

### Q6

> 如何理解 SpringBoot 约定大于配置

框架预先提供默认约定：包扫描主类所在包、默认端口 8080、内置 Tomcat、默认配置文件 application.yml、starter 聚合依赖等，开发者遵循约定即可零配置启动；需要定制时只需覆盖对应配置项，而不用像 Spring 时代写大量 XML。自动装配 + starter 是这一思想的两大实现。

### Q7

> SpringBoot 的配置文件有哪几种格式，有什么区别

- **application.properties**：键值对扁平结构，`server.port=8080`
- **application.yml**：缩进层级结构，可读性好，支持对象、列表，支持单文件多文档块（---）
同一位置两者同时存在时 **properties 优先级更高**。yml 注意：冒号后必须有空格、缩进用空格不能用 Tab。

### Q8

> application.yml 和 application.properties 的加载优先级

同一目录下 **properties > yml**；不同目录：jar 外 config/ > jar 外根目录 > jar 内 config/ > jar 内 classpath 根目录。另外 application-{profile}.yml 优先于 application.yml。

### Q9

> @Value 和 @ConfigurationProperties 的区别

- **@Value**：逐个注入，支持 SpEL，不支持松散绑定和校验，适合注入少量单个值
- **@ConfigurationProperties(prefix="xxx")**：批量绑定整个前缀下的配置到实体类，支持**松散绑定**（spring.my-name 可绑定 myName）、JSR303 数据校验、复杂类型（List/Map），适合配置项多时封装配置类

### Q10

> SpringBoot 如何实现多环境配置（dev、test、prod）

1. 定义 application-dev.yml、application-test.yml、application-prod.yml
2. 主配置文件 `spring.profiles.active=dev` 指定激活环境，或启动时命令行 `--spring.profiles.active=prod` 覆盖
3. yml 也可用 --- 分隔多文档块，每块 spring.config.activate.on-profile 指定环境
生产推荐用环境变量或启动参数激活，配置文件不进代码库。

### Q11

> 什么是 starter，常用的 starter 有哪些

starter（场景启动器）是依赖的聚合包：把某场景所需的依赖 + 自动配置打包，引入即可使用，无需手动管理版本和配置。

常用：spring-boot-starter-web（MVC+Tomcat+Jackson）、starter-test、starter-data-redis、starter-data-jpa、starter-validation、starter-actuator、starter-aop、mybatis-plus-boot-starter、druid-spring-boot-starter。

### Q12

> 如何自定义一个 starter

规范命名：官方 spring-boot-starter-xxx，第三方 xxx-spring-boot-starter。

步骤：① 创建 autoconfigure 模块：编写属性类 @ConfigurationProperties、自动配置类 @AutoConfiguration + @EnableConfigurationProperties + @ConditionalOnXxx 条件注解 + @Bean 注册核心服务；② 在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中登记自动配置类；③ 创建 starter 模块（空壳）聚合 autoconfigure 和所需依赖；④ 使用方引入 starter 依赖 + 配置属性即可。

### Q13

> SpringBoot 内嵌 Tomcat 的原理

引入 spring-boot-starter-web 后，ServletWebServerFactoryAutoConfiguration 通过 @ConditionalOnClass(Tomcat.class) 条件注册 TomcatServletWebServerFactory；容器 refresh() 的 onRefresh 阶段调用 createWebServer()：new 一个 Tomcat 实例、设置端口和上下文、把 DispatcherServlet 注册进去并启动。因此应用 jar 自带服务器，java -jar 直接运行，无需部署外部 Tomcat。

### Q14

> SpringBoot 中如何统一处理异常（@ControllerAdvice + @ExceptionHandler）

① 定义统一响应结构 Result(code,msg,data)；② 自定义业务异常类（继承 RuntimeException）；③ @RestControllerAdvice 类中写多个 @ExceptionHandler 方法：分别处理 BizException、MethodArgumentNotValidException（参数校验）、兜底 Exception；④ 业务代码直接 throw new BizException(...)。好处：Controller 无需 try-catch，错误响应格式统一。

### Q15

> SpringBoot 中如何实现定时任务（@Scheduled、cron 表达式）

① 启动类加 @EnableScheduling；② 方法标 @Scheduled。方式：cron（6 位：秒 分 时 日 月 周，如 `0 0 2 * * ?` 每天凌晨2点）、fixedRate=5000（固定频率）、fixedDelay=5000（上次执行完后再延迟）、initialDelay（首次延迟）。

注意：默认单线程调度器，任务多会阻塞，建议配置 TaskScheduler 线程池；集群部署需分布式锁或调度中心（xxl-job）防止重复执行。

### Q16

> SpringBoot 如何整合 MyBatis / MyBatisPlus

MyBatis：① 引入 mybatis-spring-boot-starter；② 配置 spring.datasource 数据源；③ yml 配 mybatis.mapper-locations=classpath:mapper/*.xml、type-aliases-package；④ 启动类 @MapperScan("com.xxx.mapper")；⑤ 接口 + XML 编写 SQL。

MyBatisPlus：换成 mybatis-plus-boot-starter，yml 前缀改 mybatis-plus，实体加 @TableName、Mapper 继承 BaseMapper 即可免写单表 SQL。

### Q17

> SpringBoot 如何解决跨域问题

浏览器同源策略（协议/域名/端口任一不同即跨域）。方案：
1. @CrossOrigin 标注在 Controller 类或方法上
2. 全局配置：实现 WebMvcConfigurer，重写 addCorsMappings，设置 allowedOrigins/allowedMethods
3. 注册 CorsFilter Bean（适合网关、过滤器场景）
4. 生产环境常在 Nginx 或网关层统一处理
复杂请求会先发 OPTIONS 预检请求，需放行。

### Q18

> SpringBoot Actuator 的作用，常用的监控端点

Actuator 提供生产级监控运维端点。常用：`/actuator/health`（健康检查，含数据库/Redis 状态）、`/metrics`（JVM、HTTP 指标）、`/env`（环境配置）、`/info`、`/loggers`（动态调整日志级别）、`/threaddump`、`/heapdump`。

默认只暴露 health 和 info，需配置 `management.endpoints.web.exposure.include=*` 开放；生产一般配合 Prometheus + Grafana 做可视化告警。

### Q19

> SpringBoot 的配置加载优先级（命令行、配置文件、环境变量）

从高到低：**命令行参数**（--server.port=8081）> SPRING_APPLICATION_JSON > **JVM 系统属性**（-D）> **OS 环境变量** > jar 外 application-{profile} > jar 外 application > jar 内 profile 配置 > jar 内 application。规律：外部 > 内部，profile 配置 > 默认配置，命令行最高。

### Q20

> SpringBoot 如何实现异步调用（@Async）

① 启动类或配置类加 @EnableAsync；② 需要异步的方法标注 @Async，调用时由代理提交到线程池执行；返回值支持 void、Future、CompletableFuture。

注意：默认使用 SimpleAsyncTaskExecutor（不复用线程），生产必须自定义 ThreadPoolTaskExecutor Bean；同类自调用 @Async 失效（同事务失效原因）；方法必须是 public。

### Q21

> @Async 和 @Transactional 一起使用需要注意什么

① **事务上下文不跨线程**：@Async 方法在另一个线程执行，调用方的事务对它完全不可见，异步方法里需要自己标 @Transactional 开启新事务；② 异步方法内部抛异常不会影响主线程事务，也不会被主线程的全局异常处理器捕获，需在异步方法内自行处理或用 AsyncUncaughtExceptionHandler；③ 二者都是基于代理，都要避免同类自调用失效。

### Q22

> SpringBoot 的过滤器、拦截器、监听器的区别和执行顺序

- **Filter 过滤器**：Servlet 规范，拦截所有请求（含静态资源），用于字符编码、跨域、全局XSS过滤
- **Interceptor 拦截器**：SpringMVC 提供，只拦截进入 Controller 的请求，可注入 Spring Bean，用于登录鉴权
- **Listener 监听器**：监听 ServletContext/HttpSession 等事件，如 Spring 的 ContextLoaderListener

执行顺序：Listener（容器事件）→ Filter → DispatcherServlet → Interceptor.preHandle → Controller → Interceptor.postHandle → 视图渲染 → afterCompletion → Filter。

### Q23

> SpringBoot 如何实现优雅关闭

配置 `server.shutdown=graceful` + `spring.lifecycle.timeout-per-shutdown-phase=30s`。收到停止信号后：不再接收新请求 → 等待正在处理的请求完成（最长超时时间）→ 关闭容器和服务器。配合 K8s 的 preStop 钩子使用效果最佳，避免请求被强制中断。

### Q24

> SpringBoot 如何进行参数校验（@Validated、@Valid）

① 引入 spring-boot-starter-validation；② DTO 字段加约束：@NotNull（不能null）、@NotBlank（字符串非空串）、@Size、@Min/@Max、@Email、@Pattern；③ Controller 参数前加 @Valid 或 @Validated；④ 校验失败抛 MethodArgumentNotValidException，在全局异常处理器中提取 fieldErrors 返回友好提示。分组校验用 @Validated(Update.class)；嵌套对象校验在字段上加 @Valid。

### Q25

> SpringBoot 打包后的 jar 为什么可以直接运行

fat jar（可执行 jar）结构：所有依赖 jar 以嵌套形式存放在 BOOT-INF/lib，自己的类在 BOOT-INF/classes。MANIFEST.MF 中 Main-Class 指定为 **JarLauncher**（Spring Boot 自定义启动器），它使用自定义类加载器 LaunchedURLClassLoader 加载嵌套 jar，再反射调用 Start-Class（我们的 main 方法）启动，配合内嵌 Tomcat 完成自举。由 spring-boot-maven-plugin 的 repackage 生成。

### Q26

> SpringBoot 读取配置文件的几种方式

1. @Value("${key}")：注入单个值
2. @ConfigurationProperties(prefix="xxx")：批量绑定配置类（推荐，配合 @EnableConfigurationProperties）
3. Environment#getProperty("key")：编程式读取
4. @PropertySource("classpath:xxx.properties")：加载自定义配置文件
5. 原生 @YamlPropertiesFactoryBean 等（少用）

### Q27

> 如何在 SpringBoot 中配置数据源和连接池（HikariCP）

yml 配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db?useSSL=false
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20      # 最大连接数
      minimum-idle: 5            # 最小空闲连接
      connection-timeout: 30000
```
SpringBoot 2.x 默认连接池是 **HikariCP**（字节码精简、FastList 无锁设计，性能最优）；国内也常用 Druid（自带 SQL 监控页面），引入 druid-spring-boot-starter 配置 StatFilter。

### Q28

> SpringBoot 事务的使用和注意事项

类或方法标 @Transactional（基于 AOP 代理）。注意事项：方法必须 public；**同类自调用失效**；默认只回滚 RuntimeException，建议写 rollbackFor = Exception.class；异常被 catch 住不回滚；只读查询可加 readOnly=true 提示优化；多线程内不生效；rollbackOnly 标记问题（内层事务回滚外层抛 UnexpectedRollbackException）。

### Q29

> SpringBoot 如何整合 Redis

① 引入 spring-boot-starter-data-redis；② 配置 spring.data.redis 的 host/port/password/database（2.x 前是 spring.redis）；③ 使用 RedisTemplate（操作对象需配置序列化器，否则默认 JDK 序列化出现乱码）或 StringRedisTemplate（字符串场景）；④ 通常封装 RedisUtil 工具类。连接池默认 Lettuce（基于 Netty），可换 Jedis；分布式锁推荐 Redisson。

### Q30

> SpringBoot 2.x 和 1.x 的主要区别

1. 基线升级：JDK 8+、Spring 5
2. 默认连接池 Jedis → **HikariCP**
3. 默认代理策略 JDK → **CGLIB**（proxyTargetClass=true）
4. 支持 WebFlux 响应式编程（Netty）
5. Actuator 端点变化：默认仅暴露 health/info，安全加固
6. 配置属性大量迁移（如 server.context-path → server.servlet.context-path）
7. Quartz 自动配置、HttpClient 换用、2.4 起配置文件处理机制重构
