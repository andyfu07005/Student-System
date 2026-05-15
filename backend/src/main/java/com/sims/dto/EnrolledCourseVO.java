package com.sims.dto;

import lombok.Data;

@Data
public class EnrolledCourseVO {
    private Long enrollmentId;
    private Long courseId;
    private String courseNo;
    private String courseName;
    private String type;
    private String teacherName;
    private Integer capacity;
    private Integer enrolledCount;
    private String semester;
    private String enrolledAt;
}
