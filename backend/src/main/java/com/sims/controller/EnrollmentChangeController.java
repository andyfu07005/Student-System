package com.sims.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.common.Result;
import com.sims.dto.EnrollmentChangeCorrectDTO;
import com.sims.dto.EnrollmentChangeCreateDTO;
import com.sims.entity.EnrollmentChange;
import com.sims.service.EnrollmentChangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollment-changes")
@RequiredArgsConstructor
public class EnrollmentChangeController {

    private final EnrollmentChangeService enrollmentChangeService;

    @PostMapping
    public Result<EnrollmentChange> create(@Valid @RequestBody EnrollmentChangeCreateDTO dto,
                                           @RequestParam(defaultValue = "1") Long operatorId) {
        return Result.ok(enrollmentChangeService.create(dto, operatorId));
    }

    @PostMapping("/correct")
    public Result<EnrollmentChange> correct(@Valid @RequestBody EnrollmentChangeCorrectDTO dto,
                                            @RequestParam(defaultValue = "1") Long operatorId) {
        return Result.ok(enrollmentChangeService.correct(dto, operatorId));
    }

    @GetMapping("/{id}")
    public Result<EnrollmentChange> getById(@PathVariable Long id) {
        return Result.ok(enrollmentChangeService.getById(id));
    }

    @GetMapping("/student/{studentId}")
    public Result<List<EnrollmentChange>> timeline(@PathVariable Long studentId) {
        return Result.ok(enrollmentChangeService.getTimelineByStudentId(studentId));
    }

    @GetMapping
    public Result<Page<EnrollmentChange>> page(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int pageSize,
                                                @RequestParam(required = false) Long studentId,
                                                @RequestParam(required = false) String changeType) {
        return Result.ok(enrollmentChangeService.page(page, pageSize, studentId, changeType));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return Result.fail(403, "异动记录不可物理删除，请使用更正功能追加更正记录");
    }
}
