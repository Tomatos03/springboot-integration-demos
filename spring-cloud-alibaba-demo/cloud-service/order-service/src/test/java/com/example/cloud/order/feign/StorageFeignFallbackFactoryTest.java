package com.example.cloud.order.feign;

import com.example.cloud.order.dto.DeductStorageRequest;
import com.example.common.vo.ResultVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StorageFeignFallbackFactoryTest {

    @Test
    void createShouldReturnDegradeResponse() {
        StorageFeignFallbackFactory factory = new StorageFeignFallbackFactory();

        StorageFeignClient fallbackClient = factory.create(new RuntimeException("timeout"));
        ResultVO<Void> response = fallbackClient.deduct(new DeductStorageRequest("C1001", 1));

        assertNotNull(response);
        assertEquals(503, response.getCode());
        assertEquals("storage-service degraded, request rejected", response.getMessage());
    }
}
