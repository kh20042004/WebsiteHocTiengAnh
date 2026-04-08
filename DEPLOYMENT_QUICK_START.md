# 🚀 Quick Deployment Guide - English 12 Smart

**EC2 Instance Details:**
- Public IPv4: `98.93.38.188`
- Key File: `Learnenglish.pem`
- Domain: `studyenglish12.site`
- Instance Type: `t3.micro`
- Region: (auto-detect from instance)

---

## ✅ Bước 1: Test SSH Connection

### Từ Windows PowerShell

```powershell
# Navigate to key directory
cd C:\Users\Kiet\WebsiteHocTiengAnh

# Test connection
ssh -i Learnenglish.pem ec2-user@98.93.38.188

# Output should be:
# The authenticity of host '98.93.38.188' can't be established.
# Are you sure you want to continue connecting (yes/no/[fingerprint])? 
# Type: yes

# You should now be logged in as ec2-user
```

If successful, you'll see:
```
[ec2-user@ip-xxx ~]$
```

---

## ⚙️ Bước 2: Chạy Auto Installation Script

### Copy Script dưới đây, chạy trên EC2 terminal:

```bash
# Create deployment script
cat > ~/deploy.sh << 'DEPLOYEOF'
#!/bin/bash
set -e

echo "════════════════════════════════════════════════════════════"
echo "  English 12 Smart - AWS EC2 Auto Deployment"
echo "════════════════════════════════════════════════════════════"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print status
print_status() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

print_info() {
    echo -e "${YELLOW}[ℹ]${NC} $1"
}

# ────────────────────────────────────────────────────────────────
# 1. Update System
# ────────────────────────────────────────────────────────────────
print_info "Step 1/10: Updating system packages..."
sudo yum update -y > /dev/null
sudo yum install -y htop git wget curl sudo > /dev/null
print_status "System updated"

# ────────────────────────────────────────────────────────────────
# 2. Install Java 17
# ────────────────────────────────────────────────────────────────
print_info "Step 2/10: Installing Java 17..."
if ! command -v java &> /dev/null; then
    cd /tmp
    wget -q https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz
    sudo mkdir -p /usr/local/java
    sudo tar -zxf openjdk-17.0.2_linux-x64_bin.tar.gz -C /usr/local/java
    sudo tee /etc/profile.d/java.sh > /dev/null << 'JAVAEOF'
export JAVA_HOME=/usr/local/java/jdk-17.0.2
export PATH=$JAVA_HOME/bin:$PATH
JAVAEOF
    source /etc/profile.d/java.sh
fi
java -version 2>&1 | head -1
print_status "Java 17 installed"

# ────────────────────────────────────────────────────────────────
# 3. Install Maven
# ────────────────────────────────────────────────────────────────
print_info "Step 3/10: Installing Maven 3.8.6..."
if ! command -v mvn &> /dev/null; then
    cd /tmp
    wget -q https://archive.apache.org/dist/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz
    sudo mkdir -p /opt
    sudo tar -zxf apache-maven-3.8.6-bin.tar.gz -C /opt
    sudo tee /etc/profile.d/maven.sh > /dev/null << 'MAVENEOF'
export MAVEN_HOME=/opt/apache-maven-3.8.6
export PATH=$MAVEN_HOME/bin:$PATH
MAVENEOF
    source /etc/profile.d/maven.sh
fi
mvn -version 2>&1 | head -1
print_status "Maven 3.8.6 installed"

# ────────────────────────────────────────────────────────────────
# 4. Install Python 3
# ────────────────────────────────────────────────────────────────
print_info "Step 4/10: Installing Python 3..."
sudo yum install -y python3 python3-pip > /dev/null
python3 --version
pip3 install --upgrade pip --quiet
print_status "Python 3 installed"

# ────────────────────────────────────────────────────────────────
# 5. Install MySQL/MariaDB
# ────────────────────────────────────────────────────────────────
print_info "Step 5/10: Installing MariaDB..."
sudo yum install -y mariadb-server > /dev/null
sudo systemctl enable mariadb > /dev/null
sudo systemctl start mariadb > /dev/null
print_status "MariaDB installed & running"

# ────────────────────────────────────────────────────────────────
# 6. Install Redis
# ────────────────────────────────────────────────────────────────
print_info "Step 6/10: Installing Redis..."
sudo yum install -y redis > /dev/null
sudo systemctl enable redis > /dev/null
sudo systemctl start redis > /dev/null
redis-cli ping > /dev/null
print_status "Redis installed & running"

# ────────────────────────────────────────────────────────────────
# 7. Install Nginx
# ────────────────────────────────────────────────────────────────
print_info "Step 7/10: Installing Nginx..."
sudo yum install -y nginx > /dev/null
sudo systemctl enable nginx > /dev/null
print_status "Nginx installed"

# ────────────────────────────────────────────────────────────────
# 8. Install Certbot
# ────────────────────────────────────────────────────────────────
print_info "Step 8/10: Installing Certbot..."
sudo yum install -y certbot python3-certbot-nginx > /dev/null
print_status "Certbot installed"

# ────────────────────────────────────────────────────────────────
# 9. Clone Project
# ────────────────────────────────────────────────────────────────
print_info "Step 9/10: Cloning project repository..."
sudo mkdir -p /var/www
sudo chown -R ec2-user:ec2-user /var/www
cd /var/www

# Clone from GitHub or use local upload
if [ ! -d "english-12-smart" ]; then
    echo "Please provide the GitHub URL or upload the project to /var/www/english-12-smart"
    echo "For now, creating placeholder..."
    mkdir -p english-12-smart
fi
print_status "Project directory ready at /var/www/english-12-smart"

# ────────────────────────────────────────────────────────────────
# 10. Setup PATH for Java & Maven
# ────────────────────────────────────────────────────────────────
print_info "Step 10/10: Setting up environment..."
source /etc/profile.d/java.sh
source /etc/profile.d/maven.sh
print_status "Environment configured"

# ────────────────────────────────────────────────────────────────
# Summary
# ────────────────────────────────────────────────────────────────
echo ""
echo "════════════════════════════════════════════════════════════"
echo -e "${GREEN}✓ Installation Complete!${NC}"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "Next Steps:"
echo "1. Clone project to /var/www/english-12-smart"
echo "2. Create application.properties with your credentials"
echo "3. Run: mvn clean package -DskipTests"
echo "4. Setup Nginx & SSL"
echo ""
echo "Verify installations:"
echo "  java -version"
echo "  mvn -version"
echo "  python3 --version"
echo "  redis-cli ping"
echo "  mysql -u root"
echo ""

DEPLOYEOF

# Make executable and run
chmod +x ~/deploy.sh
bash ~/deploy.sh
```

---

## 📦 Bước 3: Clone Dự Án

```bash
cd /var/www

# Option A: Nếu repo là public
git clone https://github.com/yourusername/english-12-smart.git
cd english-12-smart

# Option B: Nếu repo là private, sử dụng git credentials hoặc SSH key

# Verify structure
ls -la
# Should show: pom.xml, src/, python-services/, etc.
```

---

## 🔧 Bước 4: Cấu Hình Application Properties

```bash
cd /var/www/english-12-smart/src/main/resources

# Backup example
cp application-example.properties application-example.properties.bak

# Edit configuration
nano application.properties
```

**Paste configuration:**

```properties
# Server
server.port=8080
server.servlet.context-path=/
spring.application.name=english-12-smart

# MongoDB (Replace with your credentials)
spring.data.mongodb.uri=mongodb+srv://YOUR_MONGO_USER:YOUR_MONGO_PASSWORD@cluster0.mongodb.net/english_12_smart?retryWrites=true&w=majority

# Redis
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379

# JWT (Generate new secret!)
jwt.secret=your-super-secret-key-change-this-at-least-64-characters-long-12345678
jwt.expiration=86400000

# Google OAuth2 (Optional - leave empty if not using)
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
spring.security.oauth2.client.provider.google.token-uri=https://www.googleapis.com/oauth2/v4/token
spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v1/userinfo

# Cloudinary (Optional - for image uploads)
cloudinary.cloud-name=YOUR_CLOUDINARY_NAME
cloudinary.api-key=YOUR_CLOUDINARY_KEY
cloudinary.api-secret=YOUR_CLOUDINARY_SECRET

# Email (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Python TTS Service
edge-tts.service.url=http://localhost:5000

# Logging
logging.level.root=INFO
logging.level.com.english12smart=DEBUG
```

Save: `Ctrl+X` → `Y` → `Enter`

---

## 🏗️ Bước 5: Build Project

```bash
cd /var/www/english-12-smart

# Load Java & Maven paths
source /etc/profile.d/java.sh
source /etc/profile.d/maven.sh

# Build
mvn clean package -DskipTests

# Wait for completion... (5-10 minutes)
# Look for: BUILD SUCCESS
# JAR file: target/english-12-smart-1.0.0.jar
```

---

## 🗄️ Bước 6: Setup Database

```bash
# Login to MySQL
mysql -u root

# SQL commands:
CREATE DATABASE english_12_smart CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'smartapp'@'localhost' IDENTIFIED BY 'StrongSecurePassword123!';
GRANT ALL PRIVILEGES ON english_12_smart.* TO 'smartapp'@'localhost';
FLUSH PRIVILEGES;

# Update your MongoDB connection if needed
EXIT;
```

---

## 🐍 Bước 7: Setup Python TTS Service

```bash
cd /var/www/english-12-smart/python-services/edge-tts-service

# Create virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Test
python3 -c "import edge_tts; print('✓ edge_tts OK')"

# Create .env file
cat > .env << 'PYEOF'
FLASK_APP=app.py
FLASK_ENV=production
PYTHONUNBUFFERED=1
PORT=5000
HOST=127.0.0.1
PYEOF

deactivate
```

---

## 🚀 Bước 8: Create Systemd Services

### Spring Boot Service

```bash
sudo tee /etc/systemd/system/english-12-smart.service > /dev/null << 'SBEOF'
[Unit]
Description=English 12 Smart - Spring Boot
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/var/www/english-12-smart
Environment="JAVA_HOME=/usr/local/java/jdk-17.0.2"
Environment="PATH=/usr/local/java/jdk-17.0.2/bin:/opt/apache-maven-3.8.6/bin:/usr/local/bin:/usr/bin"
ExecStart=/usr/local/java/jdk-17.0.2/bin/java -jar target/english-12-smart-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
SBEOF

# Enable & Start
sudo systemctl daemon-reload
sudo systemctl enable english-12-smart
sudo systemctl start english-12-smart
```

### Python TTS Service

```bash
sudo tee /etc/systemd/system/edge-tts-service.service > /dev/null << 'PYEOF'
[Unit]
Description=Edge TTS Service
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/var/www/english-12-smart/python-services/edge-tts-service
Environment="PATH=/var/www/english-12-smart/python-services/edge-tts-service/venv/bin"
ExecStart=/var/www/english-12-smart/python-services/edge-tts-service/venv/bin/python app.py
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
PYEOF

# Enable & Start
sudo systemctl daemon-reload
sudo systemctl enable edge-tts-service
sudo systemctl start edge-tts-service
```

### Verify Services

```bash
# Check both services
sudo systemctl status english-12-smart
sudo systemctl status edge-tts-service

# View logs
sudo journalctl -u english-12-smart -f
sudo journalctl -u edge-tts-service -f
```

---

## 🔐 Bước 9: Setup Nginx + SSL

### Create Nginx Config

```bash
sudo tee /etc/nginx/conf.d/english-12-smart.conf > /dev/null << 'NGEOF'
upstream spring_boot {
    server 127.0.0.1:8080;
}

upstream edge_tts {
    server 127.0.0.1:5000;
}

# HTTP → HTTPS redirect
server {
    listen 80;
    server_name studyenglish12.site www.studyenglish12.site;
    
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
    server_name studyenglish12.site www.studyenglish12.site;
    
    ssl_certificate /etc/letsencrypt/live/studyenglish12.site/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/studyenglish12.site/privkey.pem;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    
    add_header Strict-Transport-Security "max-age=31536000" always;
    
    client_max_body_size 100M;
    
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
    
    location / {
        proxy_pass http://spring_boot;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /api/tts/ {
        proxy_pass http://edge_tts/;
        proxy_set_header Host $host;
    }
    
    location /ws {
        proxy_pass http://spring_boot;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
NGEOF

# Test & Start Nginx
sudo nginx -t
sudo systemctl start nginx
```

### Get SSL Certificate

```bash
# Update your domain first to point to this IP: 98.93.38.188
# Wait for DNS propagation (~5-30 minutes)

# Request certificate
sudo certbot certonly --nginx -d studyenglish12.site -d www.studyenglish12.site

# Follow prompts:
# 1. Email: your-email@gmail.com
# 2. Agree to terms
# 3. Choose sharing preferences

# Restart Nginx
sudo systemctl restart nginx

# Test renewal
sudo certbot renew --dry-run
```

---

## ✅ Verification Checklist

```bash
# 1. Check services running
sudo systemctl status english-12-smart
sudo systemctl status edge-tts-service
sudo systemctl status nginx

# 2. Check ports listening
sudo netstat -tulpn | grep LISTEN
# Should see: 8080 (Spring), 5000 (Python), 80 (Nginx), 443 (Nginx)

# 3. Test endpoints
curl http://localhost:8080/  # Should connect
curl http://localhost:5000/health  # Check if available

# 4. View real-time logs
sudo journalctl -u english-12-smart -f
sudo journalctl -u edge-tts-service -f
sudo tail -f /var/log/nginx/access.log
```

---

## 🔗 After Deployment

Once everything is running:

1. **Create DNS A Record**:
   - In your domain registrar, add:
     - Type: A
     - Name: @ (or studyenglish12.site)
     - Value: 98.93.38.188

2. **Wait for DNS** (can take 5-30 minutes)

3. **Visit website**:
   - https://studyenglish12.site

4. **Check SSL**:
   - Browser should show 🔒 lock icon

---

## 📋 Troubleshooting

### Service won't start
```bash
# Check logs
sudo journalctl -u english-12-smart -n 50
sudo journalctl -u edge-tts-service -n 50

# Fix permissions
sudo chown -R ec2-user:ec2-user /var/www/english-12-smart
```

### 502 Bad Gateway
```bash
# Check if Spring Boot is running
curl http://localhost:8080

# Check Nginx logs
sudo tail /var/log/nginx/error.log
```

### SSL certificate issues
```bash
# Check certificate status
sudo certbot certificates

# Renew manually
sudo certbot renew --force-renewal
```

---

**Ready to deploy? Start with Bước 1!** ✨
