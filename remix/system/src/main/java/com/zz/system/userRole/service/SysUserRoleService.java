package com.zz.system.userRole.service;

import com.zz.system.userRole.entity.SysUserRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zz.system.userRole.pojo.vo.SysUserRoleVO;

import java.util.List;

/**
 * 针对表【sys_user_role(用户角色绑定表)】的数据库操作Service
 *
 * @author yangcheng
 * @since 2026-10-24 20:02:28
 */
public interface SysUserRoleService extends IService<SysUserRole> {

    /**
     * 绑定角色
     *
     * @param userId     用户id
     * @param roleIdList 角色id列表
     */
    void bindRole(Long userId, List<Long> roleIdList);

    /**
     * 查询列表
     *
     * @param userId 用户id
     * @return 用户绑定角色列表
     */
    List<SysUserRoleVO> page(Long userId);
}
