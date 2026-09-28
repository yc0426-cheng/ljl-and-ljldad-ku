package com.zz.system.role.controller;

import com.zz.system.role.entity.SysRole;
import com.zz.system.role.pojo.dto.SysRoleDTO;
import com.zz.system.role.pojo.params.SysRoleParam;
import com.zz.system.role.pojo.vo.SysRoleVO;
import com.zz.system.role.service.SysRoleService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p><b>系统服务-角色控制器</b></p>
 *
 * @author yangcheng
 * @since 2026/9/28 19:27
 */
@Slf4j
@RestController("/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    /**
     * 查询角色
     *
     * @param param 查询参数
     * @return 角色列表
     */
    @PostMapping("/list")
    List<SysRoleVO> list(@RequestBody SysRoleParam param) {
        return sysRoleService.list(param);
    }

    /**
     * 新建角色
     *
     * @param dto 新增角色数据
     */
    @PostMapping("/add")
    void addRole(@RequestBody SysRoleDTO dto) {
        sysRoleService.addRole(dto);
    }

    /**
     * 修改角色
     *
     * @param dto 修改角色数据
     */
    @PostMapping("/update")
    void updateRole(@RequestBody SysRoleDTO dto) {
        sysRoleService.updateRole(dto);
    }

    /**
     * 删除角色
     *
     * @param roleId 角色id
     */
    @PostMapping("/delete")
    void deleteRole(@RequestBody Long roleId) {
        sysRoleService.deleteRole(roleId);
    }

    /**
     * 导出角色信息
     *
     * @param response 请求体
     * @param list     导出数据
     */
    @PostMapping("/export")
    void exportRole(HttpServletResponse response,
                    @RequestBody List<SysRole> list) {
        sysRoleService.exportRole(response, list);
    }
}
