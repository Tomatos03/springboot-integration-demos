package com.example.cloud.order.feign;

import com.example.cloud.order.dto.DeductAccountRequest;
import com.example.common.vo.ResultVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountFeignFallbackFactoryTest {

    @Test
    void createShouldReturnDegradeResponse() {
        AccountFeignFallbackFactory factory = new AccountFeignFallbackFactory();

        AccountFeignClient fallbackClient = factory.create(new RuntimeException("timeout"));
        ResultVO<Void> response = fallbackClient.deduct(new DeductAccountRequest("u1001", new BigDecimal("1.00")));

        assertNotNull(response);
        assertEquals(503, response.getCode());
        assertEquals("account-service degraded, request rejected", response.getMessage());
    }
}
