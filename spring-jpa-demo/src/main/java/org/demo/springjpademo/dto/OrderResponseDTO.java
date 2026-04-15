package org.demo.springjpademo.dto;

import org.demo.springjpademo.entity.PurchaseOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponseDTO(
        Long id,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt
) {
    public static OrderResponseDTO fromEntity(PurchaseOrder order) {
        if (order == null) {
            return null;
        }
        return new OrderResponseDTO(
                order.getId(),
                order.getAmount(),
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getCreatedAt()
        );
    }
}