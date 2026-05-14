package com.sims.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.entity.Course;

public interface CourseService extends IService<Course> {

    Page<Course> search(String keyword, String type, String major, int page, int size);

    @Override
    boolean save(Course course);
}
