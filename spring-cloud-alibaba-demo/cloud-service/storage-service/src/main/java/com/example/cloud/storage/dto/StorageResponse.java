package com.example.cloud.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageResponse {

    private String commodityCode;
    private Integer total;
    private Integer used;
    private Integer residue;
}
