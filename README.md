# AWP-backend

记账网页（Accounting-WebPage）后端服务。

## 技术栈

- Spring Boot 3.x
- MyBatis 3（mybatis-spring-boot-starter）
- MySQL 8.x（JDBC + HikariCP）
- Maven / JDK 17+

## 运行

1. 创建数据库并执行建表脚本 `src/main/resources/sql/schema.sql`。
2. 修改 `src/main/resources/application.yml` 中的数据库连接信息。
3. 启动：

```bash
mvn spring-boot:run
```

默认端口 `8080`，接口前缀 `/api`。

## 目录结构

```
src/main/java/com/awp/
├── AwpApplication.java   # 启动类
├── controller/           # 控制器
├── service/ + impl/      # 业务逻辑
├── mapper/               # MyBatis 接口
├── entity/               # 数据库实体
├── dto/                  # 传输对象
├── common/               # 统一响应、异常处理
└── config/               # 配置（跨域等）
```

详见仓库外的《AWP-设计文档.md》。
