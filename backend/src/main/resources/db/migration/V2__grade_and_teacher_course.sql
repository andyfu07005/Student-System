-- V2: 成绩管理 + 教师课程关联 + 学生关联用户
ALTER TABLE student ADD COLUMN IF NOT EXISTS user_id BIGINT COMMENT '关联系统用户ID';
CREATE INDEX IF NOT EXISTS idx_student_user_id ON student(user_id);
CREATE TABLE IF NOT EXISTS teacher_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL COMMENT '教师用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    semester VARCHAR(20) NOT NULL COMMENT '学期，如2024-2025-1',
    academic_year VARCHAR(20) NOT NULL COMMENT '学年，如2024-2025',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_teacher_course_semester (teacher_id, course_id, semester)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师-课程关联表';

CREATE TABLE IF NOT EXISTS grade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    score DECIMAL(5,2) NOT NULL COMMENT '成绩',
    semester VARCHAR(20) NOT NULL COMMENT '学期',
    academic_year VARCHAR(20) NOT NULL COMMENT '学年',
    exam_type VARCHAR(20) DEFAULT '期末' COMMENT '考试类型：期中/期末/补考',
    operator_id BIGINT COMMENT '录入人用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_course_semester_type (student_id, course_id, semester, exam_type),
    INDEX idx_student_semester (student_id, semester),
    INDEX idx_course_semester (course_id, semester),
    INDEX idx_academic_year (academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';
