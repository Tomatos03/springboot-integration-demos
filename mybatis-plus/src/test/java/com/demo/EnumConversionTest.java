package com.demo;

import com.demo.entity.User;
import com.demo.enums.UserStatus;
import com.demo.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 枚举转换测试类
 * 测试四层转换流程（通过 HTTP 请求）：
 * 1. 入参转换：JSON String -> Enum（前端发送 JSON，Spring 反序列化为 Enum）
 * 2. 存储转换：Enum -> DB Value（MyBatis-Plus 将 Enum 转换为 code 存储）
 * 3. 加载转换：DB Value -> Enum（从数据库加载时自动转换为 Enum）
 * 4. 返回序列化：Enum -> JSON（Spring 序列化为 JSON，通过 @JsonValue 返回中文描述）
 *
 * @author Tomatos
 * @date 2025/11/5
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("枚举四层转换测试（HTTP 完整链路）")
class EnumConversionTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private IUserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /** 测试用例中创建的用户 ID，由第一步生成，供后续步骤使用 */
    private static Long testUserId;
    
    @Test
    @Order(1)
    @DisplayName("步骤1️⃣：入参转换 - 前端发送 HTTP 请求，JSON 反序列化为 UserStatus 枚举")
    void testStep1_RequestConversion() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🚀 步骤1：入参转换测试");
        System.out.println("=".repeat(70));
        
        // 构造请求对象，包含 UserStatus.ACTIVE 枚举
        User requestUser = new User();
        requestUser.setUsername("测试用户_活跃");
        requestUser.setEmail("active@example.com");
        requestUser.setAge(25);
        requestUser.setStatus(UserStatus.ACTIVE);
        
        // 将对象序列化为 JSON，查看发送的数据格式
        String requestJson = objectMapper.writeValueAsString(requestUser);
        System.out.println("📤 前端发送的 JSON 请求:");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(requestJson)));
        
        // 发送 HTTP POST 请求到 /user/insert 端点
        // Spring 接收 JSON 后，通过 @JsonCreator 注解的 fromJson() 方法反序列化
        // 将 JSON 中的状态值转换为 UserStatus.ACTIVE 枚举
        ResponseEntity<Map> saveResponse = restTemplate.postForEntity(
            "/user/insert",
            requestUser,
            Map.class
        );
        
        // 验证请求成功
        assertEquals(HttpStatus.OK, saveResponse.getStatusCode(), "POST 请求应该返回 200 OK");
        Map<String, Object> saveResponseBody = saveResponse.getBody();
        assertNotNull(saveResponseBody, "响应体不应为空");
        assertTrue((Boolean) saveResponseBody.get("success"), "用户创建应该成功");
        
        // 从响应中获取保存的用户 ID，供后续步骤使用
        @SuppressWarnings("unchecked")
        Map<String, Object> savedUserData = (Map<String, Object>) saveResponseBody.get("data");
        assertNotNull(savedUserData, "返回的用户数据不应为空");
        testUserId = ((Number) savedUserData.get("id")).longValue();
        assertNotNull(testUserId, "用户 ID 应该被自动生成");
        
        // 验证返回的 status 被序列化为中文描述
        Object statusValue = savedUserData.get("status");
        assertEquals(UserStatus.ACTIVE.getKey(), statusValue, "返回 JSON 中状态应该被序列化为中文 '活跃'");
        
        System.out.println("✅ POST 响应（已序列化为中文）:");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(saveResponseBody));
        System.out.println("\n✨ 步骤1 完成：JSON \"活跃\" → UserStatus.ACTIVE 枚举");
        System.out.println("📌 生成的用户 ID: " + testUserId);
    }
    
    @Test
    @Order(2)
    @DisplayName("步骤2️⃣：存储转换 - 验证 MyBatis-Plus 将 UserStatus 转换为 code 存储到数据库")
    void testStep2_StorageConversion() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("💾 步骤2：存储转换测试");
        System.out.println("=".repeat(70));
        
        // 首先执行步骤1以获取用户 ID（如果未执行）
        if (testUserId == null) {
            testStep1_RequestConversion();
        }
        
        System.out.println("📌 使用步骤1生成的用户 ID: " + testUserId);
        System.out.println("🔍 现在验证数据库中的存储格式...");
        
        // 直接从数据库查询用户
        User loadedUser = userService.getUserById(testUserId);
        assertNotNull(loadedUser, "用户应该被保存到数据库");
        
        // 验证 MyBatis-Plus 正确处理了枚举
        // @EnumValue 注解指定 code 字段为数据库存储值
        assertEquals(UserStatus.ACTIVE, loadedUser.getStatus(), 
            "加载的状态应该是 ACTIVE 枚举");
        assertEquals(1, loadedUser.getStatus().getCode(), 
            "ACTIVE 对应的 code 应该是 1");
        
        System.out.println("📥 从数据库加载的对象信息:");
        System.out.println("   - 状态枚举值: " + loadedUser.getStatus());
        System.out.println("   - 状态编码（@EnumValue）: " + loadedUser.getStatus().getCode());
        System.out.println("   - 状态描述: " + loadedUser.getStatus().getDescription());
        System.out.println("   - 用户名: " + loadedUser.getUsername());
        System.out.println("   - 邮箱: " + loadedUser.getEmail());
        System.out.println("   - 年龄: " + loadedUser.getAge());
        
        System.out.println("\n✨ 步骤2 完成：UserStatus.ACTIVE → code (1) 存储在数据库中");
        System.out.println("📊 数据库存储的实际值是整数 1，而不是枚举名称");
    }
    
    @Test
    @Order(3)
    @DisplayName("步骤3️⃣：加载转换 - 从数据库加载时自动转换 code 为 UserStatus 枚举")
    void testStep3_LoadConversion() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📥 步骤3：加载转换测试");
        System.out.println("=".repeat(70));
        
        // 首先执行步骤1以获取用户 ID（如果未执行）
        if (testUserId == null) {
            testStep1_RequestConversion();
        }
        
        System.out.println("📌 使用步骤1生成的用户 ID: " + testUserId);
        System.out.println("🔍 验证数据库中的 code (1) 被正确转换为枚举...");
        
        // 从数据库加载用户
        // MyBatis-Plus 会自动将数据库中的 code (1, 2, 3) 转换为对应的 UserStatus 枚举
        User loadedUser = userService.getUserById(testUserId);
        assertNotNull(loadedUser, "用户应该被保存到数据库");
        
        // 验证转换结果
        assertNotNull(loadedUser.getStatus(), "状态不应为空");
        assertEquals(UserStatus.ACTIVE, loadedUser.getStatus(), 
            "加载的状态应该是 ACTIVE 枚举");
        assertEquals(1, loadedUser.getStatus().getCode(), 
            "状态的编码应该是 1");
        assertEquals("活跃", loadedUser.getStatus().getDescription(), 
            "状态的描述应该是 '活跃'");
        
        System.out.println("✅ 转换成功！");
        System.out.println("   数据库中存储的 code: 1");
        System.out.println("   转换后的枚举类型: " + loadedUser.getStatus().getClass().getSimpleName());
        System.out.println("   转换后的枚举值: " + loadedUser.getStatus());
        System.out.println("   枚举的编码: " + loadedUser.getStatus().getCode());
        System.out.println("   枚举的描述: " + loadedUser.getStatus().getDescription());
        
        System.out.println("\n✨ 步骤3 完成：code (1) → UserStatus.ACTIVE 枚举");
        System.out.println("📊 MyBatis-Plus 通过 IEnum 接口自动完成类型转换");
    }
    
    @Test
    @Order(4)
    @DisplayName("步骤4️⃣：序列化转换 - HTTP GET 响应中 UserStatus 序列化为中文描述")
    void testStep4_SerializationConversion() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📤 步骤4：序列化转换测试");
        System.out.println("=".repeat(70));
        
        // 首先执行步骤1以获取用户 ID（如果未执行）
        if (testUserId == null) {
            testStep1_RequestConversion();
        }
        
        System.out.println("📌 使用步骤1生成的用户 ID: " + testUserId);
        System.out.println("🔍 发送 HTTP GET 请求查询用户...");
        
        // 发送 HTTP GET 请求到 /user/select/{id} 端点
        // Controller 从数据库加载用户（此时 status 是 UserStatus.ACTIVE 枚举）
        // Spring 序列化为 JSON 时，通过 @JsonValue 注解调用 toJson() 方法
        // 返回中文描述而不是枚举名称
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(
            "/user/select/" + testUserId,
            Map.class
        );
        
        // 验证请求成功
        assertEquals(HttpStatus.OK, getResponse.getStatusCode(), "GET 请求应该返回 200 OK");
        Map<String, Object> getResponseBody = getResponse.getBody();
        assertNotNull(getResponseBody, "响应体不应为空");
        assertTrue((Boolean) getResponseBody.get("success"), "查询应该成功");
        
        // 获取返回的用户数据
        @SuppressWarnings("unchecked")
        Map<String, Object> returnedUserData = (Map<String, Object>) getResponseBody.get("data");
        assertNotNull(returnedUserData, "返回的用户数据不应为空");
        
        // 验证 status 被序列化为中文描述
        Object returnedStatus = returnedUserData.get("status");
        assertEquals("活跃", returnedStatus, 
            "GET 响应中 status 应该被序列化为中文 '活跃'");
        
        System.out.println("✅ GET 响应（JSON 格式）:");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(getResponseBody));
        
        System.out.println("✨ 步骤4 完成：UserStatus.ACTIVE → JSON \"活跃\"");
        System.out.println("📊 通过 @JsonValue 注解在序列化时返回 description 而非 code");
        
        // ========== 完整流程总结 ==========
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🎉 完整的四层转换流程验证完成！");
        System.out.println("=".repeat(70));
        System.out.println("1️⃣  入参转换:   JSON \"活跃\" → UserStatus.ACTIVE");
        System.out.println("2️⃣  存储转换:   UserStatus.ACTIVE → code (1)");
        System.out.println("3️⃣  加载转换:   code (1) → UserStatus.ACTIVE");
        System.out.println("4️⃣  序列化转换: UserStatus.ACTIVE → JSON \"活跃\"");
        System.out.println("=".repeat(70));
    }
}