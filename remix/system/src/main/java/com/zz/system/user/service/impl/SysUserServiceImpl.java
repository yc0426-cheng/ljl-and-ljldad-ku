package com.zz.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zz.common.core.pojo.LoginUserInfo;
import com.zz.system.user.entity.SysUser;
import com.zz.system.user.service.SysUserService;
import com.zz.system.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
 * @author yangcheng
 * @description 针对表【sys_user(用户信息表)】的数据库操作Service实现
 * @createDate 2026-08-20 16:55:09
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    @Override
    public SysUser getUserInfoByAccount(String account) {
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        // 根据账号查询，账号查询有且只有一个
        qw.eq("account", account);
        return baseMapper.selectOne(qw);
    }

    @Override
    public void editError(Long userId) {

    }

    @Override
    public void editLogin(Long userId) {

    }

    @Override
    public LoginUserInfo getLoginUserInfo(Long userId) {
        return null;
    }
}




