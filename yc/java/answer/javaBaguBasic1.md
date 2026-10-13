# Java基础 八股答案（1）

### Q1

> BigDecimal

用于精确表示小数，解决 double 浮点精度丢失问题。要点：构造时用字符串 `new BigDecimal("0.1")` 或 `BigDecimal.valueOf(0.1)`，不要用 double 构造；比较用 `compareTo` 而不是 `equals`（equals 还会比较精度 scale，1.0 和 1.00 不相等）；除不尽时必须指定精度和舍入模式 `divide(b, 2, RoundingMode.HALF_UP)`，否则抛 ArithmeticException。

### Q2

> java线程的六种状态

- **NEW**：创建了线程对象但还没调用 start()
- **RUNNABLE**：可运行状态（包含就绪和运行中，等待 CPU 调度也算）
- **BLOCKED**：等待获取 synchronized 的 monitor 锁
- **WAITING**：无限期等待，需被显式唤醒（wait()、join()、LockSupport.park()）
- **TIMED_WAITING**：限时等待，超时自动醒来（sleep(n)、wait(n)、join(n)）
- **TERMINATED**：run() 执行完毕，线程结束

### Q3

> 线程池的七大参数，以及拒绝策略的四种

七大参数：
1. corePoolSize：核心线程数（不会被回收，除非设置 allowCoreThreadTimeOut）
2. maximumPoolSize：最大线程数
3. keepAliveTime：非核心线程空闲存活时间
4. unit：存活时间的单位
5. workQueue：任务阻塞队列（如 LinkedBlockingQueue、ArrayBlockingQueue、SynchronousQueue）
6. threadFactory：线程工厂，自定义线程名等
7. handler：拒绝策略

执行顺序：核心线程 → 队列 → 非核心线程 → 拒绝策略。

四种拒绝策略：
- **AbortPolicy**（默认）：抛出 RejectedExecutionException
- **CallerRunsPolicy**：由提交任务的线程自己执行（起到削峰减速作用）
- **DiscardPolicy**：静默丢弃新任务，不抛异常
- **DiscardOldestPolicy**：丢弃队列中等待最久的任务，然后尝试提交新任务

### Q4

> 创建线程的三种方式

1. **继承 Thread 类**，重写 run()，调用 start()
2. **实现 Runnable 接口**，实现 run()，传给 Thread 执行（无返回值）
3. **实现 Callable 接口**，配合 FutureTask 执行，call() 有返回值且可以抛出受检异常，通过 futureTask.get() 获取结果

实际开发中推荐使用线程池（ExecutorService 提交 Runnable/Callable），避免频繁创建销毁线程的开销。
