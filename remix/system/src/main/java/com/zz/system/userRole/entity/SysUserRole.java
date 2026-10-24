package com.zz.system.userRole.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zz.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
* 用户角色绑定表
*
* @author yangcheng
* @since 2026-10-24 20:02:28
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user_role")
public class SysUserRole extends BaseEntity {

    /**
    * 用户绑定角色ID
    */
    private Long userRoleId;
    /**
    * 用户id
    */
    private Long userId;
    /**
    * 角色ID
    */
    private Long roleId;
}
