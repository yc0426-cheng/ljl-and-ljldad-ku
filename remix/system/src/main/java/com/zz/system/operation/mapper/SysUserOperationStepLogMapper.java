package com.zz.system.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.system.operation.entity.SysUserOperationStepLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户操作步骤记录表（子表）Mapper
 *
 * <p>本表只由 system-server 写入/查询，供 Feign 接口（OperationLogFeignClient）
 * 背后的控制器与业务 Service 使用。</p>
 *
 * <p>风格与 SysUserMapper 一致：继承 MyBatis-Plus BaseMapper 即具备单表 CRUD。</p>
 *
 * <p>常用场景提示：</p>
 * <ul>
 *     <li>按 log_id 查某次请求的全部步骤（ORDER BY step_no）用于拼 description / 前端展示调用树；</li>
 *     <li>按 module_name / method_name / status 分组统计某模块某方法的失败次数与平均耗时；</li>
 *     <li>step_no 递增分配时可考虑并发下的唯一性（学习项目可接受 MAX+1 的轻微竞态，生产可加唯一索引或由雪花/序列替代）。</li>
 * </ul>
 *
 * @description 针对表【sys_user_operation_step_log(用户操作步骤记录表)】的数据库操作Mapper
 * @since 2026-09-10
 */
@Mapper
public interface SysUserOperationStepLogMapper extends BaseMapper<SysUserOperationStepLog> {

}
