package com.sims.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.common.BusinessException;
import com.sims.common.Result;
import com.sims.entity.Grade;
import com.sims.entity.Student;
import com.sims.entity.TeacherCourse;
import com.sims.mapper.StudentMapper;
import com.sims.mapper.TeacherCourseMapper;
import com.sims.service.GradeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final StudentMapper studentMapper;
    private final TeacherCourseMapper teacherCourseMapper;

    /** 学生查看自己的成绩 */
    @GetMapping("/my")
    public Result<Page<Grade>> myGrades(
            @RequestParam(defaultValue = "") String semester,
            @RequestParam(defaultValue = "") String academicYear,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        String roleCode = (String) req.getAttribute("roleCode");
        assertStudentRole(roleCode);
        Student student = findStudentByUserId(userId);
        return Result.ok(gradeService.listStudentGrades(student.getId(), semester, academicYear, page, size));
    }

    /** 学生查看自己的成绩统计 */
    @GetMapping("/my/statistics")
    public Result<Map<String, Object>> myStatistics(
            @RequestParam(defaultValue = "") String academicYear,
            HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        String roleCode = (String) req.getAttribute("roleCode");
        assertStudentRole(roleCode);
        Student student = findStudentByUserId(userId);
        return Result.ok(gradeService.studentStatistics(student.getId(), academicYear));
    }

    /** 教师查看所授课程的成绩 */
    @GetMapping("/course/{courseId}")
    public Result<Page<Grade>> courseGrades(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "") String semester,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        String roleCode = (String) req.getAttribute("roleCode");
        if ("TEACHER".equals(roleCode)) {
            assertTeachingCourse(userId, courseId);
        }
        return Result.ok(gradeService.listCourseGrades(courseId, semester, page, size));
    }

    /** 教师查看所授课程的成绩统计 */
    @GetMapping("/course/{courseId}/statistics")
    public Result<Map<String, Object>> courseStatistics(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "") String semester,
            HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        String roleCode = (String) req.getAttribute("roleCode");
        if ("TEACHER".equals(roleCode)) {
            assertTeachingCourse(userId, courseId);
        }
        return Result.ok(gradeService.courseStatistics(courseId, semester));
    }

    /** 教师查看所授课程的成绩分布 */
    @GetMapping("/course/{courseId}/distribution")
    public Result<List<Map<String, Object>>> courseDistribution(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "") String semester,
            HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        String roleCode = (String) req.getAttribute("roleCode");
        if ("TEACHER".equals(roleCode)) {
            assertTeachingCourse(userId, courseId);
        }
        return Result.ok(gradeService.scoreDistribution(courseId, semester));
    }

    /** 管理员查看全局统计 */
    @GetMapping("/admin/statistics")
    public Result<Map<String, Object>> adminStatistics(
            @RequestParam(defaultValue = "") String academicYear,
            HttpServletRequest req) {
        assertAdminRole((String) req.getAttribute("roleCode"));
        return Result.ok(gradeService.adminStatistics(academicYear));
    }

    /** 按学生ID查看成绩（管理员/教师） */
    @GetMapping("/student/{studentId}")
    public Result<Page<Grade>> studentGrades(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(gradeService.listByStudentId(studentId, page, size));
    }

    /** 录入成绩 */
    @PostMapping
    public Result<Grade> create(@RequestBody Grade grade, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        String roleCode = (String) req.getAttribute("roleCode");
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            throw new BusinessException("无权录入成绩");
        }
        return Result.ok(gradeService.save(grade, userId));
    }

    /** 修改成绩 */
    @PutMapping("/{id}")
    public Result<Grade> update(@PathVariable Long id, @RequestBody Grade grade) {
        return Result.ok(gradeService.update(id, grade));
    }

    /** 删除成绩 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        gradeService.delete(id);
        return Result.ok();
    }

    private void assertStudentRole(String roleCode) {
        if (!"STUDENT".equals(roleCode)) {
            throw new BusinessException("仅学生角色可访问此接口");
        }
    }

    private void assertAdminRole(String roleCode) {
        if (!"ADMIN".equals(roleCode)) {
            throw new BusinessException("仅管理员角色可访问此接口");
        }
    }

    private Student findStudentByUserId(Long userId) {
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>().eq(Student::getUserId, userId));
        if (student == null) {
            throw new BusinessException("未找到关联的学生信息，请联系管理员");
        }
        return student;
    }

    private void assertTeachingCourse(Long teacherId, Long courseId) {
        long count = teacherCourseMapper.selectCount(
                new LambdaQueryWrapper<TeacherCourse>()
                        .eq(TeacherCourse::getTeacherId, teacherId)
                        .eq(TeacherCourse::getCourseId, courseId));
        if (count == 0) {
            throw new BusinessException("您未教授此课程，无权查看");
        }
    }
}
