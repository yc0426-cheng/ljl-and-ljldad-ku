1.BigDecimal:
    因为float和double采用的是IEEE 754标准的二进制浮点数表示, 而BigDecimal是采用整数intValue和小数点位置scale来存储的

2.java线程的六种状态:
    NEW, RUNNABLE, BLOCKED, WAITING, TIME_WAITING, TERMINATED
    而操作系统线程只有五种状态 NEW, READY, RUNNING, BLOCKED, TERMINATED 
    READY + RUNNING == RUNNABLE