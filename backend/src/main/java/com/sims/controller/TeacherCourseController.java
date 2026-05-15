package com.sims.controller;

import com.sims.common.BusinessException;
import com.sims.common.Result;
import com.sims.entity.TeacherCourse;
import com.sims.service.TeacherCourseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher-courses")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final TeacherCourseService teacherCourseService;

    @GetMapping("/my")
    public Result<List<TeacherCourse>> myCourses(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return Result.ok(teacherCourseService.listByTeacher(userId));
    }

    @GetMapping
    public Result<List<TeacherCourse>> list(@RequestParam(required = false) Long teacherId,
                                            @RequestParam(required = false) String semester) {
        if (teacherId != null) {
            return Result.ok(semester != null
                    ? teacherCourseService.listByTeacherAndSemester(teacherId, semester)
                    : teacherCourseService.listByTeacher(teacherId));
        }
        return Result.ok(teacherCourseService.list());
    }

    @PostMapping
    public Result<TeacherCourse> create(@RequestBody TeacherCourse tc) {
        if (tc.getTeacherId() == null || tc.getCourseId() == null) {
            throw new BusinessException("教师和课程不能为空");
        }
        teacherCourseService.save(tc);
        return Result.ok(tc);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        teacherCourseService.removeById(id);
        return Result.ok();
    }
}
