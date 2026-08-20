package com.zz.system.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zz.system.user.enums.SysUserStatusEnum;
import lombok.Data;

import java.util.Date;

/**
* 用户信息表
* @TableName sys_user
*
* @author yangcheng
* @since 2026-08-20 16:55:09
*/
@Data
@TableName("sys_user")
public class SysUser {

    /**
    * 用户ID
    */
    @TableId
    private Long userId;
    /**
    * 账号
    */
    private String account;
    /**
    * 姓名
    */
    private String name;
    /**
    * 密码
    */
    private String password;
    /**
    * 手机号码
    */
    private String phone;
    /**
    * 身份证号
    */
    private String idNumber;
    /**
    * 邮箱
    */
    private String email;
    /**
    * 密码错误次数
    */
    private Integer passErrorCount;
    /**
    * 状态
    */
    private SysUserStatusEnum status;
    /**
    * 最后登录时间
    */
    private Date lastLoginTime;
    /**
    * 删除标记
    */
    private Boolean delFlag;
}
