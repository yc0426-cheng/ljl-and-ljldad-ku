package com.zz.common.core.pojo;

import lombok.Data;

/**
 * <p><b>通用工具-登录用户信息 pojo类</b></p>
 *
 * @author yangcheng
 * @since 2026/8/20 10:45
 */
@Data
public class LoginUserInfo {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 账号
     */
    private String account;

    /**
     * 名称
     */
    private String name;

    /**
     * 头像（OSS 地址，存于 sys_user_misc 杂项表，登录时拼进本对象）
     */
    private String avatar;

    /**
     * token
     */
    private String token;
}
