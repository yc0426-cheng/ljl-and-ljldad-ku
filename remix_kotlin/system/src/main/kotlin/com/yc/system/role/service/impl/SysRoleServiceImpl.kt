package com.yc.system.role.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.yc.common.core.context.LoginUserHolder
import com.yc.common.core.exception.BaseException
import com.yc.system.role.converter.SysRoleConverter
import com.yc.system.role.entity.SysRole
import com.yc.system.role.enums.SysRoleExceptionEnum
import com.yc.system.role.mapper.SysRoleMapper
import com.yc.system.role.pojo.bo.SysRoleBO
import com.yc.system.role.pojo.dto.SysRoleDTO
import com.yc.system.role.pojo.param.SysRoleParam
import com.yc.system.role.pojo.vo.SysRoleVO
import com.yc.system.role.service.SysRoleService
import jakarta.servlet.http.HttpServletResponse
import org.apache.fesod.sheet.FesodSheet
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.*

/**
 * 针对表【sys_role】的数据库操作Service实现
 *
 * @author yangcheng
 * @since 2026-11-19 22:29:16
 */
@Service
class SysRoleServiceImpl(
    private val sysRoleConverter: SysRoleConverter
) : ServiceImpl<SysRoleMapper, SysRole>(), SysRoleService {

    override fun list(param: SysRoleParam): List<SysRoleVO> {
        val qw = QueryWrapper<SysRole>()
            .eq(param.roleName != null, "role_name", param.roleName)
            .eq(param.enabled != null, "enabled", param.enabled)

        return super<ServiceImpl>.list(qw).stream().map { sysRoleConverter.entityToVO(it) }.toList()
    }

    override fun addRole(dto: SysRoleDTO) {
        val roleNameList = super<ServiceImpl>.list().stream().map(SysRole::roleName).toList()
        if (roleNameList.contains(dto.roleName)) {
            throw BaseException.BizException(SysRoleExceptionEnum.ROLE_NAME_IS_ALREADY_EXISTS)
        }

        val role = SysRole().apply {
            roleId = 0 // 取0 mybatis plus 会自动忽略,使用雪花算法
            roleName = dto.roleName
            enabled = 1
            delFlag = 0
        }

        role.updateUser = LoginUserHolder.get()?.userId
        role.updateTime = Date()

        super<ServiceImpl>.save(role)
    }

    override fun updateRole(dto: SysRoleDTO) {
        val updateWrapper = UpdateWrapper<SysRole>()
        updateWrapper.eq(dto.roleName.isNotBlank(), "role_name", dto.roleName)
            .eq("enabled", dto.enabled)

        super<ServiceImpl>.update(updateWrapper)
    }

    override fun deleteRole(roleId: Long) {
        super<ServiceImpl>.removeById(roleId)
    }

    override fun exportRole(response: HttpServletResponse, list: List<SysRole>) {
        val os = response.outputStream
        try {
            FesodSheet.write(os, SysRoleBO::class.java)
                .sheet("用户角色列表")
                .doWrite { list }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}