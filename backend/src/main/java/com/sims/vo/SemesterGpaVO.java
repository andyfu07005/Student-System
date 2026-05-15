package com.sims.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SemesterGpaVO {
    private String academicYear;
    private String semester;
    private BigDecimal credits;
    private BigDecimal gpa;
}
