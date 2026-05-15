-- ============================================================
-- V2: 成绩表
-- ============================================================
CREATE TABLE IF NOT EXISTS grade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    score DECIMAL(5,1) NOT NULL COMMENT '成绩',
    semester VARCHAR(20) NOT NULL COMMENT '学期 如: 2024-2025-1',
    academic_year VARCHAR(20) NOT NULL COMMENT '学年 如: 2024-2025',
    exam_type VARCHAR(20) NOT NULL DEFAULT '期末' COMMENT '考试类型: 期中/期末/补考',
    remarks VARCHAR(200) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_course_semester (student_id, course_id, semester),
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id),
    INDEX idx_semester (semester),
    CONSTRAINT fk_grade_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_grade_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='成绩表';
