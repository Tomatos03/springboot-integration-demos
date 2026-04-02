package com.example.swagger3.controller.public;

import com.example.swagger3.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/info")
    public Result<String> info() {
        return new Result<>(200, "ok", "public info");
    }
}
