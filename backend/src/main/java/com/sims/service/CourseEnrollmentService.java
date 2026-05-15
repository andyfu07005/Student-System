package com.sims.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.dto.EnrolledCourseVO;
import com.sims.dto.EnrolledStudentVO;
import com.sims.entity.Course;
import com.sims.entity.CourseEnrollment;

public interface CourseEnrollmentService extends IService<CourseEnrollment> {

    void enroll(Long userId, Long courseId);

    void drop(Long userId, Long courseId);

    Page<Course> listAvailableCourses(Long userId, String keyword, String type, int page, int size);

    Page<EnrolledCourseVO> listMyCourses(Long userId, int page, int size);

    Page<EnrolledStudentVO> listCourseStudents(Long userId, Long courseId, int page, int size);

    Page<Course> listTeachingCourses(Long userId, int page, int size);
}
