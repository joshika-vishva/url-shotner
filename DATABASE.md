# 📊 Database Design - SnipLink

## Entity-Relationship Diagram

```
┌─────────────────────┐
│       USER          │
├─────────────────────┤
│ PK  id              │
│     username        │
│     email           │
│     password        │
│     role            │
│     created_at      │
└─────────────────────┘
          │
          │ 1
          │
          │
          │ N
          ▼
┌─────────────────────┐        N         ┌─────────────────────┐
│       URL           │◄──────────────────│    CLICK_LOG        │
├─────────────────────┤                   ├─────────────────────┤
│ PK  id              │                   │ PK  id              │
│ FK  user_id         │ 1                 │ FK  url_id          │
│     original_url    │                   │     timestamp       │
│ UQ  short_code      │                   │     ip_address      │
│     created_at      │                   │     device          │
│     expires_at      │                   │     browser         │
│     password        │                   │     location        │
│     click_count     │                   └─────────────────────┘
│     tags            │
│     is_active       │
│     is_safe         │
└─────────────────────┘

Legend:
PK = Primary Key
FK = Foreign Key
UQ = Unique Constraint
```

## Table Schemas

### 1. users

Stores user authentication and profile information.

| Column     | Type         | Constraints                    | Description                |
|------------|--------------|--------------------------------|----------------------------|
| id         | BIGSERIAL    | PRIMARY KEY                    | Auto-increment user ID     |
| username   | VARCHAR(50)  | NOT NULL, UNIQUE               | Unique username            |
| email      | VARCHAR(100) | NOT NULL, UNIQUE               | User email address         |
| password   | VARCHAR(255) | NOT NULL                       | BCrypt hashed password     |
| role       | VARCHAR(20)  | NOT NULL, DEFAULT 'USER'       | User role (USER/ADMIN)     |
| created_at | TIMESTAMP    | NOT NULL, DEFAULT CURRENT_TIME | Account creation timestamp |

**Indexes:**
- PRIMARY KEY on `id`
- UNIQUE INDEX on `username`
- UNIQUE INDEX on `email`

**Sample Data:**
```sql
INSERT INTO users (username, email, password, role) VALUES
('john_doe', 'john@example.com', '$2a$10$...', 'USER'),
('admin', 'admin@sniplink.com', '$2a$10$...', 'ADMIN');
```

---

### 2. urls

Stores shortened URLs and their metadata.

| Column       | Type          | Constraints                    | Description                      |
|--------------|---------------|--------------------------------|----------------------------------|
| id           | BIGSERIAL     | PRIMARY KEY                    | Auto-increment URL ID            |
| user_id      | BIGINT        | NOT NULL, FK → users(id)       | Owner of the URL                 |
| original_url | VARCHAR(2048) | NOT NULL                       | Original long URL                |
| short_code   | VARCHAR(10)   | NOT NULL, UNIQUE               | Generated short code             |
| created_at   | TIMESTAMP     | NOT NULL, DEFAULT CURRENT_TIME | URL creation timestamp           |
| expires_at   | TIMESTAMP     | NULL                           | Optional expiration date         |
| password     | VARCHAR(100)  | NULL                           | Optional password protection     |
| click_count  | BIGINT        | NOT NULL, DEFAULT 0            | Total clicks counter             |
| tags         | VARCHAR(500)  | NULL                           | Comma-separated tags             |
| is_active    | BOOLEAN       | NOT NULL, DEFAULT TRUE         | Enable/disable status            |
| is_safe      | BOOLEAN       | NOT NULL, DEFAULT TRUE         | AI safety check result           |

**Indexes:**
- PRIMARY KEY on `id`
- UNIQUE INDEX on `short_code`
- INDEX on `user_id`
- INDEX on `created_at`

**Foreign Keys:**
- `user_id` REFERENCES `users(id)` ON DELETE CASCADE

**Sample Data:**
```sql
INSERT INTO urls (user_id, original_url, short_code, tags, is_safe) VALUES
(1, 'https://example.com/very/long/url', 'abc123', 'marketing, social', TRUE),
(1, 'https://github.com/project', 'gh-proj', 'tech', TRUE);
```

---

### 3. click_logs

Stores click analytics data for each URL access.

| Column     | Type         | Constraints                   | Description                |
|------------|--------------|-------------------------------|----------------------------|
| id         | BIGSERIAL    | PRIMARY KEY                   | Auto-increment log ID      |
| url_id     | BIGINT       | NOT NULL, FK → urls(id)       | Associated URL             |
| timestamp  | TIMESTAMP    | NOT NULL, DEFAULT CURRENT_TIME| Click timestamp            |
| ip_address | VARCHAR(45)  | NULL                          | Client IP address          |
| device     | VARCHAR(50)  | NULL                          | Device type                |
| browser    | VARCHAR(50)  | NULL                          | Browser name               |
| location   | VARCHAR(100) | NULL                          | Geographic location        |

**Indexes:**
- PRIMARY KEY on `id`
- INDEX on `url_id`
- INDEX on `timestamp`

**Foreign Keys:**
- `url_id` REFERENCES `urls(id)` ON DELETE CASCADE

**Sample Data:**
```sql
INSERT INTO click_logs (url_id, ip_address, device, browser, location) VALUES
(1, '192.168.1.1', 'Mobile', 'Chrome', 'New York, US'),
(1, '192.168.1.2', 'Desktop', 'Firefox', 'London, UK');
```

---

## Relationships

### 1. User → URL (One-to-Many)
- **Relationship**: One user can create many URLs
- **Foreign Key**: `urls.user_id` → `users.id`
- **Cascade**: DELETE CASCADE (when user is deleted, all their URLs are deleted)

### 2. URL → ClickLog (One-to-Many)
- **Relationship**: One URL can have many click logs
- **Foreign Key**: `click_logs.url_id` → `urls.id`
- **Cascade**: DELETE CASCADE (when URL is deleted, all its click logs are deleted)

---

## Database Creation Script

```sql
-- Create Database
CREATE DATABASE sniplink_db;

\c sniplink_db;

-- Create Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create URLs Table
CREATE TABLE urls (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    original_url VARCHAR(2048) NOT NULL,
    short_code VARCHAR(10) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    password VARCHAR(100),
    click_count BIGINT NOT NULL DEFAULT 0,
    tags VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_safe BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create ClickLogs Table
CREATE TABLE click_logs (
    id BIGSERIAL PRIMARY KEY,
    url_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    device VARCHAR(50),
    browser VARCHAR(50),
    location VARCHAR(100),
    FOREIGN KEY (url_id) REFERENCES urls(id) ON DELETE CASCADE
);

-- Create Indexes
CREATE INDEX idx_url_user_id ON urls(user_id);
CREATE INDEX idx_url_created_at ON urls(created_at);
CREATE INDEX idx_clicklog_url_id ON click_logs(url_id);
CREATE INDEX idx_clicklog_timestamp ON click_logs(timestamp);
```

---

## Query Examples

### 1. Get User's Total Clicks
```sql
SELECT SUM(u.click_count) as total_clicks
FROM urls u
WHERE u.user_id = 1;
```

### 2. Get Top 5 Most Clicked URLs
```sql
SELECT short_code, original_url, click_count
FROM urls
WHERE user_id = 1
ORDER BY click_count DESC
LIMIT 5;
```

### 3. Get Daily Click Statistics
```sql
SELECT DATE(timestamp) as date, COUNT(*) as clicks
FROM click_logs cl
JOIN urls u ON cl.url_id = u.id
WHERE u.user_id = 1
  AND timestamp >= NOW() - INTERVAL '7 days'
GROUP BY DATE(timestamp)
ORDER BY date DESC;
```

### 4. Get Device Distribution
```sql
SELECT device, COUNT(*) as count
FROM click_logs cl
JOIN urls u ON cl.url_id = u.id
WHERE u.user_id = 1
GROUP BY device;
```

### 5. Get Active URLs Count
```sql
SELECT COUNT(*) as active_urls
FROM urls
WHERE user_id = 1 AND is_active = TRUE;
```

---

## Database Optimization

### Indexes
- **Short Code Lookup**: UNIQUE index on `urls.short_code` for fast redirection
- **User Queries**: Index on `urls.user_id` for efficient user-specific queries
- **Analytics**: Index on `click_logs.timestamp` for time-based analytics

### Performance Tips
1. Use connection pooling (HikariCP configured in Spring Boot)
2. Enable query caching for frequently accessed data
3. Use batch inserts for bulk operations
4. Implement pagination for large result sets
5. Regular VACUUM and ANALYZE operations

---

## Backup Strategy

### Automated Backups
```bash
# Daily backup script
pg_dump sniplink_db > backup_$(date +%Y%m%d).sql

# Compressed backup
pg_dump sniplink_db | gzip > backup_$(date +%Y%m%d).sql.gz
```

### Restore
```bash
psql sniplink_db < backup_20240101.sql
```

---

## Migration Notes

When deploying updates:
1. Hibernate `ddl-auto=update` handles schema changes automatically
2. For production, use `ddl-auto=validate` and manual migrations
3. Consider using Flyway or Liquibase for version-controlled migrations

---

**Database Version**: PostgreSQL 12+  
**Last Updated**: March 2026
