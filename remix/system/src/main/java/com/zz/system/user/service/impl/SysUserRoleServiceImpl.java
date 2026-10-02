package com.zz.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zz.system.user.converter.SysUserRoleConverter;
import com.zz.system.user.entity.SysUserRole;
import com.zz.system.user.pojo.vo.SysUserRoleVO;
import com.zz.system.user.service.SysUserRoleService;
import com.zz.system.user.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 针对表【sys_user_role】的数据库操作Service实现
 *
 * @author yangcheng
 * @since 2026-10-02 14:52:14
 */
@Service
@RequiredArgsConstructor
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole>
        implements SysUserRoleService {

    private final SysUserRoleConverter sysUserRoleConverter;

    /**
     * 全量分配角色：先删该用户全部旧关联，再批量写入新关联（同一事务内完成）
     *
     * @param userId  用户id
     * @param roleIds 角色id集合（自动去重、忽略 null 元素）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(Long userId, List<Long> roleIds) {
        if (userId == null) {
            return;
        }

        // 使用全量分配
        // 删除所有已分配的角色（本表无 del_flag，为物理删除）
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        super.remove(queryWrapper);

        // 插入所有分配角色；空集合只完成清空
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        // 去重 + 忽略 null：角色id转绑定记录；主键 userRoleId 由雪花算法自动生成
        Date now = new Date();
        List<SysUserRole> assignList = roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(roleId -> {
                    SysUserRole userRole = new SysUserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    userRole.setCreateTime(now);
                    return userRole;
                })
                .toList();
        super.saveBatch(assignList);
    }

    @Override
    public List<SysUserRoleVO> list(Long userId) {
        // 若是无查询用户的话，则是查询所有用户的所有角色
        if (userId == null) {
            return super.list().stream().map(sysUserRoleConverter::entityToVO).toList();
        }

        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        return super.list(queryWrapper).stream().map(sysUserRoleConverter::entityToVO).toList();
    }
}




