package com.sims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentChangeCorrectDTO {
    @NotNull(message = "原始记录ID不能为空")
    private Long correctedRecordId;

    @NotBlank(message = "更正原因不能为空")
    private String correctionReason;

    @NotBlank(message = "异动类型不能为空")
    private String changeType;

    @NotBlank(message = "异动前状态不能为空")
    private String previousStatus;

    @NotBlank(message = "异动后状态不能为空")
    private String newStatus;

    private Long previousClassId;
    private Long newClassId;

    @NotBlank(message = "异动原因不能为空")
    private String reason;
}
