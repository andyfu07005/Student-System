package com.sims.controller;

import com.sims.common.Result;
import com.sims.dto.schedule.WeekSchedule;
import com.sims.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public Result<WeekSchedule> getWeekSchedule(
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = (Long) request.getAttribute("userId");
        WeekSchedule schedule = scheduleService.getWeekSchedule(userId, date);
        return Result.ok(schedule);
    }
}
