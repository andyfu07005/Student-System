# 学生管理系统 (Student Management System)

一个基于前后端分离架构的综合性学生信息管理系统（SIMS），支持学生管理、班级管理、课程管理、成绩管理、用户权限管理及操作日志审计等功能。

## 功能特性

- **学生管理**：学生信息的增删改查、学号自动生成、批量导入导出
- **班级管理**：班级创建与维护、班主任指派、年级管理
- **课程管理**：课程开设、学分与学时管理、授课教师分配
- **成绩管理**：成绩录入与编辑、绩点自动计算、补考与重修标记、成绩统计分析
- **RBAC 权限体系**：基于角色的访问控制，菜单/按钮/接口三级权限粒度
- **操作日志审计**：全量记录用户操作行为，支持按模块、操作类型、时间范围检索
- **数据安全**：BCrypt 密码加密、CORS 跨域防护、SQL 注入防护（MyBatis-Plus 参数化查询）

## 技术栈

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 | 快速开发脚手架，自动配置 |
| ORM | MyBatis-Plus | 3.5.6 | 增强版 MyBatis，简化 CRUD |
| 数据库 | MySQL | 8.0+ | 关系型数据库，InnoDB 引擎 |
| JDK | Java | 17 | LTS 长期支持版本 |
| 构建工具 | Maven | 3.8+ | 依赖管理与项目构建 |
| 前端框架 | Vue 3 | 3.4 | Composition API + setup 语法糖 |
| 构建工具 | Vite | 5.2 | 极速开发体验，ESBuild 预构建 |
| UI 组件库 | Element Plus | 2.7 | 企业级 Vue 3 组件库 |
| HTTP 客户端 | Axios | 1.6 | Promise 风格 HTTP 请求 |
| 类型系统 | TypeScript | 5.4 | 静态类型检查，提升代码健壮性 |
| 代码规范 | ESLint + Prettier | — | 代码风格统一，保存自动格式化 |

## 项目结构

```
Student-System/
├── backend/                                   # 后端 Spring Boot 项目
│   ├── pom.xml                                # Maven 依赖与插件配置
│   └── src/main/
│       ├── java/com/student/
│       │   ├── StudentSystemApplication.java  # Spring Boot 启动入口
│       │   │                                  #   @MapperScan 自动扫描 Mapper 接口
│       │   ├── common/                        # 通用模块（工具类、响应封装、常量）
│       │   │   └── Result.java                # 统一 API 响应体 {code, message, data}
│       │   ├── config/                        # 配置模块（CORS、安全、MyBatis-Plus）
│       │   │   └── CorsConfig.java            # 跨域请求配置，开发环境放行所有来源
│       │   ├── controller/                    # 控制器层（接收请求、参数校验、调用 Service）
│       │   │   └── HealthController.java      # 健康检查接口示例
│       │   ├── service/                       # [待创建] 业务逻辑层（事务管理、业务编排）
│       │   │   └── impl/                      # Service 实现类
│       │   ├── mapper/                        # [待创建] 数据访问层（MyBatis-Plus Mapper 接口）
│       │   ├── entity/                        # [待创建] 实体类（与数据库表一一映射）
│       │   └── dto/                           # [待创建] 数据传输对象（请求/响应模型）
│       └── resources/
│           └── application.yml                # 应用配置（端口、数据源、MyBatis-Plus）
├── frontend/                                  # 前端 Vue 3 项目
│   ├── index.html                             # HTML 入口文件
│   ├── package.json                           # NPM 依赖与脚本
│   ├── vite.config.ts                         # Vite 配置（路径别名、代理、插件）
│   ├── tsconfig.json                          # TypeScript 编译配置
│   ├── tsconfig.node.json                     # Node 端 TypeScript 配置
│   ├── .eslintrc.json                         # ESLint 规则（Vue3 + TS + Prettier）
│   ├── .prettierrc.json                       # Prettier 格式化规则（无分号、单引号）
│   └── src/
│       ├── main.ts                            # 应用入口，全局注册 Element Plus
│       ├── App.vue                            # 根组件，整体布局（Header + Main）
│       ├── env.d.ts                           # TypeScript 类型声明（.vue 模块）
│       ├── api/                               # API 接口层
│       │   ├── request.ts                     # Axios 实例（baseURL、拦截器、错误处理）
│       │   └── health.ts                      # 健康检查 API 封装
│       ├── components/                        # 公共组件
│       │   └── HealthCheck.vue                # 服务健康状态展示组件
│       ├── views/                             # [待创建] 页面组件（路由对应页面）
│       ├── router/                            # [待创建] Vue Router 路由配置
│       ├── stores/                            # [待创建] Pinia 状态管理
│       └── utils/                             # [待创建] 工具函数（日期、格式化等）
└── database/
    └── schema.sql                             # 数据库初始化脚本（建库、建表、种子数据）
```

## 系统架构

### 整体架构

```
┌──────────────────────────────────────────────────────────┐
│                      客户端 (Browser)                      │
│               http://localhost:3000                       │
└────────────────────┬─────────────────────────────────────┘
                     │
                     │  /api/* 请求 → Vite Proxy 转发
                     ▼
┌──────────────────────────────────────────────────────────┐
│                  Vite Dev Server (Port 3000)              │
│  ┌──────────┐  ┌────────────┐  ┌──────────────────────┐ │
│  │  Views   │  │ Components │  │  API Layer (Axios)   │ │
│  │ (页面)   │  │  (组件)    │  │  baseURL: /api       │ │
│  └──────────┘  └────────────┘  └──────────────────────┘ │
│  ┌──────────┐  ┌────────────┐  ┌──────────────────────┐ │
│  │  Router  │  │   Stores   │  │  Utils / Helpers     │ │
│  │ (路由)   │  │  (Pinia)   │  │  (工具函数)          │ │
│  └──────────┘  └────────────┘  └──────────────────────┘ │
└────────────────────┬─────────────────────────────────────┘
                     │
                     │  HTTP JSON 请求
                     ▼
┌──────────────────────────────────────────────────────────┐
│               Spring Boot Application (Port 8080)         │
│  ┌──────────────────────────────────────────────────┐    │
│  │               Controller Layer                    │    │
│  │  @RestController + @RequestMapping               │    │
│  │  职责：接收 HTTP 请求、参数校验、调用 Service      │    │
│  └─────────────────────┬────────────────────────────┘    │
│                        │                                  │
│  ┌─────────────────────▼────────────────────────────┐    │
│  │                Service Layer                       │    │
│  │  @Service                                          │    │
│  │  职责：业务逻辑、事务管理(@Transactional)、数据组装 │    │
│  └─────────────────────┬────────────────────────────┘    │
│                        │                                  │
│  ┌─────────────────────▼────────────────────────────┐    │
│  │                Mapper Layer                        │    │
│  │  MyBatis-Plus BaseMapper<T>                        │    │
│  │  职责：数据库 CRUD、分页查询、条件构造              │    │
│  └─────────────────────┬────────────────────────────┘    │
│                        │                                  │
│  ┌─────────────────────▼────────────────────────────┐    │
│  │              Common / Config                       │    │
│  │  Result<T>：统一响应格式 {code, message, data}    │    │
│  │  CorsConfig：跨域请求配置                          │    │
│  │  MyBatis-Plus：分页插件、乐观锁、逻辑删除          │    │
│  └──────────────────────────────────────────────────┘    │
└────────────────────┬─────────────────────────────────────┘
                     │
                     │  JDBC (MySQL Connector/J)
                     ▼
┌──────────────────────────────────────────────────────────┐
│                   MySQL 8.0 (Port 3306)                   │
│                   Database: sims                          │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌──────────┐      │
│  │  RBAC   │ │ 业务表  │ │ 关联表  │ │  日志表  │      │
│  │5 张表   │ │4 张表   │ │2 张表   │ │  1 张表  │      │
│  └─────────┘ └─────────┘ └─────────┘ └──────────┘      │
└──────────────────────────────────────────────────────────┘
```

### 设计模式

| 模式 | 应用场景 | 说明 |
|------|----------|------|
| **MVC 分层架构** | 整体后端设计 | Controller → Service → Mapper，职责分离，层间单向依赖 |
| **统一响应封装** | `Result<T>` 泛型类 | 所有 API 返回统一 JSON 结构，前端拦截器统一处理 |
| **JDK 动态代理** | MyBatis-Plus Mapper | 自动生成 CRUD 实现，开发者只需定义接口 |
| **代理模式** | Vite 开发代理 | 前端 `/api` 请求自动转发至后端 8080 端口 |
| **RBAC 权限模型** | 用户-角色-权限 | 5 张表实现灵活的角色权限分配 |
| **建造者模式** | LambdaQueryWrapper | 链式调用构建复杂查询条件 |

## 数据库设计

数据库名称：`sims`，字符集 `utf8mb4`，排序规则 `utf8mb4_unicode_ci`，引擎 `InnoDB`。

### 表总览

| 表名 | 类型 | 行数预估 | 说明 |
|------|------|----------|------|
| `sys_permission` | RBAC | ~50 | 系统权限（菜单/按钮/接口） |
| `sys_role` | RBAC | ~10 | 系统角色 |
| `sys_role_permission` | RBAC | ~100 | 角色-权限关联 |
| `sys_user` | RBAC | ~100 | 系统用户 |
| `sys_user_role` | RBAC | ~100 | 用户-角色关联 |
| `class` | 业务 | ~50 | 班级信息 |
| `student` | 业务 | ~10000 | 学生档案 |
| `course` | 业务 | ~200 | 课程信息 |
| `score` | 业务 | ~100000 | 考试成绩 |
| `sys_operation_log` | 审计 | ~1000000 | 操作日志（按月归档） |

### RBAC 权限体系详解

```
                    ┌──────────────────┐
                    │  sys_operation   │
                    │     _log         │  所有操作自动记录
                    │  (操作日志)      │
                    └────────▲─────────┘
                             │ belongs to
              ┌──────────────┴──────────────────┐
              │                                 │
    ┌─────────┴──────────┐          ┌───────────┴─────────┐
    │    sys_user         │          │                     │
    │  (用户/教师/辅导员)  │          │                     │
    └─────────┬──────────┘          │                     │
              │ N:M                 │                     │
    ┌─────────┴──────────┐          │                     │
    │  sys_user_role     │          │                     │
    │  (用户角色关联)     │          │                     │
    └─────────┬──────────┘          │                     │
              │                     │                     │
    ┌─────────┴──────────┐          │                     │
    │    sys_role         │          │                     │
    │  (角色)             │◄─────────┘                     │
    └─────────┬──────────┘                                │
              │ N:M                                        │
    ┌─────────┴──────────┐                                │
    │ sys_role_permission│                                │
    │ (角色权限关联)      │                                │
    └─────────┬──────────┘                                │
              │                                           │
    ┌─────────┴──────────┐                                │
    │  sys_permission    │                                │
    │  (权限/菜单/按钮)   │                                │
    └────────────────────┘                                │
```

**权限粒度设计：**

| 类型 | type 值 | 示例 | 说明 |
|------|---------|------|------|
| 菜单 | 1 | `system`、`student` | 控制侧边栏菜单可见性 |
| 按钮 | 2 | `student:create`、`student:delete` | 控制页面按钮可见/可点击 |
| 接口 | 3 | `student:export` | 控制后端 API 访问权限 |

### 核心业务表详解

**sys_user（系统用户表）**

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT UNSIGNED | PK, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(64) | UNIQUE, NOT NULL | 登录用户名 |
| password | VARCHAR(256) | NOT NULL | BCrypt 加密密码 |
| real_name | VARCHAR(64) | — | 真实姓名 |
| email | VARCHAR(128) | UNIQUE | 邮箱 |
| phone | VARCHAR(20) | — | 手机号 |
| avatar | VARCHAR(256) | — | 头像URL |
| gender | TINYINT | 0=未知/1=男/2=女 | 性别 |
| status | TINYINT | 0=禁用/1=启用 | 账号状态 |

**student（学生表）**

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT UNSIGNED | PK, AUTO_INCREMENT | 学生ID |
| student_no | VARCHAR(32) | UNIQUE, NOT NULL | 学号 |
| name | VARCHAR(64) | NOT NULL, INDEX | 姓名 |
| gender | TINYINT | 0=未知/1=男/2=女 | 性别 |
| birth_date | DATE | — | 出生日期 |
| id_card | VARCHAR(18) | UNIQUE | 身份证号 |
| class_id | BIGINT UNSIGNED | FK → class.id, NOT NULL | 所属班级 |
| enrollment_date | DATE | NOT NULL | 入学日期 |
| status | TINYINT | 0=退学/1=在读/2=毕业/3=休学 | 学籍状态 |

**score（成绩表）**

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT UNSIGNED | PK, AUTO_INCREMENT | 成绩ID |
| student_id | BIGINT UNSIGNED | FK → student.id, NOT NULL | 学生 |
| course_id | BIGINT UNSIGNED | FK → course.id, NOT NULL | 课程 |
| score | DECIMAL(5,1) | CHECK(0-100) | 分数（NULL=未录入） |
| grade_point | DECIMAL(3,1) | — | 绩点 |
| exam_type | TINYINT | 1=期末/2=补考/3=重修 | 考试类型 |
| semester | VARCHAR(32) | NOT NULL | 学期（如 2024-2025-1） |
| recorded_by | BIGINT UNSIGNED | FK → sys_user.id | 录入人 |

**复合唯一约束：** `(student_id, course_id, exam_type, semester)` — 同一学生在同一学期同一课程的同一类型考试只允许一条成绩记录。

### 业务实体关系图

```
┌──────────┐      N:1       ┌──────────┐
│  class   │◄───────────────│ student  │
│ (班级)   │                │ (学生)   │
│          │                │          │
│ grade    │                │ student_ │
│ name     │                │   no     │
│ head_tea │                │ name     │
│  cher_id─┼──┐             │ class_id │
└──────────┘  │             └────┬─────┘
              │                  │
              │  N:1             │ 1:N
              │                  │
              │             ┌────▼─────┐      N:1      ┌──────────┐
              │             │  score   │◄───────────────│  course  │
              │             │ (成绩)   │                │ (课程)   │
              │             │          │                │          │
              │             │ score    │                │ code     │
              │             │ exam_type│                │ name     │
              │             │ semester │                │ credit   │
              │             │ recorded_│                │ hours    │
              │             │  by_id───┼──┐             │ teacher_ │
              │             └──────────┘  │             │  id_id───┼──┐
              │                           │             └──────────┘  │
              │  N:1                      │ N:1                       │ N:1
              │                           │                           │
              ▼                           ▼                           ▼
         ┌─────────────────────────────────────────────────────────────┐
         │                        sys_user                              │
         │                       (系统用户)                              │
         │   username | password(BCrypt) | real_name | status           │
         └─────────────────────────────────────────────────────────────┘
```

**关系说明：**
- `class.head_teacher_id → sys_user.id`：班级的班主任（多对一，可为空）
- `student.class_id → class.id`：学生所属班级（多对一，不可为空）
- `score.student_id → student.id`：成绩归属学生（多对一）
- `score.course_id → course.id`：成绩的课程（多对一）
- `score.recorded_by → sys_user.id`：成绩录入人（多对一）
- `course.teacher_id → sys_user.id`：授课教师（多对一）

## 预置数据

### 角色与权限分配

| 角色 | 编码 | 权限范围 |
|------|------|----------|
| **超级管理员** | ROLE_ADMIN | 全部模块：系统管理、用户管理、角色管理、学生管理、班级管理、课程管理、成绩管理、日志管理 |
| **教师** | ROLE_TEACHER | 学生（查看/新增/编辑/导出）、课程（查看/新增/编辑）、成绩（查看/录入/编辑/导出） |
| **辅导员** | ROLE_COUNSELOR | 学生（查看/导出）、班级（查看/新增/编辑） |

### 默认账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| `admin` | `admin123` | 超级管理员 | 系统初始管理员，**部署后请立即修改密码** |

### 默认权限树

```
├── 系统管理 (system)
│   ├── 用户管理 (system:user)
│   │   ├── 查看用户 (system:user:list)
│   │   ├── 新增用户 (system:user:create)
│   │   ├── 编辑用户 (system:user:update)
│   │   └── 删除用户 (system:user:delete)
│   └── 角色管理 (system:role)
│       ├── 查看角色 (system:role:list)
│       ├── 新增角色 (system:role:create)
│       ├── 编辑角色 (system:role:update)
│       └── 删除角色 (system:role:delete)
├── 学生管理 (student)
│   ├── 查看学生 (student:list)
│   ├── 新增学生 (student:create)
│   ├── 编辑学生 (student:update)
│   ├── 删除学生 (student:delete)
│   └── 导出学生 (student:export)
├── 班级管理 (class)
│   ├── 查看班级 (class:list)
│   ├── 新增班级 (class:create)
│   ├── 编辑班级 (class:update)
│   └── 删除班级 (class:delete)
├── 课程管理 (course)
│   ├── 查看课程 (course:list)
│   ├── 新增课程 (course:create)
│   ├── 编辑课程 (course:update)
│   └── 删除课程 (course:delete)
├── 成绩管理 (score)
│   ├── 查看成绩 (score:list)
│   ├── 录入成绩 (score:create)
│   ├── 编辑成绩 (score:update)
│   └── 导出成绩 (score:export)
└── 日志管理 (log)
    └── 查看日志 (log:list)
```

## 安全设计

| 安全机制 | 实现方式 | 说明 |
|----------|----------|------|
| 密码加密 | BCryptPasswordEncoder | 数据库中仅存储加密哈希，不可逆 |
| SQL 注入防护 | MyBatis-Plus 参数化查询 | `${}` 占位符？项目中统一使用 `#{}` |
| CORS 跨域控制 | CorsConfig.java | 生产环境应限制为具体域名而非 `*` |
| RBAC 权限控制 | 5 张表实现角色-菜单-按钮-接口级控制 | 前端路由守卫 + 后端注解拦截 |
| 操作审计 | sys_operation_log 表 | 全量记录用户行为，包含 IP、参数、耗时 |
| XSS 防护 | Vue 3 默认 HTML 转义 | 配合 Element Plus 安全渲染 |

## 快速开始

### 环境要求

| 软件 | 最低版本 | 推荐版本 | 说明 |
|------|----------|----------|------|
| JDK | 17 | 17 LTS / 21 LTS | Java 开发工具包 |
| Maven | 3.8 | 3.9+ | 后端构建与依赖管理 |
| MySQL | 8.0 | 8.0.33+ | 数据库服务 |
| Node.js | 18 | 20 LTS | 前端运行时 |
| npm | 9 | 10+ | 前端包管理器 |

### 1. 初始化数据库

```bash
# 方式一：命令行导入
mysql -u root -p < database/schema.sql

# 方式二：登录 MySQL 后导入
mysql -u root -p
mysql> source database/schema.sql;
```

执行后将自动完成：
- 创建 `sims` 数据库
- 建立 10 张业务表及所有索引、外键约束
- 插入 34 条默认权限、3 个角色及角色-权限关联
- 创建默认管理员账号

验证导入：
```bash
mysql -u root -p -e "USE sims; SHOW TABLES; SELECT COUNT(*) FROM sys_user;"
```

### 2. 配置后端

编辑 `backend/src/main/resources/application.yml`：

```yaml
server:
  port: 8080                         # 后端监听端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sims?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root                   # 修改为实际 MySQL 用户名
    password: root                   # 修改为实际 MySQL 密码
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true   # 下划线转驼峰（student_no → studentNo）
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 开发环境打印 SQL
```

### 3. 启动后端

```bash
cd backend

# 开发模式运行（热重载需配合 spring-boot-devtools）
mvn spring-boot:run

# 或者先编译再运行
mvn clean package -DskipTests
java -jar target/student-system-1.0.0.jar
```

验证后端：
```bash
curl http://localhost:8080/api/health
# {"code":200,"message":"success","data":{"status":"UP","service":"Student Management System"}}
```

### 4. 启动前端

```bash
cd frontend

# 安装依赖（首次运行）
npm install

# 开发模式运行
npm run dev
```

浏览器访问 `http://localhost:3000`，页面将自动调用健康检查接口展示后端连接状态。

### 5. 生产构建与部署

```bash
# 前端构建
cd frontend
npm run build                # 产物输出至 dist/ 目录

# 后端打包
cd backend
mvn clean package -DskipTests
# 产物：target/student-system-1.0.0.jar
```

**部署建议：**
- 将前端 `dist/` 目录部署至 Nginx，配置反向代理 `/api` 到后端 8080 端口
- 后端 `jar` 通过 `nohup java -jar` 或 systemd 管理
- 生产环境关闭 MyBatis-Plus SQL 日志输出：`log-impl` 改为 `org.apache.ibatis.logging.nologging.NoLoggingImpl`
- 限制 CORS 来源为具体域名，移除 `addAllowedOriginPattern("*")`

### Nginx 配置示例

```nginx
server {
    listen       80;
    server_name  localhost;

    # 前端静态资源
    location / {
        root   /usr/share/nginx/html;
        index  index.html;
        try_files $uri $uri/ /index.html;  # Vue History 模式路由
    }

    # API 反向代理到后端
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

## API 接口

### 当前可用接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/health` | 服务健康检查 | 无需认证 |

### 统一响应格式

所有 API 返回统一 JSON 结构：

```json
// 成功响应
{
  "code": 200,
  "message": "success",
  "data": {
    // 业务数据
  }
}

// 错误响应
{
  "code": 400,
  "message": "参数校验失败：学号不能为空",
  "data": null
}
```

**HTTP 状态码约定：**

| code | 含义 | 说明 |
|------|------|------|
| 200 | 成功 | 请求处理成功 |
| 400 | 参数错误 | 客户端请求参数不合法 |
| 401 | 未认证 | 未登录或 Token 过期 |
| 403 | 无权限 | 当前角色无此操作权限 |
| 404 | 未找到 | 请求的资源不存在 |
| 500 | 服务器错误 | 后端异常 |

### 规划中的 API 模块

| 模块 | 基础路径 | 预计接口数 | 状态 |
|------|----------|------------|------|
| 认证登录 | `/api/auth` | 3 | 待开发 |
| 用户管理 | `/api/users` | 5 | 待开发 |
| 角色管理 | `/api/roles` | 5 | 待开发 |
| 学生管理 | `/api/students` | 6 | 待开发 |
| 班级管理 | `/api/classes` | 5 | 待开发 |
| 课程管理 | `/api/courses` | 5 | 待开发 |
| 成绩管理 | `/api/scores` | 5 | 待开发 |
| 日志管理 | `/api/logs` | 2 | 待开发 |
| 数据统计 | `/api/statistics` | 4 | 待开发 |

## 前端设计

### 组件树（规划）

```
App.vue
├── LayoutHeader.vue            # 顶部导航栏（Logo、用户信息、退出登录）
├── LayoutSidebar.vue           # 侧边栏菜单（根据权限动态渲染）
├── LayoutMain.vue              # 主内容区
│   └── <router-view />
│       ├── LoginView.vue           # 登录页
│       ├── DashboardView.vue       # 仪表盘（统计概览）
│       ├── StudentListView.vue     # 学生列表
│       │   └── StudentDialog.vue   # 学生新增/编辑弹窗
│       ├── ClassManageView.vue     # 班级管理
│       ├── CourseManageView.vue    # 课程管理
│       ├── ScoreListView.vue       # 成绩列表
│       │   └── ScoreImport.vue     # 成绩批量导入
│       ├── UserManageView.vue      # 用户管理（管理员）
│       ├── RoleManageView.vue      # 角色权限配置（管理员）
│       └── LogViewerView.vue       # 操作日志查看（管理员）
```

### 前端开发规范

- **组件命名**：PascalCase 多单词（如 `StudentList.vue`），ESLint 已关闭单单词检查
- **API 封装**：每个业务模块独立 API 文件（`api/student.ts`），统一使用 `request.ts` 实例
- **状态管理**：跨组件共享状态使用 Pinia Store（如当前用户信息、权限列表）
- **样式方案**：Element Plus 内置样式 + scoped 局部样式
- **代码格式化**：无分号、单引号、2 空格缩进、尾逗号（ES5 规则）、printWidth=100

## 开发计划

### 当前进度

| 模块 | 后端 | 前端 | 状态 |
|------|------|------|------|
| 项目骨架 | ✅ | ✅ | 已完成 |
| 数据库设计 | ✅ | — | 已完成 |
| 健康检查 | ✅ | ✅ | 已完成 |
| 认证登录 | ⬜ | ⬜ | 待开发 |
| 用户管理 | ⬜ | ⬜ | 待开发 |
| 学生管理 | ⬜ | ⬜ | 待开发 |
| 班级管理 | ⬜ | ⬜ | 待开发 |
| 课程管理 | ⬜ | ⬜ | 待开发 |
| 成绩管理 | ⬜ | ⬜ | 待开发 |
| 日志管理 | ⬜ | ⬜ | 待开发 |
| 数据统计 | ⬜ | ⬜ | 待开发 |

### 推荐开发顺序

1. **认证模块**：实现 Spring Security + JWT 登录，前后端打通认证流程
2. **学生管理**：CRUD + 分页查询 + Excel 导出，是最核心的业务模块
3. **班级管理**：关联学生，为后续模块打基础
4. **课程管理**：关联教师，为成绩模块准备数据
5. **成绩管理**：关联学生+课程，实现成绩录入与统计
6. **用户与角色管理**：完善 RBAC 权限体系，前后端权限控制
7. **数据统计**：仪表盘图表展示（学生分布、成绩趋势等）

## 常见问题

**Q: 前端启动后页面空白？**
A: 确认后端已启动且 `http://localhost:8080/api/health` 可访问。Vite 代理依赖后端运行。

**Q: 数据库连接失败？**
A: 检查 MySQL 服务是否启动、`application.yml` 中用户名密码是否正确、数据库 `sims` 是否已创建。

**Q: Maven 依赖下载慢？**
A: 在 `pom.xml` 同级目录创建 `.mvn/maven.config` 或在 `~/.m2/settings.xml` 中配置阿里云镜像：
```xml
<mirror>
  <id>aliyun</id>
  <mirrorOf>central</mirrorOf>
  <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

**Q: npm install 报错？**
A: 确认 Node.js 版本 ≥ 18，可尝试 `npm config set registry https://registry.npmmirror.com` 切换国内镜像。

**Q: 如何新增一个角色？**
A: 在 `sys_role` 表插入角色，在 `sys_role_permission` 表配置其权限关联即可；管理界面开发中。

## 技术选型理由

| 选择 | 理由 |
|------|------|
| Spring Boot 3.2 | 最新稳定版，原生支持 GraalVM、虚拟线程，Java 17 基线 |
| MyBatis-Plus | 国产 ORM 首选，Lambda 条件构造器、分页插件、代码生成器开箱即用 |
| MySQL 8.0 | 行业标准 RDBMS，Window Functions、CTE、JSON 支持完善 |
| Vue 3 Composition API | 更好的逻辑复用与 TypeScript 支持，配合 `<script setup>` 语法糖 |
| Element Plus | 国内最成熟的 Vue 3 企业级组件库，与管理系统场景高度契合 |
| TypeScript | 静态类型检查减少运行时错误，提升大型项目可维护性 |
| Vite | 相比 Webpack 启动速度提升 10x+，HMR 即时生效 |

## License

本项目基于 [MIT License](./LICENSE) 开源。