CREATE TABLE IF NOT EXISTS grade_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    academic_year VARCHAR(20) NOT NULL COMMENT '学年, 如 2025-2026',
    semester VARCHAR(20) NOT NULL COMMENT '学期',
    score DECIMAL(5,2) NOT NULL COMMENT '百分制成绩',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_course_term (student_id, course_id, academic_year, semester),
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id),
    INDEX idx_term (academic_year, semester),
    CONSTRAINT fk_grade_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_grade_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='成绩记录表';
