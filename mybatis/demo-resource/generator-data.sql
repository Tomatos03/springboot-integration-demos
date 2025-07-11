CREATE DATABASE IF NOT EXISTS my_batis_demo;

use mybatis_demo;

CREATE TABLE user (
                      id INT PRIMARY KEY AUTO_INCREMENT,
                      name VARCHAR(50) NOT NULL,
                      age INT,
                      email VARCHAR(100)
);

INSERT INTO user (name, age, email) VALUES ('Alice', 23, 'alice@example.com');
INSERT INTO user (name, age, email) VALUES ('Bob', 30, 'bob@example.com');
INSERT INTO user (name, age, email) VALUES ('Charlie', 27, 'charlie@example.com');
INSERT INTO user (name, age, email) VALUES ('Diana', 22, 'diana@example.com');
INSERT INTO user (name, age, email) VALUES ('Eve', 35, 'eve@example.com');

CREATE TABLE product (
                         id INT PRIMARY KEY AUTO_INCREMENT,
                         name VARCHAR(100) NOT NULL,
                         price DECIMAL(10,2) NOT NULL,
                         stock INT NOT NULL,
                         description VARCHAR(255)
);

INSERT INTO product (name, price, stock, description) VALUES ('Apple iPhone 15', 6999.00, 50, '最新款苹果手机');
INSERT INTO product (name, price, stock, description) VALUES ('Dell XPS 13', 8999.99, 30, '高端轻薄笔记本电脑');
INSERT INTO product (name, price, stock, description) VALUES ('Sony WH-1000XM5', 2999.00, 100, '降噪蓝牙耳机');
INSERT INTO product (name, price, stock, description) VALUES ('Kindle Paperwhite', 999.00, 80, '电子书阅读器');
INSERT INTO product (name, price, stock, description) VALUES ('Nintendo Switch', 2199.00, 40, '家用游戏主机');
