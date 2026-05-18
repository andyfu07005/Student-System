-- ============================================================
-- Flyway V2 — 课表与选课功能
-- ============================================================

-- 为 student 表添加与系统用户的关联字段
ALTER TABLE student ADD COLUMN user_id BIGINT DEFAULT NULL COMMENT '关联系统用户ID' AFTER class_id;
ALTER TABLE student ADD INDEX idx_user_id (user_id);

CREATE TABLE IF NOT EXISTS course_schedule (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id    BIGINT       NOT NULL COMMENT '课程ID',
    teacher_id   BIGINT       NOT NULL COMMENT '教师ID(sys_user.id)',
    classroom    VARCHAR(100) NOT NULL COMMENT '上课地点',
    day_of_week  TINYINT      NOT NULL COMMENT '星期几 1=周一..7=周日',
    start_time   TIME         NOT NULL COMMENT '开始时间',
    end_time     TIME         NOT NULL COMMENT '结束时间',
    start_week   INT          NOT NULL COMMENT '起始教学周',
    end_week     INT          NOT NULL COMMENT '结束教学周',
    semester     VARCHAR(50)  NOT NULL COMMENT '学期 如 2025-2026-2',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course_id (course_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_day_of_week (day_of_week),
    INDEX idx_semester (semester),
    CONSTRAINT fk_schedule_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程安排表';

CREATE TABLE IF NOT EXISTS student_course (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id  BIGINT NOT NULL COMMENT '课程ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_course (student_id, course_id),
    INDEX idx_course_id (course_id),
    CONSTRAINT fk_sc_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_sc_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生选课表';
