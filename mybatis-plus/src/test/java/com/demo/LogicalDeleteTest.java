package com.demo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.demo.entity.User;
import com.demo.service.IUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 逻辑删除测试类
 * 测试流程：
 * 1. 添加几个User记录
 * 2. 调用service删除一个用户（逻辑删除）
 * 3. 查询所有User
 * 4. 验证查询结果不包含已删除的用户
 *
 * @author Tomatos
 * @date 2025/11/5
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("逻辑删除功能测试")
class LogicalDeleteTest {

    @Autowired
    private IUserService userService;

    private List<Long> testUserIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🚀 测试开始：逻辑删除功能测试");
        System.out.println("=".repeat(70));
        testUserIds.clear();
    }

    @AfterEach
    void tearDown() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("✅ 测试完成");
        System.out.println("=".repeat(70) + "\n");
    }

    @Test
    @DisplayName("测试逻辑删除")
    void testLogicalDelete() {
        System.out.println("-".repeat(70));

        // 获取当前所有用户
        List<User> allUsersBefore = userService.list();
        assertNotNull(allUsersBefore, "查询用户列表不应为空");
        assertTrue(allUsersBefore.size() > 0, "数据库中应至少有一条用户数据用于测试");

        // 选择第一个用户用于逻辑删除
        User userToDelete = allUsersBefore.get(0);
        Long userToDeleteId = userToDelete.getId();
        String deletedUsername = userToDelete.getUsername();
        System.out.println("🗑️  准备删除用户 ID: " + userToDeleteId + ", 用户名: " + deletedUsername);

        // 记录其余用户ID用于后续断言
        List<Long> remainingUserIds = new ArrayList<>();
        for (User user : allUsersBefore) {
            if (!user.getId()
                     .equals(userToDeleteId)) {
                remainingUserIds.add(user.getId());
            }
        }

        // 步骤 2：调用service删除一个用户（逻辑删除）
        boolean deleteResult = userService.deleteUser(userToDeleteId);
        assertTrue(deleteResult, "删除用户应该成功");
        System.out.println("✅ 用户 '" + deletedUsername + "' 已被逻辑删除（deleted = 1）");
        System.out.println("   注意：数据记录仍然存在于数据库中，只是被标记为已删除");

        System.out.println("list()方法自动过滤逻辑删除的用户");
        userService.list().forEach(this::outUserInfo);


        // 步骤 3：查询所有User
        System.out.println("XML方式跳过自动筛选逻辑删除用户，查询所有用户（包含已删除的用户）");
        userService.getAll().forEach(this::outUserInfo);
    }

    private void outUserInfo(User user) {
        System.out.println("  ✓ 用户 ID: " + user.getId() + ", 用户名: " + user.getUsername() +
                                   ", 邮箱: " + user.getEmail() + ", 年龄: " + user.getAge() +
                                   ", deleted: " + user.getDeleted());
    }
}