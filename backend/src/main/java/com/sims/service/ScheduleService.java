package com.sims.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.dto.ScheduleDTO;
import com.sims.dto.schedule.WeekSchedule;
import com.sims.entity.Schedule;

import java.time.LocalDate;

public interface ScheduleService extends IService<Schedule> {

    Page<Schedule> search(Long courseId, Long teacherId, String classroom, Integer dayOfWeek, int page, int size);

    Schedule create(ScheduleDTO dto);

    Schedule update(Long id, ScheduleDTO dto);

    WeekSchedule getWeekSchedule(Long userId, LocalDate date);
}
