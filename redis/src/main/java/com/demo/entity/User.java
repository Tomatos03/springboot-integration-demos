package com.demo.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@Data
public class User implements Serializable {

    private Long id;

    private String username;

    private String email;

    private LocalDateTime createTime;
}
