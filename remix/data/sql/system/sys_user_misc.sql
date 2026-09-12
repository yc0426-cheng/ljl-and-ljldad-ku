-- 创建数据库（如果已存在则忽略）
CREATE DATABASE IF NOT EXISTS learn
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换到该数据库
USE learn;

-- 用户信息杂项表：存放不便于放入 sys_user 主表的用户扩展信息（头像、个性签名等）
-- avatar 为 OSS 外链地址，由后端上传接口转存 OSS 后写入
CREATE TABLE IF NOT EXISTS sys_user_misc (
    misc_id      BIGINT PRIMARY KEY COMMENT '杂项记录ID',
    user_id      BIGINT       NOT NULL COMMENT '用户ID（关联 sys_user.user_id）',
    avatar       varchar(500) DEFAULT NULL COMMENT '头像 OSS 地址',
    nickname     varchar(200) DEFAULT NULL COMMENT '昵称',
    signature    varchar(500) DEFAULT NULL COMMENT '个性签名',
    create_time  DATETIME COMMENT '创建时间',
    update_time  DATETIME COMMENT '更新时间',

    UNIQUE INDEX uniq_user_id (user_id) # 一人一条扩展信息
) COMMENT ='用户信息杂项表';
