package com.sims.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeekSchedule {
    private String weekLabel;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private List<ScheduleItem> items;
}
