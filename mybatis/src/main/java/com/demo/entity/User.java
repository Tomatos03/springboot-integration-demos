package com.demo.entity;

import lombok.Data;

/**
 * @author : Tomatos
 * @date : 2025/7/11
 */
@Data
//@AllArgsConstructor // 启用User全参构造器, Mybatis 查询返回User的数据顺序必须与声明顺序一致
public class User {
    private Integer id;
    private String name;
    private String email;
    private Integer age;
}
