package com.zz.system.userRole.controller;

import com.zz.system.userRole.pojo.vo.SysUserRoleVO;
import com.zz.system.userRole.service.SysUserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p><b>系统服务-用户角色控制器</b></p>
 *
 * @author yangcheng
 * @since 2026/10/24 20:04
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SysUserRoleController {

    private final SysUserRoleService sysUserRoleService;

    /**
     * 绑定角色
     *
     * @param userId     用户id
     * @param roleIdList 角色id列表
     */
    @PostMapping("/sys/user/role/bind")
    public void bindRoles(@RequestBody Long userId,
                          @RequestBody List<Long> roleIdList) {
        sysUserRoleService.bindRole(userId, roleIdList);
    }

    /**
     * 查询列表
     *
     * @param userId 用户id
     * @return 用户绑定角色列表
     */
    @PostMapping("/sys/user/role/page")
    public List<SysUserRoleVO> page(@RequestBody Long userId) {
        return sysUserRoleService.page(userId);
    }
}
