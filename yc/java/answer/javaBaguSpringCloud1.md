# SpringCloud 八股答案

### Q1

> 什么是微服务，微服务架构有哪些优缺点

微服务是把单体应用按业务边界拆分为一组**独立部署、独立数据库、通过轻量通信（HTTP/RPC/消息）协作**的小型服务。

优点：独立开发部署、按需扩缩容、技术栈异构、故障隔离、团队自治。
缺点：分布式复杂性（网络调用、分布式事务、链路排查难）、运维成本高、数据一致性难保证、服务拆分粒度难把握。

### Q2

> SpringCloud 和 SpringBoot 的关系与区别

- SpringBoot：快速**开发单个微服务**的框架（自动装配、内嵌服务器、starter）
- SpringCloud：基于 SpringBoot 的**微服务治理全家桶**（注册发现、配置中心、负载均衡、网关、熔断限流），关注服务之间的协调治理
- 版本必须配套（Spring Cloud 2023.x 对应 SpringBoot 3.2.x）

比喻：SpringBoot 是造车工厂，SpringCloud 是交通管理系统。

### Q3

> SpringCloud 的核心组件有哪些（Netflix 第一代 和 Alibaba 第二代体系对比）

| 功能 | Netflix（一代） | Alibaba（二代，国内主流） |
|---|---|---|
| 注册中心 | Eureka | **Nacos** |
| 配置中心 | Config | **Nacos Config** |
| 负载均衡 | Ribbon | Spring Cloud **LoadBalancer** |
| 服务调用 | Feign | **OpenFeign** |
| 熔断降级 | Hystrix | **Sentinel** |
| API 网关 | Zuul | **Gateway** |
| 分布式事务 | — | **Seata** |

### Q4

> Netflix 体系为什么停更，国内主流为什么是 Spring Cloud Alibaba

Netflix 2018 年宣布大部分组件进入维护模式（只修 bug 不加功能）；Ribbon/Hystrix/Zuul 等设计较老。而 Spring Cloud Alibaba 组件持续活跃迭代：Nacos 注册+配置二合一、Sentinel 控制台实时配规则、Seata 补齐分布式事务短板，且经过阿里双十一海量流量验证、与阿里云生态整合好，因此成为国内主流。

### Q5

> 什么是服务注册与发现，其工作原理是什么

解决服务实例地址动态变化下的调用问题。原理：
1. 服务提供者启动时向注册中心**注册**自己的服务名、IP、端口
2. 注册中心通过**心跳/探活**维护健康实例列表
3. 消费者**订阅**服务列表并缓存到本地
4. 调用时通过**负载均衡**从列表中选一个实例发起请求
5. 实例下线或宕机后被剔除，列表变更推送给消费者

### Q6

> Eureka 和 Nacos 的区别（CAP、心跳、临时实例与持久实例）

- **CAP**：Eureka 保证 AP（自保护模式：大量实例失联时宁可保留旧列表也不剔除，保证可用性）；Nacos 同时支持——**临时实例走 AP（Distro 协议），持久实例走 CP（Raft 协议）**
- **健康检查**：Eureka 靠客户端心跳；Nacos 临时实例心跳上报，持久实例服务端主动探测
- **功能**：Nacos 额外是配置中心；Eureka 2.x 已闭源停更
- **感知速度**：Nacos 支持 UDP/gRPC 推送，变更感知更快

### Q7

> Nacos 作为注册中心的原理，心跳机制是怎样的

临时实例：客户端每 **5 秒**发一次心跳；**15 秒**未收到标记为不健康；**30 秒**未收到剔除实例。1.x 通过 HTTP 定时拉取（默认每 10s？实为客户端每 10 秒拉取+UDP 推送），2.x 升级为 **gRPC 长连接**，服务端变更主动推送，消费者本地缓存服务列表保证注册中心故障时仍可调用。Nacos 集群间通过 Distro 协议同步数据（AP）。

### Q8

> Nacos 作为配置中心如何实现配置的动态刷新

1.x 客户端对配置项做**长轮询**（客户端发起请求，服务端配置无变更则挂起最多 30 秒，有变更立即返回新配置的 MD5，客户端再拉取），兼顾实时性和性能；2.x 改用 gRPC 长连接推送。客户端收到变更后发布 **RefreshEvent**，@RefreshScope 标注的 Bean 被销毁重建、@ConfigurationProperties 属性重新绑定，实现不重启刷新配置。

### Q9

> Nacos 中 Namespace、Group、Data ID 的作用和配置隔离

- **Data ID**：具体配置文件标识，如 user-service-dev.yaml
- **Group**：配置分组，默认 DEFAULT_GROUP，可按项目/业务线划分
- **Namespace**：最外层隔离，常用于**环境隔离**（dev/test/prod）或多租户

优先级：Namespace > Group > Data ID，三层组合实现多环境多项目的配置隔离。

### Q10

> 服务注册中心宕机后，服务之间还能正常调用吗

**能**。因为消费者拉取服务列表后会**缓存到本地内存**，调用时直接从本地缓存负载均衡选择实例，不依赖注册中心实时在线。影响是：新服务实例上下线无法感知，已缓存列表中的实例宕机后可能调用失败（需靠重试/熔断兜底）。

### Q11

> 负载均衡的原理，Ribbon 和 Spring Cloud LoadBalancer 的区别

负载均衡是把请求分摊到多个服务实例上。Feign 调用时由负载均衡器从注册中心拉取实例列表，按策略选一个发起请求（客户端负载均衡，发生在调用方）。

区别：Ribbon 是 Netflix 组件、策略丰富但**已停更**；LoadBalancer 是 Spring 官方替代品，轻量、支持响应式（配合 Gateway），默认轮询，可自定义 RandomLoadBalancer。

### Q12

> Ribbon 有哪些常见的负载均衡策略（轮询、随机、加权等）

1. **RoundRobinRule**：轮询（默认）
2. RandomRule：随机
3. WeightedResponseTimeRule：按响应时间加权轮询（响应快的权重高）
4. RetryRule：轮询失败后重试其他实例
5. BestAvailableRule：选并发连接数最小的实例
6. AvailabilityFilteringRule：过滤掉故障和高并发实例再轮询
7. ZoneAvoidanceRule：综合区域和可用性（默认复合策略）

### Q13

> Feign 和 OpenFeign 的区别，Feign 的工作原理

Feign 是 Netflix 的声明式 HTTP 客户端（已停更）；OpenFeign 是 Spring Cloud 在其基础上的延续和增强（支持 SpringMVC 注解、更多扩展）。

工作原理：① @EnableFeignClients 扫描 @FeignClient 接口；② **JDK 动态代理**生成接口实现；③ 调用方法时被代理拦截，将方法信息编码为 HTTP 请求模板（URL、方法、参数）；④ 经负载均衡器选实例后发送请求；⑤ 响应解码为方法返回类型。让远程调用写起来像调本地方法。

### Q14

> Feign 如何实现服务间调用，如何配置超时和重试

① 引入 openfeign 依赖 + @EnableFeignClients；② 定义接口 `@FeignClient(name = "user-service")`，方法用 @GetMapping 等声明；③ 直接注入接口调用。

超时：`spring.cloud.openfeign.client.config.default.connect-timeout/read-timeout`（可按服务名单独配置）；
重试：默认 **Retryer.NEVER_RETRY**，可注入自定义 RetryerBean（maxAttempts、period、maxPeriod），注意接口必须幂等，避免与超时叠加导致重复提交。

### Q15

> Feign 底层默认使用什么 HTTP 客户端，如何替换

默认使用 JDK 自带的 HttpURLConnection，**没有连接池**，高并发下性能差。推荐替换：
- Apache HttpClient：引入 feign-httpclient，配置 feign.httpclient.enabled=true（默认自动开启若有依赖）
- OkHttp：feign.okhttp.enabled=true
两者都支持连接池复用，显著提升吞吐。

### Q16

> 服务熔断、服务降级、服务限流的区别

- **服务降级**：系统压力大或调用失败时，返回兜底数据/友好提示，放弃非核心逻辑（保核心、弃边缘）
- **服务熔断**：下游服务故障率超阈值时"跳闸"，一段时间内直接快速失败不再发起调用，防止故障蔓延；半开状态探测恢复后自动闭合
- **服务限流**：限制单位时间内的请求量（QPS/并发线程数），超出部分拒绝、排队或降级，保护系统自身不被打垮

关系：限流是预防，熔断是止损，降级是兜底手段，三者常配合使用。

### Q17

> 什么是服务雪崩，如何避免

服务雪崩：下游某个服务不可用 → 调用方的请求线程持续堆积阻塞 → 耗尽线程池 → 调用方也瘫痪 → 沿调用链向上蔓延，最终整个系统崩溃。

避免手段：① 熔断器快速失败；② 限流控制入口流量；③ 合理设置调用**超时**；④ 舱壁隔离（为每个依赖单独的线程池/信号量）；⑤ 缓存和降级兜底。

### Q18

> 熔断器的三种状态（关闭、打开、半开）及转换过程

- **Closed（关闭）**：正常放行请求，同时统计滑动窗口内的失败率
- **Open（打开）**：失败率超过阈值（如 50%）后熔断打开，所有请求直接走降级逻辑，持续一个熔断时长窗口
- **Half-Open（半开）**：窗口到期后进入半开，放行少量探测请求——成功则回到 Closed 恢复正常，失败则重新 Open

### Q19

> Hystrix 和 Sentinel 的区别

| | Hystrix（停更） | Sentinel（主流） |
|---|---|---|
| 隔离策略 | 线程池隔离为主 | 信号量（并发线程数） |
| 熔断策略 | 异常比例 | 异常比例/异常数/慢调用比例 |
| 限流 | 不支持 | 基于 QPS/并发，支持流控效果（预热、排队） |
| 热点/系统保护 | 无 | 有 |
| 控制台 | 简单监控 | 功能强，**实时改规则** |
| 规则持久化 | 不支持 | 支持对接 Nacos 等数据源 |

### Q20

> Sentinel 的限流规则有哪些（QPS、并发线程数、流控效果）

限流阈值类型：**QPS**（每秒请求数）、**并发线程数**。
流控效果：
- **直接失败**：超出阈值立即抛 FlowException（快速失败）
- **Warm Up 预热**：冷启动，阈值从 阈值/codeFactor 缓慢提升到设定值，应对突发流量
- **排队等待**：漏桶算法，请求匀速通过，超时的被丢弃
流控模式：直接、关联（关联资源达到阈值时限制自己）、链路（只限制某条调用链路）。

### Q21

> Sentinel 的热点参数限流和系统自适应保护是什么

- **热点参数限流**：统计参数值级别的 QPS，对访问异常频繁的特定参数值单独限流（如商品 ID=1 的请求限 100 QPS），支持参数例外项
- **系统自适应保护**：从整个系统维度入口限流，根据 Load、CPU 使用率、平均 RT、入口 QPS、线程数动态判断，接近饱和时自动降低流量，防止系统被压垮

### Q22

> Sentinel 的规则如何持久化

Sentinel 控制台默认把规则推到客户端**内存**，应用重启即丢失。持久化方案：实现 ReadableDataSource 对接外部数据源（**Nacos**/Apollo/Zookeeper，push 模式：规则写入 Nacos，客户端监听变更实时生效），生产推荐 Sentinel + Nacos 的规则持久化。

### Q23

> 服务网关的作用是什么（统一入口、鉴权、限流、路由转发)

网关是微服务系统的**统一入口**：
1. **动态路由**：按规则转发到对应微服务，屏蔽内部结构
2. **统一鉴权**：在网关校验 token，下游服务免重复认证
3. **限流熔断**：入口流量保护
4. **日志监控、灰度发布、跨域处理、协议转换**
好处：客户端只需知道网关地址；安全逻辑收敛到一处。

### Q24

> Spring Cloud Gateway 的三大核心概念（Route、Predicate、Filter）

- **Route 路由**：转发的最小单元 = ID + 目标 URI + 断言集合 + 过滤器集合，断言全部匹配才转发
- **Predicate 断言**：匹配条件，如 Path=/api/user/**、Method=GET、Header、Query、时间断言
- **Filter 过滤器**：在请求转发前后处理（修改请求头/参数、鉴权、限流、日志），分 GatewayFilter（针对路由）和 GlobalFilter（全局）

### Q25

> Gateway 和 Zuul 的区别（阻塞 IO 与响应式 Netty）

- **Zuul 1.x**：基于 Servlet 的同步阻塞 IO，一个请求占一个线程，性能一般；Zuul 2.x 改异步但社区生态差
- **Gateway**：基于 **Spring WebFlux + Netty** 的响应式非阻塞模型，少量线程处理高并发，性能好；官方主推，与 Spring 生态无缝整合（配合 LoadBalancer、Nacos）
新项目一律选 Gateway。

### Q26

> Gateway 路由配置中 lb:// 前缀的含义

`uri: lb://user-service` 表示目标地址不是写死的 http URL，而是**按服务名从注册中心获取实例列表，由 Spring Cloud LoadBalancer 做客户端负载均衡**后转发（lb = load balance），实现服务发现的动态路由。

### Q27

> Gateway 的 GlobalFilter 和 GatewayFilter 的区别，如何自定义全局过滤器

- **GatewayFilter**：针对**特定路由**生效，随路由配置（yml 或代码）声明
- **GlobalFilter**：对**所有路由**生效，如鉴权、日志；与 Ordered 接口配合决定执行顺序

自定义全局过滤器：实现 GlobalFilter + Ordered 接口，在 filter() 中处理请求（校验 token、记录日志），chain.filter(exchange) 放行，注册为 @Component 即可。

### Q28

> 如何在网关层统一做登录鉴权（JWT + 过滤器）

流程：① 登录服务校验账号密码，签发 JWT 返回；② 客户端后续请求在 Header 携带 token；③ 网关自定义 GlobalFilter 拦截：白名单（/login、文档）放行 → 校验 token 有效性和过期时间 → 无效直接返回 401；④ 有效则解析用户信息写入请求头（x-user-id）转发给下游，下游服务直接读 header 信任该信息。

### Q29

> Spring Cloud Config 和 Nacos Config 的区别

- **Config**：配置存储在 Git/SVN，需配合 Spring Cloud Bus（消息总线，MQ 广播）才能刷新，架构重、实时性一般
- **Nacos Config**：自带存储和控制台，长轮询/gRPC 推送实时刷新，@RefreshScope 生效，注册中心+配置中心一体化，部署简单
国内绝大多数项目选 Nacos。

### Q30

> 分布式事务的解决方案有哪些（2PC、TCC、最终一致性、Seata）

1. **2PC/XA**：两阶段提交（准备+提交），强一致但同步阻塞、性能差，适合数据库层面
2. **TCC**：Try（预留资源）-Confirm（确认）-Cancel（取消），业务侵入大，一致性较好，适合金融核心场景
3. **本地消息表 / 事务消息（RocketMQ）**：利用本地事务+消息重试达到**最终一致性**，互联网主流
4. **Seata AT 模式**：对业务无侵入，自动生成回滚日志，最常用
5. **SAGA**：长事务，正向服务+反向补偿

### Q31

> Seata 的 AT 模式原理，什么是全局锁和 undo_log

AT（Auto Transaction）模式：
- **一阶段**：Seata 代理数据源，解析业务 SQL，执行前后查询数据快照保存到 **undo_log** 表（before/after image），随后**本地事务直接提交**并注册分支事务上报 TC
- **二阶段**：全局提交 → 异步删除 undo_log；全局回滚 → 根据 undo_log 的反向 SQL 补偿恢复数据
- **全局锁**：TC 维护的行级锁，防止另一全局事务在回滚期间修改同一行造成**脏写**（本地事务提交快，但更新同一行需先拿全局锁）

### Q32

> CAP 理论和 BASE 理论是什么

**CAP**：分布式系统三大特性——一致性（Consistency）、可用性（Availability）、分区容错（Partition tolerance）不可兼得；网络分区（P）必然存在，所以实际只能在 CP 和 AP 间取舍（如 ZooKeeper CP、Eureka AP、Nacos 可切换）。

**BASE**：对 AP 的延伸——基本可用（Basically Available）、软状态（Soft state，允许中间态）、最终一致性（Eventually consistent）。互联网高可用系统的主流设计思想。

### Q33

> 分布式锁有哪些实现方式（Redis、Zookeeper、数据库）

1. **Redis**：SET key value NX PX 过期时间；推荐 **Redisson**（可重入、看门狗自动续期、Lua 原子操作）；注意锁误删和主从切换丢锁问题（RedLock）
2. **ZooKeeper**：临时顺序节点 + watch 前一个节点，客户端断连锁自动释放，可靠性强但性能低于 Redis
3. **数据库**：唯一索引插入或悲观锁 select for update，实现简单性能差，低频场景可用

选型：性能优先 Redis（Redisson），强一致优先 ZooKeeper。

### Q34

> 微服务之间如何传递用户登录信息（token、请求头、ThreadLocal）

典型链路：网关校验 JWT → 解析出用户 ID → 放入请求头转发 → 下游服务拦截器读取请求头 → 存入 **ThreadLocal**（封装 UserContext 工具类）→ 业务代码任意位置取当前用户。注意：① 服务间用 Feign 调用时需要 **RequestInterceptor 把用户信息 Header 透传**下去；② 异步/线程池场景 ThreadLocal 失效，用 InheritableThreadLocal 或 TransmittableThreadLocal，用完 remove 防泄漏。

### Q35

> 分布式 ID 生成方案有哪些（UUID、雪花算法、数据库号段）

1. **UUID**：简单无依赖，但无序（索引页分裂）、太长，不适合做主键
2. **数据库自增/步长**：多库设置不同步长，扩展性差
3. **号段模式**：一次从 DB 取一段缓存在内存用完再取（美团 Leaf）
4. **雪花算法**：64 位时间有序 ID，性能好趋势递增（默认主流）
5. 开源方案：Leaf、百度 UidGenerator、Redis INCR

### Q36

> 雪花算法的结构和工作原理，时钟回拨问题如何解决

64 位结构：1bit 符号位（0）+ **41bit 时间戳**（毫秒级，可用约 69 年）+ **10bit 机器 ID**（支持 1024 节点）+ **12bit 序列号**（每毫秒可生成 4096 个）。

特点：趋势递增、不依赖数据库、单机每秒 400 万+。

**时钟回拨**问题（NTP 校时导致时间倒流，可能生成重复 ID）：回拨小则自旋等待追上；回拨大则抛异常拒绝服务，或使用扩展位记录回拨次数/备用机器 ID（美团 Leaf 的解决方案）。

### Q37

> 微服务的链路追踪方案（Sleuth、ZipKin、SkyWalking）

链路追踪通过全局 **traceId** 贯穿一次请求经过的所有服务，记录 span（每个环节耗时），用于排查慢接口和故障定位。

- **Sleuth + ZipKin**：Spring Cloud 原生，Sleuth 埋点生成 traceId（自动注入日志），ZipKin 收集展示调用链
- **SkyWalking**：Java Agent **无侵入**埋点、性能损耗小、自带拓扑图和告警，国内主流
- 新标准：OpenTelemetry

### Q38

> 微服务拆分的原则和注意事项

原则：① **单一职责、高内聚低耦合**；② 按业务领域拆分（DDD 限界上下文），而不是按技术层拆；③ **每个服务独立数据库**，禁止跨库直连；④ 接口稳定、服务间通过 API/事件通信；⑤ 粒度适中，避免拆得过细导致分布式代价大于收益；⑥ 团队结构与架构匹配（康威定律）。

### Q39

> 微服务接口幂等性如何保证

1. **唯一索引/防重表**：业务唯一键入库兜底
2. **Token 机制**：预取 token，提交时原子校验删除（Redis SETNX）
3. **分布式锁**：按唯一业务键加锁串行处理
4. **状态机**：更新语句带前置状态条件（update ... where status='待处理'）
5. **乐观锁**：version 版本号控制
6. 前端按钮防重 + 网关去重只是辅助，**后端幂等才是根本**

### Q40

> 微服务灰度发布是如何实现的

灰度发布：新版本先对部分流量开放验证，稳定后全量。

实现：① 服务实例打**元数据标签**（version=gray）注册到 Nacos；② 网关按请求特征（Header、用户 ID、Cookie 或权重 Weight 断言）将灰度流量路由到新版本实例；③ 全链路灰度需要在网关生成灰度标签，通过请求头+Feign 拦截器**透传标签**，配合自定义负载均衡器选择同标签实例；④ 结合 Sentinel 监控指标，验证通过后逐步扩大流量。
