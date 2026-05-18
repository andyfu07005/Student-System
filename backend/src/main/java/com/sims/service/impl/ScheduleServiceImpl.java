package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.common.BusinessException;
import com.sims.dto.ScheduleDTO;
import com.sims.entity.Course;
import com.sims.entity.Schedule;
import com.sims.entity.User;
import com.sims.mapper.CourseMapper;
import com.sims.mapper.ScheduleMapper;
import com.sims.mapper.UserMapper;
import com.sims.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

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
        // 检查同一教师同一时段冲突
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

        // 检查同一教室同一时段冲突
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
}
