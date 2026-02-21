package com.ship.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;        // 添加这一行
    private String department;    // 添加这一行
    private String position;      // 添加这一行
    private String role;
    private String avatar;
    private LocalDateTime createdTime;
    private LocalDateTime lastLoginTime;
}