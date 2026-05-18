package com.sims.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.common.Result;
import com.sims.dto.GradeRecordDTO;
import com.sims.entity.GpaAlgorithm;
import com.sims.service.GradeRecordService;
import com.sims.vo.GpaSummaryVO;
import com.sims.vo.GradeRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grade-records")
@RequiredArgsConstructor
public class GradeRecordController {

    private final GradeRecordService gradeRecordService;

    @GetMapping
    public Result<Page<GradeRecordVO>> list(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String semester,
            @RequestParam(defaultValue = "FOUR_POINT") GpaAlgorithm algorithm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(gradeRecordService.search(studentId, courseId, academicYear, semester, algorithm, page, size));
    }

    @GetMapping("/gpa")
    public Result<GpaSummaryVO> gpa(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "FOUR_POINT") GpaAlgorithm algorithm) {
        return Result.ok(gradeRecordService.calculateGpa(studentId, algorithm));
    }

    @PostMapping
    public Result<GradeRecordVO> create(
            @Valid @RequestBody GradeRecordDTO dto,
            @RequestParam(defaultValue = "FOUR_POINT") GpaAlgorithm algorithm) {
        return Result.ok(gradeRecordService.create(dto, algorithm));
    }

    @PutMapping("/{id}")
    public Result<GradeRecordVO> update(
            @PathVariable Long id,
            @Valid @RequestBody GradeRecordDTO dto,
            @RequestParam(defaultValue = "FOUR_POINT") GpaAlgorithm algorithm) {
        return Result.ok(gradeRecordService.update(id, dto, algorithm));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        gradeRecordService.removeById(id);
        return Result.ok();
    }
}
