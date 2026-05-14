package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.common.BusinessException;
import com.sims.entity.Course;
import com.sims.mapper.CourseMapper;
import com.sims.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Override
    public Page<Course> search(String keyword, String type, String major, int page, int size) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Course::getName, keyword).or().like(Course::getCourseNo, keyword));
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Course::getType, type);
        }
        if (StringUtils.hasText(major)) {
            wrapper.eq(Course::getMajor, major);
        }
        wrapper.orderByDesc(Course::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public boolean save(Course course) {
        if (!StringUtils.hasText(course.getCourseNo())) {
            throw new BusinessException(400, "课程编号不能为空");
        }
        if (!StringUtils.hasText(course.getName())) {
            throw new BusinessException(400, "课程名称不能为空");
        }
        validateCourseNoUnique(course.getCourseNo(), course.getId());
        return super.save(course);
    }

    private void validateCourseNoUnique(String courseNo, Long excludeId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>().eq(Course::getCourseNo, courseNo);
        if (excludeId != null) {
            wrapper.ne(Course::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BusinessException(400, "课程编号已存在");
        }
    }
}
