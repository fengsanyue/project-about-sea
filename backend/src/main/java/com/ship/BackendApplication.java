package com.ship;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
        System.out.println("========================================");
        System.out.println("船舶多智能体系统后端启动成功！");
        System.out.println("访问地址: http://localhost:8080/api/ship/test");
        System.out.println("========================================");
    }
}