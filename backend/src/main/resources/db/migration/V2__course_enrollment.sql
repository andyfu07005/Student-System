-- ============================================================
-- V2: 课程选课功能
-- ============================================================

-- 课程表增加容量、教师、开课日期等字段
ALTER TABLE course
    ADD COLUMN capacity    INT          DEFAULT 30  COMMENT '课程容量',
    ADD COLUMN teacher_id  BIGINT       DEFAULT NULL COMMENT '授课教师ID (sys_user.id)',
    ADD COLUMN start_date  DATE         DEFAULT NULL COMMENT '开课日期',
    ADD COLUMN end_date    DATE         DEFAULT NULL COMMENT '结课日期',
    ADD COLUMN semester    VARCHAR(20)  DEFAULT NULL COMMENT '学期 (如 2025-2026-1)';

ALTER TABLE course
    ADD INDEX idx_teacher_id (teacher_id),
    ADD CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE SET NULL;

-- 学生表增加 user_id 关联系统用户
ALTER TABLE student
    ADD COLUMN user_id BIGINT DEFAULT NULL COMMENT '关联系统用户ID';

ALTER TABLE student
    ADD INDEX idx_user_id (user_id),
    ADD CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL;

-- 选课记录表
CREATE TABLE IF NOT EXISTS course_enrollment (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT       NOT NULL COMMENT '学生ID',
    course_id    BIGINT       NOT NULL COMMENT '课程ID',
    status       VARCHAR(20)  NOT NULL DEFAULT 'ENROLLED' COMMENT 'ENROLLED/DROPPED',
    enrolled_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dropped_at   DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_student_course (student_id, course_id),
    INDEX idx_course_id (course_id),
    INDEX idx_status (status),
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课记录表';
