package org.alist.hub.configure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.alist.hub.bean.UserClaims;
import org.alist.hub.utils.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;


@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求路径
        String requestPath = request.getRequestURI();

        // 忽略 /open 开头和 /login 路径
        if (requestPath.startsWith("/open") || requestPath.equals("/login")) {
            return true;
        }
        // 校验 Authorization 字段
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !isValidAuthorization(authorizationHeader)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        return true;
    }

    private boolean isValidAuthorization(String authorizationHeader) {
        Optional<UserClaims> userClaims = JwtUtil.decodeJwt(authorizationHeader);
        return userClaims.map(claims -> !claims.isExpired()).orElseGet(() -> false);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // 在请求处理之后进行操作，如果有需要可以在这里进行处理
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 在请求处理完成后进行操作，如果有需要可以在这里进行处理
    }
}
