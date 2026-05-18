# 学生管理系统 (Student Management System)

一个基于前后端分离架构的综合性学生信息管理系统（SIMS），支持学生管理、班级管理、课程管理、成绩管理、用户权限管理及操作日志审计等功能。

## 功能特性

- **学生管理**：学生信息的增删改查、学号自动生成、批量导入导出（Excel）、学籍异动管理
- **班级管理**：班级创建与维护、班主任指派、年级管理
- **课程管理**：课程开设、学分与学时管理、授课教师分配
- **选课管理**：学生在线选课与退选、课程容量控制、教师查看选课名单
- **课表查询**：学生个人课表（周视图）、教师授课课表
- **排课管理**：课程排课、时间冲突检测、教室冲突检测
- **成绩管理**：成绩录入与编辑（含修改记录）、绩点自动计算（4.0/5.0 双算法）、补考与重修标记、成绩统计分析、成绩单 PDF 导出
- **RBAC 权限体系**：基于 Apache Shiro + JWT 的角色-权限访问控制，菜单/按钮/接口三级权限粒度，前端路由守卫 + 后端注解拦截
- **操作日志审计**：全量记录用户操作行为，支持按模块、操作类型、时间范围检索
- **数据看板**：仪表盘统计概览（学生总数、教师总数、课程总数、成绩分布图表）
- **系统配置**：学期起止日期、成绩录入截止时间、选课开放时段等参数配置
- **数据安全**：BCrypt 密码加密、JWT 无状态认证、登录失败限流（Redis）、Token 黑名单、CORS 跨域防护
- **一键部署**：Docker Compose 编排（MySQL + Redis + 后端 + 前端 + Nginx）

## 技术栈

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 | 快速开发脚手架，自动配置 |
| ORM | MyBatis-Plus | 3.5.6 | 增强版 MyBatis，简化 CRUD |
| 安全框架 | Apache Shiro | 2.0 | 轻量级 RBAC 权限控制 |
| JWT | JJWT | 0.12.5 | 无状态认证令牌 |
| 数据库 | MySQL | 8.0+ | 关系型数据库，InnoDB 引擎 |
| 缓存 | Redis | 7.0+ | Session/Token 黑名单/登录限流 |
| 连接池 | Druid | 1.2.22 | 高性能数据库连接池 |
| 数据库迁移 | Flyway | 10.x | SQL 版本管理，自动迁移 |
| JDK | Java | 17 | LTS 长期支持版本 |
| 构建工具 | Maven | 3.8+ | 依赖管理与项目构建 |
| 容器化 | Docker + Compose | — | 一键部署编排 |
| 前端框架 | Vue 3 | 3.4 | Composition API + setup 语法糖 |
| 构建工具 | Vite | 5.2 | 极速开发体验，ESBuild 预构建 |
| UI 组件库 | Element Plus | 2.7 | 企业级 Vue 3 组件库 |
| 状态管理 | Pinia | 2.1 | Vue 3 官方状态管理 |
| 路由 | Vue Router | 4.3 | SPA 路由 + 权限守卫 |
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
│       │   ├── common/                        # 通用模块（统一响应、异常处理、错误码）
│       │   │   ├── Result.java                # 统一 API 响应体 {code, message, data}
│       │   │   ├── ResultCode.java             # 业务错误码枚举（1xxx~6xxx）
│       │   │   ├── BusinessException.java      # 业务异常类
│       │   │   └── GlobalExceptionHandler.java # 全局异常处理器（10 种异常类型）
│       │   ├── config/                        # 配置模块（Shiro、JWT、CORS、分页、Redis）
│       │   │   ├── ShiroConfig.java           # Shiro + JWT Filter 链配置
│       │   │   ├── JwtRealm.java              # JWT Realm（认证 + 授权）
│       │   │   ├── JwtToken.java              # Shiro JWT Token 包装
│       │   │   ├── JwtFilter.java             # Bearer Token 提取 + 黑名单检查
│       │   │   ├── CorsConfig.java            # 跨域请求配置
│       │   │   ├── MybatisPlusConfig.java     # 分页插件配置
│       │   │   ├── MyMetaObjectHandler.java   # 自动填充 created_at/updated_at
│       │   │   ├── PasswordEncoderConfig.java # BCrypt 密码编码器
│       │   │   └── RedisConfig.java           # Redis String 序列化配置
│       │   ├── controller/                    # 控制器层（11 个 Controller）
│       │   │   ├── AuthController.java        # 注册/登录/刷新Token/登出/当前用户
│       │   │   ├── UserController.java        # 用户 CRUD + 启用禁用 + 重置密码
│       │   │   ├── RoleController.java        # 角色列表
│       │   │   ├── PermissionController.java  # 权限树
│       │   │   ├── StudentController.java     # 学生 CRUD
│       │   │   ├── StudentImportExportController.java  # Excel 导入导出
│       │   │   ├── ClassController.java       # 班级 CRUD
│       │   │   ├── CourseController.java      # 课程 CRUD
│       │   │   ├── EnrollmentChangeController.java  # 学籍异动
│       │   │   ├── ScoreController.java       # 成绩录入/查询/统计/GPA
│       │   │   └── HealthController.java      # 健康检查
│       │   ├── service/                       # 业务逻辑层（Service 接口 + Impl 实现）
│       │   ├── mapper/                        # 数据访问层（MyBatis-Plus Mapper）
│       │   ├── entity/                        # 实体类（SysUser, Student, ClassInfo, Course, Score 等）
│       │   ├── dto/                           # 数据传输对象（LoginDTO, RegisterDTO, UserVO 等）
│       │   └── util/                          # 工具类
│       │       └── JwtUtil.java               # JWT 签发/校验/解析工具
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
│       │   ├── request.ts                     # Axios 实例（拦截器、错误处理、Token 注入）
│       │   ├── auth.ts                        # 认证 API
│       │   ├── user.ts                        # 用户管理 API
│       │   ├── student.ts                     # 学生管理 API
│       │   ├── class.ts                       # 班级管理 API
│       │   ├── course.ts                      # 课程管理 API
│       │   └── score.ts                       # 成绩管理 API
│       ├── components/                        # 公共组件
│       ├── views/                             # 页面组件
│       │   ├── Login.vue                      # 登录页
│       │   ├── Home.vue                       # 首页仪表盘
│       │   ├── NotFound.vue                   # 404 页面
│       │   ├── layout/MainLayout.vue          # 主布局（侧边菜单 + 权限过滤）
│       │   ├── sys/User.vue                   # 用户管理（CRUD + 启用禁用 + 重置密码）
│       │   ├── sys/Role.vue                   # 角色管理
│       │   ├── student/List.vue               # 学生管理（CRUD + 导入导出）
│       │   ├── student/Class.vue              # 班级管理
│       │   ├── student/Enrollment.vue         # 学籍异动管理
│       │   ├── course/List.vue                # 课程管理（选课/退选/课表）
│       │   └── course/Score.vue               # 成绩管理（录入/统计/成绩单/GPA）
│       ├── router/index.ts                    # 路由配置（登录守卫 + 权限守卫）
│       ├── stores/                            # Pinia 状态管理
│       │   ├── user.ts                        # 用户 Store（Token/用户信息/登出）
│       │   └── permission.ts                  # 权限 Store（加载/检查）
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
| RBAC 权限控制 | 5 张表 + Apache Shiro + JWT | 前端路由守卫 + 后端 `@RequiresPermissions` 注解拦截 |
| 登录限流 | Redis 计数器 | 5 次失败锁定 30 分钟 |
| Token 黑名单 | Redis TTL 管理 | 登出后 Token 立即失效 |
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

### 1. 准备工作

```bash
# 确保 MySQL 8.0+ 和 Redis 已启动
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS sims DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 2. 一键部署（推荐）

```bash
# 使用 Docker Compose 一键启动所有服务
docker-compose up -d
```

服务将自动启动：
- MySQL 8.0 + 自动建库
- Redis 7
- 后端 Spring Boot（8080 端口）
- 前端 Nginx（80 端口）

Flyway 会在后端启动时自动执行数据库迁移脚本，无需手动导入 SQL。

### 3. 手动启动（开发模式）

#### 初始化数据库

Flyway 会在后端启动时自动执行 `backend/src/main/resources/db/migration/` 下的迁移脚本：

- `V1__init_schema.sql` — 创建 11 张表 + 索引 + 外键约束 + 种子数据（权限/角色/管理员账号）

#### 配置后端

#### 配置后端

编辑 `backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sims?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root                   # 修改为实际 MySQL 用户名
    password: root                   # 修改为实际 MySQL 密码
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost                  # Redis 服务器地址
    port: 6379
  flyway:
    enabled: true                    # 自动执行数据库迁移
    locations: classpath:db/migration

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true   # 下划线转驼峰
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 开发环境打印 SQL
```

#### 启动后端

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
# 健康检查
curl http://localhost:8080/api/health
# {"code":200,"message":"操作成功","data":{"status":"UP","service":"Student Management System"}}

# 管理员登录获取 Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
# {"code":200,"message":"操作成功","data":{"accessToken":"eyJ...","refreshToken":"eyJ..."}}
```

#### 启动前端

```bash
cd frontend

# 安装依赖（首次运行）
npm install

# 开发模式运行
npm run dev
```

浏览器访问 `http://localhost:3000`，页面将自动调用健康检查接口展示后端连接状态。

### 4. 生产构建与部署

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

### API 接口总览

#### 认证模块 (`/api/auth`)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/register` | 用户注册 | 无需认证 |
| POST | `/api/auth/login` | 用户登录 → 返回 accessToken + refreshToken | 无需认证 |
| POST | `/api/auth/refresh` | 刷新 access token | 无需认证 |
| POST | `/api/auth/logout` | 注销（token 加入黑名单） | JWT |
| GET | `/api/auth/me` | 获取当前用户信息 | JWT |

#### 用户管理 (`/api/users`)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/users?page=&size=&username=&status=` | 分页查询用户 | `sys:user:list` |
| GET | `/api/users/{id}` | 查看用户详情 | `sys:user:list` |
| POST | `/api/users` | 创建用户 | `sys:user:create` |
| PUT | `/api/users/{id}` | 编辑用户 | `sys:user:update` |
| PUT | `/api/users/{id}/status` | 启用/禁用 | `sys:user:update` |
| PUT | `/api/users/{id}/password` | 重置密码 | `sys:user:update` |
| DELETE | `/api/users/{id}` | 删除用户 | `sys:user:delete` |

#### 学生管理 (`/api/students`)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/students?page=&size=&keyword=&classId=&status=` | 分页查询学生 | `student:list` |
| GET | `/api/students/{id}` | 查看学生详情 | `student:list` |
| POST | `/api/students` | 创建学生 | `student:create` |
| PUT | `/api/students/{id}` | 编辑学生 | `student:update` |
| DELETE | `/api/students/{id}` | 删除学生 | `student:delete` |
| POST | `/api/students/import` | Excel 批量导入 | `student:create` |
| GET | `/api/students/export` | Excel 导出 | `student:export` |
| GET | `/api/students/template` | 下载导入模板 | `student:create` |

#### 班级管理 (`/api/classes`)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/classes?page=&size=&keyword=&grade=` | 分页查询班级 | `class:list` |
| GET | `/api/classes/{id}` | 查看班级详情 | `class:list` |
| POST | `/api/classes` | 创建班级 | `class:create` |
| PUT | `/api/classes/{id}` | 编辑班级 | `class:update` |
| DELETE | `/api/classes/{id}` | 删除班级 | `class:delete` |

#### 课程管理 (`/api/courses`)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/courses?page=&size=&keyword=&type=` | 分页查询课程 | `course:list` |
| GET | `/api/courses/{id}` | 查看课程详情 | `course:list` |
| POST | `/api/courses` | 创建课程 | `course:create` |
| PUT | `/api/courses/{id}` | 编辑课程 | `course:update` |
| DELETE | `/api/courses/{id}` | 删除课程 | `course:delete` |
| POST | `/api/courses/{id}/enroll` | 学生选课 | 认证用户 |
| DELETE | `/api/courses/{id}/enroll` | 学生退选 | 认证用户 |
| GET | `/api/courses/schedule` | 查询个人课表（周视图） | 认证用户 |
| POST | `/api/courses/arrange` | 课程排课 | `course:arrange` |

#### 成绩管理 (`/api/scores`)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/scores?page=&size=&studentId=&courseId=&semester=` | 查询成绩 | `score:list` |
| POST | `/api/scores` | 录入成绩 | `score:create` |
| PUT | `/api/scores/{id}` | 修改成绩（记录修改日志） | `score:update` |
| GET | `/api/scores/statistics` | 成绩统计（平均分/及格率/分布） | `score:list` |
| GET | `/api/scores/report` | 生成成绩单（PDF） | `score:list` |
| GET | `/api/scores/gpa` | 计算 GPA（4.0/5.0 双算法） | `score:list` |

#### 学籍异动 (`/api/enrollment-changes`)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/enrollment-changes?studentId=&page=&size=` | 查询异动记录 | `student:list` |
| POST | `/api/enrollment-changes` | 记录学籍异动 | `student:update` |

#### 角色与权限 (`/api/roles`, `/api/permissions`)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/roles` | 角色列表 | `sys:role:list` |
| GET | `/api/permissions` | 权限树 | `sys:role:list` |

#### 系统运维 (`/api/system`)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/logs?page=&size=&module=&operator=&startTime=&endTime=` | 操作日志查询 | `log:list` |
| GET | `/api/dashboard` | 数据看板统计 | `system:manage` |
| GET | `/api/system/config` | 查询系统配置 | `system:manage` |
| PUT | `/api/system/config` | 更新系统配置 | `system:manage` |
| GET | `/api/health` | 服务健康检查 | 无需认证 |

### 统一响应格式

所有 API 返回统一 JSON 结构：

```json
// 成功响应
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}

// 业务错误响应
{
  "code": 1002,
  "message": "用户名已存在",
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

**业务错误码分段：**

| 范围 | 模块 |
|------|------|
| 1xxx | 用户模块（用户不存在/用户名已存在/密码错误等） |
| 2xxx | 学生模块（学生不存在/学号已存在） |
| 3xxx | 班级模块 |
| 4xxx | 课程模块 |
| 5xxx | 成绩模块 |
| 6xxx | 文件操作 |

## 前端设计

### 组件树

```
App.vue
├── Login.vue                            # 登录页
├── MainLayout.vue                       # 主布局（顶部导航 + 侧边栏 + 内容区）
│   ├── Sidebar (权限菜单过滤)             # 侧边栏菜单（根据权限动态渲染）
│   └── Main Content (<router-view />)
│       ├── Home.vue                     # 仪表盘（统计概览卡片）
│       ├── sys/User.vue                 # 用户管理（分页表格 + CRUD 弹窗 + 状态切换 + 重置密码）
│       ├── sys/Role.vue                 # 角色管理
│       ├── student/List.vue             # 学生管理（CRUD + 搜索 + 导入导出按钮）
│       ├── student/Class.vue            # 班级管理
│       ├── student/Enrollment.vue       # 学籍异动管理（时间线展示）
│       ├── course/List.vue              # 课程管理（选课/退选/课表/排课）
│       └── course/Score.vue             # 成绩管理（录入/统计图表/成绩单/GPA）
└── NotFound.vue                         # 404 页面
```

### 前端开发规范

- **组件命名**：PascalCase 多单词（如 `StudentList.vue`），ESLint 已关闭单单词检查
- **API 封装**：每个业务模块独立 API 文件（`api/student.ts`），统一使用 `request.ts` 实例
- **状态管理**：跨组件共享状态使用 Pinia Store（如当前用户信息、权限列表）
- **样式方案**：Element Plus 内置样式 + scoped 局部样式
- **代码格式化**：无分号、单引号、2 空格缩进、尾逗号（ES5 规则）、printWidth=100

## 开发计划与进度

### 总体进度：21/21 子任务代码开发完成 ✅

> 全部任务代码已合并至 main 分支，一期已验收通过，二期和三期处于审核中状态。

### 一期 MVP — 核心基础（11/11 ✅ 已完成）

| 序号 | 任务 | 模块 | 状态 |
|:----|:-----|:-----|:----:|
| 1 | 项目脚手架搭建 | 系统基础 | ✅ done |
| 2 | 数据库设计（11 张表 + 索引 + 种子数据） | 系统基础 | ✅ done |
| 3 | 统一响应与异常处理 | 系统基础 | ✅ done |
| 4 | 用户注册与登录（Shiro + JWT + BCrypt + 登录限流） | 用户权限 | ✅ done |
| 5 | RBAC 角色权限管理（前后端双重鉴权） | 用户权限 | ✅ done |
| 6 | 用户管理 CRUD（分页 + 启用禁用 + 重置密码） | 用户权限 | ✅ done |
| 7 | 学生信息 CRUD | 学生管理 | ✅ done |
| 8 | 班级管理 CRUD | 学生管理 | ✅ done |
| 9 | 学生信息批量导入导出（Excel） | 学生管理 | ✅ done |
| 10 | 学籍异动管理 | 学生管理 | ✅ done |
| 11 | 课程信息 CRUD | 课程管理 | ✅ done |

### 二期 — 选课 + 成绩（6/6 ✅ 代码完成，待审核）

| 序号 | 任务 | 模块 | 状态 |
|:----|:-----|:-----|:----:|
| 12 | 学生选课（选课/退选 + 容量控制） | 选课管理 | 🔍 in_review |
| 13 | 课表查询（周视图） | 选课管理 | 🔍 in_review |
| 14 | 成绩录入与修改（含修改日志） | 成绩管理 | 🔍 in_review |
| 15 | 成绩查询与统计（图表展示） | 成绩管理 | 🔍 in_review |
| 16 | 成绩单生成（PDF 导出） | 成绩管理 | 🔍 in_review |
| 17 | GPA 计算（4.0/5.0 双算法） | 成绩管理 | 🔍 in_review |

### 三期 — 排课 + 运维（4/4 ✅ 代码完成，待审核）

| 序号 | 任务 | 模块 | 状态 |
|:----|:-----|:-----|:----:|
| 18 | 课程安排（时间/教室冲突检测） | 排课管理 | 🔍 in_review |
| 19 | 操作日志（全量审计 + 查询） | 系统运维 | 🔍 in_review |
| 20 | 数据看板（仪表盘统计） | 系统运维 | 🔍 in_review |
| 21 | 系统配置（学期/选课时段等参数） | 系统运维 | 🔍 in_review |

### 基础设施增强（已完成）

| 任务 | 内容 | 状态 |
|:-----|:-----|:----:|
| Flyway 数据库脚本管理 | SQL 版本迁移管理 | ✅ done |
| Docker 一键部署 | Docker Compose 编排（MySQL + Redis + 后端 + 前端 + Nginx） | ✅ done |
| README 完善 | 项目文档与启动指南 | ✅ done |

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
A: 使用管理员账号登录，在「系统管理 → 角色管理」页面中创建角色并配置权限关联。

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