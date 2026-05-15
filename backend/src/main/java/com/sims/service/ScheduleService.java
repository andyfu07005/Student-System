package com.sims.service;

import com.sims.dto.schedule.WeekSchedule;

import java.time.LocalDate;

public interface ScheduleService {
    WeekSchedule getWeekSchedule(Long userId, LocalDate date);
}
