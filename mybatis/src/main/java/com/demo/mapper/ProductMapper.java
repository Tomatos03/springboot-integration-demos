package com.demo.mapper;

import com.demo.entity.Product;
import org.apache.ibatis.annotations.Param;

/**
 * @author : Tomatos
 * @date : 2025/7/11
 */
//@Mapper: 注册成Bean, 这个Bean实现了接口定义的SQL相关的方法
public interface ProductMapper {
    Product queryProductById(@Param("id") Integer id);
}
