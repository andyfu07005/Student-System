package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.common.BusinessException;
import com.sims.entity.Student;
import com.sims.mapper.ClassInfoMapper;
import com.sims.mapper.StudentMapper;
import com.sims.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    private final ClassInfoMapper classInfoMapper;

    @Override
    public Page<Student> search(String keyword, Long classId, String status, int page, int size) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Student::getName, keyword).or().like(Student::getStudentNo, keyword));
        }
        if (classId != null) {
            wrapper.eq(Student::getClassId, classId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Student::getStatus, status);
        }
        wrapper.orderByDesc(Student::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public boolean save(Student student) {
        validateStudentNoUnique(student.getStudentNo(), null);
        if (StringUtils.hasText(student.getIdCard())) {
            validateIdCardUnique(student.getIdCard(), null);
        }
        validateRequired(student);
        return super.save(student);
    }

    @Override
    public boolean updateById(Student student) {
        Student existing = getById(student.getId());
        if (existing == null) {
            throw new BusinessException(404, "学生不存在");
        }
        validateStudentNoUnique(student.getStudentNo(), student.getId());
        if (StringUtils.hasText(student.getIdCard())) {
            validateIdCardUnique(student.getIdCard(), student.getId());
        }
        validateRequired(student);
        return super.updateById(student);
    }

    private void validateRequired(Student student) {
        if (!StringUtils.hasText(student.getStudentNo())) {
            throw new BusinessException(400, "学号不能为空");
        }
        if (!StringUtils.hasText(student.getName())) {
            throw new BusinessException(400, "姓名不能为空");
        }
        if (!StringUtils.hasText(student.getGender())) {
            throw new BusinessException(400, "性别不能为空");
        }
    }

    private void validateStudentNoUnique(String studentNo, Long excludeId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, studentNo);
        if (excludeId != null) {
            wrapper.ne(Student::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BusinessException(400, "学号已存在");
        }
    }

    private void validateIdCardUnique(String idCard, Long excludeId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<Student>()
                .eq(Student::getIdCard, idCard);
        if (excludeId != null) {
            wrapper.ne(Student::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BusinessException(400, "身份证号已存在");
        }
    }
}
