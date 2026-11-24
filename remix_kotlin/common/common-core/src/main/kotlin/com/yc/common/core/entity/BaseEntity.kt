package com.yc.common.core.entity

import java.util.Date

/**
 * <p><b>通用核心工具-实体基类</b></p>
 *
 * @description 所有实体类均继承此类
 * @author yangcheng
 * @since 2026/11/17 14:30
 */
open class BaseEntity(
    var createUser: Long? = null,
    var createTime: Date? = null,
    var updateUser: Long? = null,
    var updateTime: Date? = null
)
