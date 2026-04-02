-- 插入用户测试数据
INSERT INTO sys_user (username, email, age, version, create_time, update_time, deleted) VALUES
('张三', 'zhangsan@example.com', 25, 0, NOW(), NOW(), 0),
('李四', 'lisi@example.com', 30, 0, NOW(), NOW(), 0),
('王五', 'wangwu@example.com', 28, 0, NOW(), NOW(), 0),
('赵六', 'zhaoliu@example.com', 22, 0, NOW(), NOW(), 0),
('孙七', 'sunqi@example.com', 35, 0, NOW(), NOW(), 0),
('周八', 'zhouba@example.com', 27, 0, NOW(), NOW(), 0),
('吴九', 'wujiu@example.com', 32, 0, NOW(), NOW(), 0),
('郑十', 'zhengshi@example.com', 29, 0, NOW(), NOW(), 0);

-- 插入角色测试数据
INSERT INTO sys_role (role_name, role_code, description, create_time, update_time, deleted) VALUES
('管理员', 'ADMIN', '系统管理员，拥有所有权限', NOW(), NOW(), 0),
('普通用户', 'USER', '普通用户，拥有基本权限', NOW(), NOW(), 0),
('访客', 'GUEST', '访客，只有查看权限', NOW(), NOW(), 0);

-- 插入用户角色关联数据
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- 张三 - 管理员
(2, 2),  -- 李四 - 普通用户
(3, 2),  -- 王五 - 普通用户
(4, 3),  -- 赵六 - 访客
(5, 2),  -- 孙七 - 普通用户
(6, 2),  -- 周八 - 普通用户
(7, 3),  -- 吴九 - 访客
(8, 2);  -- 郑十 - 普通用户
