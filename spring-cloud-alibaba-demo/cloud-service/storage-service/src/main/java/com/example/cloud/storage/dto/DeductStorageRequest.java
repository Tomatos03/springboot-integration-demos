package com.example.cloud.storage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeductStorageRequest {

    @NotBlank(message = "commodityCode cannot be blank")
    private String commodityCode;

    @Min(value = 1, message = "count must be greater than 0")
    private Integer count;
}
