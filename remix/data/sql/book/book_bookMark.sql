-- 创建数据库（如果已存在则忽略）
CREATE DATABASE IF NOT EXISTS learn
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换到该数据库
USE learn;

CREATE TABLE book_bookmark
(
    book_bookMark_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '页签ID',
    book_id          BIGINT NOT NULL COMMENT '书籍ID',
    user_id          BIGINT NOT NULL COMMENT '所属用户ID',
    location         JSON   NOT NULL COMMENT '定位信息JSON',
    page_no          INT COMMENT '冗余页码',
    label            VARCHAR(255) COMMENT '页签名称',
    color            VARCHAR(16) COMMENT '页签颜色',
    create_user      BIGINT NULL COMMENT '创建用户ID',
    create_time      TIMESTAMP COMMENT '创建时间',
    update_user      BIGINT NULL COMMENT '更新用户ID',
    update_time      TIMESTAMP COMMENT '更新时间',

    INDEX idx_user_book (user_id, book_id) -- 查某用户某书的全部页签
) COMMENT ='书籍页签表';