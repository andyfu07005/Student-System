package com.sims.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.common.Result;
import com.sims.dto.ScheduleDTO;
import com.sims.entity.Schedule;
import com.sims.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public Result<Page<Schedule>> list(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String classroom,
            @RequestParam(required = false) Integer dayOfWeek,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(scheduleService.search(courseId, teacherId, classroom, dayOfWeek, page, size));
    }

    @GetMapping("/{id}")
    public Result<Schedule> getById(@PathVariable Long id) {
        return Result.ok(scheduleService.getById(id));
    }

    @PostMapping
    public Result<Schedule> create(@Valid @RequestBody ScheduleDTO dto) {
        return Result.ok(scheduleService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Schedule> update(@PathVariable Long id, @Valid @RequestBody ScheduleDTO dto) {
        return Result.ok(scheduleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        scheduleService.removeById(id);
        return Result.ok();
    }
}
