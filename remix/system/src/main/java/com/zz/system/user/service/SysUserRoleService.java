package com.zz.system.user.service;

import com.zz.system.user.entity.SysUserRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zz.system.user.pojo.vo.SysUserRoleVO;

import java.util.List;

/**
* 针对表【sys_user_role】的数据库操作Service
*
* @author yangcheng
* @since 2026-10-02 14:52:14
*/
public interface SysUserRoleService extends IService<SysUserRole> {

    /**
     * 为用户分配角色
     *
     * @param userId 用户id
     * @param roleIds 角色集合
     */
    void assign(Long userId, List<Long> roleIds);

    /**
     * 查询选中用户所有角色
     *
     * @param userId 用户id
     * @return 选中用户角色列表
     */
    List<SysUserRoleVO> list(Long userId);
}
