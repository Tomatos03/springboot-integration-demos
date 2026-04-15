package com.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeductAccountRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private BigDecimal money;
}