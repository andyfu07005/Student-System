package com.sims.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.common.Result;
import com.sims.dto.EnrollRequest;
import com.sims.dto.EnrolledCourseVO;
import com.sims.dto.EnrolledStudentVO;
import com.sims.entity.Course;
import com.sims.service.CourseEnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollment")
@RequiredArgsConstructor
public class CourseEnrollmentController {

    private final CourseEnrollmentService enrollmentService;

    @PostMapping("/enroll")
    public Result<Void> enroll(HttpServletRequest request, @RequestBody EnrollRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        enrollmentService.enroll(userId, req.getCourseId());
        return Result.ok();
    }

    @PostMapping("/drop")
    public Result<Void> drop(HttpServletRequest request, @RequestBody EnrollRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        enrollmentService.drop(userId, req.getCourseId());
        return Result.ok();
    }

    @GetMapping("/available-courses")
    public Result<Page<Course>> listAvailableCourses(
            HttpServletRequest request,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(enrollmentService.listAvailableCourses(userId, keyword, type, page, size));
    }

    @GetMapping("/my-courses")
    public Result<Page<EnrolledCourseVO>> listMyCourses(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(enrollmentService.listMyCourses(userId, page, size));
    }

    @GetMapping("/course-students/{courseId}")
    public Result<Page<EnrolledStudentVO>> listCourseStudents(
            HttpServletRequest request,
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(enrollmentService.listCourseStudents(userId, courseId, page, size));
    }

    @GetMapping("/teaching-courses")
    public Result<Page<Course>> listTeachingCourses(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(enrollmentService.listTeachingCourses(userId, page, size));
    }
}
