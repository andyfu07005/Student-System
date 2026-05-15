package com.sims.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("course_enrollment")
public class CourseEnrollment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long courseId;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime enrolledAt;
    private LocalDateTime droppedAt;
}
