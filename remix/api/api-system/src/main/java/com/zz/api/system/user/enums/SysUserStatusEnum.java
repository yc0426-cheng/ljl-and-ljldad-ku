package com.zz.api.system.user.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * <p><b>系统服务-系统用户状态枚举类</b></p>
 *
 * @author yangcheng
 * @since 2026/8/20 17:17
 */
@Getter
public enum SysUserStatusEnum {
    ENABLE(1,"启用"),
    FROZEN(0,"冻结"),
    DORMANT(-1,"休眠");

    SysUserStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 值
     */
    @EnumValue //标记数据库存储值, 使值正确映射
    private final int value;

    /**
     * 描述
     */
    private final String desc;
}
