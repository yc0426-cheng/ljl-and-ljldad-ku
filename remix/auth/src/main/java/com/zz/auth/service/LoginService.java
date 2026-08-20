package com.zz.auth.service;


import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import com.zz.auth.enums.LoginExceptionEnum;
import com.zz.auth.pojo.LoginDTO;
import com.zz.common.core.exception.BizException;
import com.zz.common.core.pojo.LoginUserInfo;
import com.zz.system.user.entity.SysUser;
import com.zz.system.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p><b>认证服务-service服务类</b></p>
 *
 * @author yangcheng
 * @since 2026/8/20 10:45
 */
@Service
@RequiredArgsConstructor
public class LoginService {
    // todo 修改为api模块远程调用获取用户信息
    private final SysUserService sysUserService;



    /**
     * 登录
     *
     * @param loginDTO 登录的账户和密码
     * @return token
     */
    public String login(LoginDTO loginDTO) {
        String account = loginDTO.getAccount();
        String password = loginDTO.getPassword();
        // 获取用户信息
        SysUser user = sysUserService.getUserInfoByAccount(account);
        // 判断用户是否被启用
        if (user.getStatus().getValue() != 1) {
            throw new BizException(LoginExceptionEnum.NOT_ENABLE);
        }

        //todo 密码加密算法

        // 密码错误，次数加一
        if (!password.equals(user.getPassword())) {
            sysUserService.editError(user.getUserId());
            throw new BizException(LoginExceptionEnum.PASSWORD_ERROR);
        }

        // 登录成功，重置密码错误次数
        sysUserService.editLogin(user.getUserId());

        // 生成token
        String token = JWT.create()
                // 设置自定义载荷
                .setPayload("userId", user.getUserId())
                // 设置签发时间 (iat)
                .setIssuedAt(DateUtil.date())
                // 设置过期时间 (exp)，半个小时后
                .setExpiresAt(DateUtil.offsetHour(DateUtil.date(), 24))
                // 签名并生成 Token
                .sign();

        // 创建用户登录信息
        LoginUserInfo userInfo=sysUserService.getLoginUserInfo(user.getUserId());

        // 补充token
        userInfo.setToken(token);

        //todo 放置token至redis 并设置过期时间

        return token;
    }
}
