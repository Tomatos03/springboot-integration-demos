package com.demo.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * 配置分页插件、乐观锁插件等
 *
 * @author Tomatos
 * @date 2025/11/5
 */
@Configuration
@MapperScan("com.demo.mapper")
public class MybatisConfig {

    /**
     * 配置MyBatis-Plus拦截器
     * 包括分页插件和乐观锁插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 添加分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置数据库类型（H2数据库，如果使用MySQL请改为DbType.MYSQL）
        paginationInnerInterceptor.setDbType(DbType.MARIADB);
        // 设置最大单页限制数量，默认500条，-1不受限制
        paginationInnerInterceptor.setMaxLimit(1000L);
        // 溢出总页数后是否进行处理（默认不处理，false表示不处理）
        paginationInnerInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        
        // 2. 添加乐观锁插件（如果需要使用乐观锁）
        // 使用乐观锁时，需要在实体类的版本字段上添加 @Version 注解
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        return interceptor;
    }
}