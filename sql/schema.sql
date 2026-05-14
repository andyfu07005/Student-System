-- ============================================================
-- 学生信息管理系统 (SIMS) — 数据库初始化脚本
-- MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS sims DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sims;

-- ============================================================
-- 系统用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL,
    password    VARCHAR(256) NOT NULL,
    real_name   VARCHAR(64)  DEFAULT NULL,
    email       VARCHAR(128) DEFAULT NULL,
    phone       VARCHAR(20)  DEFAULT NULL,
    avatar      VARCHAR(512) DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1-启用 0-禁用',
    login_fail  INT          NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    locked_until DATETIME    DEFAULT NULL COMMENT '锁定截止时间',
    last_login  DATETIME     DEFAULT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    UNIQUE INDEX uk_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- ============================================================
-- 角色表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code   VARCHAR(32)  NOT NULL COMMENT '角色编码 ADMIN/TEACHER/STUDENT',
    role_name   VARCHAR(64)  NOT NULL,
    description VARCHAR(256) DEFAULT NULL,
    sort        INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- ============================================================
-- 用户角色关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_user_role (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE INDEX uk_user_role (user_id, role_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- ============================================================
-- 班级表
-- ============================================================
CREATE TABLE IF NOT EXISTS class_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '班级名称',
    grade VARCHAR(20) NOT NULL COMMENT '年级',
    major VARCHAR(100) NOT NULL COMMENT '所属专业',
    head_teacher VARCHAR(50) DEFAULT NULL COMMENT '班主任',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name_grade (name, grade),
    INDEX idx_grade (grade),
    INDEX idx_major (major)
) ENGINE=InnoDB COMMENT='班级信息表';

-- ============================================================
-- 学生表
-- ============================================================
CREATE TABLE IF NOT EXISTS student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_no VARCHAR(20) NOT NULL COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender VARCHAR(4) NOT NULL COMMENT '性别',
    birth_date DATE DEFAULT NULL COMMENT '出生日期',
    id_card VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    address VARCHAR(200) DEFAULT NULL COMMENT '家庭住址',
    enrollment_date DATE DEFAULT NULL COMMENT '入学日期',
    class_id BIGINT DEFAULT NULL COMMENT '关联班级ID',
    status VARCHAR(20) NOT NULL DEFAULT '在读' COMMENT '学籍状态: 在读/休学/退学/毕业',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_no (student_no),
    UNIQUE KEY uk_id_card (id_card),
    INDEX idx_name (name),
    INDEX idx_class_id (class_id),
    INDEX idx_status (status),
    CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class_info(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='学生信息表';

-- ============================================================
-- 课程表
-- ============================================================
CREATE TABLE IF NOT EXISTS course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_no VARCHAR(20) NOT NULL COMMENT '课程编号',
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    credit DECIMAL(3,1) NOT NULL COMMENT '学分',
    hours INT NOT NULL COMMENT '学时',
    type VARCHAR(10) NOT NULL COMMENT '课程类型: 必修/选修',
    major VARCHAR(100) DEFAULT NULL COMMENT '所属专业',
    description VARCHAR(500) DEFAULT NULL COMMENT '课程描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_course_no (course_no),
    INDEX idx_type (type),
    INDEX idx_major (major)
) ENGINE=InnoDB COMMENT='课程信息表';

-- ============================================================
-- 学籍异动记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS enrollment_change (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    change_type VARCHAR(20) NOT NULL COMMENT '异动类型: SUSPENSION/WITHDRAWAL/TRANSFER/GRADUATION',
    previous_status VARCHAR(20) NOT NULL COMMENT '异动前状态',
    new_status VARCHAR(20) NOT NULL COMMENT '异动后状态',
    previous_class_id BIGINT DEFAULT NULL COMMENT '转班前班级ID',
    new_class_id BIGINT DEFAULT NULL COMMENT '转班后班级ID',
    change_date DATE NOT NULL COMMENT '异动日期',
    reason TEXT NOT NULL COMMENT '异动原因',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    corrected_record_id BIGINT DEFAULT NULL COMMENT '被更正的原始记录ID',
    correction_reason TEXT DEFAULT NULL COMMENT '更正原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_id (student_id),
    INDEX idx_change_type (change_type),
    INDEX idx_change_date (change_date),
    INDEX idx_corrected_record_id (corrected_record_id),
    CONSTRAINT fk_change_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_change_previous_class FOREIGN KEY (previous_class_id) REFERENCES class_info(id) ON DELETE SET NULL,
    CONSTRAINT fk_change_new_class FOREIGN KEY (new_class_id) REFERENCES class_info(id) ON DELETE SET NULL,
    CONSTRAINT fk_change_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    CONSTRAINT fk_change_corrected FOREIGN KEY (corrected_record_id) REFERENCES enrollment_change(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='学籍异动记录表';

-- ============================================================
-- 操作日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL COMMENT '操作用户ID',
    username VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    module VARCHAR(50) NOT NULL COMMENT '操作模块',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    description VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
    ip VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_module (module),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB COMMENT='操作日志表';
