package com.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.User;

import java.util.List;

/**
 * 用户服务接口
 * 继承IService后，自动拥有常用的CRUD方法
 *
 * @author Tomatos
 * @date 2025/11/5
 */
public interface IUserService extends IService<User> {
    
    /**
     * 根据用户名模糊查询
     * 
     * @param username 用户名
     * @return 用户列表
     */
    List<User> getUsersByUsernameLike(String username);
    
    /**
     * 根据年龄范围分页查询
     * 
     * @param page 分页对象
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 分页结果
     */
    IPage<User> getUsersByAgeRange(Page<User> page, Integer minAge, Integer maxAge);
    
    /**
     * 统计指定年龄范围的用户数量
     * 
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 用户数量
     */
    Integer countByAgeRange(Integer minAge, Integer maxAge);
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    User getUserById(Long id);
    
    /**
     * 保存用户
     * 
     * @param user 用户信息
     * @return 是否成功
     */
    boolean saveUser(User user);
    
    /**
     * 更新用户
     * 
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUser(User user);
    
    /**
     * 删除用户（逻辑删除）
     * 
     * @param id 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long id);
    
    /**
     * 批量保存用户
     * 
     * @param users 用户列表
     * @return 是否成功
     */
    boolean batchSaveUsers(List<User> users);
    
    /**
     * 条件查询示例
     * 演示使用QueryWrapper进行灵活的条件查询
     * 
     * @param username 用户名（支持模糊查询）
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 用户列表
     */
    List<User> getUsersByCondition(String username, Integer minAge, Integer maxAge);
}