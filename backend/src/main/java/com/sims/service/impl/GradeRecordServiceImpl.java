package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.common.BusinessException;
import com.sims.dto.GradeRecordDTO;
import com.sims.entity.Course;
import com.sims.entity.GpaAlgorithm;
import com.sims.entity.GradeRecord;
import com.sims.entity.Student;
import com.sims.mapper.CourseMapper;
import com.sims.mapper.GradeRecordMapper;
import com.sims.mapper.StudentMapper;
import com.sims.service.GradePointCalculator;
import com.sims.service.GradeRecordService;
import com.sims.vo.GpaSummaryVO;
import com.sims.vo.GradeRecordVO;
import com.sims.vo.SemesterGpaVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GradeRecordServiceImpl extends ServiceImpl<GradeRecordMapper, GradeRecord> implements GradeRecordService {

    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final GradePointCalculator calculator;

    @Override
    public Page<GradeRecordVO> search(Long studentId, Long courseId, String academicYear, String semester,
                                      GpaAlgorithm algorithm, int page, int size) {
        LambdaQueryWrapper<GradeRecord> wrapper = queryWrapper(studentId, courseId, academicYear, semester);
        Page<GradeRecord> recordPage = page(new Page<>(page, size), wrapper);
        Page<GradeRecordVO> result = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        result.setRecords(recordPage.getRecords().stream().map(record -> toVO(record, algorithm)).toList());
        return result;
    }

    @Override
    public GradeRecordVO create(GradeRecordDTO dto, GpaAlgorithm algorithm) {
        validateReferences(dto);
        validateDuplicate(dto, null);
        GradeRecord record = fromDTO(dto);
        save(record);
        return toVO(record, algorithm);
    }

    @Override
    public GradeRecordVO update(Long id, GradeRecordDTO dto, GpaAlgorithm algorithm) {
        GradeRecord existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "成绩记录不存在");
        }
        validateReferences(dto);
        validateDuplicate(dto, id);
        GradeRecord record = fromDTO(dto);
        record.setId(id);
        updateById(record);
        return toVO(record, algorithm);
    }

    @Override
    public GpaSummaryVO calculateGpa(Long studentId, GpaAlgorithm algorithm) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        List<GradeRecord> records = list(queryWrapper(studentId, null, null, null));
        GpaAccumulator total = new GpaAccumulator();
        Map<String, GpaAccumulator> semesterMap = new LinkedHashMap<>();

        for (GradeRecord record : records) {
            Course course = courseMapper.selectById(record.getCourseId());
            if (course == null) continue;

            BigDecimal gradePoint = calculator.toGradePoint(record.getScore(), algorithm);
            BigDecimal credit = course.getCredit();
            BigDecimal weighted = gradePoint.multiply(credit);
            total.add(credit, weighted);

            String key = record.getAcademicYear() + "|" + record.getSemester();
            semesterMap.computeIfAbsent(key, ignored -> new GpaAccumulator()).add(credit, weighted);
        }

        GpaSummaryVO summary = new GpaSummaryVO();
        summary.setStudentId(student.getId());
        summary.setStudentNo(student.getStudentNo());
        summary.setStudentName(student.getName());
        summary.setAlgorithm(algorithm);
        summary.setCumulativeCredits(total.credits);
        summary.setCumulativeGpa(calculator.weightedGpa(total.weightedPoints, total.credits));
        summary.setSemesters(semesterMap.entrySet().stream().map(entry -> {
            String[] parts = entry.getKey().split("\\|", 2);
            SemesterGpaVO vo = new SemesterGpaVO();
            vo.setAcademicYear(parts[0]);
            vo.setSemester(parts.length > 1 ? parts[1] : "");
            vo.setCredits(entry.getValue().credits);
            vo.setGpa(calculator.weightedGpa(entry.getValue().weightedPoints, entry.getValue().credits));
            return vo;
        }).toList());
        return summary;
    }

    private LambdaQueryWrapper<GradeRecord> queryWrapper(Long studentId, Long courseId, String academicYear, String semester) {
        LambdaQueryWrapper<GradeRecord> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) wrapper.eq(GradeRecord::getStudentId, studentId);
        if (courseId != null) wrapper.eq(GradeRecord::getCourseId, courseId);
        if (StringUtils.hasText(academicYear)) wrapper.eq(GradeRecord::getAcademicYear, academicYear);
        if (StringUtils.hasText(semester)) wrapper.eq(GradeRecord::getSemester, semester);
        wrapper.orderByAsc(GradeRecord::getAcademicYear)
                .orderByAsc(GradeRecord::getSemester)
                .orderByAsc(GradeRecord::getStudentId);
        return wrapper;
    }

    private void validateReferences(GradeRecordDTO dto) {
        if (studentMapper.selectById(dto.getStudentId()) == null) {
            throw new BusinessException(400, "学生不存在");
        }
        if (courseMapper.selectById(dto.getCourseId()) == null) {
            throw new BusinessException(400, "课程不存在");
        }
    }

    private void validateDuplicate(GradeRecordDTO dto, Long excludeId) {
        LambdaQueryWrapper<GradeRecord> wrapper = new LambdaQueryWrapper<GradeRecord>()
                .eq(GradeRecord::getStudentId, dto.getStudentId())
                .eq(GradeRecord::getCourseId, dto.getCourseId())
                .eq(GradeRecord::getAcademicYear, dto.getAcademicYear())
                .eq(GradeRecord::getSemester, dto.getSemester());
        if (excludeId != null) {
            wrapper.ne(GradeRecord::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BusinessException(400, "该学生本学期课程成绩已存在");
        }
    }

    private GradeRecord fromDTO(GradeRecordDTO dto) {
        GradeRecord record = new GradeRecord();
        record.setStudentId(dto.getStudentId());
        record.setCourseId(dto.getCourseId());
        record.setAcademicYear(dto.getAcademicYear());
        record.setSemester(dto.getSemester());
        record.setScore(dto.getScore());
        return record;
    }

    private GradeRecordVO toVO(GradeRecord record, GpaAlgorithm algorithm) {
        Student student = studentMapper.selectById(record.getStudentId());
        Course course = courseMapper.selectById(record.getCourseId());
        GradeRecordVO vo = new GradeRecordVO();
        vo.setId(record.getId());
        vo.setStudentId(record.getStudentId());
        vo.setCourseId(record.getCourseId());
        vo.setAcademicYear(record.getAcademicYear());
        vo.setSemester(record.getSemester());
        vo.setScore(record.getScore());
        vo.setCreatedAt(record.getCreatedAt());
        vo.setUpdatedAt(record.getUpdatedAt());
        vo.setGradePoint(calculator.toGradePoint(record.getScore(), algorithm));
        if (student != null) {
            vo.setStudentNo(student.getStudentNo());
            vo.setStudentName(student.getName());
        }
        if (course != null) {
            vo.setCourseNo(course.getCourseNo());
            vo.setCourseName(course.getName());
            vo.setCredit(course.getCredit());
        }
        return vo;
    }

    private static class GpaAccumulator {
        private BigDecimal credits = BigDecimal.ZERO;
        private BigDecimal weightedPoints = BigDecimal.ZERO;

        private void add(BigDecimal credit, BigDecimal weighted) {
            credits = credits.add(credit);
            weightedPoints = weightedPoints.add(weighted);
        }
    }
}
