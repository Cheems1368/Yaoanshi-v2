package com.sqlapk.medicine.framework.security.core.aop;

import com.sqlapk.medicine.framework.security.core.annotations.PreAuthenticated;
import com.sqlapk.medicine.framework.security.core.util.SecurityFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import javax.servlet.http.HttpServletRequest;

import static com.sqlapk.medicine.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;
import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.framework.web.core.util.WebFrameworkUtils.getRequest;

@Aspect
@Slf4j
public class PreAuthenticatedAspect {

    @Around("@annotation(preAuthenticated)")
    public Object around(ProceedingJoinPoint joinPoint, PreAuthenticated preAuthenticated) throws Throwable {
        HttpServletRequest request = getRequest();
        String requestURI = request != null ? request.getRequestURI() : null;
        
        // 对于 /app/** 路径，只检查是否登录，不进行权限检查
        // 因为 Spring Security 配置中 /app/** 已经配置了 permitAll()
        if (requestURI != null && requestURI.startsWith("/app/")) {
            // 检查用户是否已登录
            if (SecurityFrameworkUtils.getLoginUser() == null) {
                log.warn("[PreAuthenticatedAspect] /app/** 路径需要登录，但用户未登录: {}", requestURI);
                throw exception(UNAUTHORIZED);
            }
            // 已登录用户允许访问，直接放行
            log.debug("[PreAuthenticatedAspect] 允许已认证用户访问 /app/** 路径: {}", requestURI);
            return joinPoint.proceed();
        }
        
        // 其他路径：检查用户是否已登录
        if (SecurityFrameworkUtils.getLoginUser() == null) {
            log.warn("[PreAuthenticatedAspect] 用户未登录，拒绝访问: {}", requestURI);
            throw exception(UNAUTHORIZED);
        }
        
        // 其他路径正常处理
        return joinPoint.proceed();
    }

}
