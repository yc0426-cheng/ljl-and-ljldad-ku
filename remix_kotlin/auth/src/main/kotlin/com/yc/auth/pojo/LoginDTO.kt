package com.yc.auth.pojo

import jakarta.validation.constraints.NotBlank
import lombok.NonNull

/**
 * <p><b>认证服务-登录数据</b></p>
 *
 * @author yangcheng
 * @since 2026/11/17 10:39
 */
data class LoginDTO(
    @field:NotBlank(message = "账号不能为空") val account: String,
    @field:NotBlank(message = "密码不能为空") val password: String
)
