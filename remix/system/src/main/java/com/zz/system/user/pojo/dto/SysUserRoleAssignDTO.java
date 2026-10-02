package com.zz.system.user.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * <p><b>系统服务-用户角色分配入参 DTO</b></p>
 *
 * <p>前端以 @RequestBody JSON 提交，结构如下：</p>
 * <pre>{@code
 * { "userId": 1, "roleIds": [1, 2, 3] }
 * }</pre>
 * <p>roleIds 传空数组 {@code []} 表示清空该用户的全部角色（全量分配：先删后插）。</p>
 *
 * @author yangcheng
 * @since 2026-10-02
 */
@Data
public class SysUserRoleAssignDTO {

    /**
     * 被分配角色的用户id（必填）
     */
    private Long userId;

    /**
     * 角色id集合；空集合 = 清空该用户全部角色
     */
    private List<Long> roleIds;
}
