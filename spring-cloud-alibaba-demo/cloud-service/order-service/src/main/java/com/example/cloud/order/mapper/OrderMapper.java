package com.example.cloud.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cloud.order.entity.OrderDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {
}
