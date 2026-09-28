-- 创建数据库（如果已存在则忽略）
CREATE DATABASE IF NOT EXISTS learn
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换到该数据库
USE learn;

CREATE TABLE IF NOT EXISTS sys_user_role
(
    user_role_id BIGINT PRIMARY KEY COMMENT '用户绑定角色ID',
    user_id BIGINT COMMENT '用户id',
    role_id_list     BIGINT COMMENT '角色ID列表',
    create_user BIGINT NULL COMMENT '创建用户ID',
    create_time TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    update_user BIGINT NULL COMMENT '更新用户ID',
    update_time TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间'
)