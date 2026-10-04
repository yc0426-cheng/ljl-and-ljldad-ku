# MyBatisPlus 八股答案

### Q1

> MyBatis 和 MyBatisPlus 的关系，MyBatisPlus 的核心理念

MP 是 MyBatis 的**增强工具**，核心理念是"**只做增强、不做改变**"：完全兼容 MyBatis 的 XML/注解 SQL 写法，同时封装了通用 CRUD、条件构造器和各类插件，解决 MyBatis 手写单表 SQL 的重复劳动。关系可理解为：MP = MyBatis + 通用 CRUD + 条件构造 + 内置插件。

### Q2

> MyBatisPlus 有哪些核心优势

① **无侵入**：兼容原生 MyBatis，可平滑引入；② **损耗小**：启动即自动注入基本 CRUD；③ 内置 **BaseMapper/IService** 免写单表 SQL；④ **Lambda 条件构造器**防字段写错；⑤ 内置**分页、乐观锁、逻辑删除**等插件；⑥ 多种**主键策略**（含雪花算法）；⑦ **代码生成器**；⑧ 官方中文文档完善。

### Q3

> MyBatisPlus 的核心组件有哪些（BaseMapper、IService、Wrapper、Plugin、SqlInjector）

- **BaseMapper**：通用 Mapper 接口，17+ 个单表 CRUD 方法
- **IService / ServiceImpl**：Service 层通用接口与实现（批量、分页、链式调用）
- **Wrapper**：条件构造器（QueryWrapper/LambdaQueryWrapper/UpdateWrapper）
- **插件体系**：MybatisPlusInterceptor + 各 InnerInterceptor（分页/乐观锁/防全表更新等）
- **SqlInjector**：SQL 注入器，启动时为 BaseMapper 方法注入预定义 SQL
- **GlobalConfig**：全局配置（主键策略、逻辑删除等）

### Q4

> SpringBoot 中如何整合 MyBatisPlus（依赖、@MapperScan、配置）

① 引入 `mybatis-plus-boot-starter`（不要再引 mybatis 依赖）；② yml 配置数据源 + `mybatis-plus.mapper-locations=classpath*:/mapper/**/*.xml`、`type-aliases-package`；③ 启动类加 `@MapperScan("com.xxx.mapper")`；④ 实体类 @TableName、Mapper 接口 extends BaseMapper<T> 即可使用。

### Q5

> BaseMapper 提供了哪些常用方法

新增：insert；删除：deleteById、deleteBatchIds、deleteByMap、delete(wrapper)；修改：updateById、update(entity, wrapper)；查询：selectById、selectBatchIds、selectOne、selectList、selectCount、selectPage、selectMaps（返回 Map 列表）、selectObjs。配合 Wrapper 参数可实现几乎所有单表操作。

### Q6

> IService 和 ServiceImpl 的作用，与 BaseMapper 的区别

- **BaseMapper**：Mapper（DAO）层，提供单条 CRUD
- **IService/ServiceImpl**：Service 层增强，提供**批量操作**（saveBatch、saveOrUpdateBatch）、saveOrUpdate、getOne、lambdaQuery() **链式查询**、remove 等复合方法；ServiceImpl<M, T> 内部持有 baseMapper
- 规范用法：Controller → Service（IService）→ Mapper（BaseMapper）

### Q7

> 条件构造器 Wrapper 有哪些实现类，各自的作用

- **QueryWrapper**：查询条件构造（select 语句的 where）
- **UpdateWrapper**：更新条件构造，额外支持 set/setSql 拼接 SET 子句
- **LambdaQueryWrapper / LambdaUpdateWrapper**：用 Lambda 方法引用代替字符串字段名，类型安全（推荐）
均继承 AbstractWrapper，通用方法：eq、ne、gt、ge、lt、le、between、like、in、isNull、orderBy、groupBy、or、and(嵌套)。

### Q8

> QueryWrapper 和 LambdaQueryWrapper 的区别

QueryWrapper 用**字符串**指定字段名：`eq("name", "张三")`——写错字段名编译不报错，数据库重构后易埋雷；LambdaQueryWrapper 用**方法引用**：`eq(User::getName, "张三")`——编译期检查、IDE 重构自动同步。因此优先使用 LambdaQueryWrapper（或 IService 的 lambdaQuery()）。

### Q9

> QueryWrapper 常用的方法有哪些

条件：eq/ne（=、!=）、gt/ge/lt/le（>、>=、<、<=）、between、in/notIn、like/likeLeft/likeRight（模糊，%位置）、isNull/isNotNull；逻辑：and（默认拼接）、or、and(w -> w.eq...嵌套括号)；排序分页：orderByAsc/orderByDesc、last("limit 1") 拼接末尾 SQL（有注入风险）；select(User::getId, User::getName) 指定查询列。

### Q10

> condition 参数的作用，如何实现条件非空才拼接

Wrapper 方法都重载了带 boolean condition 的版本，condition 为 true 才拼接该条件——实现动态 SQL 而无需 if 判断：

```java
LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
w.like(StringUtils.isNotBlank(name), User::getName, name)
 .eq(age != null, User::getAge, age);
```

name 为空时该条件直接跳过，替代 XML 里的 `<if>` 标签。

### Q11

> @TableName、@TableId、@TableField 注解的作用

- **@TableName("sys_user")**：实体类对应表名（默认驼峰转下划线匹配类名）
- **@TableId**：标注主键，type 指定主键策略（AUTO/ASSIGN_ID...）
- **@TableField**：字段级映射——value 指定列名、exist=false 排除非表字段、fill 自动填充策略、select=false 查询时忽略

### Q12

> MyBatisPlus 的主键生成策略有哪些（AUTO、ASSIGN_ID、ASSIGN_UUID、INPUT、NONE）

- **AUTO**：数据库自增（表主键需自增）
- **ASSIGN_ID**（默认）：雪花算法生成 64 位 Long
- **ASSIGN_UUID**：生成 32 位 UUID 字符串
- **INPUT**：手动输入（如业务主键）
- **NONE**：跟随全局配置 id-type
可注解 @TableId(type=...) 局部指定，或全局 `mybatis-plus.global-config.db-config.id-type`。

### Q13

> 雪花算法的原理和结构

64 位整数：1bit 符号位 + **41bit 毫秒时间戳**（约 69 年）+ **10bit 机器 ID**（1024 节点）+ **12bit 序列号**（每毫秒 4096 个，单机每秒约 400 万）。特点：分布式唯一、趋势递增（利于 InnoDB 索引）、本地生成不依赖 DB。风险：**时钟回拨**导致重复 ID（等待或抛异常处理）。

### Q14

> 实体类属性和数据库字段名不对应的解决方式

① 默认开启**驼峰转下划线**（map-underscore-to-camel-case=true）：userName ↔ user_name；② 显式 @TableField("nick_name") 指定列名；③ 表名不对应用 @TableName；④ 全局表前缀 table-prefix: t_。都不满足时（复杂映射）用 XML 自定义 SQL。

### Q15

> @TableField(exist = false) 的作用，哪些场景会用到

声明该属性**不是数据库表字段**，生成 CRUD SQL 时忽略它。场景：接收关联查询/统计结果的冗余属性（如 deptName、tagNameList）、实体内的临时计算字段。不加的话 MP 会把它当表字段拼进 SQL，导致"Unknown column"错误。

### Q16

> 什么是逻辑删除，如何实现（@TableLogic、全局配置 logic-delete-field）

逻辑删除 = 删除操作变更为 update 标记位，数据保留可恢复、可审计。

实现：① 表加 deleted 字段；② 实体字段标 @TableLogic，或全局配置 `mybatis-plus.global-config.db-config.logic-delete-field: deleted`（+ logic-delete-value: 1 / logic-not-delete-value: 0）。之后调用 deleteById 自动变 update，所有 select 自动追加 deleted=0 过滤。

### Q17

> 逻辑删除后查询和删除操作的底层 SQL 变化

- deleteById(1) → `UPDATE user SET deleted=1 WHERE id=1 AND deleted=0`
- selectList(null) → `SELECT ... FROM user WHERE deleted=0`（自动追加）
- updateById 同样自动拼 deleted=0

注意：逻辑删除与**唯一索引冲突**——被删记录仍占唯一键，同值无法再次插入，可用删除时间戳或"deleted + 主键"组合做删除值。

### Q18

> 什么是自动填充，如何实现 create_time 和 update_time 的自动填充（MetaObjectHandler）

自动填充：insert/update 时框架自动为指定字段赋值，避免业务代码重复写。

实现：① 实体字段标注 `@TableField(fill = FieldFill.INSERT)` 或 `INSERT_UPDATE`；② 编写配置类实现 **MetaObjectHandler** 接口：

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    public void insertFill(MetaObject metaObject) {
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
    }
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

### Q19

> 乐观锁插件 @Version 的原理和使用步骤

原理：更新时自动追加版本号条件 `UPDATE ... SET version=新 WHERE id=? AND version=旧`，影响行数为 0 说明被并发修改，业务层重试。

步骤：① 表加 version 字段，实体加 @Version；② 注册 OptimisticLockerInnerInterceptor：

```java
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor i = new MybatisPlusInterceptor();
    i.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
    return i;
}
```

③ updateById 前实体必须先查询携带旧 version；仅 updateById/update(entity, wrapper) 生效。

### Q20

> 乐观锁和悲观锁的区别，分别适用什么场景

- **乐观锁**：假设无冲突，提交时校验（版本号/CAS），失败重试；不会阻塞，**读多写少**场景（如商品详情、配置更新）
- **悲观锁**：操作前先加锁（select for update、分布式锁），其他事务阻塞等待；数据安全稳定，**写多冲突高**场景（如库存扣减、账户余额）

### Q21

> 分页插件如何配置（MybatisPlusInterceptor + PaginationInnerInterceptor）

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

使用：`Page<User> page = userMapper.selectPage(new Page<>(current, size), wrapper);` 多插件时分页放**最后**添加。

### Q22

> 分页插件的原理，物理分页和逻辑分页的区别

- **物理分页**：数据库层面 `LIMIT offset, size`，只查当前页数据，性能好——MP 分页插件即此：拦截查询 → 先执行 count 语句查总数 → 按数据库方言改写 SQL 拼接 LIMIT → 封装 Page
- **逻辑分页**：一次查出全部数据到内存，再截取（RowBounds），数据量大时内存和带宽浪费严重
MP 分页插件本质是 MyBatis 的 Interceptor 对 StatementHandler 的增强。

### Q23

> 分页查询 Page 对象中常用的属性（records、total、pages、current、size）

records：当前页数据 List；total：总记录数；pages：总页数（total/size 向上取整）；current：当前页码；size：每页条数；另有 hasPrevious()/hasNext()。前端分页组件需要 total + records + current + size 四要素。

### Q24

> 多个内置插件同时配置时的顺序要求

多个 InnerInterceptor 按添加顺序执行，官方要求**分页插件必须放最后添加**，否则分页 count 或 SQL 改写会被后续插件影响出错。常见顺序：多租户插件 → 动态表名 → 乐观锁 → 防全表更新 → 分页（最后）。

### Q25

> MyBatisPlus 如何打印 SQL 日志和执行耗时

开发环境：yml 配置

```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

生产环境用 **p6spy**（替换驱动 com.p6spy.engine.spy.P6SpyDriver，输出真实 SQL 和耗时）或 Druid 的 SQL 监控；切勿在生产开 StdOutImpl（大量 IO 影响性能）。

### Q26

> MyBatisPlus 如何实现多表查询（自定义 SQL + XML、连表 Wrapper）

MP 只做**单表增强**，多表查询回归 MyBatis 原生方式：① Mapper 接口自定义方法 + XML 写 JOIN SQL，结果映射到 VO；② @Select 注解写 SQL；③ 简单场景用 selectMaps + 手写 SQL。也可对主表用 Wrapper 查出后再根据外键**批量查询组装**（避免 N+1 循环查库）。复杂报表建议直接 XML 或走数据库视图。

### Q27

> 自定义 SQL 如何与 Wrapper 条件构造器结合使用

Mapper 方法参数加 `@Param(Constants.WRAPPER) Wrapper<T> wrapper`，XML 中用 `${ew.customSqlSegment}` 拼接 where 条件：

```java
List<User> selectByWrapper(@Param(Constants.WRAPPER) Wrapper<User> wrapper);
```

```xml
<select id="selectByWrapper" resultType="User">
    SELECT * FROM user ${ew.customSqlSegment}
</select>
```

调用方构造 LambdaQueryWrapper 传入即可，兼顾自定义 SQL 和动态条件。

### Q28

> saveBatch 批量插入的原理，如何优化批量插入性能（rewriteBatchedStatements）

saveBatch 默认走 JDBC **batch**（executeBatch），每 1000 条 flush 一次，但 MySQL 驱动默认仍逐条发送。优化：① JDBC URL 加 `rewriteBatchedStatements=true`，驱动把批量请求改写为一条多值 INSERT 一次网络传输，性能提升一个数量级；② 也可自定义 SQL 用 `<foreach>` 拼 `insert into t values (...),(...)`；③ 超大数据量分批 + 关闭事务自动提交。

### Q29

> selectOne 查询出多条数据会发生什么，如何处理

抛出 **TooManyResultsException**。处理方式：① 确保查询条件唯一；② 使用 `wrapper.last("limit 1")` 只取一条；③ 用 selectList 查询后自行判断处理；④ Service 层 getOne(wrapper, false) 不抛异常只打警告取第一条（不推荐滥用）。

### Q30

> 实体类中为什么建议使用包装类型（Integer）而不是 int

① 数据库字段可为 NULL，int 无法表示 null（映射报错或默认 0 混淆"没值"和"值为 0"）；② 包装类型能区分"未赋值"和"有值 0"，配合动态 SQL 判空（age != null）；③ 序列化/前端交互时 null 与 0 语义不同。同理金额用 BigDecimal。

### Q31

> MyBatis 中 #{} 和 ${} 的区别，SQL 注入如何防范

- **#{}**：预编译占位符 `?`，值经 PreparedStatement 参数设置，自动转义——**防 SQL 注入**，绝大多数场景使用
- **${}**：字符串**直接拼接**进 SQL，有注入风险，仅用于无法参数化的部分：动态表名、动态列名、ORDER BY 字段
防范：值一律 #{}；必须 ${} 时对入参做**白名单校验**；最小权限数据库账号。

### Q32

> MyBatis 的一级缓存和二级缓存

- **一级缓存**：SqlSession 级别，默认开启；同一 session 内相同查询直接取缓存，执行增删改或 clearCache 后清空。Spring 整合下每次 Mapper 调用是新 session，作用有限
- **二级缓存**：namespace（Mapper）级别，需开启 `<cache/>`，跨 session 共享，事务提交后才写入；多表关联时易产生**脏读**，分布式环境更明显
生产实践：分布式系统一般不用 MyBatis 缓存，统一用 Redis 做应用级缓存。

### Q33

> MyBatisPlus 的代码生成器如何使用，能生成哪些代码

引入 mybatis-plus-generator + 模板引擎（Freemarker/Velocity），使用 FastAutoGenerator 配置：① 数据源；② 全局策略（输出目录、作者）；③ 包名策略；④ 表匹配和字段策略（主键类型、命名、逻辑删除字段）。可生成 **Entity、Mapper（接口+XML）、Service（接口+实现）、Controller** 全套分层代码，配 Lombok 注解，大幅减少项目初始化工作量。

### Q34

> ActiveRecord 模式是什么

AR 模式：实体类继承 `Model<T>` 后自身即具备持久化能力，直接 `user.insert()`、`user.deleteById()`、`user.updateById()` 操作，无需注入 Mapper。本质内部仍调用 Mapper，只是简化调用形式；代价是实体类与持久层耦合，一般项目用 BaseMapper + Service 即可。

### Q35

> MyBatisPlus 如何实现自动的 CRUD（SQL 注入器 AbstractSqlInjector 的原理）

MP 启动时通过 **AbstractSqlInjector** 反射解析 Mapper 接口继承的 BaseMapper 泛型，为 insert、selectById 等**内置方法生成 MappedStatement**（预定义的 SQL 模板，如 `INSERT INTO %s (字段...) VALUES (#{...})`，字段来自 TableInfo 反射实体），注册进 MyBatis Configuration——所以不写 XML 就有 CRUD 能力。可继承 AbstractMethod + 自定义 Injector 扩展全局通用方法。

### Q36

> MyBatisPlus 常用的全局配置有哪些（id-type、table-prefix、logic-delete）

```yaml
mybatis-plus:
  global-config:
    banner: false
    db-config:
      id-type: assign_id          # 全局主键策略
      table-prefix: t_            # 全局表名前缀
      logic-delete-field: deleted # 全局逻辑删除字段
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### Q37

> MyBatisPlus 如何处理枚举类型字段（@EnumValue、IEnum）

两种方式：① 枚举中表示数据库值的字段标 **@EnumValue**（如 Gender 枚举的 code 字段），存取时自动在枚举与 code 间转换；② 实现 **IEnum<T>** 接口重写 getValue。需配置扫描枚举包 `mybatis-plus.type-enums-package`（或通用枚举包扫描）。存取 SQL 中自动用指定值代替枚举名。

### Q38

> MyBatisPlus 如何实现数据权限控制（InnerInterceptor 自定义拦截器）

MP 的 InnerInterceptor 可拦截 SQL 改写。数据权限：自定义 InnerInterceptor，在 beforeQuery 中解析 SQL（JSqlParser），按当前登录人的部门/角色**追加 where 条件**（如 dept_id IN (...)）；多租户同理，官方提供 **TenantLineInnerInterceptor** 自动为所有 SQL 拼 tenant_id 条件（含增删改）。也可简化为 service 层统一在 Wrapper 中拼权限条件。

### Q39

> MyBatisPlus 的优缺点，什么场景下不适合使用

优点：单表 CRUD 效率极高、学习成本低、插件生态全、中文文档友好。
缺点：**多表 JOIN 支持弱**（仍需写 SQL）；过度依赖 Wrapper 容易写出难维护的动态 SQL；复杂查询/报表场景没有优势；抽象层多一层，极端性能场景多一层封装。

不适合：以复杂多表关联、报表统计为主的老系统；对 SQL 完全可控性要求极高的场景（直接用 MyBatis）。

### Q40

> MyBatis 动态 SQL 有哪些标签（if、where、foreach、choose、set、trim）

- `<if test="...">`：条件成立才拼接
- `<where>`：包裹条件，智能去掉开头多余的 AND/OR
- `<set>`：update 语句智能去末尾逗号
- `<foreach collection item open close separator>`：遍历集合，用于 in 查询和批量插入
- `<choose><when><otherwise>`：多分支（相当于 switch）
- `<trim prefix suffix suffixOverrides>`：自定义前后缀与裁剪规则（where/set 的底层）
- `<sql id>` + `<include refid>`：SQL 片段复用
