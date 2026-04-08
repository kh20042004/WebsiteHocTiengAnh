# AWS EC2 Deployment Guide - English 12 Smart

> **Cập nhật**: April 8, 2026  
> **Mục tiêu**: Deploy Spring Boot 3.2.0 + Python Edge-TTS Service trên AWS EC2 t3.micro với domain & SSL

---

## 📋 Mục Lục

1. [Bước 1: Tạo EC2 Instance](#bước-1-tạo-ec2-instance)
2. [Bước 2: Cấu Hình Security Group](#bước-2-cấu-hình-security-group)
3. [Bước 3: Cài Đặt Dependencies](#bước-3-cài-đặt-dependencies)
4. [Bước 4: Clone & Build Project](#bước-4-clone--build-project)
5. [Bước 5: Cấu Hình Environment](#bước-5-cấu-hình-environment)
6. [Bước 6: Setup Database](#bước-6-setup-database)
7. [Bước 7: Chạy Services](#bước-7-chạy-services)
8. [Bước 8: Setup Nginx + SSL](#bước-8-setup-nginx--ssl)
9. [Troubleshooting](#troubleshooting)

---

## Bước 1: Tạo EC2 Instance

### 1.1 Launch Instance trên AWS Console

1. Đi tới **EC2 Dashboard** → **Launch Instance**
2. Chọn **AMI**: `Amazon Linux 2` (free tier eligible, optimized)
3. Chọn **Instance Type**: `t3.micro` (1 GB RAM, 1 vCPU)
4. **Key Pair**: Select key pair đã có (hoặc tạo mới)
   - ⚠️ **Lưu file `.pem` ở nơi an toàn**

### 1.2 Storage Configuration

- **Root volume**: 30 GB (gp3) - đủ cho application
- **Encryption**: Enable (optional nhưng recommended)

### 1.3 Tags (Optional)
```
Name: english-12-smart-prod
Environment: Production
Project: English Learning Platform
```

### 1.4 Ghi nhớ thông tin

Sau khi instance khởi động, ghi nhớ:
- **Public IPv4**: `123.45.67.89` (ví dụ)
- **Private IPv4**: `172.31.x.x`
- **Public DNS**: `ec2-123-45-67-89.compute-1.amazonaws.com`

---

## Bước 2: Cấu Hình Security Group

### 2.1 Edit Security Group Rules

| Port | Protocol | Source | Mục Đích |
|------|----------|--------|---------|
| 22 | TCP | Your IP | SSH Access |
| 80 | TCP | 0.0.0.0/0 | HTTP (Nginx) |
| 443 | TCP | 0.0.0.0/0 | HTTPS (SSL) |
| 8080 | TCP | 127.0.0.1 | Spring Boot (Internal) |
| 5000 | TCP | 127.0.0.1 | Python TTS (Internal) |
| 3306 | TCP | 127.0.0.1 | MySQL (Internal) |
| 6379 | TCP | 127.0.0.1 | Redis (Internal) |

### 2.2 SSH Access

Cấp quyền file `.pem`:
```bash
chmod 400 your-key.pem
```

Kết nối SSH:
```bash
ssh -i your-key.pem ec2-user@123.45.67.89
```

---

## Bước 3: Cài Đặt Dependencies

Sau khi SSH vào instance:

### 3.1 Update System
```bash
sudo yum update -y
sudo yum install -y htop git wget curl
```

### 3.2 Cài Java 17
```bash
# Tải Java 17
cd /tmp
wget https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz

# Extract & setup
sudo mkdir -p /usr/local/java
sudo tar -zxf openjdk-17.0.2_linux-x64_bin.tar.gz -C /usr/local/java

# Set PATH
sudo tee -a /etc/profile.d/java.sh << 'EOF'
export JAVA_HOME=/usr/local/java/jdk-17.0.2
export PATH=$JAVA_HOME/bin:$PATH
EOF

# Reload & verify
source /etc/profile.d/java.sh
java -version  # Should show: openjdk version "17.0.2"
```

### 3.3 Cài Maven 3
```bash
cd /tmp
wget https://archive.apache.org/dist/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz
sudo tar -zxf apache-maven-3.8.6-bin.tar.gz -C /opt

# Set PATH
sudo tee -a /etc/profile.d/maven.sh << 'EOF'
export MAVEN_HOME=/opt/apache-maven-3.8.6
export PATH=$MAVEN_HOME/bin:$PATH
EOF

source /etc/profile.d/maven.sh
mvn -version  # Verify
```

### 3.4 Cài Python 3
```bash
sudo yum install -y python3 python3-pip
python3 --version  # Should show Python 3.x
pip3 install --upgrade pip
```

### 3.5 Cài MySQL (or MariaDB)
```bash
# MariaDB (recommended, lighter)
sudo yum install -y mariadb-server

# Start service
sudo systemctl start mariadb
sudo systemctl enable mariadb  # Auto-start on reboot

# Secure installation
sudo mysql_secure_installation
```

### 3.6 Cài Redis
```bash
# Install from Amazon Linux repo
sudo yum install -y redis

# Start service
sudo systemctl start redis
sudo systemctl enable redis

# Verify
redis-cli ping  # Output: PONG
```

### 3.7 Cài Nginx (for reverse proxy + SSL)
```bash
sudo yum install -y nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 3.8 Cài Certbot (Let's Encrypt für SSL)
```bash
sudo yum install -y certbot python3-certbot-nginx
```

---

## Bước 4: Clone & Build Project

### 4.1 Clone Repository
```bash
# Create directory
mkdir -p /var/www
cd /var/www

# Clone project (hoặc upload nếu private repo)
git clone https://github.com/yourusername/english-12-smart.git
cd english-12-smart

# Verify structure
ls -la
# Output: pom.xml, src/, python-services/, README.md, etc.
```

### 4.2 Build Java Project
```bash
mvn clean package -DskipTests

# Output should end with:
# BUILD SUCCESS
# Time: XX.XXs

# JAR file location:
# ls -lh target/english-12-smart-1.0.0.jar
```

### 4.3 Setup Python Service
```bash
cd python-services/edge-tts-service

# Create virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Verify
python3 -c "import edge_tts; print('OK')"

# Deactivate for now
deactivate
```

---

## Bước 5: Cấu Hình Environment

### 5.1 Create Application Properties

```bash
cd /var/www/english-12-smart/src/main/resources
nano application.properties

# Paste:
```

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/
spring.application.name=english-12-smart

# MongoDB Connection
spring.data.mongodb.uri=mongodb+srv://YOUR_MONGO_USER:YOUR_MONGO_PASSWORD@cluster0.mongodb.net/english_12_smart?retryWrites=true&w=majority

# Redis Cache
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=

# JWT Secret (CHANGE THIS!)
jwt.secret=your-long-random-secret-key-at-least-64-characters-long-change-this-please-12345
jwt.expiration=86400000

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
spring.security.oauth2.client.provider.google.token-uri=https://www.googleapis.com/oauth2/v4/token
spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v1/userinfo

# Cloudinary Configuration
cloudinary.cloud-name=YOUR_CLOUDINARY_NAME
cloudinary.api-key=YOUR_CLOUDINARY_KEY
cloudinary.api-secret=YOUR_CLOUDINARY_SECRET

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Python TTS Service URL
edge-tts.service.url=http://localhost:5000

# Logging
logging.level.root=INFO
logging.level.com.english12smart=DEBUG
```

### 5.2 Create .env file (for Python service)
```bash
cd /var/www/english-12-smart/python-services/edge-tts-service
nano .env

# Paste:
```

```env
FLASK_APP=app.py
FLASK_ENV=production
PYTHONUNBUFFERED=1
PORT=5000
HOST=127.0.0.1
```

---

## Bước 6: Setup Database

### 6.1 Create MySQL Database
```bash
# Login to MySQL
mysql -u root -p

# Create database & user
CREATE DATABASE english_12_smart CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'smartapp'@'localhost' IDENTIFIED BY 'StrongPassword123!';
GRANT ALL PRIVILEGES ON english_12_smart.* TO 'smartapp'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 6.2 Verify MongoDB Connection
```bash
# Test connection (if using MongoDB Atlas)
mongosh "mongodb+srv://YOUR_USER:YOUR_PASSWORD@cluster0.mongodb.net/english_12_smart"
```

---

## Bước 7: Chạy Services

### 7.1 Create Systemd Service - Spring Boot

Create file: `/etc/systemd/system/english-12-smart.service`

```bash
sudo nano /etc/systemd/system/english-12-smart.service

# Paste:
```

```ini
[Unit]
Description=English 12 Smart - Spring Boot Application
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/var/www/english-12-smart
Environment="JAVA_HOME=/usr/local/java/jdk-17.0.2"
ExecStart=/usr/local/java/jdk-17.0.2/bin/java -jar target/english-12-smart-1.0.0.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Enable & Start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable english-12-smart
sudo systemctl start english-12-smart

# Check status
sudo systemctl status english-12-smart
```

### 7.2 Create Systemd Service - Python TTS

Create file: `/etc/systemd/system/edge-tts-service.service`

```bash
sudo nano /etc/systemd/system/edge-tts-service.service

# Paste:
```

```ini
[Unit]
Description=Edge TTS Service - Python
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/var/www/english-12-smart/python-services/edge-tts-service
Environment="PATH=/var/www/english-12-smart/python-services/edge-tts-service/venv/bin"
ExecStart=/var/www/english-12-smart/python-services/edge-tts-service/venv/bin/python app.py
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Enable & Start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable edge-tts-service
sudo systemctl start edge-tts-service

# Check status
sudo systemctl status edge-tts-service
```

### 7.3 Verify Both Services Running
```bash
# Check both
sudo systemctl status english-12-smart edge-tts-service

# View logs
sudo journalctl -u english-12-smart -f  # Follow Spring Boot logs
sudo journalctl -u edge-tts-service -f  # Follow Python logs

# Test endpoints
curl http://localhost:8080/  # Spring Boot
curl http://localhost:5000/health  # Python (if available)
```

---

## Bước 8: Setup Nginx + SSL

### 8.1 Create Nginx Configuration

Create file: `/etc/nginx/conf.d/english-12-smart.conf`

```bash
sudo nano /etc/nginx/conf.d/english-12-smart.conf

# Paste:
```

```nginx
# Upstream services
upstream spring_boot {
    server 127.0.0.1:8080;
}

upstream edge_tts {
    server 127.0.0.1:5000;
}

# Redirect HTTP to HTTPS
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }
    
    location / {
        return 301 https://$server_name$request_uri;
    }
}

# HTTPS Server
server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;
    
    # SSL Certificates (will be set up with Certbot)
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # SSL Configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    
    # Security Headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    
    # Logging
    access_log /var/log/nginx/english-12-smart-access.log;
    error_log /var/log/nginx/english-12-smart-error.log;
    
    # Proxy settings
    client_max_body_size 100M;
    
    # Compress response
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    
    # Main application
    location / {
        proxy_pass http://spring_boot;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # Python TTS service
    location /api/tts/ {
        proxy_pass http://edge_tts/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    # WebSocket support
    location /ws {
        proxy_pass http://spring_boot;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 8.2 Test & Enable Nginx

```bash
# Test config
sudo nginx -t
# Output: nginx: configuration file test is successful

# Restart Nginx
sudo systemctl restart nginx
```

### 8.3 Setup SSL with Certbot

```bash
# Request certificate (auto-renew)
sudo certbot certonly --nginx -d your-domain.com -d www.your-domain.com

# Follow prompts:
# 1. Enter email
# 2. Agree to terms
# 3. Certificate will be created

# Verify
sudo ls -la /etc/letsencrypt/live/your-domain.com/
```

### 8.4 Auto-Renew SSL Certificate

```bash
# Certbot auto-renewal timer usually enabled by default
sudo systemctl enable certbot-renew.timer
sudo systemctl start certbot-renew.timer

# Test renewal
sudo certbot renew --dry-run
```

---

## Troubleshooting

### Issue: Service won't start

```bash
# Check logs
sudo journalctl -u english-12-smart -n 50  # Last 50 lines
sudo journalctl -u edge-tts-service -n 50

# Common issues:
# 1. Port already in use
sudo lsof -i :8080
sudo lsof -i :5000

# 2. Permission denied
sudo chown -R ec2-user:ec2-user /var/www/english-12-smart

# 3. Java not found
source /etc/profile.d/java.sh
```

### Issue: MongoDB connection fails

```bash
# Check connection string in application.properties
# Verify IP whitelist in MongoDB Atlas

# Test locally:
mongosh "mongodb+srv://user:pass@cluster0.mongodb.net/test"
```

### Issue: Nginx returns 502 Bad Gateway

```bash
# Check backend services running
curl http://localhost:8080
curl http://localhost:5000

# Check Nginx logs
sudo tail -f /var/log/nginx/english-12-smart-error.log
```

### Issue: SSL certificate issues

```bash
# Check certificate validity
sudo certbot certificates

# Renew manually
sudo certbot renew --force-renewal

# Check Nginx SSL config
sudo nginx -T | grep ssl
```

---

## Monitoring & Maintenance

### Check Service Health
```bash
#!/bin/bash
echo "=== Spring Boot Status ==="
sudo systemctl status english-12-smart

echo -e "\n=== Python Service Status ==="
sudo systemctl status edge-tts-service

echo -e "\n=== Nginx Status ==="
sudo systemctl status nginx

echo -e "\n=== Database Status ==="
sudo systemctl status mariadb redis

echo -e "\n=== Disk Usage ==="
df -h
```

### View Recent Logs
```bash
# Spring Boot
sudo journalctl -u english-12-smart -n 100

# Python TTS
sudo journalctl -u edge-tts-service -n 100

# Nginx
sudo tail -50 /var/log/nginx/english-12-smart-access.log
```

### Restart All Services
```bash
sudo systemctl restart english-12-smart edge-tts-service nginx
```

---

## Checklists

### ✅ Pre-Deployment
- [ ] AWS Account created & EC2 t3.micro launched
- [ ] Key pair downloaded & secured
- [ ] Security group configured (ports 22, 80, 443 open)
- [ ] Domain name pointed to EC2 Elastic IP (optional but recommended)

### ✅ Installation
- [ ] Java 17 installed & verified
- [ ] Maven 3 installed & verified
- [ ] Python 3 installed & verified
- [ ] MySQL/MariaDB installed & running
- [ ] Redis installed & running
- [ ] Nginx installed & running
- [ ] Certbot installed

### ✅ Application Setup
- [ ] Project cloned from Git
- [ ] Maven build successful (target/english-12-smart-1.0.0.jar exists)
- [ ] application.properties configured with real credentials
- [ ] Python venv created & dependencies installed
- [ ] Database created & user granted

### ✅ Services Running
- [ ] Spring Boot service enabled & running
- [ ] Python TTS service enabled & running
- [ ] Both services restart on reboot

### ✅ Nginx & SSL
- [ ] Nginx configuration created & tested
- [ ] SSL certificate issued by Certbot
- [ ] HTTPS working (visit https://your-domain.com)
- [ ] HTTP redirects to HTTPS
- [ ] Auto-renewal configured

---

## Next Steps

1. **Monitor Resources**: Watch CPU/memory usage
   ```bash
   htop
   ```

2. **Setup CloudWatch Alarms** in AWS Console
3. **Implement Automated Backups** for database
4. **Setup Log Aggregation** (CloudWatch Logs, etc.)
5. **Performance Tuning** (Nginx buffer, JVM heap, etc.)

---

**Last Updated**: April 8, 2026  
**Status**: Ready for Deployment
