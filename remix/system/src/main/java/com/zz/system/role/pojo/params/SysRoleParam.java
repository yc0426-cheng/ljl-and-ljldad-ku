package com.zz.system.role.pojo.params;

import lombok.Data;

/**
 * <p><b>系统服务-角色查询类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/28 18:21
 */
@Data
public class SysRoleParam {

    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 是否启用
     */
    private Integer enabled;
}
