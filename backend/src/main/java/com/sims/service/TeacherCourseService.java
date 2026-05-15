package com.sims.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.entity.TeacherCourse;

import java.util.List;

public interface TeacherCourseService extends IService<TeacherCourse> {

    List<TeacherCourse> listByTeacher(Long teacherId);

    List<TeacherCourse> listByTeacherAndSemester(Long teacherId, String semester);
}
