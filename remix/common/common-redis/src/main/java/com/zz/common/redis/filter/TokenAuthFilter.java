package com.zz.common.redis.filter;

import cn.hutool.core.util.StrUtil;
import com.zz.common.core.constant.RedisKeyConstant;
import com.zz.common.core.context.LoginUserHolder;
import com.zz.common.core.pojo.LoginUserInfo;
import com.zz.common.redis.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * <p><b>通用工具-登录令牌校验过滤器</b></p>
 *
 * <p>从请求头读取令牌，校验 Redis 中是否存在对应的登录态；
 * 通过则把用户信息写入线程上下文（LoginUserHolder），否则返回 401。</p>
 *
 * <p>放行名单：登录、健康检查、监控端点等无需登录即可访问的路径。</p>
 *
 * @author yangcheng
 * @since 2026/8/22
 */
@Component
@RequiredArgsConstructor
public class TokenAuthFilter extends OncePerRequestFilter {

    private final RedisService redisService;

    /**
     * 无需登录的路径前缀
     */
    private static final String[] WHITE_LIST = {
            "/auth/login",
            "/health",
            "/actuator",
            "/error",
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (StrUtil.isBlank(path)) {
            return false;
        }
        for (String prefix : WHITE_LIST) {
            if (path.equals(prefix) || path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            LoginUserInfo userInfo = null;
            if (StrUtil.isNotBlank(token)) {
                userInfo = redisService.get(RedisKeyConstant.TOKEN + token, LoginUserInfo.class);
            }
            if (userInfo == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
                return;
            }
            LoginUserHolder.set(userInfo);
            filterChain.doFilter(request, response);
        } finally {
            LoginUserHolder.remove();
        }
    }

    /**
     * 从请求头解析令牌：优先 Authorization: Bearer，其次 X-Token
     */
    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return request.getHeader("X-Token");
    }
}
