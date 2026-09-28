package com.zz.system.role.pojo.vo;

import lombok.Data;

import java.util.Date;

/**
 * <p><b>系统服务-角色返回结果类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/28 18:24
 */
@Data
public class SysRoleVO {

    /**
     * 角色ID
     */
    private Long roleId;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 是否启用
     */
    private Integer enabled;
    /**
     * 是否删除
     */
    private Integer delFlag;
    /**
     * 创建用户ID
     */
    private Long createUser;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新用户ID
     */
    private Long updateUser;
    /**
     * 更新时间
     */
    private Date updateTime;
}
