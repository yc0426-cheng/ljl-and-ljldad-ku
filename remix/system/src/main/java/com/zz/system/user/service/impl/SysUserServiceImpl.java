package com.zz.system.user.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zz.common.core.annotation.TraceStep;
import com.zz.common.core.exception.BizException;
import com.zz.common.core.pojo.LoginUserInfo;
import com.zz.common.redis.service.RedisService;
import com.zz.system.user.converter.SysUserConverter;
import com.zz.system.user.entity.SysUser;
import com.zz.system.user.enums.SysUserExceptionEnum;
import com.zz.system.user.pojo.dto.SysUserDTO;
import com.zz.system.user.pojo.param.SysUserParam;
import com.zz.system.user.pojo.vo.SysUserVO;
import com.zz.system.user.service.SysUserService;
import com.zz.system.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yangcheng
 * @description 针对表【sys_user(用户信息表)】的数据库操作Service实现
 * @createDate 2026-08-20 16:55:09
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    private final RedisService redisService;

    private final SysUserMapper sysUserMapper;

    private final SysUserConverter sysUserConverter;

    @Override
    @TraceStep(module = "system", callType = "service")
    public SysUser getUserInfoByAccount(String account) {
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        // 根据账号查询，账号查询有且只有一个
        qw.eq("account", account);
        return baseMapper.selectOne(qw);
    }

    @Override
    @TraceStep(module = "system", callType = "service", db = "learn", table = "sys_user")
    public void editError(Long userId) {
        SysUser sysUser = baseMapper.selectById(userId);
        if (sysUser.getPassErrorCount() + 1 > 3) {
            // 返回给前端 redis内增加一个5分钟的key-value 若是当前用户存在key-value时则显示具体可以登录时间
            redisService.set("login_locked", true, 5);
            throw new BizException(SysUserExceptionEnum.OUT_OF_ERROR_COUNT);
        }
        // 修改已错误次数
        sysUser.setPassErrorCount(sysUser.getPassErrorCount() + 1);
        // 更新数据
        baseMapper.updateById(sysUser);
    }

    @Override
    @TraceStep(module = "system", callType = "service", db = "learn", table = "sys_user")
    public void editLogin(Long userId) {
        SysUser sysUser = baseMapper.selectById(userId);
        sysUser.setPassErrorCount(0);
        sysUser.setLastLoginTime(DateTime.now());
        baseMapper.updateById(sysUser);
    }

    @Override
    @TraceStep(module = "system", callType = "service")
    public LoginUserInfo getLoginUserInfo(Long userId) {
        SysUser sysUser = baseMapper.selectById(userId);
        LoginUserInfo loginUserInfo = new LoginUserInfo();
        loginUserInfo.setUserId(userId);
        loginUserInfo.setAccount(sysUser.getAccount());
        loginUserInfo.setName(sysUser.getName());
        // TODO 头像存于 sys_user_misc 杂项表（建表语句见 data/sql/system/sys_user_misc.sql），
        //  待杂项表实体/Service 实现后，在此按 userId 查询并 setAvatar，随 LoginUserInfo 存入 redis
        return loginUserInfo;
    }

    @Override
    @TraceStep(module = "system", callType = "service", db = "learn", table = "sys_user")
    public void setLastLoginTime(Long userId) {
        SysUser sysUser = baseMapper.selectById(userId);
        if (sysUser == null) {
            return;
        }
        sysUser.setLastLoginTime(DateTime.now());
        baseMapper.updateById(sysUser);
    }

    @Override
    public List<SysUserVO> page() {
        return super.list()
                .stream()
                .map(sysUserConverter::entityToVO)
                .toList();
    }

    @Override
    public List<SysUserVO> getList(SysUserParam param) {
        // 条件查询：账号/姓名留空则不参与过滤，非空时模糊匹配
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        if (StrUtil.isNotBlank(param.getAccount())) {
            qw.like("account", param.getAccount());
        }
        if (StrUtil.isNotBlank(param.getName())) {
            qw.like("name", param.getName());
        }
        return super.list(qw)
                .stream()
                .map(sysUserConverter::entityToVO)
                .toList();
    }

    @Override
    public void add(SysUserDTO dto) {
        // 判断当前用户权限是否可以新增用户,目前只有管理员可以
        LoginUserInfo loginUserInfo = new LoginUserInfo();
        if (loginUserInfo.getUserId() != 1) {
            return;
        }

        // 构造对象并插入到表中
        SysUser user = sysUserConverter.dto2Entity(dto);
        super.save(user);
    }

    @Override
    public void edit(SysUserDTO dto) {
        sysUserMapper.edit(dto);
    }
}




