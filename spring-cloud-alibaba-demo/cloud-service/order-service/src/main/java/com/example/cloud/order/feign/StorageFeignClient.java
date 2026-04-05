package com.example.cloud.order.feign;

import com.example.cloud.order.dto.DeductStorageRequest;
import com.example.common.constant.ServiceNameConstants;
import com.example.common.vo.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Queue;

@FeignClient(
        name = ServiceNameConstants.STORAGE_SERVICE,
        path = "/api/storage",
        fallbackFactory = StorageFeignFallbackFactory.class
)
public interface StorageFeignClient {

    @PostMapping("/deduct")
    ResultVO<Void> deduct(@RequestBody DeductStorageRequest request);
}
