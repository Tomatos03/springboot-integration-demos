CREATE DATABASE IF NOT EXISTS seata DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE seata;

CREATE TABLE IF NOT EXISTS global_table (
    xid VARCHAR(128) NOT NULL,
    transaction_id BIGINT NULL,
    status TINYINT NOT NULL,
    application_id VARCHAR(32) NULL,
    transaction_service_group VARCHAR(32) NULL,
    transaction_name VARCHAR(128) NULL,
    timeout INT NULL,
    begin_time BIGINT NULL,
    application_data VARCHAR(2000) NULL,
    gmt_create DATETIME NULL,
    gmt_modified DATETIME NULL,
    PRIMARY KEY (xid),
    KEY idx_status_gmt_modified (status, gmt_modified),
    KEY idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS branch_table (
    branch_id BIGINT NOT NULL,
    xid VARCHAR(128) NOT NULL,
    transaction_id BIGINT NULL,
    resource_group_id VARCHAR(32) NULL,
    resource_id VARCHAR(256) NULL,
    branch_type VARCHAR(8) NULL,
    status TINYINT NULL,
    client_id VARCHAR(64) NULL,
    application_data VARCHAR(2000) NULL,
    gmt_create DATETIME NULL,
    gmt_modified DATETIME NULL,
    PRIMARY KEY (branch_id),
    KEY idx_xid (xid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS lock_table (
    row_key VARCHAR(128) NOT NULL,
    xid VARCHAR(128) NULL,
    transaction_id BIGINT NULL,
    branch_id BIGINT NOT NULL,
    resource_id VARCHAR(256) NULL,
    table_name VARCHAR(32) NULL,
    pk VARCHAR(36) NULL,
    status TINYINT NOT NULL DEFAULT 0,
    gmt_create DATETIME NULL,
    gmt_modified DATETIME NULL,
    PRIMARY KEY (row_key),
    KEY idx_status (status),
    KEY idx_branch_id (branch_id),
    KEY idx_xid (xid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS distributed_lock (
    lock_key CHAR(20) NOT NULL,
    lock_value VARCHAR(20) NOT NULL,
    expire BIGINT NULL,
    PRIMARY KEY (lock_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO distributed_lock (lock_key, lock_value, expire)
VALUES ('AsyncCommitting', ' ', 0),
       ('RetryCommitting', ' ', 0),
       ('RetryRollbacking', ' ', 0),
       ('TxTimeoutCheck', ' ', 0)
ON DUPLICATE KEY UPDATE lock_value = VALUES(lock_value), expire = VALUES(expire);
