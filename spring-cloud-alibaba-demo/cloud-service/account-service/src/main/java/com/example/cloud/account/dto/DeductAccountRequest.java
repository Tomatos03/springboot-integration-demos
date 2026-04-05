package com.example.cloud.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeductAccountRequest {

    @NotBlank(message = "userId cannot be blank")
    private String userId;

    @NotNull(message = "money cannot be null")
    @DecimalMin(value = "0.01", message = "money must be greater than 0")
    private BigDecimal money;
}
