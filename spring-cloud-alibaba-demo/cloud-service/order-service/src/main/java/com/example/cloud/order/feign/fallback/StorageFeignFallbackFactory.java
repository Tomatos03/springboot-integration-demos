package com.example.cloud.order.feign.fallback;

import com.example.common.dto.DeductStorageRequest;
import com.example.cloud.order.feign.StorageFeignClient;
import com.example.common.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StorageFeignFallbackFactory implements FallbackFactory<StorageFeignClient> {

    @Override
    public StorageFeignClient create(Throwable cause) {
        return request -> fallbackDeduct(request, cause);
    }

    private ResultVO<Void> fallbackDeduct(DeductStorageRequest request, Throwable cause) {
        log.error("storage-service fallback triggered, request={}, cause={}", request, cause == null ? "unknown" : cause.getMessage(), cause);
        return ResultVO.fail(503, "storage-service degraded, request rejected");
    }
}