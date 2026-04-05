package com.example.cloud.storage.controller;

import com.example.cloud.storage.dto.DeductStorageRequest;
import com.example.cloud.storage.dto.StorageResponse;
import com.example.cloud.storage.service.StorageService;
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
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/deduct")
    public ResultVO<Void> deduct(@RequestBody @Valid DeductStorageRequest request) {
        storageService.deduct(request);
        return ResultVO.success();
    }

    @GetMapping("/list")
    public ResultVO<List<StorageResponse>> list() {
        return ResultVO.success(storageService.list());
    }

    @GetMapping("/health")
    public ResultVO<Map<String, String>> health() {
        return ResultVO.success(Map.of("service", "storage-service", "status", "UP"));
    }
}
