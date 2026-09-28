package com.zz.system.role.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zz.common.core.context.LoginUserHolder;
import com.zz.common.core.exception.BizException;
import com.zz.system.role.convert.SysRoleConverter;
import com.zz.system.role.entity.SysRole;
import com.zz.system.role.enums.SysRoleExceptionEnum;
import com.zz.system.role.pojo.bo.SysRoleBO;
import com.zz.system.role.pojo.dto.SysRoleDTO;
import com.zz.system.role.pojo.params.SysRoleParam;
import com.zz.system.role.pojo.vo.SysRoleVO;
import com.zz.system.role.service.SysRoleService;
import com.zz.system.role.mapper.SysRoleMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.fesod.sheet.FesodSheet;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 针对表【sys_role】的数据库操作Service实现
 *
 * @author yangcheng
 * @since 2026-09-23 22:29:16
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {

    private final SysRoleConverter sysRoleConverter;

    @Override
    public List<SysRoleVO> list(SysRoleParam param) {
        // 没有查询条件则是全量查询
        if (param == null) {
            return super.list().stream().map(sysRoleConverter::entityToVO).toList();
        }

        // 带查询条件
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(param.getRoleName() != null, SysRole::getRoleName, param.getRoleName())
                .eq(param.getEnabled() != null, SysRole::getEnabled, param.getEnabled());

        return super.list(queryWrapper).stream().map(sysRoleConverter::entityToVO).toList();
    }

    @Override
    public void addRole(SysRoleDTO dto) {
        SysRole role = new SysRole();

        // 判断是否角色名称已经存在
        List<String> roleNameList = super.list().stream().map(SysRole::getRoleName).toList();
        if (roleNameList.contains(dto.getRoleName())) {
            throw new BizException(SysRoleExceptionEnum.ROLE_NAME_IS_ALREADY_EXISTS);
        }

        role.setRoleName(dto.getRoleName());
        role.setEnabled(1); //默认为1
        role.setDelFlag(0); //默认为0
        role.setUpdateUser(LoginUserHolder.get().getUserId());
        role.setUpdateTime(DateTime.now());
        super.save(role);
    }

    @Override
    public void updateRole(SysRoleDTO dto) {
        // 没有参数不更新，直接抛异常
        if (dto == null) {
            throw new BizException(SysRoleExceptionEnum.IS_NOT_EDIT);
        }

        UpdateWrapper<SysRole> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(StrUtil.isNotBlank(dto.getRoleName()), "role_name", dto.getRoleName())
                .eq(dto.getEnabled() != null , "enabled", dto.getEnabled());

        super.update(updateWrapper);
    }

    @Override
    public void deleteRole(Long roleId) {
        super.removeById(roleId);
    }

    @Override
    public void exportRole(HttpServletResponse response, List<SysRole> list) {
        try (OutputStream os = response.getOutputStream()) {
            FesodSheet.write(os, SysRoleBO.class)
                    .sheet("用户角色列表")
                    .doWrite(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}




