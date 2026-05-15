package com.sims.dto;

import lombok.Data;

@Data
public class EnrolledStudentVO {
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String gender;
    private String phone;
    private String className;
    private String enrolledAt;
}
