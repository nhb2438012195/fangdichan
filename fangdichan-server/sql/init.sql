CREATE DATABASE IF NOT EXISTS fangdichan_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fangdichan_db;

-- sys_user
CREATE TABLE sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        ENUM('ADMIN','AGENT','CUSTOMER') NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1-启用 0-禁用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- customer_profile
CREATE TABLE customer_profile (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL UNIQUE,
    real_name   VARCHAR(50),
    phone       VARCHAR(20),
    email       VARCHAR(100),
    buy_intent  JSON,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- company
CREATE TABLE company (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL UNIQUE,
    company_name    VARCHAR(100) NOT NULL,
    address         VARCHAR(255),
    contact_phone   VARCHAR(20),
    description     TEXT,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- property
CREATE TABLE property (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id      BIGINT       NOT NULL,
    title           VARCHAR(200) NOT NULL,
    location        VARCHAR(255) NOT NULL,
    district        VARCHAR(50),
    floor           VARCHAR(20)  COMMENT '如 1-3F/4-6F/7-10F',
    floor_total     INT,
    room_type       VARCHAR(50)  NOT NULL,
    area            DECIMAL(10,2),
    price           DECIMAL(15,2),
    unit_price      DECIMAL(10,2),
    is_vacant       BOOLEAN      DEFAULT TRUE,
    status          ENUM('PENDING','APPROVED','REJECTED','SOLD','OFF_MARKET') DEFAULT 'PENDING',
    description     TEXT,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(id)
);

-- property_image
CREATE TABLE property_image (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id BIGINT       NOT NULL,
    image_url   VARCHAR(500) NOT NULL,
    sort_order  INT          DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES property(id) ON DELETE CASCADE
);

-- purchase_order
CREATE TABLE purchase_order (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no    VARCHAR(32) NOT NULL UNIQUE,
    customer_id BIGINT      NOT NULL,
    property_id BIGINT      NOT NULL,
    status      ENUM('PENDING','CONFIRMED','CANCELLED','COMPLETED') DEFAULT 'PENDING',
    message     TEXT,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES sys_user(id),
    FOREIGN KEY (property_id) REFERENCES property(id)
);

-- favorite
CREATE TABLE favorite (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_customer_property (customer_id, property_id),
    FOREIGN KEY (customer_id) REFERENCES sys_user(id),
    FOREIGN KEY (property_id) REFERENCES property(id)
);

-- suggestion
CREATE TABLE suggestion (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id     BIGINT       NOT NULL,
    company_id      BIGINT       NOT NULL,
    desired_type    VARCHAR(50),
    desired_price_min DECIMAL(15,2),
    desired_price_max DECIMAL(15,2),
    content         TEXT         NOT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES sys_user(id),
    FOREIGN KEY (company_id) REFERENCES company(id)
);

-- report
CREATE TABLE report (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id BIGINT  NOT NULL,
    customer_id BIGINT  NOT NULL,
    reason      TEXT    NOT NULL,
    status      ENUM('PENDING','DISMISSED','PROCESSED') DEFAULT 'PENDING',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES property(id),
    FOREIGN KEY (customer_id) REFERENCES sys_user(id)
);

-- conversation
CREATE TABLE conversation (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id     BIGINT  NOT NULL,
    company_id      BIGINT  NOT NULL,
    property_id     BIGINT,
    status          ENUM('OPEN','CLOSED') DEFAULT 'OPEN',
    customer_unread INT     DEFAULT 0 COMMENT '客户未读消息数',
    agent_unread    INT     DEFAULT 0 COMMENT '房地产商未读消息数',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES sys_user(id),
    FOREIGN KEY (company_id) REFERENCES company(id),
    FOREIGN KEY (property_id) REFERENCES property(id)
);

-- message
CREATE TABLE message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT   NOT NULL,
    sender_id       BIGINT   NOT NULL,
    sender_role     ENUM('CUSTOMER','AGENT') NOT NULL,
    content         TEXT     NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE
);

-- system_config
CREATE TABLE system_config (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key  VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description VARCHAR(255),
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入默认管理员（密码占位，启动时通过 Initializer 用 BCrypt 加密覆盖）
INSERT INTO sys_user (username, password, role) VALUES ('admin', 'PLACEHOLDER', 'ADMIN');
