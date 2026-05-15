package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sims.common.BusinessException;
import com.sims.dto.schedule.ScheduleItem;
import com.sims.dto.schedule.WeekSchedule;
import com.sims.entity.*;
import com.sims.mapper.*;
import com.sims.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final StudentMapper studentMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @Override
    public WeekSchedule getWeekSchedule(Long userId, LocalDate date) {
        String roleCode = getUserRoleCode(userId);

        LocalDate weekStart = date.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = date.with(DayOfWeek.SUNDAY);

        List<ScheduleItem> items;
        if ("STUDENT".equals(roleCode)) {
            items = getStudentSchedule(userId);
        } else if ("TEACHER".equals(roleCode)) {
            items = getTeacherSchedule(userId);
        } else {
            items = Collections.emptyList();
        }

        int weekOfYear = date.get(WeekFields.ISO.weekOfYear());
        String weekLabel = String.format("第%d周 (%s ~ %s)",
                weekOfYear,
                weekStart.format(DateTimeFormatter.ofPattern("MM/dd")),
                weekEnd.format(DateTimeFormatter.ofPattern("MM/dd")));

        return WeekSchedule.builder()
                .weekLabel(weekLabel)
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .items(items)
                .build();
    }

    private String getUserRoleCode(Long userId) {
        LambdaQueryWrapper<UserRole> urQw = new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(urQw);
        if (userRoles.isEmpty()) {
            return "";
        }
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List<Role> roles = roleMapper.selectBatchIds(roleIds);
        if (roles.stream().anyMatch(r -> "ADMIN".equals(r.getRoleCode()))) {
            return "ADMIN";
        }
        if (roles.stream().anyMatch(r -> "TEACHER".equals(r.getRoleCode()))) {
            return "TEACHER";
        }
        return "STUDENT";
    }

    private List<ScheduleItem> getStudentSchedule(Long userId) {
        // 查找学生记录
        LambdaQueryWrapper<Student> studentQw = new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId);
        Student student = studentMapper.selectOne(studentQw);
        if (student == null) {
            return Collections.emptyList();
        }

        // 查找学生选课
        LambdaQueryWrapper<StudentCourse> scQw = new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getStudentId, student.getId());
        List<StudentCourse> enrollments = studentCourseMapper.selectList(scQw);
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = enrollments.stream()
                .map(StudentCourse::getCourseId)
                .collect(Collectors.toList());

        // 查找课程安排
        LambdaQueryWrapper<CourseSchedule> csQw = new LambdaQueryWrapper<CourseSchedule>()
                .in(CourseSchedule::getCourseId, courseIds)
                .orderByAsc(CourseSchedule::getDayOfWeek)
                .orderByAsc(CourseSchedule::getStartTime);
        List<CourseSchedule> schedules = courseScheduleMapper.selectList(csQw);

        return buildScheduleItems(schedules);
    }

    private List<ScheduleItem> getTeacherSchedule(Long userId) {
        // 教师直接通过 teacher_id 查找
        LambdaQueryWrapper<CourseSchedule> csQw = new LambdaQueryWrapper<CourseSchedule>()
                .eq(CourseSchedule::getTeacherId, userId)
                .orderByAsc(CourseSchedule::getDayOfWeek)
                .orderByAsc(CourseSchedule::getStartTime);
        List<CourseSchedule> schedules = courseScheduleMapper.selectList(csQw);

        return buildScheduleItems(schedules);
    }

    private List<ScheduleItem> buildScheduleItems(List<CourseSchedule> schedules) {
        if (schedules.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = schedules.stream()
                .map(CourseSchedule::getCourseId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> teacherIds = schedules.stream()
                .map(CourseSchedule::getTeacherId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        Map<Long, User> teacherMap = userMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<ScheduleItem> items = new ArrayList<>();
        for (CourseSchedule cs : schedules) {
            Course course = courseMap.get(cs.getCourseId());
            User teacher = teacherMap.get(cs.getTeacherId());
            items.add(ScheduleItem.builder()
                    .id(cs.getId())
                    .courseId(cs.getCourseId())
                    .courseNo(course != null ? course.getCourseNo() : "")
                    .courseName(course != null ? course.getName() : "未知课程")
                    .teacherName(teacher != null ? teacher.getRealName() : "未知教师")
                    .classroom(cs.getClassroom())
                    .dayOfWeek(cs.getDayOfWeek())
                    .startTime(cs.getStartTime())
                    .endTime(cs.getEndTime())
                    .startWeek(cs.getStartWeek())
                    .endWeek(cs.getEndWeek())
                    .build());
        }
        return items;
    }
}
