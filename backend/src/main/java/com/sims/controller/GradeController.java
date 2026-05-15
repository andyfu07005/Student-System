package com.sims.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.common.Result;
import com.sims.entity.Grade;
import com.sims.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping
    public Result<Page<Grade>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String semester,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(gradeService.search(keyword, studentId, courseId, semester, page, size));
    }

    @GetMapping("/{id}")
    public Result<Grade> getById(@PathVariable Long id) {
        return Result.ok(gradeService.getById(id));
    }

    @PostMapping
    public Result<Grade> create(@Valid @RequestBody Grade grade) {
        gradeService.save(grade);
        return Result.ok(grade);
    }

    @PutMapping("/{id}")
    public Result<Grade> update(@PathVariable Long id, @Valid @RequestBody Grade grade) {
        grade.setId(id);
        gradeService.updateById(grade);
        return Result.ok(grade);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        gradeService.removeById(id);
        return Result.ok();
    }
}
