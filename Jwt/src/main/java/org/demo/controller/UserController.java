package org.demo.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.demo.pojo.Payload;
import org.demo.utils.JwtUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : Tomatos
 * @date : 2025/7/22
 */
@Slf4j
@RestController
public class UserController {
    @GetMapping("/hello")
    public String hello() {
        log.info("成功访问hello");
        return "Hello World!";
    }

    @GetMapping("/token")
    public String getToken(HttpServletResponse response) {
        log.info("尝试获取令牌");
        Payload<String> payload = new Payload<>();
        payload.addClaim("userId", "1");
        // payload添加其他信息...
        response.addCookie(new Cookie("token", JwtUtil.createJws(1L * 60 * 60 * 24, "aYyXSHX2tcBhZFNH6H9XAAXpk7tZAQmLtwserrmRk98=", payload)));
        return "获取到token";
    }
}
