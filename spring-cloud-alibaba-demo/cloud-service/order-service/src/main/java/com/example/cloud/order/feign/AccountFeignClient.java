package com.example.cloud.order.feign;

import com.example.cloud.order.dto.DeductAccountRequest;
import com.example.common.constant.ServiceNameConstants;
import com.example.common.vo.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = ServiceNameConstants.ACCOUNT_SERVICE,
        path = "/api/account",
        fallbackFactory = AccountFeignFallbackFactory.class
)
public interface AccountFeignClient {

    @PostMapping("/deduct")
    ResultVO<Void> deduct(@RequestBody DeductAccountRequest request);
}
