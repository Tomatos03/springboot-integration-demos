package com.example.common.dubbo;

import com.example.common.dto.DeductAccountRequest;
import com.example.common.vo.ResultVO;
import org.apache.dubbo.config.annotation.DubboService;

public interface DubboAccountService {
    
    ResultVO<Void> deduct(DeductAccountRequest request);
}