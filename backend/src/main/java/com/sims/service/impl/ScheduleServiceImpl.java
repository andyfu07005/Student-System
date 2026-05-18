package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.common.BusinessException;
import com.sims.dto.ScheduleDTO;
import com.sims.dto.schedule.ScheduleItem;
import com.sims.dto.schedule.WeekSchedule;
import com.sims.entity.*;
import com.sims.mapper.*;
import com.sims.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final StudentMapper studentMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @Override
    public Page<Schedule> search(Long courseId, Long teacherId, String classroom, Integer dayOfWeek, int page, int size) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Schedule::getCourseId, courseId);
        }
        if (teacherId != null) {
            wrapper.eq(Schedule::getTeacherId, teacherId);
        }
        if (StringUtils.hasText(classroom)) {
            wrapper.like(Schedule::getClassroom, classroom);
        }
        if (dayOfWeek != null) {
            wrapper.eq(Schedule::getDayOfWeek, dayOfWeek);
        }
        wrapper.orderByAsc(Schedule::getDayOfWeek, Schedule::getStartTime);
        Page<Schedule> result = page(new Page<>(page, size), wrapper);

        for (Schedule s : result.getRecords()) {
            Course course = courseMapper.selectById(s.getCourseId());
            if (course != null) {
                s.setCourseName(course.getName());
            }
            User teacher = userMapper.selectById(s.getTeacherId());
            if (teacher != null) {
                s.setTeacherName(teacher.getRealName());
            }
        }
        return result;
    }

    @Override
    public Schedule create(ScheduleDTO dto) {
        validateTimeOrder(dto.getStartTime(), dto.getEndTime());
        validateNoConflict(null, dto.getTeacherId(), dto.getClassroom(), dto.getDayOfWeek(), dto.getStartTime(), dto.getEndTime());

        Schedule schedule = toEntity(dto);
        save(schedule);
        fillNames(schedule);
        return schedule;
    }

    @Override
    public Schedule update(Long id, ScheduleDTO dto) {
        validateTimeOrder(dto.getStartTime(), dto.getEndTime());
        validateNoConflict(id, dto.getTeacherId(), dto.getClassroom(), dto.getDayOfWeek(), dto.getStartTime(), dto.getEndTime());

        Schedule schedule = toEntity(dto);
        schedule.setId(id);
        updateById(schedule);
        fillNames(schedule);
        return schedule;
    }

    private void validateTimeOrder(java.time.LocalTime start, java.time.LocalTime end) {
        if (!start.isBefore(end)) {
            throw new BusinessException(400, "开始时间必须早于结束时间");
        }
    }

    private void validateNoConflict(Long excludeId, Long teacherId, String classroom, Integer dayOfWeek,
                                     java.time.LocalTime startTime, java.time.LocalTime endTime) {
        LambdaQueryWrapper<Schedule> teacherWrapper = new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getTeacherId, teacherId)
                .eq(Schedule::getDayOfWeek, dayOfWeek);
        if (excludeId != null) {
            teacherWrapper.ne(Schedule::getId, excludeId);
        }
        List<Schedule> teacherSchedules = list(teacherWrapper);
        for (Schedule s : teacherSchedules) {
            if (isTimeOverlap(startTime, endTime, s.getStartTime(), s.getEndTime())) {
                User teacher = userMapper.selectById(teacherId);
                String name = teacher != null ? teacher.getRealName() : "未知";
                throw new BusinessException(400,
                        String.format("教师【%s】在星期%d %s-%s 已有排课，存在时间冲突",
                                name, dayOfWeek, s.getStartTime(), s.getEndTime()));
            }
        }

        LambdaQueryWrapper<Schedule> roomWrapper = new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getClassroom, classroom)
                .eq(Schedule::getDayOfWeek, dayOfWeek);
        if (excludeId != null) {
            roomWrapper.ne(Schedule::getId, excludeId);
        }
        List<Schedule> roomSchedules = list(roomWrapper);
        for (Schedule s : roomSchedules) {
            if (isTimeOverlap(startTime, endTime, s.getStartTime(), s.getEndTime())) {
                throw new BusinessException(400,
                        String.format("教室【%s】在星期%d %s-%s 已被占用，存在时间冲突",
                                classroom, dayOfWeek, s.getStartTime(), s.getEndTime()));
            }
        }
    }

    private boolean isTimeOverlap(java.time.LocalTime s1, java.time.LocalTime e1,
                                   java.time.LocalTime s2, java.time.LocalTime e2) {
        return s1.isBefore(e2) && e1.isAfter(s2);
    }

    private Schedule toEntity(ScheduleDTO dto) {
        Schedule schedule = new Schedule();
        schedule.setCourseId(dto.getCourseId());
        schedule.setTeacherId(dto.getTeacherId());
        schedule.setClassroom(dto.getClassroom());
        schedule.setDayOfWeek(dto.getDayOfWeek());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setCapacity(dto.getCapacity() != null ? dto.getCapacity() : 30);
        return schedule;
    }

    private void fillNames(Schedule schedule) {
        Course course = courseMapper.selectById(schedule.getCourseId());
        if (course != null) {
            schedule.setCourseName(course.getName());
        }
        User teacher = userMapper.selectById(schedule.getTeacherId());
        if (teacher != null) {
            schedule.setTeacherName(teacher.getRealName());
        }
    }

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
        LambdaQueryWrapper<Student> studentQw = new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId);
        Student student = studentMapper.selectOne(studentQw);
        if (student == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<StudentCourse> scQw = new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getStudentId, student.getId());
        List<StudentCourse> enrollments = studentCourseMapper.selectList(scQw);
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = enrollments.stream()
                .map(StudentCourse::getCourseId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<CourseSchedule> csQw = new LambdaQueryWrapper<CourseSchedule>()
                .in(CourseSchedule::getCourseId, courseIds)
                .orderByAsc(CourseSchedule::getDayOfWeek)
                .orderByAsc(CourseSchedule::getStartTime);
        List<CourseSchedule> schedules = courseScheduleMapper.selectList(csQw);

        return buildScheduleItems(schedules);
    }

    private List<ScheduleItem> getTeacherSchedule(Long userId) {
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
