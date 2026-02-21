package com.ship.controller;

import com.ship.entity.User;
import com.ship.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        User user = userService.login(username, password);

        Map<String, Object> result = new HashMap<>();
        if (user != null) {
            String token = userService.generateToken(user);
            result.put("code", 200);
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("role", user.getRole());
            result.put("message", "登录成功");
        } else {
            result.put("code", 401);
            result.put("message", "用户名或密码错误");
        }

        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        if (userService.register(user)) {
            result.put("code", 200);
            result.put("message", "注册成功");
        } else {
            result.put("code", 400);
            result.put("message", "用户名已存在");
        }

        return result;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader("Authorization") String token) {
        userService.logout(token);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "注销成功");

        return result;
    }

    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestHeader("Authorization") String token) {
        User user = userService.validateToken(token);

        Map<String, Object> result = new HashMap<>();
        if (user != null) {
            result.put("code", 200);
            result.put("data", user);
        } else {
            result.put("code", 401);
            result.put("message", "token无效");
        }

        return result;
    }
}
