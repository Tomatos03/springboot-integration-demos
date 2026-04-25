package com.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.demo.enums.UserStatus;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类
 *
 * @author Tomatos
 * @date 2025/11/5
 */
@Data
@TableName("sys_user")
public class User {
    
    /**
     * type可选值
     * IdType.AUTO: 数据库自增
     * IdType.ASSIGN_ID: 雪花算法生成ID
     * IdType.ASSIGN_UUID: UUID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名
     */
    @TableField("username")
    private String username;
    
    /**
     * 邮箱
     */
    @TableField("email")
    private String email;
    
    /**
     * 年龄
     */
    @TableField("age")
    private Integer age;
    
    /**
     * 用户状态
     * 
     * 四层转换流程：
     * 1. 入参转换：通过 StringToUserStatusConverter 将 String 转换为 UserStatus
     * 2. 存储转换：MyBatis-Plus 自动将 UserStatus 转换为 code (1,2,3) 存储到数据库
     * 3. 加载转换：MyBatis-Plus 自动将数据库的 code (1,2,3) 转换为 UserStatus
     * 4. 返回序列化：通过 @JsonValue 将 UserStatus 序列化为 description ("活跃","非活跃","被封禁")
     */
    @TableField("status")
    private UserStatus status;
    
    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间（自动填充）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 逻辑删除字段（0-未删除，1-已删除）
     */
    @TableLogic(value = "0", delval = "1")
    @TableField("deleted")
    private Integer deleted;

    /**
     * 用户角色列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Role> roles;
}