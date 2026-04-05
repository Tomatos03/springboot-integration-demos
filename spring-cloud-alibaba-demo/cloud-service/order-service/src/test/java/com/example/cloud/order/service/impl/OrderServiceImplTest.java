package com.example.cloud.order.service.impl;

import com.example.cloud.order.dto.CreateOrderRequest;
import com.example.cloud.order.dto.DeductAccountRequest;
import com.example.cloud.order.dto.DeductStorageRequest;
import com.example.cloud.order.entity.OrderDO;
import com.example.cloud.order.feign.AccountFeignClient;
import com.example.cloud.order.feign.StorageFeignClient;
import com.example.cloud.order.mapper.OrderMapper;
import com.example.common.exception.BusinessException;
import com.example.common.vo.ResultVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private StorageFeignClient storageFeignClient;

    @Mock
    private AccountFeignClient accountFeignClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrderShouldCallDownstreamAndPersistWhenSuccess() {
        CreateOrderRequest request = new CreateOrderRequest("u1001", "C1001", 2, new BigDecimal("100.00"));

        when(storageFeignClient.deduct(ArgumentMatchers.any(DeductStorageRequest.class))).thenReturn(ResultVO.success());
        when(accountFeignClient.deduct(ArgumentMatchers.any(DeductAccountRequest.class))).thenReturn(ResultVO.success());
        when(orderMapper.insert(ArgumentMatchers.any(OrderDO.class))).thenAnswer(invocation -> {
            OrderDO order = invocation.getArgument(0);
            order.setId(1L);
            return 1;
        });

        Long orderId = orderService.createOrder(request);

        assertNotNull(orderId);
        verify(storageFeignClient).deduct(ArgumentMatchers.any(DeductStorageRequest.class));
        verify(accountFeignClient).deduct(ArgumentMatchers.any(DeductAccountRequest.class));
        verify(orderMapper).insert(ArgumentMatchers.any(OrderDO.class));
    }

    @Test
    void createOrderShouldStopWhenStorageDeductFails() {
        CreateOrderRequest request = new CreateOrderRequest("u1001", "C1001", 2, new BigDecimal("100.00"));

        when(storageFeignClient.deduct(ArgumentMatchers.any(DeductStorageRequest.class))).thenReturn(ResultVO.fail("stock not enough"));

        assertThrows(BusinessException.class, () -> orderService.createOrder(request));
        verify(accountFeignClient, never()).deduct(ArgumentMatchers.any(DeductAccountRequest.class));
        verify(orderMapper, never()).insert(ArgumentMatchers.any(OrderDO.class));
    }
}
