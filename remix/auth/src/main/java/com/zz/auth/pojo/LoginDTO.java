package com.zz.auth.pojo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p><b>认证服务-登录数据</b></p>
 *
 * @author yangcheng
 * @since 2026/8/20 10:39
 */
@Data
public class LoginDTO {

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;
}
