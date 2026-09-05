package com.zz.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zz.common.core.pojo.LoginUserInfo;
import com.zz.system.user.entity.SysUser;

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
     * 写入操作日志
     */
    void writeUserLog();
}
