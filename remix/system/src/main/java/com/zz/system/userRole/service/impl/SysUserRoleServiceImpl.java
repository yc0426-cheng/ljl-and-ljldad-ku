package com.zz.system.userRole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zz.system.userRole.converter.SysUserRoleConverter;
import com.zz.system.userRole.entity.SysUserRole;
import com.zz.system.userRole.pojo.vo.SysUserRoleVO;
import com.zz.system.userRole.service.SysUserRoleService;
import com.zz.system.userRole.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 针对表【sys_user_role(用户角色绑定表)】的数据库操作Service实现
 *
 * @author yangcheng
 * @since 2026-10-24 20:02:28
 */
@Service
@RequiredArgsConstructor
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole>
        implements SysUserRoleService {

    private final SysUserRoleConverter sysUserRoleConverter;

    @Override
    public void bindRole(Long userId, List<Long> roleIdList) {
        // 全量修改
        // 删除本来的绑定
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        super.remove(queryWrapper);

        // 绑定新增的
        List<SysUserRole> sysUserRoleList = new ArrayList<>();
        for (Long roleId : roleIdList) {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(roleId);
            sysUserRoleList.add(sysUserRole);
        }
        super.saveBatch(sysUserRoleList.stream().distinct().toList());
    }

    @Override
    public List<SysUserRoleVO> page(Long userId) {
        // 查询全部
        if (userId == null) {
            return super.list()
                    .stream()
                    .map(sysUserRoleConverter::entityToVO)
                    .toList();
        }


        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> list = super.list(queryWrapper);
        return list.stream()
                .map(sysUserRoleConverter::entityToVO)
                .toList();
    }
}




