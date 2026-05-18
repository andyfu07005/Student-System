package com.sims.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("student_course")
public class StudentCourse {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long courseId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
