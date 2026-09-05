-- 创建数据库（如果已存在则忽略）
CREATE DATABASE IF NOT EXISTS learn
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换到该数据库
USE learn;

CREATE TABLE IF NOT EXISTS sys_user_operation_log (
    log_id  BIGINT PRIMARY KEY COMMENT '用户记录ID',
    user_id BIGINT COMMENT '操作用户ID',
    description varchar(1000) COMMENT '操作描述',
    operation_date DATETIME COMMENT '操作日期',
    operation_ip varchar(50) COMMENT '操作地址',
    operation_method varchar(10) COMMENT '请求方法',
    error_message varchar(200) DEFAULT null COMMENT '错误信息',
    status int default 0 COMMENT '操作状态（0 = 失败 ， 1 = 成功）',

    index(user_id) # user_id索引
) comment '用户操作记录表'