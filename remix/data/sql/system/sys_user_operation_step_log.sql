-- 创建数据库（如果已存在则忽略）
CREATE DATABASE IF NOT EXISTS learn
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换到该数据库
USE learn;

-- 步骤记录子表：
-- 关联 sys_user_operation_log（请求级主表，一行 = 一次完整请求），
-- 本表一行 = 该请求调用链上的一次方法调用（模块级步骤），用 log_id 关联主表。
-- 调用链可能是嵌套的（如 login(auth) 内部再发起 feign 调用），
-- 用 parent_step_id 表达父子层级：顶层步骤为 NULL，子调用指向其所属父步骤。
-- step_no 为整条请求内的先序序号（类似你 description 里箭头串的先后顺序），
-- 既方便按 (log_id, step_no) 排序展示，也可据此还原 login(auth)->login(feign) 之类的摘要。
CREATE TABLE IF NOT EXISTS sys_user_operation_step_log (
    step_id        BIGINT PRIMARY KEY COMMENT '步骤记录ID',
    log_id         BIGINT       NOT NULL COMMENT '所属请求记录ID，关联 sys_user_operation_log.log_id',
    parent_step_id BIGINT       DEFAULT NULL COMMENT '父步骤ID，顶层步骤为 NULL，关联本表 step_id',
    step_no        INT          NOT NULL COMMENT '请求内先序步骤序号，从1开始',
    module_name    varchar(50)  NOT NULL COMMENT '执行模块：auth / feign / system 等',
    method_name    varchar(100) NOT NULL COMMENT '方法名：login / getUserInfoByAccount 等',
    call_type      varchar(20)  DEFAULT NULL COMMENT '调用方式：controller / feign / local 等',
    status         int          DEFAULT 0 COMMENT '本步骤状态：0 = 失败，1 = 成功，2 = 执行中',
    error_message  varchar(500) DEFAULT NULL COMMENT '本步骤错误信息（仅失败时记录）',
    start_time     datetime(3)  DEFAULT NULL COMMENT '步骤开始时间(毫秒精度)',
    end_time       datetime(3)  DEFAULT NULL COMMENT '步骤结束时间(毫秒精度)',
    cost_ms        BIGINT       DEFAULT NULL COMMENT '本步骤耗时毫秒 = end_time - start_time',
    target_db      varchar(50)  DEFAULT NULL COMMENT '(可选)本步骤修改的目标数据库名，如 learn；只读步骤为 NULL',
    target_table   varchar(100) DEFAULT NULL COMMENT '(可选)本步骤修改的目标表名，如 sys_user；只读步骤为 NULL',

    index(log_id),             # 按请求查该请求下的全部步骤
    index(log_id, step_no),    # 按请求 + 先序序号排序查询
    index(parent_step_id)      # 按父步骤查其子调用
) comment '用户操作步骤记录表'

-- 若之前已用旧版 DDL 建过表，需要补列时手动执行：
-- ALTER TABLE sys_user_operation_step_log
--     ADD COLUMN target_db    varchar(50)  DEFAULT NULL COMMENT '修改的目标库',
--     ADD COLUMN target_table varchar(100) DEFAULT NULL COMMENT '修改的目标表';

-- 数据形态示例：
-- log_id=1001 的 login(auth) 内部调用了 feign 的 login：
--   step_id=1, log_id=1001, parent_step_id=NULL, step_no=1, module_name=auth,   method_name=login, status=1
--   step_id=2, log_id=1001, parent_step_id=1,    step_no=2, module_name=feign,  method_name=login, status=0, error_message='密码错误'
-- 主表 sys_user_operation_log.status 仍表示整次请求最终结果；
-- 本表 status/error_message 表示到每个步骤为止的明细。
