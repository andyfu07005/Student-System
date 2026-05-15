package com.sims.vo;

import com.sims.entity.GpaAlgorithm;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class GpaSummaryVO {
    private Long studentId;
    private String studentNo;
    private String studentName;
    private GpaAlgorithm algorithm;
    private BigDecimal cumulativeCredits;
    private BigDecimal cumulativeGpa;
    private List<SemesterGpaVO> semesters = new ArrayList<>();
}
