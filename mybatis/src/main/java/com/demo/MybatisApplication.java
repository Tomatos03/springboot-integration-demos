package com.demo;

import com.demo.entity.Product;
import com.demo.entity.User;
import com.demo.mapper.ProductMapper;
import com.demo.mapper.UserMapper;
import com.demo.utils.SpringContextUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.demo.mapper")
@SpringBootApplication
public class MybatisApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisApplication.class, args);

        UserMapper userMapper = SpringContextUtil.getBean(UserMapper.class);
        User user = userMapper.queryUser(1);
        System.out.println(user);

        ProductMapper productMapper = SpringContextUtil.getBean(ProductMapper.class);
        Product product = productMapper.queryProductById(1);
        System.out.println(product);
    }
}
