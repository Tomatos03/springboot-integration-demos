package com.example.cloud.order.service;

import com.example.cloud.order.dto.CreateOrderRequest;
import com.example.cloud.order.entity.OrderDO;

import java.util.List;

public interface OrderService {

    Long createOrder(CreateOrderRequest request);

    List<OrderDO> list();
}
