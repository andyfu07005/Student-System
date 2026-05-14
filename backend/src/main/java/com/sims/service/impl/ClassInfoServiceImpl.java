package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.common.BusinessException;
import com.sims.entity.ClassInfo;
import com.sims.entity.Student;
import com.sims.mapper.ClassInfoMapper;
import com.sims.mapper.StudentMapper;
import com.sims.service.ClassInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoMapper, ClassInfo> implements ClassInfoService {

    private final StudentMapper studentMapper;

    @Override
    public Page<ClassInfo> search(String keyword, int page, int size) {
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ClassInfo::getName, keyword)
                    .or().like(ClassInfo::getGrade, keyword)
                    .or().like(ClassInfo::getMajor, keyword));
        }
        wrapper.orderByDesc(ClassInfo::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<Student> getStudentsByClassId(Long classId) {
        return studentMapper.selectList(new LambdaQueryWrapper<Student>().eq(Student::getClassId, classId));
    }

    @Override
    public boolean save(ClassInfo classInfo) {
        if (!StringUtils.hasText(classInfo.getName())) {
            throw new BusinessException(400, "班级名称不能为空");
        }
        if (!StringUtils.hasText(classInfo.getGrade())) {
            throw new BusinessException(400, "年级不能为空");
        }
        validateNameUnique(classInfo.getName(), classInfo.getGrade(), null);
        return super.save(classInfo);
    }

    @Override
    public void deleteClass(Long id) {
        long studentCount = studentMapper.selectCount(
                new LambdaQueryWrapper<Student>().eq(Student::getClassId, id));
        if (studentCount > 0) {
            throw new BusinessException(400, "该班级下存在 " + studentCount + " 名学生，无法删除");
        }
        removeById(id);
    }

    private void validateNameUnique(String name, String grade, Long excludeId) {
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getName, name)
                .eq(ClassInfo::getGrade, grade);
        if (excludeId != null) {
            wrapper.ne(ClassInfo::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BusinessException(400, "同年级下已存在同名班级");
        }
    }
}
