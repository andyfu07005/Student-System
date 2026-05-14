package com.sims.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.common.Result;
import com.sims.entity.ClassInfo;
import com.sims.entity.Student;
import com.sims.service.ClassInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassInfoController {

    private final ClassInfoService classInfoService;

    @GetMapping
    public Result<Page<ClassInfo>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(classInfoService.search(keyword, page, size));
    }

    @GetMapping("/{id}")
    public Result<ClassInfo> getById(@PathVariable Long id) {
        return Result.ok(classInfoService.getById(id));
    }

    @GetMapping("/{id}/students")
    public Result<List<Student>> getStudents(@PathVariable Long id) {
        return Result.ok(classInfoService.getStudentsByClassId(id));
    }

    @PostMapping
    public Result<ClassInfo> create(@Valid @RequestBody ClassInfo classInfo) {
        classInfoService.save(classInfo);
        return Result.ok(classInfo);
    }

    @PutMapping("/{id}")
    public Result<ClassInfo> update(@PathVariable Long id, @Valid @RequestBody ClassInfo classInfo) {
        classInfo.setId(id);
        classInfoService.updateById(classInfo);
        return Result.ok(classInfo);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        classInfoService.deleteClass(id);
        return Result.ok();
    }
}
