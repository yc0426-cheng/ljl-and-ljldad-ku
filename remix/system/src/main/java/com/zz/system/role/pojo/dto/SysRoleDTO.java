package com.zz.system.role.pojo.dto;

import lombok.Data;

/**
 * <p><b>系统服务-角色修改类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/23 23:04
 */
@Data
public class SysRoleDTO {
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 是否启用
     */
    private Integer enabled;
}
