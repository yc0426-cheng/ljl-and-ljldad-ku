# Spring 八股答案

### Q1

> spring启动过程

1. 加载配置（XML/注解），定位资源
2. 创建 BeanFactory，解析并注册所有 BeanDefinition 到 BeanDefinitionMap
3. 执行 BeanFactoryPostProcessor（如占位符解析、@Configuration 处理）
4. 注册 BeanPostProcessor
5. 实例化所有非懒加载的单例 Bean：实例化 → 属性注入 → 初始化（详见 Bean 生命周期）
6. 发布 ContextRefreshedEvent 等事件；web 环境下初始化 DispatcherServlet，启动完成

### Q2

> spring事务失效场景

1. 方法不是 public（代理只增强 public 方法）
2. **同类自调用**：this.method() 绕过了代理对象（最常考）
3. 异常被 try-catch 吞掉，事务感知不到
4. 抛出的是 checked 异常且未指定 rollbackFor（默认只回滚 RuntimeException/Error）
5. 类没有被 Spring 管理（没加 @Service 等）
6. 数据库引擎不支持事务（MyISAM）
7. 多线程调用，事务上下文不跨线程传播
8. 传播行为配置错误（如 NOT_SUPPORTED）
9. @Transactional 加在接口上且使用 CGLIB 代理等代理方式不匹配问题

### Q3

> spring事务的传播行为

- **REQUIRED**（默认）：当前有事务就加入，没有就新建
- **REQUIRES_NEW**：挂起当前事务，新建独立事务（内层回滚不影响外层已提交部分）
- **NESTED**：嵌套事务，基于 savepoint，内层回滚可只回滚到保存点
- **SUPPORTS**：有事务就加入，没有就以非事务运行
- **NOT_SUPPORTED**：挂起当前事务，以非事务运行
- **MANDATORY**：必须在事务中运行，否则抛异常
- **NEVER**：不能在事务中运行，否则抛异常

### Q4

> spring bean的生命周期

1. **实例化**（构造方法/工厂方法）
2. **属性填充**（依赖注入 @Autowired）
3. **Aware 回调**：BeanNameAware、BeanFactoryAware、ApplicationContextAware
4. **BeanPostProcessor#postProcessBeforeInitialization**
5. **初始化**：@PostConstruct → InitializingBean#afterPropertiesSet → init-method
6. **BeanPostProcessor#postProcessAfterInitialization**（AOP 代理对象在此生成）
7. **使用中**（单例存入单例池）
8. **销毁**：@PreDestroy → DisposableBean#destroy → destroy-method

### Q5

> springAOP通知的类型

@Before 前置、@After 后置（最终，无论是否异常都执行）、@AfterReturning 返回后（方法正常返回后）、@AfterThrowing 异常后、@Around 环绕（功能最强，可以控制目标方法是否执行、修改参数和返回值）。

### Q6

> Spring IOC 容器初始化过程

① Resource 定位（找到 Bean 定义来源：XML/注解）→ ② BeanDefinition 的载入与解析（转成 BeanDefinition）→ ③ 注册到 BeanDefinitionRegistry（本质是一个 Map）→ ④ 执行 BeanFactoryPostProcessor 扩展 → ⑤ 非懒加载单例 Bean 的实例化和初始化。注意：容器初始化完成 ≠ 所有 Bean 已创建，懒加载 Bean 首次 getBean 时才创建。

### Q7

> Spring Bean 注册到容器有哪些方式

1. XML：`<bean id class>`
2. 注解扫描：@Component 及派生（@Service/@Repository/@Controller）
3. 配置类：@Configuration + @Bean（适合第三方类）
4. @Import：导入普通类 / ImportSelector / ImportBeanDefinitionRegistrar
5. FactoryBean 工厂 Bean（getObject 返回实际对象，如 MyBatis 的 MapperFactoryBean）
6. BeanDefinitionRegistryPostProcessor 编程式动态注册

### Q8

> Spring 自动装配的方式有哪些

XML 时代：byName、byType、constructor。
注解时代：@Autowired（先 byType 再 byName）、@Resource（JSR-250，先 byName 再 byType）、@Qualifier 指定名称、@Primary 默认优先、构造器/setter/字段注入三种位置。
SpringBoot 的自动装配：@EnableAutoConfiguration + 条件注解按 classpath 自动配置。

### Q9

> @Qualifier 注解有什么作用

配合 @Autowired，**按名称**指定注入哪一个 Bean，解决同一类型存在多个 Bean 时的歧义（NoUniqueBeanDefinitionException）。用法：`@Autowired @Qualifier("mysqlUserDao")`。

### Q10

> @Bean 和 @Component 有什么区别

- **@Component**：标注在类上，通过类路径扫描自动注册，类的创建逻辑由 Spring 默认构造
- **@Bean**：标注在配置类的方法上，方法返回值注册为 Bean，创建逻辑自己控制，常用于**第三方库的类**（无法在其源码上加 @Component）
- @Bean 可以灵活设置 initMethod、destroyMethod、按条件创建；一个类上只能有一个 @Component

### Q11

> Spring 事务在什么情况下会失效

同第 2 题：非 public 方法、同类自调用、异常被捕获、checked 异常未指定 rollbackFor、类未被 Spring 管理、多线程、传播行为配置错误、存储引擎不支持事务。核心原因：事务基于 AOP 代理实现，任何绕过代理或让代理感知不到异常的行为都会导致失效。

### Q12

> Spring 中的 @Value 注解的作用是什么

向 Bean 的字段、构造参数、方法参数注入值，支持三种来源：① 配置文件属性 `@Value("${server.port}")`；② SpEL 表达式 `@Value("#{user.name}")`；③ 字面量 `@Value("abc")`。不能注入静态字段；批量绑定配置建议用 @ConfigurationProperties。

### Q13

> Spring 中的 @Primary 注解的作用是什么

同一类型存在多个 Bean 时，标注 @Primary 的 Bean 成为**默认注入**的选择。优先级：@Qualifier 指定 > @Primary > 注解扫描的默认（类名首字母小写）。

### Q14

> Spring 中的 @Profile 注解的作用是什么

按**环境**条件化注册 Bean：@Profile("dev") 只在开发环境生效。通过 `spring.profiles.active=prod` 激活对应环境，实现 dev/test/prod 的 Bean（如数据源）差异化配置。

### Q15

> Spring 中的 @PostConstruct 和 @PreDestroy 注解的作用是什么

JSR-250 注解：@PostConstruct 在**依赖注入完成后、Bean 投入使用前**回调一次（常用于初始化缓存、预加载数据）；@PreDestroy 在**容器关闭销毁 Bean 前**回调（释放资源）。执行顺序：@PostConstruct → afterPropertiesSet → init-method。

### Q16

> Spring 中的 @RequestBody 和 @ResponseBody 注解的作用是什么

- **@RequestBody**：把请求体中的 JSON 反序列化绑定到 Java 对象（基于 HttpMessageConverter）
- **@ResponseBody**：把方法返回值序列化为 JSON 写入响应体，跳过视图解析
两者是前后端分离开发的核心注解。

### Q17

> Spring 中的 @PathVariable 注解的作用是什么

从 RESTful 风格的 URL 路径中取模板变量的值：`@GetMapping("/user/{id}") public User get(@PathVariable Long id)`。适合资源标识类参数。

### Q18

> Spring 中的 @ModelAttribute 注解的作用是什么

① 标注在方法参数上：将请求参数绑定到对象或从 Model 中取属性；② 标注在方法上：该 Controller 的每个 Handler 执行前都会先执行它，用于填充公共数据；③ @ControllerAdvice + @ModelAttribute 可全局生效。

### Q19

> Spring 中的 @ExceptionHandler 注解的作用是什么

声明一个异常处理方法，捕获指定类型的异常。作用范围：Controller 类内（局部）或 @ControllerAdvice/@RestControllerAdvice 类内（全局）。全局异常处理的标准写法是 @RestControllerAdvice + @ExceptionHandler(Exception.class) 返回统一错误结构。

### Q20

> Spring 中的 @ResponseStatus 注解的作用是什么

设置 HTTP 响应状态码和原因短语。可标注在：① 方法上，方法执行后返回该状态码；② 自定义异常类上，抛出该异常时返回对应状态码（如 @ResponseStatus(HttpStatus.NOT_FOUND) class BizException）。

### Q21

> Spring 中的 @Validated 和 @Valid 注解有什么区别

- **@Valid**：JSR-303 标准注解，支持**嵌套校验**（对象内对象也校验）
- **@Validated**：Spring 提供，支持**分组校验**（groups），可标注在类上配合方法参数校验（AOP 实现），但**不支持嵌套校验**
常配合 @NotNull/@NotBlank/@Size/@Email 等约束注解使用，校验失败抛 MethodArgumentNotValidException 由全局异常处理器统一返回。

### Q22

> Spring 中的 @Scheduled 注解的作用是什么

声明定时任务，配合 @EnableScheduling 使用。参数：cron 表达式（6 位：秒 分 时 日 月 周）、fixedRate（固定频率，上次开始时间算）、fixedDelay（固定间隔，上次结束时间算）、initialDelay（首次延迟）。默认单线程调度，多任务建议配置 TaskScheduler 线程池；集群环境需分布式锁或 xxl-job 防止重复执行。

### Q23

> Spring 中的 @Lazy 注解的作用是什么

延迟初始化：容器启动时不创建该 Bean，首次使用时才创建。用途：① 加快启动速度；② 解决构造器注入的循环依赖。也可用于 @Bean/@Component。

### Q24

> 介绍下 Spring MVC 的核心组件，以及工作原理

核心组件：**DispatcherServlet**（前端控制器）、**HandlerMapping**（处理器映射器）、**HandlerAdapter**（处理器适配器）、**Handler**（Controller）、**ViewResolver**（视图解析器）、**View**（视图）。

工作流程：请求 → DispatcherServlet → HandlerMapping 找到 Handler 及拦截器链 → HandlerAdapter 调用 Handler → 返回 ModelAndView → ViewResolver 解析视图 → View 渲染响应。前后端分离时 @ResponseBody 直接通过消息转换器写 JSON，不走视图解析。

### Q25

> Spring MVC 中如何处理表单提交

form 表单提交键值对：① 实体对象接收——Handler 方法参数为对象，属性名与表单 name 一致自动绑定；② @RequestParam 逐个接收。POST 需注意字符编码（CharacterEncodingFilter，SpringBoot 已默认配置）。若提交 JSON 则用 @RequestBody。

### Q26

> Spring MVC 中的拦截器是什么？如何定义一个拦截器

拦截器（HandlerInterceptor）是 SpringMVC 提供的 AOP 式组件，在 Handler 执行前后插入逻辑，常用于登录校验、权限、日志、性能监控。

定义步骤：① 实现 HandlerInterceptor 接口，重写 preHandle（前置，返回 false 中断）、postHandle（渲染前）、afterCompletion（完成后，清理资源）；② 注册：实现 WebMvcConfigurer，在 addInterceptors 中 addInterceptor 并配置 addPathPatterns/excludePathPatterns。

### Q27

> Spring 由哪些重要的模块组成

- **Core Container**：Core、Beans、Context、SpEL（核心容器）
- **AOP / Aspects**：面向切面
- **Data Access**：JDBC、ORM（整合 MyBatis/JPA）、Transactions（事务）
- **Web**：Web MVC、WebSocket、WebFlux
- **Test**：JUnit 集成测试支持
- **Instrumentation / Messaging** 等

### Q28

> 什么是 Spring IOC 和 DI

- **IOC（控制反转）**：对象的创建和依赖管理从程序代码反转给 Spring 容器，降低耦合，是设计思想
- **DI（依赖注入）**：IOC 的具体实现方式——容器在创建 Bean 时把它依赖的对象注入进来（构造器注入/setter 注入/字段注入）
比喻：以前自己 new 对象（自己做饭），现在容器负责创建并送上门（点外卖）。

### Q29

> Spring Bean 一共有几种作用域

6 种：**singleton**（默认，单例池缓存）、**prototype**（每次获取新建）、**request**（一次 HTTP 请求）、**session**（一次会话）、**application**（整个 ServletContext）、**websocket**（一次 WebSocket 会话）。后四种仅 web 环境有效。

### Q30

> Spring 一共有几种注入方式

三种：**构造器注入**（官方推荐：依赖不可变、非空、便于单测）、**setter 注入**（可选依赖）、**字段注入**（@Autowired 直接标字段，简洁但不利于测试和不可变性）。XML 还有 byName/byType。

### Q31

> Spring AOP 默认用的是什么动态代理，两者的区别

Spring 默认：**目标类有接口用 JDK 动态代理，无接口用 CGLIB**；SpringBoot 2.x 起默认统一 CGLIB（proxyTargetClass=true）。

区别：
- JDK 代理：基于接口，Proxy + InvocationHandler，反射调用，JDK 原生支持
- CGLIB：基于继承，字节码生成子类，MethodInterceptor 拦截，不能代理 final 类/方法；首次生成字节码较慢但调用性能高
