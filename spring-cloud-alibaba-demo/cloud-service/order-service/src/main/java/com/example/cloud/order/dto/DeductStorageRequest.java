package com.example.cloud.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeductStorageRequest {

    private String commodityCode;
    private Integer count;
}
