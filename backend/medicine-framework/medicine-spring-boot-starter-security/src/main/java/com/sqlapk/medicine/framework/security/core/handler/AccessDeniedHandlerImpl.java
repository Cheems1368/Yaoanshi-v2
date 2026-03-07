package com.sqlapk.medicine.framework.security.core.handler;

import com.sqlapk.medicine.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.util.servlet.ServletUtils;
import com.sqlapk.medicine.framework.security.core.util.SecurityFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.ExceptionTranslationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.sqlapk.medicine.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;

/**
 * 访问一个需要认证的 URL 资源，已经认证（登录）但是没有权限的情况下，返回 {@link GlobalErrorCodeConstants#FORBIDDEN} 错误码。
 *
 * 补充：Spring Security 通过 {@link ExceptionTranslationFilter#handleAccessDeniedException(HttpServletRequest, HttpServletResponse, FilterChain, AccessDeniedException)} 方法，调用当前类
 *
 * @author 芋道源码
 */
@Slf4j
@SuppressWarnings("JavadocReference")
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        
        // 对于 /app/** 路径的特殊处理
        if (requestURI != null && requestURI.startsWith("/app/")) {
            if (userId != null) {
                log.error("[handle][访问 /app/** 路径 URL({}) 时，用户({}) 已认证但触发权限拒绝。这不应该发生，因为 /app/** 配置了 permitAll()。可能是方法级权限检查导致的。]", requestURI, userId);
                log.error("[handle] AccessDeniedException 详情: {}", e.getMessage(), e);
                String errorMessage = String.format(
                    "没有该操作权限（用户ID: %d, 路径: %s）。请检查：1) Controller方法是否有 @PreAuthorize 或其他权限注解；2) 用户是否有正确的角色；3) 方法级权限配置是否正确。注意：/app/** 路径应该允许所有已认证用户访问。",
                    userId, requestURI
                );
                ServletUtils.writeJSON(response, CommonResult.error(FORBIDDEN.getCode(), errorMessage));
                return;
            }
        }
        
        // 打印 warn 的原因是，不定期合并 warn，看看有没恶意破坏
        log.warn("[handle][访问 URL({}) 时，用户({}) 权限不够] {}", requestURI, userId, e.getMessage());
        // 返回 403
        ServletUtils.writeJSON(response, CommonResult.error(FORBIDDEN));
    }

}
