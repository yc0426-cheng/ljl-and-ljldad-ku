package com.zz.system.user.controller;

import com.zz.system.user.pojo.dto.SysUserRoleAssignDTO;
import com.zz.system.user.service.SysUserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p><b>系统服务-系统用户角色控制器</b></p>
 *
 * @author yangcheng
 * @since 2026/10/2 16:35
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SysUserRoleController {

    private final SysUserRoleService sysUserRoleService;

    /**
     * 为用户分配角色（全量分配：先清空旧角色再写入新角色）
     *
     * @param dto 请求体 {@code {"userId":1,"roleIds":[1,2,3]}}；roleIds 传空数组 = 清空全部角色
     */
    @PostMapping("/sys/user/role/assign")
    public void assignRoles(@RequestBody SysUserRoleAssignDTO dto) {
        sysUserRoleService.assign(dto.getUserId(), dto.getRoleIds());
    }
}
