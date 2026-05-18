package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.common.BusinessException;
import com.sims.entity.Grade;
import com.sims.mapper.GradeMapper;
import com.sims.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements GradeService {

    @Override
    public Page<Grade> search(String keyword, Long studentId, Long courseId, String semester, int page, int size) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(Grade::getStudentId, studentId);
        }
        if (courseId != null) {
            wrapper.eq(Grade::getCourseId, courseId);
        }
        if (StringUtils.hasText(semester)) {
            wrapper.eq(Grade::getSemester, semester);
        }
        wrapper.orderByDesc(Grade::getSemester).orderByAsc(Grade::getCourseId);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public boolean save(Grade grade) {
        validateRequired(grade);
        validateUnique(grade.getStudentId(), grade.getCourseId(), grade.getSemester(), null);
        return super.save(grade);
    }

    @Override
    public boolean updateById(Grade grade) {
        Grade existing = getById(grade.getId());
        if (existing == null) {
            throw new BusinessException(404, "成绩记录不存在");
        }
        validateRequired(grade);
        validateUnique(grade.getStudentId(), grade.getCourseId(), grade.getSemester(), grade.getId());
        return super.updateById(grade);
    }

    private void validateRequired(Grade grade) {
        if (grade.getStudentId() == null) {
            throw new BusinessException(400, "学生不能为空");
        }
        if (grade.getCourseId() == null) {
            throw new BusinessException(400, "课程不能为空");
        }
        if (grade.getScore() == null) {
            throw new BusinessException(400, "成绩不能为空");
        }
        if (!StringUtils.hasText(grade.getSemester())) {
            throw new BusinessException(400, "学期不能为空");
        }
    }

    private void validateUnique(Long studentId, Long courseId, String semester, Long excludeId) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>()
                .eq(Grade::getStudentId, studentId)
                .eq(Grade::getCourseId, courseId)
                .eq(Grade::getSemester, semester);
        if (excludeId != null) {
            wrapper.ne(Grade::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BusinessException(400, "该学生本学期已有该课程的成绩记录");
        }
    }
}
