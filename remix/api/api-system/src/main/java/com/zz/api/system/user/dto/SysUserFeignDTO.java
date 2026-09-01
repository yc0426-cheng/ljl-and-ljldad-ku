package com.zz.api.system.user.dto;

import com.zz.api.system.user.enums.SysUserStatusEnum;
import lombok.Data;

import java.util.Date;

/**
 * <p><b>系统服务-用户信息远程调用数据传输类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/1 17:06
 */
@Data
public class SysUserFeignDTO {

    /**
    * 用户ID
    */
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
