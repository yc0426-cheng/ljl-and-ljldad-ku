package com.zz.system.userRole.pojo.vo;

import lombok.Data;

/**
 * <p><b>系统服务-用户角色返回结果类</b></p>
 *
 * @author yangcheng
 * @since 2026/10/24 23:57
 */
@Data
public class SysUserRoleVO {

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
