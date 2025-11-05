package com.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.entity.User;
import com.demo.mapper.UserMapper;
import com.demo.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现类
 * 继承ServiceImpl，自动获得常用的CRUD方法
 *
 * @author Tomatos
 * @date 2025/11/5
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {
    
    @Override
    public List<User> getUsersByUsernameLike(String username) {
        // 调用自定义的Mapper方法
        return baseMapper.selectByUsernameLike(username);
    }
    
    @Override
    public IPage<User> getUsersByAgeRange(Page<User> page, Integer minAge, Integer maxAge) {
        // 调用自定义的分页查询方法
        return baseMapper.selectPageByAgeRange(page, minAge, maxAge);
    }
    
    @Override
    public Integer countByAgeRange(Integer minAge, Integer maxAge) {
        // 调用自定义的统计方法
        return baseMapper.countByAgeRange(minAge, maxAge);
    }
    
    @Override
    public User getUserById(Long id) {
        // 使用MyBatis-Plus提供的方法
        return getById(id);
    }
    
    @Override
    public boolean saveUser(User user) {
        // 使用MyBatis-Plus提供的保存方法
        return save(user);
    }
    
    @Override
    public boolean updateUser(User user) {
        // 使用MyBatis-Plus提供的更新方法
        return updateById(user);
    }
    
    @Override
    public boolean deleteUser(Long id) {
        // 使用MyBatis-Plus提供的逻辑删除方法
        return removeById(id);
    }
    
    @Override
    public boolean batchSaveUsers(List<User> users) {
        // 使用MyBatis-Plus提供的批量保存方法
        // 第二个参数是批次大小，每批100条
        return saveBatch(users, 100);
    }
    
    @Override
    public List<User> getUsersByCondition(String username, Integer minAge, Integer maxAge) {
        // 演示使用LambdaQueryWrapper进行条件查询
        // LambdaQueryWrapper的优势：类型安全，避免字段名拼写错误
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果用户名不为空，添加模糊查询条件
        // 第一个参数是条件是否生效，第二个参数是字段，第三个参数是值
        queryWrapper.like(username != null && !username.isEmpty(), User::getUsername, username);
        
        // 如果最小年龄不为空，添加大于等于条件
        queryWrapper.ge(minAge != null, User::getAge, minAge);
        
        // 如果最大年龄不为空，添加小于等于条件
        queryWrapper.le(maxAge != null, User::getAge, maxAge);
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(User::getCreateTime);
        
        return list(queryWrapper);
    }
    
    /**
     * 演示使用QueryWrapper进行条件查询（另一种方式）
     * 
     * @param username 用户名
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 用户列表
     */
    public List<User> getUsersByConditionWithQueryWrapper(String username, Integer minAge, Integer maxAge) {
        // 使用普通的QueryWrapper
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        
        // 使用字符串形式的字段名（不推荐，容易拼写错误）
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        
        if (minAge != null) {
            queryWrapper.ge("age", minAge);
        }
        
        if (maxAge != null) {
            queryWrapper.le("age", maxAge);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        return list(queryWrapper);
    }
}