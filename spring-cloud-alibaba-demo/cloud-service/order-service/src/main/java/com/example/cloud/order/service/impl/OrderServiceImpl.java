package com.example.cloud.order.service.impl;

import com.example.cloud.order.dto.CreateOrderRequest;
import com.example.common.dto.DeductAccountRequest;
import com.example.common.dto.DeductStorageRequest;
import com.example.common.dubbo.DubboAccountService;
import com.example.common.dubbo.DubboStorageService;
import com.example.cloud.order.entity.OrderDO;
import com.example.cloud.order.feign.AccountFeignClient;
import com.example.cloud.order.feign.StorageFeignClient;
import com.example.cloud.order.mapper.OrderMapper;
import com.example.cloud.order.service.OrderService;
import com.example.common.utils.Assert;
import com.example.common.vo.ResultVO;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final StorageFeignClient storageFeignClient;
    private final AccountFeignClient accountFeignClient;

    @DubboReference
    private DubboStorageService dubboStorageService;
    @DubboReference
    private DubboAccountService dubboAccountService;

    @Value("${remote.call-mode:feign}")
    private String callMode;

    @Override
    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(CreateOrderRequest request) {
        log.info("开始创建订单，使用调用模式: {}", callMode);

        if ("dubbo".equalsIgnoreCase(callMode)) {
            log.info("[Dubbo方式] 调用库存服务，商品编码: {}, 数量: {}", request.getCommodityCode(), request.getCount());
            ResultVO<Void> storageResult = dubboStorageService.deduct(
                    new DeductStorageRequest(request.getCommodityCode(), request.getCount())
            );
            log.info("[Dubbo方式] 库存服务返回结果: code={}, message={}", storageResult.getCode(), storageResult.getMessage());
            Assert.notNull(storageResult, "库存服务调用失败");
            Assert.isTrue(storageResult.getCode() == 200, "库存服务调用失败: " + storageResult.getMessage());

            log.info("[Dubbo方式] 调用账户服务，用户ID: {}, 金额: {}", request.getUserId(), request.getMoney());
            ResultVO<Void> deduct = dubboAccountService.deduct(
                    new DeductAccountRequest(request.getUserId(), request.getMoney())
            );
            log.info("[Dubbo方式] 账户服务返回结果: code={}, message={}", deduct.getCode(), deduct.getMessage());
            Assert.notNull(deduct, "账户服务调用失败");
            Assert.isTrue(deduct.getCode() == 200, "账户服务调用失败: " + deduct.getMessage());
        } else {
            log.info("[OpenFeign方式] 调用库存服务，商品编码: {}, 数量: {}", request.getCommodityCode(), request.getCount());
            ResultVO<Void> storageResult = storageFeignClient.deduct(
                    new DeductStorageRequest(request.getCommodityCode(), request.getCount())
            );
            log.info("[OpenFeign方式] 库存服务返回结果: code={}, message={}", storageResult.getCode(), storageResult.getMessage());
            Assert.notNull(storageResult, "库存服务调用失败");
            Assert.isTrue(storageResult.getCode() == 200, "库存服务调用失败: " + storageResult.getMessage());

            log.info("[OpenFeign方式] 调用账户服务，用户ID: {}, 金额: {}", request.getUserId(), request.getMoney());
            ResultVO<Void> deduct = accountFeignClient.deduct(
                    new DeductAccountRequest(request.getUserId(), request.getMoney())
            );
            log.info("[OpenFeign方式] 账户服务返回结果: code={}, message={}", deduct.getCode(), deduct.getMessage());
            Assert.notNull(deduct, "账户服务调用失败");
            Assert.isTrue(deduct.getCode() == 200, "账户服务调用失败: " + deduct.getMessage());
        }

        log.info("开始创建订单数据库记录");
        OrderDO order = new OrderDO();
        order.setUserId(request.getUserId());
        order.setCommodityCode(request.getCommodityCode());
        order.setCount(request.getCount());
        order.setMoney(request.getMoney());
        order.setStatus("CREATED");
        orderMapper.insert(order);
        Assert.notNull(order.getId(), "订单创建失败");
        log.info("订单创建成功，订单ID: {}", order.getId());
        return order.getId();
    }

    @Override
    public List<OrderDO> list() {
        return orderMapper.selectList(null);
    }
}