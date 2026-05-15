package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.common.BusinessException;
import com.sims.dto.EnrolledCourseVO;
import com.sims.dto.EnrolledStudentVO;
import com.sims.entity.*;
import com.sims.mapper.*;
import com.sims.service.CourseEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentServiceImpl extends ServiceImpl<CourseEnrollmentMapper, CourseEnrollment>
        implements CourseEnrollmentService {

    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void enroll(Long userId, Long courseId) {
        Student student = findStudentByUserId(userId);
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        int capacity = course.getCapacity() != null ? course.getCapacity() : 30;
        long enrolled = countEnrolled(courseId);
        if (enrolled >= capacity) {
            throw new BusinessException(400, "课程已满员，无法选课");
        }
        LambdaQueryWrapper<CourseEnrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseEnrollment::getStudentId, student.getId())
               .eq(CourseEnrollment::getCourseId, courseId)
               .eq(CourseEnrollment::getStatus, "ENROLLED");
        if (count(wrapper) > 0) {
            throw new BusinessException(400, "已选过该课程");
        }
        // Check if previously dropped and re-enroll
        LambdaQueryWrapper<CourseEnrollment> droppedWrapper = new LambdaQueryWrapper<>();
        droppedWrapper.eq(CourseEnrollment::getStudentId, student.getId())
                      .eq(CourseEnrollment::getCourseId, courseId)
                      .eq(CourseEnrollment::getStatus, "DROPPED");
        CourseEnrollment existing = getOne(droppedWrapper);
        if (existing != null) {
            existing.setStatus("ENROLLED");
            existing.setEnrolledAt(LocalDateTime.now());
            existing.setDroppedAt(null);
            updateById(existing);
        } else {
            CourseEnrollment enrollment = new CourseEnrollment();
            enrollment.setStudentId(student.getId());
            enrollment.setCourseId(courseId);
            enrollment.setStatus("ENROLLED");
            enrollment.setEnrolledAt(LocalDateTime.now());
            save(enrollment);
        }
    }

    @Override
    @Transactional
    public void drop(Long userId, Long courseId) {
        Student student = findStudentByUserId(userId);
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (course.getStartDate() != null && !course.getStartDate().isAfter(LocalDate.now())) {
            throw new BusinessException(400, "课程已开课，不可退选");
        }
        LambdaQueryWrapper<CourseEnrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseEnrollment::getStudentId, student.getId())
               .eq(CourseEnrollment::getCourseId, courseId)
               .eq(CourseEnrollment::getStatus, "ENROLLED");
        CourseEnrollment enrollment = getOne(wrapper);
        if (enrollment == null) {
            throw new BusinessException(400, "未选该课程，无法退选");
        }
        enrollment.setStatus("DROPPED");
        enrollment.setDroppedAt(java.time.LocalDateTime.now());
        updateById(enrollment);
    }

    @Override
    public Page<Course> listAvailableCourses(Long userId, String keyword, String type, int page, int size) {
        Student student = findStudentByUserId(userId);
        // Get all enrolled course IDs for this student
        LambdaQueryWrapper<CourseEnrollment> enrolledWrapper = new LambdaQueryWrapper<>();
        enrolledWrapper.eq(CourseEnrollment::getStudentId, student.getId())
                       .eq(CourseEnrollment::getStatus, "ENROLLED");
        List<CourseEnrollment> enrollments = list(enrolledWrapper);
        List<Long> enrolledCourseIds = enrollments.stream()
                .map(CourseEnrollment::getCourseId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Course::getName, keyword).or().like(Course::getCourseNo, keyword));
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Course::getType, type);
        }
        if (!enrolledCourseIds.isEmpty()) {
            wrapper.notIn(Course::getId, enrolledCourseIds);
        }
        wrapper.orderByDesc(Course::getCreatedAt);
        Page<Course> result = courseMapper.selectPage(new Page<>(page, size), wrapper);

        // Fill enrolled count and teacher name
        for (Course course : result.getRecords()) {
            course.setEnrolledCount((int) countEnrolled(course.getId()));
            if (course.getTeacherId() != null) {
                User teacher = userMapper.selectById(course.getTeacherId());
                if (teacher != null) {
                    course.setTeacherName(teacher.getRealName());
                }
            }
        }
        return result;
    }

    @Override
    public Page<EnrolledCourseVO> listMyCourses(Long userId, int page, int size) {
        Student student = findStudentByUserId(userId);
        LambdaQueryWrapper<CourseEnrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseEnrollment::getStudentId, student.getId())
               .eq(CourseEnrollment::getStatus, "ENROLLED")
               .orderByDesc(CourseEnrollment::getEnrolledAt);
        Page<CourseEnrollment> enrollmentPage = page(new Page<>(page, size), wrapper);

        List<Long> courseIds = enrollmentPage.getRecords().stream()
                .map(CourseEnrollment::getCourseId)
                .collect(Collectors.toList());
        Map<Long, Course> courseMap = Map.of();
        if (!courseIds.isEmpty()) {
            List<Course> courses = courseMapper.selectBatchIds(courseIds);
            courseMap = courses.stream().collect(Collectors.toMap(Course::getId, c -> c));
        }

        // Get teacher names
        Map<Long, String> teacherNameMap = Map.of();
        List<Long> teacherIds = courseMap.values().stream()
                .map(Course::getTeacherId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (!teacherIds.isEmpty()) {
            List<User> teachers = userMapper.selectBatchIds(teacherIds);
            teacherNameMap = teachers.stream().collect(Collectors.toMap(User::getId, User::getRealName));
        }

        Page<EnrolledCourseVO> result = new Page<>(page, size, enrollmentPage.getTotal());
        result.setRecords(enrollmentPage.getRecords().stream().map(e -> {
            EnrolledCourseVO vo = new EnrolledCourseVO();
            vo.setEnrollmentId(e.getId());
            vo.setCourseId(e.getCourseId());
            vo.setEnrolledAt(e.getEnrolledAt() != null ? e.getEnrolledAt().toString() : null);
            Course c = courseMap.get(e.getCourseId());
            if (c != null) {
                vo.setCourseNo(c.getCourseNo());
                vo.setCourseName(c.getName());
                vo.setType(c.getType());
                vo.setSemester(c.getSemester());
                vo.setCapacity(c.getCapacity());
                vo.setEnrolledCount((int) countEnrolled(c.getId()));
                if (c.getTeacherId() != null) {
                    vo.setTeacherName(teacherNameMap.get(c.getTeacherId()));
                }
            }
            return vo;
        }).collect(Collectors.toList()));
        return result;
    }

    @Override
    public Page<EnrolledStudentVO> listCourseStudents(Long userId, Long courseId, int page, int size) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (!userId.equals(course.getTeacherId())) {
            throw new BusinessException(403, "无权查看该课程的选课名单");
        }

        LambdaQueryWrapper<CourseEnrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseEnrollment::getCourseId, courseId)
               .eq(CourseEnrollment::getStatus, "ENROLLED")
               .orderByAsc(CourseEnrollment::getEnrolledAt);
        Page<CourseEnrollment> enrollmentPage = page(new Page<>(page, size), wrapper);

        List<Long> studentIds = enrollmentPage.getRecords().stream()
                .map(CourseEnrollment::getStudentId)
                .collect(Collectors.toList());
        Map<Long, Student> studentMap = Map.of();
        if (!studentIds.isEmpty()) {
            List<Student> students = studentMapper.selectBatchIds(studentIds);
            studentMap = students.stream().collect(Collectors.toMap(Student::getId, s -> s));
        }

        Page<EnrolledStudentVO> result = new Page<>(page, size, enrollmentPage.getTotal());
        result.setRecords(enrollmentPage.getRecords().stream().map(e -> {
            EnrolledStudentVO vo = new EnrolledStudentVO();
            vo.setStudentId(e.getStudentId());
            vo.setEnrolledAt(e.getEnrolledAt() != null ? e.getEnrolledAt().toString() : null);
            Student s = studentMap.get(e.getStudentId());
            if (s != null) {
                vo.setStudentNo(s.getStudentNo());
                vo.setStudentName(s.getName());
                vo.setGender(s.getGender());
                vo.setPhone(s.getPhone());
            }
            return vo;
        }).collect(Collectors.toList()));
        return result;
    }

    private Student findStudentByUserId(Long userId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getUserId, userId);
        Student student = studentMapper.selectOne(wrapper);
        if (student == null) {
            throw new BusinessException(400, "未找到关联的学生信息，请先完善个人资料");
        }
        return student;
    }

    @Override
    public Page<Course> listTeachingCourses(Long userId, int page, int size) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getTeacherId, userId).orderByDesc(Course::getCreatedAt);
        Page<Course> result = courseMapper.selectPage(new Page<>(page, size), wrapper);
        for (Course course : result.getRecords()) {
            course.setEnrolledCount((int) countEnrolled(course.getId()));
        }
        return result;
    }

    private long countEnrolled(Long courseId) {
        LambdaQueryWrapper<CourseEnrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseEnrollment::getCourseId, courseId)
               .eq(CourseEnrollment::getStatus, "ENROLLED");
        return count(wrapper);
    }
}
