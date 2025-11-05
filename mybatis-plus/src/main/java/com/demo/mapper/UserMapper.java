package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 * 继承BaseMapper后，自动拥有增删改查等常用方法
 *
 * @author Tomatos
 * @date 2025/11/5
 */
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 自定义查询：根据用户名模糊查询
     * 对应XML: UserMapper.xml
     * 
     * @param username 用户名
     * @return 用户列表
     */
    List<User> selectByUsernameLike(@Param("username") String username);
    
    /**
     * 自定义分页查询：根据年龄范围查询
     * 
     * @param page 分页对象
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 分页结果
     */
    IPage<User> selectPageByAgeRange(Page<User> page, 
                                     @Param("minAge") Integer minAge, 
                                     @Param("maxAge") Integer maxAge);
    
    /**
     * 自定义统计查询：统计指定年龄范围的用户数量
     * 
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 用户数量
     */
    Integer countByAgeRange(@Param("minAge") Integer minAge, 
                           @Param("maxAge") Integer maxAge);
}