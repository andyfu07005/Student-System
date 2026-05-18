package com.sims.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.common.Result;
import com.sims.dto.ScheduleDTO;
import com.sims.dto.schedule.WeekSchedule;
import com.sims.entity.Schedule;
import com.sims.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // ==== 排课管理 CRUD ====

    @GetMapping("/schedules")
    public Result<Page<Schedule>> list(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String classroom,
            @RequestParam(required = false) Integer dayOfWeek,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(scheduleService.search(courseId, teacherId, classroom, dayOfWeek, page, size));
    }

    @GetMapping("/schedules/{id}")
    public Result<Schedule> getById(@PathVariable Long id) {
        return Result.ok(scheduleService.getById(id));
    }

    @PostMapping("/schedules")
    public Result<Schedule> create(@Valid @RequestBody ScheduleDTO dto) {
        return Result.ok(scheduleService.create(dto));
    }

    @PutMapping("/schedules/{id}")
    public Result<Schedule> update(@PathVariable Long id, @Valid @RequestBody ScheduleDTO dto) {
        return Result.ok(scheduleService.update(id, dto));
    }

    @DeleteMapping("/schedules/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        scheduleService.removeById(id);
        return Result.ok();
    }

    // ==== 课表查询（学生/教师周课表） ====

    @GetMapping("/schedule")
    public Result<WeekSchedule> getWeekSchedule(
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = (Long) request.getAttribute("userId");
        WeekSchedule schedule = scheduleService.getWeekSchedule(userId, date);
        return Result.ok(schedule);
    }
}
