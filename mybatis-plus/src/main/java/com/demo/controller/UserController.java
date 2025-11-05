package com.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.entity.User;
import com.demo.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户Controller
 * 演示MyBatis-Plus的增删改查、分页查询、自定义查询功能
 *
 * @author Tomatos
 * @date 2025/11/5
 */
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private IUserService userService;
    
    /**
     * 1. 新增用户
     * POST /user
     * 
     * 请求示例：
     * {
     *   "username": "测试用户",
     *   "email": "test@example.com",
     *   "age": 25
     * }
     */
    @PostMapping
    public Map<String, Object> save(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.saveUser(user);
        result.put("success", success);
        result.put("message", success ? "保存成功" : "保存失败");
        result.put("data", user);
        return result;
    }
    
    /**
     * 2. 根据ID查询用户
     * GET /user/{id}
     * 
     * 访问示例：GET http://localhost:8080/user/1
     */
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserById(id);
        result.put("success", user != null);
        result.put("data", user);
        result.put("message", user != null ? "查询成功" : "用户不存在");
        return result;
    }
    
    /**
     * 3. 更新用户
     * PUT /user
     * 
     * 请求示例：
     * {
     *   "id": 1,
     *   "username": "更新用户名",
     *   "email": "update@example.com",
     *   "age": 26
     * }
     */
    @PutMapping
    public Map<String, Object> update(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.updateUser(user);
        result.put("success", success);
        result.put("message", success ? "更新成功" : "更新失败");
        result.put("data", user);
        return result;
    }
    
    /**
     * 4. 删除用户（逻辑删除）
     * DELETE /user/{id}
     * 
     * 访问示例：DELETE http://localhost:8080/user/1
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.deleteUser(id);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }
    
    /**
     * 5. 查询所有用户
     * GET /user/list
     * 
     * 访问示例：GET http://localhost:8080/user/list
     */
    @GetMapping("/list")
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        List<User> users = userService.list();
        result.put("success", true);
        result.put("data", users);
        result.put("total", users.size());
        result.put("message", "查询成功");
        return result;
    }
    
    /**
     * 6. 分页查询
     * GET /user/page?current=1&size=10
     * 
     * 访问示例：GET http://localhost:8080/user/page?current=1&size=5
     * 
     * @param current 当前页码（默认第1页）
     * @param size 每页显示条数（默认10条）
     */
    @GetMapping("/page")
    public Map<String, Object> page(@RequestParam(defaultValue = "1") Integer current,
                                     @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = new HashMap<>();
        Page<User> page = new Page<>(current, size);
        IPage<User> userPage = userService.page(page);
        result.put("success", true);
        result.put("data", userPage.getRecords());
        result.put("total", userPage.getTotal());
        result.put("current", userPage.getCurrent());
        result.put("size", userPage.getSize());
        result.put("pages", userPage.getPages());
        result.put("message", "分页查询成功");
        return result;
    }
    
    /**
     * 7. 条件查询（使用LambdaQueryWrapper）
     * GET /user/search?username=xxx&minAge=18&maxAge=30
     * 
     * 访问示例：GET http://localhost:8080/user/search?username=张&minAge=20&maxAge=30
     * 
     * @param username 用户名（模糊查询）
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     */
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam(required = false) String username,
                                       @RequestParam(required = false) Integer minAge,
                                       @RequestParam(required = false) Integer maxAge) {
        Map<String, Object> result = new HashMap<>();
        List<User> users = userService.getUsersByCondition(username, minAge, maxAge);
        result.put("success", true);
        result.put("data", users);
        result.put("total", users.size());
        result.put("message", "条件查询成功");
        return result;
    }
    
    /**
     * 8. 自定义查询：根据用户名模糊查询（XML方式）
     * GET /user/like?username=xxx
     * 
     * 访问示例：GET http://localhost:8080/user/like?username=张
     * 
     * @param username 用户名
     */
    @GetMapping("/like")
    public Map<String, Object> getUsersByUsernameLike(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();
        List<User> users = userService.getUsersByUsernameLike(username);
        result.put("success", true);
        result.put("data", users);
        result.put("total", users.size());
        result.put("message", "模糊查询成功");
        return result;
    }
    
    /**
     * 9. 自定义分页查询：根据年龄范围分页查询（XML方式）
     * GET /user/page-by-age?current=1&size=10&minAge=18&maxAge=30
     * 
     * 访问示例：GET http://localhost:8080/user/page-by-age?current=1&size=5&minAge=20&maxAge=30
     * 
     * @param current 当前页码
     * @param size 每页显示条数
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     */
    @GetMapping("/page-by-age")
    public Map<String, Object> pageByAgeRange(@RequestParam(defaultValue = "1") Integer current,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               @RequestParam(required = false) Integer minAge,
                                               @RequestParam(required = false) Integer maxAge) {
        Map<String, Object> result = new HashMap<>();
        Page<User> page = new Page<>(current, size);
        IPage<User> userPage = userService.getUsersByAgeRange(page, minAge, maxAge);
        result.put("success", true);
        result.put("data", userPage.getRecords());
        result.put("total", userPage.getTotal());
        result.put("current", userPage.getCurrent());
        result.put("size", userPage.getSize());
        result.put("pages", userPage.getPages());
        result.put("message", "年龄范围分页查询成功");
        return result;
    }
    
    /**
     * 10. 统计查询：统计指定年龄范围的用户数量
     * GET /user/count?minAge=18&maxAge=30
     * 
     * 访问示例：GET http://localhost:8080/user/count?minAge=20&maxAge=30
     * 
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     */
    @GetMapping("/count")
    public Map<String, Object> countByAgeRange(@RequestParam(required = false) Integer minAge,
                                               @RequestParam(required = false) Integer maxAge) {
        Map<String, Object> result = new HashMap<>();
        Integer count = userService.countByAgeRange(minAge, maxAge);
        result.put("success", true);
        result.put("count", count);
        result.put("message", "统计成功");
        return result;
    }
    
    /**
     * 11. 批量保存用户
     * POST /user/batch
     * 
     * 请求示例：
     * [
     *   {"username": "用户1", "email": "user1@example.com", "age": 20},
     *   {"username": "用户2", "email": "user2@example.com", "age": 22}
     * ]
     */
    @PostMapping("/batch")
    public Map<String, Object> batchSave(@RequestBody List<User> users) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.batchSaveUsers(users);
        result.put("success", success);
        result.put("message", success ? "批量保存成功" : "批量保存失败");
        result.put("total", users.size());
        return result;
    }
    
    /**
     * 12. 统计总用户数
     * GET /user/total
     * 
     * 访问示例：GET http://localhost:8080/user/total
     */
    @GetMapping("/total")
    public Map<String, Object> total() {
        Map<String, Object> result = new HashMap<>();
        long count = userService.count();
        result.put("success", true);
        result.put("total", count);
        result.put("message", "统计成功");
        return result;
    }
}