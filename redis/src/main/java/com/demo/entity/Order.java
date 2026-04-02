package com.demo.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@Data
public class Order implements Serializable {

    private String orderId;

    private Long userId;

    private BigDecimal amount;

    /** PENDING-待支付 PAID-已支付 CANCELLED-已取消 */
    private String status;

    private LocalDateTime createTime;

    /** 超时时间（分钟） */
    private int timeoutMinutes;
}
