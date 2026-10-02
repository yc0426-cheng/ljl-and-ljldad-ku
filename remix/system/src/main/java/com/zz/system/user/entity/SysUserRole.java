package com.zz.system.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zz.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* 用户角色绑定表
*
* @author yangcheng
* @since 2026-10-02 14:52:14
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user_role")
public class SysUserRole extends BaseEntity {

    /**
    * 用户绑定角色ID
    */
    @TableId
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
