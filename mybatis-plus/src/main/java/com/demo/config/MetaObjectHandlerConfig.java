package com.demo.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充配置
 * 自动填充创建时间和更新时间
 *
 * @author Tomatos
 * @date 2025/11/5
 */
@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {
    
    /**
     * 插入时自动填充
     * 当执行 insert 操作时，会自动填充标注了 @TableField(fill = FieldFill.INSERT) 的字段
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 自动填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
    
    /**
     * 更新时自动填充
     * 当执行 update 操作时，会自动填充标注了 @TableField(fill = FieldFill.UPDATE) 的字段
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}