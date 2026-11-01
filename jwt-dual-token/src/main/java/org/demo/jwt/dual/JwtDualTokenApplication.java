package org.demo.jwt.dual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 双 Token（Access Token + Refresh Token）方案演示入口。
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
@SpringBootApplication
public class JwtDualTokenApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtDualTokenApplication.class, args);
    }
}
