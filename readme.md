# 宠物医院管理系统 (Pet Hospital Management System)

基于 Spring Boot + Vue 3 的宠物医院管理系统，支持用户注册登录、预约挂号、诊疗记录、药品管理、医患聊天等功能。

## 技术栈

- **后端**: Spring Boot 2.7、MyBatis-Plus、Shiro、MySQL、Redis、JWT
- **前端**: Vue 3、Vite、Element Plus、Vue Router、Axios
- **运行环境**: JDK 8+、Node.js 18+、MySQL 5.7+、Redis

---

## 一、环境依赖

| 依赖 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 1.8+ | 后端运行 |
| Maven | 3.6+ | 后端构建（或使用 IDE 自带） |
| Node.js | 18+ | 前端开发与构建 |
| MySQL | 5.7+ | 数据库 |
| Redis | 5.0+ | 缓存、验证码、会话 |

---

## 二、Windows 安装与部署

### 2.1 安装 JDK

```powershell
# 使用 winget 安装（如已安装可跳过）
winget install Microsoft.OpenJDK.8

# 验证
java -version
```

或从 [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) / [Adoptium](https://adoptium.net/) 下载安装，并配置 `JAVA_HOME`。

### 2.2 安装 Maven（可选）

```powershell
# 使用 Chocolatey
choco install maven

# 验证
mvn -v
```

或在 IDEA 中使用自带的 Maven。

### 2.3 安装 Node.js

```powershell
# 使用 winget
winget install OpenJS.NodeJS.LTS

# 验证
node -v
npm -v
```

### 2.4 安装 MySQL

1. 从 [MySQL 下载页](https://dev.mysql.com/downloads/mysql/) 下载 Windows 安装包并安装。
2. 创建数据库与用户：

```sql
CREATE DATABASE phms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'phms'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON phms.* TO 'phms'@'localhost';
FLUSH PRIVILEGES;
```

3. 导入初始数据（将 `dump-phms-xxx.sql` 或项目提供的 SQL 文件放到合适路径后执行）：

```powershell
mysql -u root -p phms < "dump-phms-202601170511.sql"
```

或在 MySQL 客户端中执行该 SQL 文件。

### 2.5 安装并启动 Redis

**方式一：Windows 版 Redis**

```powershell
# 从 https://github.com/microsoftarchive/redis/releases 下载 Redis for Windows
# 解压后进入目录，启动服务：
redis-server.exe
```

**方式二：WSL2 中安装 Redis**

```bash
# 在 WSL 终端中
sudo apt update
sudo apt install redis-server -y
sudo service redis-server start
```

**方式三：使用项目提供的批处理（若存在）**

```powershell
# 启动 Redis
.\启动Redis.bat

# 停止 Redis
.\停止Redis.bat
```

验证 Redis：

```powershell
redis-cli ping
# 应返回 PONG
```

### 2.6 配置后端

编辑 `Pet Hospital Management System Backend/src/main/resources/application.properties`：

- 修改数据库连接（如需要）：
  - `spring.datasource.url`
  - `spring.datasource.username`
  - `spring.datasource.password`
- 确认 Redis 为本机且未改端口（默认 6379）。
- 邮件相关（注册验证码等）按需配置 `spring.mail.*`。

### 2.7 启动后端（Windows）

```powershell
cd "Pet Hospital Management System Backend"

# 使用 Maven 运行
mvn spring-boot:run

# 或先打包再运行
mvn clean package -DskipTests
java -jar target/phms-1.0.8.jar
```

后端默认地址：`http://localhost:8186`。

### 2.8 安装前端依赖并启动（Windows）

```powershell
cd "Pet Hospital Management System Frontend"

# 安装依赖
npm install

# 开发模式运行
npm run dev
```

前端默认地址：`http://localhost:5173`。浏览器访问该地址即可使用系统。

### 2.9 生产构建（Windows）

```powershell
# 前端打包
cd "Pet Hospital Management System Frontend"
npm run build

# 后端打包
cd "Pet Hospital Management System Backend"
mvn clean package -DskipTests
```

将 `Backend/target/phms-1.0.8.jar` 与 `Frontend/dist` 部署到服务器，或用 Nginx 托管 `dist`，接口代理到 `8186`。

---

## 三、Linux 安装与部署

### 3.1 安装 JDK 8

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-8-jdk -y

# CentOS/RHEL
sudo yum install java-1.8.0-openjdk java-1.8.0-openjdk-devel -y

# 验证
java -version
javac -version
```

### 3.2 安装 Maven

```bash
# Ubuntu/Debian
sudo apt install maven -y

# CentOS/RHEL
sudo yum install maven -y

mvn -v
```

### 3.3 安装 Node.js 18+

```bash
# 使用 NodeSource（Ubuntu/Debian）
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y

# 或使用 nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc
nvm install 18
nvm use 18

node -v
npm -v
```

### 3.4 安装 MySQL

```bash
# Ubuntu/Debian
sudo apt install mysql-server -y
sudo systemctl start mysql
sudo systemctl enable mysql

# CentOS/RHEL
sudo yum install mysql-server -y
sudo systemctl start mysqld
sudo systemctl enable mysqld
```

创建数据库并导入数据：

```bash
sudo mysql -u root -p -e "CREATE DATABASE phms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p phms < dump-phms-202601170511.sql
```

（若使用远程 SQL 文件，请先上传到服务器再执行。）

### 3.5 安装并启动 Redis

```bash
# Ubuntu/Debian
sudo apt install redis-server -y
sudo systemctl start redis-server
sudo systemctl enable redis-server

# CentOS/RHEL
sudo yum install redis -y
sudo systemctl start redis
sudo systemctl enable redis

# 验证
redis-cli ping
```

### 3.6 配置后端

与 Windows 相同，修改 `Pet Hospital Management System Backend/src/main/resources/application.properties` 中的数据库连接、Redis、邮件等配置。若 MySQL/Redis 不在本机，请修改相应主机与端口。

### 3.7 启动后端（Linux）

```bash
cd "Pet Hospital Management System Backend"

# 开发运行
mvn spring-boot:run

# 或后台运行
nohup mvn spring-boot:run > backend.log 2>&1 &

# 生产：打包后运行
mvn clean package -DskipTests
nohup java -jar target/phms-1.0.8.jar > backend.log 2>&1 &
```

### 3.8 安装前端依赖并启动（Linux）

```bash
cd "Pet Hospital Management System Frontend"

npm install
npm run dev
```

生产环境建议使用 `npm run build` 生成 `dist`，再用 Nginx 托管。

### 3.9 使用 Nginx 托管前端（可选）

```bash
# 安装 Nginx
sudo apt install nginx -y   # Ubuntu/Debian
# 或
sudo yum install nginx -y   # CentOS/RHEL

# 前端打包
cd "Pet Hospital Management System Frontend"
npm run build

# 将 dist 复制到 Nginx 目录
sudo cp -r dist/* /var/www/html/

# 配置反向代理到后端（示例片段）
# 在 /etc/nginx/sites-available/default 或新建站点配置中加入：
# location /api/ { proxy_pass http://127.0.0.1:8186/; }
# 以及 /login、/logout、/captcha 等接口的 proxy_pass

sudo nginx -t
sudo systemctl reload nginx
```

---

## 四、快速启动命令汇总

### Windows（开发环境）

```powershell
# 1. 启动 Redis（若未安装为服务）
redis-server

# 2. 启动后端（新开终端）
cd "Pet Hospital Management System Backend"
mvn spring-boot:run

# 3. 启动前端（再开终端）
cd "Pet Hospital Management System Frontend"
npm install
npm run dev
```

访问：`http://localhost:5173`。

### Linux（开发环境）

```bash
# 1. 启动 Redis
sudo systemctl start redis-server

# 2. 启动后端
cd "Pet Hospital Management System Backend"
mvn spring-boot:run &

# 3. 启动前端
cd "Pet Hospital Management System Frontend"
npm install && npm run dev
```

访问：`http://服务器IP:5173`（或配置 Nginx 后访问 80 端口）。

### 生产部署（Linux）

```bash
# 后端
cd "Pet Hospital Management System Backend"
mvn clean package -DskipTests
nohup java -jar target/phms-1.0.8.jar --spring.profiles.active=prod > backend.log 2>&1 &

# 前端
cd "Pet Hospital Management System Frontend"
npm ci
npm run build
# 将 dist 部署到 Nginx 或其他 Web 服务器
```

---

## 五、端口说明

| 服务 | 默认端口 |
|------|----------|
| 后端 API | 8186 |
| 前端开发服务器 | 5173 |
| MySQL | 3306 |
| Redis | 6379 |

---

## 六、常见问题

1. **Redis 连接失败**  
   确认 Redis 已启动：`redis-cli ping` 返回 `PONG`。Windows 可先运行 `redis-server.exe` 或使用 WSL 中的 Redis。

2. **数据库连接失败**  
   检查 MySQL 是否启动、库名/用户名/密码是否与 `application.properties` 一致，以及是否已导入 SQL。

3. **前端请求后端 404**  
   开发环境下确认 Vite 代理目标为 `http://localhost:8186`（见 `vite.config.ts`），且后端已启动。

4. **邮箱验证码收不到**  
   在 `application.properties` 中配置正确的 QQ 邮箱 SMTP 与授权码；若仅做本地测试，可使用项目提供的“获取验证码”调试接口（见后端说明）。

---

## 七、仓库与许可

- 代码仓库：请根据实际 GitHub 地址填写。
- 本项目仅供学习与毕业设计使用。

---

**部署前请确保：MySQL 已建库并导入数据、Redis 已启动、后端配置文件中的数据库与 Redis 地址正确。**
