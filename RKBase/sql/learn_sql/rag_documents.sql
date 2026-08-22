-- 创建数据库（如果已存在则忽略）
-- RAG文档源表
CREATE TABLE IF NOT EXISTS rag_document
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文档ID',
    user_id        BIGINT       NOT NULL COMMENT '所属用户ID',
    file_name      VARCHAR(200) NOT NULL COMMENT '原始文件名',
    file_path      VARCHAR(500) NOT NULL COMMENT '文件存储路径（本地路径或S3 URL）',
    file_type      VARCHAR(20)  DEFAULT NULL COMMENT '文件类型: pdf, md, txt, docx',
    status         INT          DEFAULT 0 NOT NULL COMMENT '处理状态: 0-已上传, 1-处理中, 2-已索引, 3-失败',
    meta_data      JSON         DEFAULT NULL COMMENT '元数据（JSON格式），如作者、页数、文件大小等',
    del_flag       BOOL         DEFAULT FALSE COMMENT '删除标记',
    UNIQUE KEY uk_user_file (user_id, file_name, del_flag)
) COMMENT ='RAG文档源表';
