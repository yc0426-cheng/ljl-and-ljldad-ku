package com.zz.system.operation.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户操作步骤记录表（子表/方法级）
 *
 * <p>一行 = 一次完整请求调用链上的一个方法调用，通过 log_id 关联主表
 * sys_user_operation_log；通过 parent_step_id 表达调用树的父子层级：
 * 根步骤 parent_step_id 为 NULL，子调用指向其所属父步骤的 step_id。</p>
 *
 * <p>字段与 SQL 文件 data/sql/system/sys_user_operation_step_log.sql 一一对应；
 * 由 system-server 统一写入，其他模块通过 Feign 通知，不直接操作本表。</p>
 *
 * <p>数据形态示例（login(auth) 内部调用了一次 feign）：</p>
 * <ul>
 *     <li>step_id=1, log_id=1001, parent_step_id=NULL, step_no=1, module_name=auth,  method_name=login, status=1</li>
 *     <li>step_id=2, log_id=1001, parent_step_id=1,    step_no=2, module_name=feign, method_name=login, status=0, error_message='密码错误'</li>
 * </ul>
 *
 * @TableName sys_user_operation_step_log
 * @author yangcheng
 * @since 2026-09-10
 */
@Data
@TableName("sys_user_operation_step_log")
public class SysUserOperationStepLog {

    /**
     * 主键：步骤记录 ID（雪花算法）
     */
    @TableId
    private Long stepId;

    /**
     * 所属请求 log_id：关联 sys_user_operation_log.log_id
     */
    private Long logId;

    /**
     * 父步骤 step_id：根步骤为 NULL，子调用指向父步骤，关联本表 step_id（支持嵌套调用树）
     */
    private Long parentStepId;

    /**
     * 请求内先序步骤序号，从 1 开始（根为 1，子步骤按创建顺序递增）；
     * 用于排序展示，以及请求结束时拼 description
     */
    private Integer stepNo;

    /**
     * 执行模块：auth / feign / system 等
     */
    private String moduleName;

    /**
     * 方法名：login / getUserInfoByAccount 等
     */
    private String methodName;

    /**
     * 调用方式：controller / service / feign / local 等
     */
    private String callType;

    /**
     * 本步骤状态：0 = 失败，1 = 成功，2 = 执行中（中间态）
     */
    private Integer status;

    /**
     * 本步骤错误信息（仅失败时记录）
     */
    private String errorMessage;

    /**
     * 步骤开始时间（毫秒精度 datetime(3)）
     */
    private Date startTime;

    /**
     * 步骤结束时间（毫秒精度 datetime(3)）
     */
    private Date endTime;

    /**
     * 本步骤耗时毫秒 = end_time - start_time
     */
    private Long costMs;

    /**
     * （可选）本步骤修改的目标数据库名，如 learn；只读步骤为 null
     */
    private String targetDb;

    /**
     * （可选）本步骤修改的目标表名，如 sys_user；只读步骤为 null
     */
    private String targetTable;
}
