package com.demo.mapper;

import com.demo.entity.User;
import org.apache.ibatis.annotations.Param;

/**
 * @author : Tomatos
 * @date : 2025/7/11
 */
public interface UserMapper {
    // @Param注解标准方法参数提供给XML文件使用
    User queryUser(@Param("id") Integer id);
}