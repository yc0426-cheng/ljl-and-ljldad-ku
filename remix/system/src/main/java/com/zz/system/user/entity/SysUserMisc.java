package com.zz.system.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zz.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
* 用户信息杂项表
*
* @author yangcheng
* @since 2026-09-15 14:26:04
*/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user_misc")
public class SysUserMisc extends BaseEntity {

    /**
    * 杂项记录ID
    */

    private Long miscId;

    /**
    * 用户ID（关联 sys_user.user_id）
    */
    private Long userId;

    /**
    * 头像 OSS 地址
    */
    private String avatar;

    /**
    * 昵称
    */
    private String nickname;

    /**
    * 个性签名
    */
    private String signature;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 更新时间
    */
    private Date updateTime;

    /**
     * 历史头像
     */
    private String historyAvatars;
}
