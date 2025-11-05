-- 创建用户表
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) COMMENT '邮箱',
    age INT COMMENT '年龄',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）'
);

CREATE INDEX idx_username ON user(username);
CREATE INDEX idx_age ON user(age);