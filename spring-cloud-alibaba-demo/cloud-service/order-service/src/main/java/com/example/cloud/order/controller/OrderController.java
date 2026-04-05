package com.example.cloud.order.controller;

import com.example.cloud.order.dto.CreateOrderRequest;
import com.example.cloud.order.entity.OrderDO;
import com.example.cloud.order.service.OrderService;
import com.example.common.vo.ResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@RefreshScope
public class OrderController {

    private final OrderService orderService;

    @Value("${order.config.orderNo:default-value}")
    private String orderNo;

    @PostMapping("/create")
    public ResultVO<Long> create(@RequestBody @Valid CreateOrderRequest request) {
        Long orderId = orderService.createOrder(request);
        return ResultVO.success(orderId);
    }

    @GetMapping("/list")
    public ResultVO<List<OrderDO>> list() {
        return ResultVO.success(orderService.list());
    }

    @GetMapping("/health")
    public ResultVO<Map<String, String>> health() {
        return ResultVO.success(Map.of("service", "order-service", "status", "UP"));
    }

    @GetMapping("/config")
    public ResultVO<Map<String, String>> getConfig() {
        return ResultVO.success(Map.of("Nacos Config OrderNo: ", orderNo));
    }
}
