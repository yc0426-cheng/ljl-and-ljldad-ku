# Java基础 八股答案（2）

### Q1

> == 和 equals 的区别

`==`：基本类型比较值，引用类型比较内存地址。
`equals`：Object 中默认等同 `==`；String、Integer 等重写后比较内容。
规律：`a == b` 为 true 则 `a.equals(b)` 一定为 true，反之不成立。

### Q2

> 为什么重写 equals 必须重写 hashCode

Java 约定：equals 相等的两个对象 hashCode 必须相等。如果不重写 hashCode，两个"相等"的对象会算出不同的 hash 值，在 HashMap/HashSet 中会被放到不同的桶里，导致去重失效、contains 找不到。

### Q3

> String、StringBuilder、StringBuffer 的区别

- **String**：不可变字符序列，每次拼接都产生新对象
- **StringBuilder**：可变，非线程安全，性能最好，单线程拼接首选
- **StringBuffer**：可变，方法加了 synchronized，线程安全，性能略低

循环拼接字符串必须用 StringBuilder，避免产生大量中间对象。

### Q4

> String 为什么设计成不可变的

1. **字符串常量池复用**：相同字面量共享一个对象，节省内存
2. **安全性**：String 常作为文件路径、类名、数据库连接参数，不可变防止被篡改
3. **线程安全**：不可变对象天然线程安全，可自由共享
4. **hashCode 可缓存**：hash 只算一次，适合做 HashMap 的 key

底层实现：JDK9 前是 `final char[]`，之后改为 `final byte[]`，类本身也是 final 不可继承。

### Q5

> Java 中的基本数据类型有哪些，各占多少字节

byte(1)、short(2)、int(4)、long(8)、float(4)、double(8)、char(2)、boolean（规范未明确大小，单个时约1字节）。

### Q6

> 自动装箱和拆箱是什么，Integer 缓存机制（-128~127）

装箱：基本类型自动转为包装类，调用 `Integer.valueOf()`；拆箱：调用 `intValue()`。
Integer 内部有 IntegerCache 缓存了 -128~127 的对象，`Integer a = 127, b = 127` 时 `a == b` 为 true；128 时是两个新对象，`==` 为 false。所以包装类型比较必须用 equals。注意 `==` 一边是基本类型时会自动拆箱再比较。

### Q7

> 面向对象的三大特性及各自的含义

- **封装**：隐藏内部实现细节，通过 getter/setter 和访问修饰符控制访问
- **继承**：子类复用父类的属性和方法（extends），Java 单继承
- **多态**：父类引用指向子类对象，编译看左边（类型），运行看右边（实际对象的方法实现）；前提是继承/实现 + 重写 + 父类引用指向子类对象

### Q8

> 重载和重写的区别

- **重载（Overload）**：同一个类中方法名相同、参数列表不同（类型/个数/顺序），与返回值无关，编译期确定
- **重写（Override）**：子类重新实现父类方法，方法签名必须相同；访问权限不能比父类更小，抛出的异常不能比父类更大，运行期确定

### Q9

> 抽象类和接口的区别

- 抽象类**单继承**，接口可以**多实现**
- 抽象类可以有构造方法、普通成员变量、具体方法；接口的变量默认 `public static final`，方法默认 `public abstract`（Java8 后可以有 default、static 方法）
- 抽象类是"is-a"，提取同类事物的共性；接口是"can-do"，定义行为规范
- 设计上：抽象类是对类本质的抽象，接口是对行为的抽象

### Q10

> Java 中的异常体系，checked 和 unchecked 异常的区别

Throwable 分为 Error 和 Exception：
- **Error**：严重错误，程序无法处理，如 OutOfMemoryError、StackOverflowError
- **Exception**：分为 checked（受检）和 unchecked（非受检）
  - checked：编译期强制 try-catch 或 throws，如 IOException、SQLException
  - unchecked：RuntimeException 及其子类，如 NullPointerException、ArrayIndexOutOfBoundsException、ClassCastException，编译器不强制处理

### Q11

> final、finally、finalize 的区别

- **final**：修饰类（不能被继承）、方法（不能被重写）、变量（基本类型值不可变，引用类型指向不可变）
- **finally**：try-catch 后的代码块，无论是否异常都执行（System.exit() 或 JVM 崩溃除外），用于释放资源
- **finalize**：Object 的方法，对象被 GC 回收前调用，JDK9 已废弃，用 try-with-resources 或 Cleaner 替代

### Q12

> 深拷贝和浅拷贝的区别

- **浅拷贝**：只复制对象本身和基本类型字段，引用类型字段仍指向同一个对象，修改会影响原对象。实现：实现 Cloneable 重写 clone()
- **深拷贝**：递归复制所有引用对象，两个对象完全独立。实现：所有引用对象都重写 clone 逐层复制，或序列化/反序列化（推荐），或手动 new 复制

### Q13

> Java 集合框架的结构，List、Set、Map 的区别

- **List**：有序、可重复。ArrayList（数组）、LinkedList（链表）、Vector（线程安全，过时）
- **Set**：不可重复。HashSet（哈希表，无序）、LinkedHashSet（维护插入顺序）、TreeSet（红黑树，排序）
- **Map**：键值对，key 不重复。HashMap、LinkedHashMap、TreeMap、Hashtable、ConcurrentHashMap

### Q14

> ArrayList 和 LinkedList 的区别

- 底层：ArrayList 是动态数组，LinkedList 是双向链表
- 随机访问：ArrayList 支持 O(1) 下标访问，LinkedList 需从头遍历 O(n)
- 插入删除：ArrayList 尾部均摊 O(1)，中间需移动元素 O(n)；LinkedList 定位到位置后改指针 O(1)（但定位本身 O(n)）
- 内存：ArrayList 连续空间、缓存友好；LinkedList 每个节点额外存前后指针
- 实际开发绝大多数场景 ArrayList 更优，LinkedList 很少用

### Q15

> ArrayList 扩容机制

无参构造初始为空数组，首次 add 时扩容到默认容量 **10**；容量不足时扩容为原来的 **1.5 倍**（`oldCapacity + (oldCapacity >> 1)`），通过 `Arrays.copyOf` 复制到新数组。建议预估容量指定初始大小，避免多次扩容复制。

### Q16

> HashMap 的底层数据结构（JDK1.7 和 1.8 的区别）

- **JDK1.7**：数组 + 链表，头插法（并发扩容可能形成环形链表死循环）
- **JDK1.8**：数组 + 链表 + 红黑树，改为尾插法；链表长度 ≥ 8 且数组容量 ≥ 64 时转红黑树（查询 O(logn)），红黑树节点数 ≤ 6 时退化回链表

### Q17

> HashMap 的 put 过程和扩容机制

put 过程：计算 hash（key 的 hashCode 高 16 位异或低 16 位，让高位参与运算减少冲突）→ `(n-1) & hash` 定位桶 → 桶为空直接放 → 是链表则尾插遍历（key 相同覆盖，Java8 后才覆盖）→ 是红黑树按树插入 → size 超过阈值扩容。

扩容：默认容量 16，负载因子 0.75，超过 `容量 × 0.75` 就扩容为 **2 倍**。1.8 扩容时元素新位置要么是原下标，要么是"原下标 + 旧容量"（取决于新增的 hash 位是 0 还是 1），无需重新计算 hash。

### Q18

> HashMap 和 HashTable 的区别

| 对比 | HashMap | HashTable |
|---|---|---|
| 线程安全 | 不安全 | 安全（方法加 synchronized，锁整表，性能差） |
| null | 允许 1 个 null key、多个 null value | 不允许 null |
| 初始容量 | 16，扩容 2 倍 | 11，扩容 2n+1 |
| 底层 | 1.8 数组+链表+红黑树 | 数组+链表 |
| 继承 | AbstractMap | Dictionary（过时类） |

线程安全场景用 ConcurrentHashMap。

### Q19

> HashMap 和 ConcurrentHashMap 的区别

CHM 是线程安全的 HashMap：1.7 用分段锁 Segment（默认 16 段，并发度 16）；1.8 抛弃分段锁，用 **CAS + synchronized 锁单个桶的头节点**，锁粒度更细并发更高。CHM 的 key 和 value 都不允许为 null；迭代器是弱一致性的，不抛 ConcurrentModificationException。

### Q20

> ConcurrentHashMap 在 JDK1.7 和 1.8 中的实现区别

- **1.7**：Segment 数组（Segment 继承 ReentrantLock）+ HashEntry 数组，锁粒度是"段"，并发度固定为 Segment 个数（默认16）
- **1.8**：Node 数组 + 链表 + 红黑树；put 时桶为空用 CAS 插入，不为空 synchronized 锁头节点；并发度等于数组长度，随扩容提升；size 统计用 CounterCell 分段计数（借鉴 LongAdder）

### Q21

> HashMap 为什么线程不安全

1. **1.7 头插法**：并发扩容可能形成环形链表，get 时死循环 CPU 100%
2. **数据覆盖丢失**：两个线程同时 put 到同一个空桶，后写的覆盖先写的
3. **size 不准确**：++size 非原子操作
4. **fail-fast**：迭代时其他线程修改结构，抛 ConcurrentModificationException
解决：ConcurrentHashMap 或 Collections.synchronizedMap。

### Q22

> hash 冲突的解决方法

- **链地址法**：冲突的元素挂成链表（HashMap 采用，1.8 长链表转红黑树）
- **开放定址法**：冲突后探测下一个空位（线性探测、平方探测，ThreadLocalMap 采用线性探测）
- **再哈希法**：换一个 hash 函数重算
- **公共溢出区**：冲突元素放入溢出表

### Q23

> JVM 内存区域划分，各区域存放什么

**线程私有：**
- 程序计数器：记录当前线程执行的字节码行号
- 虚拟机栈：栈帧（局部变量表、操作数栈、动态链接、方法出口），方法调用入栈出栈，可能 StackOverflowError
- 本地方法栈：为 native 方法服务

**线程共享：**
- 堆：对象实例和数组，GC 主战场，分新生代（Eden + 2个Survivor）和老年代
- 方法区（JDK8 后为元空间 Metaspace，使用本地内存）：类元信息、运行时常量池、静态变量

### Q24

> 堆和栈的区别

- 堆线程共享，存对象实例，GC 管理，可能 OOM
- 栈线程私有，存栈帧（局部变量、方法调用链），自动分配释放，可能 StackOverflowError
- 堆生命周期随应用，栈随方法调用

### Q25

> 垃圾回收算法有哪些（标记-清除、标记-复制、标记-整理）

- **标记-清除**：标记后直接清除，简单但产生内存碎片
- **标记-复制**：内存分两块，存活对象复制到另一块再整体清空，无碎片但浪费空间；新生代采用（Eden:S0:S1 = 8:1:1）
- **标记-整理**：标记后将存活对象向一端移动，再清理边界外内存，无碎片但移动成本高；老年代采用
- **分代收集**：按对象存活周期分代，新生代用复制算法，老年代用标记清除/整理

### Q26

> 常见的垃圾回收器有哪些，CMS 和 G1 的区别

新生代：Serial、ParNew、Parallel Scavenge；老年代：Serial Old、Parallel Old、CMS；整堆：G1、ZGC、Shenandoah。

CMS vs G1：
- CMS 标记-清除，有内存碎片；G1 标记-整理（Region 间复制），无碎片
- CMS 关注最短停顿；G1 将堆划分为多个 Region，可预测停顿（-XX:MaxGCPauseMillis），优先回收垃圾最多的 Region（Garbage First 由此得名）
- CMS 并发标记时产生浮动垃圾，G1 用 SATB 技术
- JDK9 起 G1 为默认垃圾回收器

### Q27

> 如何判断对象可以被回收（引用计数法、可达性分析）

- **引用计数法**：对象被引用计数+1，为 0 可回收；缺点是无法解决循环引用，JVM 不采用
- **可达性分析**：从 GC Roots（虚拟机栈中引用的对象、静态变量、常量、JNI 引用等）向下搜索，不可达的对象可回收。不可达对象还要经过两次标记（期间可在 finalize 中自救）才真正回收

### Q28

> 类加载的过程，类加载器有哪些，什么是双亲委派模型

类加载过程：**加载**（读取字节码生成 Class 对象）→ **验证** → **准备**（静态变量分配内存赋零值）→ **解析**（符号引用转直接引用）→ **初始化**（执行 `<clinit>`，静态变量赋真实值、静态块执行）。

类加载器：Bootstrap（核心类库）、Ext/Platform、Application（classpath）、自定义。

**双亲委派**：收到加载请求先委托父加载器，父加载器无法完成才自己加载。作用：避免类重复加载；防止核心 API 被篡改（如自定义 java.lang.String 不会被加载）。

### Q29

> 内存泄漏和内存溢出的区别，常见的内存泄漏场景

- **内存泄漏（leak）**：不再使用的对象仍被引用，无法被 GC 回收
- **内存溢出（OOM）**：内存不足，无法分配新对象。泄漏积累最终导致 OOM

常见泄漏场景：静态集合长期持有对象、ThreadLocal 用完不 remove（线程池复用线程）、连接/流未关闭、监听器和回调未注销、内部类持有外部类引用。

### Q30

> JVM 常见调优参数有哪些

`-Xms` / `-Xmx`：堆初始/最大（建议设成一样避免抖动）；`-Xmn`：新生代大小；`-XX:SurvivorRatio`：Eden 与 Survivor 比例；`-XX:MetaspaceSize`：元空间初始；`-XX:+UseG1GC`：选择 GC；`-XX:MaxGCPauseMillis`：目标停顿时间；`-XX:HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=...`：OOM 时转储堆快照；`-XX:+PrintGCDetails`：GC 日志。

### Q31

> volatile 关键字的作用，能否保证原子性

作用：① **可见性**——写 volatile 变量立即刷回主存，读时从主存取，保证各线程看到最新值；② **禁止指令重排**——通过内存屏障实现（典型应用：DCL 单例防半初始化对象）。

**不保证原子性**：`i++` 是读-改-写三步操作，volatile 无法保证，需要用 AtomicInteger 或锁。典型用法：状态标志位、DCL 单例。

### Q32

> synchronized 和 ReentrantLock 的区别

- synchronized 是 JVM 关键字，自动释放锁；ReentrantLock 是 JDK API，必须手动 unlock（放 finally）
- ReentrantLock 支持**可中断获取锁**（lockInterruptibly）、**尝试获取锁**（tryLock 超时）、**公平锁**、**多个 Condition 条件队列**
- 性能上 1.6 优化后两者接近；简单场景用 synchronized，需要高级功能用 ReentrantLock

### Q33

> synchronized 锁升级的过程（偏向锁、轻量级锁、重量级锁）

锁对象头记录锁状态，升级流程（不可逆）：
1. **无锁** → 2. **偏向锁**：只有一个线程访问，在对象头记录线程 ID，之后该线程进入无需同步 → 3. **轻量级锁**：有第二个线程竞争，通过 CAS 自旋获取（适合线程交替执行、锁持有时间短）→ 4. **重量级锁**：自旋失败次数过多，膨胀为 monitor（操作系统互斥量），线程阻塞挂起，涉及用户态内核态切换。
（JDK15 已默认废弃偏向锁）

### Q34

> synchronized 修饰静态方法和实例方法的区别

- 修饰**实例方法**：锁当前对象 this，不同实例之间互不影响
- 修饰**静态方法**：锁 Class 对象，该类的所有实例共用一把锁（全局锁）
- 修饰代码块：锁 synchronized(括号里的对象)

### Q35

> wait 和 sleep 的区别

- 所属：wait 是 Object 的方法；sleep 是 Thread 的静态方法
- 锁：**wait 释放锁**，sleep **不释放锁**
- 唤醒：wait 需 notify/notifyAll（或超时）；sleep 时间到自动醒
- 使用位置：wait 必须在 synchronized 块内调用；sleep 任何地方都可以
- 用途：wait 用于线程间通信，sleep 用于暂停执行

### Q36

> ThreadLocal 的原理，为什么会内存泄漏

每个 Thread 内部有一个 ThreadLocalMap，key 是 ThreadLocal 对象（**弱引用**），value 是存的值（**强引用**）。

内存泄漏原因：ThreadLocal 外部引用被回收后，map 中的 key 变为 null，但 value 仍被强引用着，只要线程不死（线程池中线程长期存活）value 就无法回收。对策：用完必须调用 `remove()`；get/set 时也会顺带清理部分 key=null 的entry。

用途：线程隔离（每个线程一份副本），如存用户登录信息、数据库连接、SimpleDateFormat。

### Q37

> 死锁产生的四个必要条件，如何避免死锁

四个必要条件：**互斥**、**请求与保持**、**不可剥夺**、**循环等待**。

避免方式（破坏任一条件）：按固定顺序加锁（破坏循环等待）、一次性申请所有资源（破坏请求保持）、使用 tryLock 带超时（破坏不可剥夺）。排查：jstack / jconsole 查看线程状态。

### Q38

> 什么是 Java 中的反射，反射的应用场景

反射是在运行时动态获取类的信息（Class 对象：构造器、方法、字段）并操作的能力，可以绕过编译期检查访问 private 成员。

获取 Class 的方式：`Class.forName("全类名")`、`类名.class`、`对象.getClass()`。

应用场景：Spring IOC 容器创建 Bean、MyBatis 结果映射、JDK 动态代理、注解处理、JSON 序列化框架。缺点：性能低于直接调用、破坏封装。

### Q39

> JDK 动态代理和 CGLIB 动态代理的区别

- **JDK 动态代理**：基于**接口**，通过 Proxy.newProxyInstance + InvocationHandler 生成实现相同接口的代理类，反射调用；目标类必须有接口
- **CGLIB**：基于**继承**，通过字节码技术生成目标类的子类作为代理，覆写非 final 方法，MethodInterceptor 拦截；不能代理 final 类和 final 方法

Spring AOP 默认：有接口用 JDK，无接口用 CGLIB；SpringBoot 2.x 起默认统一 CGLIB（proxyTargetClass=true）。

### Q40

> Java 8 的新特性有哪些

Lambda 表达式、函数式接口（@FunctionalInterface）、Stream API、Optional 防 NPE、方法引用（::）、接口 default/static 方法、新的日期时间 API（LocalDate/LocalDateTime，线程安全）、HashMap/ConcurrentHashMap 底层重构（红黑树）。

### Q41

> Lambda 表达式和函数式接口

Lambda：`(参数) -> 表达式或语句块`，是函数式接口的实例，语法糖替代匿名内部类。

函数式接口：只有一个抽象方法的接口，@FunctionalInterface 校验。四大内置：`Function<T,R>`（有入参有返回）、`Consumer<T>`（只消费）、`Supplier<T>`（只提供）、`Predicate<T>`（判断返回 boolean）。

### Q42

> Stream 流的常用操作，中间操作和终端操作的区别

- **中间操作**（返回新 Stream，惰性求值不立即执行）：filter 过滤、map/flatMap 转换、sorted 排序、distinct 去重、limit/skip 截取、peek
- **终端操作**（触发执行）：forEach、collect（配合 Collectors.toList/groupingBy/joining/toMap）、reduce 归约、count、min/max、anyMatch/allMatch/noneMatch、findFirst

区别：中间操作是惰性的，只有终端操作触发才执行整个流水线；流只能被消费一次。

### Q43

> 接口中可以有默认方法吗（default 方法）

可以。Java 8 起接口允许 default 方法（有方法体，实现类直接继承，也可重写）和 static 静态方法。作用：在不破坏已有实现类的前提下给接口升级新方法（如 Collection.stream()）。冲突规则：类继承的方法优先于接口默认方法；实现多个接口有相同默认方法时必须重写。

### Q44

> Object 类中有哪些常用方法

共 11 个：equals、hashCode、toString、clone、getClass、finalize（废弃）、wait()（3 个重载）、notify、notifyAll。其中 equals/hashCode/wait/notify 是面试高频，getClass 用于反射获取运行时类型。
