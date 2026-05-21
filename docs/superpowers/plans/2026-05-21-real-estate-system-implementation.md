# 房地产客户购房查询系统 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) for syntax tracking.

**Goal:** 实现包含管理后台（浏览器）和桌面客户端（Electron）的房地产购房查询系统

**Architecture:** 单体 Spring Boot 后端（common → security → module/* 分层），Vue 3 + Element Plus 管理后台（浏览器），Vue 3 + Electron 桌面客户端（桌面风格 UI），MySQL 数据库，MinIO 对象存储，WebSocket 实时通信

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, MySQL 8.x, Spring Security + JWT, Vue 3 + Vite, Element Plus, Electron, ECharts, MinIO, WebSocket, Apache POI

---

## 部署配置（服务器信息）

### 服务器信息

| 项目 | 值 |
|------|-----|
| IP 地址 | `182.92.176.70` |
| 操作系统 | Linux |
| SSH 用户 | `root` |
| SSH 密码 | `nhb2438012195` |
| 部署方式 | Docker Compose |

### 服务端口映射

| 服务 | 内部端口 | 外部端口 | 说明 |
|------|---------|---------|------|
| MySQL | 3306 | 3306 | 数据库 |
| MinIO API | 9000 | 9000 | 对象存储 |
| MinIO Console | 9001 | 9001 | MinIO 管理面板 |
| Spring Boot | 8080 | 8080 | 后端 API |
| Admin Web (Nginx) | 80 | 80 | 管理后台前端 |
| Client (Electron) | — | — | 客户端直连后端 8080 |

### 服务凭证

| 服务 | 用户名 | 密码 |
|------|--------|------|
| MySQL root | `root` | `nhb2438012195` |
| MySQL 应用库 | `fangdichan_db` | — |
| MinIO root | `root` | `nhb2438012195` |
| MinIO Bucket | `fangdichan` | — |
| 管理员默认账号 | `admin` | `admin123`（启动时初始化） |

### Docker Compose 配置

```yaml
# docker-compose.yml（项目根目录）
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: nhb2438012195
      MYSQL_DATABASE: fangdichan_db
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./fangdichan-server/sql/init.sql:/docker-entrypoint-initdb.d/init.sql
    restart: always
    networks:
      - app-net

  minio:
    image: minio/minio
    environment:
      MINIO_ROOT_USER: root
      MINIO_ROOT_PASSWORD: nhb2438012195
    ports:
      - "9000:9000"
      - "9001:9001"
    command: server /data --console-address ":9001"
    volumes:
      - minio-data:/data
    restart: always
    networks:
      - app-net

  server:
    build: ./fangdichan-server
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
    depends_on:
      - mysql
      - minio
    restart: always
    networks:
      - app-net

  admin-web:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./fangdichan-admin-web/nginx/nginx.conf:/etc/nginx/conf.d/default.conf
      - ./fangdichan-admin-web/dist:/usr/share/nginx/html
    depends_on:
      - server
    restart: always
    networks:
      - app-net

volumes:
  mysql-data:
  minio-data:

networks:
  app-net:
```

### Spring Boot Dockerfile

```dockerfile
# fangdichan-server/Dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/fangdichan-server-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Nginx 配置（Admin Web）

```nginx
# fangdichan-admin-web/nginx/nginx.conf
server {
    listen 80;
    server_name 182.92.176.70;

    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://server:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location /ws/ {
        proxy_pass http://server:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

### application-prod.yml

```yaml
# fangdichan-server/src/main/resources/application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/fangdichan_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: nhb2438012195
    driver-class-name: com.mysql.cj.jdbc.Driver

minio:
  endpoint: http://182.92.176.70:9000
  access-key: root
  secret-key: nhb2438012195
  bucket-name: fangdichan
```

**说明：**
- MySQL 连接使用 Docker Compose 内部网络的服务名 `mysql`（容器间通信），无需通过公网 IP
- MinIO endpoint 使用公网 IP `http://182.92.176.70:9000`，因为图片 URL 需要客户端（管理后台浏览器、桌面端）可访问
- `application.yml` 中的 `jwt.secret` 和 `jwt.expiration` 等配置为环境无关项，保持默认值即可

### .env（可选，分离敏感信息）

如果希望将密码从 docker-compose.yml 分离出来，可创建 `.env` 文件：

```
# .env
MYSQL_ROOT_PASSWORD=nhb2438012195
MINIO_ROOT_PASSWORD=nhb2438012195
```

然后在 docker-compose.yml 中引用 `${MYSQL_ROOT_PASSWORD}`。

### 部署步骤

```bash
# 1. 构建后端
cd fangdichan-server && mvn clean package -DskipTests

# 2. 构建管理后台
cd fangdichan-admin-web && npm run build

# 3. 上传到服务器
scp -r fangdichan-server/target/fangdichan-server-1.0.0.jar root@182.92.176.70:/deploy/
scp fangdichan-server/Dockerfile root@182.92.176.70:/deploy/
scp -r fangdichan-admin-web/dist root@182.92.176.70:/deploy/admin-web/
scp fangdichan-admin-web/nginx/nginx.conf root@182.92.176.70:/deploy/
scp fangdichan-server/sql/init.sql root@182.92.176.70:/deploy/
scp docker-compose.yml root@182.92.176.70:/deploy/

# 4. 服务器上启动
ssh root@182.92.176.70 "cd /deploy && docker compose up -d"
```

---

## 测试策略

**后端测试框架**: JUnit 5 + Mockito + Spring Boot Test（`spring-boot-starter-test`）

**前端测试框架**: Vitest + @vue/test-utils（仅管理后台，桌面端暂不覆盖）

**测试目录结构**:
- `fangdichan-server/src/test/java/com/fdsc/` — 后端测试，包路径与主代码一致
- `fangdichan-admin-web/src/__tests__/` — 前端测试

**测试要求**:
- 每个 Service 方法至少一个正常路径 + 一个异常路径测试
- Controller 集成测试验证 HTTP 状态码和响应结构
- 测试不依赖外部数据库，使用 Mock 隔离
- 前一个 Task 的测试通过后，再进入下一个 Task

---

## 阶段一：项目初始化与基础设施

### Task 1: 初始化后端项目（fangdichan-server）

**Files:**

- Create: `fangdichan-server/pom.xml`
- Create: `fangdichan-server/src/main/java/com/fdsc/FdscApplication.java`
- Create: `fangdichan-server/src/main/resources/application.yml`
- Create: `fangdichan-server/src/main/resources/application-dev.yml`
- Create: `fangdichan-server/src/main/resources/application-prod.yml`

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    <groupId>com.fdsc</groupId>
    <artifactId>fangdichan-server</artifactId>
    <version>1.0.0</version>
    <name>fangdichan-server</name>
    <description>房地产购房查询系统后端</description>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <jjwt.version>0.12.3</jjwt.version>
        <minio.version>8.5.7</minio.version>
    </properties>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- MinIO -->
        <dependency>
            <groupId>io.minio</groupId>
            <artifactId>minio</artifactId>
            <version>${minio.version}</version>
        </dependency>

        <!-- POI -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>5.2.5</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建启动类**

```java
package com.fdsc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FdscApplication {
    public static void main(String[] args) {
        SpringApplication.run(FdscApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建配置文件**

```yaml
# application.yml
server:
  port: 8080

spring:
  profiles:
    active: dev
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: fdsc-jwt-secret-key-2026-this-is-a-very-long-secret-key
  expiration: 86400000

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: fangdichan
```

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fangdichan_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
```

- [ ] **Step 4: 验证项目启动**

Run: `cd fangdichan-server && mvn clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git init
git add fangdichan-server/pom.xml fangdichan-server/src/
git commit -m "feat: init fangdichan-server Spring Boot project"
```

---

### Task 2: 初始化前端管理后台（fangdichan-admin-web）

**Files:**

- Create: `fangdichan-admin-web/package.json`
- Create: `fangdichan-admin-web/vite.config.js`
- Create: `fangdichan-admin-web/index.html`
- Create: `fangdichan-admin-web/src/main.js`
- Create: `fangdichan-admin-web/src/App.vue`

- [ ] **Step 1: 初始化 Vue 3 项目**

Run: `npm create vite@latest fangdichan-admin-web -- --template vue`
Or create manually:

```json
// package.json
{
  "name": "fangdichan-admin-web",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.0",
    "pinia": "^2.1.0",
    "element-plus": "^2.4.0",
    "axios": "^1.6.0",
    "echarts": "^5.4.0",
    "@element-plus/icons-vue": "^2.3.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0"
  }
}
```

- [ ] **Step 2: 创建入口文件**

```javascript
// src/main.js
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.use(createPinia())
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
app.mount('#app')
```

```vue
<!-- src/App.vue -->
<template>
  <router-view />
</template>
```

```javascript
// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true }
    }
  }
})
```

- [ ] **Step 3: 安装依赖并验证**

Run: `cd fangdichan-admin-web && npm install && npm run build`
Expected: 构建成功

- [ ] **Step 4: Commit**

```bash
git add fangdichan-admin-web/
git commit -m "feat: init fangdichan-admin-web Vue 3 project"
```

---

### Task 3: 初始化客户桌面端（fangdichan-client）

**Files:**

- Create: `fangdichan-client/package.json`
- Create: `fangdichan-client/vite.config.js`
- Create: `fangdichan-client/electron/main.js`
- Create: `fangdichan-client/electron/preload.js`
- Create: `fangdichan-client/index.html`
- Create: `fangdichan-client/src/main.js`
- Create: `fangdichan-client/src/App.vue`

- [ ] **Step 1: 初始化项目配置**

```json
// package.json
{
  "name": "fangdichan-client",
  "private": true,
  "version": "1.0.0",
  "main": "electron/main.js",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "electron:dev": "concurrently \"vite\" \"wait-on http://localhost:5173 && electron .\"",
    "electron:build": "vite build && electron-builder"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.0",
    "pinia": "^2.1.0",
    "element-plus": "^2.4.0",
    "axios": "^1.6.0",
    "echarts": "^5.4.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0",
    "electron": "^28.0.0",
    "concurrently": "^8.2.0",
    "wait-on": "^7.2.0"
  }
}
```

- [ ] **Step 2: 创建 Electron 主进程**

```javascript
// electron/main.js
const { app, BrowserWindow } = require('electron')
const path = require('path')

function createWindow() {
    const win = new BrowserWindow({
        width: 1200,
        height: 800,
        minWidth: 900,
        minHeight: 600,
        frame: false,
        webPreferences: {
            preload: path.join(__dirname, 'preload.js'),
            contextIsolation: true,
            nodeIntegration: false
        }
    })
    if (process.env.VITE_DEV_SERVER_URL) {
        win.loadURL(process.env.VITE_DEV_SERVER_URL)
    } else {
        win.loadFile(path.join(__dirname, '../dist/index.html'))
    }
}

app.whenReady().then(createWindow)
app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit() })
```

- [ ] **Step 3: 安装依赖并验证**

Run: `cd fangdichan-client && npm install`
Expected: 无报错

- [ ] **Step 4: Commit**

```bash
git add fangdichan-client/
git commit -m "feat: init fangdichan-client Electron + Vue 3 project"
```

---

### Task 4: 初始化数据库 & 公共后端基础

**Files:**

- Create: `fangdichan-server/sql/init.sql`
- Create: `fangdichan-server/src/main/java/com/fdsc/common/result/Result.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/common/exception/BusinessException.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/common/exception/GlobalExceptionHandler.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/common/config/CorsConfig.java`

- [ ] **Step 1: 创建数据库初始化 SQL**

```sql
-- sql/init.sql
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
```

- [ ] **Step 2: 创建统一响应封装**

```java
package com.fdsc.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
```

- [ ] **Step 3: 创建业务异常类**

```java
package com.fdsc.common.exception;

public class BusinessException extends RuntimeException {
    private final int code;
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    public int getCode() { return code; }
}
```

- [ ] **Step 4: 创建全局异常处理器**

```java
package com.fdsc.common.exception;

import com.fdsc.common.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(500, "服务器内部错误");
    }
}
```

- [ ] **Step 5: 创建 CORS 配置**

```java
package com.fdsc.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **Step 6: 创建通用分页结果类 PageResult**

```java
package com.fdsc.common.result;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private long page;
    private long size;
    private long total;
    private long pages;

    public static <T> PageResult<T> of(List<T> list, long page, long size, long total) {
        PageResult<T> r = new PageResult<>();
        r.setList(list);
        r.setPage(page);
        r.setSize(size);
        r.setTotal(total);
        r.setPages((long) Math.ceil((double) total / size));
        return r;
    }
}
```

- [ ] **Step 7: 执行 SQL 初始化**

Run: `mysql -u root -p < fangdichan-server/sql/init.sql`
Expected: 数据库创建成功，11 张表建立

- [ ] **Step 8: Commit**

```bash
git add fangdichan-server/sql/ fangdichan-server/src/main/java/com/fdsc/common/
git commit -m "feat: add database init script and backend common module"
```

---

## 阶段二：后端安全认证

### Task 5: JWT + Spring Security

**Files:**

- Create: `fangdichan-server/src/main/java/com/fdsc/security/JwtTokenProvider.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/security/JwtAuthenticationFilter.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/security/SecurityConfig.java`

- [ ] **Step 1: 创建 JwtTokenProvider**

```java
package com.fdsc.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long expiration;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(Long userId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try { parseClaims(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
```

- [ ] **Step 2: 创建 JWT 认证过滤器**

```java
package com.fdsc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 3: 创建 SecurityConfig**

```java
package com.fdsc.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/agent/**").hasAnyRole("ADMIN", "AGENT")
                .requestMatchers("/api/customer/**").hasAnyRole("ADMIN", "CUSTOMER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 4: 编译验证**

Run: `cd fangdichan-server && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add fangdichan-server/src/main/java/com/fdsc/security/
git commit -m "feat: implement JWT authentication and Spring Security config"
```

---

### Task 5.5: 为已完成模块（Tasks 1-5）补充测试

**Files:**
- Create: `fangdichan-server/src/test/java/com/fdsc/security/JwtTokenProviderTest.java`

- [ ] **Step 1: 创建 JWT 工具测试**

```java
package com.fdsc.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider("test-secret-key-for-unit-testing-purposes-only!", 3600000);
    }

    @Test
    void generateAndValidateToken_shouldSucceed() {
        String token = jwtTokenProvider.generateToken(1L, "ADMIN");
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectId() {
        String token = jwtTokenProvider.generateToken(42L, "CUSTOMER");
        assertEquals(42L, jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    void getRoleFromToken_shouldReturnCorrectRole() {
        String token = jwtTokenProvider.generateToken(1L, "AGENT");
        assertEquals("AGENT", jwtTokenProvider.getRoleFromToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid-token"));
    }
}
```

- [ ] **Step 2: 运行测试**

Run: `cd fangdichan-server && mvn test -q`
Expected: BUILD SUCCESS，所有 JWT 测试通过

- [ ] **Step 3: Commit**

```bash
git add fangdichan-server/src/test/java/com/fdsc/security/
git commit -m "test: add JWT token provider unit tests"
```

---

### Task 6: 用户登录注册接口

**Files:**

- Create: `fangdichan-server/src/main/java/com/fdsc/module/user/entity/SysUser.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/user/entity/CustomerProfile.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/user/mapper/UserMapper.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/user/dto/LoginRequest.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/user/dto/LoginResponse.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/user/service/UserService.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/user/service/impl/UserServiceImpl.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/user/controller/AuthController.java`

- [ ] **Step 1: 创建 User 实体**

```java
package com.fdsc.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 CustomerProfile 实体**

```java
package com.fdsc.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("customer_profile")
public class CustomerProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String realName;
    private String phone;
    private String email;
    private String buyIntent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 3: 创建 UserMapper**

```java
package com.fdsc.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fdsc.module.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<SysUser> {}
```

- [ ] **Step 4: 创建 DTO 类**

```java
// LoginRequest.java
package com.fdsc.module.user.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}

// LoginResponse.java
package com.fdsc.module.user.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private Long userId;
}
```

- [ ] **Step 5: 创建 UserService**

```java
package com.fdsc.module.user.service;

import com.fdsc.module.user.dto.LoginRequest;
import com.fdsc.module.user.dto.LoginResponse;

public interface UserService {
    LoginResponse login(LoginRequest request);
    void register(LoginRequest request, String role);
}
```

- [ ] **Step 6: 创建 UserServiceImpl**

```java
package com.fdsc.module.user.service.impl;

import com.fdsc.common.exception.BusinessException;
import com.fdsc.module.user.dto.LoginRequest;
import com.fdsc.module.user.dto.LoginResponse;
import com.fdsc.module.user.entity.CustomerProfile;
import com.fdsc.module.user.entity.SysUser;
import com.fdsc.module.user.mapper.CustomerProfileMapper;
import com.fdsc.module.user.mapper.UserMapper;
import com.fdsc.module.user.service.UserService;
import com.fdsc.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final CustomerProfileMapper customerProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账户已被禁用");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());
        return new LoginResponse(token, user.getRole(), user.getId());
    }

    @Override
    public void register(LoginRequest request, String role) {
        SysUser existing = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (existing != null) {
            throw new BusinessException(400, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus(1);
        userMapper.insert(user);

        // 客户注册时同步创建空 profile
        if ("CUSTOMER".equals(role)) {
            CustomerProfile profile = new CustomerProfile();
            profile.setUserId(user.getId());
            customerProfileMapper.insert(profile);
        }
    }
}
```

- [ ] **Step 7: 创建 AuthController**

```java
package com.fdsc.module.user.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.user.dto.LoginRequest;
import com.fdsc.module.user.dto.LoginResponse;
import com.fdsc.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody LoginRequest request,
                              @RequestParam(defaultValue = "CUSTOMER") String role) {
        userService.register(request, role);
        return Result.success(null);
    }
}
```

- [ ] **Step 8: 创建 AdminInitializer（启动时初始化管理员密码）**

```java
package com.fdsc.module.user.service.impl;

import com.fdsc.module.user.entity.SysUser;
import com.fdsc.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        SysUser admin = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "admin"));
        if (admin != null && "PLACEHOLDER".equals(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode("admin123"));
            userMapper.updateById(admin);
            System.out.println("✅ 管理员初始密码已初始化 (admin/admin123)");
        }
    }
}
```

- [ ] **Step 9: 编译验证**

Run: `cd fangdichan-server && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 10: 创建 Service 单元测试**

**Files:**
- Create: `fangdichan-server/src/test/java/com/fdsc/module/user/service/UserServiceImplTest.java`

```java
package com.fdsc.module.user.service;

import com.fdsc.common.exception.BusinessException;
import com.fdsc.module.user.dto.LoginRequest;
import com.fdsc.module.user.entity.SysUser;
import com.fdsc.module.user.mapper.UserMapper;
import com.fdsc.module.user.mapper.CustomerProfileMapper;
import com.fdsc.module.user.service.impl.UserServiceImpl;
import com.fdsc.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserMapper userMapper;
    @Mock private CustomerProfileMapper customerProfileMapper;
    @Mock private JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, customerProfileMapper, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void login_shouldSucceedWithValidCredentials() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setRole("ADMIN");
        user.setStatus(1);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(jwtTokenProvider.generateToken(1L, "ADMIN")).thenReturn("mock-token");

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");
        var response = userService.login(request);

        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void login_shouldThrowWithWrongPassword() {
        SysUser user = new SysUser();
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setStatus(1);
        when(userMapper.selectOne(any())).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");
        assertThrows(BusinessException.class, () -> userService.login(request));
    }

    @Test
    void register_shouldThrowWhenUsernameExists() {
        when(userMapper.selectOne(any())).thenReturn(new SysUser());

        LoginRequest request = new LoginRequest();
        request.setUsername("existing");
        request.setPassword("pass123");
        assertThrows(BusinessException.class, () -> userService.register(request, "CUSTOMER"));
    }
}
```

- [ ] **Step 11: 创建 Controller 集成测试**

**Files:**
- Create: `fangdichan-server/src/test/java/com/fdsc/module/user/controller/AuthControllerTest.java`

```java
package com.fdsc.module.user.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.user.dto.LoginRequest;
import com.fdsc.module.user.dto.LoginResponse;
import com.fdsc.module.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserService userService;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void login_shouldReturn200() throws Exception {
        when(userService.login(any())).thenReturn(new LoginResponse("token", "ADMIN", 1L));

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        mockMvc.perform(post("/api/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
```

- [ ] **Step 12: 运行测试**

Run: `cd fangdichan-server && mvn test -q`
Expected: BUILD SUCCESS，所有测试通过

- [ ] **Step 13: 启动并测试登录**

Run: `mvn spring-boot:run`，然后用 `curl -X POST http://localhost:8080/api/public/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'`
Expected: 返回 200 + token

- [ ] **Step 13: Commit**

```bash
git add fangdichan-server/src/main/java/com/fdsc/module/user/
git add fangdichan-server/src/test/java/com/fdsc/module/user/
git commit -m "feat: implement user auth module (login/register/JWT)"
```

---

## 阶段三：后端业务模块（以下每个模块为一个 Task）

### Task 7: 客户信息管理模块

**Files:**

- Create: `fangdichan-server/src/main/java/com/fdsc/module/user/controller/CustomerController.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/user/mapper/CustomerProfileMapper.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/user/service/CustomerProfileService.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/user/service/impl/CustomerProfileServiceImpl.java`

- [ ] **Step 1: 创建 CustomerProfileMapper**

```java
package com.fdsc.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fdsc.module.user.entity.CustomerProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerProfileMapper extends BaseMapper<CustomerProfile> {}
```

- [ ] **Step 2: 创建 CustomerProfileService + impl**

```java
public interface CustomerProfileService {
    CustomerProfile getProfile(Long userId);
    void updateProfile(Long userId, CustomerProfile profile);
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
```

实现中包含：查询个人资料、更新资料、修改密码（验证原密码）。

- [ ] **Step 3: 创建 CustomerController**

```java
@RestController
@RequestMapping("/api/customer/profile")
public class CustomerController {
    // GET /api/customer/profile - 获取个人信息(从JWT获取userId)
    // PUT /api/customer/profile - 更新个人信息
    // PUT /api/customer/profile/password - 修改密码
}
```

- [ ] **Step 4: 编译验证**

Run: `cd fangdichan-server && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 创建 Service 测试**

测试模式参考 Task 6 Step 10（Mock Mapper → 验证业务逻辑），对应测试该任务的 Service 类。

- [ ] **Step 6: 运行测试**

Run: `cd fangdichan-server && mvn test -q`
Expected: BUILD SUCCESS，全部测试通过

- [ ] **Step 7: Commit**

```bash
git add fangdichan-server/src/
git commit -m "feat: add module and tests"
```

---

### Task 8: 房地产公司模块

**Files:**

- Create: `fangdichan-server/src/main/java/com/fdsc/module/company/entity/Company.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/company/mapper/CompanyMapper.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/company/service/CompanyService.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/company/service/impl/CompanyServiceImpl.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/company/controller/CompanyController.java`

- [ ] **Step 1: 创建 Company 实体**

```java
@Data
@TableName("company")
public class Company {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String companyName;
    private String address;
    private String contactPhone;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 CompanyMapper**

```java
@Mapper
public interface CompanyMapper extends BaseMapper<Company> {}
```

- [ ] **Step 3: 创建 CompanyService + impl**

```java
public interface CompanyService {
    Company getCompanyByUserId(Long userId);
    Company getCompanyById(Long id);
    void updateCompany(Long userId, Company company);
    List<Company> listAll();
}
```

- [ ] **Step 4: 创建两个 Controller**

```java
// AgentController - 房地产商自己管理公司
@RestController
@RequestMapping("/api/agent/company")
public class AgentCompanyController {
    // GET /api/agent/company - 获取自己的公司信息
    // PUT /api/agent/company - 更新公司信息
}

// CustomerCompanyController - 客户查看公司信息
@RestController
@RequestMapping("/api/customer/company")
public class CustomerCompanyController {
    // GET /api/customer/company/list - 所有公司列表
    // GET /api/customer/company/{id} - 公司详情
}
```

- [ ] **Step 5: 编译验证**

Run: `cd fangdichan-server && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: 创建测试 + 运行测试**

Run: `cd fangdichan-server && mvn test -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

---

### Task 9: 房源模块（CRUD + 审核流程 + 图片上传）

**Files:**

- Create: `fangdichan-server/src/main/java/com/fdsc/module/property/entity/Property.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/property/entity/PropertyImage.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/property/mapper/PropertyMapper.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/property/mapper/PropertyImageMapper.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/property/service/PropertyService.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/property/service/impl/PropertyServiceImpl.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/property/controller/AgentPropertyController.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/property/controller/AdminPropertyController.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/property/controller/CustomerPropertyController.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/common/config/MinioConfig.java`

- [ ] **Step 1: 创建 Property / PropertyImage 实体**

Property 字段映射所有表字段。注意 `unit_price` 在 setter 或 service 中自动计算：`price / area`。

- [ ] **Step 2: 创建 PropertyMapper / PropertyImageMapper（BaseMapper）**
- [ ] **Step 3: 创建 MinioConfig**

```java
package com.fdsc.common.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;
    @Value("${minio.bucket-name}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @PostConstruct
    public void initBucket() {
        try {
            MinioClient client = minioClient();
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("✅ MinIO bucket '{}' 已自动创建", bucketName);
            }
        } catch (Exception e) {
            log.warn("⚠️ MinIO bucket 初始化失败: {}", e.getMessage());
        }
    }
}
```

- [ ] **Step 4: 创建 PropertyService**

包含方法：

- `createProperty(Long companyId, Property property)` — 创建房源，初始 status=PENDING
- `updateProperty(Long companyId, Long propertyId, Property property)` — 修改
- `setOffMarket(Long companyId, Long propertyId)` — 下架
- `approveProperty(Long adminId, Long propertyId)` — 管理员审核通过
- `rejectProperty(Long adminId, Long propertyId)` — 管理员审核拒绝
- `listByCompany(Long companyId, int page, int size)` — 某公司房源分页
- `search(String keyword, String district, String roomType, BigDecimal priceMin, BigDecimal priceMax, BigDecimal areaMin, BigDecimal areaMax, int page, int size)` — 客户搜索（动态 WHERE，keyword 模糊匹配 title 和 location 字段）
- `getDetail(Long propertyId)` — 房源详情（含图片列表）
- `uploadImage(MultipartFile file)` — 上传到 MinIO，返回 URL
- `deleteImage(Long imageId)` — 删除图片

- [ ] **Step 5: 创建图片上传控制器**

```java
@RestController
@RequestMapping("/api/agent/property")
public class AgentPropertyController {
    // CRUD 房源、上传/删除图片
    // POST /api/agent/property - 创建
    // PUT /api/agent/property/{id} - 修改
    // PUT /api/agent/property/{id}/off-market - 下架
    // GET /api/agent/property/list - 我的房源列表
    // POST /api/agent/property/image/upload - 上传图片
    // DELETE /api/agent/property/image/{id} - 删除图片
}
```

```java
@RestController
@RequestMapping("/api/admin/property")
public class AdminPropertyController {
    // GET /api/admin/property/pending - 待审核列表
    // PUT /api/admin/property/{id}/approve - 通过
    // PUT /api/admin/property/{id}/reject - 拒绝
}
```

```java
@RestController
@RequestMapping("/api/customer/property")
public class CustomerPropertyController {
    // GET /api/customer/property/search - 搜索（带分页和筛选参数）
    // GET /api/customer/property/{id} - 详情
    // GET /api/customer/property/recommended - 首页推荐
}
```

- [ ] **Step 6: 编译验证**

Run: `cd fangdichan-server && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: 创建测试 + 运行测试**

为该模块核心 Service（PropertyService）编写单元测试，覆盖 createProperty、search、approveProperty 等方法，Mock Mapper 层验证业务逻辑。

Run: `cd fangdichan-server && mvn test -q`
Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

---

### Task 10: 购房订单模块

**Files:**

- Create: `fangdichan-server/src/main/java/com/fdsc/module/order/entity/PurchaseOrder.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/order/mapper/PurchaseOrderMapper.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/order/service/PurchaseOrderService.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/order/service/impl/PurchaseOrderServiceImpl.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/order/controller/CustomerOrderController.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/order/controller/AgentOrderController.java`

- [ ] **Step 1: 创建实体 + Mapper**
- [ ] **Step 2: 创建 Service**

```java
public interface PurchaseOrderService {
    void createOrder(Long customerId, Long propertyId, String message);
    void confirmOrder(Long agentId, Long orderId);
    void cancelOrder(Long userId, Long orderId, String role);
    PageResult<PurchaseOrder> listByCustomer(Long customerId, int page, int size);
    PageResult<PurchaseOrder> listByCompany(Long companyId, int page, int size);
}
```

流程：客户提交 → 检查房源状态为 `APPROVED` 且无其他 `PENDING` 或 `CONFIRMED` 订单 → 创建 `PENDING` 订单 → 房地产商确认 → 状态变为 `CONFIRMED`，同时房源标记 `SOLD`

`createOrder` 实现中的检查逻辑：
```java
// 检查房源状态
Property property = propertyMapper.selectById(propertyId);
if (property == null || !"APPROVED".equals(property.getStatus())) {
    throw new BusinessException(400, "该房源不可购买");
}
// 检查是否有其他待处理订单
Long count = purchaseOrderMapper.selectCount(
    new LambdaQueryWrapper<PurchaseOrder>()
        .eq(PurchaseOrder::getPropertyId, propertyId)
        .in(PurchaseOrder::getStatus, "PENDING", "CONFIRMED"));
if (count > 0) {
    throw new BusinessException(400, "该房源已有客户在交易中");
}
```

- [ ] **Step 3: 创建 Controller**
- [ ] **Step 4: 编译验证**

Run: `cd fangdichan-server && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 创建测试 + 运行测试**

Run: `cd fangdichan-server && mvn test -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

---

### Task 11: 收藏 & 建议 & 举报模块

**Files:**

- Create: `fangdichan-server/src/main/java/com/fdsc/module/favorite/` (entity, mapper, service, controller)
- Create: `fangdichan-server/src/main/java/com/fdsc/module/suggestion/` (entity, mapper, service, controller)
- Create: `fangdichan-server/src/main/java/com/fdsc/module/report/` (entity, mapper, service, controller)

- [ ] **Step 1: 收藏模块**

```java
@RestController
@RequestMapping("/api/customer/favorite")
public class CustomerFavoriteController {
    // POST /api/customer/favorite/{propertyId} - 收藏
    // DELETE /api/customer/favorite/{propertyId} - 取消收藏
    // GET /api/customer/favorite/list - 收藏列表（分页）
}
```

Favorite 实体：id, customerId, propertyId, createdAt
FavoriteMapper：BaseMapper
Service：toggleFavorite（不存在则添加，存在则删除）、listByCustomer

- [ ] **Step 2: 建议模块**

```java
@RestController
@RequestMapping("/api/customer/suggestion")
public class CustomerSuggestionController {
    // POST /api/customer/suggestion - 提交建议
    // GET /api/customer/suggestion/list - 查看自己的建议
}

@RestController
@RequestMapping("/api/agent/suggestion")
public class AgentSuggestionController {
    // GET /api/agent/suggestion/list - 查看客户对自己的建议
}
```

Suggestion 实体：id, customerId, companyId, desiredType, desiredPriceMin, desiredPriceMax, content, createdAt
Service：create、listByCustomer、listByCompany

- [ ] **Step 3: 举报模块**

```java
@RestController
@RequestMapping("/api/customer/report")
public class CustomerReportController {
    // POST /api/customer/report - 举报房源
}

@RestController
@RequestMapping("/api/admin/report")
public class AdminReportController {
    // GET /api/admin/report/pending - 待处理列表
    // PUT /api/admin/report/{id}/dismiss - 驳回
    // PUT /api/admin/report/{id}/process - 处理(下架房源)
}
```

Report 实体：id, propertyId, customerId, reason, status, createdAt, updatedAt

- [ ] **Step 4: 编译验证**

Run: `cd fangdichan-server && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 创建测试 + 运行测试**

Run: `cd fangdichan-server && mvn test -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

---

### Task 12: 消息模块 + WebSocket

**Files:**

- Create: `fangdichan-server/src/main/java/com/fdsc/module/message/entity/Conversation.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/message/entity/Message.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/message/mapper/ConversationMapper.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/message/mapper/MessageMapper.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/message/service/MessageService.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/message/service/impl/MessageServiceImpl.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/message/controller/CustomerMessageController.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/message/controller/AgentMessageController.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/common/config/WebSocketConfig.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/security/WebSocketAuthInterceptor.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/websocket/MessageWebSocketHandler.java`

- [ ] **Step 1: 创建实体 + Mapper**
- [ ] **Step 2: 创建 WebSocket 配置**

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageHandler(), "/ws")
                .addInterceptors(webSocketAuthInterceptor())
                .setAllowedOrigins("*");
    }
}
```

- [ ] **Step 3: 创建 WebSocket 认证拦截器**

```java
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        String token = extractTokenFromQuery(query);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            attributes.put("userId", jwtTokenProvider.getUserIdFromToken(token));
            attributes.put("role", jwtTokenProvider.getRoleFromToken(token));
            return true;
        }
        return false;
    }
}
```

- [ ] **Step 4: 创建 MessageWebSocketHandler**

```java
@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {
    // 管理在线用户连接: ConcurrentHashMap<Long, WebSocketSession>
    // afterConnectionEstablished: 用户上线，存入 map
    // handleTextMessage: 接收消息，保存到数据库，推送给接收方
    // afterConnectionClosed: 用户离线，从 map 移除
}
```

- [ ] **Step 5: 创建 MessageService**

```java
public interface MessageService {
    Conversation createConversation(Long customerId, Long companyId, Long propertyId);
    void sendMessage(Long conversationId, Long senderId, String senderRole, String content);
    List<Message> getMessages(Long conversationId, int page, int size);
    List<Conversation> listConversations(Long userId, String role);
}
```

发送消息流程：保存 message → 递增 conversation 中接收方的 unread 计数 → 通过 WebSocket 推送给接收方 → 接收方阅读消息时将对应 unread 字段清零。

listConversations 返回时附带各会话的未读计数。

- [ ] **Step 6: 创建客户和房地产商的 Controller**
- [ ] **Step 7: 编译验证**

Run: `cd fangdichan-server && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 8: 创建测试 + 运行测试**

Run: `cd fangdichan-server && mvn test -q`
Expected: BUILD SUCCESS

- [ ] **Step 9: Commit**

---

### Task 13: 关联分析模块

**Files:**

- Create: `fangdichan-server/src/main/java/com/fdsc/module/analysis/controller/AnalysisController.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/analysis/service/AnalysisService.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/analysis/service/impl/AnalysisServiceImpl.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/analysis/dto/VacancyAnalysisVO.java`

- [ ] **Step 1: 创建 VO 类**

```java
@Data
public class VacancyAnalysisVO {
    private List<VacancyItem> district;
    private List<VacancyItem> floor;
    private List<VacancyItem> roomType;

    @Data
    public static class VacancyItem {
        private String name;
        private long total;
        private long vacant;
        private double vacancyRate;
    }
}
```

- [ ] **Step 2: 创建 AnalysisService**

```java
public interface AnalysisService {
    VacancyAnalysisVO getVacancyAnalysis(Long companyId);
}
```

实现中用 MyBatis-Plus 的 LambdaQueryWrapper 按 district/floor/roomType 分组统计：

```java
// 按区域统计
List<Map<String, Object>> districtStats = propertyMapper.selectMaps(
    new QueryWrapper<Property>()
        .eq("company_id", companyId)
        .eq("status", "APPROVED")
        .select("district, COUNT(*) as total, SUM(CASE WHEN is_vacant = 1 THEN 1 ELSE 0 END) as vacant")
        .groupBy("district")
);
```

- [ ] **Step 3: 创建 AnalysisController**

```java
@RestController
@RequestMapping("/api/agent/analysis")
public class AnalysisController {
    @GetMapping("/vacancy")
    public Result<VacancyAnalysisVO> getVacancyAnalysis(@AuthenticationPrincipal Long userId) {
        // 通过 userId 找到 companyId，然后返回分析数据
    }
}
```

- [ ] **Step 4: 编译验证**

Run: `cd fangdichan-server && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 创建测试 + 运行测试**

Run: `cd fangdichan-server && mvn test -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

---

### Task 14: 报表导出模块

**Files:**

- Create: `fangdichan-server/src/main/java/com/fdsc/module/report/controller/ReportController.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/report/service/ReportService.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/report/service/impl/ReportServiceImpl.java`

- [ ] **Step 1: 创建报表服务**

```java
public interface ReportService {
    byte[] exportPropertyList(Long companyId, String district, String roomType);
    byte[] exportAnalysisReport(Long companyId);
}
```

使用 Apache POI 创建 Excel：创建 Workbook → 创建 Sheet → 创建 Row/Cell → 写入数据 → 转为 byte[]

- [ ] **Step 2: 创建 Controller**

```java
@RestController
@RequestMapping("/api/agent/report")
public class ReportController {
    @GetMapping("/property-export")
    public ResponseEntity<byte[]> exportPropertyList(...) {
        byte[] data = reportService.exportPropertyList(...);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=房源报表.xlsx")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(data);
    }
}
}
```

- [ ] **Step 3: 编译验证**

Run: `cd fangdichan-server && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 创建测试 + 运行测试**

Run: `cd fangdichan-server && mvn test -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

---

### Task 15: 系统配置模块（管理员）

**Files:**

- Create: `fangdichan-server/src/main/java/com/fdsc/module/config/entity/SystemConfig.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/config/mapper/SystemConfigMapper.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/config/service/SystemConfigService.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/config/service/impl/SystemConfigServiceImpl.java`
- Create: `fangdichan-server/src/main/java/com/fdsc/module/config/controller/SystemConfigController.java`

- [ ] **Step 1: 创建实体 + Mapper**

```java
@Data
@TableName("system_config")
public class SystemConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 Service**

```java
public interface SystemConfigService {
    String getConfig(String key);
    void setConfig(String key, String value, String description);
    List<SystemConfig> listAll();
}
```

- [ ] **Step 3: 创建 Controller**

```java
@RestController
@RequestMapping("/api/admin/config")
public class SystemConfigController {
    // GET /api/admin/config - 所有配置
    // PUT /api/admin/config/{key} - 更新配置
}
```

- [ ] **Step 4: 编译验证**

Run: `cd fangdichan-server && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 创建测试 + 运行测试**

Run: `cd fangdichan-server && mvn test -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

---

## 阶段四：管理后台前端

### Task 16: 管理后台布局 + 登录 + 路由

**Files:**

- Create: `fangdichan-admin-web/src/layout/AdminLayout.vue`
- Create: `fangdichan-admin-web/src/views/login/Login.vue`
- Create: `fangdichan-admin-web/src/router/index.js`
- Create: `fangdichan-admin-web/src/store/auth.js`
- Create: `fangdichan-admin-web/src/api/auth.js`
- Create: `fangdichan-admin-web/src/api/request.js`

- [ ] **Step 1: 创建 axios 封装**

```javascript
// src/api/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({ baseURL: '/api' })

request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

request.interceptors.response.use(
    res => res.data,
    err => {
        if (err.response?.status === 401) {
            localStorage.removeItem('token')
            router.push('/login')
        }
        ElMessage.error(err.response?.data?.msg || '请求失败')
        return Promise.reject(err)
    }
)

export default request
```

- [ ] **Step 2: 创建 auth store**

```javascript
// src/store/auth.js
import { defineStore } from 'pinia'
import request from '../api/request'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        role: localStorage.getItem('role') || '',
        userId: localStorage.getItem('userId') || ''
    }),
    actions: {
        async login(username, password) {
            const res = await request.post('/public/login', { username, password })
            this.token = res.data.token
            this.role = res.data.role
            this.userId = res.data.userId
            localStorage.setItem('token', res.data.token)
            localStorage.setItem('role', res.data.role)
            localStorage.setItem('userId', res.data.userId)
        },
        logout() {
            this.token = ''
            this.role = ''
            this.userId = ''
            localStorage.clear()
        }
    }
})
```

- [ ] **Step 3: 创建路由**

```javascript
// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layout/AdminLayout.vue'

const routes = [
    { path: '/login', component: () => import('../views/login/Login.vue') },
    { path: '/register', component: () => import('../views/login/Register.vue') },
    {
        path: '/',
        component: AdminLayout,
        children: [
            { path: '', redirect: '/dashboard' },
            { path: 'dashboard', component: () => import('../views/dashboard/Dashboard.vue') },
            { path: 'user', component: () => import('../views/user/UserManagement.vue'), meta: { role: 'ADMIN' } },
            { path: 'company', component: () => import('../views/company/CompanyInfo.vue'), meta: { role: 'AGENT' } },
            { path: 'property', component: () => import('../views/property/PropertyManagement.vue'), meta: { role: 'AGENT' } },
            { path: 'audit', component: () => import('../views/audit/AuditManagement.vue'), meta: { role: 'ADMIN' } },
            { path: 'order', component: () => import('../views/order/OrderManagement.vue'), meta: { role: 'AGENT' } },
            { path: 'analysis', component: () => import('../views/analysis/AnalysisView.vue'), meta: { role: 'AGENT' } },
            { path: 'report-handle', component: () => import('../views/report/ReportManagement.vue'), meta: { role: 'ADMIN' } },
            { path: 'message', component: () => import('../views/message/MessageView.vue') },
            { path: 'config', component: () => import('../views/config/SystemConfig.vue'), meta: { role: 'ADMIN' } },
        ]
    }
]

const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.path !== '/login' && !token) return next('/login')
    next()
})

export default router
```

- [ ] **Step 4: 创建 Login.vue**

```vue
<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>购房通管理后台</h2>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item><el-input v-model="form.username" placeholder="用户名" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" /></el-form-item>
        <el-button type="primary" @click="handleLogin" :loading="loading">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../store/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const handleLogin = async () => {
    loading.value = true
    try {
        await auth.login(form.username, form.password)
        router.push('/')
    } finally { loading.value = false }
}
</script>
```

- [ ] **Step 5: 创建注册页面 Register.vue**

```vue
<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>注册账号</h2>
      <el-form :model="form" @submit.prevent="handleRegister">
        <el-form-item><el-input v-model="form.username" placeholder="用户名" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" /></el-form-item>
        <el-form-item><el-input v-model="form.confirm" type="password" placeholder="确认密码" /></el-form-item>
        <el-form-item>
          <el-select v-model="form.role">
            <el-option value="AGENT" label="房地产商" />
            <el-option value="CUSTOMER" label="客户" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="handleRegister" :loading="loading">注册</el-button>
        <el-button @click="router.push('/login')">返回登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '', confirm: '', role: 'AGENT' })

const handleRegister = async () => {
  if (form.password !== form.confirm) { ElMessage.error('密码不一致'); return }
  loading.value = true
  try {
    await request.post('/public/register', { username: form.username, password: form.password }, { params: { role: form.role } })
    ElMessage.success('注册成功')
    router.push('/login')
  } finally { loading.value = false }
}
</script>
```

- [ ] **Step 6: 创建 AdminLayout.vue**

```vue
<template>
  <el-container style="height: 100vh">
    <el-aside width="220px">
      <el-menu :router="true" :default-active="$route.path" background-color="#304156"
               text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/dashboard">📊 仪表盘</el-menu-item>
        <el-menu-item index="/user" v-if="role === 'ADMIN'">👥 用户管理</el-menu-item>
        <el-menu-item index="/company" v-if="role === 'AGENT'">🏢 公司信息</el-menu-item>
        <el-menu-item index="/property" v-if="role === 'AGENT'">🏠 房源管理</el-menu-item>
        <el-menu-item index="/audit" v-if="role === 'ADMIN'">📋 审核管理</el-menu-item>
        <el-menu-item index="/order" v-if="role === 'AGENT'">📦 订单管理</el-menu-item>
        <el-menu-item index="/analysis" v-if="role === 'AGENT'">📈 关联分析</el-menu-item>
        <el-menu-item index="/report-handle" v-if="role === 'ADMIN'">⚠️ 举报处理</el-menu-item>
        <el-menu-item index="/message">💬 消息</el-menu-item>
        <el-menu-item index="/config" v-if="role === 'ADMIN'">⚙️ 系统配置</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header><span>欢迎，{{ role === 'ADMIN' ? '管理员' : '房地产商' }}</span></el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useAuthStore } from '../store/auth'
const { role } = useAuthStore()
</script>
```

- [ ] **Step 6: 验证前端运行**

Run: `cd fangdichan-admin-web && npm run dev`
Expected: 开发服务器启动成功

- [ ] **Step 7: Commit**

---

### Task 17: 管理后台各页面

**Files:**

- Create: `fangdichan-admin-web/src/views/dashboard/Dashboard.vue`
- Create: `fangdichan-admin-web/src/views/user/UserManagement.vue`
- Create: `fangdichan-admin-web/src/views/company/CompanyInfo.vue`
- Create: `fangdichan-admin-web/src/views/property/PropertyManagement.vue`
- Create: `fangdichan-admin-web/src/views/audit/AuditManagement.vue`
- Create: `fangdichan-admin-web/src/views/order/OrderManagement.vue`
- Create: `fangdichan-admin-web/src/views/analysis/AnalysisView.vue`
- Create: `fangdichan-admin-web/src/views/report/ReportManagement.vue`
- Create: `fangdichan-admin-web/src/views/message/MessageView.vue`
- Create: `fangdichan-admin-web/src/views/config/SystemConfig.vue`

- [ ] **Step 1: 仪表盘 Dashboard.vue**

简单展示：角色信息、快捷入口卡片（管理员看到审核数量概览、房地产商看到房源统计）。

- [ ] **Step 2: 用户管理 UserManagement.vue (管理员)**

```vue
<template>
  <div>
    <el-table :data="users" stripe>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="role" label="角色" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">{{ row.status ? '启用' : '禁用' }}</template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button @click="toggleStatus(row)" :type="row.status ? 'warning' : 'success'">
            {{ row.status ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
```

- [ ] **Step 3: 公司信息 CompanyInfo.vue (房地产商)**

表单：公司名称、地址、联系电话、简介。提交 PUT 请求到 `/api/agent/company`。

- [ ] **Step 4: 房源管理 PropertyManagement.vue (房地产商)**

```
表格：房源列表（标题、区域、价格、状态）
操作：发布新房源（弹窗表单）、编辑、下架
发布表单包含：标题、区域、地址、楼层区间、总楼层、户型、面积、价格、描述、图片上传
```

- [ ] **Step 5: 审核管理 AuditManagement.vue (管理员)**

```
表格：待审核房源列表（含房地产商名称）
操作：查看详情 → 通过 / 拒绝
```

- [ ] **Step 6: 订单管理 OrderManagement.vue (房地产商)**

```
表格：客户购房申请列表
操作：查看详情 → 确认成交 / 取消
```

- [ ] **Step 7: 关联分析 AnalysisView.vue (房地产商)**

使用 ECharts 展示三个维度的柱状图：

```vue
<template>
  <div>
    <h3>按区域空置率</h3>
    <div ref="districtChart" style="height: 300px"></div>
    <h3>按楼层空置率</h3>
    <div ref="floorChart" style="height: 300px"></div>
    <h3>按户型空置率</h3>
    <div ref="roomTypeChart" style="height: 300px"></div>
  </div>
</template>
```

使用 `echarts.init()` 渲染柱状图，从 `/api/agent/analysis/vacancy` 获取数据。

- [ ] **Step 8: 举报处理 / 消息 / 系统配置页面**

ReportManagement.vue：待处理举报列表，操作：驳回/处理
MessageView.vue：会话列表 + 聊天窗口
SystemConfig.vue：配置键值对列表，可编辑

- [ ] **Step 9: 页面联调**

Run: `cd fangdichan-admin-web && npm run dev`，同时启动后端
Expected: 各页面能正常加载数据

- [ ] **Step 10: Commit**

---

## 阶段五：客户桌面端

### Task 18: 桌面端布局 + 路由 + 状态管理

**Files:**

- Create: `fangdichan-client/src/layout/ClientLayout.vue`
- Create: `fangdichan-client/src/router/index.js`
- Create: `fangdichan-client/src/store/auth.js`
- Create: `fangdichan-client/src/api/request.js`
- Create: `fangdichan-client/src/api/auth.js`

- [ ] **Step 1: 创建 axios 封装 + auth store（同管理后台逻辑）**
- [ ] **Step 2: 创建路由**

```javascript
const routes = [
    { path: '/login', component: () => import('../views/login/Login.vue') },
    { path: '/register', component: () => import('../views/login/Register.vue') },
    {
        path: '/',
        component: ClientLayout,
        children: [
            { path: '', redirect: '/home' },
            { path: 'home', component: () => import('../views/home/Home.vue') },
            { path: 'search', component: () => import('../views/search/Search.vue') },
            { path: 'detail/:id', component: () => import('../views/detail/PropertyDetail.vue') },
            { path: 'favorite', component: () => import('../views/favorite/Favorite.vue') },
            { path: 'order', component: () => import('../views/order/OrderList.vue') },
            { path: 'message', component: () => import('../views/message/MessageView.vue') },
            { path: 'company', component: () => import('../views/company/CompanyList.vue') },
            { path: 'company/:id', component: () => import('../views/company/CompanyDetail.vue') },
            { path: 'profile', component: () => import('../views/profile/Profile.vue') },
            { path: 'suggestion', component: () => import('../views/suggestion/Suggestion.vue') },
            { path: 'report/:propertyId', component: () => import('../views/report/ReportForm.vue') },
        ]
    }
]
```

- [ ] **Step 3: 创建 ClientLayout.vue（桌面风格）**

```vue
<template>
  <div class="app-window">
    <!-- 自定义标题栏 -->
    <div class="title-bar">
      <span class="title">🏡 购房通</span>
      <div class="window-controls">
        <button @click="minimize">—</button>
        <button @click="toggleMaximize">□</button>
        <button @click="close">✕</button>
      </div>
    </div>
    <div class="app-body">
      <!-- 左侧导航 -->
      <div class="activity-bar">
        <div v-for="item in navItems" :key="item.path"
             :class="['nav-icon', { active: $route.path.startsWith(item.path) }]"
             @click="router.push(item.path)"
             :title="item.label">
          {{ item.icon }}
        </div>
      </div>
      <!-- 主区域 -->
      <div class="main-area">
        <router-view />
      </div>
    </div>
    <!-- 状态栏 -->
    <div class="status-bar">
      <span>已登录 | {{ username }}</span>
    </div>
  </div>
</template>

<script setup>
const navItems = [
  { path: '/home', icon: '🏠', label: '首页' },
  { path: '/search', icon: '🔍', label: '搜索' },
  { path: '/favorite', icon: '❤️', label: '收藏' },
  { path: '/message', icon: '💬', label: '消息' },
  { path: '/order', icon: '📄', label: '订单' },
  { path: '/company', icon: '🏢', label: '公司' },
  { path: '/profile', icon: '👤', label: '个人' },
]
</script>

<style scoped>
.app-window { display: flex; flex-direction: column; height: 100vh; }
.title-bar { background: #2c2c2c; color: #ccc; height: 32px; display: flex; align-items: center; justify-content: space-between; padding: 0 12px; -webkit-app-region: drag; font-size: 13px; }
.window-controls button { -webkit-app-region: no-drag; background: none; border: none; color: #ccc; padding: 4px 12px; cursor: pointer; }
.app-body { display: flex; flex: 1; }
.activity-bar { width: 50px; background: #3c3c3c; display: flex; flex-direction: column; align-items: center; padding: 8px 0; gap: 8px; }
.nav-icon { padding: 8px; border-radius: 4px; cursor: pointer; font-size: 18px; }
.nav-icon.active { background: #505050; }
.main-area { flex: 1; background: #f0f0f0; overflow: auto; }
.status-bar { background: #007acc; color: white; padding: 2px 12px; font-size: 11px; display: flex; justify-content: space-between; }
</style>
```

- [ ] **Step 4: 将 Element Plus 覆写为桌面风格**

创建 `src/assets/desktop-theme.css`，覆写 Element Plus 默认样式使其更紧凑、更桌面原生（减少圆角、调整间距、使用桌面风格颜色）。

```css
/* src/assets/desktop-theme.css */
.el-button { border-radius: 2px; }
.el-card { border-radius: 4px; box-shadow: none; border: 1px solid #e0e0e0; }
.el-table th { background: #f5f5f5; }
```

- [ ] **Step 5: 验证运行**

Run: `cd fangdichan-client && npm run dev`
Expected: 页面显示桌面风格布局

- [ ] **Step 6: Commit**

---

### Task 19: 桌面端各页面

**Files:**

- Create: `fangdichan-client/src/views/login/Login.vue`
- Create: `fangdichan-client/src/views/login/Register.vue`
- Create: `fangdichan-client/src/views/home/Home.vue`
- Create: `fangdichan-client/src/views/search/Search.vue`
- Create: `fangdichan-client/src/views/detail/PropertyDetail.vue`
- Create: `fangdichan-client/src/views/favorite/Favorite.vue`
- Create: `fangdichan-client/src/views/order/OrderList.vue`
- Create: `fangdichan-client/src/views/message/MessageView.vue`
- Create: `fangdichan-client/src/views/profile/Profile.vue`
- Create: `fangdichan-client/src/views/company/CompanyList.vue`
- Create: `fangdichan-client/src/views/company/CompanyDetail.vue`
- Create: `fangdichan-client/src/views/suggestion/Suggestion.vue`
- Create: `fangdichan-client/src/views/report/ReportForm.vue`

- [ ] **Step 1: 登录/注册页面**

Login.vue：用户名 + 密码 → 登录 → 跳转首页
Register.vue：用户名 + 密码 + 确认密码 → 注册 → 跳转登录

- [ ] **Step 2: 首页 Home.vue**

```
工具栏：搜索框 + "帮我找房"按钮 + 快捷分类标签（两室/三室/朝阳区/海淀区）
内容区：推荐房源卡片网格（3列）
获取 /api/customer/property/recommended 数据
每张卡片：图片占位色块 + 价格 + 标题 + 面积/楼层/标签
```

"帮我找房"按钮打开步骤向导弹窗：

```
el-dialog: 步骤1 选择区域 → 步骤2 选择预算 → 步骤3 选择户型 → 展示结果
```

- [ ] **Step 3: 搜索页面 Search.vue**

```
左侧面板：筛选条件
  区域 (el-select)
  价格范围 (el-input-number min/max)
  户型 (el-select)
  面积范围 (el-input-number min/max)
  搜索按钮

右侧：搜索结果列表（分页）
接口: GET /api/customer/property/search?district=&roomType=&priceMin=&priceMax=&page=&size=
```

- [ ] **Step 4: 房源详情页 PropertyDetail.vue**

```
图片轮播（el-carousel）
价格、标题、区域、楼层、户型、面积、单价、描述
房地产商信息（公司名、电话）
操作按钮：收藏 ❤️、立即购买、联系房地产商（进入消息）、举报
```

- [ ] **Step 5: 收藏 / 订单 / 消息页面**

收藏页：卡片列表 + 取消收藏
订单页：表格列表 + 状态标签
消息页：左侧会话列表 + 右侧聊天窗口（WebSocket 连接）

- [ ] **Step 6: 个人中心 / 建议 / 举报 / 公司页面**

Profile.vue：修改个人信息、购房意向管理、修改密码
Suggestion.vue：填写建议表单（目标公司 + 期望房型 + 价格 + 内容）
ReportForm.vue：选择房源 + 填写举报原因
CompanyList.vue：公司列表 → 点击进入 CompanyDetail（公司信息 + 该公司房源列表）

- [ ] **Step 7: WebSocket 客户端连接**

在消息页面连接 WebSocket：

```javascript
const ws = new WebSocket(`ws://localhost:8080/ws?token=${token}`)
ws.onmessage = (event) => {
    const msg = JSON.parse(event.data)
    messages.value.push(msg)
}
```

- [ ] **Step 8: 页面联调**

Run: 同时启动后端和客户端，测试核心流程：注册→登录→浏览房源→搜索→查看详情→收藏→购房申请→消息沟通

- [ ] **Step 9: Commit**

---

## 阶段六：集成与部署

### Task 20: 完善全局异常处理

**Files:**

- Modify: `fangdichan-server/src/main/java/com/fdsc/common/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: 添加参数校验和权限不足的异常处理**

在 GlobalExceptionHandler 中增加：
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public Result<?> handleValidation(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getFieldErrors().stream()
        .map(f -> f.getField() + ": " + f.getDefaultMessage())
        .collect(Collectors.joining(", "));
    return Result.error(400, msg);
}

@ExceptionHandler(AccessDeniedException.class)
public Result<?> handleAccessDenied(AccessDeniedException e) {
    return Result.error(403, "权限不足");
}
```

- [ ] **Step 2: 编译 + 测试验证**

Run: `cd fangdichan-server && mvn clean compile && mvn test -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

---

### Task 21: 构建与部署配置

- [ ] **Step 1: 构建后端 jar**

Run: `cd fangdichan-server && mvn clean package -DskipTests`
Expected: `target/fangdichan-server-1.0.0.jar`

- [ ] **Step 2: 构建管理后台**

Run: `cd fangdichan-admin-web && npm run build`
Expected: `dist/` 目录

- [ ] **Step 3: 验证 Electron 客户端构建**

Run: `cd fangdichan-client && npm run build`
Expected: `dist/` 目录

- [ ] **Step 4: 最终 Commit**

```bash
git add -A
git commit -m "feat: complete real estate query system implementation"
```

---

## 自审检查

### Spec 覆盖

- ✅ 所有 11 张表已覆盖（Task 4 + Task 7-15）
- ✅ 三个角色登录/注册（Task 6）
- ✅ 管理员功能：用户管理、审核、举报、系统配置（Task 9-11, 15, 17）
- ✅ 房地产商功能：公司管理、房源CRUD、订单、分析、沟通（Task 8-13, 17）
- ✅ 客户功能：搜索、详情、收藏、订单、沟通、建议、举报（Task 9-12, 19）
- ✅ 引导查询向导（Task 19 Step 2）
- ✅ 关联分析图表（Task 13, 17 Step 7）
- ✅ 报表导出（Task 14）
- ✅ WebSocket 实时通信（Task 12, 19 Step 7）
- ✅ MinIO 图片存储（Task 9 Step 3）
- ✅ 分页（各列表接口 + MyBatis-Plus 分页插件）
- ✅ 修改密码（Task 7）

### 待确认项

- [ ] 支付功能不在当前范围内
- [ ] 邮箱/短信验证不在当前范围内
