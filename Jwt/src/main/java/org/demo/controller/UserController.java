package org.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.manager.jwt.TokenManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @author : Tomatos
 * @date : 2025/7/22
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final TokenManager tokenManager;

    @GetMapping("/accessResource")
    public String hello() {
        log.info("访问受保护资源");
        return "Hello World!";
    }

    @GetMapping("/getToken")
    public String getToken() {
        log.info("获取令牌");
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("userId", 1);
        return tokenManager.generatorToken(payload);
    }
}