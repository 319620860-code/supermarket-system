# 超市信息管理系统（Supermarket Management System）

基于 Web 的超市信息管理系统：Spring Boot 3.2.2 后端 + Vue 3 前端，MySQL 8.0 存储，Redis 可选缓存。

> 毕业设计项目。涵盖商品、库存、供应商、销售订单、报表、权限（RBAC）、操作日志等模块。

## 目录结构

```
biyesheji/
├── backend/            # Spring Boot 后端（默认端口 8080）
│   └── src/main/java/com/supermarket/backend
├── frontend/           # Vue 3 + Vite 前端（开发端口 3000）
├── *.sql               # 数据库初始化 / 测试数据 / 校验脚本
└── (CloudCode/ 为无关目录，已 gitignore)
```

## 技术栈

- 后端：Spring Boot 3.2.2、Spring Security、MyBatis-Plus 3.5.8、Spring Data Redis（可选）、Quartz、JWT、EasyExcel
- 前端：Vue 3.3、Vite 4、Element Plus、ECharts、Axios、Vue Router
- 数据库：MySQL 8.0；缓存：Redis（开发态默认内存缓存）

## 环境要求

- JDK 21+（已在 JDK 25 验证通过）
- Maven 3.9+
- Node.js 18+
- MySQL 8.0

## 数据库初始化

1. 创建数据库 `supermarket`（推荐 UTF-8）：
   ```sql
   CREATE DATABASE supermarket CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```
2. 执行根目录 SQL 初始化数据，例如 `insert_test_data.sql`（按需执行 `check_*.sql` / `verify_data.sql` / `test_*.sql` 做校验）。
3. Quartz 调度表由 `spring.quartz.jdbc.initialize-schema=always` 在应用启动时自动建表，无需手动执行。

## 本地运行

后端：

```bash
cd backend
mvn spring-boot:run
# 或打包后运行
mvn package && java -jar target/supermarket-backend-0.0.1-SNAPSHOT.jar
```

前端（开发模式，默认 http://localhost:3000，已配置 `/api` 代理到后端 8080）：

```bash
cd frontend
npm install
npm run dev
```

生产构建：执行 `npm run build` 生成 `frontend/dist/`，将其拷贝到 `backend/src/main/resources/static/` 后由后端在 8080 统一提供服务。

## 配置（环境变量 / application-local.properties）

`application.properties` 已通过 `${ENV:默认值}` 支持环境变量覆盖，常用项：

| 变量 | 说明 | 默认 |
| --- | --- | --- |
| `DB_URL` | JDBC 连接串 | `jdbc:mysql://localhost:3306/supermarket?...` |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号 | `root` / `123456` |
| `JWT_SECRET` | JWT 签名密钥（**生产务必设置**） | 内置默认值 |
| `JWT_EXPIRATION` | Token 有效期(ms) | `86400000` |
| `SPRING_SECURITY_USER_NAME` / `SPRING_SECURITY_USER_PASSWORD` | 开发用登录 | `admin` / `admin` |
| `REDIS_HOST` / `REDIS_PORT` | 启用 Redis 缓存时配置（取消 `application.properties` 中 Redis 段注释） | `localhost` / `6379` |

> 敏感配置请放在 `application-local.properties`（已被 gitignore），不要提交到仓库。

## 默认账号

Spring Security 开发账号：`admin` / `admin`（生产请通过环境变量修改）。
