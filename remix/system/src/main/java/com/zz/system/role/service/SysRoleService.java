package com.zz.system.role.service;

import com.zz.system.role.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zz.system.role.pojo.dto.SysRoleDTO;
import com.zz.system.role.pojo.params.SysRoleParam;
import com.zz.system.role.pojo.vo.SysRoleVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
* 针对表【sys_role】的数据库操作Service
*
* @author yangcheng
* @since 2026-09-23 22:29:16
*/
public interface SysRoleService extends IService<SysRole> {

    /**
     * 查询角色
     *
     * @param param 查询参数
     * @return 角色列表
     */
    List<SysRoleVO> list(SysRoleParam param);

    /**
     * 新建角色
     *
     * @param dto 新增角色数据
     */
    void addRole(SysRoleDTO dto);

    /**
     * 修改角色
     *
     * @param dto 修改角色数据
     */
    void updateRole(SysRoleDTO dto);

    /**
     * 删除角色
     *
     * @param roleId 角色id
     */
    void deleteRole(Long roleId);

    /**
     * 导出角色信息
     *
     * @param response 请求体
     * @param list 导出数据
     */
    void exportRole(HttpServletResponse response, List<SysRole> list);
}
