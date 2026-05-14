package com.sims.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.entity.Student;

public interface StudentService extends IService<Student> {

    Page<Student> search(String keyword, Long classId, String status, int page, int size);

    @Override
    boolean save(Student student);

    boolean updateById(Student student);
}
