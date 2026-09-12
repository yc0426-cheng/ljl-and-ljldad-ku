package com.zz.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zz.common.core.pojo.LoginUserInfo;
import com.zz.system.user.entity.SysUser;
import com.zz.system.user.pojo.dto.SysUserDTO;
import com.zz.system.user.pojo.param.SysUserParam;
import com.zz.system.user.pojo.vo.SysUserVO;

import java.util.List;

/**
 * @author yangcheng
 * @description 针对表【sys_user(用户信息表)】的数据库操作Service
 * @createDate 2026-08-20 16:55:09
 */
public interface SysUserService extends IService<SysUser> {
    /**
     * 根据账号获取用户信息
     *
     * @param account 账号
     * @return 用户信息
     */
    SysUser getUserInfoByAccount(String account);

    /**
     * 登录密码错误
     *
     * @param userId 用户id
     */
    void editError(Long userId);

    /**
     * 登录重置错误次数
     *
     * @param userId 用户id
     */
    void editLogin(Long userId);

    /**
     * 创建用户登录信息
     *
     * @param userId 用户id
     * @return 用户登录信息
     */
    LoginUserInfo getLoginUserInfo(Long userId);

    /**
     * 更新最后登录时间（登出时由 auth 经 feign 调用）
     *
     * @param userId 用户id
     */
    void setLastLoginTime(Long userId);

    /**
     * 分页用户信息
     *
     * @return 用户列表
     */
    List<SysUserVO> page();

    /**
     * 获取用户信息列表
     *
     * @return 用户列表
     */
    List<SysUserVO> getList(SysUserParam param);

    /**
     * 新增用户
     *
     * @param dto 新增用户数据
     */
    void add(SysUserDTO dto);

    /**
     * 修改用户
     *
     * @param dto 修改用户信息
     */
    void edit(SysUserDTO dto);
}
