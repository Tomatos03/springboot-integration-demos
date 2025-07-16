package com.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author : Tomatos
 * @date : 2025/7/16
 */
@Data
@AllArgsConstructor
public class User {
    private String name;
    private String email;
    private String avatar;
}
