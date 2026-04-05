CREATE DATABASE IF NOT EXISTS cloud_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS skywalking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE cloud_demo;

CREATE TABLE IF NOT EXISTS account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(64) NOT NULL,
    total DECIMAL(18,2) NOT NULL,
    used DECIMAL(18,2) NOT NULL,
    residue DECIMAL(18,2) NOT NULL,
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_account_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS storage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    commodity_code VARCHAR(64) NOT NULL,
    total INT NOT NULL,
    used INT NOT NULL,
    residue INT NOT NULL,
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_storage_commodity_code (commodity_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(64) NOT NULL,
    commodity_code VARCHAR(64) NOT NULL,
    count INT NOT NULL,
    money DECIMAL(18,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_order_user_id (user_id),
    KEY idx_order_commodity_code (commodity_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS undo_log (
    branch_id BIGINT NOT NULL,
    xid VARCHAR(128) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB NOT NULL,
    log_status INT NOT NULL,
    log_created DATETIME NOT NULL,
    log_modified DATETIME NOT NULL,
    ext VARCHAR(100) DEFAULT NULL,
    PRIMARY KEY (branch_id, xid),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO account (user_id, total, used, residue)
VALUES ('u1001', 1000.00, 0.00, 1000.00),
       ('u1002', 1500.00, 0.00, 1500.00)
ON DUPLICATE KEY UPDATE total = VALUES(total), used = VALUES(used), residue = VALUES(residue);

INSERT INTO storage (commodity_code, total, used, residue)
VALUES ('C1001', 100, 0, 100),
       ('C1002', 200, 0, 200)
ON DUPLICATE KEY UPDATE total = VALUES(total), used = VALUES(used), residue = VALUES(residue);
