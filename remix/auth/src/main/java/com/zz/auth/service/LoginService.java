package com.zz.auth.service;


import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import com.zz.auth.enums.LoginExceptionEnum;
import com.zz.auth.pojo.LoginDTO;
import com.zz.common.core.constant.RedisKeyConstant;
import com.zz.common.core.context.LoginUserHolder;
import com.zz.common.core.exception.BizException;
import com.zz.common.core.pojo.LoginUserInfo;
import com.zz.common.core.properties.JwtProperties;
import com.zz.common.redis.service.RedisService;
import com.zz.system.user.entity.SysUser;
import com.zz.system.user.enums.SysUserStatusEnum;
import com.zz.system.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * <p><b>认证服务-service服务类</b></p>
 *
 * @author yangcheng
 * @since 2026/8/20 10:45
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {
    // todo 修改为api模块远程调用获取用户信息
    private final SysUserService sysUserService;
    // redis服务
    private final RedisService redisService;
    // jwt配置
    private final JwtProperties jwtProperties;

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
        if (user.getStatus() != SysUserStatusEnum.ENABLE) {
            throw new BizException(LoginExceptionEnum.USER_LOCKED);
        }

        // BCrypt密码加密算法 不可逆 自带随机盐
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 密码错误，次数加一
        if (!encoder.matches(password, user.getPassword())) {
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
                .setKey(jwtProperties.getSecret().getBytes())
                // 签名并生成 Token
                .sign();

        // 创建用户登录信息
        LoginUserInfo userInfo = sysUserService.getLoginUserInfo(user.getUserId());

        // 补充token
        userInfo.setToken(token);

        // 放置token至redis 并设置过期时间
        redisService.set(RedisKeyConstant.TOKEN + token, userInfo, 86400);

        return token;
    }

    /**
     * 登出
     */
    public void logout() {
        // 获取当前登录的用户信息
        LoginUserInfo userInfo = LoginUserHolder.get();
        String token = userInfo.getToken();

        // 获取剩余过期时间
        Long ttl = redisService.getRedisTemplate().getExpire(token);

        // 删除token内的用户信息
        redisService.delete(RedisKeyConstant.TOKEN + token);

        // 将其添加黑名单常量池
        redisService.set(RedisKeyConstant.BLACK_LIST_PREFIX + token, userInfo, ttl + 1);
    }
}
