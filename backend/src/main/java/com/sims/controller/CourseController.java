package com.sims.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.common.Result;
import com.sims.entity.Course;
import com.sims.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public Result<Page<Course>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String major,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(courseService.search(keyword, type, major, page, size));
    }

    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id) {
        return Result.ok(courseService.getById(id));
    }

    @PostMapping
    public Result<Course> create(@Valid @RequestBody Course course) {
        courseService.save(course);
        return Result.ok(course);
    }

    @PutMapping("/{id}")
    public Result<Course> update(@PathVariable Long id, @Valid @RequestBody Course course) {
        course.setId(id);
        courseService.updateById(course);
        return Result.ok(course);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.ok();
    }
}
