package com.ship.config;

import com.ship.entity.User;
import com.ship.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Autowired
    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    //@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行登录和注册接口
        String path = request.getRequestURI();
        if (path.contains("/api/auth/login") || path.contains("/api/auth/register") || path.contains("/api/ship/test")) {
            return true;
        }

        // 验证token
        String token = request.getHeader("Authorization");
        System.out.println("拦截器收到token: " + token);

        if (token != null && !token.isEmpty()) {
            User user = userService.validateToken(token);
            if (user != null) {
                System.out.println("验证通过，用户: " + user.getUsername());
                request.setAttribute("currentUser", user);
                return true;
            } else {
                System.out.println("token无效: " + token);
            }
        } else {
            System.out.println("没有提供token");
        }

        // 未授权
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
        return false;
    }
}