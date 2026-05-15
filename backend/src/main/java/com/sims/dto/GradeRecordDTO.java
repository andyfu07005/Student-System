package com.sims.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeRecordDTO {
    @NotNull(message = "学生不能为空")
    private Long studentId;

    @NotNull(message = "课程不能为空")
    private Long courseId;

    @NotBlank(message = "学年不能为空")
    private String academicYear;

    @NotBlank(message = "学期不能为空")
    private String semester;

    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0.0", message = "成绩不能小于0")
    @DecimalMax(value = "100.0", message = "成绩不能大于100")
    private BigDecimal score;
}
