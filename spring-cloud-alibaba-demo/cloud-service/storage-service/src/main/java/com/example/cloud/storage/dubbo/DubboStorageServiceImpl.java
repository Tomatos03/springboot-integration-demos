package com.example.cloud.storage.dubbo;

import com.example.common.dto.DeductStorageRequest;
import com.example.cloud.storage.service.StorageService;
import com.example.common.dubbo.DubboStorageService;
import com.example.common.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@RequiredArgsConstructor
public class DubboStorageServiceImpl implements DubboStorageService {

    private final StorageService storageService;

    @Override
    public ResultVO<Void> deduct(DeductStorageRequest request) {
        storageService.deduct(request);
        return ResultVO.success();
    }
}