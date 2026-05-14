package com.sims.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("enrollment_change")
public class EnrollmentChange {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String changeType;
    private String previousStatus;
    private String newStatus;
    private Long previousClassId;
    private Long newClassId;
    private LocalDate changeDate;
    private String reason;
    private Long operatorId;
    private Long correctedRecordId;
    private String correctionReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
