package com.zz.common.core.entity;

import lombok.Data;

import java.util.Date;

/**
 * <p><b>通用核心工具-实体基类</b></p>
 *
 * @description 所有实体类均继承此类
 * @author yangcheng
 * @since 2026/9/15 14:30
 */
@Data
public class BaseEntity {

    /**
     * 创建用户
     */
    private Long createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新用户
     */
    private Long updateUser;

    /**
     * 更新时间
     */
    private Date updateTime;
}
