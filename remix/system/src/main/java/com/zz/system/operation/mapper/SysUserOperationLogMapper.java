package com.zz.system.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.system.operation.entity.SysUserOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户操作记录表（主表）Mapper
 *
 * <p>本表只由 system-server 写入/查询，供 Feign 接口（OperationLogFeignClient）
 * 背后的控制器与业务 Service 使用。</p>
 *
 * <p>风格与 SysUserMapper 一致：继承 MyBatis-Plus BaseMapper 即具备单表 CRUD，
 * 复杂查询（如按步骤拼接 description 需要跨子表统计时）可在 XML 中补充。</p>
 *
 * @description 针对表【sys_user_operation_log(用户操作记录表)】的数据库操作Mapper
 * @since 2026-09-10
 */
@Mapper
public interface SysUserOperationLogMapper extends BaseMapper<SysUserOperationLog> {

}
