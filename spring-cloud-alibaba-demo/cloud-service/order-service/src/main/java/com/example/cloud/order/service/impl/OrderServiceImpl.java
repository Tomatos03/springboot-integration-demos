package com.example.cloud.order.service.impl;

import com.example.cloud.order.dto.CreateOrderRequest;
import com.example.cloud.order.dto.DeductAccountRequest;
import com.example.cloud.order.dto.DeductStorageRequest;
import com.example.cloud.order.entity.OrderDO;
import com.example.cloud.order.feign.AccountFeignClient;
import com.example.cloud.order.feign.StorageFeignClient;
import com.example.cloud.order.mapper.OrderMapper;
import com.example.cloud.order.service.OrderService;
import com.example.common.exception.BusinessException;
import com.example.common.utils.Assert;
import com.example.common.vo.ResultVO;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final StorageFeignClient storageFeignClient;
    private final AccountFeignClient accountFeignClient;

    @Override
    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(CreateOrderRequest request) {
        ResultVO<Void> storageResult = storageFeignClient.deduct(
                new DeductStorageRequest(request.getCommodityCode(), request.getCount())
        );
        Assert.notNull(storageResult, "库存服务调用失败");
        Assert.isTrue(storageResult.getCode() == 200, "库存服务调用失败: " + storageResult.getMessage());

        ResultVO<Void> deduct = accountFeignClient.deduct(
                new DeductAccountRequest(request.getUserId(), request.getMoney())
        );
        Assert.notNull(deduct, "账户服务调用失败");
        Assert.isTrue(deduct.getCode() == 200, "账户服务调用失败: " + deduct.getMessage());

        OrderDO order = new OrderDO();
        order.setUserId(request.getUserId());
        order.setCommodityCode(request.getCommodityCode());
        order.setCount(request.getCount());
        order.setMoney(request.getMoney());
        order.setStatus("CREATED");
        orderMapper.insert(order);
        Assert.notNull(order.getId(), "订单创建失败");
        return order.getId();
    }

    @Override
    public List<OrderDO> list() {
        return orderMapper.selectList(null);
    }
}
