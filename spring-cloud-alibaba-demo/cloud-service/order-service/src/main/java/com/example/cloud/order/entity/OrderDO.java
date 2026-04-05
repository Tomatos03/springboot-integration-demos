package com.example.cloud.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class OrderDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;

    private String commodityCode;

    private Integer count;

    private BigDecimal money;

    private String status;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}
