-- 创建用户表
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) COMMENT '邮箱',
    age INT COMMENT '年龄',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）'
);

CREATE INDEX idx_username ON sys_user(username);
CREATE INDEX idx_age ON sys_user(age);

-- 创建角色表
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）'
);

-- 创建用户角色关联表
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID'
);

CREATE INDEX idx_user_id ON sys_user_role(user_id);
CREATE INDEX idx_role_id ON sys_user_role(role_id);
