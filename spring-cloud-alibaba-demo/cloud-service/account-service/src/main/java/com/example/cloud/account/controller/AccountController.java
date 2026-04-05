package com.example.cloud.account.controller;

import com.example.cloud.account.dto.AccountResponse;
import com.example.cloud.account.dto.DeductAccountRequest;
import com.example.cloud.account.service.AccountService;
import com.example.common.vo.ResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/deduct")
    public ResultVO<Void> deduct(@RequestBody @Valid DeductAccountRequest request) {
        accountService.deduct(request);
        return ResultVO.success();
    }

    @GetMapping("/list")
    public ResultVO<List<AccountResponse>> list() {
        return ResultVO.success(accountService.list());
    }

    @GetMapping("/health")
    public ResultVO<Map<String, String>> health() {
        return ResultVO.success(Map.of("service", "account-service", "status", "UP"));
    }
}
