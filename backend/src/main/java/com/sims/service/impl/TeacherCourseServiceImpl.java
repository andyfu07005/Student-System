package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.entity.TeacherCourse;
import com.sims.mapper.TeacherCourseMapper;
import com.sims.service.TeacherCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherCourseServiceImpl extends ServiceImpl<TeacherCourseMapper, TeacherCourse>
        implements TeacherCourseService {

    @Override
    public List<TeacherCourse> listByTeacher(Long teacherId) {
        LambdaQueryWrapper<TeacherCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherCourse::getTeacherId, teacherId);
        return list(wrapper);
    }

    @Override
    public List<TeacherCourse> listByTeacherAndSemester(Long teacherId, String semester) {
        LambdaQueryWrapper<TeacherCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherCourse::getTeacherId, teacherId);
        if (semester != null && !semester.isBlank()) {
            wrapper.eq(TeacherCourse::getSemester, semester);
        }
        return list(wrapper);
    }
}
