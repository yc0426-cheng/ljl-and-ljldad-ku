-- 创建数据库（如果已存在则忽略）
CREATE DATABASE IF NOT EXISTS learn
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换到该数据库
USE learn;

CREATE TABLE book_page
(
    book_page_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分页记录主键ID',
    book_id      BIGINT      NOT NULL COMMENT '书籍ID',
    book_version INT         NOT NULL DEFAULT 1 COMMENT '书籍版本，重新解析后递增',
    page_no      INT         NOT NULL COMMENT '页码',
    sort_order   INT         NOT NULL DEFAULT 0 COMMENT '导出排序号，默认等于page_no，保证顺序稳定',
    content_type VARCHAR(16) NOT NULL DEFAULT 'text' COMMENT '内容类型',
    oss_key      VARCHAR(512) COMMENT '该页资源OSS对象key（非完整URL），便于换域名/签名',
    content_url  VARCHAR(512) COMMENT '该页渲染资源OSS地址',
    content_text LONGTEXT COMMENT '该页纯文本内容',
    char_start   BIGINT COMMENT '该页文本在全书中的起始字符偏移',
    char_end     BIGINT COMMENT '该页文本在全书中的结束字符偏移',
    char_count   INT COMMENT '该页字符数',
    status       TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1有效 0失效（重新分页时旧记录置0）',
    create_user  BIGINT      NULL COMMENT '创建用户ID',
    create_time  TIMESTAMP COMMENT '创建时间',
    update_user  BIGINT      NULL COMMENT '更新用户ID',
    update_time  TIMESTAMP COMMENT '更新时间',

    -- 同一本书同一版本同一页只允许一条
    UNIQUE KEY uk_book_ver_page (book_id, book_version, page_no),
    -- 按书+版本查所有有效页（导出主查询）
    KEY idx_book_ver_sort (book_id, book_version, sort_order),
    -- 批注反查页：字符偏移落在哪页
    KEY idx_book_char (book_id, book_version, char_start, char_end)
) COMMENT ='书籍分页表';