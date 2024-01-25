package org.alist.hub.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.alist.hub.bean.UserClaims;
import org.alist.hub.exception.ServiceException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class RequestContextUtil {

    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getRequest();
    }

    public static UserClaims getUserClaims() {
        Optional<UserClaims> userClaims = JwtUtil.decodeJwt(getCurrentRequest().getHeader("Authorization"));
        if (userClaims.isEmpty()) {
            throw new ServiceException("获取用户信息失败");
        }
        return userClaims.get();
    }
}
