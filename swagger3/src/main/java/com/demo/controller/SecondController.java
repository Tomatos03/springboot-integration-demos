package com.demo.controller;

import com.demo.dto.SecondDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : Tomatos
 * @date : 2025/7/9
 */
@Tag(name = "second模块", description = "模块相关描述")
@RestController
@RequestMapping("/second")
public class SecondController {
    @GetMapping("/method")
    public SecondDTO method0() {
        return new SecondDTO(-1, -1);
    }
}
