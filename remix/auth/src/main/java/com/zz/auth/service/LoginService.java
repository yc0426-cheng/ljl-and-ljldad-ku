package com.zz.auth.service;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import com.zz.api.system.user.SysUserFeignClient;
import com.zz.api.system.user.dto.SysUserFeignDTO;
import com.zz.api.system.user.enums.SysUserStatusEnum;
import com.zz.auth.enums.LoginExceptionEnum;
import com.zz.auth.pojo.LoginDTO;
import com.zz.common.core.constant.RedisKeyConstant;
import com.zz.common.core.context.LoginUserHolder;
import com.zz.common.core.exception.BizException;
import com.zz.common.core.pojo.LoginUserInfo;
import com.zz.common.core.properties.JwtProperties;
import com.zz.common.redis.service.RedisService;
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
    // 远端调用接口
    private final SysUserFeignClient sysUserFeignClient;
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
        SysUserFeignDTO user = sysUserFeignClient.getUserInfoByAccount(account);

        // 判断用户是否被启用
        if (user.getStatus() != SysUserStatusEnum.ENABLE) {
            throw new BizException(LoginExceptionEnum.USER_LOCKED);
        }

        // BCrypt密码加密算法 不可逆 自带随机盐
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 密码错误，次数加一
        if (!encoder.matches(password, user.getPassword())) {
            sysUserFeignClient.editError(user.getUserId());
            throw new BizException(LoginExceptionEnum.PASSWORD_ERROR);
        }

        // 登录成功，重置密码错误次数
        sysUserFeignClient.editLogin(user.getUserId());

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
        LoginUserInfo userInfo = sysUserFeignClient.getLoginUserInfo(user.getUserId());

        // 补充token
        userInfo.setToken(token);

        // 放置token至redis 并设置过期时间
        redisService.set(RedisKeyConstant.TOKEN + token, userInfo, 86400);

        return token;
    }

    /**
     * 校验token是否有效
     * 有效标准：redis中存在 TOKEN:&lt;token&gt; 键（未过期），且不在登出黑名单中
     *
     * @param token 纯token（不含Bearer前缀，由controller剥离）
     * @return token对应的用户信息
     */
    public LoginUserInfo checkToken(String token) {
        // 空token直接无效
        if (StrUtil.isBlank(token)) {
            throw new BizException(LoginExceptionEnum.TOKEN_INVALID);
        }

        // 黑名单校验：已登出的token视为无效
        Boolean inBlackList = redisService.getRedisTemplate()
                .hasKey(RedisKeyConstant.BLACK_LIST_PREFIX + token);
        if (inBlackList) {
            throw new BizException(LoginExceptionEnum.TOKEN_INVALID);
        }

        // 从redis获取token对应的用户信息；取不到即已过期或不存在
        LoginUserInfo userInfo = redisService.get(RedisKeyConstant.TOKEN + token, LoginUserInfo.class);
        if (userInfo == null) {
            throw new BizException(LoginExceptionEnum.TOKEN_INVALID);
        }
        return userInfo;
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
