package com.zz.system.operation.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户操作记录表（主表/请求级）
 *
 * <p>一行 = 一次完整的用户操作/请求。description 是对整次请求的摘要描述，
 * 如 login(auth)-&gt;getUserInfoByAccount(feign)，可由落库端在 finishRequest 时
 * 按步骤表 sys_user_operation_step_log 的 step_no 先序自动拼接生成。</p>
 *
 * <p>字段与 SQL 文件 data/sql/system/sys_user_operation_log.sql 一一对应；
 * 由 system-server 统一写入，其他模块通过 Feign 通知，不直接操作本表。</p>
 *
 * <p>主键由 MyBatis-Plus 全局 id-type=assign_id（雪花算法）自动生成，
 * 与 system-server Nacos 配置保持一致。</p>
 *
 * @TableName sys_user_operation_log
 * @author yangcheng
 * @since 2026-09-10
 */
@Data
@TableName("sys_user_operation_log")
public class SysUserOperationLog {

    /**
     * 主键：请求记录 ID（雪花算法）
     */
    @TableId
    private Long logId;

    /**
     * 操作用户 ID：登录等匿名入口先为 null，登录成功后可在 finishRequest 回填
     */
    private Long userId;

    /**
     * 操作描述：如 login(auth)-&gt;getUserInfoByAccount(feign)
     * <p>由落库端在请求结束时按 step_no 先序拼接各步骤生成</p>
     */
    private String description;

    /**
     * 操作日期（请求开始时间，写入时取当前时间）
     */
    private Date operationDate;

    /**
     * 操作（客户端）IP
     */
    private String operationIp;

    /**
     * 请求方法（HTTP 方法，如 POST）
     */
    private String operationMethod;

    /**
     * 错误信息：整次请求失败时记录，成功为 null
     */
    private String errorMessage;

    /**
     * 操作状态：0 = 失败，1 = 成功（2 执行中仅作为写入过程的中间态，正常结束前会落成 0/1）
     */
    private Integer status;
}
