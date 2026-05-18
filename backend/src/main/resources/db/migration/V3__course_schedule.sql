-- ============================================================
-- V3: 排课功能
-- ============================================================

CREATE TABLE IF NOT EXISTS course_schedule (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id    BIGINT       NOT NULL COMMENT '课程ID',
    teacher_id   BIGINT       NOT NULL COMMENT '授课教师ID',
    classroom    VARCHAR(100) NOT NULL COMMENT '上课地点',
    day_of_week  INT          NOT NULL COMMENT '星期 (1-7)',
    start_time   TIME         NOT NULL COMMENT '开始时间',
    end_time     TIME         NOT NULL COMMENT '结束时间',
    capacity     INT          DEFAULT 30 COMMENT '容纳人数',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_teacher_day (teacher_id, day_of_week),
    INDEX idx_classroom_day (classroom, day_of_week),
    INDEX idx_course_id (course_id),
    CONSTRAINT fk_schedule_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课表';
