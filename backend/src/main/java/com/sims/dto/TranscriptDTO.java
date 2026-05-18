package com.sims.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class TranscriptDTO {
    private String schoolName = "学生管理系统";
    private String academicYear;
    private String semester;
    private String studentNo;
    private String studentName;
    private String gender;
    private String className;
    private String grade;
    private String major;
    private String enrollmentDate;
    private List<SemesterGroup> semesters;
    private BigDecimal totalGpa;
    private BigDecimal totalCredits;

    @Data
    public static class SemesterGroup {
        private String semester;
        private List<CourseGrade> courses;
        private BigDecimal semesterGpa;
        private BigDecimal semesterCredits;
    }

    @Data
    public static class CourseGrade {
        private String courseNo;
        private String courseName;
        private BigDecimal credit;
        private String courseType;
        private BigDecimal score;
        private String examType;
        private BigDecimal gradePoint;
    }
}
