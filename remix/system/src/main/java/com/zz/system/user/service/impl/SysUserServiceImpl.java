package com.zz.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zz.common.core.exception.BizException;
import com.zz.common.core.pojo.LoginUserInfo;
import com.zz.system.user.entity.SysUser;
import com.zz.system.user.enums.SysUserExceptionEnum;
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
        SysUser sysUser = baseMapper.selectById(userId);
        if (sysUser.getPassErrorCount() + 1 > 3){
            // todo 返回给前端 redis内增加一个5分钟的key-value 若是当前用户存在key-value时则显示具体可以登录时间
            throw new BizException(SysUserExceptionEnum.OUT_OF_ERROR_COUNT);
        }
        // 修改已错误次数
        sysUser.setPassErrorCount(sysUser.getPassErrorCount() + 1);
        // 更新数据
        baseMapper.updateById(sysUser);
    }

    @Override
    public void editLogin(Long userId) {
        SysUser sysUser = baseMapper.selectById(userId);
        sysUser.setPassErrorCount(0);
        baseMapper.updateById(sysUser);
    }

    @Override
    public LoginUserInfo getLoginUserInfo(Long userId) {
        SysUser sysUser = baseMapper.selectById(userId);
        LoginUserInfo loginUserInfo=new LoginUserInfo();
        loginUserInfo.setUserId(userId);
        loginUserInfo.setAccount(sysUser.getAccount());
        loginUserInfo.setName(sysUser.getName());
        return loginUserInfo;
    }
}




