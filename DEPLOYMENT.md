# 🚢 Deployment Guide - SnipLink

This guide covers deployment options for the SnipLink URL shortener application.

---

## Table of Contents
1. [Local Development](#local-development)
2. [Docker Deployment](#docker-deployment)
3. [Cloud Deployment (AWS)](#cloud-deployment-aws)
4. [Cloud Deployment (Heroku)](#cloud-deployment-heroku)
5. [Production Checklist](#production-checklist)
6. [Monitoring & Maintenance](#monitoring--maintenance)

---

## Local Development

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+

### Steps
```bash
# 1. Clone repository
git clone https://github.com/yourusername/sniplink.git
cd sniplink

# 2. Create PostgreSQL database
createdb sniplink_db

# 3. Configure application.properties
# Edit src/main/resources/application.properties

# 4. Build project
mvn clean package

# 5. Run application
mvn spring-boot:run

# Access at http://localhost:8080
```

---

## Docker Deployment

### Dockerfile

Create `Dockerfile` in project root:

```dockerfile
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && apt-get install -y maven

# Build application
RUN mvn clean package -DskipTests

# Expose port
EXPOSE 8080

# Run application
CMD ["java", "-jar", "target/sniplink-1.0.0.jar"]
```

### Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  # PostgreSQL Database
  postgres:
    image: postgres:14
    container_name: sniplink-db
    environment:
      POSTGRES_DB: sniplink_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - sniplink-network

  # Spring Boot Application
  app:
    build: .
    container_name: sniplink-app
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/sniplink_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      JWT_SECRET: your_production_jwt_secret_here
      JWT_EXPIRATION: 86400000
    ports:
      - "8080:8080"
    networks:
      - sniplink-network

volumes:
  postgres_data:

networks:
  sniplink-network:
    driver: bridge
```

### Deploy with Docker Compose

```bash
# Build and start containers
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop containers
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

---

## Cloud Deployment (AWS)

### AWS Elastic Beanstalk

#### 1. Install AWS CLI and EB CLI
```bash
# Install AWS CLI
pip install awscli

# Configure AWS credentials
aws configure

# Install EB CLI
pip install awsebcli
```

#### 2. Initialize Elastic Beanstalk
```bash
# Initialize EB application
eb init -p "Corretto 17" sniplink-app --region us-east-1

# Create environment
eb create sniplink-prod
```

#### 3. Configure Environment Variables
```bash
eb setenv \
  SPRING_DATASOURCE_URL=jdbc:postgresql://your-rds-endpoint:5432/sniplink_db \
  SPRING_DATASOURCE_USERNAME=your_db_user \
  SPRING_DATASOURCE_PASSWORD=your_db_password \
  JWT_SECRET=your_production_jwt_secret \
  JWT_EXPIRATION=86400000
```

#### 4. Deploy
```bash
# Build application
mvn clean package

# Deploy to EB
eb deploy

# Open application
eb open
```

### AWS RDS (PostgreSQL)

1. **Create RDS Instance**:
   - Go to AWS RDS Console
   - Create PostgreSQL database
   - Note endpoint, username, password

2. **Security Group**:
   - Allow inbound traffic on port 5432 from EB security group

3. **Update application.properties**:
   ```properties
   spring.datasource.url=jdbc:postgresql://your-rds-endpoint:5432/sniplink_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

---

## Cloud Deployment (Heroku)

### Prerequisites
- Heroku account
- Heroku CLI installed

### Steps

#### 1. Create Heroku App
```bash
# Login to Heroku
heroku login

# Create app
heroku create sniplink-app

# Add PostgreSQL addon
heroku addons:create heroku-postgresql:mini
```

#### 2. Configure Environment Variables
```bash
heroku config:set JWT_SECRET=your_production_jwt_secret
heroku config:set JWT_EXPIRATION=86400000
```

#### 3. Create Procfile
Create `Procfile` in project root:
```
web: java -jar target/sniplink-1.0.0.jar --server.port=$PORT
```

#### 4. Update application.properties for Heroku
```properties
spring.datasource.url=${JDBC_DATABASE_URL}
spring.datasource.username=${JDBC_DATABASE_USERNAME}
spring.datasource.password=${JDBC_DATABASE_PASSWORD}
server.port=${PORT:8080}
```

#### 5. Deploy
```bash
# Add files to git
git add .
git commit -m "Deploy to Heroku"

# Push to Heroku
git push heroku main

# Open application
heroku open

# View logs
heroku logs --tail
```

---

## Production Checklist

### Security
- [ ] Change default JWT secret to strong, unique value
- [ ] Enable HTTPS/TLS
- [ ] Configure CORS with specific origins
- [ ] Enable rate limiting
- [ ] Set strong database passwords
- [ ] Use environment variables for sensitive data
- [ ] Enable Spring Security CSRF for forms
- [ ] Implement IP whitelisting for admin endpoints
- [ ] Regular security updates

### Configuration
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` in production
- [ ] Configure production database (RDS, Cloud SQL, etc.)
- [ ] Set appropriate logging levels
- [ ] Configure connection pooling (HikariCP settings)
- [ ] Set up database backups
- [ ] Configure CDN for static assets
- [ ] Enable compression (Gzip)

### Performance
- [ ] Enable database indexes
- [ ] Configure caching (Redis, Memcached)
- [ ] Set up load balancing
- [ ] Optimize database queries
- [ ] Enable HTTP/2
- [ ] Implement CDN for static resources

### Monitoring
- [ ] Set up application monitoring (New Relic, DataDog)
- [ ] Configure log aggregation (ELK Stack, Splunk)
- [ ] Set up error tracking (Sentry, Rollbar)
- [ ] Create health check endpoint
- [ ] Set up alerts for critical errors
- [ ] Monitor database performance

### Backup & Recovery
- [ ] Automated daily database backups
- [ ] Test restoration procedures
- [ ] Document recovery procedures
- [ ] Set up disaster recovery plan

---

## Production Configuration

### application-prod.properties

Create `src/main/resources/application-prod.properties`:

```properties
# Server Configuration
server.port=${PORT:8080}
server.compression.enabled=true
server.http2.enabled=true

# Database Configuration
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

# Logging
logging.level.root=WARN
logging.level.com.sniplink=INFO
logging.file.name=/var/log/sniplink/application.log

# CORS
cors.allowed-origins=${CORS_ORIGINS:https://sniplink.com}

# Rate Limiting
rate.limit.requests=100
rate.limit.duration=60000
```

### Run with Production Profile
```bash
java -jar sniplink-1.0.0.jar --spring.profiles.active=prod
```

---

## Monitoring & Maintenance

### Health Check Endpoint

Add to your controller:
```java
@RestController
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
```

### Application Metrics

Add Spring Boot Actuator:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Configure in `application.properties`:
```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

### Database Maintenance

```sql
-- Regular vacuum (PostgreSQL)
VACUUM ANALYZE;

-- Check table sizes
SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Archive old click logs (older than 6 months)
DELETE FROM click_logs WHERE timestamp < NOW() - INTERVAL '6 months';
```

### Log Rotation

Create `/etc/logrotate.d/sniplink`:
```
/var/log/sniplink/*.log {
    daily
    rotate 30
    compress
    delaycompress
    notifempty
    create 0640 appuser appgroup
    sharedscripts
    postrotate
        systemctl reload sniplink
    endscript
}
```

---

## SSL/TLS Configuration

### Using Let's Encrypt

```bash
# Install Certbot
sudo apt-get install certbot

# Obtain certificate
sudo certbot certonly --standalone -d sniplink.com

# Configure Spring Boot for HTTPS
```

Add to `application-prod.properties`:
```properties
server.ssl.enabled=true
server.ssl.key-store=/etc/letsencrypt/live/sniplink.com/keystore.p12
server.ssl.key-store-password=your_keystore_password
server.ssl.key-store-type=PKCS12
```

---

## Scaling Strategy

### Horizontal Scaling
- Deploy multiple app instances behind load balancer
- Use external session storage (Redis)
- Implement database read replicas

### Vertical Scaling
- Increase JVM heap size: `-Xmx2g -Xms2g`
- Optimize database resources
- Use faster storage (SSD)

---

## Troubleshooting

### Common Issues

**Connection Refused**
```bash
# Check if application is running
ps aux | grep java

# Check logs
tail -f /var/log/sniplink/application.log

# Check port availability
netstat -tuln | grep 8080
```

**Database Connection Issues**
```bash
# Test database connectivity
psql -h localhost -U postgres -d sniplink_db

# Check connection pool
# Look for connection timeout errors in logs
```

**High Memory Usage**
```bash
# Check JVM memory
jstat -gc <pid>

# Generate heap dump
jmap -dump:format=b,file=heapdump.bin <pid>
```

---

## Rollback Procedure

```bash
# For Heroku
heroku releases
heroku rollback v<previous_version>

# For Elastic Beanstalk
eb use sniplink-prod
eb deploy --version <previous_version>

# For Docker
docker-compose down
docker-compose up -d --force-recreate
```

---

**Last Updated**: March 2026  
**Maintained By**: SnipLink DevOps Team
