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
    password       varchar(200)      NOT NULL comment '密码（BCrypt哈希）',
    phone          char(11)          not null comment '手机号码',
    id_number       char(18)          not null comment '身份证号',
    email          varchar(200)      not null comment '邮箱',
    pass_error_count int     default 0 not null comment '密码错误次数',
    status         int     default 1 not null comment '状态',
    last_login_time  timestamp(6) comment '最后登录时间',
    del_flag        bool default false comment '删除标记'
) COMMENT ='用户信息表';

-- 种子用户（admin 与 user2 的登录密码均为 666666，以下为 BCrypt 哈希）
insert into learn.sys_user(user_id, account, name, password, phone, id_number, email, pass_error_count, status, last_login_time,
                           del_flag)
values (
          1, 'admin', '管理员', '$2a$10$unNxxINmpUiXq/MdwUZY5e9aAvhLQtqB0KCWLubLQ0msm5fEPBXmK',
          11122223333, 123456888888889999, '666666@qq.com', 0, 1, null, 0
       ),
       (
          2, 'user2', '测试用户2', '$2a$10$unNxxINmpUiXq/MdwUZY5e9aAvhLQtqB0KCWLubLQ0msm5fEPBXmK',
          11122223334, 123456888888889998, 'user2@qq.com', 0, 1, null, 0
       );
