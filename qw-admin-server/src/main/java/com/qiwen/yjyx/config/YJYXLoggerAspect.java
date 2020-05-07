package com.qiwen.yjyx.config;

import com.qiwen.base.config.log.DefaultLoggerAspectHandler;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 新增模块访问日志打印插件
 */
@Slf4j
@Aspect
@Component
public class YJYXLoggerAspect {

    private final DefaultLoggerAspectHandler handler = new DefaultLoggerAspectHandler(log);

    /**
     * 定义日志切入点
     */
    @Pointcut("execution(public * com.qiwen.yjyx.controller..*(..)) ")
    public void controllerAspect() { }

    @Around("controllerAspect()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        return handler.aroundHandler(joinPoint);
    }
}
