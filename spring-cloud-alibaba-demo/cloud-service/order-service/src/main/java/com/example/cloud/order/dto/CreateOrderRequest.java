package com.example.cloud.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "userId cannot be blank")
    private String userId;

    @NotBlank(message = "commodityCode cannot be blank")
    private String commodityCode;

    @NotNull(message = "count cannot be null")
    @Min(value = 1, message = "count must be greater than 0")
    private Integer count;

    @NotNull(message = "money cannot be null")
    @DecimalMin(value = "0.01", message = "money must be greater than 0")
    private BigDecimal money;
}
