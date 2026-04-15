package com.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeductStorageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String commodityCode;
    private Integer count;
}