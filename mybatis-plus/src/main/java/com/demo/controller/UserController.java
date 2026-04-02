package com.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.entity.User;
import com.demo.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户Controller
 * 按CRUD分类组织API端点：新增(insert)、查询(select)、更新(update)、删除(delete)、分页(page)
 *
 * @author Tomatos
 * @date 2025/11/5
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    // ==================== 新增操作 ====================

    /**
     * 单条插入用户
     * POST /user/insert
     *
     * 请求示例：
     * {
     *   "username": "测试用户",
     *   "email": "test@example.com",
     *   "age": 25
     * }
     */
    @PostMapping("/insert")
    public Map<String, Object> save(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.saveUser(user);
        result.put("success", success);
        result.put("message", success ? "保存成功" : "保存失败");
        result.put("data", user);
        return result;
    }

    /**
     * 批量插入用户
     * POST /user/insert/batch
     *
     * 请求示例：
     * [
     *   {"username": "用户1", "email": "user1@example.com", "age": 20},
     *   {"username": "用户2", "email": "user2@example.com", "age": 22}
     * ]
     */
    @PostMapping("/insert/batch")
    public Map<String, Object> batchSave(@RequestBody List<User> users) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.batchSaveUsers(users);
        result.put("success", success);
        result.put("message", success ? "批量保存成功" : "批量保存失败");
        result.put("total", users.size());
        return result;
    }

    /**
     * 选择性插入（只插入非null字段）
     * POST /user/insert/selective
     *
     * 请求示例：
     * {
     *   "username": "测试用户",
     *   "age": 25
     * }
     */
    @PostMapping("/insert/selective")
    public Map<String, Object> saveSelective(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.saveSelective(user);
        result.put("success", success);
        result.put("message", success ? "选择性保存成功" : "选择性保存失败");
        result.put("data", user);
        return result;
    }

    // ==================== 查询操作 ====================

    /**
     * 根据ID查询用户
     * GET /user/select/{id}
     *
     * 访问示例：GET http://localhost:8080/user/select/1
     */
    @GetMapping("/select/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserById(id);
        result.put("success", user != null);
        result.put("data", user);
        result.put("message", user != null ? "查询成功" : "用户不存在");
        return result;
    }

    /**
     * 条件查询单条记录
     * GET /user/select/one?username=xxx&age=25
     *
     * @param username 用户名
     * @param age 年龄
     */
    @GetMapping("/select/one")
    public Map<String, Object> getOneByCondition(@RequestParam(required = false) String username,
                                                  @RequestParam(required = false) Integer age) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getOneByCondition(username, age);
        result.put("success", user != null);
        result.put("data", user);
        result.put("message", user != null ? "查询成功" : "未找到匹配记录");
        return result;
    }

    /**
     * 查询全部用户
     * GET /user/select/list
     *
     * 访问示例：GET http://localhost:8080/user/select/list
     */
    @GetMapping("/select/list")
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
     * 条件查询用户列表（使用LambdaQueryWrapper）
     * GET /user/select/condition?username=xxx&minAge=18&maxAge=30
     *
     * @param username 用户名（模糊查询）
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     */
    @GetMapping("/select/condition")
    public Map<String, Object> getUsersByCondition(@RequestParam(required = false) String username,
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
     * 根据ID列表查询用户（in查询）
     * GET /user/select/ids?ids=1,2,3
     *
     * @param ids 用户ID列表（逗号分隔）
     */
    @GetMapping("/select/ids")
    public Map<String, Object> getUsersByIds(@RequestParam String ids) {
        Map<String, Object> result = new HashMap<>();
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        List<User> users = userService.getUsersByIds(idList);
        result.put("success", true);
        result.put("data", users);
        result.put("total", users.size());
        result.put("message", "in查询成功");
        return result;
    }

    /**
     * isNull/isNotNull条件查询
     * GET /user/select/null-check?isNull=true  (查询email为null的用户)
     * GET /user/select/null-check?isNull=false (查询email不为null的用户)
     *
     * @param isNull 是否查询null记录
     */
    @GetMapping("/select/null-check")
    public Map<String, Object> getUsersByNullCondition(@RequestParam Boolean isNull) {
        Map<String, Object> result = new HashMap<>();
        List<User> users = userService.getUsersByNullCondition(isNull);
        result.put("success", true);
        result.put("data", users);
        result.put("total", users.size());
        result.put("message", "null条件查询成功");
        return result;
    }

    /**
     * 嵌套条件查询（使用嵌套条件构造器）
     * GET /user/select/nested?username=xxx&minAge=18&maxAge=30
     *
     * @param username 用户名
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     */
    @GetMapping("/select/nested")
    public Map<String, Object> getUsersByNestedCondition(@RequestParam(required = false) String username,
                                                          @RequestParam(required = false) Integer minAge,
                                                          @RequestParam(required = false) Integer maxAge) {
        Map<String, Object> result = new HashMap<>();
        List<User> users = userService.getUsersByNestedCondition(username, minAge, maxAge);
        result.put("success", true);
        result.put("data", users);
        result.put("total", users.size());
        result.put("message", "嵌套条件查询成功");
        return result;
    }

    /**
     * 模糊查询（XML方式）
     * GET /user/select/like?username=xxx
     *
     * 访问示例：GET http://localhost:8080/user/select/like?username=张
     *
     * @param username 用户名
     */
    @GetMapping("/select/like")
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
     * 查询总记录数
     * GET /user/select/total
     *
     * 访问示例：GET http://localhost:8080/user/select/total
     */
    @GetMapping("/select/total")
    public Map<String, Object> total() {
        Map<String, Object> result = new HashMap<>();
        long count = userService.count();
        result.put("success", true);
        result.put("total", count);
        result.put("message", "统计成功");
        return result;
    }

    /**
     * 多表关联查询 - 查询所有用户及其角色
     * GET /user/select/with-roles
     *
     * 访问示例：GET http://localhost:8080/user/select/with-roles
     */
    @GetMapping("/select/with-roles")
    public Map<String, Object> getUsersWithRoles() {
        Map<String, Object> result = new HashMap<>();
        List<User> users = userService.getUsersWithRoles();
        result.put("success", true);
        result.put("data", users);
        result.put("total", users.size());
        result.put("message", "多表关联查询成功");
        return result;
    }

    /**
     * 多表关联查询 - 根据ID查询用户及其角色
     * GET /user/select/{id}/with-roles
     *
     * 访问示例：GET http://localhost:8080/user/select/1/with-roles
     */
    @GetMapping("/select/{id}/with-roles")
    public Map<String, Object> getUserWithRolesById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getUserWithRolesById(id);
        result.put("success", user != null);
        result.put("data", user);
        result.put("message", user != null ? "查询成功" : "用户不存在");
        return result;
    }

    // ==================== 更新操作 ====================

    /**
     * 根据ID更新用户
     * PUT /user/update
     *
     * 请求示例：
     * {
     *   "id": 1,
     *   "username": "更新用户名",
     *   "email": "update@example.com",
     *   "age": 26
     * }
     */
    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.updateUser(user);
        result.put("success", success);
        result.put("message", success ? "更新成功" : "更新失败");
        result.put("data", user);
        return result;
    }

    /**
     * 选择性更新（只更新非null字段）
     * PUT /user/update/selective
     *
     * 请求示例：
     * {
     *   "id": 1,
     *   "age": 30
     * }
     */
    @PutMapping("/update/selective")
    public Map<String, Object> updateSelectiveById(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.updateSelectiveById(user);
        result.put("success", success);
        result.put("message", success ? "选择性更新成功" : "选择性更新失败");
        result.put("data", user);
        return result;
    }

    /**
     * 按条件更新用户
     * PUT /user/update/condition?username=xxx&minAge=18&maxAge=30
     *
     * 请求示例：
     * PUT /user/update/condition?username=张&minAge=20&maxAge=30
     * Body: {"email": "updated@example.com", "age": 28}
     *
     * @param username 用户名条件
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     */
    @PutMapping("/update/condition")
    public Map<String, Object> updateByCondition(@RequestBody User user,
                                                  @RequestParam(required = false) String username,
                                                  @RequestParam(required = false) Integer minAge,
                                                  @RequestParam(required = false) Integer maxAge) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.updateByCondition(user, username, minAge, maxAge);
        result.put("success", success);
        result.put("message", success ? "条件更新成功" : "条件更新失败");
        return result;
    }

    /**
     * 使用UpdateWrapper更新
     * PUT /user/update/wrapper?id=1&email=xxx&age=25
     *
     * @param id 用户ID
     * @param email 邮箱
     * @param age 年龄
     */
    @PutMapping("/update/wrapper")
    public Map<String, Object> updateByWrapper(@RequestParam Long id,
                                                @RequestParam String email,
                                                @RequestParam Integer age) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.updateByWrapper(id, email, age);
        result.put("success", success);
        result.put("message", success ? "UpdateWrapper更新成功" : "UpdateWrapper更新失败");
        return result;
    }

    /**
     * 使用LambdaUpdateWrapper更新
     * PUT /user/update/lambda-wrapper?id=1&email=xxx&age=25
     *
     * @param id 用户ID
     * @param email 邮箱
     * @param age 年龄
     */
    @PutMapping("/update/lambda-wrapper")
    public Map<String, Object> updateByLambdaWrapper(@RequestParam Long id,
                                                      @RequestParam String email,
                                                      @RequestParam Integer age) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.updateByLambdaWrapper(id, email, age);
        result.put("success", success);
        result.put("message", success ? "LambdaUpdateWrapper更新成功" : "LambdaUpdateWrapper更新失败");
        return result;
    }

    // ==================== 删除操作 ====================

    /**
     * 根据ID删除用户（逻辑删除）
     * DELETE /user/delete/{id}
     *
     * 访问示例：DELETE http://localhost:8080/user/delete/1
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.deleteUser(id);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }

    /**
     * 按条件删除用户
     * DELETE /user/delete/condition?username=xxx&minAge=18&maxAge=30
     *
     * @param username 用户名条件
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     */
    @DeleteMapping("/delete/condition")
    public Map<String, Object> deleteByCondition(@RequestParam(required = false) String username,
                                                  @RequestParam(required = false) Integer minAge,
                                                  @RequestParam(required = false) Integer maxAge) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.deleteByCondition(username, minAge, maxAge);
        result.put("success", success);
        result.put("message", success ? "条件删除成功" : "条件删除失败");
        return result;
    }

    /**
     * 批量删除用户
     * DELETE /user/delete/batch?ids=1,2,3
     *
     * @param ids 用户ID列表（逗号分隔）
     */
    @DeleteMapping("/delete/batch")
    public Map<String, Object> batchDeleteByIds(@RequestParam String ids) {
        Map<String, Object> result = new HashMap<>();
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        boolean success = userService.batchDeleteByIds(idList);
        result.put("success", success);
        result.put("message", success ? "批量删除成功" : "批量删除失败");
        result.put("total", idList.size());
        return result;
    }

    // ==================== 分页查询 ====================

    /**
     * 基本分页查询
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
     * 年龄范围分页查询（XML方式）
     * GET /user/page/age?current=1&size=10&minAge=18&maxAge=30
     *
     * 访问示例：GET http://localhost:8080/user/page/age?current=1&size=5&minAge=20&maxAge=30
     *
     * @param current 当前页码
     * @param size 每页显示条数
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     */
    @GetMapping("/page/age")
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
}
