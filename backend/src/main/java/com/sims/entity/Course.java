package com.sims.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseNo;
    private String name;
    private BigDecimal credit;
    private Integer hours;
    private String type;
    private String major;
    private String description;
    private Integer capacity;
    private Long teacherId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String semester;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private Integer enrolledCount;
}
