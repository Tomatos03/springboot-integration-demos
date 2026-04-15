package com.example.common.dubbo;

import com.example.common.dto.DeductStorageRequest;
import com.example.common.vo.ResultVO;
import org.apache.dubbo.config.annotation.DubboService;

public interface DubboStorageService {
    
    ResultVO<Void> deduct(DeductStorageRequest request);
}