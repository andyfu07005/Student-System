package com.sims.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GradeRecordVO {
    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Long courseId;
    private String courseNo;
    private String courseName;
    private BigDecimal credit;
    private String academicYear;
    private String semester;
    private BigDecimal score;
    private BigDecimal gradePoint;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
