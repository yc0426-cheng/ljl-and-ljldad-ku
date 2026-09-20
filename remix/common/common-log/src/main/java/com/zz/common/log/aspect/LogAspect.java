package com.zz.common.log.aspect;

import com.zz.common.log.annotation.OperationLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * <p><b>日志工具类-log记录切面</b></p>
 *
 * @author yangcheng
 * @since 2026/9/20 20:25
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    // 拦截所有加入注解 @OperationLog 的方法
    @Pointcut("@annotation(com.zz.common.log.annotation.OperationLog)")
    public void logPointCut(){}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();

        // 获取连接点信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        OperationLog operateLog = method.getAnnotation(OperationLog.class);

        OperationLog.LogModel model = operateLog.model();

        // 获取request
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 打印请求基础信息
        String url = request.getRequestURI();
        String httpMethod = request.getMethod();
        String ip = request.getRemoteAddr();
        String className = point.getTarget().getClass().getName();
        String methodName = signature.getName();
        Object[] args = point.getArgs();

        log.info("=====【操作日志开始】=====");
        log.info("操作描述：{}", operateLog != null ? operateLog.value() : "");
        log.info("请求来自{}", model);
        log.info("请求IP：{}", ip);
        log.info("请求地址：{}", url);
        log.info("请求方式：{}", httpMethod);
        log.info("调用类.方法：{}.{}", className, methodName);
        log.info("请求入参：{}", Arrays.toString(args));

        Object result;
        try {
            // 执行目标方法
            result = point.proceed();
            long cost = System.currentTimeMillis() - start;
            log.info("返回结果：{}", result);
            log.info("执行耗时：{} ms", cost);
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - start;
            log.error("方法异常，耗时:{}ms，异常信息：", cost, e);
            // 抛出异常，让全局异常处理器捕获
            throw e;
        }
        log.info("=====【操作日志结束】=====\n");
        return result;
    }
}
