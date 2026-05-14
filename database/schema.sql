-- ============================================================
-- 学生信息管理系统 (SIMS) — MySQL 8.0 数据库初始化脚本
-- ============================================================
--
-- ER 图 (Mermaid 语法，可在支持 Mermaid 的编辑器中预览):
--
-- erDiagram
--   sys_permission ||--o{ sys_role_permission : "1:N"
--   sys_role        ||--o{ sys_role_permission : "1:N"
--   sys_role        ||--o{ sys_user_role      : "1:N"
--   sys_user        ||--o{ sys_user_role      : "1:N"
--   sys_user        ||--o{ sys_operation_log  : "1:N"
--   sys_user        ||--o{ class              : "head_teacher"
--   sys_user        ||--o{ course             : "teacher"
--   sys_user        ||--o{ score              : "recorded_by"
--   class           ||--o{ student            : "1:N"
--   student         ||--o{ score              : "1:N"
--   course          ||--o{ score              : "1:N"
--
--   关系说明:
--   - sys_user ↔ sys_role:       多对多 (通过 sys_user_role)
--   - sys_role ↔ sys_permission: 多对多 (通过 sys_role_permission)
--   - class → sys_user:          多对一 (班主任, head_teacher_id)
--   - student → class:           多对一 (所属班级, class_id)
--   - score → student:           多对一 (学生成绩)
--   - score → course:            多对一 (课程成绩)
--   - score → sys_user:          多对一 (录入人, recorded_by)
--   - course → sys_user:         多对一 (授课教师, teacher_id)
--   - sys_operation_log → sys_user: 多对一 (操作人)
--

CREATE DATABASE IF NOT EXISTS `sims`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `sims`;

-- ============================================================
-- 1. 权限表
-- ============================================================
CREATE TABLE `sys_permission` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id`     BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父权限ID，0=顶级',
  `name`          VARCHAR(64)     NOT NULL COMMENT '权限名称',
  `code`          VARCHAR(128)    NOT NULL COMMENT '权限标识符（如 sys:user:create）',
  `type`          TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '类型：1=菜单 2=按钮 3=接口',
  `path`          VARCHAR(256)    DEFAULT NULL COMMENT '前端路由',
  `icon`          VARCHAR(64)     DEFAULT NULL COMMENT '菜单图标',
  `sort_order`    INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '同级排序',
  `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_code` (`code`),
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限表';

-- ============================================================
-- 2. 角色表
-- ============================================================
CREATE TABLE `sys_role` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name`          VARCHAR(64)     NOT NULL COMMENT '角色名称',
  `code`          VARCHAR(64)     NOT NULL COMMENT '角色编码（如 ROLE_ADMIN）',
  `description`   VARCHAR(256)    DEFAULT NULL COMMENT '角色描述',
  `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- ============================================================
-- 3. 角色-权限关联表
-- ============================================================
CREATE TABLE `sys_role_permission` (
  `role_id`       BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`role_id`, `permission_id`),
  CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ============================================================
-- 4. 用户表
-- ============================================================
CREATE TABLE `sys_user` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`      VARCHAR(64)     NOT NULL COMMENT '用户名',
  `password`      VARCHAR(256)    NOT NULL COMMENT '加密后的密码',
  `real_name`     VARCHAR(64)     DEFAULT NULL COMMENT '真实姓名',
  `email`         VARCHAR(128)    DEFAULT NULL COMMENT '邮箱',
  `phone`         VARCHAR(20)     DEFAULT NULL COMMENT '手机号',
  `avatar`        VARCHAR(256)    DEFAULT NULL COMMENT '头像URL',
  `gender`        TINYINT UNSIGNED DEFAULT 0 COMMENT '性别：0=未知 1=男 2=女',
  `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
  `last_login_at` DATETIME        DEFAULT NULL COMMENT '最后登录时间',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_username` (`username`),
  UNIQUE INDEX `uk_email` (`email`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- ============================================================
-- 5. 用户-角色关联表
-- ============================================================
CREATE TABLE `sys_user_role` (
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`),
  CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ============================================================
-- 6. 班级表
-- ============================================================
CREATE TABLE `class` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `name`          VARCHAR(64)     NOT NULL COMMENT '班级名称（如 2024级计算机科学1班）',
  `grade`         INT UNSIGNED    NOT NULL COMMENT '入学年份（如 2024）',
  `description`   VARCHAR(256)    DEFAULT NULL COMMENT '班级描述',
  `head_teacher_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '班主任用户ID',
  `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0=已毕业 1=在读',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_name` (`name`),
  INDEX `idx_grade` (`grade`),
  INDEX `idx_head_teacher_id` (`head_teacher_id`),
  CONSTRAINT `fk_class_head_teacher` FOREIGN KEY (`head_teacher_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- ============================================================
-- 7. 学生表
-- ============================================================
CREATE TABLE `student` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `student_no`    VARCHAR(32)     NOT NULL COMMENT '学号',
  `name`          VARCHAR(64)     NOT NULL COMMENT '姓名',
  `gender`        TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '性别：0=未知 1=男 2=女',
  `birth_date`    DATE            DEFAULT NULL COMMENT '出生日期',
  `id_card`       VARCHAR(18)     DEFAULT NULL COMMENT '身份证号',
  `phone`         VARCHAR(20)     DEFAULT NULL COMMENT '联系电话',
  `email`         VARCHAR(128)    DEFAULT NULL COMMENT '邮箱',
  `address`       VARCHAR(256)    DEFAULT NULL COMMENT '家庭地址',
  `class_id`      BIGINT UNSIGNED NOT NULL COMMENT '所属班级ID',
  `enrollment_date` DATE          NOT NULL COMMENT '入学日期',
  `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0=退学 1=在读 2=毕业 3=休学',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_student_no` (`student_no`),
  UNIQUE INDEX `uk_id_card` (`id_card`),
  INDEX `idx_name` (`name`),
  INDEX `idx_class_id` (`class_id`),
  INDEX `idx_status` (`status`),
  CONSTRAINT `fk_student_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

-- ============================================================
-- 8. 课程表
-- ============================================================
CREATE TABLE `course` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `code`          VARCHAR(32)     NOT NULL COMMENT '课程编码（如 CS101）',
  `name`          VARCHAR(128)    NOT NULL COMMENT '课程名称',
  `credit`        DECIMAL(3,1)    NOT NULL COMMENT '学分',
  `hours`         INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '总学时',
  `teacher_id`    BIGINT UNSIGNED DEFAULT NULL COMMENT '授课教师用户ID',
  `semester`      VARCHAR(32)     DEFAULT NULL COMMENT '开课学期（如 2024-2025-1）',
  `description`   VARCHAR(512)    DEFAULT NULL COMMENT '课程描述',
  `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0=停用 1=启用',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_code` (`code`),
  INDEX `idx_name` (`name`),
  INDEX `idx_teacher_id` (`teacher_id`),
  INDEX `idx_semester` (`semester`),
  CONSTRAINT `fk_course_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- ============================================================
-- 9. 成绩表
-- ============================================================
CREATE TABLE `score` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '成绩ID',
  `student_id`    BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
  `course_id`     BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
  `score`         DECIMAL(5,1)    DEFAULT NULL COMMENT '分数（可为NULL表示未录入）',
  `grade_point`   DECIMAL(3,1)    DEFAULT NULL COMMENT '绩点',
  `exam_type`     TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '考试类型：1=期末考试 2=补考 3=重修',
  `semester`      VARCHAR(32)     NOT NULL COMMENT '学期（如 2024-2025-1）',
  `remark`        VARCHAR(256)    DEFAULT NULL COMMENT '备注',
  `recorded_by`   BIGINT UNSIGNED DEFAULT NULL COMMENT '录入人用户ID',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_student_course_exam_semester` (`student_id`, `course_id`, `exam_type`, `semester`),
  INDEX `idx_course_id` (`course_id`),
  INDEX `idx_semester` (`semester`),
  INDEX `idx_score` (`score`),
  CONSTRAINT `fk_score_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_score_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_score_recorder` FOREIGN KEY (`recorded_by`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `chk_score_range` CHECK (`score` IS NULL OR (`score` >= 0 AND `score` <= 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩表';

-- ============================================================
-- 10. 操作日志表
-- ============================================================
CREATE TABLE `sys_operation_log` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id`       BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID',
  `username`      VARCHAR(64)     DEFAULT NULL COMMENT '操作用户名（冗余，防止用户被删后无法追溯）',
  `module`        VARCHAR(64)     NOT NULL COMMENT '操作模块（如 student, course, score）',
  `action`        VARCHAR(64)     NOT NULL COMMENT '操作类型（如 CREATE, UPDATE, DELETE, LOGIN, EXPORT）',
  `target_type`   VARCHAR(64)     DEFAULT NULL COMMENT '操作对象类型',
  `target_id`     BIGINT UNSIGNED DEFAULT NULL COMMENT '操作对象ID',
  `description`   VARCHAR(512)    DEFAULT NULL COMMENT '操作描述',
  `request_method` VARCHAR(10)    DEFAULT NULL COMMENT '请求方法（GET, POST, PUT, DELETE）',
  `request_url`   VARCHAR(512)    DEFAULT NULL COMMENT '请求URL',
  `request_params` TEXT           DEFAULT NULL COMMENT '请求参数（JSON）',
  `request_body`  TEXT            DEFAULT NULL COMMENT '请求体（JSON，不含敏感字段）',
  `response_status` INT           DEFAULT NULL COMMENT 'HTTP响应状态码',
  `error_message` TEXT            DEFAULT NULL COMMENT '错误信息（如操作抛异常）',
  `ip_address`    VARCHAR(64)     DEFAULT NULL COMMENT '客户端IP',
  `user_agent`    VARCHAR(512)    DEFAULT NULL COMMENT 'User-Agent',
  `duration_ms`   INT UNSIGNED    DEFAULT NULL COMMENT '处理耗时（毫秒）',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_module` (`module`),
  INDEX `idx_action` (`action`),
  INDEX `idx_target` (`target_type`, `target_id`),
  INDEX `idx_created_at` (`created_at`),
  CONSTRAINT `fk_log_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';

-- ============================================================
-- 默认数据：初始化超级管理员角色与权限
-- ============================================================

INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `sort_order`) VALUES
(1,  0,  '系统管理',    'system',           1, 10),
(2,  1,  '用户管理',    'system:user',       1, 11),
(3,  2,  '查看用户',    'system:user:list',  2, 12),
(4,  2,  '新增用户',    'system:user:create', 2, 13),
(5,  2,  '编辑用户',    'system:user:update', 2, 14),
(6,  2,  '删除用户',    'system:user:delete', 2, 15),
(7,  1,  '角色管理',    'system:role',       1, 16),
(8,  7,  '查看角色',    'system:role:list',  2, 17),
(9,  7,  '新增角色',    'system:role:create', 2, 18),
(10, 7,  '编辑角色',    'system:role:update', 2, 19),
(11, 7,  '删除角色',    'system:role:delete', 2, 20),
(12, 0,  '学生管理',    'student',           1, 30),
(13, 12, '查看学生',    'student:list',      2, 31),
(14, 12, '新增学生',    'student:create',    2, 32),
(15, 12, '编辑学生',    'student:update',    2, 33),
(16, 12, '删除学生',    'student:delete',    2, 34),
(17, 12, '导出学生',    'student:export',    2, 35),
(18, 0,  '班级管理',    'class',             1, 40),
(19, 18, '查看班级',    'class:list',        2, 41),
(20, 18, '新增班级',    'class:create',      2, 42),
(21, 18, '编辑班级',    'class:update',      2, 43),
(22, 18, '删除班级',    'class:delete',      2, 44),
(23, 0,  '课程管理',    'course',            1, 50),
(24, 23, '查看课程',    'course:list',       2, 51),
(25, 23, '新增课程',    'course:create',     2, 52),
(26, 23, '编辑课程',    'course:update',     2, 53),
(27, 23, '删除课程',    'course:delete',     2, 54),
(28, 0,  '成绩管理',    'score',             1, 60),
(29, 28, '查看成绩',    'score:list',        2, 61),
(30, 28, '录入成绩',    'score:create',      2, 62),
(31, 28, '编辑成绩',    'score:update',      2, 63),
(32, 28, '导出成绩',    'score:export',      2, 64),
(33, 0,  '日志管理',    'log',               1, 70),
(34, 33, '查看日志',    'log:list',          2, 71);

INSERT INTO `sys_role` (`id`, `name`, `code`, `description`) VALUES
(1, '超级管理员', 'ROLE_ADMIN',    '系统最高权限，管理所有模块'),
(2, '教师',       'ROLE_TEACHER',  '管理学生、课程与成绩'),
(3, '辅导员',     'ROLE_COUNSELOR', '查看学生信息与班级管理');

-- 给超级管理员分配所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission`;

-- 给教师分配学生/课程/成绩模块权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, id FROM `sys_permission`
WHERE `code` IN (
  'student', 'student:list', 'student:create', 'student:update', 'student:export',
  'course', 'course:list', 'course:create', 'course:update',
  'score', 'score:list', 'score:create', 'score:update', 'score:export'
);

-- 给辅导员分配学生/班级模块权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 3, id FROM `sys_permission`
WHERE `code` IN (
  'student', 'student:list', 'student:export',
  'class', 'class:list', 'class:create', 'class:update'
);

-- 初始化一个超级管理员用户（密码为 admin123，BCrypt 加密）
-- 实际部署时请替换为安全密码的加密值
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `status`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 1);

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);
