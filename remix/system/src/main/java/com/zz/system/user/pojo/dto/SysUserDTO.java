package com.zz.system.user.pojo.dto;

import com.zz.system.user.enums.SysUserStatusEnum;
import lombok.Data;

/**
 * <p><b>模块-类说明</b></p>
 *
 * @author yangcheng
 * @since 2026/9/10 16:46
 */
@Data
public class SysUserDTO {
    /**
     * id
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
     * 状态
     */
    private SysUserStatusEnum status;
}
