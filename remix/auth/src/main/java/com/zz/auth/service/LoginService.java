package com.zz.auth.service;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import com.zz.auth.enums.LoginExceptionEnum;
import com.zz.auth.pojo.LoginDTO;
import com.zz.common.core.constant.RedisKeyConstant;
import com.zz.common.core.context.LoginUserHolder;
import com.zz.common.core.exception.BizException;
import com.zz.common.core.pojo.LoginUserInfo;
import com.zz.common.redis.service.RedisService;
import com.zz.system.user.entity.SysUser;
import com.zz.system.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

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

    private final RedisService redisService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expire-hours}")
    private long expireHours;

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

        // BCrypt密码加密算法 不可逆 自带随机盐
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 密码错误，次数加一
        if (!encoder.matches(password, user.getPassword())) {
            sysUserService.editError(user.getUserId());
            throw new BizException(LoginExceptionEnum.PASSWORD_ERROR);
        }

        // 登录成功，重置密码错误次数
        sysUserService.editLogin(user.getUserId());

        // 生成token（带签名密钥，防止伪造）
        String token = JWT.create()
                // 设置自定义载荷
                .setPayload("userId", user.getUserId())
                // 设置签发时间 (iat)
                .setIssuedAt(DateUtil.date())
                // 设置过期时间 (exp)，默认24小时后
                .setExpiresAt(DateUtil.offsetHour(DateUtil.date(), (int) expireHours))
                // 设置签名密钥并签名
                .setKey(jwtSecret.getBytes(StandardCharsets.UTF_8))
                .sign();

        // 创建用户登录信息
        LoginUserInfo userInfo = sysUserService.getLoginUserInfo(user.getUserId());

        // 补充token
        userInfo.setToken(token);

        // 放置token至redis，过期时间与JWT保持一致（单位：秒）
        redisService.set(RedisKeyConstant.TOKEN + token, userInfo, expireHours * 3600);

        return token;
    }

    /**
     * 登出
     */
    public void logout() {
        try {
            // 获取当前登录的用户信息（由 TokenAuthFilter 写入 ThreadLocal）
            LoginUserInfo userInfo = LoginUserHolder.get();
            if (userInfo == null || StrUtil.isBlank(userInfo.getToken())) {
                throw new BizException(LoginExceptionEnum.NOT_LOGIN);
            }
            String token = userInfo.getToken();

            // 获取剩余过期时间（单位：秒）
            Long ttl = redisService.getRedisTemplate().getExpire(RedisKeyConstant.TOKEN + token);

            // 删除token内的用户信息
            redisService.delete(RedisKeyConstant.TOKEN + token);

            // 将其加入黑名单，剩余有效期再续1秒，避免刚登出又被校验通过
            if (ttl != null && ttl > 0) {
                redisService.set(RedisKeyConstant.BLACK_LIST_PREFIX + token, userInfo, ttl + 1);
            }
        } finally {
            // 清理线程上下文，防止内存泄漏
            LoginUserHolder.remove();
        }
    }
}
