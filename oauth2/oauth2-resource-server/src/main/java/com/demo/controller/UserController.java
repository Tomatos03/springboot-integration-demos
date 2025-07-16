package com.demo.controller;

import com.demo.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : Tomatos
 * @date : 2025/7/16
 */
@RestController
public class UserController {
    @GetMapping("/userinfo")
    public User getUser() {
        return new User("Xiao Ming", "22354@qq.com", "www.baidu.com");
    }
}
