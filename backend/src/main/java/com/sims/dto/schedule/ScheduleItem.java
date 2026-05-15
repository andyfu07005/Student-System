package com.sims.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleItem {
    private Long id;
    private Long courseId;
    private String courseNo;
    private String courseName;
    private String teacherName;
    private String classroom;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer startWeek;
    private Integer endWeek;
}
