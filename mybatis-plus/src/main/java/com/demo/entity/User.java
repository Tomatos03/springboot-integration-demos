package com.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

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
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 用户角色列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Role> roles;
}