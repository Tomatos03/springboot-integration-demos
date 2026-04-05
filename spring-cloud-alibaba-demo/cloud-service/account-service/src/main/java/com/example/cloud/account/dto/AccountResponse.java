package com.example.cloud.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private String userId;
    private BigDecimal total;
    private BigDecimal used;
    private BigDecimal residue;
}
