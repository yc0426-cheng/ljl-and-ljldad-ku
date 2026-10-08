-- 创建数据库（如果已存在则忽略）
CREATE DATABASE IF NOT EXISTS learn
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换到该数据库
USE learn;

CREATE TABLE IF NOT EXISTS sys_role
(
    role_id     BIGINT PRIMARY KEY COMMENT '角色ID',
    role_name   varchar(200) COMMENT '角色名称',
    enabled     boolean      default 1 COMMENT '是否启用',
    del_flag    boolean      default 0 COMMENT '是否删除',
    create_user BIGINT NULL COMMENT '创建用户ID',
    create_time TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    update_user BIGINT NULL COMMENT '更新用户ID',
    update_time TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间'
) comment '系统角色表';


-- 写入初始角色数据
INSERT INTO sys_role (role_id, role_name, enabled, del_flag)
VALUES (0001, '超级管理员', 1, 0),
       (0002, '普通用户', 1, 0),
       (0003, '图书管理员', 1, 0),
       (0004, '题目管理员', 1, 0)
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name),
                        enabled   = VALUES(enabled),
                        del_flag  = VALUES(del_flag);