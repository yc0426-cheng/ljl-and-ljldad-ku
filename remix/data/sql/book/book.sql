-- 创建数据库（如果已存在则忽略）
CREATE DATABASE IF NOT EXISTS learn
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换到该数据库
USE learn;

CREATE TABLE book
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID，全局唯一',
    title       VARCHAR(255) NOT NULL COMMENT '书名',
    cover_url   VARCHAR(512) COMMENT '封面图OSS地址',
    file_url    VARCHAR(512) NOT NULL COMMENT '书籍原文件OSS地址',
    file_format VARCHAR(16) COMMENT '文件格式',
    file_size   BIGINT COMMENT '文件大小(字节)',
    file_hash   VARCHAR(64) COMMENT '文件SHA-256',
    total_pages INT COMMENT '总页数',
    language    VARCHAR(16) COMMENT '书籍语言',
    status      TINYINT DEFAULT 1 COMMENT '状态：1正常 0下架 2解析中 3解析失败',
    uploader_id BIGINT COMMENT '上传者ID',
    create_user BIGINT       NULL COMMENT '创建用户ID',
    create_time TIMESTAMP COMMENT '创建时间',
    update_user BIGINT       NULL COMMENT '更新用户ID',
    update_time TIMESTAMP COMMENT '更新时间',
    INDEX idx_title (title),   -- 书名搜索索引
    INDEX idx_hash (file_hash) -- 秒传查重索引
) COMMENT ='书籍信息表';