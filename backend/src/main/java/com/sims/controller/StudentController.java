package com.sims.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.common.Result;
import com.sims.entity.Student;
import com.sims.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public Result<Page<Student>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(studentService.search(keyword, classId, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Long id) {
        return Result.ok(studentService.getById(id));
    }

    @PostMapping
    public Result<Student> create(@Valid @RequestBody Student student) {
        studentService.save(student);
        return Result.ok(student);
    }

    @PutMapping("/{id}")
    public Result<Student> update(@PathVariable Long id, @Valid @RequestBody Student student) {
        student.setId(id);
        studentService.updateById(student);
        return Result.ok(student);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        studentService.removeById(id);
        return Result.ok();
    }
}
