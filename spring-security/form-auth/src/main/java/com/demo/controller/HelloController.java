package com.demo.controller;

/**
 * @author : Tomatos
 * @date : 2025/7/13
 */

import com.demo.entity.TUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * @author : Tomatos
 * @date : 2025/7/13
 */
@RestController
public class HelloController {
    @PreAuthorize("hasRole('saler')")
    @GetMapping("/hello")
    public String hello() {
        return "Hello";
    }

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/welcome")
    public Principal welcome(Principal principal) {
        return principal;
    }

    @GetMapping("/welcome0")
    public Principal welcome(Authentication authentication) {
        return authentication;
    }

    @GetMapping("/welcome1")
    public TUser welcome() {
        return (TUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
