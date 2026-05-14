package com.sims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EnrollmentChangeCreateDTO {
    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotBlank(message = "异动类型不能为空")
    private String changeType;

    @NotBlank(message = "异动前状态不能为空")
    private String previousStatus;

    @NotBlank(message = "异动后状态不能为空")
    private String newStatus;

    private Long previousClassId;
    private Long newClassId;

    @NotNull(message = "异动日期不能为空")
    private LocalDate changeDate;

    @NotBlank(message = "异动原因不能为空")
    private String reason;
}
