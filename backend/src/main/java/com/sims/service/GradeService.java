package com.sims.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.entity.Grade;

import java.util.List;
import java.util.Map;

public interface GradeService {

    Page<Grade> listStudentGrades(Long studentId, String semester, String academicYear, int page, int size);

    Map<String, Object> studentStatistics(Long studentId, String academicYear);

    Page<Grade> listCourseGrades(Long courseId, String semester, int page, int size);

    Map<String, Object> courseStatistics(Long courseId, String semester);

    Map<String, Object> adminStatistics(String academicYear);

    List<Map<String, Object>> scoreDistribution(Long courseId, String semester);

    Page<Grade> listByStudentId(Long studentId, int page, int size);

    Grade save(Grade grade, Long operatorId);

    Grade update(Long id, Grade grade);

    void delete(Long id);
}
