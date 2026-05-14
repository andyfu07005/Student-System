package com.sims.config;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtFilter extends BasicHttpAuthenticationFilter {

    /** 登录接口放行 */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (isLoginRequest(request)) return true;
        return executeLogin(request, response);
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String jwt = authHeader.substring(7);
        JwtToken token = new JwtToken(jwt);
        try {
            getSubject(request, response).login(token);
            return true;
        } catch (AuthenticationException e) {
            log.debug("JWT auth failed: {}", e.getMessage());
            return false;
        }
    }

    /** 未登录时返回 401，不走重定向 */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setStatus(HttpStatus.UNAUTHORIZED.value());
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"code\":401,\"message\":\"请先登录\",\"data\":null}");
        return false;
    }

    private boolean isLoginRequest(ServletRequest request) {
        String path = ((HttpServletRequest) request).getRequestURI();
        return path.contains("/api/auth/login") || path.contains("/api/auth/register");
    }
}
