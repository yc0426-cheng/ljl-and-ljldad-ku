package com.zz.system.role.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zz.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 *
 *
 * @author yangcheng
 * @since 2026-09-23 22:29:16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /**
     * 角色ID
     */
    @TableId
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
    @TableLogic // 逻辑删除
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
