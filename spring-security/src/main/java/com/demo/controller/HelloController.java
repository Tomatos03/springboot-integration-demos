package com.demo.controller;

/**
 * @author : Tomatos
 * @date : 2025/7/13
 */
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : Tomatos
 * @date : 2025/7/13
 */
@RestController
@RequestMapping("demo")
public class HelloController {
    @RequestMapping("/index")
    public String index(){
        return "Hello World!";
    }

    @GetMapping("/hello")
    public String Hello() {
        return "Hello Spring Security";
    }
}
