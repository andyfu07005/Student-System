package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.common.BusinessException;
import com.sims.entity.Grade;
import com.sims.mapper.GradeMapper;
import com.sims.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeMapper gradeMapper;

    @Override
    public Page<Grade> listStudentGrades(Long studentId, String semester, String academicYear,
                                         int page, int size) {
        LambdaQueryWrapper<Grade> wrapper = buildStudentGradeWrapper(studentId, semester, academicYear);
        wrapper.orderByDesc(Grade::getCreatedAt);
        return gradeMapper.selectPageWithDetails(new Page<>(page, size), wrapper);
    }

    @Override
    public Map<String, Object> studentStatistics(Long studentId, String academicYear) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>()
                .eq(Grade::getStudentId, studentId);
        if (StringUtils.hasText(academicYear)) {
            wrapper.eq(Grade::getAcademicYear, academicYear);
        }
        Map<String, Object> stats = gradeMapper.selectStatistics(wrapper);
        if (stats == null) stats = new HashMap<>();
        stats.put("passRate", calcRate(stats, "pass_count", "fail_count"));
        stats.put("excellentRate", calcRate(stats, "excellent_count", "total_count"));
        stats.put("distribution", gradeMapper.selectScoreDistribution(wrapper));
        return stats;
    }

    @Override
    public Page<Grade> listCourseGrades(Long courseId, String semester, int page, int size) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>()
                .eq(Grade::getCourseId, courseId);
        if (StringUtils.hasText(semester)) {
            wrapper.eq(Grade::getSemester, semester);
        }
        wrapper.orderByDesc(Grade::getScore);
        return gradeMapper.selectPageWithDetails(new Page<>(page, size), wrapper);
    }

    @Override
    public Map<String, Object> courseStatistics(Long courseId, String semester) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>()
                .eq(Grade::getCourseId, courseId);
        if (StringUtils.hasText(semester)) {
            wrapper.eq(Grade::getSemester, semester);
        }
        Map<String, Object> stats = gradeMapper.selectStatistics(wrapper);
        if (stats == null) stats = new HashMap<>();
        stats.put("passRate", calcRate(stats, "pass_count", "fail_count"));
        stats.put("excellentRate", calcRate(stats, "excellent_count", "total_count"));
        stats.put("distribution", gradeMapper.selectScoreDistribution(wrapper));
        return stats;
    }

    @Override
    public Map<String, Object> adminStatistics(String academicYear) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(academicYear)) {
            wrapper.eq(Grade::getAcademicYear, academicYear);
        }
        Map<String, Object> stats = gradeMapper.selectStatistics(wrapper);
        if (stats == null) stats = new HashMap<>();
        stats.put("passRate", calcRate(stats, "pass_count", "fail_count"));
        stats.put("excellentRate", calcRate(stats, "excellent_count", "total_count"));

        // 按课程分组统计
        List<Map<String, Object>> courseDist = new ArrayList<>();
        List<Map<String, Object>> rawDist = gradeMapper.selectScoreDistribution(wrapper);
        stats.put("distribution", rawDist);

        // 按学期统计
        if (StringUtils.hasText(academicYear)) {
            LambdaQueryWrapper<Grade> semWrapper = new LambdaQueryWrapper<Grade>()
                    .eq(Grade::getAcademicYear, academicYear)
                    .select(Grade::getSemester);
            semWrapper.groupBy(Grade::getSemester);
            // Keep distribution only, course breakdown handled separately
        }

        return stats;
    }

    @Override
    public List<Map<String, Object>> scoreDistribution(Long courseId, String semester) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>()
                .eq(Grade::getCourseId, courseId);
        if (StringUtils.hasText(semester)) {
            wrapper.eq(Grade::getSemester, semester);
        }
        return gradeMapper.selectScoreDistribution(wrapper);
    }

    @Override
    public Page<Grade> listByStudentId(Long studentId, int page, int size) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>()
                .eq(Grade::getStudentId, studentId)
                .orderByDesc(Grade::getCreatedAt);
        return gradeMapper.selectPageWithDetails(new Page<>(page, size), wrapper);
    }

    @Override
    public Grade save(Grade grade, Long operatorId) {
        if (grade.getScore() == null) {
            throw new BusinessException("成绩不能为空");
        }
        if (grade.getScore().compareTo(BigDecimal.ZERO) < 0
                || grade.getScore().compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("成绩必须在0-100之间");
        }
        if (grade.getStudentId() == null) {
            throw new BusinessException("学生不能为空");
        }
        if (grade.getCourseId() == null) {
            throw new BusinessException("课程不能为空");
        }
        grade.setOperatorId(operatorId);
        if (!StringUtils.hasText(grade.getExamType())) {
            grade.setExamType("期末");
        }
        gradeMapper.insert(grade);
        return grade;
    }

    @Override
    public Grade update(Long id, Grade req) {
        Grade grade = gradeMapper.selectById(id);
        if (grade == null) {
            throw new BusinessException("成绩记录不存在");
        }
        if (req.getScore() != null) {
            if (req.getScore().compareTo(BigDecimal.ZERO) < 0
                    || req.getScore().compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException("成绩必须在0-100之间");
            }
            grade.setScore(req.getScore());
        }
        if (StringUtils.hasText(req.getSemester())) grade.setSemester(req.getSemester());
        if (StringUtils.hasText(req.getAcademicYear())) grade.setAcademicYear(req.getAcademicYear());
        if (StringUtils.hasText(req.getExamType())) grade.setExamType(req.getExamType());
        gradeMapper.updateById(grade);
        return grade;
    }

    @Override
    public void delete(Long id) {
        if (gradeMapper.selectById(id) == null) {
            throw new BusinessException("成绩记录不存在");
        }
        gradeMapper.deleteById(id);
    }

    private LambdaQueryWrapper<Grade> buildStudentGradeWrapper(Long studentId, String semester,
                                                               String academicYear) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>()
                .eq(Grade::getStudentId, studentId);
        if (StringUtils.hasText(semester)) {
            wrapper.eq(Grade::getSemester, semester);
        }
        if (StringUtils.hasText(academicYear)) {
            wrapper.eq(Grade::getAcademicYear, academicYear);
        }
        return wrapper;
    }

    private String calcRate(Map<String, Object> stats, String numeratorKey, String denominatorKey) {
        Object numObj = stats.get(numeratorKey);
        Object denObj = stats.get(denominatorKey);
        if (numObj == null || denObj == null) return "0.00";
        long num = ((Number) numObj).longValue();
        long den = ((Number) denObj).longValue();
        if (den == 0) return "0.00";
        // Include the numerator count in denominator for pass rate
        if ("pass_count".equals(numeratorKey) && "fail_count".equals(denominatorKey)) {
            den = num + den;
        }
        return BigDecimal.valueOf(num)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(den), 2, RoundingMode.HALF_UP)
                .toString();
    }
}
